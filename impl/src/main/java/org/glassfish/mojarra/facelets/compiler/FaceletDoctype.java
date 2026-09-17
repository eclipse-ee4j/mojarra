/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.mojarra.facelets.compiler;

import jakarta.faces.component.Doctype;

public class FaceletDoctype implements Doctype {

    private final String rootElement;
    private final String _public;
    private final String system;

    public FaceletDoctype(String rootElement, String _public, String system) {
        this.rootElement = rootElement;
        this._public = _public;
        this.system = system;
    }

    @Override
    public String getRootElement() {
        return rootElement;
    }

    @Override
    public String getPublic() {
        return _public;
    }

    @Override
    public String getSystem() {
        return system;
    }

}
