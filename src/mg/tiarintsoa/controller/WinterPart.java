package mg.tiarintsoa.controller;

import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        // Create the full path
        Path path = Paths.get(FrontController.STATIC_DIRECTORY + filePath);

        // Get the parent directory of the file
        Path parentDir = path.getParent();

        // Create directories if they don't exist
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // Write the file content to the specified path
        Files.write(path, getBytes());

        System.out.println("File saved to: " + path.toAbsolutePath());
    }
}
