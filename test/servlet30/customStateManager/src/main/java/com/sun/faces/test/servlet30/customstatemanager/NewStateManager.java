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

package com.sun.faces.test.servlet30.customstatemanager;

import javax.faces.application.StateManager;
import javax.faces.application.StateManagerWrapper;

import javax.faces.FactoryFinder;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import javax.faces.component.UIViewRoot;

import javax.faces.context.FacesContext;

import java.io.IOException;

public class NewStateManager extends StateManagerWrapper {

    private StateManager oldStateManager = null;

    public NewStateManager(StateManager oldStateManager) {
	this.oldStateManager = oldStateManager;
    }

    public StateManager getWrapped() {
	return oldStateManager;
    }

    /**
     * <p>Just save the view in the session.</p>
     */

    public Object saveView(FacesContext context) {    
        return oldStateManager.saveView(context);
    }

    /**
     * <p>Override superclass processing and call the new version of
     * <code>writeState()</code> that takes <code>Object</code>.</p>
     */
	
    public void writeState(FacesContext context, Object state) throws IOException {
	getResponseStateManager(context).writeState(context, state);
    }


    public UIViewRoot restoreView(FacesContext context, String viewId,
                                  String renderKitId) {
	
        return oldStateManager.restoreView(context, viewId, renderKitId);
    }

    private ResponseStateManager getResponseStateManager(FacesContext context){
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(context, 
							    RenderKitFactory.HTML_BASIC_RENDER_KIT);
	ResponseStateManager responseStateManager = 
	    renderKit.getResponseStateManager();
	
	return responseStateManager;
    }

    char requestIdSerial = 0;

    private String createUniqueRequestId() {
	if (requestIdSerial++ == Character.MAX_VALUE) {
	    requestIdSerial = 0;
	}
	return UIViewRoot.UNIQUE_ID_PREFIX + ((int) requestIdSerial);
    }


}
