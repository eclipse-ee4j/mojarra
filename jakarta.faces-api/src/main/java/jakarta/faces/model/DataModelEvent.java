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

import java.util.EventObject;

/**
 * <p>
 * <strong>DataModelEvent</strong> represents an event of interest to registered listeners that occurred on the
 * specified {@link DataModel}.
 * </p>
 */

public class DataModelEvent extends EventObject {

    // ------------------------------------------------------------ Constructors

    private static final long serialVersionUID = -1822980374964965366L;

    /**
     * <p>
     * Construct an event object that is associated with the specified row index and associated data.
     * </p>
     *
     * @param model The {@link DataModel} on which this event occurred
     * @param index The zero relative row index for which this event occurred, or -1 for no specific row association
     * @param data Representation of the data for the row specified by <code>index</code>, or <code>null</code> for no
     * particular row data
     */
    public DataModelEvent(DataModel model, int index, Object data) {

        super(model);
        this.index = index;
        this.data = data;

    }

    // ------------------------------------------------------ Instance Variables

    private Object data = null;

    private int index = 0;

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return the {@link DataModel} that fired this event.
     * </p>
     *
     * @return the {@link DataModel} that fired this event
     */
    public DataModel getDataModel() {

        return (DataModel) getSource();

    }

    /**
     * <p>
     * Return the object representing the data for the specified row index, or <code>null</code> for no associated row data.
     * </p>
     *
     * @return the object representing the data for the specified row index, or <code>null</code> for no associated row data
     */
    public Object getRowData() {

        return data;

    }

    /**
     * <p>
     * Return the row index for this event, or -1 for no specific row.
     * </p>
     *
     * @return the row index for this event, or -1 for no specific row
     */
    public int getRowIndex() {

        return index;

    }

}
