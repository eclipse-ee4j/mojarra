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

package com.sun.faces.test.servlet30.factory;

import java.beans.BeanInfo;
import java.io.IOException;
import java.util.Map;
import javax.faces.FacesWrapper;
import javax.faces.application.Resource;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;

public class CustomViewDeclarationLanguage extends ViewDeclarationLanguage
        implements FacesWrapper<ViewDeclarationLanguage> {

    private final ViewDeclarationLanguage toWrap;

    public CustomViewDeclarationLanguage(ViewDeclarationLanguage toWrap) {
        this.toWrap = toWrap;
    }

    public void logMethodInvocation(String method) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        final String key = "CustomViewDeclarationLanguageMessage";

        StringBuilder builder = (StringBuilder) requestMap.get(key);
        if (null == builder) {
            builder = new StringBuilder();
            requestMap.put(key, builder);
        }
        builder.append(" ").append(method);
    }

    @Override
    public void buildView(FacesContext fc, UIViewRoot uivr) throws IOException {
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        if (!params.containsKey("skipBuildView")) {
            this.logMethodInvocation("buildView");
            this.getWrapped().buildView(fc, uivr);
        }
    }

    @Override
    public ViewDeclarationLanguage getWrapped() {
        return this.toWrap;
    }

    @Override
    public UIViewRoot createView(FacesContext fc, String string) {
        this.logMethodInvocation("createView");
        return this.getWrapped().createView(fc, string);
    }

    @Override
    public BeanInfo getComponentMetadata(FacesContext fc, Resource rsrc) {
        this.logMethodInvocation("getComponentMetadata");
        return this.getWrapped().getComponentMetadata(fc, rsrc);
    }

    @Override
    public Resource getScriptComponentResource(FacesContext fc, Resource rsrc) {
        this.logMethodInvocation("getScriptComponentResource");
        return this.getWrapped().getScriptComponentResource(fc, rsrc);
    }

    @Override
    public StateManagementStrategy getStateManagementStrategy(FacesContext fc, String string) {
        this.logMethodInvocation("getStateManagementStrategy");
        return this.getWrapped().getStateManagementStrategy(fc, string);
    }

    @Override
    public ViewMetadata getViewMetadata(FacesContext fc, String string) {
        this.logMethodInvocation("getViewMetadata");
        return this.getWrapped().getViewMetadata(fc, string);
    }

    @Override
    public void renderView(FacesContext fc, UIViewRoot uivr) throws IOException {
        this.logMethodInvocation("renderView");
        this.getWrapped().renderView(fc, uivr);
    }

    @Override
    public UIViewRoot restoreView(FacesContext fc, String string) {
        this.logMethodInvocation("restoreView");
        return this.getWrapped().restoreView(fc, string);
    }
}
