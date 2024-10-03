package mg.tiarintsoa.annotation;

import mg.tiarintsoa.enumeration.RequestVerb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMapping {

    String value();
    RequestVerb verb() default RequestVerb.GET;

}
