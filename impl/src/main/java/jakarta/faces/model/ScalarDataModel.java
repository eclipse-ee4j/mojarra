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
 * <strong>ScalarDataModel</strong> is a convenience implementation of {@link DataModel} that wraps an individual Java
 * object.
 * </p>
 */

public class ScalarDataModel<E> extends DataModel<E> {

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Construct a new {@link ScalarDataModel} with no specified wrapped data.
     * </p>
     */
    public ScalarDataModel() {

        this(null);

    }

    /**
     * <p>
     * Construct a new {@link ScalarDataModel} wrapping the specified scalar object.
     * </p>
     *
     * @param scalar Scalar to be wrapped (if any)
     */
    public ScalarDataModel(E scalar) {

        super();
        setWrappedData(scalar);

    }

    // ------------------------------------------------------ Instance Variables

    // The currently selected row index (zero-relative)
    private int index;

    // The scalar we are wrapping
    private E scalar;

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return <code>true</code> if there is <code>wrappedData</code> available, and the current value of
     * <code>rowIndex</code> is zero. Otherwise, return <code>false</code>.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row availability
     */
    @Override
    public boolean isRowAvailable() {

        return scalar != null && index == 0;

    }

    /**
     * <p>
     * If there is <code>wrappedData</code> available, return 1. If no <code>wrappedData</code> is available, return -1.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row count
     */
    @Override
    public int getRowCount() {

        if (scalar == null) {
            return -1;
        }
        return 1;

    }

    /**
     * <p>
     * If wrapped data is available, return the wrapped data instance. Otherwise, return <code>null</code>.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row data
     * @throws IllegalArgumentException if now row data is available at the currently specified row index
     */
    @Override
    public E getRowData() {

        if (scalar == null) {
            return null;
        } else if (!isRowAvailable()) {
            throw new NoRowAvailableException();
        } else {
            return scalar;
        }

    }

    /**
     * @throws jakarta.faces.FacesException {@inheritDoc}
     */
    @Override
    public int getRowIndex() {

        return index;

    }

    /**
     * @throws jakarta.faces.FacesException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public void setRowIndex(int rowIndex) {

        if (rowIndex < -1) {
            throw new IllegalArgumentException();
        }
        int old = index;
        index = rowIndex;
        if (scalar == null) {
            return;
        }
        DataModelListener[] listeners = getDataModelListeners();
        if (old != index && listeners != null) {
            Object rowData = null;
            if (isRowAvailable()) {
                rowData = getRowData();
            }
            DataModelEvent event = new DataModelEvent(this, index, rowData);
            for (DataModelListener listener : listeners) {
                if (null != listener) {
                    listener.rowSelected(event);
                }
            }
        }

    }

    @Override
    public Object getWrappedData() {

        return this.scalar;

    }

    /**
     * @throws ClassCastException {@inheritDoc}
     */
    @Override
    public void setWrappedData(Object data) {

        if (data == null) {
            scalar = null;
            setRowIndex(-1);
        } else {
            scalar = (E) data;
            index = -1;
            setRowIndex(0);
        }

    }

}
