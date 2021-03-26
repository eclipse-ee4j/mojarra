/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Contributors to the Eclipse Foundation.
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
package com.sun.faces.test.javaee8.uiinput;

import java.io.Serializable;
import java.util.Objects;

public class Issue4734Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    public Issue4734Entity() {
        //
    }

    public Issue4734Entity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Issue4734Entity && Objects.equals(id, ((Issue4734Entity) other).id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + id + "]";
    }
}