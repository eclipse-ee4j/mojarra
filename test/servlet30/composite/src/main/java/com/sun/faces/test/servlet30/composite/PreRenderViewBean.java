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

package com.sun.faces.test.servlet30.composite;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class PreRenderViewBean {

    private List<PreRenderViewDataItem> data;

    public List<PreRenderViewDataItem> getData() {
        if (data == null) {
            data = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                data.add(this.createTestDataItem("Item" + i));
            }
        }

        return data;
    }

    public PreRenderViewDataItem createTestDataItem(String text) {
        PreRenderViewDataItem item = new PreRenderViewDataItem();
        item.setText(text);
        return item;
    }
}
