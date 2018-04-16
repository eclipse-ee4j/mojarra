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

package com.sun.faces.event;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.PostRestoreStateEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent( value = "com.sun.faces.event.UIAddComponent3" )
public class UIAddComponent3
    extends UIComponentBase {

    //
    // Constructor
    //

    public UIAddComponent3() {

        setRendererType( "testcomponent" );

        FacesContext context = FacesContext.getCurrentInstance();
            UIViewRoot root = context.getViewRoot();
    }

    //
    // Public methods
    //

    @Override
    public String getFamily() {
        return "com.sun.faces.event";
    }

}
