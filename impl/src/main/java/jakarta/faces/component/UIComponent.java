/*
 * Copyright (c) 2022 Contributors to Eclipse Foundation.
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

import static com.sun.faces.util.Util.isAnyNull;
import static com.sun.faces.util.Util.isOneOf;
import static jakarta.faces.application.Resource.COMPONENT_RESOURCE_KEY;
import static jakarta.faces.component.visit.VisitHint.SKIP_TRANSIENT;
import static jakarta.faces.component.visit.VisitHint.SKIP_UNRENDERED;
import static jakarta.faces.component.visit.VisitResult.ACCEPT;
import static jakarta.faces.component.visit.VisitResult.COMPLETE;
import static java.util.Collections.emptyMap;
import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.FacesWrapper;
import jakarta.faces.application.Resource;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.PostRestoreStateEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.event.SystemEventListenerHolder;
import jakarta.faces.render.Renderer;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_0_rev_a changed_modified_2_1 changed_modified_2_2
 * changed_modified_2_3">UIComponent</strong> is the base class for all user interface components in Jakarta Server
 * Faces. The set of {@link UIComponent} instances associated with a particular request and response are organized into
 * a component tree under a {@link UIViewRoot} that represents the entire content of the request or response.
 * </p>
 *
 * <p>
 * For the convenience of component developers, {@link UIComponentBase} provides the default behavior that is specified
 * for a {@link UIComponent}, and is the base class for all of the concrete {@link UIComponent} "base" implementations.
 * Component writers are encouraged to subclass {@link UIComponentBase}, instead of directly implementing this abstract
 * class, to reduce the impact of any future changes to the method signatures.
 * </p>
 *
 * <p class="changed_added_2_0">
 * If the {@link jakarta.faces.event.ListenerFor} annotation is attached to the class definition of a
 * <code>Component</code>, that class must also implement {@link jakarta.faces.event.ComponentSystemEventListener}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Dynamically modifying the component tree can happen at any time, during and after restoring the view, but not during
 * state saving and needs to function properly with respect to rendering and state saving
 * </p>
 */
public abstract class UIComponent implements PartialStateHolder, TransientStateHolder, SystemEventListenerHolder, ComponentSystemEventListener {

    private static Logger LOGGER = Logger.getLogger("jakarta.faces.component", "jakarta.faces.LogStrings");

    /**
     * <p class="changed_added_2_0">
     * The value of this constant is used as the key in the component attribute map, the value for which is a
     * <code>java.beans.BeanInfo</code> implementation describing the composite component. This <code>BeanInfo</code> is
     * known as the <em>composite component BeanInfo</em>.
     * </p>
     *
     * @since 2.0
     */
    public static final String BEANINFO_KEY = "jakarta.faces.component.BEANINFO_KEY";

    /**
     * <p class="changed_added_2_0">
     * The value of this constant is used as the key in the <em>composite component BeanDescriptor</em> for the
     * <code>Map&lt;PropertyDescriptor&gt;</code> that contains meta-information for the declared facets for this composite
     * component. This map must contain an entry under the key {@link #COMPOSITE_FACET_NAME}, even if no facets were
     * explicitly declared. See {@link #COMPOSITE_FACET_NAME}.
     * </p>
     *
     * @since 2.0
     */
    public static final String FACETS_KEY = "jakarta.faces.component.FACETS_KEY";

    /**
     * <p class="changed_added_2_0">
     * The value of this constant is used as the key in the component attributes <code>Map</code> for the
     * {@link jakarta.faces.view.Location} in the view at which this component instance resides.
     * </p>
     *
     * @since 2.0
     */
    public static final String VIEW_LOCATION_KEY = "jakarta.faces.component.VIEW_LOCATION_KEY";

    /**
     * <p class="changed_added_2_0">
     * The value of this constant is used as the key in the <em>composite component BeanDescriptor</em> for a
     * <code>ValueExpression</code> that evaluates to the <code>component-type</code> of the <em>composite component
     * root</em> <code>UIComponent</code> for this composite component, if one was declared by the composite component
     * author.
     * </p>
     *
     * @since 2.0
     */
    public static final String COMPOSITE_COMPONENT_TYPE_KEY = "jakarta.faces.component.COMPOSITE_COMPONENT_TYPE";

    /**
     * <p class="changed_added_2_0">
     * The value of this constant is used as the key in the <code>Map</code> returned as described in {@link #FACETS_KEY}
     * for the <code>PropertyDescriptor</code> describing the composite component facet. The value of this constant is also
     * used as the key in the <code>Map</code> returned from {@link #getFacets}. In this case, it refers to the actual facet
     * that is the {@link jakarta.faces.component.UIPanel} that is the parent of the all of the components in the
     * <code>&lt;composite:implementation&gt;</code> section of the <em>composite component VDL file</em>.
     * </p>
     *
     * @since 2.0
     */
    public static final String COMPOSITE_FACET_NAME = "jakarta.faces.component.COMPOSITE_FACET_NAME";

    /**
     * <p class="changed_added_2_1">
     * This constant enables one to quickly discover the names of the declared composite component attributes that have been
     * given default values by the composite component author. The information is exposed as a
     * <code>Collection&lt;String&gt;</code> returned from the <code>getValue()</code> method on the <em>composite component
     * BeanDescriptor</em>, when this constant is passed as the argument.
     * </p>
     *
     * @since 2.1
     */
    public static final String ATTRS_WITH_DECLARED_DEFAULT_VALUES = "jakarta.faces.component.ATTR_NAMES_WITH_DEFAULT_VALUES";

    /**
     * key used to look up current component stack if FacesContext attributes
     */
    private static final String _CURRENT_COMPONENT_STACK_KEY = "jakarta.faces.component.CURRENT_COMPONENT_STACK";

    /**
     * key used to look up current composite component stack if FacesContext attributes
     */
    private static final String _CURRENT_COMPOSITE_COMPONENT_STACK_KEY = "jakarta.faces.component.CURRENT_COMPOSITE_COMPONENT_STACK";

    enum PropertyKeysPrivate {
        attributesThatAreSet
    }

    /**
     * Properties that are tracked by state saving.
     */
    enum PropertyKeys {
        rendered, attributes, bindings, rendererType, systemEventListeners, behaviors, passThroughAttributes
    }

    /**
     * List of attributes that have been set on the component (this may be from setValueExpression, the attributes map, or
     * setters from the concrete HTML components. This allows for faster rendering of attributes as this list is
     * authoritative on what has been set.
     */
    List<String> attributesThatAreSet;
    ComponentStateHelper stateHelper;
    UIComponent compositeParent;
    private boolean isInView;
    private Map<String, String> resourceBundleMap;

    // It is safe to cache this because components never go from being
    // composite to non-composite.
    private transient Boolean isCompositeComponent;

    /**
     * Track whether we have been pushed as current in order to handle mismatched pushes and pops of Jakarta Expression
     * Language context stack. We use a counter to handle cases where the same component is pushed on multiple times
     */
    private int _isPushedAsCurrentRefCount = 0;

    // -------------------------------------------------------------- Attributes

    /**
     * <p>
     * Return a mutable <code>Map</code> representing the attributes (and properties, see below) associated wth this
     * {@link UIComponent}, keyed by attribute name (which must be a String). The returned implementation must support all
     * of the standard and optional <code>Map</code> methods, plus support the following additional requirements:
     * </p>
     * <ul>
     * <li>The <code>Map</code> implementation must implement the <code>java.io.Serializable</code> interface.</li>
     * <li>Any attempt to add a <code>null</code> key or value must throw a <code>NullPointerException</code>.</li>
     * <li>Any attempt to add a key that is not a String must throw a <code>ClassCastException</code>.</li>
     * <li>If the attribute name specified as a key matches a property of this {@link UIComponent}'s implementation class,
     * the following methods will have special behavior:
     * <ul>
     * <li><code>containsKey</code> - Return <code>false</code>.</li>
     * <li><code>get()</code> - If the property is readable, call the getter method and return the returned value (wrapping
     * primitive values in their corresponding wrapper classes); otherwise throw <code>IllegalArgumentException</code>.</li>
     * <li><code>put()</code> - If the property is writeable, call the setter method to set the corresponding value
     * (unwrapping primitive values in their corresponding wrapper classes). If the property is not writeable, or an attempt
     * is made to set a property of primitive type to <code>null</code>, throw <code>IllegalArgumentException</code>.</li>
     * <li><code>remove</code> - Throw <code>IllegalArgumentException</code>.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @return the component attribute map.
     */
    public abstract Map<String, Object> getAttributes();

    /**
     * <p class="changed_added_2_2">
     * This is a convenience method that simply calls {@link #getPassThroughAttributes(boolean)}, passing {@code true} as
     * the argument. This method must never return {@code null}.
     * </p>
     *
     * @return the pass-through attribute map.
     * @since 2.2
     */
    public Map<String, Object> getPassThroughAttributes() {
        return getPassThroughAttributes(true);
    }

    /**
     * <p class="changed_added_2_2">
     * This method has the same specification as {@link #getPassThroughAttributes() } except that it is allowed to return
     * {@code null} if and only if the argument {@code create} is {@code false} and no pass through attribute data structure
     * exists for this instance. The returned {@code Map} implementation must support all of the standard and optional
     * {@code Map} methods, plus support the following additional requirements. The map must be stored in using
     * {@link #getStateHelper}.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * The {@code Map} implementation must implement {@code java.io.Serializable}.
     * </p>
     *
     * <p>
     * Any attempt to add a {@code null} key or value must throw a {@code NullPointerException}.
     * </p>
     *
     * <p>
     * Any attempt to add a key that is not a {@code String} must throw an {@code IllegalArgumentException}.
     * </p>
     *
     * <p>
     * For backward compatibility with components that extend directly from this class, a default implementation is provided
     * that returns the empty map.
     * </p>
     *
     * </div>
     *
     * @param create if <code>true</code>, a new {@code Map} instance will be created if it does not exist already. If
     * <code>false</code>, and there is no existing <code>Map</code> instance, one will not be created and <code>null</code>
     * will be returned.
     * @return A {@code Map} instance, or {@code null}.
     *
     * @since 2.2
     */
    public Map<String, Object> getPassThroughAttributes(boolean create) {
        return emptyMap();
    }

