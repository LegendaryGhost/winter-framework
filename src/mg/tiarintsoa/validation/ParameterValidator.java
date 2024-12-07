package mg.tiarintsoa.validation;

import mg.tiarintsoa.annotation.RequestParameter;
import mg.tiarintsoa.validation.annotation.NotBlank;
import mg.tiarintsoa.validation.annotation.Number;
import mg.tiarintsoa.validation.annotation.Range;
import mg.tiarintsoa.validation.annotation.Required;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;

public class ParameterValidator {

    public static void validateParameter(Object value, Parameter parameter, FieldErrors fieldErrors) throws IllegalAccessException {

        if (!parameter.isAnnotationPresent(RequestParameter.class)) return;

        RequestParameter requestParameter = parameter.getAnnotation(RequestParameter.class);
        String parameterName = requestParameter.value();
        checkValidationAnnotations(parameter, parameterName, value, fieldErrors);

        Class<?> parameterClass = parameter.getType();
        if (parameterClass.equals(String.class)) {
            return;
        }

        for (Field field : parameterClass.getDeclaredFields()) {
            field.setAccessible(true);
            Object fieldValue = field.get(value);
            field.setAccessible(false);

            if (!field.isAnnotationPresent(RequestParameter.class)) break;

            String fieldName = field.getAnnotation(RequestParameter.class).value();
            checkValidationAnnotations(field, parameterName + "." + fieldName, fieldValue, fieldErrors);
        }
    }

    private static void checkValidationAnnotations(AnnotatedElement annotatedElement, String fieldName, Object value, FieldErrors fieldErrors) {

        if (annotatedElement.isAnnotationPresent(Required.class)) {
            Required requiredAnnotation = annotatedElement.getAnnotation(Required.class);
            validateRequired(value, requiredAnnotation, fieldName, fieldErrors);
        }
        if (annotatedElement.isAnnotationPresent(NotBlank.class)) {
            NotBlank notBlankAnnotation = annotatedElement.getAnnotation(NotBlank.class);
            validateNotBlank(value, notBlankAnnotation, fieldName, fieldErrors);
        }
        if (annotatedElement.isAnnotationPresent(Number.class)) {
            Number numberAnnotation = annotatedElement.getAnnotation(Number.class);
            validateNumber(value, numberAnnotation, fieldName, fieldErrors);
        }
        if (annotatedElement.isAnnotationPresent(Range.class)) {
            Range rangeAnnotation = annotatedElement.getAnnotation(Range.class);
            validateRange(value, rangeAnnotation, fieldName, fieldErrors);
        }
    }

    private static void validateRange(Object value, Range annotation, String fieldName, FieldErrors fieldErrors) {
        if (value == null) return;

        if (value.getClass().equals(String.class)) {
            int stringLength = ((String) value).length();
            if (stringLength < annotation.min() || stringLength > annotation.max()) fieldErrors.addFieldError(fieldName, annotation.message());
        } else if (isValidNumber(value.toString())) {
            BigDecimal number = new BigDecimal(value.toString());
            double doubleValue = number.doubleValue();
            if (doubleValue < annotation.min() || doubleValue > annotation.max()) fieldErrors.addFieldError(fieldName, annotation.message());
        }
    }

    private static void validateRequired(Object value, Required annotation, String fieldName, FieldErrors fieldErrors) {
        if (value == null) {
            fieldErrors.addFieldError(fieldName, annotation.message());
        }
    }

    private static void validateNotBlank(Object value, NotBlank annotation, String fieldName, FieldErrors fieldErrors) {
        if (value == null) {
            fieldErrors.addFieldError(fieldName, annotation.message());
            return;
        }
        if (value.toString().isEmpty()) {
            fieldErrors.addFieldError(fieldName, annotation.message());
        }
    }

    private static void validateNumber(Object value, Number annotation, String fieldName, FieldErrors fieldErrors) {
        if (value == null) {
            fieldErrors.addFieldError(fieldName, annotation.message());
            return;
        }
        if(!isValidNumber(value.toString())) {
            fieldErrors.addFieldError(fieldName, annotation.message());
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
