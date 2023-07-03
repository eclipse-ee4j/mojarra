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

import com.sun.faces.util.Cache;

import jakarta.faces.event.SystemEvent;

/**
 * Utility class for dealing with application events.
 */
public class SystemEventHelper {

    private final Cache<Class<? extends SystemEvent>, SystemEventInfo> systemEventInfoCache;

    // -------------------------------------------------------- Constructors

    public SystemEventHelper() {

        systemEventInfoCache = new Cache<>(SystemEventInfo::new);

    }

    // ------------------------------------------------------ Public Methods

    public EventInfo getEventInfo(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass) {

        EventInfo info = null;
        SystemEventInfo systemEventInfo = systemEventInfoCache.get(systemEventClass);
        if (systemEventInfo != null) {
            info = systemEventInfo.getEventInfo(sourceClass);
        }

        return info;

    }

    public EventInfo getEventInfo(Class<? extends SystemEvent> systemEventClass, Object source, Class<?> sourceBaseType, boolean useSourceForLookup) {

        Class<?> sourceClass = useSourceForLookup ? sourceBaseType != null ? sourceBaseType : source.getClass() : Void.class;
        return getEventInfo(systemEventClass, sourceClass);

    }

} // END SystemEventHelper
