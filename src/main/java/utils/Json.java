package utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Utility class for JSON operations.
 *
 * @author Alkisum
 * @version 4.1
 * @since 4.1
 */
public final class Json {

    /**
     * Exclusion strategy when exporting to JSON.
     */
    public static final ExclusionStrategy EXCLUSION_STRATEGY = new ExclusionStrategy() {

        @Override
        public boolean shouldSkipClass(final Class<?> clazz) {
            return false;
        }

        @Override
        public boolean shouldSkipField(final FieldAttributes field) {
            return field.getAnnotation(JsonIgnore.class) != null;
        }
    };

    /**
     * Json constructor.
     */
    private Json() {

    }
}
