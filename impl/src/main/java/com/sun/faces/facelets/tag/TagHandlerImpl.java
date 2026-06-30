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

package com.sun.faces.facelets.tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.faces.view.facelets.CompositeFaceletHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

import com.sun.faces.RIConstants;

/**
 *
 * @author edburns
 */
public abstract class TagHandlerImpl extends TagHandler {

    public TagHandlerImpl(TagConfig config) {
        super(config);
    }

    /**
     * Flag the current view build as containing build-time-dynamic content (a JSTL conditional/iteration or a dynamic
     * include) so its facelet is re-applied on every (re)build. Build-time-dynamic handlers call this at the top of
     * their {@code apply}; the absence of the flag lets {@code FaceletViewHandlingStrategy} skip the redundant
     * render-time re-apply for a purely component-driven view (see {@code refreshTransientBuildOnPSS}).
     *
     * @param ctx the {@link FaceletContext} for the current build
     */
    protected static void markDynamicTransientBuild(FaceletContext ctx) {
        ctx.getFacesContext().getAttributes().put(RIConstants.DYNAMIC_TRANSIENT_BUILD, Boolean.TRUE);
    }

    /**
     * Searches child handlers, starting at the 'nextHandler' for all instances of the passed type. This process will stop
     * searching a branch if an instance is found.
     *
     * @param type Class type to search for
     * @return iterator over instances of FaceletHandlers of the matching type
     */
    protected final Iterator findNextByType(Class type) {
        List found = new ArrayList();
        if (type.isAssignableFrom(nextHandler.getClass())) {
            found.add(nextHandler);
        } else if (nextHandler instanceof CompositeFaceletHandler) {
            FaceletHandler[] h = ((CompositeFaceletHandler) nextHandler).getHandlers();
            for (int i = 0; i < h.length; i++) {
                if (type.isAssignableFrom(h[i].getClass())) {
                    found.add(h[i]);
                }
            }
        }
        return found.iterator();
    }

    public final static Iterator findNextByType(FaceletHandler nextHandler, Class type) {
        List found = new ArrayList();
        if (type.isAssignableFrom(nextHandler.getClass())) {
            found.add(nextHandler);
        } else if (nextHandler instanceof CompositeFaceletHandler) {
            FaceletHandler[] h = ((CompositeFaceletHandler) nextHandler).getHandlers();
            for (int i = 0; i < h.length; i++) {
                if (type.isAssignableFrom(h[i].getClass())) {
                    found.add(h[i]);
                }
            }
        }
        return found.iterator();

    }

}
