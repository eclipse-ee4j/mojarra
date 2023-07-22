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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.faces.component.UIData;

/**
 * <p>
 * <span class="changed_modified_2_0"><strong>DataModel</strong></span> is an abstraction around arbitrary data binding
 * technologies that can be used to adapt a variety of data sources for use by Jakarta Faces components that
 * support per-row processing for their child components (such as {@link UIData}.
 * </p>
 *
 * <p>
 * The data collection underlying a {@link DataModel} instance is modeled as a collection of row objects that can be
 * accessed by a zero-relative cursor (row index). The APIs provide mechanisms to position to a specified zero-relative
 * row index, and to retrieve an object that represents the data that corresponds to the current row index.
 * </p>
 *
 * <p>
 * A concrete {@link DataModel} instance is attached to a particular collection of underlying data by calling the
 * <code>setWrappedData()</code> method. It can be detached from that underlying data collection by passing a
 * <code>null</code> parameter to this method.
 * </p>
 *
 * <p>
 * Concrete {@link DataModel} implementations must provide a public zero-arguments constructor that calls
 * <code>setWrappedData(null)</code>. A convenience constructor that takes a wrapped object of the appropriate type (and
 * passes it on via a call to <code>setWrappedData()</code>, should also be provided.
 * </p>
 *
 * <p>
 * Event listeners may be registered to receive notifications of when a new row index is selected.
 * </p>
 */

public abstract class DataModel<E> implements Iterable<E> {

    private static final DataModelListener[] EMPTY_DATA_MODEL_LISTENER = new DataModelListener[0];

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return a flag indicating whether there is <code>rowData</code> available at the current <code>rowIndex</code>. If no
     * <code>wrappedData</code> is available, return <code>false</code>.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row availability
     *
     * @return true if and only if there is data available at the current index, false otherwise.
     */
    public abstract boolean isRowAvailable();

    /**
     * <p>
     * Return the number of rows of data objects represented by this {@link DataModel}. If the number of rows is unknown, or
     * no <code>wrappedData</code> is available, return -1.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row count
     *
     * @return the number of rows of data represented by this {@code DataModel}
     */
    public abstract int getRowCount();

    /**
     * <p>
     * Return an object representing the data for the currently selected row index. If no <code>wrappedData</code> is
     * available, return <code>null</code>.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row data
     * @throws IllegalArgumentException if now row data is available at the currently specified row index
     *
     * @return an object representing the data for the currently selected row index
     */
    public abstract E getRowData();

    /**
     * <p>
     * Return the zero-relative index of the currently selected row. If we are not currently positioned on a row, or no
     * <code>wrappedData</code> is available, return -1.
     * </p>
     *
     * @throws jakarta.faces.FacesException if an error occurs getting the row index
     *
     * @return the index of the currently selected row
     */
    public abstract int getRowIndex();

    /**
     * <p>
     * Set the zero-relative index of the currently selected row, or -1 to indicate that we are not positioned on a row. It
     * is possible to set the row index at a value for which the underlying data collection does not contain any row data.
     * Therefore, callers may use the <code>isRowAvailable()</code> method to detect whether row data will be available for
     * use by the <code>getRowData()</code> method.
     * </p>
     *
     * <p>
     * If there is no <code>wrappedData</code> available when this method is called, the specified <code>rowIndex</code> is
     * stored (and may be retrieved by a subsequent call to <code>getRowData()</code>), but no event is sent. Otherwise, if
     * the currently selected row index is changed by this call, a {@link DataModelEvent} will be sent to the
     * <code>rowSelected()</code> method of all registered {@link DataModelListener}s.
     * </p>
     *
     * @param rowIndex The new zero-relative index (must be non-negative)
     *
     * @throws jakarta.faces.FacesException if an error occurs setting the row index
     * @throws IllegalArgumentException if <code>rowIndex</code> is less than -1
     */
    public abstract void setRowIndex(int rowIndex);

    /**
     * <p>
     * Return the object representing the data wrapped by this {@link DataModel}, if any.
     * </p>
     *
     * @return the {@code Object} that this model wraps.
     */
    public abstract Object getWrappedData();

    /**
     * <p>
     * Set the object representing the data collection wrapped by this {@link DataModel}. If the specified <code>data</code>
     * is <code>null</code>, detach this {@link DataModel} from any previously wrapped data collection instead.
     * </p>
     *
     * <p>
     * If <code>data</code> is non-<code>null</code>, the currently selected row index must be set to zero, and a
     * {@link DataModelEvent} must be sent to the <code>rowSelected()</code> method of all registered
     * {@link DataModelListener}s indicating that this row is now selected.
     * </p>
     *
     * @param data Data collection to be wrapped, or <code>null</code> to detach from any previous data collection
     *
     * @throws ClassCastException if <code>data</code> is not of the appropriate type for this {@link DataModel}
     * implementation
     */
    public abstract void setWrappedData(Object data);

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>
     * The list of registered {@link DataModelListener}s for this {@link DataModel}. This variable will be <code>null</code>
     * unless there is at least one registered listener.
     * </p>
     */
    private List<DataModelListener> listeners = null;

    // --------------------------------------------- Event Listener Registration

    /**
     * <p>
     * Add a new {@link DataModelListener} to the set interested in notifications from this {@link DataModel}.
     * </p>
     *
     * @param listener The new {@link DataModelListener} to be registered
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    public void addDataModelListener(DataModelListener listener) {

        if (listener == null) {
            throw new NullPointerException();
        }
        if (listeners == null) {
            // noinspection CollectionWithoutInitialCapacity
            listeners = new ArrayList<>();
        }
        listeners.add(listener);

    }

    /**
     * <p>
     * Return the set of {@link DataModelListener}s interested in notifications from this {@link DataModel}. If there are no
     * such listeners, an empty array is returned.
     * </p>
     *
     * @return the listeners for this instance, or an empty array
     */
    public DataModelListener[] getDataModelListeners() {

        if (listeners == null) {
            return EMPTY_DATA_MODEL_LISTENER;
        } else {
            return listeners.toArray(new DataModelListener[listeners.size()]);
        }

    }

    /**
     * <p>
     * Remove an existing {@link DataModelListener} from the set interested in notifications from this {@link DataModel}.
     * </p>
     *
     * @param listener The old {@link DataModelListener} to be deregistered
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    public void removeDataModelListener(DataModelListener listener) {

        if (listener == null) {
            throw new NullPointerException();
        }
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listeners = null;
            }
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Return a read-only <code>Iterator</code> over the row data for this model.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public Iterator<E> iterator() {

        return new DataModelIterator<>(this);

    }

    // ---------------------------------------------------------- Nested Classes

    @SuppressWarnings({ "unchecked" })
    private static final class DataModelIterator<T> implements Iterator<T> {

        private final DataModel<T> model;
        private int index;

        // -------------------------------------------------------- Constructors

        DataModelIterator(DataModel<T> model) {

            this.model = model;
            this.model.setRowIndex(index);

        }

        // ----------------------------------------------- Methods from Iterator

        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {

            return model.isRowAvailable();

        }

        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public T next() {

            if (!model.isRowAvailable()) {
                throw new NoSuchElementException();
            }
            Object o = model.getRowData();
            model.setRowIndex(++index);
            return (T) o;

        }

        /**
         * Unsupported.
         *
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {

            throw new UnsupportedOperationException();

        }

    } // END DataModelIterator

}
