package org.eclipse.mojarra.test.issue5460;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;

@FacesComponent("issue5460Component")
public class Issue5460Component extends UIInput implements NamingContainer {

    @Override
    public String getFamily() {
        return UINamingContainer.COMPONENT_FAMILY;
    }

    @Override
    public void decode(FacesContext context) {
        Object value = getValue();
        setSubmittedValue(value == null ? "" : value);
        super.decode(context);
    }

    public String getAttributeResults() {
        return getAttributes().get("required") + " " + getAttributes().get("styleClass");
    }
}
