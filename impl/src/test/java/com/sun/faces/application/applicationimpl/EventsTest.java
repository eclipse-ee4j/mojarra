/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sun.faces.application.applicationimpl.events.EventInfo;

import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

/**
 * Pins the dispatch contract of {@code Events.processListenersAccountingForAdds}: every matching listener is
 * invoked exactly once, a listener subscribed while the listeners are being invoked is picked up, and a
 * listener not for the source is skipped. The fast path preserves this behaviour without the per-event copy
 * and map the original snapshot algorithm allocated on every published event.
 */
class EventsTest {

    private static SystemEvent invoke(List<SystemEventListener> listeners, SystemEvent event, Object source) throws Exception {
        Method method = Events.class.getDeclaredMethod("processListenersAccountingForAdds",
                List.class, SystemEvent.class, Object.class, EventInfo.class);
        method.setAccessible(true);
        return (SystemEvent) method.invoke(new Events(), listeners, event, source, mock(EventInfo.class));
    }

    private static SystemEventListener listenerForSource() {
        SystemEventListener listener = mock(SystemEventListener.class);
        when(listener.isListenerForSource(any())).thenReturn(true);
        return listener;
    }

    @Test
    void everyMatchingListenerIsInvokedExactlyOnce() throws Exception {
        SystemEventListener a = listenerForSource(), b = listenerForSource(), c = listenerForSource();
        List<SystemEventListener> listeners = new ArrayList<>(List.of(a, b, c));
        SystemEvent event = mock(SystemEvent.class);
        when(event.isAppropriateListener(any())).thenReturn(true);

        invoke(listeners, event, new Object());

        verify(event, times(1)).processListener(a);
        verify(event, times(1)).processListener(b);
        verify(event, times(1)).processListener(c);
    }

    @Test
    void aListenerSubscribedDuringDispatchIsAlsoInvoked() throws Exception {
        SystemEventListener first = listenerForSource();
        SystemEventListener subscribedDuringDispatch = listenerForSource();
        List<SystemEventListener> listeners = new ArrayList<>();
        listeners.add(first);

        SystemEvent event = mock(SystemEvent.class);
        when(event.isAppropriateListener(any())).thenReturn(true);
        // Invoking 'first' subscribes another view listener; it must still be invoked, exactly once.
        doAnswer(invocation -> {
            listeners.add(subscribedDuringDispatch);
            return null;
        }).when(event).processListener(first);

        invoke(listeners, event, new Object());

        verify(event, times(1)).processListener(first);
        verify(event, times(1)).processListener(subscribedDuringDispatch);
    }

    @Test
    void aListenerNotForTheSourceIsSkipped() throws Exception {
        SystemEventListener matching = listenerForSource();
        SystemEventListener nonMatching = mock(SystemEventListener.class);
        when(nonMatching.isListenerForSource(any())).thenReturn(false);
        List<SystemEventListener> listeners = new ArrayList<>(List.of(matching, nonMatching));
        SystemEvent event = mock(SystemEvent.class);
        when(event.isAppropriateListener(any())).thenReturn(true);

        invoke(listeners, event, new Object());

        verify(event, times(1)).processListener(matching);
        verify(event, never()).processListener(nonMatching);
    }
}
