/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.issue6001;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * A view scoped bean holding the type of each slot, so that the restore of a postback sees the types the response was
 * rendered from and the render of that same postback sees the types the action left behind.
 */
@Named
@ViewScoped
public class Issue6001Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> types = new ArrayList<>(List.of("text", "table"));

    public void toggle() {
        types.set(0, "text".equals(types.get(0)) ? "table" : "text");
    }

    public List<String> getTypes() {
        return types;
    }
}
