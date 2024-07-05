package mg.tiarintsoa.session;

import jakarta.servlet.http.HttpSession;

public class WinterSession {

    private HttpSession session;

    public WinterSession() {}

    public WinterSession(HttpSession session) {
        this.session = session;
    }

    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    public void add(String key, Object value) {
        session.setAttribute(key, value);
    }

    public Object get(String key) {
        return session.getAttribute(key);
    }

    public void delete(String key) {
        session.removeAttribute(key);
    }
}