    // ---------------------------------------------------------------- Bindings

    /**
     * <p>
     * Return the {@link ValueExpression} used to calculate the value for the specified attribute or property name, if any.
     * </p>
     *
     * <p>
     * This method must be overridden and implemented for components that comply with Jakarta Faces 1.2 and later.
     * </p>
     *
     * @param name Name of the attribute or property for which to retrieve a {@link ValueExpression}
     * @return the value expression, or <code>null</code>.
     * @since 1.2
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     *
     */
    public ValueExpression getValueExpression(String name) {

        if (name == null) {
            throw new NullPointerException();
        }

        @SuppressWarnings("unchecked")
        Map<String, ValueExpression> map = (Map<String, ValueExpression>) getStateHelper().get(UIComponentBase.PropertyKeys.bindings);

        return map != null ? map.get(name) : null;
    }

    /**
     * <p>
     * Set the {@link ValueExpression} used to calculate the value for the specified attribute or property name, if any.
     * </p>
     *
     * <p>
     * The implementation must call {@link ValueExpression#isLiteralText} on the argument <code>expression</code>. If
     * <code>isLiteralText()</code> returns <code>true</code>, invoke {@link ValueExpression#getValue} on the argument
     * expression and pass the result as the <code>value</code> parameter in a call to <code>this.{@link
     * #getAttributes()}.put(name, value)</code> where <code>name</code> is the argument <code>name</code>. If an exception
     * is thrown as a result of calling {@link ValueExpression#getValue}, wrap it in a {@link jakarta.faces.FacesException}
     * and re-throw it. If <code>isLiteralText()</code> returns <code>false</code>, simply store the un-evaluated
     * <code>expression</code> argument in the collection of <code>ValueExpression</code>s under the key given by the
     * argument <code>name</code>.
     * </p>
     *
     * <p>
     * This method must be overridden and implemented for components that comply with Jakarta Faces 1.2 and later.
     * </p>
     *
     * @since 1.2
     *
     * @param name Name of the attribute or property for which to set a {@link ValueExpression}
     * @param binding The {@link ValueExpression} to set, or <code>null</code> to remove any currently set
     * {@link ValueExpression}
     *
     * @throws IllegalArgumentException if <code>name</code> is one of <code>id</code> or <code>parent</code>
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     *
     */
    public void setValueExpression(String name, ValueExpression binding) {

        if (name == null) {
            throw new NullPointerException();
        }

        if (isOneOf(name, "id", "parent")) {
            throw new IllegalArgumentException();
        }

        if (binding != null) {
            if (!binding.isLiteralText()) {

                @SuppressWarnings("unchecked")
                List<String> sProperties = (List<String>) getStateHelper().get(PropertyKeysPrivate.attributesThatAreSet);

                if (sProperties == null) {
                    getStateHelper().add(PropertyKeysPrivate.attributesThatAreSet, name);
                } else if (!sProperties.contains(name)) {
                    getStateHelper().add(PropertyKeysPrivate.attributesThatAreSet, name);
                }

                getStateHelper().put(UIComponentBase.PropertyKeys.bindings, name, binding);

            } else {
                ELContext context = FacesContext.getCurrentInstance().getELContext();
                try {
                    getAttributes().put(name, binding.getValue(context));
                } catch (ELException ele) {
                    throw new FacesException(ele);
                }
            }
        } else {
            getStateHelper().remove(PropertyKeysPrivate.attributesThatAreSet, name);
            getStateHelper().remove(UIComponentBase.PropertyKeys.bindings, name);
        }
    }

    // -------------------------------------------------------------- Properties

    boolean initialState;

    /**
     * <p class="changed_added_2_0">
     * An implementation of {@link PartialStateHolder#markInitialState}, this method is called by the runtime to indicate
     * that the instance should start tracking changes to its state.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public void markInitialState() {
        initialState = true;
    }

    /**
     * <p class="changed_added_2_0">
     * An implementation of {@link PartialStateHolder#initialStateMarked}, this method is called by the runtime to test if
     * the {@link PartialStateHolder#markInitialState} method was called.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public boolean initialStateMarked() {
        return initialState;
    }

    /**
     * <p class="changed_added_2_0">
     * An implementation of {@link PartialStateHolder#clearInitialState}, this method is called by the runtime to tell the
     * instance to stop tracking state changes.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public void clearInitialState() {
        initialState = false;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the {@link StateHelper} instance used to help this component implement {@link PartialStateHolder}.
     * </p>
     *
     * @return the state helper.
     * @since 2.0
     */
    protected StateHelper getStateHelper() {
        return getStateHelper(true);
    }

    /**
     * <p class="changed_added_2_0">
     * Like {@link #getStateHelper()}, but only create a state helper instance if the argument <code>creat</code> is
     * <code>true</code>.
     * </p>
     *
     * @param create if <code>true</code>, a new {@link StateHelper} instance will be created if it does not exist already.
     * If <code>false</code>, and there is no existing <code>StateHelper</code> instance, one will not be created and
     * <code>null</code> will be returned.
     *
     * @return the state helper.
     * @since 2.0
     */
    protected StateHelper getStateHelper(boolean create) {

        if (create && stateHelper == null) {
            stateHelper = new ComponentStateHelper(this);
        }

        return stateHelper;
    }

    /**
     * <p class="changed_added_2_1">
     * Return the {@link TransientStateHelper} instance for this <code>UIComponent</code> instance. The default
     * implementation simply calls through to {@link #getTransientStateHelper(boolean)} passing <code>true</code> as the
     * argument.
     * </p>
     *
     * @return the transient state helper.
     * @since 2.1
     */
    public TransientStateHelper getTransientStateHelper() {
        return getTransientStateHelper(true);
    }

    /**
     * <p class="changed_added_2_1">
     * Return the {@link TransientStateHelper} instance for this <code>UIComponent</code> instance.
     * </p>
     *
     * @param create if <code>true</code> create, if necessary, any internal data structures. If <code>false</code>, do not
     * create any instances. In this case, it is possible for this method to return <code>null</code>.
     * @return the transient state helper.
     * @since 2.1
     */

    public TransientStateHelper getTransientStateHelper(boolean create) {

        if (create && stateHelper == null) {
            stateHelper = new ComponentStateHelper(this);
        }

        return stateHelper;
    }

    /**
     * <p class="changed_added_2_1">
     * For components that need to support the concept of transient state, this method will restore any state saved on a
     * prior call to {@link #saveTransientState}.
     * </p>
     *
     * @since 2.1
     */
    @Override
    public void restoreTransientState(FacesContext context, Object state) {
        boolean forceCreate = state != null;
        TransientStateHelper helper = getTransientStateHelper(forceCreate);

        if (helper != null) {
            helper.restoreTransientState(context, state);
        }
    }

    /**
     * <p class="changed_added_2_1">
     * For components that need to support the concept of transient state, this method will save any state that is known to
     * be transient in nature.
     * </p>
     *
     * @since 2.1
     */
    @Override
    public Object saveTransientState(FacesContext context) {
        TransientStateHelper helper = getTransientStateHelper(false);

        return helper == null ? null : helper.saveTransientState(context);
    }

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if this component is within the view hierarchy otherwise <code>false</code>
     *
     * @return <code>true</code> if within a view hierarchy, <code>false</code> otherwise.
     * @since 2.0
     */
    public boolean isInView() {
        return isInView;
    }

    /**
     * <p class="changed_added_2_0">
     * Updates the status as to whether or not this component is currently within the view hierarchy. <strong>This method
     * must never be called by developers; a {@link UIComponent}'s internal implementation will call it as components are
     * added to or removed from a parent's child <code>List</code> or facet <code>Map</code></strong>.
     * </p>
     *
     * @param isInView flag indicating whether or not this component is within the view hierachy
     *
     * @since 2.0
     */
    public void setInView(boolean isInView) {
        this.isInView = isInView;
    }

    /**
     * <p class="changed_added_2_0">
     * Enable Jakarta Expression Language to access the <code>clientId</code> of a component. This is particularly useful in
     * combination with the <code>component</code> and <code>cc</code> implicit objects. A default implementation is
     * provided that simply calls {@link FacesContext#getCurrentInstance} and then calls through to
     * {@link #getClientId(FacesContext)}.
     * </p>
     *
     * @return the client id.
     * @since 2.0
     */
    public String getClientId() {
        return getClientId(FacesContext.getCurrentInstance());
    }

