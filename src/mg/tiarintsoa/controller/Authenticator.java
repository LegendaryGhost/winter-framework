package mg.tiarintsoa.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mg.tiarintsoa.annotation.Authenticated;

import java.util.Arrays;

public class Authenticator {

    public static boolean isAuthorised(HttpServletRequest request, Authenticated authenticated) {
	HttpSession session = request.getSession();

	Object roleObject = session.getAttribute("role");
	if (roleObject == null) return false;

	String role = roleObject.toString();
	String[] authorisedRoles = authenticated.roles();
	return Arrays.asList(authorisedRoles).contains(role);
    }

}
