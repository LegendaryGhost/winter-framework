package com.tiarintsoa.controller;

import jakarta.servlet.ServletException;
import com.tiarintsoa.annotation.Controller;
import com.tiarintsoa.annotation.Post;
import com.tiarintsoa.annotation.UrlMapping;
import com.tiarintsoa.enumeration.RequestVerb;
import com.tiarintsoa.reflection.Reflect;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class ControllerPackageScanner {

    public static HashMap<String, Mapping> scan(String controllersPackage) throws ServletException {
        if (controllersPackage == null || controllersPackage.isEmpty()) {
            throw new ServletException("The controllers_package parameter is empty. Please check your web.xml file and add the \"controllers_package\" init variable.");
        }

        HashMap<String, Mapping> urlMappings = new HashMap<>();
        List<Class<?>> controllers = getAnnotatedControllers(controllersPackage);
        for (Class<?> controller : controllers) {
            String controllerUrl = controller.isAnnotationPresent(UrlMapping.class) ? controller.getAnnotation(UrlMapping.class).value() : "";
            for (Method method : controller.getDeclaredMethods()) {
                if (isEndPointMethod(method)) {
                    String methodUrl = getMappedUrl(method);
                    String fullUrl = controllerUrl + methodUrl;
                    RequestVerb verb = getMappedVerb(method);

                    Mapping mapping = urlMappings.get(fullUrl);
                    if(mapping == null) {
                        mapping = new Mapping(controller);
                        urlMappings.put(fullUrl, mapping);
                    }

                    if (!mapping.getController().equals(controller)) {
                        throw new ServletException("The URL \"" + fullUrl + "\" should not be mapped in 2 different controllers.");
                    }

                    try {
                        mapping.addVerbMapping(verb, method, fullUrl);
                        if (!mapping.isRestAPI(verb)) validateMethodReturnType(method, controller);
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
        return method.isAnnotationPresent(Post.class) ? RequestVerb.POST : RequestVerb.GET;
    }

}
