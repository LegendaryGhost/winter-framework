package mg.tiarintsoa.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FieldErrors {

    private final HashMap<String, List<String>> fieldErrors = new HashMap<>();

    public boolean hasErrors() {
        return !fieldErrors.isEmpty();
    }

    public boolean fieldHasErrors(String field) {
        return fieldErrors.containsKey(field);
    }

    public void addFieldError(String field, String message) {
        if (!fieldErrors.containsKey(field)) {
            fieldErrors.put(field, new ArrayList<>());
        }
        fieldErrors.get(field).add(message);
    }

    public List<String> getFieldErrors(String field) {
        return fieldErrors.get(field);
    }

}
