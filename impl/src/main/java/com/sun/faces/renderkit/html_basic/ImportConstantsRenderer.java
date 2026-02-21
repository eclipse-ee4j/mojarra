/*
 * Copyright (c) Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.renderkit.html_basic;

import jakarta.faces.component.UIImportConstants;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.render.Renderer;

import com.sun.faces.application.view.ViewMetadataImpl;

/**
 * <b>ImportConstantsRenderer</b> is a class that runs the <code>f:importConstants</code> which is declared outside the metadata facet.
 *
 * @author Bauke Scholtz
 * @since 5.0
 * @see UIImportConstants
 */
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class ImportConstantsRenderer extends Renderer<UIImportConstants> implements ComponentSystemEventListener {

    // Constants ------------------------------------------------------------------------------------------------------

    public static final String RENDERER_TYPE = "jakarta.faces.ImportConstants";

    // Actions --------------------------------------------------------------------------------------------------------

    /**
     * After adding component to view, run {@link ViewMetadataImpl#importConstants(jakarta.faces.context.FacesContext, UIImportConstants)} immediately.
     * NOTE: when declared inside f:metadata, the component isn't added to the component tree, so this renderer wouldn't run in first place, so no precheck needed.
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        ViewMetadataImpl.importConstants(event.getFacesContext(), (UIImportConstants) event.getComponent());
    }
}
