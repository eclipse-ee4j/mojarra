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

package javax.faces.application;

import javax.faces.FacesException;

/**
 * <p class="changed_added_2_2">
 * This exception is thrown by the runtime when a violation of the view protection mechanism is
 * encountered.
 * </p>
 * 
 * @since 2.2
 */

public class ProtectedViewException extends FacesException {

    private static final long serialVersionUID = -1906819977415598769L;

    public ProtectedViewException(Throwable rootCause) {
        super(rootCause);
    }

    public ProtectedViewException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtectedViewException(String message) {
        super(message);
    }

    public ProtectedViewException() {
    }

}
