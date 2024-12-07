package mg.tiarintsoa.controller;

import jakarta.servlet.http.HttpServletRequest;
import mg.tiarintsoa.annotation.*;
import mg.tiarintsoa.enumeration.RequestVerb;
import mg.tiarintsoa.exception.MissingErrorUrlException;
import mg.tiarintsoa.exception.VerbNotFoundException;
import mg.tiarintsoa.reflection.Reflect;
import mg.tiarintsoa.session.WinterSession;
import mg.tiarintsoa.validation.FieldErrors;
import mg.tiarintsoa.validation.ParameterValidator;

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

    public Class<?> getController() {
        return controller;
    }

    public void addVerbMapping(RequestVerb verb, Method method, String url) throws Exception {
        if (methods.containsKey(verb)) throw new Exception("The url and verb (" + url + ", " + verb + ") cannot be mapped more than one time");
        methods.put(verb, method);
    }

    public String getErrorUrl(RequestVerb verb) throws MissingErrorUrlException {
        if (!methods.get(verb).isAnnotationPresent(ErrorUrl.class))
            throw new MissingErrorUrlException();

        ErrorUrl errorUrl = methods.get(verb).getAnnotation(ErrorUrl.class);
        return errorUrl.value();
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

    private Object getValueFromParameterName(HttpServletRequest request, Class<?> parameterClass, String parameterName) throws Exception {
        if (parameterClass.equals(Integer.class) || parameterClass.equals(int.class)) {
            try {
                return Integer.parseInt(request.getParameter(parameterName));
            } catch (NumberFormatException e) {
                return parameterClass.equals(int.class) ? 0 : null;
            }
        }

        if (parameterClass.equals(Double.class) || parameterClass.equals(double.class)) {
            try {
                return Double.parseDouble(request.getParameter(parameterName));
            } catch (NumberFormatException e) {
                return parameterClass.equals(double.class) ? 0.0 : null;
            }
        }

        if (parameterClass.equals(String.class)) {
            return request.getParameter(parameterName);
        }

        Object newValue = null;
        for(Field field: parameterClass.getDeclaredFields()) {
            Object parameterSubValue = null;

            if (field.isAnnotationPresent(RequestParameter.class)) {
                RequestParameter annotation = field.getAnnotation(RequestParameter.class);
                parameterSubValue = getValueFromParameterName(request, field.getType(), parameterName + "." + annotation.value());
            } else if (field.isAnnotationPresent(RequestFile.class)) {
                RequestFile annotation = field.getAnnotation(RequestFile.class);
                parameterSubValue = getPartFromFileName(request, field.getType(), parameterName + "." + annotation.value());
            }

            if (parameterSubValue != null) {
                if (newValue == null) newValue = Reflect.createInstance(parameterClass);
                field.setAccessible(true);
                field.set(newValue, parameterSubValue);
                field.setAccessible(false);
            }
        }

        return newValue;
    }

    private WinterPart getPartFromFileName(HttpServletRequest request, Class<?> parameterClass, String fileName) throws Exception {
        if (!parameterClass.equals(WinterPart.class)) {
            throw new Exception("Parameter annotated with @RequestFile should be of type WinterPart");
        }

        return new WinterPart(request.getPart(fileName));
    }

    private Object getRequestParameterValue(HttpServletRequest request, Parameter parameter, FieldErrors fieldErrors) throws Exception {
        Object value;
        Class<?> parameterClass = parameter.getType();

        if (parameter.isAnnotationPresent(RequestParameter.class)) {
            RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
            String parameterName = requestParameter.value();
            value = getValueFromParameterName(request, parameterClass, parameterName);
        } else if (parameter.isAnnotationPresent(RequestFile.class)) {
            RequestFile requestFile = parameter.getAnnotation(RequestFile.class);
            String fileName = requestFile.value();
            value = getPartFromFileName(request, parameterClass, fileName);
        } else if (parameterClass.equals(FieldErrors.class)) {
            value = fieldErrors;
        } else {
            throw new Exception("ETU003057: parameter \"" + parameter.getName() + "\" doesn't have the annotation @RequestParameter or @RequestFile");
        }

        return value;
    }

    public Object executeMethod(HttpServletRequest request, RequestVerb verb, String url, FieldErrors fieldErrors) throws Exception {
        Method method = methods.get(verb);
        if (method == null) throw new VerbNotFoundException("The URL \"" + url + "\" is not associated with the verb " + verb);

        Object controllerInstance = getControllerInstance();
        Parameter[] parameters = method.getParameters();
        Object[] parametersValues = new Object[parameters.length];

        // Sets the session instance according to the user
        winterSession.setSession(request.getSession());

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            parametersValues[i] = getRequestParameterValue(request, parameter, fieldErrors);
            ParameterValidator.validateParameter(parametersValues[i], parameter, fieldErrors);
        }

        return method.invoke(controllerInstance, parametersValues);
    }
}
