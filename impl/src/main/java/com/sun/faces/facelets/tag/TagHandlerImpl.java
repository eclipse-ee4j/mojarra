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
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

/**
 *
 * @author edburns
 */
public abstract class TagHandlerImpl extends TagHandler {

    public TagHandlerImpl(TagConfig config) {
        super(config);
    }

    /**
     * Searches child handlers, starting at the 'nextHandler' for all instances of the passed type. This process will stop
     * searching a branch if an instance is found.
     *
     * @param type Class type to search for
     * @return iterator over instances of FaceletHandlers of the matching type
     */
    protected final <T> Iterator<T> findNextByType(Class<T> type) {
        return findNextByType(nextHandler, type);
    }

    public static final <T> Iterator<T> findNextByType(FaceletHandler nextHandler, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (type.isAssignableFrom(nextHandler.getClass())) {
            found.add(type.cast(nextHandler));
        } else if (nextHandler instanceof CompositeFaceletHandler) {
            FaceletHandler[] h = ((CompositeFaceletHandler) nextHandler).getHandlers();
            for (FaceletHandler handler : h) {
                if (type.isAssignableFrom(handler.getClass())) {
                    found.add(type.cast(handler));
                }
            }
        }
        return found.iterator();
    }

}
