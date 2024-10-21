package mg.tiarintsoa.controller;

import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;

public class WinterPart {

    private final Part part;

    public WinterPart(Part part) {
        this.part = part;
    }

    public byte[] getBytes() throws IOException {
        return part.getInputStream().readAllBytes();
    }

    public String getSubmittedFileName() {
        return part.getSubmittedFileName();
    }

    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    public long getSize() {
        return part.getSize();
    }

    public boolean isEmpty() {
        return part.getSize() == 0;
    }

    public void save(String filePath) throws IOException {
        part.write(filePath);
    }
}
