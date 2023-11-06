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

import java.util.Collection;

/**
 * <p class="changed_added_2_2">
 * <strong>CollectionDataModel</strong> is a convenience implementation of {@link DataModel} that wraps an
 * <code>Collection</code> of Java objects.
 * </p>
 */

public class CollectionDataModel<E> extends DataModel<E> {

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Construct a new {@link CollectionDataModel} with no specified wrapped data.
     * </p>
     */
    public CollectionDataModel() {

        this(null);

    }

    /**
     * <p>
     * Construct a new {@link CollectionDataModel} wrapping the specified list.
     * </p>
     *
     * @param collection Collection to be wrapped.
     */
    public CollectionDataModel(Collection<E> collection) {

        super();
        setWrappedData(collection);

    }

    // ------------------------------------------------------ Instance Variables

    // The current row index (zero relative)
    private int index = -1;

    private Collection<E> inner;
    private E[] arrayFromInner;

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return <code>true</code> if there is <code>wrappedData</code> available, and the current value of
     * <code>rowIndex</code> is greater than or equal to zero, and less than the size of the list. Otherwise, return
     * <code>false</code>.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row availability
     */
    @Override
    public boolean isRowAvailable() {

        return arrayFromInner != null && index >= 0 && index < arrayFromInner.length;
    }

    /**
     * <p>
     * If there is <code>wrappedData</code> available, return the length of the list. If no <code>wrappedData</code> is
     * available, return -1.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row count
     */
    @Override
    public int getRowCount() {

        if (arrayFromInner == null) {
            return -1;
        }
        return arrayFromInner.length;

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
    @Override
    public E getRowData() {

        if (arrayFromInner == null) {
            return null;
        } else if (!isRowAvailable()) {
            throw new NoRowAvailableException();
        } else {
            return arrayFromInner[index];
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
        if (arrayFromInner == null) {
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

        return this.inner;

    }

    /**
     * Set the wrapped data.
     *
     * @param data the wrapped data.
     * @throws ClassCastException if <code>data</code> is non-<code>null</code> and is not a <code>Collection</code>
     */
    @Override
    public void setWrappedData(Object data) {
        if (data == null) {
            inner = null;
            arrayFromInner = null;
            setRowIndex(-1);
        } else {
            inner = (Collection<E>) data;
            arrayFromInner = (E[]) new Object[inner.size()];
            inner.toArray(arrayFromInner);
            setRowIndex(0);
        }
    }
}
