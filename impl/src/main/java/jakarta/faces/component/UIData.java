/*
 * Copyright (c) 2023 Contributors to Eclipse Foundation.
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

import java.io.IOException;

import com.sun.faces.api.component.UIDataImpl;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.ScalarDataModel;

// ------------------------------------------------------------- Private Classes
// Private class to represent saved state information

/**
 * <strong class="changed_modified_2_0_rev_a changed_modified_2_1 changed_modified_2_2">UIData</strong> is a
 * {@link UIComponent} that supports data binding to a collection of data objects represented by a {@link DataModel}
 * instance, which is the current value of this component itself (typically established via a {@link ValueExpression}).
 *
 * <p>
 * During iterative processing over the rows of data in the data model, the object for the current row is exposed as a
 * request attribute under the key specified by the <code>var</code> property.
 * </p>
 *
 * <p>
 * Only children of type {@link UIColumn} should be processed by renderers associated with this component.
 * </p>
 *
 * <p>
 * By default, the <code>rendererType</code> property is set to <code>jakarta.faces.Table</code>. This value can be
 * changed by calling the <code>setRendererType()</code> method.
 * </p>
 */
public class UIData extends UIComponentBase implements NamingContainer, UniqueIdVendor {

    // ------------------------------------------------------ Manifest Constants

