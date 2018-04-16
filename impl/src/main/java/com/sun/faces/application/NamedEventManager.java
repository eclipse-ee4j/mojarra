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

package com.sun.faces.application;

import static com.sun.faces.util.Util.loadClass;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.FacesException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.PostRenderViewEvent;
import javax.faces.event.PostValidateEvent;
import javax.faces.event.PreRenderComponentEvent;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.PreValidateEvent;
import javax.faces.event.SystemEvent;

/**
 * Note: New, relevant spec'd ComponentSystemEvents must be added to the constructor
 */
public class NamedEventManager {

    private Map<String, Class<? extends SystemEvent>> namedEvents = new ConcurrentHashMap<>();
    private Map<String, Set<Class<? extends SystemEvent>>> duplicateNames = new ConcurrentHashMap<>();

    public NamedEventManager() {
        namedEvents.put("javax.faces.event.PreRenderComponent", PreRenderComponentEvent.class);
        namedEvents.put("javax.faces.event.PreRenderView", PreRenderViewEvent.class);
        namedEvents.put("javax.faces.event.PostRenderView", PostRenderViewEvent.class);
        namedEvents.put("javax.faces.event.PostAddToView", PostAddToViewEvent.class);
        namedEvents.put("javax.faces.event.PreValidate", PreValidateEvent.class);
        namedEvents.put("javax.faces.event.PostValidate", PostValidateEvent.class);
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
                namedEvent = (Class<? extends SystemEvent>) loadClass(name, this);
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
        Set<Class<? extends SystemEvent>> events = duplicateNames.get(name);

        if (events == null) {
            events = new HashSet<>();
            duplicateNames.put(name, events);
        }
        events.add(event);

        if (registeredEvent != null) {
            events.add(registeredEvent);
        }
    }

    public boolean isDuplicateNamedEvent(String name) {
        return namedEvents.get(name) != null || duplicateNames.get(name) != null;
    }
}
