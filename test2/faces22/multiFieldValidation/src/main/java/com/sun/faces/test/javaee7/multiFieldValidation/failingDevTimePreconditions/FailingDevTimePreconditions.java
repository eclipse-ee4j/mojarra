/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee7.multiFieldValidation.failingDevTimePreconditions;

import com.sun.faces.test.javaee7.multiFieldValidation.PasswordHolder;
import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;

@Named
@SessionScoped
@Password(groups = PasswordValidationGroup.class)
public class FailingDevTimePreconditions implements PasswordHolder, Cloneable, Serializable {
    private static final long serialVersionUID = 3090640848750726353L;

    private String password1;

    private String password2;

    public FailingDevTimePreconditions() {
        password1 = "";
        password2 = "";
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        FailingDevTimePreconditions other = (FailingDevTimePreconditions) super.clone();
        other.setPassword1(this.getPassword1());
        other.setPassword2(this.getPassword2());
        return other;
    }

    @NotNull
    @Override
    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    @NotNull
    @Override
    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

}