    /**
     * <p>
     * Return a client-side identifier for this component, generating one if necessary. The associated {@link Renderer}, if
     * any, will be asked to convert the clientId to a form suitable for transmission to the client.
     * </p>
     *
     * <p>
     * The return from this method must be the same value throughout the lifetime of the instance, unless the
     * <code>id</code> property of the component is changed, or the component is placed in a {@link NamingContainer} whose
     * client ID changes (for example, {@link UIData}). However, even in these cases, consecutive calls to this method must
     * always return the same value. The implementation must follow these steps in determining the clientId:
     * </p>
     *
     * <p>
     * Find the closest ancestor to <b>this</b> component in the view hierarchy that implements
     * <code>NamingContainer</code>. Call <code>getContainerClientId()</code> on it and save the result as the
     * <code>parentId</code> local variable. Call {@link #getId} on <b>this</b> component and save the result as the
     * <code>myId</code> local variable. If <code>myId</code> is <code>null</code>, call
     * <code>context.getViewRoot().createUniqueId()</code> and assign the result to myId. If <code>parentId</code> is
     * non-<code>null</code>, let <code>myId</code> equal <code>parentId
     * + {@link UINamingContainer#getSeparatorChar} + myId</code>. Call {@link Renderer#convertClientId}, passing
     * <code>myId</code>, and return the result.
     * </p>
     *
     * @param context The {@link FacesContext} for the current request
     * @return the client id.
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract String getClientId(FacesContext context);

    /**
     * <p>
     * Allow components that implement {@link NamingContainer} to selectively disable prepending their clientId to their
     * descendent's clientIds by breaking the prepending logic into a separately callable method. See {@link #getClientId}
     * for usage.
     * </p>
     *
     * <p>
     * By default, this method will call through to {@link #getClientId} and return the result.
     *
     * @param context the Faces context.
     * @return the container client id.
     * @since 1.2
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public String getContainerClientId(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        return getClientId(context);
    }

    /**
     * <p class="changed_modified_2_3">
     * Return the identifier of the component family to which this component belongs. This identifier, in conjunction with
     * the value of the <code>rendererType</code> property, may be used to select the appropriate {@link Renderer} for this
     * component instance. Note this method should NOT return <code>null</code>
     * </p>
     *
     * @return the component family (not null).
     */
    public abstract String getFamily();

    /**
     * <p>
     * Return the component identifier of this {@link UIComponent}.
     * </p>
     *
     * @return the component identifier.
     */
    public abstract String getId();

    /**
     * <p>
     * Set the component identifier of this {@link UIComponent} (if any). Component identifiers must obey the following
     * syntax restrictions:
     * </p>
     * <ul>
     * <li>Must not be a zero-length String.</li>
     * <li>First character must be a letter or an underscore ('_').</li>
     * <li>Subsequent characters must be a letter, a digit, an underscore ('_'), or a dash ('-').</li>
     * <li>
     * </ul>
     *
     * <p>
     * Component identifiers must also obey the following semantic restrictions (note that this restriction is
     * <strong>NOT</strong> enforced by the <code>setId()</code> implementation):
     * </p>
     * <ul>
     * <li>The specified identifier must be unique among all the components (including facets) that are descendents of the
     * nearest ancestor {@link UIComponent} that is a {@link NamingContainer}, or within the scope of the entire component
     * tree if there is no such ancestor that is a {@link NamingContainer}.</li>
     * </ul>
     *
     * @param id The new component identifier, or <code>null</code> to indicate that this {@link UIComponent} does not have
     * a component identifier
     *
     * @throws IllegalArgumentException if <code>id</code> is not syntactically valid
     */
    public abstract void setId(String id);

    /**
     * <p>
     * Return the parent {@link UIComponent} of this <code>UIComponent</code>, if any. A component must allow child
     * components to be added to and removed from the list of children of this component, even though the child component
     * returns null from <code>getParent( )</code>.
     * </p>
     *
     * @return the parent component.
     */
    public abstract UIComponent getParent();

    /**
     * <p class="changed_modified_2_0">
     * <span class="changed_modified_2_0_rev_a">Set</span> the parent <code>UIComponent</code> of this
     * <code>UIComponent</code>. <span class="changed_added_2_0 changed_modified_2_0_rev_a">If
     * <code>parent.isInView()</code> returns <code>true</code>, calling this method will first cause a
     * {@link jakarta.faces.event.PreRemoveFromViewEvent} to be published, for this node, and then the children of this
     * node. Then, once the re-parenting has occurred, a {@link jakarta.faces.event.PostAddToViewEvent} will be published as
     * well, first for this node, and then for the node's children, <span class="changed_modified_2_0_rev_a">but only if any
     * of the following conditions are true.</span></span>
     * </p>
     *
     * <div class="changed_modified_2_0_rev_a">
     *
     * <ul>
     * <li>
     * <p>
     * {@link jakarta.faces.context.FacesContext#getCurrentPhaseId} returns {@link jakarta.faces.event.PhaseId#RESTORE_VIEW}
     * and partial state saving is enabled.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * {@link jakarta.faces.context.FacesContext#isPostback} returns <code>false</code> and
     * {@link jakarta.faces.context.FacesContext#getCurrentPhaseId} returns something other than
     * {@link jakarta.faces.event.PhaseId#RESTORE_VIEW}
     * </p>
     * </li>
     * </ul>
     *
     * </div>
     *
     *
     * <p class="changed_modified_2_0">
     * <strong>This method must never be called by developers; a {@link UIComponent}'s internal implementation will call it
     * as components are added to or removed from a parent's child <code>List</code> or facet <code>Map</code></strong>.
     * </p>
     *
     * @param parent The new parent, or <code>null</code> for the root node of a component tree
     */
    public abstract void setParent(UIComponent parent);

    /**
     * <p>
     * Return <code>true</code> if this component (and its children) should be rendered during the <em>Render Response</em>
     * phase of the request processing lifecycle.
     * </p>
     *
     * @return <code>true</code> if the component should be rendered, <code>false</code> otherwise.
     */
    public abstract boolean isRendered();

    /**
     * <p>
     * Set the <code>rendered</code> property of this {@link UIComponent}.
     * </p>
     *
     * @param rendered If <code>true</code> render this component; otherwise, do not render this component
     */
    public abstract void setRendered(boolean rendered);

    /**
     * <p>
     * Return the {@link Renderer} type for this {@link UIComponent} (if any).
     * </p>
     *
     * @return the renderer type.
     */
    public abstract String getRendererType();

    /**
     * <p>
     * Set the {@link Renderer} type for this {@link UIComponent}, or <code>null</code> for components that render
     * themselves.
     * </p>
     *
     * @param rendererType Logical identifier of the type of {@link Renderer} to use, or <code>null</code> for components
     * that render themselves
     */
    public abstract void setRendererType(String rendererType);

    /**
     * <p>
     * Return a flag indicating whether this component is responsible for rendering its child components. The default
     * implementation in {@link UIComponentBase#getRendersChildren} tries to find the renderer for this component. If it
     * does, it calls {@link Renderer#getRendersChildren} and returns the result. If it doesn't, it returns false. As of
     * version 1.2 of the Jakarta Faces Specification, component authors are encouraged to return <code>true</code>
     * from this method and rely on {@link UIComponentBase#encodeChildren}.
     * </p>
     *
     * @return <code>true</code> if the component renders its children, <code>false</code> otherwise.
     */
    public abstract boolean getRendersChildren();

    /**
     * <p class="changed_added_2_0">
     * Return a <code>Map&lt;String,String&gt;</code> of the <code>ResourceBundle</code> for this component. A component may
     * have a <code>ResourceBundle</code> associated with it. This bundle may contain localized properties relating to
     * instances of this component. The default implementation first looks for a <code>ResourceBundle</code> with a base
     * name equal to the fully qualified class name of the current <code>UIComponent this</code> and <code>Locale</code>
     * equal to the <code>Locale</code> of the current <code>UIViewRoot</code>. If no such bundle is found, and the
     * component is a composite component, let <em>resourceName</em> be the <em>resourceName</em> of the {@link Resource}
     * for this composite component, replacing the file extension with ".properties". Let <em>libraryName</em> be the
     * <em>libraryName</em> of the the {@link Resource} for this composite component. Call
     * {@link jakarta.faces.application.ResourceHandler#createResource(java.lang.String,java.lang.String)}, passing the
     * derived <em>resourceName</em> and <em>libraryName</em>. Note that this will automatically allow for the localization
     * of the <code>ResourceBundle</code> due to the localization facility implemented in <code>createResource</code>, which
     * is specified in section 2.6.1.3 "Resource Identifiers" of the Jakarta Faces Specification Document.
     * If the resultant {@link Resource} exists and can be
     * found, the <code>InputStream</code> for the resource is used to create a <code>ResourceBundle</code>. If either of
     * the two previous steps for obtaining the <code>ResourceBundle</code> for this component is successful, the
     * <code>ResourceBundle</code> is wrapped in a <code>Map&lt;String,String&gt;</code> and returned. Otherwise
     * <code>Collections.EMPTY_MAP</code> is returned.
     * </p>
     *
     * @return the resource bundle map.
     * @since 2.0
     */
    public Map<String, String> getResourceBundleMap() {

        if (resourceBundleMap == null) {

            FacesContext context = FacesContext.getCurrentInstance();

            // Step 1: look for a ResourceBundle under the FQCN of this instance
            ResourceBundle resourceBundle = findResourceBundleUnderFQCNofThis(context);

            // Step 2: if this is a composite component, look for a
            // ResourceBundle as a Resource
            if (resourceBundle == null) {
                resourceBundle = findResourceBundleAsResource(context);
            }

            // Step 3: if the previous steps yielded a ResourceBundle, wrap it
            // with a Map
            if (resourceBundle != null) {
                resourceBundleMap = wrapBundleAsMap(resourceBundle);
            }

            if (resourceBundleMap == null) {
                resourceBundleMap = emptyMap();
            }
        }

        return resourceBundleMap;
    }

    // This is necessary for Jakarta Faces components that extend from UIComponent
    // directly rather than extending from UIComponentBase. Such components
    // may need to have implementations provided for methods that originated
    // from a spec version more recent than the version with which the component
    // complies. Currently this private property is only consulted in the
    // getValueExpression() method.
    // private boolean isUIComponentBase;
    // private boolean isUIComponentBaseIsSet = false;
    //
    // private boolean isUIComponentBase() {
    // if (!isUIComponentBaseIsSet) {
    // isUIComponentBase = (this instanceof UIComponentBase);
    // }
    //
    // return isUIComponentBase;
    // }

