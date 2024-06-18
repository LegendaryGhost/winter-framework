package mg.tiarintsoa.reflection;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Reflect {

    public static Object createInstance(Class<?> targetClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> constructor = targetClass.getDeclaredConstructor();
        return constructor.newInstance();
    }

    public static List<Class<?>> getAnnotatedClasses(String targetPackage, Class<? extends Annotation> targetAnnotation) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource((targetPackage).replace(".", "/"));

        if (resource == null) {
            throw new IOException("Target package not found: " + targetPackage);
        }

        String filePath = resource.getFile();
        String decodedPath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);
        File file = new File(decodedPath);

        if (!file.isDirectory()) {
            throw new IOException("The target package cannot be a file: " + targetPackage);
        }

        File[] files = file.listFiles();
        assert files != null;
        if (files.length == 0) {
            throw new IOException("The target package is empty: " + targetPackage);
        }

        scanDirectory(classes, targetPackage, targetAnnotation, file);

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