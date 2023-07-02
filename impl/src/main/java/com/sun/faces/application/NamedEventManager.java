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

package com.sun.faces.application;

import static com.sun.faces.util.Util.loadClass;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.faces.FacesException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.PostRenderViewEvent;
import jakarta.faces.event.PostValidateEvent;
import jakarta.faces.event.PreRenderComponentEvent;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.event.PreValidateEvent;
import jakarta.faces.event.SystemEvent;

/**
 * Note: New, relevant spec'd ComponentSystemEvents must be added to the constructor
 */
public class NamedEventManager {

    private final Map<String, Class<? extends SystemEvent>> namedEvents = new ConcurrentHashMap<>();
    private final Map<String, Set<Class<? extends SystemEvent>>> duplicateNames = new ConcurrentHashMap<>();

    public NamedEventManager() {
        namedEvents.put("jakarta.faces.event.PreRenderComponent", PreRenderComponentEvent.class);
        namedEvents.put("jakarta.faces.event.PreRenderView", PreRenderViewEvent.class);
        namedEvents.put("jakarta.faces.event.PostRenderView", PostRenderViewEvent.class);
        namedEvents.put("jakarta.faces.event.PostAddToView", PostAddToViewEvent.class);
        namedEvents.put("jakarta.faces.event.PreValidate", PreValidateEvent.class);
        namedEvents.put("jakarta.faces.event.PostValidate", PostValidateEvent.class);
        namedEvents.put("preRenderComponent", PreRenderComponentEvent.class);
        namedEvents.put("preRenderView", PreRenderViewEvent.class);
        namedEvents.put("postRenderView", PostRenderViewEvent.class);
        namedEvents.put("postAddToView", PostAddToViewEvent.class);
        namedEvents.put("preValidate", PreValidateEvent.class);
        namedEvents.put("postValidate", PostValidateEvent.class);
    }

    public void addNamedEvent(String name, Class<? extends SystemEvent> event) {
        namedEvents.put(name, event);
    }

    @SuppressWarnings("unchecked")
    public Class<? extends SystemEvent> getNamedEvent(String name) {
        Class<? extends SystemEvent> namedEvent = namedEvents.get(name);

        if (namedEvent == null) {
            try {
                namedEvent = loadClass(name, this);
            } catch (ClassNotFoundException ex) {
                throw new FacesException("An unknown event type was specified:  " + name, ex);
            }
        }

        if (!ComponentSystemEvent.class.isAssignableFrom(namedEvent)) {
            throw new ClassCastException();
        }

        return namedEvent;
    }

    public void addDuplicateName(String name, Class<? extends SystemEvent> event) {
        Class<? extends SystemEvent> registeredEvent = namedEvents.remove(name);
        Set<Class<? extends SystemEvent>> events = duplicateNames.computeIfAbsent(name, k -> new HashSet<>());

        events.add(event);

        if (registeredEvent != null) {
            events.add(registeredEvent);
        }
    }

    public boolean isDuplicateNamedEvent(String name) {
        return namedEvents.get(name) != null || duplicateNames.get(name) != null;
    }
}
