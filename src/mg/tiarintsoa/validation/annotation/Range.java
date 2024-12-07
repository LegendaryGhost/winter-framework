package mg.tiarintsoa.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Range {

    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
    String message() default "The parameter must be in the range specified";

}
