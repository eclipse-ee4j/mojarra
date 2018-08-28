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

package com.sun.faces.systest.model;

import java.util.List;
import java.util.ArrayList;

public class EnumBean {

    public enum Simple {
        Value1, Value2, Value3, Value4
    }

    public enum Simple2 {
        Value
    }

    private Simple selected;
    private Simple selected2;
    private Simple selected3;
    private Simple[] mSelected;
    public List<Simple> lSelected;

    public EnumBean() {
        selected = Simple.Value2;
        selected2 = Simple.Value3;
        selected3 = Simple.Value4;
        mSelected = new Simple[] { Simple.Value1, Simple.Value3 };
        lSelected = new ArrayList<Simple>(4);
        lSelected.add(Simple.Value2);
        lSelected.add(Simple.Value4);
    }

    public Simple getSelected() {
        return selected;
    }

    public void setSelected(Simple selected) {
        this.selected = selected;
    }

    public Simple getSelected2() {
        return selected2;
    }

    public void setSelected2(Simple selected2) {
        this.selected2 = selected2;
    }

    public Simple getSelected3() {
        return selected3;
    }

    public void setSelected3(Simple selected3) {
        this.selected3 = selected3;
    }

    public Simple[] getSelectedArray() {
        return mSelected;
    }

    public void setSelectedArray(Simple[] mSelected) {
        this.mSelected = mSelected;
    }

    public List getSelectedList() {
        return lSelected;
    }

    public void setSelectedList(List<Simple> lSelected) {
        this.lSelected = lSelected;
    }

} // END EnumBean
