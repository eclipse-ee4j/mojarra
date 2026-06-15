package jakarta.faces.component.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;

class HtmlComponentUtils {

    private static final String ATTRIBUTES_THAT_ARE_SET = "jakarta.faces.component.UIComponentBase.attributesThatAreSet";
    private static final String OPTIMIZED_PACKAGE = "jakarta.faces.component.";

    /**
     * Records (or clears) that the given pass-through HTML attribute was explicitly set on a standard component, in the
     * component's {@code attributesThatAreSet} list, so that at render time an unset attribute can be skipped without a
     * reflective property read.
     * <p>
     * The generated concrete {@code Html*} setters call this for the pass-through attributes they expose ({@code style},
     * {@code styleClass}, {@code dir}, {@code lang}, {@code title}, the {@code on*} handlers, {@code accesskey},
     * {@code tabindex}, {@code alt}, {@code border}, ...). It is intentionally <em>not</em> called for properties that
     * are not emitted as HTML attributes by value ({@code escape}, {@code label}, {@code value}, {@code required}, ...),
     * which drive component logic and are read unconditionally, nor for the boolean attributes rendered through their
     * own path ({@code disabled}, {@code readonly}); none of those would gain anything from the skip.
     * <p>
     * This is the <em>write</em> side of a two-part optimization whose <em>read</em> side is
     * {@code com.sun.faces.renderkit.AttributeManager} together with {@code RenderKitUtils.renderPassThruAttributes} /
     * {@code getAttributeIfSet}. {@code AttributeManager} declares <em>which</em> pass-through attributes a renderer
     * emits; this list records <em>which were actually set</em>; rendering walks the former and consults the latter to
     * skip the reflective getter for the unset ones. The two sets overlap but are not equal: {@code style}, {@code dir},
     * {@code lang} and {@code title} appear in both, whereas {@code styleClass} is tracked here yet absent from
     * {@code AttributeManager}, because it must be emitted as the HTML {@code class} attribute (a name rename the generic
     * pass-through loop cannot perform) and is therefore rendered inline by each renderer instead.
     * <p>
     * Only standard components (those in the {@code jakarta.faces.component} package, i.e. the generated {@code Html*}
     * classes) maintain the list; for any other component type it is never created and the optimization is simply
     * skipped. A {@code null} {@code value} clears the tracking unless a {@link ValueExpression} is bound to {@code name},
     * in which case the attribute stays "set" because its value is supplied dynamically per request.
     *
     * @param component the standard component on which the attribute was set or cleared
     * @param name      the pass-through attribute (property) name
     * @param value     the new literal value, or {@code null} when the attribute is being cleared
     */
    static void handleAttribute(UIComponent component, String name, Object value) {
        final Map<String, Object> attributes = component.getAttributes();

        @SuppressWarnings("unchecked")
        List<String> setAttributes = (List<String>) attributes.get(ATTRIBUTES_THAT_ARE_SET);

        if (setAttributes == null) {
            String className = component.getClass().getName();
            if (className.startsWith(OPTIMIZED_PACKAGE)) {
                setAttributes = new ArrayList<>(6);
                attributes.put(ATTRIBUTES_THAT_ARE_SET, setAttributes);
            }
        }

        if (setAttributes != null) {
            if (value == null) {
                ValueExpression ve = component.getValueExpression(name);
                if (ve == null) {
                    setAttributes.remove(name);
                }
            } else if (!setAttributes.contains(name)) {
                setAttributes.add(name);
            }
        }
    }

}
