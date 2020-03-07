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

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;

import jakarta.faces.context.ResponseWriter;

/**
 * <B>ListRenderer</B> is a class that renders the current value of <code>UISelectOne<code> or <code>UISelectMany<code>
 * component as a list of options.
 */

public class ListboxRenderer extends MenuRenderer {

    // ------------------------------------------------------- Protected Methods

    @Override
    protected void writeDefaultSize(ResponseWriter writer, int itemCount) throws IOException {

        // If size not specified, default to number of items
        writer.writeAttribute("size", itemCount, "size");

    }

} // end of class ListboxRenderer
