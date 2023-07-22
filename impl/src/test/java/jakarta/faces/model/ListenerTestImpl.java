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

package jakarta.faces.model;

/**
 * <p>
 * Test implementation of DataModelListener.</p>
 */
public class ListenerTestImpl implements DataModelListener {

    // ----------------------------------------------- DataModelListener Methods
    @Override
    public void rowSelected(DataModelEvent event) {
        Object rowData = event.getRowData();
        int rowIndex = event.getRowIndex();
        trace(String.valueOf(rowIndex));
        if ((rowIndex >= 0) && (rowData == null)) {
            throw new IllegalArgumentException("rowIndex=" + rowIndex
                    + " but rowData is null");
        } else if ((rowIndex == -1) && (rowData != null)) {
            throw new IllegalArgumentException("rowIndex=" + rowIndex
                    + " but rowData is not null");
        } else if (rowIndex < -1) {
            throw new IllegalArgumentException("rowIndex=" + rowIndex);
        }

    }

    // ---------------------------------------------------------- Static Methods
    private static StringBuffer trace = new StringBuffer();

    public static String trace() {
        return (trace.toString());
    }

    public static void trace(String value) {
        if (value == null) {
            trace = new StringBuffer();
        } else {
            trace.append('/');
            trace.append(value);
        }
    }
}
