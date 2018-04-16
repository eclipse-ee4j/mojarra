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

package com.sun.faces.application.applicationimpl.events;

import static com.sun.faces.util.Util.coalesce;

import javax.faces.event.SystemEvent;

import com.sun.faces.util.Cache;
import com.sun.faces.util.Cache.Factory;

/**
 * Simple wrapper class for application level SystemEvents. It provides the structure to map a
 * single SystemEvent to multiple sources which are represented by <code>SourceInfo</code>
 * instances.
 */
public class SystemEventInfo {

    private Cache<Class<?>, EventInfo> cache = new Cache<>(new Factory<Class<?>, EventInfo>() {
        @Override
        public EventInfo newInstance(Class<?> arg) throws InterruptedException {
            return new EventInfo(systemEvent, arg);
        }
    });
    private Class<? extends SystemEvent> systemEvent;

    // -------------------------------------------------------- Constructors

    public SystemEventInfo(Class<? extends SystemEvent> systemEvent) {
        this.systemEvent = systemEvent;
    }

    // ------------------------------------------------------ Public Methods

    public EventInfo getEventInfo(Class<?> source) {
        return cache.get(coalesce(source, Void.class));
    }

}
