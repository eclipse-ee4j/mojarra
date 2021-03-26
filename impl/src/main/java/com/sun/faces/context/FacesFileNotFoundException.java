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

package com.sun.faces.context;

import java.io.FileNotFoundException;

/*
 * This exception thrown in cases when a resource is not found entirely
 * due to error originating in the user agent, as opposed to an error
 * originating due to the server side code.  For example it is thrown
 * when the user agent requests a non-existent Facelet page.  It is not
 * thrown when a Facelet template client cannot load it's template.
 *
 * The default ExceptionHandler looks for the existence of this
 * exception as a way to tell when it should send an HTTP 404 status
 * code, or an HTTP 500 status code.

 */
public class FacesFileNotFoundException extends FileNotFoundException {
    private static final long serialVersionUID = 7593137790944497673L;

    public FacesFileNotFoundException(String s) {
        super(s);
    }

    public FacesFileNotFoundException() {
    }

}
