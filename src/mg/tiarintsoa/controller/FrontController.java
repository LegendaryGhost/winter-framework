package mg.tiarintsoa.controller;

import com.google.gson.Gson;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.tiarintsoa.enumeration.RequestVerb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class FrontController extends HttpServlet {

    private HashMap<String, Mapping> urlMappings;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        String controllersPackage = getInitParameter("controllers_package");
        urlMappings = ControllerPackageScanner.scan(controllersPackage);
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
