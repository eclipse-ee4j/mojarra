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

package javax.faces.model;


import java.util.SortedMap;
import javax.servlet.jsp.jstl.sql.Result;


/**
 * <p><strong>ResultDataModel</strong> is a convenience implementation of
 * {@link DataModel} that wraps a JSTL <code>Result</code> object, typically
 * representing the results of executing an SQL query via JSTL tags.</p>
 */

public class ResultDataModel extends DataModel<SortedMap<String,Object>> {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new {@link ResultDataModel} with no specified
     * wrapped data.</p>
     */
    public ResultDataModel() {

        this(null);

    }


    /**
     * <p>Construct a new {@link ResultDataModel} wrapping the specified
     * <code>Result</code>.</p>
     *
     * @param result <code>Result</code> to be wrapped (if any)
     */
    public ResultDataModel(Result result) {

        super();
        setWrappedData(result);

    }


    // ------------------------------------------------------ Instance Variables


    // The current row index (zero relative)
    private int index = -1;


    // The Result we are wrapping
    private Result result = null;


    // The individual rows of this Result, each represented as a Map
    // with column names as keys and associated data values as values
    private SortedMap rows[] = null;


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return <code>true</code> if there is <code>wrappedData</code>
     * available, and the current value of <code>rowIndex</code> is greater
     * than or equal to zero, and less than the length of the array returned
     * by calling <code>getRows()</code> on the underlying <code>Result</code>.
     * Otherwise, return <code>false</code>.</p>
     *
     * @throws javax.faces.FacesException if an error occurs getting the row availability
     */ 
    @Override
    public boolean isRowAvailable() {

        if (result == null) {
	    return (false);
        } else if ((index >= 0) && (index < rows.length)) {
            return (true);
        } else {
            return (false);
        }

    }


    /**
     * <p>If there is <code>wrappedData</code> available, return the
     * length of the array returned by calling <code>getRows()</code>
     * on the underlying <code>Result</code>.  If no <code>wrappedData</code>
     * is available, return -1.</p>
     *
     * @throws javax.faces.FacesException if an error occurs getting the row count
     */
    @Override
    public int getRowCount() {

        if (result == null) {
	    return (-1);
        }
        return (rows.length);

    }


    /**
     * <p>If row data is available, return the <code>SortedMap</code> array
     * element at the index specified by <code>rowIndex</code> of the
     * array returned by calling <code>getRows()</code> on the underlying
     * <code>Result</code>.  If no wrapped data is available,
     * return <code>null</code>.</p>
     *
     * <p>Note that, if a non-<code>null</code> <code>Map</code> is returned
     * by this method, it will contain the values of the columns for the
     * current row, keyed by column name.  Column name comparisons must be
     * performed in a case-insensitive manner.</p>
     *
     * @throws javax.faces.FacesException if an error occurs getting the row data
     * @throws IllegalArgumentException if now row data is available
     *  at the currently specified row index
     */
    @Override
    public SortedMap<String,Object> getRowData() {

        if (result == null) {
	    return (null);
        } else if (!isRowAvailable()) {
            throw new NoRowAvailableException();
        } else {
            //noinspection unchecked
            return ((SortedMap<String,Object>)rows[index]);
        }

    }


    /**
     * @throws javax.faces.FacesException {@inheritDoc}     
     */ 
    @Override
    public int getRowIndex() {

        return (index);

    }


    /**
     * @throws javax.faces.FacesException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */ 
    @Override
    public void setRowIndex(int rowIndex) {

        if (rowIndex < -1) {
            throw new IllegalArgumentException();
        }
        int old = index;
        index = rowIndex;
	if (result == null) {
	    return;
	}
	DataModelListener [] listeners = getDataModelListeners();
        if ((old != index) && (listeners != null)) {
            SortedMap<String,Object> rowData = null;
            if (isRowAvailable()) {
                rowData = getRowData();
            }
            DataModelEvent event =
                new DataModelEvent(this, index, rowData);
            int n = listeners.length;
            for (int i = 0; i < n; i++) {
		if (null != listeners[i]) {
		    listeners[i].rowSelected(event);
		}
            }
        }

    }


    @Override
    public Object getWrappedData() {

        return (this.result);

    }


    /**
     * @throws ClassCastException if <code>data</code> is
     *  non-<code>null</code> and is not a <code>Result</code>
     */
    @Override
    public void setWrappedData(Object data) {

        if (data == null) {
            result = null;
            rows = null;
            setRowIndex(-1);
        } else {
            result = (Result) data;
            rows = result.getRows();
            index = -1;
            setRowIndex(0);
        }

    }


}
