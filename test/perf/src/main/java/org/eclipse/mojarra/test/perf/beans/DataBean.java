/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.perf.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Row data for the table ({@code h:dataTable}), repeat ({@code ui:repeat}), composite and foreach
 * ({@code c:forEach items}) scenarios. Scenarios group into four size tiers, each a single tunable constant below:
 * {@link #getReadonlyRows() readonly} rows, {@link #getInputRows() input} rows (also the tier the flat forms match),
 * {@link #getForeachRows() foreach} rows, and nested {@link #getGroups() groups}. Each getter builds its list lazily
 * so a view generates only the rows it renders.
 */
@Named
@ViewScoped
public class DataBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int READONLY_ROWS = 200;
    private static final int INPUT_ROWS = 35;
    private static final int FOREACH_ROWS = 100;
    private static final int GROUPS = 5;
    private static final int GROUP_ROWS = 10;

    @Inject
    private RowFactory rowFactory;

    private List<Row> readonlyRows;
    private List<Row> inputRows;
    private List<Row> foreachRows;
    private List<Group> groups;

    /** Readonly (outputs-only) table/repeat/composite rows, e.g. {@code #{dataBean.readonlyRows}}. */
    public List<Row> getReadonlyRows() {
        if (readonlyRows == null) {
            readonlyRows = rowFactory.generate(READONLY_ROWS);
        }
        return readonlyRows;
    }

    /** Per-row-input table/repeat/composite rows (plain and ajax), e.g. {@code #{dataBean.inputRows}}. */
    public List<Row> getInputRows() {
        if (inputRows == null) {
            inputRows = rowFactory.generate(INPUT_ROWS);
        }
        return inputRows;
    }

    /** Rows the {@code foreach-*} {@code c:forEach} iterates, e.g. {@code #{dataBean.foreachRows}}. */
    public List<Row> getForeachRows() {
        if (foreachRows == null) {
            foreachRows = rowFactory.generate(FOREACH_ROWS);
        }
        return foreachRows;
    }

    /** Nested scenarios: {@value #GROUPS} groups of {@value #GROUP_ROWS} rows, e.g. {@code #{dataBean.groups}}. */
    public List<Group> getGroups() {
        if (groups == null) {
            List<Group> list = new ArrayList<>(GROUPS);
            for (int g = 0; g < GROUPS; g++) {
                list.add(new Group("Group " + g, rowFactory.generate(GROUP_ROWS)));
            }
            groups = list;
        }
        return groups;
    }

    public String save() {
        return null;
    }

    public static class Group implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final List<Row> rows;
        public Group(String name, List<Row> rows) {
            this.name = name;
            this.rows = rows;
        }
        public String getName() {
            return name;
        }
        public List<Row> getRows() {
            return rows;
        }
    }
}
