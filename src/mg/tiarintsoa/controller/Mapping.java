package mg.tiarintsoa.controller;

import jakarta.servlet.http.HttpServletRequest;
import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.annotation.RequestSubParameter;
import mg.tiarintsoa.annotation.RestAPI;
import mg.tiarintsoa.reflection.Reflect;
import mg.tiarintsoa.session.WinterSession;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Mapping {

    private Class<?> controller;
    private Method method;
    private Object controllerInstance;
    private static final WinterSession winterSession = new WinterSession();

    public Mapping(Class<?> controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    public Class<?> getController() {
        return controller;
    }

    public void setController(Class<?> controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public boolean isRestAPI() {
        return controller.isAnnotationPresent(RestAPI.class);
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

    public Object executeMethod(HttpServletRequest request) throws Exception {
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
