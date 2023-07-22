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

package com.sun.faces.application.applicationimpl;

import static com.sun.faces.util.Util.notNull;
import static jakarta.faces.application.ProjectStage.Development;
import static java.util.logging.Level.WARNING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.faces.application.applicationimpl.events.ComponentSystemEventHelper;
import com.sun.faces.application.applicationimpl.events.EventInfo;
import com.sun.faces.application.applicationimpl.events.ReentrantLisneterInvocationGuard;
import com.sun.faces.application.applicationimpl.events.SystemEventHelper;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.event.SystemEventListenerHolder;

public class Events {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private static final String CONTEXT = "context";
    private static final String LISTENER = "listener";
    private static final String SOURCE = "source";
    private static final String SYSTEM_EVENT_CLASS = "systemEventClass";

    private final SystemEventHelper systemEventHelper = new SystemEventHelper();
    private final ComponentSystemEventHelper compSysEventHelper = new ComponentSystemEventHelper();

    /*
     * This class encapsulates the behavior to prevent infinite loops when the publishing of one event leads to the queueing
     * of another event of the same type. Special provision is made to allow the case where this guaring mechanims happens
     * on a per-FacesContext, per-SystemEvent.class type basis.
     */

    private final ReentrantLisneterInvocationGuard listenerInvocationGuard = new ReentrantLisneterInvocationGuard();

    /*
     * @see jakarta.faces.application.Application#publishEvent(FacesContext, Class, Class, Object)
     */
    public void publishEvent(FacesContext context, Class<? extends SystemEvent> systemEventClass, Class<?> sourceBaseType, Object source,
            ProjectStage projectStage) {

        notNull(CONTEXT, context);
        notNull(SYSTEM_EVENT_CLASS, systemEventClass);
        notNull(SOURCE, source);

        if (!needsProcessing(context, systemEventClass)) {
            return;
        }

        // Source is not compatible with the provided base type.
        // Log a warning that the types are incompatible and return.
        if (projectStage == Development && sourceBaseType != null && !sourceBaseType.isInstance(source)) {
            if (LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "faces.application.publish.event.base_type_mismatch", new Object[] { source.getClass().getName(), sourceBaseType.getName() });
            }
            return;
        }

