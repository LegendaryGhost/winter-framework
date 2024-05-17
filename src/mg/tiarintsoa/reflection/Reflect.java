package mg.tiarintsoa.reflection;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Reflect {

    public static List<Class<?>> getAnnotatedClasses(String targetPackage, Class<? extends Annotation> targetAnnotation) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources((targetPackage).replace(".", "/"));
        if (resources == null) {
            throw new IllegalArgumentException("Target package not found");
        }

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String filePath = resource.getFile();
            String decodedPath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);
            File file = new File(decodedPath);
            scanDirectory(classes, targetPackage, targetAnnotation, file);
        }
        return classes;
    }

    private static void scanDirectory(List<Class<?>> classes, String targetPackage, Class<? extends Annotation> targetAnnotation, File directory) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        assert files != null;
        for(File file: files) {
            if(file.isDirectory()) {
                scanDirectory(classes, targetPackage + "." + file.getName(), targetAnnotation, file);
            } else {
                scanFile(classes, targetPackage, targetAnnotation, file);
            }
        }
    }

    private static void scanFile(List<Class<?>> classes, String targetPackage, Class<? extends Annotation> targetAnnotation, File file) throws ClassNotFoundException {
        String fileName = file.getName();
        if (fileName.endsWith(".class")) {
            String className = fileName.substring(0, fileName.length() - 6);
            Class<?> clazz = Class.forName(targetPackage + "." + className);
            if (clazz.isAnnotationPresent(targetAnnotation)) {
                classes.add(clazz);
            }
        }
    }

}