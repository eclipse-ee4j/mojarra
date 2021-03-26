/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.composite;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Enumeration;
import javax.el.ValueExpression;
import javax.faces.application.Resource;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.view.ViewDeclarationLanguage;

@FacesComponent(value="systest.BootstrapComponent")
public class BootstrapCompositeComponent extends UIOutput {

    @Override
    public void encodeAll(FacesContext context) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        ViewDeclarationLanguage vdl = context.getApplication().
                getViewHandler().getViewDeclarationLanguage(context,
                "/composite/boostrapCompositeComponentMetadata.xhtml");
        Resource compositeComponentResource = context.getApplication().getResourceHandler().createResource("componentWithMetadata.xhtml", "composite");

        long
                beforeFirstCall = System.currentTimeMillis(),
                afterFirstCall, beforeSecondCall, afterSecondCall,
                firstCallDuration, secondCallDuration;
        BeanInfo metadata = vdl.getComponentMetadata(context, compositeComponentResource);
        afterFirstCall = System.currentTimeMillis();
        firstCallDuration = afterFirstCall - beforeFirstCall;

        CompositeComponentMetadataUtils.writeMetadata(metadata, writer);

        beforeSecondCall = System.currentTimeMillis();
        metadata = vdl.getComponentMetadata(context, compositeComponentResource);
        afterSecondCall = System.currentTimeMillis();
        
        secondCallDuration = afterSecondCall - beforeSecondCall;

        CompositeComponentMetadataUtils.writeMetadata(metadata, writer);

        writer.write("firstCallDuration: " + firstCallDuration +
                " secondCallDuration: " + secondCallDuration + "\n");
        if (firstCallDuration > secondCallDuration) {
            writer.write("First call longer than second call by " +
                    (firstCallDuration - secondCallDuration));
        } else {
            writer.write("Cache did not work!");
        }


    }
}
