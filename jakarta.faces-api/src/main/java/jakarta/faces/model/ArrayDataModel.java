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
 * <strong>ArrayDataModel</strong> is a convenience implementation of {@link DataModel} that wraps an array of Java
 * objects.
 * </p>
 */

public class ArrayDataModel<E> extends DataModel<E> {

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Construct a new {@link ArrayDataModel} with no specified wrapped data.
     * </p>
     */
    public ArrayDataModel() {

        this(null);

    }

    /**
     * <p>
     * Construct a new {@link ArrayDataModel} wrapping the specified array.
     * </p>
     *
     * @param array Array to be wrapped (if any)
     */
    public ArrayDataModel(E[] array) {

        super();
        setWrappedData(array);

    }

    // ------------------------------------------------------ Instance Variables

    // The array we are wrapping
    private Object[] array;

    // The current row index (zero relative)
    private int index = -1;

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return <code>true</code> if there is <code>wrappedData</code> available, and the current value of
     * <code>rowIndex</code> is greater than or equal to zero, and less than the length of the array. Otherwise, return
     * <code>false</code>.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row availability
     */
    @Override
    public boolean isRowAvailable() {

        return array != null && index >= 0 && index < array.length;

    }

    /**
     * <p>
     * If there is <code>wrappedData</code> available, return the length of the array. If no <code>wrappedData</code> is
     * available, return -1.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row count
     */
    @Override
    public int getRowCount() {

        if (array == null) {
            return -1;
        }
        return array.length;

    }

    /**
     * <p>
     * If row data is available, return the array element at the index specified by <code>rowIndex</code>. If no wrapped
     * data is available, return <code>null</code>.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row data
     * @throws IllegalArgumentException if now row data is available at the currently specified row index
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public E getRowData() {

        if (array == null) {
            return null;
        } else if (!isRowAvailable()) {
            throw new NoRowAvailableException();
        } else {
            return (E) array[index];
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
        if (array == null) {
            return;
        }
        DataModelListener[] listeners = getDataModelListeners();
        if (old != index && listeners != null) {
            Object rowData = null;
            if (isRowAvailable()) {
                rowData = getRowData();
            }
            DataModelEvent event = new DataModelEvent(this, index, rowData);
            int n = listeners.length;
            for (DataModelListener listener : listeners) {
                if (null != listener) {
                    listener.rowSelected(event);
                }
            }
        }

    }

    @Override
    public Object getWrappedData() {

        return this.array;

    }

    /**
     * @throws ClassCastException if <code>data</code> is non-<code>null</code> and is not an array of Java objects.
     */
    @Override
    public void setWrappedData(Object data) {

        if (data == null) {
            array = null;
            setRowIndex(-1);
        } else {
            array = (Object[]) data;
            index = -1;
            setRowIndex(0);
        }

    }

}
