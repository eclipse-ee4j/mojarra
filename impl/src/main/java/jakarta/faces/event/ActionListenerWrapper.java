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

import jakarta.faces.FacesWrapper;

/**
 * <p class="changed_added_2_2">
 * Provides a simple implementation of {@link ActionListener} that can be subclassed by developers wishing to provide
 * specialized behavior to an existing {@link ActionListener} instance. The default implementation of all methods is to
 * call through to the wrapped {@link ActionListener}.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * Usage: extend this class and override {@link #getWrapped} to return the instance we are wrapping.
 * </p>
 *
 * </div>
 *
 * @since 2.2
 */
public abstract class ActionListenerWrapper implements ActionListener, FacesWrapper<ActionListener> {

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        getWrapped().processAction(event);
    }

    @Override
    public abstract ActionListener getWrapped();

}
