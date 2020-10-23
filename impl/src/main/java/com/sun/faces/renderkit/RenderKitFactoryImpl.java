/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.renderkit;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.faces.util.MessageUtils;

import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

public class RenderKitFactoryImpl extends RenderKitFactory {

//
// Protected Constants
//
    protected String renderKitId;
    protected String className;
    protected ConcurrentHashMap<String, RenderKit> renderKits;

//
// Class Variables
//

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers
//
    /**
     * Constructor registers default Render kit.
     */
    public RenderKitFactoryImpl() {
        super(null);
        renderKits = new ConcurrentHashMap<>();
        addRenderKit(HTML_BASIC_RENDER_KIT, new RenderKitImpl());
    }

    @Override
    public void addRenderKit(String renderKitId, RenderKit renderKit) {

        if (renderKitId == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "renderKitId");
            throw new NullPointerException(message);
        }
        if (renderKit == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "renderKit");
            throw new NullPointerException(message);
        }

        renderKits.put(renderKitId, renderKit);

    }

    @Override
    public RenderKit getRenderKit(FacesContext context, String renderKitId) {

        if (renderKitId == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "renderKitId");
            throw new NullPointerException(message);
        }
        // PENDING (rogerk) do something with FacesContext ...
        //
        // If an instance already exists, return it.
        //

        return renderKits.get(renderKitId);
    }

    @Override
    public Iterator<String> getRenderKitIds() {
        return renderKits.keySet().iterator();
    }

}
