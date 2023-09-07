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

package jakarta.faces.component;

import static com.sun.faces.util.Util.extractFirstNumericSegment;
import static com.sun.faces.util.Util.isNestedInIterator;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PostValidateEvent;
import jakarta.faces.event.PreValidateEvent;
import jakarta.faces.model.ArrayDataModel;
import jakarta.faces.model.CollectionDataModel;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.FacesDataModel;
import jakarta.faces.model.IterableDataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.ResultSetDataModel;
import jakarta.faces.model.ScalarDataModel;

// ------------------------------------------------------------- Private Classes
// Private class to represent saved state information

/**
 * <p>
 * <strong class="changed_modified_2_0_rev_a changed_modified_2_1 changed_modified_2_2">UIData</strong> is a
 * {@link UIComponent} that supports data binding to a collection of data objects represented by a {@link DataModel}
 * instance, which is the current value of this component itself (typically established via a {@link ValueExpression}).
 * During iterative processing over the rows of data in the data model, the object for the current row is exposed as a
 * request attribute under the key specified by the <code>var</code> property.
 * </p>
 * <p>
 * Only children of type {@link UIColumn} should be processed by renderers associated with this component.
 * </p>
 * <p>
 * By default, the <code>rendererType</code> property is set to <code>jakarta.faces.Table</code>. This value can be
 * changed by calling the <code>setRendererType()</code> method.
 * </p>
 */

public class UIData extends UIComponentBase implements NamingContainer, UniqueIdVendor {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Data";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Data";

    // --------------------------------------------------------------- Constants

