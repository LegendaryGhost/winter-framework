package mg.tiarintsoa.controller;

import jakarta.servlet.http.HttpServletRequest;

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

    public Object executeMethod(HttpServletRequest request) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Object controllerInstance = getControllerInstance();
        Parameter[] parameters = method.getParameters();
        Object[] parametersValues = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            String parameterName = parameters[i].getName();
            Object parameterValue = request.getParameter(parameterName);
            parametersValues[i] = parameterValue;
        }

        return method.invoke(controllerInstance, parametersValues);
    }
}
