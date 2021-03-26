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

package com.sun.faces.application.applicationimpl.events;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.HashMap;
import java.util.Map;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.SystemEvent;

public class ReentrantLisneterInvocationGuard {

    public boolean isGuardSet(FacesContext ctx, Class<? extends SystemEvent> systemEventClass) {
        Boolean result;
        Map<Class<? extends SystemEvent>, Boolean> data = getDataStructure(ctx);
        result = data.get(systemEventClass);

        return null == result ? false : result;
    }

    public void setGuard(FacesContext ctx, Class<? extends SystemEvent> systemEventClass) {
        Map<Class<? extends SystemEvent>, Boolean> data = getDataStructure(ctx);
        data.put(systemEventClass, TRUE);

    }

    public void clearGuard(FacesContext ctx, Class<? extends SystemEvent> systemEventClass) {
        Map<Class<? extends SystemEvent>, Boolean> data = getDataStructure(ctx);
        data.put(systemEventClass, FALSE);

    }

    private Map<Class<? extends SystemEvent>, Boolean> getDataStructure(FacesContext ctx) {
        Map<Class<? extends SystemEvent>, Boolean> result = null;
        Map<Object, Object> ctxMap = ctx.getAttributes();
        final String IS_PROCESSING_LISTENERS_KEY = "com.sun.faces.application.ApplicationImpl.IS_PROCESSING_LISTENERS";

        if (null == (result = (Map<Class<? extends SystemEvent>, Boolean>) ctxMap.get(IS_PROCESSING_LISTENERS_KEY))) {
            result = new HashMap<>(12);
            ctxMap.put(IS_PROCESSING_LISTENERS_KEY, result);
        }

        return result;
    }

}
