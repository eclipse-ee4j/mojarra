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

package com.sun.faces.test.servlet30.ajax;

import java.io.Serializable;

public class Issue1581Bug implements Serializable {
    private boolean uiselected;
    private String text;

    public String getText() { 
        return text; 
    }

    public void setText(String text) { 
        this.text = text; 
    }

    public boolean getUiselected() { 
        return uiselected; 
    }

    public void setUiselected(boolean uiselected) { 
        this.uiselected = uiselected; 
    }

    public Issue1581Bug(boolean uiselected, String text) { 
        this.uiselected = uiselected; this.text = text; 
    }

    public Issue1581Bug() {
    }

    @Override
    public String toString() { 
        return text+" "+uiselected; 
    }
}
