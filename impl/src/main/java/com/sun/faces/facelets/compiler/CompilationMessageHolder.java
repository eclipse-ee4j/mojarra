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

package com.sun.faces.facelets.compiler;

import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/*
 * The CompilationManager vends one of these
 * to store compilation messages for later use.  During page compilation,
 * if any messages need to be shown to the user, they will be
 * added using this interface.  If, during page execution,
 * this interface can be used to remove the messages.
 *
 * Currently the messages are keyed by namespace prefix.
 *
 * The EncodingHandler class is always the outermost FaceletHandler in any
 * Facelet compilation unit.  Therefore, this handler is used
 * to anchor the implementation of the CompilationMessageHolder so
 * other tags can access it.  See EncodingHandler for how to do it.
 *
 */
public interface CompilationMessageHolder {

    List<FacesMessage> getNamespacePrefixMessages(FacesContext context, String prefix);

    @Deprecated(since = "4.1", forRemoval = true)
    void removeNamespacePrefixMessages(String prefix);

    void processCompilationMessages(FacesContext context);

}
