package mg.tiarintsoa.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.tiarintsoa.annotation.Controller;
import mg.tiarintsoa.reflection.Reflect;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FrontController extends HttpServlet {

    private List<Class<?>> controllers;

    @Override
    public void init() {
        String controllersPackage = this.getInitParameter("controllers_package");
        try {
            this.controllers = Reflect.getAnnotatedClasses(controllersPackage, Controller.class);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        // Get the URL of the request
        StringBuffer requestURL = req.getRequestURL();
        // If there are query parameters, append them to the URL
        String queryString = req.getQueryString();
        if (queryString != null) {
            requestURL.append("?").append(queryString);
        }
        out.println("<h1>Request URL: " + requestURL.toString() + "</h1>");
        out.println("<h1>Controllers package: " + this.getInitParameter("controllers_package") + "</h1>");
        out.println("<ul>");
        for (Class<?> controllerClass : controllers) {
            out.println("<li>" + controllerClass.getName() + "</li>");
        }
        out.println("</ul>");
    }
}