    /**
     * The standard component type for this component.
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Data";

    /**
     * The standard component family for this component.
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Data";


    UIDataImpl uiDataImpl;

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIData} instance with default property values.
     * </p>
     */
    public UIData() {
        super(new UIDataImpl());
        setRendererType("jakarta.faces.Table");
        this.uiDataImpl = (UIDataImpl) getUiComponentBaseImpl();
        uiDataImpl.setPeer(this);
    }


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
        return uiDataImpl.getFirst();
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
        uiDataImpl.setFirst(first);
    }

    /**
     * <p>
     * Return the footer facet of this component (if any). A convenience method for <code>getFacet("footer")</code>.
     * </p>
     *
     * @return the footer facet.
     */
    public UIComponent getFooter() {
        return uiDataImpl.getFooter();
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
        uiDataImpl.setFooter(footer);
    }

    /**
     * <p>
     * Return the header facet of this component (if any). A convenience method for <code>getFacet("header")</code>.
     * </p>
     *
     * @return the header facet.
     */
    public UIComponent getHeader() {
        return uiDataImpl.getHeader();
    }

    /**
     * Set the header facet of this component. A convenience method for <code>getFacets().put("header", header)</code>.
     *
     * @param header the new header facet
     *
     * @throws NullPointerException if <code>header</code> is <code>null</code>
     */
    public void setHeader(UIComponent header) {
        uiDataImpl.setHeader(header);
    }

    /**
     * Return a flag indicating whether there is <code>rowData</code> available at the current <code>rowIndex</code>. If no
     * <code>wrappedData</code> is available, return <code>false</code>.
     *
     * @return whether the row is available.
     *
     * @throws FacesException if an error occurs getting the row availability
     */
    public boolean isRowAvailable() {
        return uiDataImpl.isRowAvailable();
    }

    /**
     * Return the number of rows in the underlying data model. If the number of available rows is unknown, return -1.
     *
     * @return the row count.
     * @throws FacesException if an error occurs getting the row count
     */
    public int getRowCount() {
        return uiDataImpl.getRowCount();
    }

    /**
     * Return the data object representing the data for the currently selected row index, if any.
     *
     * @return the row data.
     *
     * @throws FacesException if an error occurs getting the row data
     * @throws IllegalArgumentException if now row data is available at the currently specified row index
     */
    public Object getRowData() {
        return uiDataImpl.getRowData();
    }

    /**
     * Return the zero-relative index of the currently selected row. If we are not currently positioned on a row, return -1.
     * This property is <strong>not</strong> enabled for value binding expressions.
     *
     * @return the row index.
     *
     * @throws FacesException if an error occurs getting the row index
     */
    public int getRowIndex() {
        return uiDataImpl.getRowIndex();
    }

    /**
     * <span class="changed_modified_2_1">Set</span> the zero relative index of the current row, or -1 to indicate that no
     * row is currently selected, by implementing the following algorithm. It is possible to set the row index at a value
     * for which the underlying data collection does not contain any row data. Therefore, callers may use the
     * <code>isRowAvailable()</code> method to detect whether row data will be available for use by the
     * <code>getRowData()</code> method.
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
        uiDataImpl.setRowIndex(rowIndex);
    }

    /**
     * Return the number of rows to be displayed, or zero for all remaining rows in the table. The default value of this
     * property is zero.
     *
     * @return the number of rows.
     */
    public int getRows() {
        return uiDataImpl.getRows();
    }

    /**
     * Set the number of rows to be displayed, or zero for all remaining rows in the table.
     *
     * @param rows New number of rows
     *
     * @throws IllegalArgumentException if <code>rows</code> is negative
     */
    public void setRows(int rows) {
        uiDataImpl.setRows(rows);
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
        return uiDataImpl.getVar();
    }

    /**
     * Set the request-scope attribute under which the data object for the current row wil be exposed when iterating.
     *
     * @param var The new request-scope attribute name
     */
    public void setVar(String var) {
        uiDataImpl.setVar(var);
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
        return uiDataImpl.isRowStatePreserved();
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
        uiDataImpl.setRowStatePreserved(preserveComponentState);
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
        return uiDataImpl.getValue();
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
        uiDataImpl.setValue(value);
    }


    // ----------------------------------------------------- UIComponent Methods


    /**
     * Set the {@link ValueExpression} used to calculate the value for the specified attribute or property name, if any. In
     * addition, if a {@link ValueExpression} is set for the <code>value</code> property, remove any synthesized
     * {@link DataModel} for the data previously bound to this component.
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
        uiDataImpl.setValueExpression(name, binding);
    }

    /**
     * Return a client identifier for this component that includes the current value of the <code>rowIndex</code> property,
     * if it is not set to -1. This implies that multiple calls to <code>getClientId()</code> may return different results,
     * but ensures that child components can themselves generate row-specific client identifiers (since {@link UIData} is a
     * {@link NamingContainer}).
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    public String getClientId(FacesContext context) {
        return uiDataImpl.getClientId(context);

    }

    /**
     * Override behavior from {@link UIComponentBase#invokeOnComponent} to provide special care for positioning the data
     * properly before finding the component and invoking the callback on it.
     *
     * <p>
     * If the argument <code>clientId</code> is equal
     * to <code>this.getClientId()</code> simply invoke the <code>contextCallback</code>, passing the <code>context</code>
     * argument and <b>this</b> as arguments, and return <code>true.</code>
     *
     * If the argument <code>clientId</code> is not
     * equal to <code>this.getClientId()</code>, inspect each of the facet children of this <code>UIData</code> instance and
     * for each one, compare its <code>clientId</code> with the argument <code>clientId</code>.
     *
     * If there is a match, invoke
     * the <code>contextCallback</code>, passing the <code>context</code> argument and <b>this</b> as arguments, and return
     * <code>true</code>. Otherwise, attempt to extract a rowIndex from the <code>clientId</code>.
     *
     * For example, if the
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
        return uiDataImpl.invokeOnComponent(context, clientId, callback);
    }

    /**
     * Override the default {@link UIComponentBase#queueEvent} processing to wrap any queued events in a wrapper so that we
     * can reset the current row index in <code>broadcast()</code>.
     *
     * @param event {@link FacesEvent} to be queued
     *
     * @throws IllegalStateException if this component is not a descendant of a {@link UIViewRoot}
     * @throws NullPointerException if <code>event</code> is <code>null</code>
     */
    @Override
    public void queueEvent(FacesEvent event) {
        uiDataImpl.queueEvent(event);
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
        uiDataImpl.broadcast(event);
    }

    /**
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
        uiDataImpl.encodeBegin(context);
    }

    /**
     * Override the default {@link UIComponentBase#processDecodes} processing to perform the following steps.

     * <ul>
     *   <li>If the <code>rendered</code> property of this {@link UIComponent} is <code>false</code>, skip further
     *       processing.</li>
     *   <li>Set the current <code>rowIndex</code> to -1.</li>
     *   <li>Call the <code>processDecodes()</code> method of all facets of this {@link UIData}, in the order determined by a
     *       call to <code>getFacets().keySet().iterator()</code>.</li>
     *   <li>Call the <code>processDecodes()</code> method of all facets of the {@link UIColumn} children of this
     *       {@link UIData}.</li>
     *   <li>Iterate over the set of rows that were included when this component was rendered (i.e. those defined by the
     *       <code>first</code> and <code>rows</code> properties), performing the following processing for each row:
     *       <ul>
     *         <li>Set the current <code>rowIndex</code> to the appropriate value for this row.</li>
     *         <li>If <code>isRowAvailable()</code> returns <code>true</code>, iterate over the children components of each
     *             {@link UIColumn} child of this {@link UIData} component, calling the <code>processDecodes()</code> method for each
     *             such child.</li>
     *       </ul>
     *    </li>
     *    <li>Set the current <code>rowIndex</code> to -1.</li>
     *    <li>Call the <code>decode()</code> method of this component.</li>
     *    <li>If a <code>RuntimeException</code> is thrown during decode processing, call {@link FacesContext#renderResponse}
     *        and re-throw the exception.</li>
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    public void processDecodes(FacesContext context) {
        uiDataImpl.processDecodes(context);
    }

    /**
     * <p class="changed_modified_2_3">
     * Override the default {@link UIComponentBase#processValidators} processing to perform the following steps.
     * </p>
     *
     * <ul>
     *   <li>If the <code>rendered</code> property of this {@link UIComponent} is <code>false</code>, skip further
     *       processing.</li>
     *   <li>Set the current <code>rowIndex</code> to -1.</li>
     *   <li>Call the <code>processValidators()</code> method of all facets of this {@link UIData}, in the order determined by
     *       a call to <code>getFacets().keySet().iterator()</code>.</li>
     *   <li>Call the <code>processValidators()</code> method of all facets of the {@link UIColumn} children of this
     *       {@link UIData}.</li>
     *   <li>Iterate over the set of rows that were included when this component was rendered (i.e. those defined by the
     *       <code>first</code> and <code>rows</code> properties), performing the following processing for each row:
     *       <ul>
     *          <li>Set the current <code>rowIndex</code> to the appropriate value for this row.</li>
     *          <li>If <code>isRowAvailable()</code> returns <code>true</code>, iterate over the children components of each
     *              {@link UIColumn} child of this {@link UIData} component, calling the <code>processValidators()</code> method for each
     *              such child.</li>
     *       </ul>
     *   </li>
     *   <li>Set the current <code>rowIndex</code> to -1.</li>
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     * @see jakarta.faces.event.PreValidateEvent
     * @see jakarta.faces.event.PostValidateEvent
     */
    @Override
    public void processValidators(FacesContext context) {
        uiDataImpl.processValidators(context);
    }

    /**
     * <p>
     * Override the default {@link UIComponentBase#processUpdates} processing to perform the following steps.
     * </p>
     *
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
        uiDataImpl.processUpdates(context);

    }

    @Override
    public String createUniqueId(FacesContext context, String seed) {
        return uiDataImpl.createUniqueId(context, seed);
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
     *   <li>
     *     <p>
     *     If this component has facets, call {@link UIComponent#getFacets} on this instance and invoke the
     *     <code>values()</code> method. For each <code>UIComponent</code> in the returned <code>Map</code>, call
     *     {@link UIComponent#visitTree}.
     *     </p>
     *   </li>
     *
     *   <li>
     *
     *     <div class="changed_modified_2_0_rev_a">
     *
     *       <p>
     *       If this component has children, for each <code>UIColumn</code> child:
     *       </p>
     *
     *       <p>
     *       Call {@link VisitContext#invokeVisitCallback} on that <code>UIColumn</code> instance. If such a call returns
     *       <code>true</code>, terminate visiting and return <code>true</code> from this method.
     *       </p>
     *
     *       <p>
     *       If the child <code>UIColumn</code> has facets, call {@link UIComponent#visitTree} on each one.
     *       </p>
     *
     *       <p>
     *       Take no action on non-<code>UIColumn</code> children.
     *       </p>
     *
     *     </div>
     *   </li>
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
     * @param callback the callback to be invoked for each node encountered in the visit.
     *
     * @throws NullPointerException if any of the parameters are <code>null</code>.
     *
     */
    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {
        return uiDataImpl.visitTree(context, callback);
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
        uiDataImpl.markInitialState();
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        uiDataImpl.restoreState(context, state);
    }

    @Override
    public Object saveState(FacesContext context) {
        return uiDataImpl.saveState(context);
    }


    // --------------------------------------------------------- Protected Methods

    /**
     * Return the internal {@link DataModel} object representing the data objects that we will iterate over in this
     * component's rendering.
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
        return uiDataImpl.getDataModel();
    }

    /**
     * Set the internal DataModel. This <code>UIData</code> instance must use the given {@link DataModel} as its internal
     * value representation from now until the next call to <code>setDataModel</code>. If the given <code>DataModel</code>
     * is <code>null</code>, the internal <code>DataModel</code> must be reset in a manner so that the next call to
     * {@link #getDataModel} causes lazy instantion of a newly refreshed <code>DataModel</code>.
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
        uiDataImpl.setDataModel(dataModel);
    }
}
