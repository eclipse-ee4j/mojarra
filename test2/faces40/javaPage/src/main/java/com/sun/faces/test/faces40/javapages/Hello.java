package com.sun.faces.test.faces40.javapages;

import static jakarta.faces.application.StateManager.IS_BUILDING_INITIAL_STATE;

import java.io.IOException;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.annotation.View;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;

@View("/hello.xhtml")
@ApplicationScoped
public class Hello extends Facelet {

    @Override
    public void apply(FacesContext facesContext, UIComponent root) throws IOException {
        if (!facesContext.getAttributes().containsKey(IS_BUILDING_INITIAL_STATE)) {
            return;
        }

        List<UIComponent> rootChildren = root.getChildren();

        UIOutput output = new UIOutput();
        output.setValue("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        rootChildren.add(output);


        HtmlBody body = new HtmlBody();
        rootChildren.add(body);


        HtmlForm form = new HtmlForm();
        form.setId("form");
        body.getChildren().add(form);


        HtmlOutputText message = new HtmlOutputText();
        message.setId("message");
        form.getChildren().add(message);


        HtmlCommandButton actionButton = new HtmlCommandButton();
        actionButton.setId("button");
        actionButton.addActionListener(e -> message.setValue("Hello, World"));
        actionButton.setValue("Do action");
        form.getChildren().add(actionButton);


        output = new UIOutput();
        output.setValue("</html>");
        rootChildren.add(output);
    }

}
