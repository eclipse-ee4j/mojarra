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

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.inject.Named;

/**
 * The target of the {@code binding} of the component the fragment holds, keyed by slot. Request scoped, as a
 * {@link UIComponent} is not thread safe and must not outlive the request, so both builds of a postback read back
 * the very component the build before them put here.
 */
@Named
@RequestScoped
public class Issue6001Holder {

    private final Map<String, UIComponent> components = new HashMap<>();

    public Map<String, UIComponent> getComponents() {
        return components;
    }
}
