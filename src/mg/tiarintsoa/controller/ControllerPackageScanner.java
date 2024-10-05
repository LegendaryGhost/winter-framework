package mg.tiarintsoa.controller;

import jakarta.servlet.ServletException;
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.UrlMapping;
import mg.tiarintsoa.enumeration.RequestVerb;
import mg.tiarintsoa.reflection.Reflect;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class ControllerPackageScanner {

    public static HashMap<String, Mapping> scan(String controllersPackage) throws ServletException {
        if (controllersPackage == null || controllersPackage.isEmpty()) {
            throw new ServletException("The controllers_package parameter is empty. Please check your web.xml file.");
        }

        HashMap<String, Mapping> urlMappings = new HashMap<>();
        List<Class<?>> controllers = getAnnotatedControllers(controllersPackage);
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                if (isEndPointMethod(method)) {
                    String url = getMappedUrl(method);
                    RequestVerb verb = getMappedVerb(method);

                    Mapping mapping = urlMappings.get(url);
                    if(mapping == null) {
                        mapping = new Mapping(controller);
                        urlMappings.put(url, mapping);
                    }

                    if (!mapping.isRestAPI(verb)) validateMethodReturnType(method, controller);

                    try {
                        mapping.addVerbMapping(verb, method, url);
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
                }
            }
        }

        return urlMappings;
    }

    private static void validateMethodReturnType(Method method, Class<?> controller) throws ServletException {
        if (!isValidReturnType(method)) {
            throw new ServletException("Unsupported return type for method " + method.getName() + " in controller " + controller.getName());
        }
    }

    private static boolean isValidReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        return returnType.equals(String.class) || returnType.equals(ModelView.class);
    }

    private static List<Class<?>> getAnnotatedControllers(String controllersPackage) throws ServletException {
        try {
            return Reflect.getAnnotatedClasses(controllersPackage, Controller.class);
        } catch (IOException | ClassNotFoundException e) {
            throw new ServletException(e);
        }
    }

    private static boolean isEndPointMethod(Method method) {
        return method.isAnnotationPresent(UrlMapping.class);
    }

    private static String getMappedUrl(Method method) {
        UrlMapping url = method.getAnnotation(UrlMapping.class);
        return url.value();
    }

    private static RequestVerb getMappedVerb(Method method) {
        return method.getAnnotation(UrlMapping.class).verb();
    }

}
