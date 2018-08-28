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

package com.sun.faces.test.servlet30.facelets;

import static java.util.Collections.singletonMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.ListDataModel;
import javax.inject.Named;

@Named
@SessionScoped
public class ForEachBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<String> names;
    private ArrayList<Integer> numbers;
    private ArrayList<String> repeatValues;
    private Object[] pages;
    private ListDataModel<String> dataModel;

    int count;

    final int max = 10;

    public ForEachBean() {
        init();
        append();
    }

    private void init() {
        count = 0;
        names = new ArrayList<String>();
        numbers = new ArrayList<Integer>();
        repeatValues = new ArrayList<String>();
        dataModel = new ListDataModel<>(names);

        Map<String, String> item1 = singletonMap("page", "includedDynamically01.xhtml");
        Map<String, String> item2 = singletonMap("page", "includedDynamically02.xhtml");

        Object[] myPages = { item1, item2 };

        pages = myPages;
    }

    public int getCount() {
        return count;
    }

    public boolean isEvenCount() {
        return count % 2 == 0;
    }

    private void append() {
        count++;

        if (names.size() > 10) {
            names.clear();
        }

        if (numbers.size() > 10) {
            numbers.clear();
        }

        if (repeatValues.size() > 10) {
            repeatValues.clear();
        }

        names.add("Bobby");
        names.add("Jerry");
        names.add("Phil");

        for (int i = 0; i < 3; i++) {
            numbers.add(new Integer(i));
        }

        repeatValues.add("Blue");
        repeatValues.add("Red");
        repeatValues.add("Green");
    }

    public void modify(PhaseEvent e) {
        if (!e.getPhaseId().equals(PhaseId.APPLY_REQUEST_VALUES)) {
            return;
        }
        append();

    }

    public String getReset() {
        names.clear();
        numbers.clear();
        repeatValues.clear();
        count = 0;

        append();

        return "true";
    }

    public ArrayList<String> getRepeatValues() {
        return repeatValues;
    }

    public ArrayList<Integer> getNumbers() {
        return numbers;
    }

    public List<String> getNames() {
        return names;
    }

    public Object[] getPages() {
        return pages;
    }

    public ListDataModel getDataModel() {
        return dataModel;
    }

}