        try {
            // The side-effect of calling invokeListenersFor
            // will create a SystemEvent object appropriate to event/source
            // combination. This event will be passed on subsequent invocations
            // of invokeListenersFor

            // Look for and invoke any listeners stored on the source instance.
            SystemEvent event = invokeComponentListenersFor(systemEventClass, source);

            // Look for and invoke any 'view' listeners
            event = invokeViewListenersFor(context, systemEventClass, event, source);

            // Look for and invoke any listeners stored on the application using source type.
            event = invokeListenersFor(systemEventClass, event, source, sourceBaseType, true);

            // Look for and invoke any listeners not specific to the source class
            invokeListenersFor(systemEventClass, event, source, null, false);
        } catch (AbortProcessingException ape) {
            context.getApplication().publishEvent(context, ExceptionQueuedEvent.class, new ExceptionQueuedEventContext(context, ape));
        }
    }

    /*
     * @see Application#subscribeToEvent(Class, jakarta.faces.event.SystemEventListener)
     */
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener) {
        subscribeToEvent(systemEventClass, null, listener);
    }

    /*
     * @see Application#subscribeToEvent(Class, Class, jakarta.faces.event.SystemEventListener)
     */
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {

        notNull(SYSTEM_EVENT_CLASS, systemEventClass);
        notNull(LISTENER, listener);

        getListeners(systemEventClass, sourceClass).add(listener);
    }

    /*
     * @see Application#unsubscribeFromEvent(Class, Class, jakarta.faces.event.SystemEventListener)
     */
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {

        notNull(SYSTEM_EVENT_CLASS, systemEventClass);
        notNull(LISTENER, listener);

        Set<SystemEventListener> listeners = getListeners(systemEventClass, sourceClass);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * @return the SystemEventListeners that should be used for the provided combination of SystemEvent and source.
     */
    private Set<SystemEventListener> getListeners(Class<? extends SystemEvent> systemEvent, Class<?> sourceClass) {

        Set<SystemEventListener> listeners = null;
        EventInfo sourceInfo = systemEventHelper.getEventInfo(systemEvent, sourceClass);
        if (sourceInfo != null) {
            listeners = sourceInfo.getListeners();
        }

        return listeners;

    }

    private boolean needsProcessing(FacesContext context, Class<? extends SystemEvent> systemEventClass) {
        return context.isProcessingEvents() || ExceptionQueuedEvent.class.isAssignableFrom(systemEventClass);
    }

    /**
     * @return process any listeners for the specified SystemEventListenerHolder and return any SystemEvent that may have
     * been created as a side-effect of processing the listeners.
     */
    private SystemEvent invokeComponentListenersFor(Class<? extends SystemEvent> systemEventClass, Object source) {

        if (source instanceof SystemEventListenerHolder) {

            List<SystemEventListener> listeners = ((SystemEventListenerHolder) source).getListenersForEventClass(systemEventClass);
            if (null == listeners) {
                return null;
            }
            EventInfo eventInfo = compSysEventHelper.getEventInfo(systemEventClass, source.getClass());
            return processListeners(listeners, null, source, eventInfo);
        }
        return null;

    }

    private SystemEvent invokeViewListenersFor(FacesContext ctx, Class<? extends SystemEvent> systemEventClass, SystemEvent event, Object source) {
        SystemEvent result = event;

        if (listenerInvocationGuard.isGuardSet(ctx, systemEventClass)) {
            return result;
        }
        listenerInvocationGuard.setGuard(ctx, systemEventClass);

        UIViewRoot root = ctx.getViewRoot();
        try {
            if (root != null) {
                List<SystemEventListener> listeners = root.getViewListenersForEventClass(systemEventClass);
                if (null == listeners) {
                    return null;
                }

                EventInfo rootEventInfo = systemEventHelper.getEventInfo(systemEventClass, UIViewRoot.class);
                // process view listeners
                result = processListenersAccountingForAdds(listeners, event, source, rootEventInfo);
            }
        } finally {
            listenerInvocationGuard.clearGuard(ctx, systemEventClass);
        }
        return result;

    }

    /**
     * Traverse the <code>List</code> of listeners and invoke any that are relevent for the specified source.
     *
     * @throws jakarta.faces.event.AbortProcessingException propagated from the listener invocation
     */
    private SystemEvent invokeListenersFor(Class<? extends SystemEvent> systemEventClass, SystemEvent event, Object source, Class<?> sourceBaseType,
            boolean useSourceLookup) throws AbortProcessingException {

        EventInfo eventInfo = systemEventHelper.getEventInfo(systemEventClass, source, sourceBaseType, useSourceLookup);
        if (eventInfo != null) {
            Set<SystemEventListener> listeners = eventInfo.getListeners();
            event = processListeners(listeners, event, source, eventInfo);
        }

        return event;
    }

    /**
     * Iterate through and invoke the listeners. If the passed event was <code>null</code>, create the event, and return it.
     */
    private SystemEvent processListeners(Collection<SystemEventListener> listeners, SystemEvent event, Object source, EventInfo eventInfo) {

        if (listeners != null && !listeners.isEmpty()) {
            ArrayList<SystemEventListener> list = new ArrayList<>(listeners);

            for (SystemEventListener curListener : list) {
                if (curListener != null && curListener.isListenerForSource(source)) {
                    if (event == null) {
                        event = eventInfo.createSystemEvent(source);
                    }
                    assert event != null;
                    if (event.isAppropriateListener(curListener)) {
                        event.processListener(curListener);
                    }
                }
            }
        }

        return event;

    }

    private SystemEvent processListenersAccountingForAdds(List<SystemEventListener> listeners, SystemEvent event, Object source, EventInfo eventInfo) {

        if (listeners != null && !listeners.isEmpty()) {

            // copy listeners
            // go thru copy completely
            // compare copy to original
            // if original differs from copy, make a new copy.
            // The new copy consists of the original list - processed

            SystemEventListener[] listenersCopy = new SystemEventListener[listeners.size()];
            int i = 0;
            for (i = 0; i < listenersCopy.length; i++) {
                listenersCopy[i] = listeners.get(i);
            }

            Map<SystemEventListener, Boolean> processedListeners = new HashMap<>(listeners.size());
            boolean processedSomeEvents = false, originalDiffersFromCopy = false;

            do {
                i = 0;
                originalDiffersFromCopy = false;
                if (0 < listenersCopy.length) {
                    for (i = 0; i < listenersCopy.length; i++) {
                        SystemEventListener curListener = listenersCopy[i];
                        if (curListener != null && curListener.isListenerForSource(source)) {
                            if (event == null) {
                                event = eventInfo.createSystemEvent(source);
                            }
                            assert event != null;
                            if (!processedListeners.containsKey(curListener) && event.isAppropriateListener(curListener)) {
                                processedSomeEvents = true;
                                event.processListener(curListener);
                                processedListeners.put(curListener, Boolean.TRUE);
                            }
                        }
                    }
                    if (originalDiffersFromCopy(listeners, listenersCopy)) {
                        originalDiffersFromCopy = true;
                        listenersCopy = copyListWithExclusions(listeners, processedListeners);
                    }
                }
            } while (originalDiffersFromCopy && processedSomeEvents);
        }

        return event;

    }

    private boolean originalDiffersFromCopy(Collection<SystemEventListener> original, SystemEventListener[] copy) {
        boolean foundDifference = false;
        int i = 0, originalLen = original.size(), copyLen = copy.length;

        if (originalLen == copyLen) {
            SystemEventListener originalItem, copyItem;
            Iterator<SystemEventListener> iter = original.iterator();
            while (iter.hasNext() && !foundDifference) {
                originalItem = iter.next();
                copyItem = copy[i++];
                foundDifference = originalItem != copyItem;
            }
        } else {
            foundDifference = true;
        }

        return foundDifference;
    }

    private SystemEventListener[] copyListWithExclusions(Collection<SystemEventListener> original, Map<SystemEventListener, Boolean> excludes) {
        SystemEventListener[] result = null, temp = new SystemEventListener[original.size()];
        int i = 0;
        for (SystemEventListener cur : original) {
            if (!excludes.containsKey(cur)) {
                temp[i++] = cur;
            }
        }
        result = new SystemEventListener[i];
        System.arraycopy(temp, 0, result, 0, i);

        return result;
    }

}
