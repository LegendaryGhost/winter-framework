package mg.tiarintsoa.controller;

import jakarta.servlet.http.HttpServletRequest;
import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.annotation.RequestSubParameter;
import mg.tiarintsoa.reflection.Reflect;

import java.lang.reflect.*;

public class Mapping {

    private Class<?> controller;
    private Method method;
    private Object controllerInstance;

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

    public Object getControllerInstance() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (controllerInstance == null) {
            controllerInstance = Reflect.createInstance(controller);
        }
        return controllerInstance;
    }

    private Object getValueFromParameterName(HttpServletRequest request, Object previousValue, Class<?> parameterClass, String parameterName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        if (parameterClass.equals(String.class)) {
            String newValue = request.getParameter(parameterName);
            return newValue == null ? previousValue : newValue;
        }

        Object newValue = previousValue;
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

    private Object getRequestParameterValue(HttpServletRequest request, Parameter parameter) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Object value = null;
        Class<?> parameterClass = parameter.getType();

        if (parameter.isAnnotationPresent(RequestParameter.class)) {
            RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
            String parameterName = requestParameter.value();
            value = getValueFromParameterName(request, null, parameterClass, parameterName);
        }

        // Override the annotation based values if convention values are found
        String parameterName = parameter.getName();
        value = getValueFromParameterName(request, value, parameterClass, parameterName);

        return value;
    }

    public Object executeMethod(HttpServletRequest request) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Object controllerInstance = getControllerInstance();
        Parameter[] parameters = method.getParameters();
        Object[] parametersValues = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            parametersValues[i] = getRequestParameterValue(request, parameter);
        }

        return method.invoke(controllerInstance, parametersValues);
    }
}
