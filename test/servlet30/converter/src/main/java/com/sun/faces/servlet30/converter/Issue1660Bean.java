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

package com.sun.faces.servlet30.converter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.model.SelectItem;

@ManagedBean
@RequestScoped
public class Issue1660Bean implements Serializable {

    private Issue1660SimpleEnum simpleValue = Issue1660SimpleEnum.VALUE1;
    private Issue1660ComplexEnum complexValue = Issue1660ComplexEnum.VALUE2;

    public Issue1660SimpleEnum getSimpleValue() {
        return simpleValue;
    }

    public void setSimpleValue(Issue1660SimpleEnum simpleValue) {
        this.simpleValue = simpleValue;
    }

    public Issue1660ComplexEnum getComplexValue() {
        return complexValue;
    }

    public void setComplexValue(Issue1660ComplexEnum complexValue) {
        this.complexValue = complexValue;
    }

    public List<SelectItem> getSimpleValues() {
        List<SelectItem> ret = new ArrayList<SelectItem>();
        for (Issue1660SimpleEnum val : Issue1660SimpleEnum.values()) {
            ret.add(new SelectItem(val, val.toString()));
        }
        return ret;
    }

    public List<SelectItem> getComplexValues() {
        List<SelectItem> ret = new ArrayList<SelectItem>();
        for (Issue1660ComplexEnum val : Issue1660ComplexEnum.values()) {
            ret.add(new SelectItem(val, val.toString()));
        }
        return ret;
    }
}
