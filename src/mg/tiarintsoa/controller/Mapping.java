package mg.tiarintsoa.controller;

import jakarta.servlet.http.HttpServletRequest;
import mg.tiarintsoa.annotation.RequestParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
            Constructor<?> constructor = controller.getDeclaredConstructor();
            constructor.setAccessible(true);
            controllerInstance = constructor.newInstance();
        }
        return controllerInstance;
    }

    private String getParameterValue(HttpServletRequest request, Parameter parameter) {
        String value = request.getParameter(parameter.getName());

        if (value == null && parameter.isAnnotationPresent(RequestParameter.class)) {
            RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
            value = request.getParameter(requestParameter.value());
        }

        return value;
    }

    public Object executeMethod(HttpServletRequest request) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Object controllerInstance = getControllerInstance();
        Parameter[] parameters = method.getParameters();
        Object[] parametersValues = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            parametersValues[i] = getParameterValue(request, parameter);
        }

        return method.invoke(controllerInstance, parametersValues);
    }
}