    private static final ListDataModel EMPTY_DATA_MODEL = new ListDataModel(Collections.emptyList());

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIData} instance with default property values.
     * </p>
     */
    public UIData() {

        super();
        setRendererType("jakarta.faces.Table");

    }

    // ------------------------------------------------------ Instance Variables

    /**
     * Properties that are tracked by state saving.
     */
    enum PropertyKeys {
        /**
         * <p>
         * The first row number (zero-relative) to be displayed.
         * </p>
         */
        first,

        /**
         * <p>
         * The zero-relative index of the current row number, or -1 for no current row association.
         * </p>
         */
        rowIndex,

        /**
         * <p>
         * The number of rows to display, or zero for all remaining rows in the table.
         * </p>
         */
        rows,

        /**
         * <p>
         * This map contains <code>SavedState</code> instances for each descendant component, keyed by the client identifier of
         * the descendant. Because descendant client identifiers will contain the <code>rowIndex</code> value of the parent,
         * per-row state information is actually preserved.
         * </p>
         */
        saved,

        /**
         * <p>
         * The local value of this {@link UIComponent}.
         * </p>
         */
        value,

        /**
         * <p>
         * The request scope attribute under which the data object for the current row will be exposed when iterating.
         * </p>
         */
        var,

        /**
         * <p>
         * Last id vended by {@link UIData#createUniqueId(jakarta.faces.context.FacesContext, String)}.
         * </p>
         */
        lastId,

        /**
         *
         */
        rowStatePreserved
    }

    /**
     * <p>
     * The {@link DataModel} associated with this component, lazily instantiated if requested. This object is not part of
     * the saved and restored state of the component.
     * </p>
     */
    private DataModel model = null;

    /**
     * <p>
     * During iteration through the rows of this table, This ivar is used to store the previous "var" value for this
     * instance. When the row iteration is complete, this value is restored to the request map.
     */
    private Object oldVar;

    /**
     * <p>
     * Holds the base client ID that will be used to generate per-row client IDs (this will be null if this UIData is nested
     * within another).
     * </p>
     *
     * <p>
     * This is not part of the component state.
     * </p>
     */
    private String baseClientId = null;

    /**
     * <p>
     * Length of the cached <code>baseClientId</code> plus one for the {@link UINamingContainer#getSeparatorChar}.
     * </p>
     *
     * <p>
     * This is not part of the component state.
     * </p>
     */
    private int baseClientIdLength;

    /**
     * <p>
     * StringBuilder used to build per-row client IDs.
     * </p>
     *
     * <p>
     * This is not part of the component state.
     * </p>
     */
    private StringBuilder clientIdBuilder = null;

    /**
     * <p>
     * Flag indicating whether or not this UIData instance is nested within another UIData instance
     * </p>
     *
     * <p>
     * This is not part of the component state.
     * </p>
     */
    private Boolean isNested = null;

    private Map<String, Object> _rowDeltaStates = new HashMap<>();
    private Map<String, Object> _rowTransientStates = new HashMap<>();

    private Object _initialDescendantFullComponentState = null;

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {

        return COMPONENT_FAMILY;

    }

    /**
     * <p>
     * Return the zero-relative row number of the first row to be displayed.
     * </p>
     *
     * @return the row number.
     */
    public int getFirst() {

        return (Integer) getStateHelper().eval(PropertyKeys.first, 0);

    }

    /**
     * <p>
     * Set the zero-relative row number of the first row to be displayed.
     * </p>
     *
     * @param first New first row number
     *
     * @throws IllegalArgumentException if <code>first</code> is negative
     */
    public void setFirst(int first) {

        if (first < 0) {
            throw new IllegalArgumentException(String.valueOf(first));
        }
        getStateHelper().put(PropertyKeys.first, first);

    }

    /**
     * <p>
     * Return the footer facet of this component (if any). A convenience method for <code>getFacet("footer")</code>.
     * </p>
     *
     * @return the footer facet.
     */
    public UIComponent getFooter() {

        return getFacet("footer");

    }

    /**
     * <p>
     * Set the footer facet of this component. A convenience method for <code>getFacets().put("footer", footer)</code>.
     * </p>
     *
     * @param footer the new footer facet
     *
     * @throws NullPointerException if <code>footer</code> is <code>null</code>
     */
    public void setFooter(UIComponent footer) {

        getFacets().put("footer", footer);

    }

    /**
     * <p>
     * Return the header facet of this component (if any). A convenience method for <code>getFacet("header")</code>.
     * </p>
     *
     * @return the header facet.
     */
    public UIComponent getHeader() {

        return getFacet("header");

    }

    /**
     * <p>
     * Set the header facet of this component. A convenience method for <code>getFacets().put("header", header)</code>.
     * </p>
     *
     * @param header the new header facet
     *
     * @throws NullPointerException if <code>header</code> is <code>null</code>
     */
    public void setHeader(UIComponent header) {

        getFacets().put("header", header);

    }

    /**
     * <p>
     * Return a flag indicating whether there is <code>rowData</code> available at the current <code>rowIndex</code>. If no
     * <code>wrappedData</code> is available, return <code>false</code>.
     * </p>
     *
     * @return whether the row is available.
     *
     * @throws FacesException if an error occurs getting the row availability
     */
    public boolean isRowAvailable() {

        return getDataModel().isRowAvailable();

    }

    /**
     * <p>
     * Return the number of rows in the underlying data model. If the number of available rows is unknown, return -1.
     * </p>
     *
     * @return the row count.
     * @throws FacesException if an error occurs getting the row count
     */
    public int getRowCount() {

        return getDataModel().getRowCount();

    }

    /**
     * <p>
     * Return the data object representing the data for the currently selected row index, if any.
     * </p>
     *
     * @return the row data.
     *
     * @throws FacesException if an error occurs getting the row data
     * @throws IllegalArgumentException if now row data is available at the currently specified row index
     */
    public Object getRowData() {

        return getDataModel().getRowData();

    }

    /**
     * <p>
     * Return the zero-relative index of the currently selected row. If we are not currently positioned on a row, return -1.
     * This property is <strong>not</strong> enabled for value binding expressions.
     * </p>
     *
     * @return the row index.
     *
     * @throws FacesException if an error occurs getting the row index
     */
    public int getRowIndex() {

        return (Integer) getStateHelper().eval(PropertyKeys.rowIndex, -1);

    }

    /**
     * <p>
     * <span class="changed_modified_2_1">Set</span> the zero relative index of the current row, or -1 to indicate that no
     * row is currently selected, by implementing the following algorithm. It is possible to set the row index at a value
     * for which the underlying data collection does not contain any row data. Therefore, callers may use the
     * <code>isRowAvailable()</code> method to detect whether row data will be available for use by the
     * <code>getRowData()</code> method.
     * </p>
     *
     * <p class="changed_added_2_1">
     * To support transient state among descendents, please consult the specification for {@link #setRowStatePreserved},
     * which details the requirements for <code>setRowIndex()</code> when the <code>rowStatePreserved</code> JavaBeans
     * property is set to <code>true</code>.
     * </p>
     *
     * <ul>
     * <li>Save current state information for all descendant components (as described below).
     * <li>Store the new row index, and pass it on to the {@link DataModel} associated with this {@link UIData}
     * instance.</li>
     * <li>If the new <code>rowIndex</code> value is -1:
     * <ul>
     * <li>If the <code>var</code> property is not null, remove the corresponding request scope attribute (if any).</li>
     * <li>Reset the state information for all descendant components (as described below).</li>
     * </ul>
     * </li>
     * <li>If the new <code>rowIndex</code> value is not -1:
     * <ul>
     * <li>If the <code>var</code> property is not null, call <code>getRowData()</code> and expose the resulting data object
     * as a request scope attribute whose key is the <code>var</code> property value.</li>
     * <li>Reset the state information for all descendant components (as described below).
     * </ul>
     * </li>
     * </ul>
     *
     * <p>
     * To save current state information for all descendant components, {@link UIData} must maintain per-row information for
     * each descendant as follows:
     * </p>
     * <ul>
     * <li>If the descendant is an instance of <code>EditableValueHolder</code>, save the state of its
     * <code>localValue</code> property.</li>
     * <li>If the descendant is an instance of <code>EditableValueHolder</code>, save the state of the
     * <code>localValueSet</code> property.</li>
     * <li>If the descendant is an instance of <code>EditableValueHolder</code>, save the state of the <code>valid</code>
     * property.</li>
     * <li>If the descendant is an instance of <code>EditableValueHolder</code>, save the state of the
     * <code>submittedValue</code> property.</li>
     * </ul>
     *
     * <p>
     * To restore current state information for all descendant components, {@link UIData} must reference its previously
     * stored information for the current <code>rowIndex</code> and call setters for each descendant as follows:
     * </p>
     * <ul>
     * <li>If the descendant is an instance of <code>EditableValueHolder</code>, restore the <code>value</code>
     * property.</li>
     * <li>If the descendant is an instance of <code>EditableValueHolder</code>, restore the state of the
     * <code>localValueSet</code> property.</li>
     * <li>If the descendant is an instance of <code>EditableValueHolder</code>, restore the state of the <code>valid</code>
     * property.</li>
     * <li>If the descendant is an instance of <code>EditableValueHolder</code>, restore the state of the
     * <code>submittedValue</code> property.</li>
     * </ul>
     *
     * @param rowIndex The new row index value, or -1 for no associated row
     *
     * @throws FacesException if an error occurs setting the row index
     * @throws IllegalArgumentException if <code>rowIndex</code> is less than -1
     */
    public void setRowIndex(int rowIndex) {
        if (isRowStatePreserved()) {
            setRowIndexRowStatePreserved(rowIndex);
        } else {
            setRowIndexWithoutRowStatePreserved(rowIndex);
        }
    }

    private void setRowIndexWithoutRowStatePreserved(int rowIndex) {
        // Save current state for the previous row index
        saveDescendantState();

        // Update to the new row index
        // this.rowIndex = rowIndex;
        getStateHelper().put(PropertyKeys.rowIndex, rowIndex);
        DataModel localModel = getDataModel();
        localModel.setRowIndex(rowIndex);

        // if rowIndex is -1, clear the cache
        if (rowIndex == -1) {
            setDataModel(null);
        }

        // Clear or expose the current row data as a request scope attribute
        String var = (String) getStateHelper().get(PropertyKeys.var);
        if (var != null) {
            Map<String, Object> requestMap = getFacesContext().getExternalContext().getRequestMap();
            if (rowIndex == -1) {
                oldVar = requestMap.remove(var);
            } else if (isRowAvailable()) {
                requestMap.put(var, getRowData());
            } else {
                requestMap.remove(var);
                if (null != oldVar) {
                    requestMap.put(var, oldVar);
                    oldVar = null;
                }
            }
        }

        // Reset current state information for the new row index
        restoreDescendantState();

    }

    private void setRowIndexRowStatePreserved(int rowIndex) {
        if (rowIndex < -1) {
            throw new IllegalArgumentException("rowIndex is less than -1");
        }

        if (getRowIndex() == rowIndex) {
            return;
        }

        FacesContext facesContext = getFacesContext();

        if (_initialDescendantFullComponentState != null) {
            // Just save the row
            Map<String, Object> sm = saveFullDescendantComponentStates(facesContext, null, getChildren().iterator(), false);
            if (sm != null && !sm.isEmpty()) {
                _rowDeltaStates.put(getContainerClientId(facesContext), sm);
            }
            if (getRowIndex() != -1) {
                _rowTransientStates.put(getContainerClientId(facesContext),
                        saveTransientDescendantComponentStates(facesContext, null, getChildren().iterator(), false));
            }
        }

        // Update to the new row index
        // this.rowIndex = rowIndex;
        getStateHelper().put(PropertyKeys.rowIndex, rowIndex);
        DataModel localModel = getDataModel();
        localModel.setRowIndex(rowIndex);

        // if rowIndex is -1, clear the cache
        if (rowIndex == -1) {
            setDataModel(null);
        }

        // Clear or expose the current row data as a request scope attribute
        String var = (String) getStateHelper().get(PropertyKeys.var);
        if (var != null) {
            Map<String, Object> requestMap = getFacesContext().getExternalContext().getRequestMap();
            if (rowIndex == -1) {
                oldVar = requestMap.remove(var);
            } else if (isRowAvailable()) {
                requestMap.put(var, getRowData());
            } else {
                requestMap.remove(var);
                if (null != oldVar) {
                    requestMap.put(var, oldVar);
                    oldVar = null;
                }
            }
        }

        if (_initialDescendantFullComponentState != null) {
            Object rowState = _rowDeltaStates.get(getContainerClientId(facesContext));
            if (rowState == null) {
                // Restore as original
                restoreFullDescendantComponentStates(facesContext, getChildren().iterator(), _initialDescendantFullComponentState, false);
            } else {
                // Restore first original and then delta
                restoreFullDescendantComponentDeltaStates(facesContext, getChildren().iterator(), rowState, _initialDescendantFullComponentState, false);
            }
            if (getRowIndex() == -1) {
                restoreTransientDescendantComponentStates(facesContext, getChildren().iterator(), null, false);
            } else {
                rowState = _rowTransientStates.get(getContainerClientId(facesContext));
                if (rowState == null) {
                    restoreTransientDescendantComponentStates(facesContext, getChildren().iterator(), null, false);
                } else {
                    restoreTransientDescendantComponentStates(facesContext, getChildren().iterator(), (Map<String, Object>) rowState, false);
                }
            }
        }
    }

    /**
     * <p>
     * Return the number of rows to be displayed, or zero for all remaining rows in the table. The default value of this
     * property is zero.
     * </p>
     *
     * @return the number of rows.
     */
    public int getRows() {

        return (Integer) getStateHelper().eval(PropertyKeys.rows, 0);

    }

    /**
     * <p>
     * Set the number of rows to be displayed, or zero for all remaining rows in the table.
     * </p>
     *
     * @param rows New number of rows
     *
     * @throws IllegalArgumentException if <code>rows</code> is negative
     */
    public void setRows(int rows) {

        if (rows < 0) {
            throw new IllegalArgumentException(String.valueOf(rows));
        }
        getStateHelper().put(PropertyKeys.rows, rows);

    }

    /**
     * <p>
     * Return the request-scope attribute under which the data object for the current row will be exposed when iterating.
     * This property is <strong>not</strong> enabled for value binding expressions.
     * </p>
     *
     * @return he request-scope attribute.
     */
    public String getVar() {

        return (String) getStateHelper().get(PropertyKeys.var);

    }

    /**
     * <p>
     * Set the request-scope attribute under which the data object for the current row wil be exposed when iterating.
     * </p>
     *
     * @param var The new request-scope attribute name
     */
    public void setVar(String var) {

        getStateHelper().put(PropertyKeys.var, var);

    }

    /**
     * <p class="changed_added_2_1">
     * Return the value of the <code>rowStatePreserved</code> JavaBeans property. See {@link #setRowStatePreserved}.
     * </p>
     *
     * @return the value of the <code>rowStatePreserved</code>.
     *
     * @since 2.1
     */

    public boolean isRowStatePreserved() {
        Boolean b = (Boolean) getStateHelper().get(PropertyKeys.rowStatePreserved);
        return b != null && b.booleanValue();
    }

    /**
     * <p class="changed_added_2_1">
     * If this property is set to <code>true</code>, the <code>UIData</code> must take steps to ensure that modifications to
     * its iterated children will be preserved on a per-row basis. This allows applications to modify component properties,
     * such as the style-class, for a specific row, rather than having such modifications apply to all rows.
     * </p>
     *
     * <div class="changed_added_2_1">
     *
     * <p>
     * To accomplish this, <code>UIData</code> must call {@link StateHolder#saveState} and
     * {@link TransientStateHolder#saveTransientState} on its children to capture their state on exiting each row. When
     * re-entering the row, {@link StateHolder#restoreState} and {@link TransientStateHolder#restoreTransientState} must be
     * called in order to reinitialize the children to the correct state for the new row. All of this action must take place
     * during the processing of {@link #setRowIndex}.
     * </p>
     *
     * <p>
     * Users should consider enabling this feature for cases where it is necessary to modify properties of
     * <code>UIData</code>'s children in a row-specific way. Note, however, that row-level state saving/restoring does add
     * overhead. As such, this feature should be used judiciously.
     * </p>
     *
     * </div>
     *
     * @param preserveComponentState the flag if the state should be preserved.
     *
     * @since 2.1
     */

    public void setRowStatePreserved(boolean preserveComponentState) {
        getStateHelper().put(PropertyKeys.rowStatePreserved, preserveComponentState);
    }

    // ----------------------------------------------------- StateHolder Methods

    /**
     * <p>
     * <span class="changed_modified_2_2">Return</span> the value of the UIData. This value must either be be of type
     * {@link DataModel}, or a type that can be adapted into a {@link DataModel}. <code>UIData</code> will automatically
     * adapt the following types:
     * </p>
     * <ul>
     * <li>Arrays</li>
     * <li><code>java.util.List</code></li>
     * <li><code>java.sql.ResultSet</code></li>
     * <li class="changed_added_2_2"><code>java.util.Collection</code></li>
     * </ul>
     * <p>
     * All other types will be adapted using the {@link ScalarDataModel} class, which will treat the object as a single row
     * of data.
     * </p>
     *
     * @return the object for the value.
     */
    public Object getValue() {
        return getStateHelper().eval(PropertyKeys.value);
    }

    /**
     * <p>
     * Set the value of the <code>UIData</code>. This value must either be be of type {@link DataModel}, or a type that can
     * be adapted into a {@link DataModel}.
     * </p>
     *
     * @param value the new value
     */
    public void setValue(Object value) {
        setDataModel(null);
        getStateHelper().put(PropertyKeys.value, value);
    }

    // ----------------------------------------------------- UIComponent Methods


    /**
     * <p>
     * Set the {@link ValueExpression} used to calculate the value for the specified attribute or property name, if any. In
     * addition, if a {@link ValueExpression} is set for the <code>value</code> property, remove any synthesized
     * {@link DataModel} for the data previously bound to this component.
     * </p>
     *
     * @param name Name of the attribute or property for which to set a {@link ValueExpression}
     * @param binding The {@link ValueExpression} to set, or <code>null</code> to remove any currently set
     * {@link ValueExpression}
     *
     * @throws IllegalArgumentException if <code>name</code> is one of <code>id</code>, <code>parent</code>,
     * <code>var</code>, or <code>rowIndex</code>
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @since 1.2
     */
    @Override
    public void setValueExpression(String name, ValueExpression binding) {

        if (null != name) {
            switch (name) {
            case "value":
                model = null;
                break;
            case "var":
            case "rowIndex":
                throw new IllegalArgumentException();
            }
        }
        super.setValueExpression(name, binding);

    }

    /**
     * <p>
     * Return a client identifier for this component that includes the current value of the <code>rowIndex</code> property,
     * if it is not set to -1. This implies that multiple calls to <code>getClientId()</code> may return different results,
     * but ensures that child components can themselves generate row-specific client identifiers (since {@link UIData} is a
     * {@link NamingContainer}).
     * </p>
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    public String getClientId(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // If baseClientId and clientIdBuilder are both null, this is the
        // first time that getClientId() has been called.
        // If we're not nested within another UIData, then:
        // - create a new StringBuilder assigned to clientIdBuilder containing
        // our client ID.
        // - toString() the builder - this result will be our baseClientId
        // for the duration of the component
        // - append UINamingContainer.getSeparatorChar() to the builder
        // If we are nested within another UIData, then:
        // - create an empty StringBuilder that will be used to build
        // this instance's ID
        if (baseClientId == null && clientIdBuilder == null) {
            if (!isNestedWithinIterator(context)) {
                clientIdBuilder = new StringBuilder(super.getClientId(context));
                baseClientId = clientIdBuilder.toString();
                baseClientIdLength = baseClientId.length() + 1;
                clientIdBuilder.append(UINamingContainer.getSeparatorChar(context));
                clientIdBuilder.setLength(baseClientIdLength);
            } else {
                clientIdBuilder = new StringBuilder();
            }
        }
        int rowIndex = getRowIndex();
        if (rowIndex >= 0) {
            String cid;
            if (!isNestedWithinIterator(context)) {
                // we're not nested, so the clientIdBuilder is already
                // primed with clientID +
                // UINamingContainer.getSeparatorChar(). Append the
                // current rowIndex, and toString() the builder. reset
                // the builder to it's primed state.
                cid = clientIdBuilder.append(rowIndex).toString();
                clientIdBuilder.setLength(baseClientIdLength);
            } else {
                // we're nested, so we have to build the ID from scratch
                // each time. Reuse the same clientIdBuilder instance
                // for each call by resetting the length to 0 after
                // the ID has been computed.
                cid = clientIdBuilder.append(super.getClientId(context)).append(UINamingContainer.getSeparatorChar(context)).append(rowIndex).toString();
                clientIdBuilder.setLength(0);
            }
            return cid;
        } else {
            if (!isNestedWithinIterator(context)) {
                // Not nested and no row available, so just return our baseClientId
                return baseClientId;
            } else {
                // nested and no row available, return the result of getClientId().
                // this is necessary as the client ID will reflect the row that
                // this table represents
                return super.getClientId(context);
            }
        }

    }

    /**
     * <p>
     * Override behavior from {@link UIComponentBase#invokeOnComponent} to provide special care for positioning the data
     * properly before finding the component and invoking the callback on it. If the argument <code>clientId</code> is equal
     * to <code>this.getClientId()</code> simply invoke the <code>contextCallback</code>, passing the <code>context</code>
     * argument and <b>this</b> as arguments, and return <code>true.</code> If the argument <code>clientId</code> is not
     * equal to <code>this.getClientId()</code>, inspect each of the facet children of this <code>UIData</code> instance and
     * for each one, compare its <code>clientId</code> with the argument <code>clientId</code>. If there is a match, invoke
     * the <code>contextCallback</code>, passing the <code>context</code> argument and <b>this</b> as arguments, and return
     * <code>true</code>. Otherwise, attempt to extract a rowIndex from the <code>clientId</code>. For example, if the
     * argument <code>clientId</code> was <code>form:data:3:customerHeader</code> the rowIndex would be <code>3</code>. Let
     * this value be called <code>newIndex</code>. The current rowIndex of this instance must be saved aside and restored
     * before returning in all cases, regardless of the outcome of the search or if any exceptions are thrown in the
     * process.
     * </p>
     *
     * <p>
     * The implementation of this method must never return <code>true</code> if setting the rowIndex of this instance to be
     * equal to <code>newIndex</code> causes this instance to return <code>false</code> from {@link #isRowAvailable}.
     * </p>
     *
     * @throws NullPointerException {@inheritDoc}
     * @throws FacesException {@inheritDoc} Also throws <code>FacesException</code> if any exception is thrown when deriving
     * the rowIndex from the argument <code>clientId</code>.
     * @since 1.2
     */
    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback) throws FacesException {
        if (null == context || null == clientId || null == callback) {
            throw new NullPointerException();
        }

        String myId = super.getClientId(context);
        boolean found = false;
        if (clientId.equals(myId)) {
            try {
                pushComponentToEL(context, compositeParent);
                callback.invokeContextCallback(context, this);
                return true;
            } catch (Exception e) {
                throw new FacesException(e);
            } finally {
                popComponentFromEL(context);
            }
        }

        // check the facets, if any, of UIData
        if (getFacetCount() > 0) {
            for (UIComponent c : getFacets().values()) {
                if (clientId.equals(c.getClientId(context))) {
                    callback.invokeContextCallback(context, c);
                    return true;
                }
            }
        }

        // check column level facets, if any
        if (getChildCount() > 0) {
            for (UIComponent column : getChildren()) {
                if (column instanceof UIColumn) {
                    if (column.getFacetCount() > 0) {
                        for (UIComponent facet : column.getFacets().values()) {
                            if (facet.invokeOnComponent(context, clientId, callback)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        /*
         * Check if we are looking for a component that is part of the actual skeleton.
         */
        if (getChildCount() > 0) {
            for (UIComponent column : getChildren()) {
                if (column instanceof UIColumn) {
                    if (column.invokeOnComponent(context, clientId, callback)) {
                        return true;
                    }
                }
            }
        }

        int lastSep, newRow, savedRowIndex = getRowIndex();
        char sepChar = UINamingContainer.getSeparatorChar(context);
        // If we need to strip out the rowIndex from our id
        // PENDING(edburns): is this safe with respect to I18N?
        if (myId.endsWith(sepChar + Integer.toString(savedRowIndex, 10))) {
            lastSep = myId.lastIndexOf(sepChar);
            assert -1 != lastSep;
            myId = myId.substring(0, lastSep);
        }

        // myId will be something like form:outerData for a non-nested table,
        // and form:outerData:3:data for a nested table.
        // clientId will be something like form:outerData:3:outerColumn
        // for a non-nested table. clientId will be something like
        // outerData:3:data:3:input for a nested table.
        // We need to find the first occurring client ID segment which is parseable as a number.
        if (clientId.startsWith(myId)) {
            try {
                try {
                    newRow = extractFirstNumericSegment(clientId.substring(myId.length()), sepChar);
                } catch (NumberFormatException ex) {
                    // PENDING(edburns): I18N
                    String message = "Trying to extract rowIndex from clientId \'" + clientId + "\' " + ex.getMessage();
                    throw new NumberFormatException(message);
                }
                setRowIndex(newRow);
                if (isRowAvailable()) {
                    found = super.invokeOnComponent(context, clientId, callback);
                }
            } catch (FacesException fe) {
                throw fe;
            } catch (NumberFormatException e) {
                throw new FacesException(e);
            } finally {
                setRowIndex(savedRowIndex);
            }
        }
        return found;
    }

    /**
     * <p>
     * Override the default {@link UIComponentBase#queueEvent} processing to wrap any queued events in a wrapper so that we
     * can reset the current row index in <code>broadcast()</code>.
     * </p>
     *
     * @param event {@link FacesEvent} to be queued
     *
     * @throws IllegalStateException if this component is not a descendant of a {@link UIViewRoot}
     * @throws NullPointerException if <code>event</code> is <code>null</code>
     */
    @Override
    public void queueEvent(FacesEvent event) {

        super.queueEvent(new WrapperEvent(this, event, getRowIndex()));

    }

    /**
     * <p>
     * Override the default {@link UIComponentBase#broadcast} processing to unwrap any wrapped {@link FacesEvent} and reset
     * the current row index, before the event is actually broadcast. For events that we did not wrap (in
     * <code>queueEvent()</code>), default processing will occur.
     * </p>
     *
     * @param event The {@link FacesEvent} to be broadcast
     *
     * @throws AbortProcessingException Signal the Jakarta Faces implementation that no further processing on the
     * current event should be performed
     * @throws IllegalArgumentException if the implementation class of this {@link FacesEvent} is not supported by this
     * component
     * @throws NullPointerException if <code>event</code> is <code>null</code>
     */
    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {

        if (!(event instanceof WrapperEvent)) {
            super.broadcast(event);
            return;
        }
        FacesContext context = event.getFacesContext();
        // Set up the correct context and fire our wrapped event
        WrapperEvent revent = (WrapperEvent) event;
        if (isNestedWithinIterator(context)) {
            setDataModel(null);
        }
        int currentRowIndex = getRowIndex();
        int broadcastedRowIndex = revent.getRowIndex();
        boolean needsToSetIndex = currentRowIndex != -1 || broadcastedRowIndex != -1; // #5213

        if (needsToSetIndex) {
            setRowIndex(broadcastedRowIndex);
        }

        FacesEvent rowEvent = revent.getFacesEvent();
        UIComponent source = rowEvent.getComponent();
        UIComponent compositeParent = null;
        try {
            if (!UIComponent.isCompositeComponent(source)) {
                compositeParent = UIComponent.getCompositeComponentParent(source);
            }
            if (compositeParent != null) {
                compositeParent.pushComponentToEL(context, null);
            }
            source.pushComponentToEL(context, null);
            source.broadcast(rowEvent);
        } finally {
            source.popComponentFromEL(context);
            if (compositeParent != null) {
                compositeParent.popComponentFromEL(context);
            }
        }

        if (needsToSetIndex) {
            setRowIndex(currentRowIndex);
        }
    }

    /**
     * <p>
     * In addition to the default behavior, ensure that any saved per-row state for our child input components is discarded
     * unless it is needed to rerender the current page with errors.
     *
     * @param context FacesContext for the current request
     *
     * @throws IOException if an input/output error occurs while rendering
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    public void encodeBegin(FacesContext context) throws IOException {

        preEncode(context);
        super.encodeBegin(context);

    }

    /**
     * <p>
     * Override the default {@link UIComponentBase#processDecodes} processing to perform the following steps.
     * </p>
     * <ul>
     * <li>If the <code>rendered</code> property of this {@link UIComponent} is <code>false</code>, skip further
     * processing.</li>
     * <li>Set the current <code>rowIndex</code> to -1.</li>
     * <li>Call the <code>processDecodes()</code> method of all facets of this {@link UIData}, in the order determined by a
     * call to <code>getFacets().keySet().iterator()</code>.</li>
     * <li>Call the <code>processDecodes()</code> method of all facets of the {@link UIColumn} children of this
     * {@link UIData}.</li>
     * <li>Iterate over the set of rows that were included when this component was rendered (i.e. those defined by the
     * <code>first</code> and <code>rows</code> properties), performing the following processing for each row:
     * <ul>
     * <li>Set the current <code>rowIndex</code> to the appropriate value for this row.</li>
     * <li>If <code>isRowAvailable()</code> returns <code>true</code>, iterate over the children components of each
     * {@link UIColumn} child of this {@link UIData} component, calling the <code>processDecodes()</code> method for each
     * such child.</li>
     * </ul>
     * </li>
     * <li>Set the current <code>rowIndex</code> to -1.</li>
     * <li>Call the <code>decode()</code> method of this component.</li>
     * <li>If a <code>RuntimeException</code> is thrown during decode processing, call {@link FacesContext#renderResponse}
     * and re-throw the exception.</li>
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    public void processDecodes(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }

        pushComponentToEL(context, this);
        preDecode(context);
        iterate(context, PhaseId.APPLY_REQUEST_VALUES);
        decode(context);
        popComponentFromEL(context);

    }

    /**
     * <p class="changed_modified_2_3">
     * Override the default {@link UIComponentBase#processValidators} processing to perform the following steps.
     * </p>
     * <ul>
     * <li>If the <code>rendered</code> property of this {@link UIComponent} is <code>false</code>, skip further
     * processing.</li>
     * <li>Set the current <code>rowIndex</code> to -1.</li>
     * <li>Call the <code>processValidators()</code> method of all facets of this {@link UIData}, in the order determined by
     * a call to <code>getFacets().keySet().iterator()</code>.</li>
     * <li>Call the <code>processValidators()</code> method of all facets of the {@link UIColumn} children of this
     * {@link UIData}.</li>
     * <li>Iterate over the set of rows that were included when this component was rendered (i.e. those defined by the
     * <code>first</code> and <code>rows</code> properties), performing the following processing for each row:
     * <ul>
     * <li>Set the current <code>rowIndex</code> to the appropriate value for this row.</li>
     * <li>If <code>isRowAvailable()</code> returns <code>true</code>, iterate over the children components of each
     * {@link UIColumn} child of this {@link UIData} component, calling the <code>processValidators()</code> method for each
     * such child.</li>
     * </ul>
     * </li>
     * <li>Set the current <code>rowIndex</code> to -1.</li>
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     * @see jakarta.faces.event.PreValidateEvent
     * @see jakarta.faces.event.PostValidateEvent
     */
    @Override
    public void processValidators(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }
        pushComponentToEL(context, this);
        Application app = context.getApplication();
        app.publishEvent(context, PreValidateEvent.class, this);
        preValidate(context);
        iterate(context, PhaseId.PROCESS_VALIDATIONS);
        app.publishEvent(context, PostValidateEvent.class, this);
        popComponentFromEL(context);

    }

    /**
     * <p>
     * Override the default {@link UIComponentBase#processUpdates} processing to perform the following steps.
     * </p>
     * <ul>
     * <li>If the <code>rendered</code> property of this {@link UIComponent} is <code>false</code>, skip further
     * processing.</li>
     * <li>Set the current <code>rowIndex</code> to -1.</li>
     * <li>Call the <code>processUpdates()</code> method of all facets of this {@link UIData}, in the order determined by a
     * call to <code>getFacets().keySet().iterator()</code>.</li>
     * <li>Call the <code>processUpdates()</code> method of all facets of the {@link UIColumn} children of this
     * {@link UIData}.</li>
     * <li>Iterate over the set of rows that were included when this component was rendered (i.e. those defined by the
     * <code>first</code> and <code>rows</code> properties), performing the following processing for each row:
     * <ul>
     * <li>Set the current <code>rowIndex</code> to the appropriate value for this row.</li>
     * <li>If <code>isRowAvailable()</code> returns <code>true</code>, iterate over the children components of each
     * {@link UIColumn} child of this {@link UIData} component, calling the <code>processUpdates()</code> method for each
     * such child.</li>
     * </ul>
     * </li>
     * <li>Set the current <code>rowIndex</code> to -1.</li>
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    public void processUpdates(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }

        pushComponentToEL(context, this);
        preUpdate(context);
        iterate(context, PhaseId.UPDATE_MODEL_VALUES);
        popComponentFromEL(context);
        // This is not a EditableValueHolder, so no further processing is required

    }

    @Override
    public String createUniqueId(FacesContext context, String seed) {
        Integer i = (Integer) getStateHelper().get(PropertyKeys.lastId);
        int lastId = i != null ? i : 0;
        getStateHelper().put(PropertyKeys.lastId, ++lastId);
        return UIViewRoot.UNIQUE_ID_PREFIX + (seed == null ? lastId : seed);
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_0_rev_a">Override</span> the behavior in {@link UIComponent#visitTree} to handle
     * iteration correctly.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * If the {@link UIComponent#isVisitable} method of this instance returns <code>false</code>, take no action and return.
     * </p>
     *
     * <p>
     * Call {@link UIComponent#pushComponentToEL} and invoke the visit callback on this <code>UIData</code> instance as
     * described in {@link UIComponent#visitTree}. Let the result of the invoctaion be <em>visitResult</em>. If
     * <em>visitResult</em> is {@link VisitResult#COMPLETE}, take no further action and return <code>true</code>. Otherwise,
     * determine if we need to visit our children. The default implementation calls
     * {@link VisitContext#getSubtreeIdsToVisit} passing <code>this</code> as the argument. If the result of that call is
     * non-empty, let <em>doVisitChildren</em> be <code>true</code>. If <em>doVisitChildren</em> is <code>true</code> and
     * <em>visitResult</em> is {@link VisitResult#ACCEPT}, take the following action.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * If this component has facets, call {@link UIComponent#getFacets} on this instance and invoke the
     * <code>values()</code> method. For each <code>UIComponent</code> in the returned <code>Map</code>, call
     * {@link UIComponent#visitTree}.
     * </p>
     * </li>
     *
     * <li>
     *
     * <div class="changed_modified_2_0_rev_a">
     *
     * <p>
     * If this component has children, for each <code>UIColumn</code> child:
     * </p>
     *
     * <p>
     * Call {@link VisitContext#invokeVisitCallback} on that <code>UIColumn</code> instance. If such a call returns
     * <code>true</code>, terminate visiting and return <code>true</code> from this method.
     * </p>
     *
     * <p>
     * If the child <code>UIColumn</code> has facets, call {@link UIComponent#visitTree} on each one.
     * </p>
     *
     * <p>
     * Take no action on non-<code>UIColumn</code> children.
     * </p>
     *
     * </div></li>
     *
     * <li>
     *
     * <div class="changed_modified_2_0_rev_a">
     *
     * <p>
     * Save aside the result of a call to {@link #getRowIndex}.
     * </p>
     *
     * <p>
     * For each child component of this <code>UIData</code> that is also an instance of {@link UIColumn},
     * </p>
     *
     * <p>
     * Iterate over the rows.
     * </p>
     *
     * </div>
     *
     * <ul>
     *
     * <li>
     * <p>
     * Let <em>rowsToProcess</em> be the return from {@link #getRows}.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Let <em>rowIndex</em> be the return from {@link #getFirst} - 1.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * While the number of rows processed is less than <em>rowsToProcess</em>, take the following actions.
     * </p>
     *
     * <p>
     * Call {@link #setRowIndex}, passing the current row index.
     * </p>
     *
     * <p>
     * If {@link #isRowAvailable} returns <code>false</code>, take no further action and return <code>false</code>.
     * </p>
     *
     * <p class="changed_modified_2_0_rev_a">
     * Call {@link UIComponent#visitTree} on each of the children of this <code>UIColumn</code> instance.
     * </p>
     *
     * </li>
     *
     * </ul>
     *
     * </li>
     *
     * </ul>
     *
     * <p>
     * Call {@link #popComponentFromEL} and restore the saved row index with a call to {@link #setRowIndex}.
     * </p>
     *
     * <p>
     * Return <code>false</code> to allow the visiting to continue.
     * </p>
     *
     * </div>
     *
     * @param context the <code>VisitContext</code> that provides context for performing the visit.
     *
     * @param callback the callback to be invoked for each node encountered in the visit.
     *
     * @throws NullPointerException if any of the parameters are <code>null</code>.
     *
     *
     */
    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {

        // First check to see whether we are visitable. If not
        // short-circuit out of this subtree, though allow the
        // visit to proceed through to other subtrees.
        if (!isVisitable(context)) {
            return false;
        }

        FacesContext facesContext = context.getFacesContext();
        // NOTE: that the visitRows local will be obsolete once the
        // appropriate visit hints have been added to the API
        boolean visitRows = requiresRowIteration(context);

        // Clear out the row index is one is set so that
        // we start from a clean slate.
        int oldRowIndex = -1;
        if (visitRows) {
            oldRowIndex = getRowIndex();
            setRowIndex(-1);
        }

        // Push ourselves to EL
        pushComponentToEL(facesContext, null);

        try {

            // Visit ourselves. Note that we delegate to the
            // VisitContext to actually perform the visit.
            VisitResult result = context.invokeVisitCallback(this, callback);

            // If the visit is complete, short-circuit out and end the visit
            if (result == VisitResult.COMPLETE) {
                return true;
            }

            // Visit children, short-circuiting as necessary
            // NOTE: that the visitRows parameter will be obsolete once the
            // appropriate visit hints have been added to the API
            if (result == VisitResult.ACCEPT && doVisitChildren(context, visitRows)) {

                // First visit facets
                // NOTE: that the visitRows parameter will be obsolete once the
                // appropriate visit hints have been added to the API
                if (visitFacets(context, callback, visitRows)) {
                    return true;
                }

                // Next column facets
                // NOTE: that the visitRows parameter will be obsolete once the
                // appropriate visit hints have been added to the API
                if (visitColumnsAndColumnFacets(context, callback, visitRows)) {
                    return true;
                }

                // And finally, visit rows
                // NOTE: that the visitRows parameter will be obsolete once the
                // appropriate visit hints have been added to the API
                if (visitRows(context, callback, visitRows)) {
                    return true;
                }
            }
        } finally {
            // Clean up - pop Jakarta Expression Language and restore old row index
            popComponentFromEL(facesContext);
            if (visitRows) {
                setRowIndex(oldRowIndex);
            }
        }

        // Return false to allow the visit to continue
        return false;
    }

    /**
     * <p class="changed_added_2_1">
     * Override the base class method to take special action if the method is being invoked when
     * {@link StateManager#IS_BUILDING_INITIAL_STATE} is true <strong>and</strong> the <code>rowStatePreserved</code>
     * JavaBeans property for this instance is <code>true</code>.
     * </p>
     *
     * <p class="changed_modified_2_1">
     * The additional action taken is to traverse the descendents and save their state without regard to any particular row
     * value.
     * </p>
     *
     * @since 2.1
     */

    @Override
    public void markInitialState() {
        if (isRowStatePreserved()) {
            if (getFacesContext().getAttributes().containsKey(StateManager.IS_BUILDING_INITIAL_STATE)) {
                _initialDescendantFullComponentState = saveDescendantInitialComponentStates(getFacesContext(), getChildren().iterator(), false);
            }
        }
        super.markInitialState();
    }

    private void restoreFullDescendantComponentStates(FacesContext facesContext, Iterator<UIComponent> childIterator, Object state,
            boolean restoreChildFacets) {
        Iterator<? extends Object[]> descendantStateIterator = null;
        while (childIterator.hasNext()) {
            if (descendantStateIterator == null && state != null) {
                descendantStateIterator = ((Collection<? extends Object[]>) state).iterator();
            }
            UIComponent component = childIterator.next();

            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient()) {
                Object childState = null;
                Object descendantState = null;
                if (descendantStateIterator != null && descendantStateIterator.hasNext()) {
                    Object[] object = descendantStateIterator.next();
                    childState = object[0];
                    descendantState = object[1];
                }

                component.clearInitialState();
                component.restoreState(facesContext, childState);
                component.markInitialState();

                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets) {
                    childsIterator = component.getFacetsAndChildren();
                } else {
                    childsIterator = component.getChildren().iterator();
                }
                restoreFullDescendantComponentStates(facesContext, childsIterator, descendantState, true);
            }
        }
    }

    private Collection<Object[]> saveDescendantInitialComponentStates(FacesContext facesContext, Iterator<UIComponent> childIterator, boolean saveChildFacets) {
        Collection<Object[]> childStates = null;
        while (childIterator.hasNext()) {
            if (childStates == null) {
                childStates = new ArrayList<>();
            }

            UIComponent child = childIterator.next();
            if (!child.isTransient()) {
                // Add an entry to the collection, being an array of two
                // elements. The first element is the state of the children
                // of this component; the second is the state of the current
                // child itself.

                Iterator<UIComponent> childsIterator;
                if (saveChildFacets) {
                    childsIterator = child.getFacetsAndChildren();
                } else {
                    childsIterator = child.getChildren().iterator();
                }
                Object descendantState = saveDescendantInitialComponentStates(facesContext, childsIterator, true);
                Object state = child.saveState(facesContext);
                childStates.add(new Object[] { state, descendantState });
            }
        }
        return childStates;
    }

    private Map<String, Object> saveFullDescendantComponentStates(FacesContext facesContext, Map<String, Object> stateMap, Iterator<UIComponent> childIterator,
            boolean saveChildFacets) {
        while (childIterator.hasNext()) {
            UIComponent child = childIterator.next();
            if (!child.isTransient()) {
                Iterator<UIComponent> childsIterator;
                if (saveChildFacets) {
                    childsIterator = child.getFacetsAndChildren();
                } else {
                    childsIterator = child.getChildren().iterator();
                }
                stateMap = saveFullDescendantComponentStates(facesContext, stateMap, childsIterator, true);
                Object state = child.saveState(facesContext);
                if (state != null) {
                    if (stateMap == null) {
                        stateMap = new HashMap<>();
                    }
                    stateMap.put(child.getClientId(facesContext), state);
                }
            }
        }
        return stateMap;
    }

    private void restoreFullDescendantComponentDeltaStates(FacesContext facesContext, Iterator<UIComponent> childIterator, Object state, Object initialState,
            boolean restoreChildFacets) {
        Map<String, Object> descendantStateIterator = null;
        Iterator<? extends Object[]> descendantFullStateIterator = null;
        while (childIterator.hasNext()) {
            if (descendantStateIterator == null && state != null) {
                descendantStateIterator = (Map<String, Object>) state;
            }
            if (descendantFullStateIterator == null && initialState != null) {
                descendantFullStateIterator = ((Collection<? extends Object[]>) initialState).iterator();
            }
            UIComponent component = childIterator.next();

            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient()) {
                Object childInitialState = null;
                Object descendantInitialState = null;
                Object childState = null;
                if (descendantStateIterator != null && descendantStateIterator.containsKey(component.getClientId(facesContext))) {
                    // Object[] object = (Object[]) descendantStateIterator.get(component.getClientId(facesContext));
                    // childState = object[0];
                    childState = descendantStateIterator.get(component.getClientId(facesContext));
                }
                if (descendantFullStateIterator != null && descendantFullStateIterator.hasNext()) {
                    Object[] object = descendantFullStateIterator.next();
                    childInitialState = object[0];
                    descendantInitialState = object[1];
                }

                component.clearInitialState();
                if (childInitialState != null) {
                    component.restoreState(facesContext, childInitialState);
                    component.markInitialState();
                    component.restoreState(facesContext, childState);
                } else {
                    component.restoreState(facesContext, childState);
                    component.markInitialState();
                }

                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets) {
                    childsIterator = component.getFacetsAndChildren();
                } else {
                    childsIterator = component.getChildren().iterator();
                }
                restoreFullDescendantComponentDeltaStates(facesContext, childsIterator, state, descendantInitialState, true);
            }
        }
    }

    private void restoreTransientDescendantComponentStates(FacesContext facesContext, Iterator<UIComponent> childIterator, Map<String, Object> state,
            boolean restoreChildFacets) {
        while (childIterator.hasNext()) {
            UIComponent component = childIterator.next();

            // reset the client id (see spec 3.1.6)
            component.setId(component.getId());
            if (!component.isTransient()) {
                component.restoreTransientState(facesContext, state == null ? null : state.get(component.getClientId(facesContext)));

                Iterator<UIComponent> childsIterator;
                if (restoreChildFacets) {
                    childsIterator = component.getFacetsAndChildren();
                } else {
                    childsIterator = component.getChildren().iterator();
                }
                restoreTransientDescendantComponentStates(facesContext, childsIterator, state, true);
            }
        }

    }

    private Map<String, Object> saveTransientDescendantComponentStates(FacesContext facesContext, Map<String, Object> childStates,
            Iterator<UIComponent> childIterator, boolean saveChildFacets) {
        while (childIterator.hasNext()) {
            UIComponent child = childIterator.next();
            if (!child.isTransient()) {
                Iterator<UIComponent> childsIterator;
                if (saveChildFacets) {
                    childsIterator = child.getFacetsAndChildren();
                } else {
                    childsIterator = child.getChildren().iterator();
                }
                childStates = saveTransientDescendantComponentStates(facesContext, childStates, childsIterator, true);
                Object state = child.saveTransientState(facesContext);
                if (state != null) {
                    if (childStates == null) {
                        childStates = new HashMap<>();
                    }
                    childStates.put(child.getClientId(facesContext), state);
                }
            }
        }
        return childStates;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (state == null) {
            return;
        }

        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        Object restoredRowStates = UIComponentBase.restoreAttachedState(context, values[1]);
        if (restoredRowStates == null) {
            if (!_rowDeltaStates.isEmpty()) {
                _rowDeltaStates.clear();
            }
        } else {
            _rowDeltaStates = (Map<String, Object>) restoredRowStates;
        }
    }

    private void resetClientIds(UIComponent component) {
        Iterator<UIComponent> iterator = component.getFacetsAndChildren();
        while (iterator.hasNext()) {
            UIComponent child = iterator.next();
            resetClientIds(child);
            child.setId(child.getId());
        }
    }

    @Override
    public Object saveState(FacesContext context) {
        resetClientIds(this);

        if (initialStateMarked()) {
            Object superState = super.saveState(context);

            if (superState == null && _rowDeltaStates.isEmpty()) {
                return null;
            } else {
                Object values[] = null;
                Object attachedState = UIComponentBase.saveAttachedState(context, _rowDeltaStates);
                if (superState != null || attachedState != null) {
                    values = new Object[] { superState, attachedState };
                }
                return values;
            }
        } else {
            Object values[] = new Object[2];
            values[0] = super.saveState(context);
            values[1] = UIComponentBase.saveAttachedState(context, _rowDeltaStates);
            return values;
        }
    }

    // --------------------------------------------------------- Protected Methods

    /**
     * <p>
     * Return the internal {@link DataModel} object representing the data objects that we will iterate over in this
     * component's rendering.
     * </p>
     *
     * <p>
     * If the model has been cached by a previous call to {@link #setDataModel}, return it. Otherwise call
     * {@link #getValue}. If the result is null, create an empty {@link ListDataModel} and return it. If the result is an
     * instance of {@link DataModel}, return it. Otherwise, adapt the result as described in {@link #getValue} and return
     * it.
     * </p>
     *
     * @return the data model.
     */
    protected DataModel getDataModel() {

        // Return any previously cached DataModel instance
        if (model != null) {
            return model;
        }

        // Synthesize a DataModel around our current value if possible
        Object current = getValue();
        if (current == null) {
            setDataModel(EMPTY_DATA_MODEL);
        } else if (current instanceof DataModel) {
            setDataModel((DataModel) current);
        } else if (current instanceof List) {
            setDataModel(new ListDataModel((List) current));
        } else if (Object[].class.isAssignableFrom(current.getClass())) {
            setDataModel(new ArrayDataModel((Object[]) current));
        } else if (current instanceof ResultSet) {
            setDataModel(new ResultSetDataModel((ResultSet) current));
        } else if (current instanceof Collection) {
            setDataModel(new CollectionDataModel((Collection) current));
        } else if (current instanceof Iterable) {
            setDataModel(new IterableDataModel<>((Iterable<?>) current));
        } else if (current instanceof Map) {
            setDataModel(new IterableDataModel<>(((Map<?, ?>) current).entrySet()));
        } else {
            DataModel<?> dataModel = createDataModel(current.getClass());
            if (dataModel != null) {
                dataModel.setWrappedData(current);
                setDataModel(dataModel);
            } else {
                setDataModel(new ScalarDataModel(current));
            }
        }
        return model;

    }

    @SuppressWarnings("all")
    static private class FacesDataModelAnnotationLiteral extends AnnotationLiteral<FacesDataModel> implements FacesDataModel {
        private static final long serialVersionUID = 1L;

        /**
         * Stores the forClass attribute.
         */
        private final Class<?> forClass;

        public FacesDataModelAnnotationLiteral(Class<?> forClass) {
            this.forClass = forClass;
        }

        @Override
        public Class<?> forClass() {
            return forClass;
        }
    }

    private DataModel<?> createDataModel(final Class<?> forClass) {

        List<DataModel<?>> dataModel = new ArrayList<>(1);
        CDI<Object> cdi = CDI.current();

        // Scan the map in order, the first class that is a super class or equal to the class for which
        // we're looking for a DataModel is the closest match, since the Map is sorted on inheritance relation
        getDataModelClassesMap(cdi).entrySet().stream().filter(e -> e.getKey().isAssignableFrom(forClass)).findFirst().ifPresent(

                // Get the bean from CDI which is of the class type that we found during annotation scanning
                // and has the @FacesDataModel annotation, with the "forClass" attribute set to the closest
                // super class of our target class.

                e -> dataModel.add(cdi.select(e.getValue(), new FacesDataModelAnnotationLiteral(e.getKey())).get()));

        return dataModel.isEmpty() ? null : dataModel.get(0);
    }

    @SuppressWarnings("unchecked")
    private Map<Class<?>, Class<? extends DataModel<?>>> getDataModelClassesMap(CDI<Object> cdi) {
        BeanManager beanManager = cdi.getBeanManager();

        // Get the Map with classes for which a custom DataModel implementation is available from CDI
        Bean<?> bean = beanManager.resolve(beanManager.getBeans("comSunFacesDataModelClassesMap"));
        Object beanReference = beanManager.getReference(bean, Map.class, beanManager.createCreationalContext(bean));

        return (Map<Class<?>, Class<? extends DataModel<?>>>) beanReference;
    }

    /**
     * <p>
     * Set the internal DataModel. This <code>UIData</code> instance must use the given {@link DataModel} as its internal
     * value representation from now until the next call to <code>setDataModel</code>. If the given <code>DataModel</code>
     * is <code>null</code>, the internal <code>DataModel</code> must be reset in a manner so that the next call to
     * {@link #getDataModel} causes lazy instantion of a newly refreshed <code>DataModel</code>.
     * </p>
     *
     * <p>
     * Subclasses might call this method if they either want to restore the internal <code>DataModel</code> during the
     * <em>Restore View</em> phase or if they want to explicitly refresh the current <code>DataModel</code> for the
     * <em>Render Response</em> phase.
     * </p>
     *
     * @param dataModel the new <code>DataModel</code> or <code>null</code> to cause the model to be refreshed.
     */

    protected void setDataModel(DataModel dataModel) {
        model = dataModel;
    }

    // ---------------------------------------------------- Private Methods

    /**
     * Called by {@link UIData#visitTree} to determine whether or not the <code>visitTree</code> implementation should visit
     * the rows of UIData or by manipulating the row index before visiting the components themselves.
     *
     * Once we have the appropriate Visit hints for state saving, this method will become obsolete.
     *
     * @param ctx the <code>FacesContext</code> for the current request
     *
     * @return true if row index manipulation is required by the visit to this UIData instance
     */
    private boolean requiresRowIteration(VisitContext ctx) {

        return !ctx.getHints().contains(VisitHint.SKIP_ITERATION);

    }

    // Perform pre-decode initialization work. Note that this
    // initialization may be performed either during a normal decode
    // (ie. processDecodes()) or during a tree visit (ie. visitTree()).
    private void preDecode(FacesContext context) {
        setDataModel(null); // Re-evaluate even with server-side state saving
        Map<String, SavedState> saved = (Map<String, SavedState>) getStateHelper().get(PropertyKeys.saved);
        if (null == saved || !keepSaved(context)) {
            // noinspection CollectionWithoutInitialCapacity
            getStateHelper().remove(PropertyKeys.saved);
        }
    }

    // Perform pre-validation initialization work. Note that this
    // initialization may be performed either during a normal validation
    // (ie. processValidators()) or during a tree visit (ie. visitTree()).
    private void preValidate(FacesContext context) {
        if (isNestedWithinIterator(context)) {
            setDataModel(null);
        }
    }

    // Perform pre-update initialization work. Note that this
    // initialization may be performed either during normal update
    // (ie. processUpdates()) or during a tree visit (ie. visitTree()).
    private void preUpdate(FacesContext context) {
        if (isNestedWithinIterator(context)) {
            setDataModel(null);
        }
    }

    // Perform pre-encode initialization work. Note that this
    // initialization may be performed either during a normal encode
    // (ie. encodeBegin()) or during a tree visit (ie. visitTree()).
    private void preEncode(FacesContext context) {
        setDataModel(null); // re-evaluate even with server-side state saving
        if (!keepSaved(context)) {
            //// noinspection CollectionWithoutInitialCapacity
            // saved = new HashMap<String, SavedState>();
            getStateHelper().remove(PropertyKeys.saved);
        }
    }

    /**
     * <p>
     * Perform the appropriate phase-specific processing and per-row iteration for the specified phase, as follows:
     * <ul>
     * <li>Set the <code>rowIndex</code> property to -1, and process the facets of this {@link UIData} component exactly
     * once.</li>
     * <li>Set the <code>rowIndex</code> property to -1, and process the facets of the {@link UIColumn} children of this
     * {@link UIData} component exactly once.</li>
     * <li>Iterate over the relevant rows, based on the <code>first</code> and <code>row</code> properties, and process the
     * children of the {@link UIColumn} children of this {@link UIData} component once per row.</li>
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     * @param phaseId {@link PhaseId} of the phase we are currently running
     */
    private void iterate(FacesContext context, PhaseId phaseId) {

        // Process each facet of this component exactly once
        setRowIndex(-1);
        if (getFacetCount() > 0) {
            for (UIComponent facet : getFacets().values()) {
                if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                    facet.processDecodes(context);
                } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                    facet.processValidators(context);
                } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                    facet.processUpdates(context);
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }

        // collect rendered columns once
        List<UIColumn> renderedColumns = new ArrayList<>(getChildCount());
        if (getChildCount() > 0) {
            for (UIComponent child : getChildren()) {
                if (child instanceof UIColumn && child.isRendered()) {
                    renderedColumns.add((UIColumn) child);
                }
            }
        }

        // Process each facet of our child UIColumn components exactly once
        setRowIndex(-1);
        for (UIColumn column : renderedColumns) {
            if (column.getFacetCount() > 0) {
                for (UIComponent columnFacet : column.getFacets().values()) {
                    if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                        columnFacet.processDecodes(context);
                    } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                        columnFacet.processValidators(context);
                    } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                        columnFacet.processUpdates(context);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }

        // Iterate over our UIColumn children, once per row
        int processed = 0;
        int rowIndex = getFirst() - 1;
        int rows = getRows();

        while (true) {

            // Have we processed the requested number of rows?
            if (rows > 0 && ++processed > rows) {
                break;
            }

            // Expose the current row in the specified request attribute
            setRowIndex(++rowIndex);
            if (!isRowAvailable()) {
                break; // Scrolled past the last row
            }

            // Perform phase-specific processing as required
            // on the *children* of the UIColumn (facets have
            // been done a single time with rowIndex=-1 already)
            for (UIColumn kid : renderedColumns) {
                if (kid.getChildCount() > 0) {
                    for (UIComponent grandkid : kid.getChildren()) {
                        if (!grandkid.isRendered()) {
                            continue;
                        }
                        if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                            grandkid.processDecodes(context);
                        } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                            grandkid.processValidators(context);
                        } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                            grandkid.processUpdates(context);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    }
                }
            }

        }

        // Clean up after ourselves
        setRowIndex(-1);

    }

    // Tests whether we need to visit our children as part of
    // a tree visit
    private boolean doVisitChildren(VisitContext context, boolean visitRows) {

        // Just need to check whether there are any ids under this
        // subtree. Make sure row index is cleared out since
        // getSubtreeIdsToVisit() needs our row-less client id.
        if (visitRows) {
            setRowIndex(-1);
        }
        Collection<String> idsToVisit = context.getSubtreeIdsToVisit(this);
        assert idsToVisit != null;

        // All ids or non-empty collection means we need to visit our children.
        return !idsToVisit.isEmpty();
    }

