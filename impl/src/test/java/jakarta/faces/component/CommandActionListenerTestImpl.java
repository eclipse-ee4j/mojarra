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

import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

/**
 * <p>
 * Test implementation of {@link ActionListener}.
 * </p>
 */
public class CommandActionListenerTestImpl implements ActionListener {

    protected String actionListenerId = null;

    public CommandActionListenerTestImpl(String actionListenerId) {
        this.actionListenerId = actionListenerId;
    }

    @Override
    public void processAction(ActionEvent event) {
        trace(actionListenerId);
    }

    // ---------------------------------------------------- Static Trace Methods
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
