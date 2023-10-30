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

package jakarta.faces.event;

/**
 * <p class="changed_added_2_0">
 * A generic base interface for event listeners for various types of {@link BehaviorEvent}s. All listener interfaces for
 * specific {@link BehaviorEvent} event types must extend this interface.
 * </p>
 *
 * <p>
 * Implementations of this interface must have a zero-args public constructor. If the class that implements this
 * interface has state that needs to be saved and restored between requests, the class must also implement
 * {@link jakarta.faces.component.StateHolder}.
 * </p>
 *
 * @since 2.0
 */
public interface BehaviorListener extends FacesListener {

}