    // ------------------------------------------------- Tree Management Methods

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> a mutable <code>List</code> representing the child
     * {@link UIComponent}s associated with this component. The returned implementation must support all of the standard and
     * optional <code>List</code> methods, plus support the following additional requirements:
     * </p>
     * <ul>
     * <li>The <code>List</code> implementation must implement the <code>java.io.Serializable</code> interface.</li>
     * <li>Any attempt to add a <code>null</code> must throw a NullPointerException</li>
     * <li>Any attempt to add an object that does not implement {@link UIComponent} must throw a ClassCastException.</li>
     * <li>Whenever a new child component is added, the <code>parent</code> property of the child must be set to this
     * component instance. If the <code>parent</code> property of the child was already non-null, the child must first be
     * removed from its previous parent (where it may have been either a child or a facet).</li>
     * <li>Whenever an existing child component is removed, the <code>parent</code> property of the child must be set to
     * <code>null</code>.</li>
     *
     * <li class="changed_modified_2_1">
     * <p>
     * After the child component has been added to the view, {@link jakarta.faces.application.Application#publishEvent} must
     * be called, passing {@link jakarta.faces.event.PostAddToViewEvent}<code>.class</code> as the first argument and the
     * newly added component as the second argument if any the following cases are true.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * {@link jakarta.faces.context.FacesContext#getCurrentPhaseId} returns {@link jakarta.faces.event.PhaseId#RESTORE_VIEW}
     * and partial state saving is enabled.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * {@link jakarta.faces.context.FacesContext#isPostback} returns <code>false</code> and
     * {@link jakarta.faces.context.FacesContext#getCurrentPhaseId} returns something other than
     * {@link jakarta.faces.event.PhaseId#RESTORE_VIEW}
     * </p>
     * </li>
     *
     * </ul>
     *
     * </li>
     *
     * </ul>
     *
     * @return the list of children.
     */
    public abstract List<UIComponent> getChildren();

    /**
     * <p>
     * Return the number of child {@link UIComponent}s that are associated with this {@link UIComponent}. If there are no
     * children, this method must return 0. The method must not cause the creation of a child component list.
     * </p>
     *
     * @return the number of child components.
     */
    public abstract int getChildCount();

    /**
     * <p>
     * <span class="changed_modified_2_2">Search</span> for and return the {@link UIComponent} with an <code>id</code> that
     * matches the specified search expression (if any), according to the algorithm described below.
     * </p>
     *
     * <p class="changed_added_2_2">
     * WARNING: The found <code>UIComponent</code> instance, if any, is returned <strong>without</strong> regard for its
     * tree traversal context. Retrieving an Jakarta Expression Language-bound attribute from the component is not safe.
     * Jakarta Expression Language expressions can contain implicit objects, such as <code>#{component}</code>, which assume
     * they are being evaluated within the scope of a tree traversal context. Evaluating expressions with these kinds of
     * implicit objects outside of a tree traversal context produces undefined results. See {@link #invokeOnComponent} for a
     * method that <strong>does</strong> correctly account for the tree traversal context when operating on the found
     * <code>UIComponent</code> instance. {@link #invokeOnComponent} is also useful to find components given a simple
     * <code>clientId</code>.
     *
     * <p>
     * Component identifiers are required to be unique within the scope of the closest ancestor {@link NamingContainer} that
     * encloses this component (which might be this component itself). If there are no {@link NamingContainer} components in
     * the ancestry of this component, the root component in the tree is treated as if it were a {@link NamingContainer},
     * whether or not its class actually implements the {@link NamingContainer} interface.
     * </p>
     *
     * <p>
     * A <em>search expression</em> consists of either an identifier (which is matched exactly against the <code>id</code>
     * property of a {@link UIComponent}, or a series of such identifiers linked by the
     * {@link UINamingContainer#getSeparatorChar} character value. The search algorithm should operates as follows, though
     * alternate alogrithms may be used as long as the end result is the same:
     * </p>
     *
     * <ul>
     * <li>Identify the {@link UIComponent} that will be the base for searching, by stopping as soon as one of the following
     * conditions is met:
     * <ul>
     * <li>If the search expression begins with the the separator character (called an "absolute" search expression), the
     * base will be the root {@link UIComponent} of the component tree. The leading separator character will be stripped
     * off, and the remainder of the search expression will be treated as a "relative" search expression as described
     * below.</li>
     * <li>Otherwise, if this {@link UIComponent} is a {@link NamingContainer} it will serve as the basis.</li>
     * <li>Otherwise, search up the parents of this component. If a {@link NamingContainer} is encountered, it will be the
     * base.</li>
     * <li>Otherwise (if no {@link NamingContainer} is encountered) the root {@link UIComponent} will be the base.</li>
     * </ul>
     * </li>
     * <li>The search expression (possibly modified in the previous step) is now a "relative" search expression that will be
     * used to locate the component (if any) that has an <code>id</code> that matches, within the scope of the base
     * component. The match is performed as follows:
     * <ul>
     * <li>If the search expression is a simple identifier, this value is compared to the <code>id</code> property, and then
     * recursively through the facets and children of the base {@link UIComponent} (except that if a descendant
     * {@link NamingContainer} is found, its own facets and children are not searched).</li>
     * <li>If the search expression includes more than one identifier separated by the separator character, the first
     * identifier is used to locate a {@link NamingContainer} by the rules in the previous bullet point. Then, the
     * <code>findComponent()</code> method of this {@link NamingContainer} will be called, passing the remainder of the
     * search expression.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param expr Search expression identifying the {@link UIComponent} to be returned
     *
     * @return the found {@link UIComponent}, or <code>null</code> if the component was not found.
     *
     * @throws IllegalArgumentException if an intermediate identifier in a search expression identifies a
     * {@link UIComponent} that is not a {@link NamingContainer}
     * @throws NullPointerException if <code>expr</code> is <code>null</code>
     */
    public abstract UIComponent findComponent(String expr);

    /**
     * <p>
     * <span class="changed_modified_2_1">Starting</span> at this component in the View hierarchy, search for a component
     * with a <code>clientId</code> equal to the argument <code>clientId</code> and, if found, call the
     * {@link ContextCallback#invokeContextCallback} method on the argument <code>callback</code>, passing the current
     * {@link FacesContext} and the found component as arguments. This method is similar to {@link #findComponent} but it
     * does not support the leading {@link UINamingContainer#getSeparatorChar} syntax for searching from the root of the
     * View.
     * </p>
     *
     * <p>
     * The default implementation will first check if <code>this.getClientId()</code> is equal to the argument
     * <code>clientId</code>. If so, <span class="changed_added_2_1">first call {@link #pushComponentToEL}, then</span> call
     * the {@link ContextCallback#invokeContextCallback} method on the argument callback, passing through the
     * <code>FacesContext</code> argument and passing this as the component argument. <span class="changed_added_2_1">Then
     * call {@link #popComponentFromEL}</span>. If an <code>Exception</code> is thrown by the callback, wrap it in a
     * {@link FacesException} and re-throw it. Otherwise, return <code>true</code>.
     * </p>
     *
     * <p>
     * Otherwise, for each component returned by {@link #getFacetsAndChildren}, call <code>invokeOnComponent()</code>
     * passing the arguments to this method, in order. The first time <code>invokeOnComponent()</code> returns true, abort
     * traversing the rest of the <code>Iterator</code> and return <code>true</code>.
     * </p>
     *
     * <p>
     * When calling {@link ContextCallback#invokeContextCallback} the implementation of this method must guarantee that the
     * state of the component passed to the callback correctly reflects the component's position in the View hierarchy with
     * respect to any state found in the argument <code>clientId</code>. For example, an iterating component such as
     * {@link UIData} will need to set its row index to correctly reflect the argument <code>clientId</code> before finding
     * the appropriate child component backed by the correct row. When the callback returns, either normally or by throwing
     * an <code>Exception</code> the implementation of this method must restore the state of the view to the way it was
     * before invoking the callback.
     * </p>
     *
     * <p>
     * If none of the elements from {@link #getFacetsAndChildren} returned <code>true</code> from
     * <code>invokeOnComponent()</code>, return <code>false</code>.
     * </p>
     *
     * <p>
     * Simple usage example to find a component by <code>clientId</code>.
     * </p>
     *
     * <pre>
     * <code>
    private UIComponent found = null;

    private void doFind(FacesContext context, String clientId) {
      context.getViewRoot().invokeOnComponent(context, clientId,
          new ContextCallback() {
             public void invokeContextCallback(FacesContext context,
                                           UIComponent component) {
               found = component;
             }
          });
    }
    * </code>
     * </pre>
     *
     *
     *
     * @since 1.2
     *
     * @param context the {@link FacesContext} for the current request
     *
     * @param clientId the client identifier of the component to be passed to the argument callback.
     *
     * @param callback an implementation of the Callback interface.
     *
     * @throws NullPointerException if any of the arguments are null
     *
     * @throws FacesException if the argument Callback throws an Exception, it is wrapped in a <code>FacesException</code>
     * and re-thrown.
     *
     * @return <code>true</code> if the a component with the given <code>clientId</code> is found, the callback method was
     * successfully invoked passing that component as an argument, and no Exception was thrown. Returns <code>false</code>
     * if no component with the given <code>clientId</code> is found.
     *
     */
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback) throws FacesException {

        if (isAnyNull(context, clientId, callback)) {
            throw new NullPointerException();
        }

        boolean found = false;
        if (clientId.equals(getClientId(context))) {
            try {
                pushComponentToEL(context, this);
                callback.invokeContextCallback(context, this);

                return true;
            } catch (Exception e) {
                throw new FacesException(e);
            } finally {
                popComponentFromEL(context);
            }
        } else {
            Iterator<UIComponent> facetsAndChildrenIterator = getFacetsAndChildren();

            while (facetsAndChildrenIterator.hasNext() && !found) {
                found = facetsAndChildrenIterator.next().invokeOnComponent(context, clientId, callback);
            }
        }

        return found;
    }


    // ------------------------------------------------ Facet Management Methods

    /**
     * <p>
     * Return a mutable <code>Map</code> representing the facet {@link UIComponent}s associated with this
     * {@link UIComponent}, keyed by facet name (which must be a String). The returned implementation must support all of
     * the standard and optional <code>Map</code> methods, plus support the following additional requirements:
     * </p>
     *
     * <ul>
     * <li>The <code>Map</code> implementation must implement the <code>java.io.Serializable</code> interface.</li>
     * <li>Any attempt to add a <code>null</code> key or value must throw a NullPointerException.</li>
     * <li>Any attempt to add a key that is not a String must throw a ClassCastException.</li>
     * <li>Any attempt to add a value that is not a {@link UIComponent} must throw a ClassCastException.</li>
     * <li>Whenever a new facet {@link UIComponent} is added:
     * <ul>
     * <li>The <code>parent</code> property of the component must be set to this component instance.</li>
     * <li>If the <code>parent</code> property of the component was already non-null, the component must first be removed
     * from its previous parent (where it may have been either a child or a facet).</li>
     * </ul>
     * </li>
     *
     * <li>Whenever an existing facet {@link UIComponent} is removed:
     * <ul>
     * <li>The <code>parent</code> property of the facet must be set to <code>null</code>.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @return the map of facets.
     */
    public abstract Map<String, UIComponent> getFacets();

    /**
     * <p>
     * Return the number of facet {@link UIComponent}s that are associated with this {@link UIComponent}. If there are no
     * facets, this method must return 0. The method must not cause the creation of a facet component map.
     * </p>
     *
     * <p>
     * For backwards compatability with classes that extend UIComponent directly, a default implementation is provided that
     * simply calls {@link #getFacets} and then calls the <code>size()</code> method on the returned <code>Map</code>. A
     * more optimized version of this method is provided in {@link UIComponentBase#getFacetCount}.
     *
     * @return the number of facets.
     * @since 1.2
     */
    public int getFacetCount() {
        return getFacets().size();
    }

    /**
     * <p>
     * Convenience method to return the named facet, if it exists, or <code>null</code> otherwise. If the requested facet
     * does not exist, the facets Map must not be created.
     * </p>
     *
     * @param name Name of the desired facet
     * @return the component, or <code>null</code>.
     */
    public abstract UIComponent getFacet(String name);

    /**
     * <p>
     * Return an <code>Iterator</code> over the facet followed by child {@link UIComponent}s of this {@link UIComponent}.
     * Facets are returned in an undefined order, followed by all the children in the order they are stored in the child
     * list. If this component has no facets or children, an empty <code>Iterator</code> is returned.
     * </p>
     *
     * <p>
     * The returned <code>Iterator</code> must not support the <code>remove()</code> operation.
     * </p>
     *
     * @return the facets and children iterator.
     */
    public abstract Iterator<UIComponent> getFacetsAndChildren();

    // -------------------------------------------- Lifecycle Processing Methods

    /**
     * <p>
     * Broadcast the specified {@link FacesEvent} to all registered event listeners who have expressed an interest in events
     * of this type. Listeners are called in the order in which they were added.
     * </p>
     * <p class="changed_added_2_0">
     * If the <code>event</code> is an instance of {@link jakarta.faces.event.BehaviorEvent} and the current
     * <code>component</code> is the source of the <code>event</code> call
     * {@link jakarta.faces.event.BehaviorEvent#getBehavior} to get the {@link jakarta.faces.component.behavior.Behavior}
     * for the event.
     *
     * <span class="changed_modified_2_0_rev_a">Call
     * {@link jakarta.faces.component.behavior.Behavior#broadcast(jakarta.faces.event.BehaviorEvent)} on the
     * <code>Behavior</code> instance</span>.
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
    public abstract void broadcast(FacesEvent event) throws AbortProcessingException;

    /**
     * <p>
     * Decode any new state of this {@link UIComponent} from the request contained in the specified {@link FacesContext},
     * and store this state as needed.
     * </p>
     * <p>
     * During decoding, events may be enqueued for later processing (by event listeners who have registered an interest), by
     * calling <code>queueEvent()</code>.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract void decode(FacesContext context);

    /**
     * <p class="changed_added_2_0">
     * Perform a tree visit starting at this node in the tree.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * UIComponent.visitTree() implementations do not invoke the {@link VisitCallback} directly, but instead call
     * {@link VisitContext#invokeVisitCallback} to invoke the callback. This allows {@code VisitContext} implementations to
     * provide optimized tree traversals, for example by only calling the {@code
     * VisitCallback} for a subset of components.
     * </p>
     *
     * <p>
     * UIComponent.visitTree() implementations must call UIComponent.pushComponentToEL() before performing the visit and
     * UIComponent.popComponentFromEL() after the visit.
     * </p>
     * </div>
     *
     * @param visitContext the <code>VisitContext</code> for this visit
     * @param callback the <code>VisitCallback</code> instance whose <code>visit</code> method will be called for each node
     * visited.
     * @return component implementations may return <code>true</code> to indicate that the tree visit is complete (eg. all
     * components that need to be visited have been visited). This results in the tree visit being short-circuited such that
     * no more components are visited.
     *
     * @see VisitContext#invokeVisitCallback VisitContext.invokeVisitCallback()
     *
     * @since 2.0
     */
    public boolean visitTree(VisitContext visitContext, VisitCallback callback) {

        // First check to see whether we are visitable. If not
        // short-circuit out of this subtree, though allow the
        // visit to proceed through to other subtrees.
        if (!isVisitable(visitContext)) {
            return false;
        }

        // Push ourselves to Jakarta Expression Language before visiting
        FacesContext facesContext = visitContext.getFacesContext();
        pushComponentToEL(facesContext, null);

        try {
            // Visit ourselves. Note that we delegate to the
            // VisitContext to actually perform the visit.
            VisitResult result = visitContext.invokeVisitCallback(this, callback);

            // If the visit is complete, short-circuit out and end the visit
            if (result == COMPLETE) {
                return true;
            }

            // Visit children if necessary
            if (result == ACCEPT) {
                Iterator<UIComponent> kids = getFacetsAndChildren();

                while (kids.hasNext()) {
                    boolean done = kids.next().visitTree(visitContext, callback);

                    // If any kid visit returns true, we are done.
                    if (done) {
                        return true;
                    }
                }
            }
        } finally {
            // Pop ourselves off the Jakarta Expression Language stack
            popComponentFromEL(facesContext);
        }

        // Return false to allow the visit to continue
        return false;
    }

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if this component should be visited, <code>false</code> otherwise. Called by
     * {@link UIComponent#visitTree UIComponent.visitTree()} to determine whether this component satisfies the hints
     * returned by {@link jakarta.faces.component.visit.VisitContext#getHints}.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * If this method returns false, the tree visited is short-circuited such that neither the component nor any of its
     * descendents will be visited
     * </p>
     *
     * <p>
     * Custom {@code visitTree()} implementations may call this method to determine whether the component is visitable
     * before performing any visit-related processing.
     * </p>
     *
     * </div>
     *
     * @param context the Visit context.
     * @return <code>true</code> if visitable, <code>false</code> otherwise.
     * @since 2.0
     */
    protected boolean isVisitable(VisitContext context) {

        // VisitHints currently defines two hints that affect visitability:
        // VIIST_RENDERED and VISIT_TRANSIENT.
        // Check for both of these and if set, verify that we comply.
        Set<VisitHint> hints = context.getHints();

        if (hints.contains(SKIP_UNRENDERED) && !isRendered() || hints.contains(SKIP_TRANSIENT) && isTransient()) {
            return false;
        }

        return true;
    }

    /**
     * <p>
     * <span class="changed_modified_2_0">If</span> our <code>rendered</code> property is <code>true</code>, render the
     * beginning of the current state of this {@link UIComponent} to the response contained in the specified
     * {@link FacesContext}. Call
     * {@link #pushComponentToEL(jakarta.faces.context.FacesContext,jakarta.faces.component.UIComponent)}. Call
     * {@link jakarta.faces.application.Application#publishEvent}, passing
     * {@link jakarta.faces.event.PreRenderComponentEvent}<code>.class</code> as the first argument and the component
     * instance to be rendered as the second argument.
     * </p>
     *
     * <p>
     * If a {@link Renderer} is associated with this {@link UIComponent}, the actual encoding will be delegated to
     * {@link Renderer#encodeBegin(FacesContext, UIComponent)}.
     * </p>
     *
     * <p class="changed_added_2_0">
     * If our <code>rendered</code> property is <code>false</code>, call
     * {@link #pushComponentToEL(jakarta.faces.context.FacesContext,jakarta.faces.component.UIComponent)} and return
     * immediately.
     * </p>
     *
     * @param context {@link FacesContext} for the response we are creating
     *
     * @throws IOException if an input/output error occurs while rendering
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract void encodeBegin(FacesContext context) throws IOException;

    /**
     * <p>
     * If our <code>rendered</code> property is <code>true</code>, render the child {@link UIComponent}s of this
     * {@link UIComponent}. This method will only be called if the <code>rendersChildren</code> property is
     * <code>true</code>.
     * </p>
     *
     * <p>
     * If a {@link Renderer} is associated with this {@link UIComponent}, the actual encoding will be delegated to
     * {@link Renderer#encodeChildren(FacesContext, UIComponent)}. <span class="changed_modified_2_0">If no {@link Renderer}
     * is associated with this {@link UIComponent}, iterate over each of the children of this component and call
     * {@link #encodeAll(jakarta.faces.context.FacesContext)}.</span>
     * </p>
     *
     * @param context {@link FacesContext} for the response we are creating
     *
     * @throws IOException if an input/output error occurs while rendering
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract void encodeChildren(FacesContext context) throws IOException;

    /**
     * <p>
     * <span class="changed_modified_2_0">If</span> our <code>rendered</code> property is <code>true</code>, render the
     * ending of the current state of this {@link UIComponent}.
     * </p>
     *
     * <p>
     * If a {@link Renderer} is associated with this {@link UIComponent}, the actual encoding will be delegated to
     * {@link Renderer#encodeEnd(FacesContext, UIComponent)}.
     * </p>
     *
     * <p class="changed_added_2_0">
     * Call {@link UIComponent#popComponentFromEL}. before returning regardless of the value of the <code>rendered</code>
     * property.
     * </p>
     *
     * @param context {@link FacesContext} for the response we are creating
     *
     * @throws IOException if an input/output error occurs while rendering
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract void encodeEnd(FacesContext context) throws IOException;

    /**
     * <p>
     * If this component returns <code>true</code> from {@link #isRendered}, take the following action.
     * </p>
     *
     * <p>
     * Render this component and all its children that return <code>true</code> from <code>isRendered()</code>, regardless
     * of the value of the {@link #getRendersChildren} flag.
     * </p>
     *
     * @param context the Faces context.
     * @since 1.2
     * @throws IOException if an input/output error occurs while rendering
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public void encodeAll(FacesContext context) throws IOException {

        if (context == null) {
            throw new NullPointerException();
        }

        if (!isRendered()) {
            return;
        }

        encodeBegin(context);

        if (getRendersChildren()) {
            encodeChildren(context);
        } else if (getChildCount() > 0) {
            for (UIComponent kid : getChildren()) {
                kid.encodeAll(context);
            }
        }

        encodeEnd(context);
    }

    @SuppressWarnings("unchecked")
    private static ArrayDeque<UIComponent> _getComponentELStack(String keyName, Map<Object, Object> contextAttributes) {
        return (ArrayDeque<UIComponent>) contextAttributes.computeIfAbsent(keyName, e -> new ArrayDeque<>());
    }

    /**
     * <p class="changed_added_2_0">
     * Push the current <code>UIComponent</code> <code>this</code> to the {@link FacesContext} attribute map
     * saving the previous <code>UIComponent</code>
     * for a subsequent call to {@link #popComponentFromEL}.
     * </p>
     *
     * <p class="changed_added_2_0">
     * This method and <code>popComponentFromEL()</code> form the basis for the contract that enables the Jakarta Expression
     * Language Expression "<code>#{component}</code>" to resolve to the "current" component that is being processed in the
     * lifecycle. The requirements for when <code>pushComponentToEL()</code> and <code>popComponentFromEL()</code> must be
     * called are specified as needed in the javadoc for this class.
     * </p>
     *
     * <p class="changed_added_2_0">
     * After <code>pushComponentToEL()</code> returns, a call to {@link #getCurrentComponent} must return <code>this</code>
     * <code>UIComponent</code> instance until <code>popComponentFromEL()</code> is called, after which point the previous
     * <code>UIComponent</code> instance will be returned from <code>getCurrentComponent()</code>
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @param component the <code>component</code> to push to the EL. If <code>component</code> is <code>null</code> the
     * <code>UIComponent</code> instance that this call was invoked upon will be pushed to the EL.
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     *
     * @see jakarta.faces.context.FacesContext#getAttributes()
     *
     * @since 2.0
     */
    public void pushComponentToEL(FacesContext context, UIComponent component) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (component == null) {
            component = this;
        }

        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<UIComponent> componentELStack = _getComponentELStack(_CURRENT_COMPONENT_STACK_KEY, contextAttributes);
        componentELStack.push(component);
        component._isPushedAsCurrentRefCount++;

        // If the pushed component is a composite component, we need to update that
        // stack as well
        if (UIComponent.isCompositeComponent(component)) {
            _getComponentELStack(_CURRENT_COMPOSITE_COMPONENT_STACK_KEY, contextAttributes).push(component);
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Pop the current <code>UIComponent</code> from the {@link FacesContext} attributes map so that the previous
     * <code>UIComponent</code>, if any, becomes the current component.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     *
     * @see jakarta.faces.context.FacesContext#getAttributes()
     *
     * @since 2.0
     */
    public void popComponentFromEL(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Map<Object, Object> contextAttributes = context.getAttributes();

        ArrayDeque<UIComponent> componentELStack = _getComponentELStack(_CURRENT_COMPONENT_STACK_KEY, contextAttributes);

        // Detect cases where the stack has become unbalanced. Due to how UIComponentBase
        // implemented pushing and pooping of components from the ELContext, components
        // that
        // overrode just one of encodeBegin or encodeEnd, or only called super in one case
        // will become unbalanced. Detect and correct for those cases here.
        //
        // detect case where push was never called. In that case, pop should be a no-op
        if (_isPushedAsCurrentRefCount < 1) {
            if (componentELStack.peek() != this) {
                return;
            }
            LOGGER.log(Level.SEVERE, "the component(" + this + ") is the head component of the stack, but it's _isPushedAsCurrentRefCount < 1");
        }

        // check for the other unbalanced case, a component was pushed but never popped.
        // Keep
        // popping those components until we get to our component
        for (UIComponent topComponent = componentELStack.peek(); topComponent != this; topComponent = componentELStack.peek()) {
            topComponent.popComponentFromEL(context);
        }

        // pop ourselves off of the stack
        componentELStack.pop();
        _isPushedAsCurrentRefCount--;

        // If we're a composite component, we also have to pop ourselves off of the
        // composite stack
        if (UIComponent.isCompositeComponent(this)) {
            ArrayDeque<UIComponent> compositeELStack = _getComponentELStack(_CURRENT_COMPOSITE_COMPONENT_STACK_KEY, contextAttributes);
            if (!compositeELStack.isEmpty()) {
                compositeELStack.pop();
            }
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if <code>component</code> is a composite component, otherwise <code>false</code>.
     * </p>
     *
     * @param component the {@link UIComponent} to test
     * @return <code>true</code> if this is a composite component, <code>false</code> otherwise.
     * @throws NullPointerException if <code>component</code> is <code>null</code>
     * @since 2.0
     */
    public static boolean isCompositeComponent(UIComponent component) {

        if (component == null) {
            throw new NullPointerException();
        }
        boolean result = false;
        if (null != component.isCompositeComponent) {
            result = component.isCompositeComponent.booleanValue();
        } else {
            result = component.isCompositeComponent = component.getAttributes().containsKey(Resource.COMPONENT_RESOURCE_KEY);
        }
        return result;

    }

    /**
     * <p>
     * Finds the nearest composite component parent of the specified component.
     * </p>
     *
     * @param component the component from which to start the search from
     *
     * @return if <code>component</code> is <code>null</code>, return <code>null</code>, otherwise search the component's
     * parent hierachy for the nearest parent composite component. If no parent composite component is found, return
     * <code>null</code>
     *
     * @since 2.0
     */
    public static UIComponent getCompositeComponentParent(UIComponent component) {

        if (component == null) {
            return null;
        } else {
            if (component.compositeParent != null) {
                return component.compositeParent;
            }
            UIComponent parent = component.getParent();
            while (parent != null) {
                if (UIComponent.isCompositeComponent(parent)) {
                    if (component.isInView()) {
                        component.compositeParent = parent;
                    }
                    return parent;
                }
                parent = parent.getParent();
            }
            return null;
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>UIComponent</code> instance that is currently processing. This is equivalent to evaluating the
     * Jakarta Expression Language expression "<code>#{component}</code>" and doing a <code>getValue</code> operation on the
     * resultant <code>ValueExpression</code>.
     * </p>
     *
     * <p class="changed_added_2_0">
     * This method must return <code>null</code> if there is no currently processing <code>UIComponent</code>
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @return the current component, or <code>null</code>.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     *
     * @since 2.0
     */
    public static UIComponent getCurrentComponent(FacesContext context) {
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<UIComponent> componentELStack = _getComponentELStack(_CURRENT_COMPONENT_STACK_KEY, contextAttributes);

        return componentELStack.peek();
    }

    /**
     * <p class="changed_added_2_0">
     * Return the closest ancestor component, relative to the component returned from {@link #getCurrentComponent}, that is
     * a composite component, or <code>null</code> if no such component exists.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @return the current composite component, or <code>null</code>.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     *
     * @since 2.0
     */
    public static UIComponent getCurrentCompositeComponent(FacesContext context) {
        return _getComponentELStack(_CURRENT_COMPOSITE_COMPONENT_STACK_KEY, context.getAttributes()).peek();
    }

    // -------------------------------------------------- Event Listener Methods

    /**
     * <p>
     * Add the specified {@link FacesListener} to the set of listeners registered to receive event notifications from this
     * {@link UIComponent}. It is expected that {@link UIComponent} classes acting as event sources will have corresponding
     * typesafe APIs for registering listeners of the required type, and the implementation of those registration methods
     * will delegate to this method. For example:
     * </p>
     *
     * <pre>
     * public class FooEvent extends FacesEvent { ... }
     *
     * public interface FooListener extends FacesListener {
     *   public void processFoo(FooEvent event);
     * }
     *
     * public class FooComponent extends UIComponentBase {
     *   ...
     *   public void addFooListener(FooListener listener) {
     *     addFacesListener(listener);
     *   }
     *   public void removeFooListener(FooListener listener) {
     *     removeFacesListener(listener);
     *   }
     *   ...
     * }
     * </pre>
     *
     * @param listener The {@link FacesListener} to be registered
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    protected abstract void addFacesListener(FacesListener listener);

    /**
     * <p>
     * Return an array of registered {@link FacesListener}s that are instances of the specified class. If there are no such
     * registered listeners, a zero-length array is returned. The returned array can be safely be cast to an array strongly
     * typed to an element type of <code>clazz</code>.
     * </p>
     *
     * @param clazz Class that must be implemented by a {@link FacesListener} for it to be returned
     * @return the Faces listeners, or a zero-length array.
     * @throws IllegalArgumentException if <code>class</code> is not, and does not implement, {@link FacesListener}
     * @throws NullPointerException if <code>clazz</code> is <code>null</code>
     */
    protected abstract FacesListener[] getFacesListeners(Class clazz);

    /**
     * <p>
     * Remove the specified {@link FacesListener} from the set of listeners registered to receive event notifications from
     * this {@link UIComponent}.
     *
     * @param listener The {@link FacesListener} to be deregistered
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    protected abstract void removeFacesListener(FacesListener listener);

    /**
     * <p>
     * Queue an event for broadcast at the end of the current request processing lifecycle phase. The default implementation
     * in {@link UIComponentBase} must delegate this call to the <code>queueEvent()</code> method of the parent
     * {@link UIComponent}.
     * </p>
     *
     * @param event {@link FacesEvent} to be queued
     *
     * @throws IllegalStateException if this component is not a descendant of a {@link UIViewRoot}
     * @throws NullPointerException if <code>event</code> is <code>null</code>
     */
    public abstract void queueEvent(FacesEvent event);

    /**
     * <p class="changed_modified_2_1">
     * This implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of not
     * breaking existing applications that extend this class. {@link UIComponentBase} provides the implementation of this
     * method.
     * </p>
     *
     * @param eventClass the event class.
     * @param componentListener the listener.
     * @since 2.1
     */
    public void subscribeToEvent(Class<? extends SystemEvent> eventClass, ComponentSystemEventListener componentListener) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_modified_2_1">
     * This implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of not
     * breaking existing applications that extend this class. {@link UIComponentBase} provides the implementation of this
     * method.
     * </p>
     *
     * @param eventClass the event class.
     * @param componentListener the component listener.
     * @since 2.1
     */
    public void unsubscribeFromEvent(Class<? extends SystemEvent> eventClass, ComponentSystemEventListener componentListener) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_modified_2_1">
     * This implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of not
     * breaking existing applications that extend this class. {@link UIComponentBase} provides the implementation of this
     * method.
     * </p>
     *
     * @param eventClass the event class.
     * @return the list of listeners, <span class="changed_modified_4_0">never <code>null</code></span>.
     * @since 2.1
     */
    @Override
    public List<SystemEventListener> getListenersForEventClass(Class<? extends SystemEvent> eventClass) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_0">
     * Starting with "this", return the closest component in the ancestry that is a <code>NamingContainer</code> or
     * <code>null</code> if none can be found.
     * </p>
     *
     * @return the naming container, or <code>null</code>.
     * @since 2.0
     */
    public UIComponent getNamingContainer() {
        UIComponent namingContainer = this;
        while (namingContainer != null) {
            if (namingContainer instanceof NamingContainer) {
                return namingContainer;
            }
            namingContainer = namingContainer.getParent();
        }

        return null;
    }

    // ------------------------------------------------ Lifecycle Phase Handlers

    /**
     * <p>
     * <span class="changed_modified_2_0">Perform</span> the component tree processing required by the <em>Restore View</em>
     * phase of the request processing lifecycle for all facets of this component, all children of this component, and this
     * component itself, as follows.
     * </p>
     * <ul>
     * <li class="changed_modified_2_0">Call the <code>restoreState()</code> method of this component.</li>
     *
     * <li class="changed_added_2_0">Call {@link UIComponent#pushComponentToEL}.</li>
     *
     * <li>Call the <code>processRestoreState()</code> method of all facets and children of this {@link UIComponent} in the
     * order determined by a call to <code>getFacetsAndChildren()</code>. <span class="changed_added_2_0">After returning
     * from the <code>processRestoreState()</code> method on a child or facet, call
     * {@link UIComponent#popComponentFromEL}</span></li>
     * </ul>
     *
     * <p>
     * This method may not be called if the state saving method is set to server.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @param state the state.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract void processRestoreState(FacesContext context, Object state);

    /**
     * <p>
     * <span class="changed_modified_2_0">Perform</span> the component tree processing required by the <em>Apply Request
     * Values</em> phase of the request processing lifecycle for all facets of this component, all children of this
     * component, and this component itself, as follows.
     * </p>
     *
     * <ul>
     * <li>If the <code>rendered</code> property of this {@link UIComponent} is <code>false</code>, skip further
     * processing.</li>
     * <li class="changed_added_2_0">Call {@link #pushComponentToEL}.</li>
     *
     * <li>Call the <code>processDecodes()</code> method of all facets and children of this {@link UIComponent}, in the
     * order determined by a call to <code>getFacetsAndChildren()</code>.</li>
     *
     * <li>Call the <code>decode()</code> method of this component.</li>
     *
     * <li>Call {@link #popComponentFromEL} from inside of a <code>finally block, just before returning.</code></li>
     *
     * <li>If a <code>RuntimeException</code> is thrown during decode processing, call {@link FacesContext#renderResponse}
     * and re-throw the exception.</li>
     * </ul>
     *
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract void processDecodes(FacesContext context);

    /**
     * <p class="changed_added_2_0">
     * The default implementation performs the following action. If the argument <code>event</code> is an instance of
     * {@link PostRestoreStateEvent}, call <code>this.</code>{@link #getValueExpression} passing the literal string
     * &#8220;binding&#8221;, without the quotes, as the argument. If the result is non-<code>null</code>, set the value of
     * the <code>ValueExpression</code> to be <code>this</code>.
     * </p>
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        if (event instanceof PostRestoreStateEvent) {

            // If this component has a component value reference expression,
            // make sure to populate the ValueExpression for it.
            ValueExpression valueExpression = getValueExpression("binding");
            if (valueExpression != null) {
                valueExpression.setValue(FacesContext.getCurrentInstance().getELContext(), this);
            }

            isCompositeComponent = null;
        }
    }

    /**
     * <p>
     * <span class="changed_modified_2_0 changed_modified_2_3"><span class=
     * "changed_modified_2_0_rev_a">Perform</span></span> the component tree processing required by the <em>Process
     * Validations</em> phase of the request processing lifecycle for all facets of this component, all children of this
     * component, and this component itself, as follows.
     * </p>
     *
     * <ul>
     * <li>If the <code>rendered</code> property of this {@link UIComponent} is <code>false</code>, skip further
     * processing.</li>
     * <li class="changed_added_2_0">Call {@link #pushComponentToEL}.</li>
     * <li>Call the <code>processValidators()</code> method of all facets and children of this {@link UIComponent}, in the
     * order determined by a call to <code>getFacetsAndChildren()</code>.</li>
     * <li><span class="changed_modified_2_0_rev_a">After returning from calling <code>getFacetsAndChildren()</code> call
     * {@link UIComponent#popComponentFromEL}.</span></li>
     * </ul>
     *
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     * @see jakarta.faces.event.PreValidateEvent
     * @see jakarta.faces.event.PostValidateEvent
     */
    public abstract void processValidators(FacesContext context);

    /**
     * <p>
     * <span class="changed_modified_2_0">Perform</span> the component tree processing required by the <em>Update Model
     * Values</em> phase of the request processing lifecycle for all facets of this component, all children of this
     * component, and this component itself, as follows.
     * </p>
     *
     * <ul>
     * <li>If the <code>rendered</code> property of this {@link UIComponent} is <code>false</code>, skip further
     * processing.</li>
     *
     * <li class="changed_added_2_0">Call {@link #pushComponentToEL}.</li>
     *
     * <li>Call the <code>processUpdates()</code> method of all facets and children of this {@link UIComponent}, in the
     * order determined by a call to <code>getFacetsAndChildren()</code>. <span class="changed_added_2_0">After returning
     * from the <code>processUpdates()</code> method on a child or facet, call
     * {@link UIComponent#popComponentFromEL}</span></li>
     * </ul>
     *
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract void processUpdates(FacesContext context);

    /**
     * <p>
     * <span class="changed_modified_2_0">Perform</span> the component tree processing required by the state saving portion
     * of the <em>Render Response</em> phase of the request processing lifecycle for all facets of this component, all
     * children of this component, and this component itself, as follows.
     * </p>
     *
     * <ul>
     *
     * <li>consult the <code>transient</code> property of this component. If true, just return <code>null</code>.</li>
     *
     * <li class="changed_added_2_0">Call {@link #pushComponentToEL}.</li>
     *
     * <li>Call the <code>processSaveState()</code> method of all facets and children of this {@link UIComponent} in the
     * order determined by a call to <code>getFacetsAndChildren()</code>, skipping children and facets that are transient.
     * Ensure that {@link #popComponentFromEL} is called correctly after each child or facet.</li>
     *
     * <li>Call the <code>saveState()</code> method of this component.</li>
     *
     * <li>Encapsulate the child state and your state into a Serializable Object and return it.</li>
     *
     * </ul>
     *
     * <p>
     * This method may not be called if the state saving method is set to server.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @return the saved state.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract Object processSaveState(FacesContext context);

    // ----------------------------------------------------- Convenience Methods

    /**
     * <p>
     * Convenience method to return the {@link FacesContext} instance for the current request.
     * </p>
     *
     * @return the Faces context.
     */
    protected abstract FacesContext getFacesContext();

    /**
     * <p>
     * Convenience method to return the {@link Renderer} instance associated with this component, if any; otherwise, return
     * <code>null</code>.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return the renderer, or <code>null</code>.
     */
    protected abstract Renderer getRenderer(FacesContext context);

    // --------------------------------------------------------- Package Private

    static final class ComponentSystemEventListenerAdapter
            implements ComponentSystemEventListener, SystemEventListener, StateHolder, FacesWrapper<ComponentSystemEventListener> {

        ComponentSystemEventListener wrapped;
        Class<?> instanceClass;

        // -------------------------------------------------------- Constructors

        ComponentSystemEventListenerAdapter() {
            // necessary for state saving
        }

        ComponentSystemEventListenerAdapter(ComponentSystemEventListener wrapped, UIComponent component) {
            this.wrapped = wrapped;
            instanceClass = component.getClass();
        }

        // ------------------------------------ Methods from SystemEventListener

        /**
         * Process the event.
         *
         * @param event the event.
         * @throws AbortProcessingException if the event processing should be aborted.
         */
        @Override
        public void processEvent(SystemEvent event) throws AbortProcessingException {
            wrapped.processEvent((ComponentSystemEvent) event);
        }

        // ------------------------------------ Methods from SystemEventListener

        /**
         * Process the event.
         *
         * @param event the event.
         * @throws AbortProcessingException if the event processing should be aborted.
         */
        @Override
        public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
            wrapped.processEvent(event);
        }

        /**
         * Is this a listener for the given component.
         *
         * @param component the component.
         * @return <code>true</code> if it is a listener, <code>false</code> otherwise.
         */
        @Override
        public boolean isListenerForSource(Object component) {

            if (wrapped instanceof SystemEventListener) {
                return ((SystemEventListener) wrapped).isListenerForSource(component);
            }

            return instanceClass.isAssignableFrom(component.getClass());
        }

        // -------------------------------------------- Methods from StateHolder

        /**
         * Save the state.
         *
         * @param context the Faces context.
         * @return the saved state.
         */
        @Override
        public Object saveState(FacesContext context) {

            if (context == null) {
                throw new NullPointerException();
            }

            return new Object[] { wrapped instanceof UIComponent ? null : new StateHolderSaver(context, wrapped), instanceClass };
        }

        /**
         * Restore the state.
         *
         * @param context the Faces context.
         * @param state the state.
         */
        @Override
        public void restoreState(FacesContext context, Object state) {

            if (context == null) {
                throw new NullPointerException();
            }

            if (state == null) {
                return;
            }

            Object[] s = (Object[]) state;
            Object listener = s[0];
            wrapped = (ComponentSystemEventListener) (listener == null ? UIComponent.getCurrentComponent(context)
                    : ((StateHolderSaver) listener).restore(context));
            instanceClass = (Class<?>) s[1];
        }

        /**
         * Get the transient flag.
         *
         * @return <code>true</code> if transient, <code>false</code> otherwise.
         */
        @Override
        public boolean isTransient() {

            if (wrapped instanceof StateHolder) {
                return ((StateHolder) wrapped).isTransient();
            }

            return false;
        }

        /**
         * Set the transient flag.
         *
         * <p>
         * This is a no-op in this case.
         * </p>
         *
         * @param newTransientValue the new transient flag value.
         */
        @Override
        public void setTransient(boolean newTransientValue) {
            // no-op
        }

        // ------------------------------------------- Methods from FacesWrapper

        /**
         * Get the wrapped ComponentSystemEventListener.
         *
         * @return the wrapped ComponentSystemEventListener.
         */
        @Override
        public ComponentSystemEventListener getWrapped() {
            return wrapped;
        }

        // ------------------------------------------------------ Public Methods

        @Override
        public int hashCode() {
            return wrapped.hashCode() ^ instanceClass.hashCode();
        }

        @Override
        public boolean equals(Object obj) {

            if (!(obj instanceof ComponentSystemEventListenerAdapter)) {
                return false;
            }

            ComponentSystemEventListenerAdapter in = (ComponentSystemEventListenerAdapter) obj;

            return wrapped.equals(in.wrapped) && instanceClass.equals(in.instanceClass);

        }
    } // END ComponentSystemEventListenerAdapter

    // --------------------------------------------------------- Private methods

    private Map<String, String> wrapBundleAsMap(final ResourceBundle bundle) {
        return new Map<String, String>() {

            // This is an immutable Map

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                Iterator<Map.Entry<String, String>> entries = entrySet().iterator();
                Map.Entry<String, String> cur;
                while (entries.hasNext()) {
                    cur = entries.next();
                    sb.append(cur.getKey()).append(": ").append(cur.getValue()).append('\n');
                }

                return sb.toString();
            }

            // Do not need to implement for immutable Map
            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsKey(Object key) {
                if (key != null) {
                    return bundle.getString(key.toString()) != null;
                }

                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                Enumeration<String> keys = bundle.getKeys();
                while (keys.hasMoreElements()) {
                    if (Objects.equals(value, bundle.getString(keys.nextElement()))) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public Set<Map.Entry<String, String>> entrySet() {
                HashMap<String, String> mappings = new HashMap<>();

                Enumeration<String> keys = bundle.getKeys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    String value = bundle.getString(key);
                    mappings.put(key, value);
                }

                return mappings.entrySet();
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean equals(Object obj) {
                return !(obj == null || !(obj instanceof Map)) && entrySet().equals(((Map<String, String>) obj).entrySet());
            }

            @Override
            public String get(Object key) {
                if (key == null) {
                    return null;
                }

                try {
                    return bundle.getString(key.toString());
                } catch (MissingResourceException e) {
                    return "???" + key + "???";
                }
            }

            @Override
            public int hashCode() {
                return bundle.hashCode();
            }

            @Override
            public boolean isEmpty() {
                return !bundle.getKeys().hasMoreElements();
            }

            @Override
            public Set<String> keySet() {
                Set<String> keySet = new HashSet<>();
                Enumeration<String> keys = bundle.getKeys();
                while (keys.hasMoreElements()) {
                    keySet.add(keys.nextElement());
                }

                return keySet;
            }

            // Do not need to implement for immutable Map
            @Override
            public String put(String k, String v) {
                throw new UnsupportedOperationException();
            }

            // Do not need to implement for immutable Map
            @Override
            public void putAll(Map<? extends String, ? extends String> m) {
                throw new UnsupportedOperationException();
            }

            // Do not need to implement for immutable Map
            @Override
            public String remove(Object k) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int size() {
                int result = 0;

                Enumeration<String> keys = bundle.getKeys();
                while (keys.hasMoreElements()) {
                    keys.nextElement();
                    result++;
                }

                return result;
            }

            @Override
            public Collection<String> values() {
                List<String> result = new ArrayList<>();

                Enumeration<String> keys = bundle.getKeys();
                while (keys.hasMoreElements()) {
                    result.add(bundle.getString(keys.nextElement()));
                }

                return result;
            }
        };
    }

    private ResourceBundle findResourceBundleUnderFQCNofThis(FacesContext context) {
        String className = this.getClass().getName();
        Locale currentLocale = null;
        UIViewRoot root = null;
        ResourceBundle resourceBundle = null;

        // Step 1: look for a ResourceBundle under the FQCN of this instance
        if (context != null) {
            if ((root = context.getViewRoot()) != null) {
                currentLocale = root.getLocale();
            }
        }

        if (currentLocale == null) {
            currentLocale = Locale.getDefault();
        }

        try {
            resourceBundle = ResourceBundle.getBundle(className, currentLocale);
        } catch (MissingResourceException e) {
            // It is not an error if there is no ResourceBundle
        }

        return resourceBundle;
    }

    private ResourceBundle findResourceBundleAsResource(FacesContext context) {

        if (getAttributes().containsKey(COMPONENT_RESOURCE_KEY)) {
            Resource ccResource = (Resource) getAttributes().get(COMPONENT_RESOURCE_KEY);

            if (ccResource != null) {

                ccResource = findComponentResourceBundleLocaleMatch(context, ccResource.getResourceName(), ccResource.getLibraryName());

                if (ccResource != null) {
                    try (InputStream propertiesInputStream = ccResource.getInputStream()) {
                        return new PropertyResourceBundle(propertiesInputStream);
                    } catch (IOException ex) {
                        Logger.getLogger(UIComponent.class.getName()).log(SEVERE, null, ex);
                    }
                }
            }
        }

        return null;
    }

    // PENDING(rlubke): I'm sure there's a more efficient
    // way to handle this.
    private Resource findComponentResourceBundleLocaleMatch(FacesContext context, String resourceName, String libraryName) {
        Resource result = null;
        ResourceBundle resourceBundle = null;
        int i;
        if (-1 != (i = resourceName.lastIndexOf("."))) {
            resourceName = resourceName.substring(0, i) + ".properties";
            if (null != context) {
                result = context.getApplication().getResourceHandler().createResource(resourceName, libraryName);
                InputStream propertiesInputStream = null;
                try {
                    propertiesInputStream = result.getInputStream();
                    resourceBundle = new PropertyResourceBundle(propertiesInputStream);
                } catch (IOException ex) {
                    Logger.getLogger(UIComponent.class.getName()).log(SEVERE, null, ex);
                } finally {
                    if (propertiesInputStream != null) {
                        try {
                            propertiesInputStream.close();
                        } catch (IOException ioe) {
                            if (LOGGER.isLoggable(SEVERE)) {
                                LOGGER.log(SEVERE, null, ioe);
                            }
                        }
                    }
                }
            }
        }

        return resourceBundle != null ? result : null;
    }

}
