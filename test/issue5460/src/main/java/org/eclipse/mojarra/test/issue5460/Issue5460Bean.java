/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue5460;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue5460Bean {

    private String cc1;
    private String cc2;
    private String cc3;
    private String cc4;
    private String cc5;
    private String cc6;

    public String getCc1() {
        return cc1;
    }

    public void setCc1(String cc1) {
        this.cc1 = cc1;
    }

    public String getCc2() {
        return cc2;
    }

    public void setCc2(String cc2) {
        this.cc2 = cc2;
    }

    public String getCc3() {
        return cc3;
    }

    public void setCc3(String cc3) {
        this.cc3 = cc3;
    }

    public String getCc4() {
        return cc4;
    }

    public void setCc4(String cc4) {
        this.cc4 = cc4;
    }

    public String getCc5() {
        return cc5;
    }

    public void setCc5(String cc5) {
        this.cc5 = cc5;
    }

    public String getCc6() {
        return cc6;
    }

    public void setCc6(String cc6) {
        this.cc6 = cc6;
    }
}
