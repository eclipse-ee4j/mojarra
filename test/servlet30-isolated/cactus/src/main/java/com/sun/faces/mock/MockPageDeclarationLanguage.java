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

package com.sun.faces.mock;

import java.beans.BeanInfo;
import java.io.IOException;

import javax.faces.application.Resource;
import javax.faces.context.FacesContext;
import javax.faces.component.UIViewRoot;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewMetadata;

/**
 *
 * @author edburns
 */
public class MockPageDeclarationLanguage extends ViewDeclarationLanguage {

    public BeanInfo getComponentMetadata(FacesContext context, 
            Resource componentResource) {
	throw new UnsupportedOperationException();
    }

    public Resource getScriptComponentResource(FacesContext context,
					       Resource componentResource) {
	throw new UnsupportedOperationException();
    }

    public UIViewRoot createView(FacesContext ctx, String viewId) {
        throw new UnsupportedOperationException();
    }

    public UIViewRoot restoreView(FacesContext ctx, String viewId) {
        throw new UnsupportedOperationException();
    }

    public void renderView(FacesContext ctx, UIViewRoot view)
          throws IOException {
        throw new UnsupportedOperationException();
    }

    public StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId) {
        return null;
    }

    @Override
    public void buildView(FacesContext arg0, UIViewRoot arg1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ViewMetadata getViewMetadata(FacesContext arg0, String arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
