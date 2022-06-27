package jakarta.faces.component.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;

class HtmlComponentUtils {

    private static final String ATTRIBUTES_THAT_ARE_SET = "jakarta.faces.component.UIComponentBase.attributesThatAreSet";
    private static final String OPTIMIZED_PACKAGE = "jakarta.faces.component.";

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
