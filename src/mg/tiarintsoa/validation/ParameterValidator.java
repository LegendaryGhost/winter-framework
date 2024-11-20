package mg.tiarintsoa.validation;

import mg.tiarintsoa.exception.InvalidRequestParameterException;
import mg.tiarintsoa.validation.annotation.NotBlank;
import mg.tiarintsoa.validation.annotation.Number;
import mg.tiarintsoa.validation.annotation.Required;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;

public class ParameterValidator {

    public static void validate(Object value, Parameter parameter) throws IllegalAccessException {
        if (parameter.isAnnotationPresent(Required.class)) {
            Required requiredAnnotation = parameter.getAnnotation(Required.class);
            validateRequired(value, requiredAnnotation);
        }
        if (parameter.isAnnotationPresent(NotBlank.class)) {
            NotBlank notBlankAnnotation = parameter.getAnnotation(NotBlank.class);
            validateNotBlank(value, notBlankAnnotation);
        }
        if (parameter.isAnnotationPresent(Number.class)) {
            Number numberAnnotation = parameter.getAnnotation(Number.class);
            validateNumber(value, numberAnnotation);
        }

        Class<?> parameterClass = parameter.getType();
        if (parameterClass.equals(String.class)) {
            return;
        }

        for (Field field : parameterClass.getDeclaredFields()) {
            field.setAccessible(true);
            Object fieldValue = field.get(value);
            field.setAccessible(false);

            if (field.isAnnotationPresent(Required.class)) {
                Required requiredAnnotation = field.getAnnotation(Required.class);
                validateRequired(fieldValue, requiredAnnotation);
            }
            if (field.isAnnotationPresent(NotBlank.class)) {
                NotBlank notBlankAnnotation = field.getAnnotation(NotBlank.class);
                validateNotBlank(fieldValue, notBlankAnnotation);
            }
            if (field.isAnnotationPresent(Number.class)) {
                Number numberAnnotation = field.getAnnotation(Number.class);
                validateNumber(fieldValue, numberAnnotation);
            }
        }
    }

    private static void validateRequired(Object value, Required annotation) {
        if (value == null) {
            throw new InvalidRequestParameterException(annotation.message());
        }
    }

    private static void validateNotBlank(Object value, NotBlank annotation) {
        if (value == null) {
            throw new InvalidRequestParameterException(annotation.message());
        }
        if (value.toString().isEmpty()) {
            throw new InvalidRequestParameterException(annotation.message());
        }
    }

    private static void validateNumber(Object value, Number annotation) {
        if (value == null) {
            throw new InvalidRequestParameterException(annotation.message());
        }
        if(!isValidNumber(value.toString())) {
            throw new InvalidRequestParameterException(annotation.message());
        }
    }

    private static boolean isValidNumber(String str) {
        try {
            new BigDecimal(str); // Handles integers, floating-point numbers, and decimals
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
