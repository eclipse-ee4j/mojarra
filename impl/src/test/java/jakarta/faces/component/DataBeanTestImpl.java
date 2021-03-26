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

package jakarta.faces.component;

import java.io.Serializable;

// Test JavaBean for DataModel Tests
public class DataBeanTestImpl implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String command;

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    private String input;

    public String getInput() {
        return this.input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    private String output;

    public String getOutput() {
        return this.output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
