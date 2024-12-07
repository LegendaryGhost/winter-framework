package mg.tiarintsoa.exception;

public class MissingErrorUrlException extends Exception {
    public MissingErrorUrlException() {
        super("Missing error URL");
    }
}
