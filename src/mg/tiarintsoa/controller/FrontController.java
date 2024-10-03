package mg.tiarintsoa.controller;

import com.google.gson.Gson;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.UrlMapping;
import mg.tiarintsoa.enumeration.RequestVerb;
import mg.tiarintsoa.reflection.Reflect;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class FrontController extends HttpServlet {

    private final HashMap<String, Mapping> urlMappings = new HashMap<>();
    private final Gson gson = new Gson();

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
                if (isEndPointMethod(method)) {
                    String url = getMappedUrl(method);
                    RequestVerb verb = getMappedVerb(method);

                    Mapping mapping = urlMappings.get(url);
                    if(mapping == null) {
                        mapping = new Mapping(controller);
                        urlMappings.put(url, mapping);
                    }

                    try {
                        mapping.addVerbMapping(verb, method, url);
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
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

    private boolean isEndPointMethod(Method method) {
        return method.isAnnotationPresent(UrlMapping.class);
    }

    private String getMappedUrl(Method method) {
        UrlMapping url = method.getAnnotation(UrlMapping.class);
        return url.value();
    }

    private RequestVerb getMappedVerb(Method method) {
        return method.getAnnotation(UrlMapping.class).verb();
    }

    private void validateUrlUniqueness(String url) throws ServletException {
        if (urlMappings.containsKey(url)) {
            throw new ServletException("URL \"" + url + "\" has more than one mapping associated with it.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        processRequest(req, resp, RequestVerb.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        processRequest(req, resp, RequestVerb.POST);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp, RequestVerb verb) throws IOException, ServletException {
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
            Object responseObject = mapping.executeMethod(req, verb, url);
            if (mapping.isRestAPI(verb)) {
                processRestRequest(resp, responseObject);
            } else {
                processBasicRequest(req, resp, responseObject);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void processBasicRequest(HttpServletRequest req, HttpServletResponse resp, Object responseObject) throws ServletException, IOException {
        if (responseObject instanceof String responseString) {
            PrintWriter out = resp.getWriter();
            out.println("<main>" + responseString + "</main>");
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
    }

    protected void processRestRequest(HttpServletResponse resp, Object responseObject) throws IOException {
        // Set the response type to JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();
        String jsonResponse;

        if (responseObject instanceof ModelView modelView) {
            jsonResponse = gson.toJson(modelView.getData());
        } else {
            jsonResponse = gson.toJson(responseObject);
        }

        out.println(jsonResponse);
    }
}
