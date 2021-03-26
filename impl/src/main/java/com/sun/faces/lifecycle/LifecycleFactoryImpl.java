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

// LifecycleFactoryImpl.java

package com.sun.faces.lifecycle;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;

/**
 * <B>LifecycleFactoryImpl</B> is the stock implementation of Lifecycle in the JSF RI.
 * <P>
 *
 * @see jakarta.faces.lifecycle.LifecycleFactory
 */

public class LifecycleFactoryImpl extends LifecycleFactory {

    // Log instance for this class
    private static Logger LOGGER = FacesLogger.LIFECYCLE.getLogger();

    protected ConcurrentHashMap<String, Lifecycle> lifecycleMap = null;

    // ------------------------------------------------------------ Constructors

    public LifecycleFactoryImpl() {
        super(null);
        lifecycleMap = new ConcurrentHashMap<>();

        // We must have an implementation under this key.
        lifecycleMap.put(LifecycleFactory.DEFAULT_LIFECYCLE, new LifecycleImpl(FacesContext.getCurrentInstance()));
//        lifecycleMap.put(ActionLifecycle.ACTION_LIFECYCLE,
//                         new ActionLifecycle());
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Created Default Lifecycle");
        }
    }

    // -------------------------------------------------- Methods from Lifecycle

    @Override
    public void addLifecycle(String lifecycleId, Lifecycle lifecycle) {
        if (lifecycleId == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "lifecycleId"));
        }
        if (lifecycle == null) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "lifecycle"));
        }
        if (null != lifecycleMap.get(lifecycleId)) {
            Object params[] = { lifecycleId };
            String message = MessageUtils.getExceptionMessageString(MessageUtils.LIFECYCLE_ID_ALREADY_ADDED_ID, params);
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(MessageUtils.getExceptionMessageString(MessageUtils.LIFECYCLE_ID_ALREADY_ADDED_ID, params));
            }
            throw new IllegalArgumentException(message);
        }
        lifecycleMap.put(lifecycleId, lifecycle);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("addedLifecycle: " + lifecycleId + " " + lifecycle);
        }
    }

    @Override
    public Lifecycle getLifecycle(String lifecycleId) throws FacesException {

        if (null == lifecycleId) {
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "lifecycleId"));
        }

        if (null == lifecycleMap.get(lifecycleId)) {
            Object[] params = { lifecycleId };
            String message = MessageUtils.getExceptionMessageString(MessageUtils.CANT_CREATE_LIFECYCLE_ERROR_MESSAGE_ID, params);
            throw new IllegalArgumentException(message);
        }

        Lifecycle result = lifecycleMap.get(lifecycleId);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("getLifecycle: " + lifecycleId + " " + result);
        }
        return result;
    }

    @Override
    public Iterator<String> getLifecycleIds() {
        return lifecycleMap.keySet().iterator();
    }

// The testcase for this class is TestLifecycleFactoryImpl.java

} // end of class LifecycleFactoryImpl
