package mg.tiarintsoa.controller;

import java.util.HashMap;

public class ModelView {
    private String url;
    private HashMap<String, Object> data = new HashMap<>();

    public ModelView() {}

    public ModelView(String url) {
        this.url = url;
    }

    public void addObject(String key, Object value) {
        data.put(key, value);
    }

    public Object getObject(String key) {
        return data.get(key);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
}
