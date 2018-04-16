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
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.PostRestoreStateEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent( value = "com.sun.faces.test.servlet30.dynamic.AddComponent1" )
public class AddComponent1 extends UIComponentBase implements SystemEventListener {

    //
    // Constructor - subscribes to PreRenderViewEvent(s)
    // and PostRestoreStateEvent(s)
    //

    public AddComponent1() {
        setRendererType( "component" );
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();
        root.subscribeToViewEvent( PreRenderViewEvent.class, this );
        root.subscribeToViewEvent( PostRestoreStateEvent.class, this );
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
    //   If not a postback (as in PreRenderViewEvent), creates a PanelGrid
    //   and adds it as a child to this component. 
    //   On postback (as in PostRestoreStateEvent), creates an OutputText
    //   and adds it to the PanelGrid (only creates and adds if not done
    //   previously).

    @Override
    public void processEvent( SystemEvent event )
        throws AbortProcessingException {
        if ( !FacesContext.getCurrentInstance().isPostback() ) {
            HtmlPanelGrid component = new HtmlPanelGrid();
            component.setId("PANEL");
            component.setStyle( "border: 1px dashed blue; padding: 5px; margin: 5px" );
            getChildren().add( component );
        } else {
            // Get PanelGrid component
            HtmlPanelGrid component = (HtmlPanelGrid)getChildren().get(0);
            // If the child has not already been added - add it
            String added = (String)component.getAttributes().get("CHILD_ADDED");
            if (null == added) {
                HtmlOutputText output = new HtmlOutputText();
                output.setId("OUTPUT");
                output.setValue("NEW-OUTPUT");
                component.getChildren().add(output);
                component.getAttributes().put("CHILD_ADDED","CHILD_ADDED");
            }
        }
    }
}
