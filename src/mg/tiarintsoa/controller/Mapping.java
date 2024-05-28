package mg.tiarintsoa.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
}
