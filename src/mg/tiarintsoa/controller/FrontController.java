package mg.tiarintsoa.controller;

import com.google.gson.Gson;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.tiarintsoa.enumeration.RequestVerb;
import mg.tiarintsoa.exception.VerbNotFoundException;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@MultipartConfig
public class FrontController extends HttpServlet {

    private HashMap<String, Mapping> urlMappings;
    private final Gson gson = new Gson();
    public static final String STATIC_FOLDER_NAME = "static";
    public static String ROOT_DIRECTORY;
    public static String STATIC_DIRECTORY;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ROOT_DIRECTORY = config.getServletContext().getRealPath("/");
        STATIC_DIRECTORY = ROOT_DIRECTORY + File.separator + STATIC_FOLDER_NAME + File.separator;
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

        // Handle static resources (files starting with "static/")
        if (url.startsWith("/static/")) {
            serveStaticFile(req, resp, url);
            return;
        }

        // Proceed with normal mapping logic
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
        } catch (VerbNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // Method to serve static files
    protected void serveStaticFile(HttpServletRequest req, HttpServletResponse resp, String url) throws IOException {
        // Remove the "/static/" prefix to get the actual file path
        String filePath = url.substring("/static/".length());

        // Create a File object for the requested file
        File file = new File(STATIC_DIRECTORY + URLDecoder.decode(filePath, StandardCharsets.UTF_8));

        if (!file.exists() || file.isDirectory()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + filePath);
            return;
        }

        // Set the content type based on the file extension
        String mimeType = req.getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream"; // Default to binary if mime type is unknown
        }
        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());

        // Serve the file content
        try (FileInputStream fileInputStream = new FileInputStream(file);
             OutputStream outputStream = resp.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
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
