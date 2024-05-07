package mg.tiarintsoa.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class FrontController extends HttpServlet {

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
