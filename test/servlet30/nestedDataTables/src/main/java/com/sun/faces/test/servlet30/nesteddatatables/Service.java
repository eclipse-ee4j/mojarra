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

package com.sun.faces.test.servlet30.nesteddatatables;

import java.util.Vector;
import java.io.Serializable;

public class Service implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String _name;
    Vector _ports = new Vector();

    public Service() {
        _name = "";
    }

    public Service(String name) {
        _name = name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public Vector getPorts() {
        return _ports;
    }

    public void setPorts(Vector ports) {
        _ports = ports;
    }

    public void addPort(Port port) {
        _ports.addElement(port);
    }

    public void deletePort(Port port) {
        _ports.remove(port);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Service)) {
            return false;
        }

        String otherName = ((Service) o).getName();

        return _name == null ? (otherName == null) : _name.equals(otherName);
    }
}
