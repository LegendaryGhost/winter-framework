package mg.tiarintsoa.controller;

import jakarta.servlet.http.HttpServletRequest;
import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.annotation.RequestSubParameter;
import mg.tiarintsoa.annotation.RestController;
import mg.tiarintsoa.annotation.RestEndPoint;
import mg.tiarintsoa.enumeration.RequestVerb;
import mg.tiarintsoa.exception.VerbNotFoundException;
import mg.tiarintsoa.reflection.Reflect;
import mg.tiarintsoa.session.WinterSession;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

public class Mapping {

    private final Class<?> controller;
    private final HashMap<RequestVerb, Method> methods = new HashMap<>();
    private Object controllerInstance;
    private static final WinterSession winterSession = new WinterSession();

    public Mapping(Class<?> controller) {
        this.controller = controller;
    }

    public void addVerbMapping(RequestVerb verb, Method method, String url) throws Exception {
        if (methods.containsKey(verb)) throw new Exception("The url and verb (" + url + ", " + verb + ") cannot be mapped more than one time");
        methods.put(verb, method);
    }

    public boolean isRestAPI(RequestVerb verb) {
        return methods.get(verb).isAnnotationPresent(RestEndPoint.class) || controller.isAnnotationPresent(RestController.class);
    }

    public Object getControllerInstance() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (controllerInstance == null) {
            controllerInstance = Reflect.createInstance(controller);
            Field[] fields = controller.getDeclaredFields();
            for (Field field: fields) {
                Class<?> fieldType = field.getType();
                if (fieldType.equals(WinterSession.class)) {
                    field.setAccessible(true);
                    field.set(controllerInstance, winterSession);
                    field.setAccessible(false);
                }
            }
        }
        return controllerInstance;
    }

    private Object getValueFromParameterName(HttpServletRequest request, Class<?> parameterClass, String parameterName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        if (parameterClass.equals(String.class)) {
            return request.getParameter(parameterName);
        }

        Object newValue = null;
        for(Field field: parameterClass.getDeclaredFields()) {
            String parameterSubValue = null;

            if (field.isAnnotationPresent(RequestSubParameter.class)) {
                RequestSubParameter annotation = field.getAnnotation(RequestSubParameter.class);
                parameterSubValue = request.getParameter(parameterName + "." + annotation.value());
            }

            String conventionSubValue = request.getParameter(parameterName + "." + field.getName());
            parameterSubValue = conventionSubValue == null ? parameterSubValue : conventionSubValue;

            if (parameterSubValue != null) {
                if (newValue == null) newValue = Reflect.createInstance(parameterClass);
                field.setAccessible(true);
                field.set(newValue, parameterSubValue);
                field.setAccessible(false);
            }
        }

        return newValue;
    }

    private Object getRequestParameterValue(HttpServletRequest request, Parameter parameter) throws Exception {
        Object value;
        Class<?> parameterClass = parameter.getType();

        if (!parameter.isAnnotationPresent(RequestParameter.class)) {
            throw new Exception("ETU003057: parameter \"" + parameter.getName() + "\" doesn't have the annotation @RequestParameter");
        }

        RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
        String parameterName = requestParameter.value();
        value = getValueFromParameterName(request, parameterClass, parameterName);

        return value;
    }

    public Object executeMethod(HttpServletRequest request, RequestVerb verb, String url) throws Exception {
        Method method = methods.get(verb);
        if (method == null) throw new VerbNotFoundException("The URL \"" + url + "\" is not associated with the verb " + verb);

        Object controllerInstance = getControllerInstance();
        Parameter[] parameters = method.getParameters();
        Object[] parametersValues = new Object[parameters.length];

        // Sets the session instance according to the user
        winterSession.setSession(request.getSession());

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            parametersValues[i] = getRequestParameterValue(request, parameter);
        }

        return method.invoke(controllerInstance, parametersValues);
    }
}
