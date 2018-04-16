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

package javax.faces.event;


import java.util.EventListener;


/**
 * <p>A generic base interface for event listeners for various types of
 * {@link FacesEvent}s.  All listener interfaces for specific
 * {@link FacesEvent} event types must extend this interface.</p>
 *
 * <p>Implementations of this interface must have a zero-args public
 * constructor.  If the class that implements this interface has state
 * that needs to be saved and restored between requests, the class must
 * also implement {@link javax.faces.component.StateHolder}.</p>
 */

public interface FacesListener extends EventListener {

}
