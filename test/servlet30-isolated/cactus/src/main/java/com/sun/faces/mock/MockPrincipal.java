/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: MockPrincipal.java,v 1.1 2005/10/18 17:48:00 edburns Exp $
 */



package com.sun.faces.mock;


import java.security.Principal;


/**
 * <p>Mock <strong>Principal</strong> object for low-level unit tests.</p>
 */

public class MockPrincipal implements Principal {


    public MockPrincipal() {
        super();
        this.name = "";
        this.roles = new String[0];
    }


    public MockPrincipal(String name) {
        super();
        this.name = name;
        this.roles = new String[0];
    }


    public MockPrincipal(String name, String roles[]) {
        super();
        this.name = name;
        this.roles = roles;
    }


    protected String name = null;


    protected String roles[] = null;


    public String getName() {
        return (this.name);
    }


    public boolean isUserInRole(String role) {
        for (int i = 0; i < roles.length; i++) {
            if (role.equals(roles[i])) {
                return (true);
            }
        }
        return (false);
    }


    public boolean equals(Object o) {
        if (o == null) {
            return (false);
        }
        if (!(o instanceof Principal)) {
            return (false);
        }
        Principal p = (Principal) o;
        if (name == null) {
            return (p.getName() == null);
        } else {
            return (name.equals(p.getName()));
        }
    }


    public int hashCode() {
        if (name == null) {
            return ("".hashCode());
        } else {
            return (name.hashCode());
        }
    }


}
