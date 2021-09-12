/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package com.sun.faces.test.faces40.javapagewithmetadata;

import static jakarta.faces.component.UIViewRoot.METADATA_FACET_NAME;

import java.io.IOException;
import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.annotation.View;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;

@View("/hello.xhtml")
@ApplicationScoped
public class Hello extends Facelet {

    @Override
    public void applyMetadata(FacesContext facesContext, UIComponent parent) throws IOException {
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();

        UIViewParameter uiViewParameter = new UIViewParameter();
        uiViewParameter.setName("id");
        uiViewParameter.setValueExpression("value", expressionFactory.createValueExpression(elContext, "#{bean.id}", String.class));


        UIPanel metadataFacet = new UIPanel();
        metadataFacet.getChildren().add(uiViewParameter);


        parent.getFacets().put(METADATA_FACET_NAME, metadataFacet);
    }

    @Override
    public void apply(FacesContext facesContext, UIComponent root) throws IOException {
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();

        List<UIComponent> rootChildren = root.getChildren();

        UIOutput html = new UIOutput();
        html.setValue("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        rootChildren.add(html);


        HtmlBody body = new HtmlBody();
        rootChildren.add(body);


        HtmlOutputText message = new HtmlOutputText();
        message.setValueExpression("value", expressionFactory.createValueExpression(elContext, "#{bean.message}", String.class));
        body.getChildren().add(message);


        html = new UIOutput();
        html.setValue("</html>");
        rootChildren.add(html);
    }

}