//    // Performs pre-phase initialization before visiting children
//    // (if necessary).
//    private void preVisitChildren(VisitContext visitContext) {
//
//        // If EXECUTE_LIFECYCLE hint is set, we need to do
//        // lifecycle-related initialization before visiting children
//        if (visitContext.getHints().contains(VisitHint.EXECUTE_LIFECYCLE)) {
//            FacesContext facesContext = visitContext.getFacesContext();
//            PhaseId phaseId = facesContext.getCurrentPhaseId();
//
//            if (phaseId == PhaseId.APPLY_REQUEST_VALUES)
//                preDecode(facesContext);
//            else if (phaseId == PhaseId.PROCESS_VALIDATIONS)
//                preValidate(facesContext);
//            else if (phaseId == PhaseId.UPDATE_MODEL_VALUES)
//                preUpdate(facesContext);
//            else if (phaseId == PhaseId.RENDER_RESPONSE)
//                preEncode(facesContext);
//        }
//    }

    // Visit each facet of this component exactly once.
    private boolean visitFacets(VisitContext context, VisitCallback callback, boolean visitRows) {

        if (visitRows) {
            setRowIndex(-1);
        }
        if (getFacetCount() > 0) {
            for (UIComponent facet : getFacets().values()) {
                if (facet.visitTree(context, callback)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Visit each UIColumn and any facets it may have defined exactly once
    private boolean visitColumnsAndColumnFacets(VisitContext context, VisitCallback callback, boolean visitRows) {
        if (visitRows) {
            setRowIndex(-1);
        }
        if (getChildCount() > 0) {
            for (UIComponent column : getChildren()) {
                if (column instanceof UIColumn) {
                    VisitResult result = context.invokeVisitCallback(column, callback); // visit the column directly
                    if (result == VisitResult.COMPLETE) {
                        return true;
                    }
                    if (column.getFacetCount() > 0) {
                        for (UIComponent columnFacet : column.getFacets().values()) {
                            if (columnFacet.visitTree(context, callback)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    // Visit each column and row
    private boolean visitRows(VisitContext context, VisitCallback callback, boolean visitRows) {

        // Iterate over our UIColumn children, once per row
        int processed = 0;
        int rowIndex = 0;
        int rows = 0;
        if (visitRows) {
            rowIndex = getFirst() - 1;
            rows = getRows();
        }

        while (true) {

            // Have we processed the requested number of rows?
            if (visitRows) {
                if (rows > 0 && ++processed > rows) {
                    break;
                }
                // Expose the current row in the specified request attribute
                setRowIndex(++rowIndex);
                if (!isRowAvailable()) {
                    break; // Scrolled past the last row
                }
            }

            // Visit as required on the *children* of the UIColumn
            // (facets have been done a single time with rowIndex=-1 already)
            if (getChildCount() > 0) {
                for (UIComponent kid : getChildren()) {
                    if (!(kid instanceof UIColumn)) {
                        continue;
                    }
                    if (kid.getChildCount() > 0) {
                        for (UIComponent grandkid : kid.getChildren()) {
                            if (grandkid.visitTree(context, callback)) {
                                return true;
                            }
                        }
                    }
                }
            }

            if (!visitRows) {
                break;
            }

        }

        return false;
    }

    /**
     * <p>
     * Return <code>true</code> if we need to keep the saved per-child state information. This will be the case if any of
     * the following are true:
     * </p>
     *
     * <ul>
     *
     * <li>there are messages queued with severity ERROR or FATAL.</li>
     *
     * <li>this <code>UIData</code> instance is nested inside of another <code>UIData</code> instance</li>
     *
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     */
    private boolean keepSaved(FacesContext context) {

        return contextHasErrorMessages(context) || isNestedWithinIterator(context);

    }

    private Boolean isNestedWithinIterator(FacesContext context) {
        if (isNested == null) {
            isNested = isNestedInIterator(context, this);
        }
        return isNested;
    }

    private boolean contextHasErrorMessages(FacesContext context) {

        FacesMessage.Severity sev = context.getMaximumSeverity();
        return sev != null && FacesMessage.SEVERITY_ERROR.compareTo(sev) >= 0;

    }

    /**
     * <p>
     * Restore state information for all descendant components, as described for <code>setRowIndex()</code>.
     * </p>
     */
    private void restoreDescendantState() {

        FacesContext context = getFacesContext();
        if (getChildCount() > 0) {
            for (UIComponent kid : getChildren()) {
                if (kid instanceof UIColumn) {
                    restoreDescendantState(kid, context);
                }
            }
        }

    }

    /**
     * <p>
     * Restore state information for the specified component and its descendants.
     * </p>
     *
     * @param component Component for which to restore state information
     * @param context {@link FacesContext} for the current request
     */
    private void restoreDescendantState(UIComponent component, FacesContext context) {

        // Reset the client identifier for this component
        String id = component.getId();
        component.setId(id); // Forces client id to be reset
        Map<String, SavedState> saved = (Map<String, SavedState>) getStateHelper().get(PropertyKeys.saved);
        // Restore state for this component (if it is a EditableValueHolder)
        if (component instanceof EditableValueHolder) {
            EditableValueHolder input = (EditableValueHolder) component;
            String clientId = component.getClientId(context);

            SavedState state = saved == null ? null : saved.get(clientId);
            if (state == null) {
                input.resetValue();
            } else {
                input.setValue(state.getValue());
                input.setValid(state.isValid());
                input.setSubmittedValue(state.getSubmittedValue());
                // This *must* be set after the call to setValue(), since
                // calling setValue() always resets "localValueSet" to true.
                input.setLocalValueSet(state.isLocalValueSet());
            }
        } else if (component instanceof UIForm) {
            UIForm form = (UIForm) component;
            String clientId = component.getClientId(context);
            SavedState state = saved == null ? null : saved.get(clientId);
            if (state == null) {
                // submitted is transient state
                form.setSubmitted(false);
            } else {
                form.setSubmitted(state.getSubmitted());
            }
        }

        // Restore state for children of this component
        if (component.getChildCount() > 0) {
            for (UIComponent kid : component.getChildren()) {
                restoreDescendantState(kid, context);
            }
        }

        // Restore state for facets of this component
        if (component.getFacetCount() > 0) {
            for (UIComponent facet : component.getFacets().values()) {
                restoreDescendantState(facet, context);
            }
        }

    }

    /**
     * <p>
     * Save state information for all descendant components, as described for <code>setRowIndex()</code>.
     * </p>
     */
    private void saveDescendantState() {

        FacesContext context = getFacesContext();
        if (getChildCount() > 0) {
            for (UIComponent kid : getChildren()) {
                if (kid instanceof UIColumn) {
                    saveDescendantState(kid, context);
                }
            }
        }

    }

    /**
     * <p>
     * Save state information for the specified component and its descendants.
     * </p>
     *
     * @param component Component for which to save state information
     * @param context {@link FacesContext} for the current request
     */
    private void saveDescendantState(UIComponent component, FacesContext context) {

        // Save state for this component (if it is a EditableValueHolder)
        Map<String, SavedState> saved = (Map<String, SavedState>) getStateHelper().get(PropertyKeys.saved);
        if (component instanceof EditableValueHolder) {
            EditableValueHolder input = (EditableValueHolder) component;
            SavedState state = null;
            String clientId = component.getClientId(context);
            if (saved == null) {
                state = new SavedState();
            }
            if (state == null) {
                state = saved.get(clientId);
                if (state == null) {
                    state = new SavedState();
                }
            }
            state.setValue(input.getLocalValue());
            state.setValid(input.isValid());
            state.setSubmittedValue(input.getSubmittedValue());
            state.setLocalValueSet(input.isLocalValueSet());
            if (state.hasDeltaState()) {
                getStateHelper().put(PropertyKeys.saved, clientId, state);
            } else if (saved != null) {
                getStateHelper().remove(PropertyKeys.saved, clientId);
            }
        } else if (component instanceof UIForm) {
            UIForm form = (UIForm) component;
            String clientId = component.getClientId(context);
            SavedState state = null;
            if (saved == null) {
                state = new SavedState();
            }
            if (state == null) {
                state = saved.get(clientId);
                if (state == null) {
                    state = new SavedState();
                }
            }
            state.setSubmitted(form.isSubmitted());
            if (state.hasDeltaState()) {
                getStateHelper().put(PropertyKeys.saved, clientId, state);
            } else if (saved != null) {
                getStateHelper().remove(PropertyKeys.saved, clientId);
            }
        }

        // Save state for children of this component
        if (component.getChildCount() > 0) {
            for (UIComponent uiComponent : component.getChildren()) {
                saveDescendantState(uiComponent, context);
            }
        }

        // Save state for facets of this component
        if (component.getFacetCount() > 0) {
            for (UIComponent facet : component.getFacets().values()) {
                saveDescendantState(facet, context);
            }
        }

    }

}

@SuppressWarnings({ "SerializableHasSerializationMethods", "NonSerializableFieldInSerializableClass" })
class SavedState implements Serializable {

    private static final long serialVersionUID = 2920252657338389849L;
    private Object submittedValue;
    private boolean submitted;

    Object getSubmittedValue() {
        return submittedValue;
    }

    void setSubmittedValue(Object submittedValue) {
        this.submittedValue = submittedValue;
    }

    private boolean valid = true;

    boolean isValid() {
        return valid;
    }

    void setValid(boolean valid) {
        this.valid = valid;
    }

    private Object value;

    Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    private boolean localValueSet;

    boolean isLocalValueSet() {
        return localValueSet;
    }

    public void setLocalValueSet(boolean localValueSet) {
        this.localValueSet = localValueSet;
    }

    public boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public boolean hasDeltaState() {
        return submittedValue != null || value != null || localValueSet || !valid || submitted;
    }

    @Override
    public String toString() {
        return "submittedValue: " + submittedValue + " value: " + value + " localValueSet: " + localValueSet;
    }

}

// Private class to wrap an event with a row index
class WrapperEvent extends FacesEvent {

    private static final long serialVersionUID = -1064272913195655452L;

    public WrapperEvent(UIComponent component, FacesEvent event, int rowIndex) {
        super(component);
        this.event = event;
        this.rowIndex = rowIndex;
    }

    private final FacesEvent event;
    private final int rowIndex;

    public FacesEvent getFacesEvent() {
        return event;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public PhaseId getPhaseId() {
        return event.getPhaseId();
    }

    @Override
    public void setPhaseId(PhaseId phaseId) {
        event.setPhaseId(phaseId);
    }

    @Override
    public boolean isAppropriateListener(FacesListener listener) {
        return false;
    }

    @Override
    public void processListener(FacesListener listener) {
        throw new IllegalStateException();
    }

}
