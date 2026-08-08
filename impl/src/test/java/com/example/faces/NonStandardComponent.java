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

package com.example.faces;

import jakarta.faces.component.UIComponentBase;

/**
 * A component from outside {@code jakarta.faces.component}, i.e. one whose setters do not record into the
 * attributes-that-are-set list. It lives in this package precisely because the behaviour under test is decided by the
 * component's package name.
 */
public class NonStandardComponent extends UIComponentBase {

    @Override
    public String getFamily() {
        return "com.example.faces.NonStandard";
    }
}
