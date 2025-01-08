package mg.tiarintsoa.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.tiarintsoa.annotation.Authenticated;

import java.util.Arrays;

public class Authenticator {

    public static boolean isAuthorised(HttpServletRequest request, Authenticated authenticatedAnnotation) {
	HttpSession session = request.getSession();

	Object authenticatedObject = session.getAttribute("authenticated");
	if (authenticatedObject == null) return false;

	boolean authenticated = (boolean) authenticatedObject;
	if (authenticatedAnnotation.roles().length == 0) return authenticated;

	Object roleObject = session.getAttribute("role");
	if (roleObject == null) return false;

	String role = roleObject.toString();
	String[] authorisedRoles = authenticatedAnnotation.roles();
	return authenticated && Arrays.asList(authorisedRoles).contains(role);
    }

}
