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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

/**
 * Represent a logical association between a SystemEvent and a Source. This call will contain the Listeners specific to
 * this association as well as provide a method to construct new SystemEvents as required.
 */
public class EventInfo {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private final Class<? extends SystemEvent> systemEvent;
    private final Class<?> sourceClass;
    private final Set<SystemEventListener> listeners;
    private Constructor eventConstructor;
    private final Map<Class<?>, Constructor> constructorMap;

    // -------------------------------------------------------- Constructors

    public EventInfo(Class<? extends SystemEvent> systemEvent, Class<?> sourceClass) {

        this.systemEvent = systemEvent;
        this.sourceClass = sourceClass;
        listeners = new CopyOnWriteArraySet<>();
        constructorMap = new HashMap<>();
        if (!sourceClass.equals(Void.class)) {
            eventConstructor = getEventConstructor(sourceClass);
        }

    }

    // ------------------------------------------------------ Public Methods

    public Set<SystemEventListener> getListeners() {

        return listeners;

    }

    public SystemEvent createSystemEvent(Object source) {

        Constructor toInvoke = getCachedConstructor(source.getClass());
        if (toInvoke != null) {
            try {
                return (SystemEvent) toInvoke.newInstance(source);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new FacesException(e);
            }
        }
        return null;

    }

    // ----------------------------------------------------- Private Methods

    private Constructor getCachedConstructor(Class<?> source) {

        if (eventConstructor != null) {
            return eventConstructor;
        } else {
            Constructor c = constructorMap.get(source);
            if (c == null) {
                c = getEventConstructor(source);
                if (c != null) {
                    constructorMap.put(source, c);
                }
            }
            return c;
        }

    }

    private Constructor getEventConstructor(Class<?> source) {

        Constructor ctor = null;
        try {
            return systemEvent.getDeclaredConstructor(source);
        } catch (NoSuchMethodException ignored) {
            Constructor[] ctors = systemEvent.getConstructors();
            if (ctors != null) {
                for (Constructor c : ctors) {
                    Class<?>[] params = c.getParameterTypes();
                    if (params.length != 1) {
                        continue;
                    }
                    if (params[0].isAssignableFrom(source)) {
                        return c;
                    }
                }
            }
            if (eventConstructor == null && LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Unable to find Constructor within {0} that accepts {1} instances.",
                        new Object[] { systemEvent.getName(), sourceClass.getName() });
            }
        }
        return ctor;

    }

}
