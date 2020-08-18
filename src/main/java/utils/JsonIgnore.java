package utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to exclude fields from being serializing or deserializing when exporting to JSON or
 * importing from JSON.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonIgnore {

}
