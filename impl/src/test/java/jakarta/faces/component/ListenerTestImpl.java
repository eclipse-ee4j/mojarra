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

package jakarta.faces.component;

import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesListener;

public class ListenerTestImpl implements FacesListener {

    public ListenerTestImpl(String id, String fromId, String toId) {
        this.id = id;
        this.fromId = fromId; // When an event with this id is received ...
        this.toId = toId; // queue an additional event with this id
    }

    public ListenerTestImpl(String id) {
        this.id = id;
    }

    public ListenerTestImpl(String id, boolean abort) {
        this.id = id;
        this.abort = abort;
    }

    private boolean abort = false;
    private String fromId = null;
    private String id = null;
    private String toId = null;

    public String getId() {
        return this.id;
    }

    public void processTest(EventTestImpl event) {
        if (getId() != null) {
            trace(getId());
        }
        if (event.getId() != null) {
            trace(event.getId());
            if (event.getId().equals(fromId)) {
                event.getComponent().queueEvent(new EventTestImpl(event.getComponent(), toId));
            }
        }
        if (abort) {
            throw new AbortProcessingException();
        }
    }

    // Accumulated trace log
    private static StringBuffer trace = new StringBuffer();

    // Append to the current trace log (or clear if null)
    public static void trace(String text) {
        if (text == null) {
            trace.setLength(0);
        } else {
            trace.append('/');
            trace.append(text);
        }
    }

    // Retrieve the current trace log
    public static String trace() {
        return trace.toString();
    }
}
