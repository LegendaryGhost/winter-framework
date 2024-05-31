package mg.tiarintsoa.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.annotation.GetMapping;
import mg.tiarintsoa.exception.UnsupportedReturnTypeException;
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
    public void init() {
        String controllersPackage = this.getInitParameter("controllers_package");
        try {
            List<Class<?>> controllers = Reflect.getAnnotatedClasses(controllersPackage, Controller.class);

            for (Class<?> controller: controllers) {
                for (Method method: controller.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        String url = getMapping.value();

                        // Check if the URL has already been mapped
                        if (urlMappings.containsKey(url)) {
                            throw new IllegalStateException("URL " + url + " has already been mapped to another controller's method.");
                        }

                        urlMappings.put(url, new Mapping(controller, method));
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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
            // Set the response status to 404 Not Found
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("<h1>404 Not Found</h1>");
            out.println("<p>The requested URL " + url + " was not found on this server.</p>");
            return;
        }

        try {
            Method method = mapping.getMethod();
            Object controllerInstance = mapping.getControllerInstance();
            Object responseObject = method.invoke(controllerInstance);
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
            } else {
                throw new UnsupportedReturnTypeException("Unsupported controller's method return value: "
                    + responseObject.getClass().getName()
                    + " returned instead of String or ModelView."
                );
            }
        } catch (UnsupportedReturnTypeException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
