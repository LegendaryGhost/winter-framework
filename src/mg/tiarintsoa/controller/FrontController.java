package mg.tiarintsoa.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.GetMapping;
import mg.tiarintsoa.reflection.Reflect;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class FrontController extends HttpServlet {

    private final HashMap<String, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        scanControllersPackage();
    }

    private void scanControllersPackage() throws ServletException {
        String controllersPackage = getInitParameter("controllers_package");

        if (controllersPackage == null || controllersPackage.isEmpty()) {
            throw new ServletException("The controllers_package parameter is empty. Please check your web.xml file.");
        }

        List<Class<?>> controllers = getAnnotatedControllers(controllersPackage);
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                if (isGetMappingMethod(method)) {
                    String url = getGetMappingUrl(method);
                    validateUrlUniqueness(url);
                    validateMethodReturnType(method, controller);
                    urlMappings.put(url, new Mapping(controller, method));
                }
            }
        }
    }

    private void validateMethodReturnType(Method method, Class<?> controller) throws ServletException {
        if (!isValidReturnType(method)) {
            throw new ServletException("Unsupported return type for method " + method.getName() + " in controller " + controller.getName());
        }
    }

    private boolean isValidReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        return returnType.equals(String.class) || returnType.equals(ModelView.class);
    }

    private List<Class<?>> getAnnotatedControllers(String controllersPackage) throws ServletException {
        try {
            return Reflect.getAnnotatedClasses(controllersPackage, Controller.class);
        } catch (IOException | ClassNotFoundException e) {
            throw new ServletException(e);
        }
    }

    private boolean isGetMappingMethod(Method method) {
        return method.isAnnotationPresent(GetMapping.class);
    }

    private String getGetMappingUrl(Method method) {
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        return getMapping.value();
    }

    private void validateUrlUniqueness(String url) throws ServletException {
        if (urlMappings.containsKey(url)) {
            throw new ServletException("URL \"" + url + "\" has more than one mapping associated with it.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        PrintWriter out = resp.getWriter();

        // Get the part of the URL after the base URL of the webapp
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestURI.substring(contextPath.length());
        Mapping mapping = urlMappings.get(url);

        if (mapping == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested URL \"" + url + "\" was not found on this server.");
            return;
        }

        try {
            Object responseObject = mapping.executeMethod(req);
            if (responseObject instanceof String responseString) {
                out.println("<p>" + responseString + "</p>");
            } else if (responseObject instanceof ModelView modelView) {
                // Bind the attributes to the request
                HashMap<String, Object> data = modelView.getData();
                for (String key: data.keySet()) {
                    req.setAttribute(key, data.get(key));
                }

                // Forward the request to the view
                RequestDispatcher dispatcher = req.getRequestDispatcher(modelView.getUrl());
                dispatcher.forward(req, resp);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            throw new ServletException(e);
        }
    }
}
