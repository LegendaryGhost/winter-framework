package mg.tiarintsoa.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.tiarintsoa.annotation.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FrontController extends HttpServlet {

    private List<Class<?>> controllers;

    @Override
    public void init() throws ServletException {
        try {
            this.controllers = scanControllersPackage();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Class<?>> scanControllersPackage() throws ClassNotFoundException {
        String controllersPackage = this.getInitParameter("controllers_package");
        List<Class<?>> controllers = new ArrayList<>();

        Class<?>[] classes = Class.forName(controllersPackage).getClasses();
        for (Class<?> packageClass : classes) {
            if (packageClass.isAnnotationPresent(Controller.class)) {
                controllers.add(packageClass);
            }
        }

        return controllers;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        // Get the URL of the request
        StringBuffer requestURL = req.getRequestURL();
        // If there are query parameters, append them to the URL
        String queryString = req.getQueryString();
        if (queryString != null) {
            requestURL.append("?").append(queryString);
        }
        out.println("Request URL: " + requestURL.toString());
    }
}
