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

import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;

public class EventTestImpl extends FacesEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public EventTestImpl(UIComponent component) {
        this(component, null);
    }

    public EventTestImpl(UIComponent component, String id) {
        super(component);
        this.id = id;
    }

    private String id;

    public String getId() {
        return this.id;
    }

    @Override
    public boolean isAppropriateListener(FacesListener listener) {
        return listener instanceof ListenerTestImpl;
    }

    @Override
    public void processListener(FacesListener listener) {
        ((ListenerTestImpl) listener).processTest(this);
    }
}
