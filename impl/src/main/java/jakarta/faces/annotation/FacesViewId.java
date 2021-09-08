package jakarta.faces.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**

 *
 */
@Target({ FIELD, METHOD, TYPE })
@Retention(RUNTIME)
@Qualifier
@Documented
public @interface FacesViewId {

    /**
     * <p>
     * Set the view id
     * </p>
     *
     * @return the classes corresponding to the bean types of the bean
     */
    String value() default "";

    /**
     * Supports inline instantiation of the {@link FacesViewId} annotation.
     *
     */
    public final static class Literal extends AnnotationLiteral<FacesViewId> implements FacesViewId {

        public static final Literal INSTANCE = of("");

        private static final long serialVersionUID = 1L;

        private final String value;

        public static Literal of(String value) {
            return new Literal(value);
        }

        private Literal(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

}
