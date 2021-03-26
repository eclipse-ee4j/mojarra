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

package com.sun.faces.test.servlet30.dynamic;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent( value = "com.sun.faces.test.servlet30.dynamic.MoveComponent" )
public class MoveComponent extends UIComponentBase implements SystemEventListener {

    //
    // Constructor - subscribes to PostAddToViewEvent(s)
    //

    public MoveComponent() {
        setRendererType( "component" );
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();
        root.subscribeToViewEvent( PreRenderViewEvent.class, this );
    }

    //
    // Public methods
    //

    @Override
    public String getFamily() {
        return "com.sun.faces.test.servlet30.dynamic";
    }

    public boolean isListenerForSource( Object source ) {
        return ( source instanceof UIViewRoot );
    }

    // This event method will do the following:
    //   PostAddToViewEvent processing: 
    //     Dynamically modifies component tree by moving "Foo" from outside
    //     the panel to inside the panel.

    @Override
    public void processEvent( SystemEvent event )
        throws AbortProcessingException {
        // Do not re-modify the component tree

        if (FacesContext.getCurrentInstance().isPostback()) {
                return;
        }

        // Dynamically modify the component tree. Move Foo from outside to inside the panel

        HtmlOutputText  outputText = (HtmlOutputText) getChildren().get( 0 );
        HtmlPanelGroup panel = (HtmlPanelGroup) getChildren().get( 1 );
        boolean removed = getChildren().remove( outputText );
        panel.getChildren().add( 0, outputText );
    }
}
