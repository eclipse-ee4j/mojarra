/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import static java.util.Collections.unmodifiableList;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.MethodExpression;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.event.PostConstructViewMapEvent;
import jakarta.faces.event.PostRestoreStateEvent;
import jakarta.faces.event.PreDestroyViewMapEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewMetadata;
import jakarta.faces.webapp.FacesServlet;

/**
 * <p>
 * <strong class="changed_modified_2_0"><span class="changed_modified_2_0_rev_a changed_modified_2_1
 * changed_modified_2_2 changed_modified_2_3">UIViewRoot</span></strong> is the UIComponent that represents the root of
 * the UIComponent tree. This component renders markup as the response to Ajax requests. It also serves as the root of
 * the component tree, and as a place to hang per-view {@link PhaseListener}s.
 * </p>
 *
 * <p>
 * For each of the following lifecycle phase methods:
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * {@link #processDecodes}
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * {@link #processValidators}
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * {@link #processUpdates}
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * {@link #processApplication}
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * RenderResponse, via {@link #encodeBegin} and {@link #encodeEnd}
 * </p>
 * </li>
 *
 * </ul>
 *
 * <p>
 * Take the following action regarding <code>PhaseListener</code>s.
 * </p>
 *
 * <blockquote>
 *
 * <p>
 * Initialize a state flag to <code>false</code>.
 * </p>
 *
 * <p>
 * If {@link #getBeforePhaseListener} returns non-<code>null</code>, invoke the listener, passing in the correct
 * corresponding {@link PhaseId} for this phase.
 * </p>
 *
 * <p>
 * Upon return from the listener, call {@link FacesContext#getResponseComplete} and
 * {@link FacesContext#getRenderResponse}. If either return <code>true</code> set the internal state flag to
 * <code>true</code>.
 * </p>
 *
 * <p>
 * If or one or more listeners have been added by a call to {@link #addPhaseListener}, invoke the
 * <code>beforePhase</code> method on each one whose {@link PhaseListener#getPhaseId} matches the current phaseId,
 * passing in the same <code>PhaseId</code> as in the previous step.
 * </p>
 *
 * <p>
 * Upon return from each listener, call {@link FacesContext#getResponseComplete} and
 * {@link FacesContext#getRenderResponse}. If either return <code>true</code> set the internal state flag to
 * <code>true</code>.
 * </p>
 *
 *
 * <p>
 * Execute any processing for this phase if the internal state flag was not set.
 * </p>
 *
 * <p>
 * If {@link #getAfterPhaseListener} returns non-<code>null</code>, invoke the listener, passing in the correct
 * corresponding {@link PhaseId} for this phase.
 * </p>
 *
 * <p>
 * If or one or more listeners have been added by a call to {@link #addPhaseListener}, invoke the
 * <code>afterPhase</code> method on each one whose {@link PhaseListener#getPhaseId} matches the current phaseId,
 * passing in the same <code>PhaseId</code> as in the previous step.
 * </p>
 *
 * </blockquote>
 */

public class UIViewRoot extends UIComponentBase implements UniqueIdVendor {

    // ------------------------------------------------------ Manifest Constants

    /**
     * The key in the facet collection that contains the meta data of the view root.
     * For example, the <code>UIViewParameter</code>s are stored here.
     */
    public static final String METADATA_FACET_NAME = "jakarta_faces_metadata";

    /**
     * <p class="changed_added_2_0">
     * The key in the value set of the <em>view metadata BeanDescriptor</em>, the value of which is a
     * <code>List&lt;{@link UIViewParameter.Reference}&gt;</code>.
     * </p>
     *
     * @since 2.0
     */
    public static final String VIEW_PARAMETERS_KEY = "jakarta.faces.component.VIEW_PARAMETERS_KEY";

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.ViewRoot";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.ViewRoot";

    /**
     * <p class="changed_added_2_3">
     * If this param is set, and calling toLowerCase().equals("true") on a String representation of its value returns true,
     * exceptions thrown by {@link PhaseListener}s installed on the {@code UIViewRoot} are queued to the
     * {@link jakarta.faces.context.ExceptionHandler} instead of being logged and swallowed.
     * </p>
     *
     * @since 2.3
     */
    public static final String VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME = "jakarta.faces.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS";

    /**
     * <p>
     * The prefix that will be used for identifiers generated by the <code>createUniqueId()</code> method.
     */
    static public final String UNIQUE_ID_PREFIX = "j_id";

    private static Lifecycle lifecycle;

    private static final Logger LOGGER = Logger.getLogger("jakarta.faces", "jakarta.faces.LogStrings");

    private static final String LOCATION_IDENTIFIER_PREFIX = "jakarta_faces_location_";
    private static final Map<String, String> LOCATION_IDENTIFIER_MAP = new LinkedHashMap<>(3, 1.0f);
    static {
        LOCATION_IDENTIFIER_MAP.put("head", LOCATION_IDENTIFIER_PREFIX + "HEAD");
        LOCATION_IDENTIFIER_MAP.put("form", LOCATION_IDENTIFIER_PREFIX + "FORM");
        LOCATION_IDENTIFIER_MAP.put("body", LOCATION_IDENTIFIER_PREFIX + "BODY");
    }

    enum PropertyKeys {
        /**
         * <p>
         * The render kit identifier of the {@link jakarta.faces.render.RenderKit} associated wth this view.
         * </p>
         */
        renderKitId,
        /**
         * <p>
         * The view identifier of this view.
         * </p>
         */
        viewId, locale, lastId, beforePhase, afterPhase, phaseListeners, resourceLibraryContracts
    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIViewRoot} instance with default property values.
     * </p>
     */
    public UIViewRoot() {
        super();
        setRendererType(null);
        setId(createUniqueId());
    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>
     * Set during view build time.
     */
    private Doctype doctype;

    /**
     * <p>
     * Set and cleared during the lifetime of a lifecycle phase. Has no meaning between phases. If <code>true</code>, the
     * lifecycle processing for the current phase must not take place.
     * </p>
     */
    private boolean skipPhase;

    /**
     * <p>
     * Set and cleared during the lifetime of a lifecycle phase. Has no meaning between phases. If <code>true</code>, the
     * <code>MethodExpression</code> associated with <code>afterPhase</code> will not be invoked nor will any PhaseListeners
     * associated with this UIViewRoot.
     */
    private boolean beforeMethodException;

    /**
     * <p>
     * Set and cleared during the lifetime of a lifecycle phase. Has no meaning between phases.
     */
    private ListIterator<PhaseListener> phaseListenerIterator;

    // -------------------------------------------------------------- Properties

    /**
     * <p class="changed_added_2_0">
     * Override superclass method to always return {@code true} because a {@code UIViewRoot} is defined to always be in a
     * view.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public boolean isInView() {
        return true;
    }

    /**
     * <p class="changed_added_2_0">
     * Overridden to take no action.
     * </p>
     *
     * @since 2.0
     * @param isInView ignore the value.
     */
    @Override
    public void setInView(boolean isInView) {
        // no-op
    }

    /**
     * @see UIComponent#getFamily()
     */
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * <p>
     * Return the render kit identifier of the {@link jakarta.faces.render.RenderKit} associated with this view. Unless
     * explicitly set, as in {@link jakarta.faces.application.ViewHandler#createView}, the returned value will be
     * <code>null.</code>
     * </p>
     *
     * @return the render kit id, or <code>null</code>.
     */
    public String getRenderKitId() {
        return (String) getStateHelper().eval(PropertyKeys.renderKitId);
    }

    /**
     * <p>
     * Set the render kit identifier of the {@link jakarta.faces.render.RenderKit} associated with this view. This method
     * may be called at any time between the end of <em>Apply Request Values</em> phase of the request processing lifecycle
     * (i.e. when events are being broadcast) and the beginning of the <em>Render Response</em> phase.
     * </p>
     *
     * @param renderKitId The new {@link jakarta.faces.render.RenderKit} identifier, or <code>null</code> to disassociate
     * this view with any specific {@link jakarta.faces.render.RenderKit} instance
     */
    public void setRenderKitId(String renderKitId) {
        getStateHelper().put(PropertyKeys.renderKitId, renderKitId);
    }

    /**
     * <p>
     * Return the view identifier for this view.
     * </p>
     *
     * @return the view id.
     */
    public String getViewId() {
        return (String) getStateHelper().get(PropertyKeys.viewId);
    }

    /**
     * <p>
     * Set the view identifier for this view.
     * </p>
     *
     * @param viewId The new view identifier
     */
    public void setViewId(String viewId) {
        getStateHelper().put(PropertyKeys.viewId, viewId);
    }

    /**
     * <p>
     * Return the doctype of this view.
     * </p>
     *
     * @return the doctype of this view.
     *
     * @since 4.0
     */
    public Doctype getDoctype() {
        return doctype;
    }

    /**
     * <p>
     * Set the doctype of this view.
     * </p>
     *
     * @param doctype The doctype.
     *
     * @since 4.0
     */
    public void setDoctype(Doctype doctype) {
        this.doctype = doctype;
    }

    // ------------------------------------------------ Event Management Methods

    /**
     * <p>
     * Return the {@link MethodExpression} that will be invoked before this view is rendered.
     * </p>
     *
     * @return the {@link MethodExpression} that will be invoked before this view is rendered.
     * @since 1.2
     */
    public MethodExpression getBeforePhaseListener() {
        return (MethodExpression) getStateHelper().get(PropertyKeys.beforePhase);
    }

    /**
     * <p>
     * <span class="changed_modified_2_0 changed_modified_2_0_rev_a">Allow</span> an arbitrary method to be called for the
     * "beforePhase" event as the UIViewRoot runs through its lifecycle. This method will be called for all phases
     * <span class="changed_modified_2_0_rev_a">except {@link PhaseId#RESTORE_VIEW}. Unlike a true {@link PhaseListener},
     * this approach doesn't allow for only receiving {@link PhaseEvent}s for a given phase.</span>
     * </p>
     *
     * <p>
     * The method must conform to the signature of {@link PhaseListener#beforePhase}.
     * </p>
     *
     * @param newBeforePhase the {@link MethodExpression} that will be invoked before this view is rendered.
     * @since 1.2
     */
    public void setBeforePhaseListener(MethodExpression newBeforePhase) {
        getStateHelper().put(PropertyKeys.beforePhase, newBeforePhase);
    }

    /**
     * <p>
     * Return the {@link MethodExpression} that will be invoked after this view is rendered.
     * </p>
     *
     * @return the {@link MethodExpression} that will be invoked after this view is rendered.
     *
     * @since 1.2
     */
    public MethodExpression getAfterPhaseListener() {
        return (MethodExpression) getStateHelper().get(PropertyKeys.afterPhase);
    }

    /**
     * <p>
     * <span class="changed_modified_2_0">Allow</span> an arbitrary method to be called for the "afterPhase" event as the
     * UIViewRoot runs through its lifecycle. This method will be called for all phases
     * <span class="changed_modified_2_0">including {@link PhaseId#RESTORE_VIEW}</span>. Unlike a true
     * {@link PhaseListener}, this approach doesn't allow for only receiving {@link PhaseEvent}s for a given phase.
     * </p>
     * <p>
     * The method must conform to the signature of {@link PhaseListener#afterPhase}.
     * </p>
     *
     * @param newAfterPhase the {@link MethodExpression} that will be invoked after this view is rendered.
     *
     * @since 1.2
     */
    public void setAfterPhaseListener(MethodExpression newAfterPhase) {
        getStateHelper().put(PropertyKeys.afterPhase, newAfterPhase);
    }

    /**
     * <p>
     * If the argument <code>toRemove</code> is in the list of {@link PhaseListener}s for this instance, it must be removed.
     * </p>
     *
     * @param toRemove the {@link PhaseListener} to remove.
     *
     * @since 1.2
     */
    public void removePhaseListener(PhaseListener toRemove) {
        getStateHelper().remove(PropertyKeys.phaseListeners, toRemove);
    }

    /**
     * <p>
     * Add the argument <code>newPhaseListener</code> to the list of {@link PhaseListener}s on this <code>UIViewRoot</code>.
     * </p>
     *
     * @param newPhaseListener the {@link PhaseListener} to add
     *
     * @since 1.2
     */
    public void addPhaseListener(PhaseListener newPhaseListener) {
        getStateHelper().add(PropertyKeys.phaseListeners, newPhaseListener);
    }

    /**
     *
     * <p class="changed_added_2_0">
     * Return an unmodifiable list of the <code>PhaseListener</code> instances attached to this <code>UIViewRoot</code>
     * instance.
     * </p>
     *
     * @return the list of phase listeners.
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    public List<PhaseListener> getPhaseListeners() {
        List<PhaseListener> result = (List<PhaseListener>) getStateHelper().get(PropertyKeys.phaseListeners);

        return result != null ? unmodifiableList(result) : unmodifiableList(Collections.<PhaseListener>emptyList());
    }

    /**
     * <p class="changed_added_2_0">
     * Add argument <code>component</code>, which is assumed to represent a resource instance, as a resource to this view. A
     * resource instance is rendered by a resource <code>Renderer</code>, as described in the Standard HTML RenderKit. The
     * default implementation must call through to
     * {@link #addComponentResource(jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.String)}.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param componentResource The {@link UIComponent} representing a {@link jakarta.faces.application.Resource} instance
     *
     * @since 2.0
     */
    public void addComponentResource(FacesContext context, UIComponent componentResource) {
        addComponentResource(context, componentResource, null);
    }

    /**
     * <p class="changed_added_2_0">
     * Add argument <code>component</code>, which is assumed to represent a resource instance, as a resource to this view. A
     * resource instance is rendered by a resource <code>Renderer</code>, as described in the Standard HTML RenderKit.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * The <code>component</code> must be added using the following algorithm:
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * If the <code>target</code> argument is <code>null</code>, look for a <code>target</code> attribute on the
     * <code>component</code>. If there is no <code>target</code> attribute, set <code>target</code> to be the default value
     * <code>head</code>
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Call {@link #getComponentResources} to obtain the child list for the given target.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * If the component ID of <code>componentResource</code> matches the the ID of a resource that has allready been added,
     * remove the old resource.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Add the <code>component</code> resource to the list.
     * </p>
     * </li>
     *
     * </ul>
     *
     * </div>
     *
     * <div class="changed_added_2_3">
     * <p>
     * The resource <code>Renderer</code> must ensure of the following:
     * <ul>
     * <li>Do not render when {@link ResourceHandler#isResourceRendered(FacesContext, String, String)} returns
     * <code>true</code>.</li>
     * <li>After rendering, call {@link ResourceHandler#markResourceRendered(FacesContext, String, String)}.</li>
     * </ul>
     * </div>
     *
     * @param context {@link FacesContext} for the current request
     * @param componentResource The {@link UIComponent} representing a {@link jakarta.faces.application.Resource} instance
     * @param target The name of the facet for which the {@link UIComponent} will be added
     *
     * @since 2.0
     */
    public void addComponentResource(FacesContext context, UIComponent componentResource, String target) {
        final Map<String, Object> attributes = componentResource.getAttributes();

        // look for a target in the component attribute set if arg is not set.
        if (target == null) {
            target = (String) attributes.get("target");
        }
        if (target == null) {
            target = "head";
        }
        List<UIComponent> facetChildren = getComponentResources(context, target, true);
        String id = componentResource.getId();
        if (id != null) {
            facetChildren.removeIf(c -> id.equals(c.getId()));
        }

        // add the resource to the facet
        facetChildren.add(componentResource);
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_deleted_2_0_rev_a changed_modified_2_1">Return</span> an unmodifiable <code>List</code> of
     * {@link UIComponent}s for the provided <code>target</code> agrument. Each <code>component</code> in the
     * <code>List</code> is assumed to represent a resource instance.
     * </p>
     *
     * <div class="changed_added_2_0">
     * <p>
     * The default implementation must use an algorithm equivalent to the the following.
     * </p>
     * <ul>
     * <li>Locate the facet for the <code>component</code> by calling <code>getFacet()</code> using <code>target</code> as
     * the argument.</li>
     *
     * <li>If the facet is not found, create the facet by calling <code>context.getApplication().createComponent().</code>
     * <span class="changed_modified_2_0_rev_a">The argument to this method must refer to a component that extends
     * {@link UIPanel} and overrides the <code>encodeAll()</code> method to take no action. This is necessary to prevent
     * component resources from being inadvertently rendered.</span>
     *
     * <ul>
     *
     * <li class="changed_modified_2_1">Set the <code>id</code> of the facet to be a string created by prepending the
     * literal string &#8220;<code>jakarta_faces_location_</code>&#8221; (without the quotes) to the value of the
     * <code>target</code> argument</li>
     *
     * <li>Add the facet to the facets <code>Map</code> using <code>target</code> as the key</li>
     * </ul>
     * <li>return the children of the facet</li>
     * </ul>
     *
     * </div>
     *
     * @param context the Faces context.
     * @param target The name of the facet for which the components will be returned.
     *
     * @return A <code>List</code> of {@link UIComponent} children of the facet with the name <code>target</code>. If no
     * children are found for the facet, return <code>Collections.emptyList()</code>.
     *
     * @throws NullPointerException if <code>target</code> or <code>context</code> is <code>null</code>
     *
     * @since 2.0
     */
    public List<UIComponent> getComponentResources(FacesContext context, String target) {
        if (target == null) {
            throw new NullPointerException();
        }

        List<UIComponent> resources = getComponentResources(context, target, false);

        return resources != null ? resources : Collections.emptyList();
    }

    /**
     * <p class="changed_added_2_3">
     * Return an unmodifiable ordered <code>List</code> of all {@link UIComponent} resources of all supported targets. Each
     * <code>component</code> in the <code>List</code> is assumed to represent a resource instance. The ordering is the same
     * as the resources would appear in the component tree.
     * </p>
     *
     * @param context The Faces context.
     *
     * @return A <code>List</code> of all {@link UIComponent} resources of all supported targets. If no resources are found,
     * return an empty <code>List</code>.
     *
     * @throws NullPointerException If <code>context</code> is <code>null</code>.
     *
     * @since 2.3
     */
    public List<UIComponent> getComponentResources(FacesContext context) {
        List<UIComponent> resources = new ArrayList<>();

        for (String target : LOCATION_IDENTIFIER_MAP.keySet()) {
            resources.addAll(getComponentResources(context, target));
        }

        return unmodifiableList(resources);
    }

    /**
     * <p class="changed_added_2_0">
     * Remove argument <code>component</code>, which is assumed to represent a resource instance, as a resource to this
     * view.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param componentResource The {@link UIComponent} representing a {@link jakarta.faces.application.Resource} instance
     *
     * @since 2.0
     */
    public void removeComponentResource(FacesContext context, UIComponent componentResource) {
        removeComponentResource(context, componentResource, null);
    }

    /**
     * <p class="changed_added_2_0">
     * Remove argument <code>component</code>, which is assumed to represent a resource instance, as a resource to this
     * view. A resource instance is rendered by a resource <code>Renderer</code>, as described in the Standard HTML
     * RenderKit.
     * </p>
     *
     * <div class="changed_added_2_0">
     * <p>
     * The <code>component</code> must be removed using the following algorithm:
     * <ul>
     * <li>If the <code>target</code> argument is <code>null</code>, look for a <code>target</code> attribute on the
     * <code>component</code>.<br>
     * If there is no <code>target</code> attribute, set <code>target</code> to be the default value <code>head</code></li>
     * <li>Call {@link #getComponentResources} to obtain the child list for the given target.</li>
     * <li>Remove the <code>component</code> resource from the child list.</li>
     * </ul>
     * </div>
     *
     * @param context {@link FacesContext} for the current request
     * @param componentResource The {@link UIComponent} representing a {@link jakarta.faces.application.Resource} instance
     * @param target The name of the facet for which the {@link UIComponent} will be added
     *
     * @since 2.0
     */
    public void removeComponentResource(FacesContext context, UIComponent componentResource, String target) {
        final Map<String, Object> attributes = componentResource.getAttributes();

        // look for a target in the component attribute set if arg is not set.
        if (target == null) {
            target = (String) attributes.get("target");
        }
        if (target == null) {
            target = "head";
        }
        List<UIComponent> facetChildren = getComponentResources(context, target, false);
        if (facetChildren != null) {
            facetChildren.remove(componentResource);
        }
    }

    /**
     * <p>
     * An array of Lists of events that have been queued for later broadcast, with one List for each lifecycle phase. The
     * list indices match the ordinals of the PhaseId instances. This instance is lazily instantiated. This list is
     * <strong>NOT</strong> part of the state that is saved and restored for this component.
     * </p>
     */
    private List<List<FacesEvent>> events;

    /**
     * <p>
     * Override the default {@link UIComponentBase#queueEvent} behavior to accumulate the queued events for later
     * broadcasting.
     * </p>
     *
     * @param event {@link FacesEvent} to be queued
     *
     * @throws IllegalStateException if this component is not a descendant of a {@link UIViewRoot}
     * @throws NullPointerException if <code>event</code> is <code>null</code>
     */
    @Override
    public void queueEvent(FacesEvent event) {
        if (event == null) {
            throw new NullPointerException();
        }

        // We are a UIViewRoot, so no need to check for the ISE
        if (events == null) {
            int len = PhaseId.VALUES.size();
            List<List<FacesEvent>> events = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                events.add(new ArrayList<>(5));
            }
            this.events = events;
        }

        events.get(event.getPhaseId().getOrdinal()).add(event);
    }

    /**
     * <p class="changed_added_2_0">
     * Broadcast any events that have been queued. First broadcast events that have been queued for
     * {@link PhaseId#ANY_PHASE}. Then broadcast ane events that have been queued for the current phase. In both cases,
     * {@link UIComponent#pushComponentToEL} must be called before the event is broadcast, and
     * {@link UIComponent#popComponentFromEL} must be called after the return from the broadcast, even in the case of an
     * exception.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param phaseId {@link PhaseId} of the current phase
     *
     * @since 2.0
     */
    public void broadcastEvents(FacesContext context, PhaseId phaseId) {
        if (events == null) {
            // no events have been queued
            return;
        }

        boolean hasMoreAnyPhaseEvents;
        boolean hasMoreCurrentPhaseEvents;

        List<FacesEvent> eventsForPhaseId = events.get(PhaseId.ANY_PHASE.getOrdinal());

        // keep iterating till we have no more events to broadcast.
        // This is necessary for events that cause other events to be
        // queued. PENDING(edburns): here's where we'd put in a check
        // to prevent infinite event queueing.
        do {
            // broadcast the ANY_PHASE events first
            if (null != eventsForPhaseId) {
                // We cannot use an Iterator because we will get
                // ConcurrentModificationException errors, so fake it
                while (!eventsForPhaseId.isEmpty()) {
                    FacesEvent event = eventsForPhaseId.get(0);
                    UIComponent source = event.getComponent();
                    UIComponent compositeParent = null;
                    try {
                        if (!UIComponent.isCompositeComponent(source)) {
                            compositeParent = UIComponent.getCompositeComponentParent(source);
                        }
                        if (compositeParent != null) {
                            compositeParent.pushComponentToEL(context, null);
                        }
                        source.pushComponentToEL(context, null);
                        source.broadcast(event);
                    } catch (AbortProcessingException e) {
                        context.getApplication().publishEvent(context, ExceptionQueuedEvent.class,
                                new ExceptionQueuedEventContext(context, e, source, phaseId));
                    } finally {
                        source.popComponentFromEL(context);
                        if (compositeParent != null) {
                            compositeParent.popComponentFromEL(context);
                        }
                    }
                    eventsForPhaseId.remove(0); // Stay at current position
                }
            }

            // then broadcast the events for this phase.
            if ((eventsForPhaseId = events.get(phaseId.getOrdinal())) != null) {
                // We cannot use an Iterator because we will get
                // ConcurrentModificationException errors, so fake it
                while (!eventsForPhaseId.isEmpty()) {
                    FacesEvent event = eventsForPhaseId.get(0);
                    UIComponent source = event.getComponent();
                    UIComponent compositeParent = null;
                    try {
                        if (!UIComponent.isCompositeComponent(source)) {
                            compositeParent = getCompositeComponentParent(source);
                        }
                        if (compositeParent != null) {
                            compositeParent.pushComponentToEL(context, null);
                        }
                        source.pushComponentToEL(context, null);
                        source.broadcast(event);
                    } catch (AbortProcessingException ape) {
                        // A "return" here would abort remaining events too
                        context.getApplication().publishEvent(context, ExceptionQueuedEvent.class,
                                new ExceptionQueuedEventContext(context, ape, source, phaseId));
                    } finally {
                        source.popComponentFromEL(context);
                        if (compositeParent != null) {
                            compositeParent.popComponentFromEL(context);
                        }
                    }
                    eventsForPhaseId.remove(0); // Stay at current position
                }
            }

            // true if we have any more ANY_PHASE events
            hasMoreAnyPhaseEvents = null != (eventsForPhaseId = events.get(PhaseId.ANY_PHASE.getOrdinal())) && !eventsForPhaseId.isEmpty();
            // true if we have any more events for the argument phaseId
            hasMoreCurrentPhaseEvents = null != events.get(phaseId.getOrdinal()) && !events.get(phaseId.getOrdinal()).isEmpty();

        } while (hasMoreAnyPhaseEvents || hasMoreCurrentPhaseEvents);
    }


    // ------------------------------------------------ Lifecycle Phase Handlers

    @SuppressWarnings("unchecked")
    private void initState() {
        skipPhase = false;
        beforeMethodException = false;
        List<PhaseListener> listeners = (List<PhaseListener>) getStateHelper().get(PropertyKeys.phaseListeners);
        phaseListenerIterator = listeners != null ? listeners.listIterator() : null;
    }

    // avoid creating the PhaseEvent if possible by doing redundant
    // null checks.
    private void notifyBefore(FacesContext context, PhaseId phaseId) {
        if (getBeforePhaseListener() != null || phaseListenerIterator != null) {
            notifyPhaseListeners(context, phaseId, true);
        }
    }

    // avoid creating the PhaseEvent if possible by doing redundant
    // null checks.
    private void notifyAfter(FacesContext context, PhaseId phaseId) {
        if (getAfterPhaseListener() != null || phaseListenerIterator != null) {
            notifyPhaseListeners(context, phaseId, false);
        }
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_deleted_2_0_rev_a">The</span> default implementation must call
     * {@link UIComponentBase#processRestoreState} from within a <code>try</code> block. The <code>try</code> block must
     * have a <code>finally</code> block that ensures that no {@link FacesEvent}s remain in the event queue.
     * <a class="changed_deleted_2_0_rev_a"> <!-- text removed in 2.0 Rev a: and that the this.visitTree is called, passing
     * a ContextCallback that takes the following action: call the processEvent method of the current component. The
     * argument event must be an instance of PostRestoreStateEvent whose component property is the current component in the
     * traversal. --></a>
     * </p>
     *
     * @param context the <code>FacesContext</code> for this requets
     * @param state the opaque state object obtained from the {@link jakarta.faces.application.StateManager}
     */
    @Override
    public void processRestoreState(FacesContext context, Object state) {
        // hack to work around older state managers that may not set the
        // view root early enough
        if (context.getViewRoot() == null) {
            context.setViewRoot(this);
        }

        super.processRestoreState(context, state);
    }

    /**
     * <p class="changed_added_2_3">
     * If the argument <code>event</code> is an instance of {@link PostRestoreStateEvent} and
     * {@link PartialViewContext#isPartialRequest()} returns <code>true</code>, then loop over all component resources and
     * call {@link ResourceHandler#markResourceRendered(FacesContext, String, String)} for each of them. Finally, delegate
     * to super.
     * </p>
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        FacesContext context = event.getFacesContext();

        if (event instanceof PostRestoreStateEvent && context.getPartialViewContext().isPartialRequest() && !context.getPartialViewContext().isRenderAll()) {
            ResourceHandler resourceHandler = context.getApplication().getResourceHandler();

            for (UIComponent resource : getComponentResources(context)) {
                String name = (String) resource.getAttributes().get("name");
                String library = (String) resource.getAttributes().get("library");
                resourceHandler.markResourceRendered(context, name, library);
            }
        }

        super.processEvent(event);
    }

    /**
     * <div class="changed_added_2_0">
     * <p>
     * Perform partial processing by calling {@link jakarta.faces.context.PartialViewContext#processPartial} with
     * {@link PhaseId#APPLY_REQUEST_VALUES} if:
     * <ul>
     * <li>{@link jakarta.faces.context.PartialViewContext#isPartialRequest} returns <code>true</code> and we don't have a
     * request to process all components in the view ({@link jakarta.faces.context.PartialViewContext#isExecuteAll} returns
     * <code>false</code>)</li>
     * </ul>
     * Perform full processing by calling {@link UIComponentBase#processDecodes} if one of the following conditions are met:
     * <ul>
     * <li>{@link jakarta.faces.context.PartialViewContext#isPartialRequest} returns <code>true</code> and we have a request
     * to process all components in the view ({@link jakarta.faces.context.PartialViewContext#isExecuteAll} returns
     * <code>true</code>)</li>
     * <li>{@link jakarta.faces.context.PartialViewContext#isPartialRequest} returns <code>false</code></li>
     * </ul>
     * </div>
     * <p class="changed_modified_2_0">
     * Override the default {@link UIComponentBase#processDecodes} behavior to broadcast any queued events after the default
     * processing or partial processing has been completed and to clear out any events for later phases if the event
     * processing for this phase caused {@link FacesContext#renderResponse} or {@link FacesContext#responseComplete} to be
     * called.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    public void processDecodes(FacesContext context) {
        initState();
        notifyBefore(context, PhaseId.APPLY_REQUEST_VALUES);

        try {
            if (!skipPhase) {
                if (context.getPartialViewContext().isPartialRequest() && !context.getPartialViewContext().isExecuteAll()) {
                    context.getPartialViewContext().processPartial(PhaseId.APPLY_REQUEST_VALUES);
                } else {
                    super.processDecodes(context);
                }
                broadcastEvents(context, PhaseId.APPLY_REQUEST_VALUES);
            }
        } finally {
            clearFacesEvents(context);
            notifyAfter(context, PhaseId.APPLY_REQUEST_VALUES);
        }
    }

    /**
     * <p class="changed_added_2_2">
     * Visit the clientIds and, if the component is an instance of {@link EditableValueHolder}, call its
     * {@link EditableValueHolder#resetValue} method. Use {@link #visitTree} to do the visiting.
     * </p>
     *
     * @since 2.2
     *
     * @param context the {@link FacesContext} for the request we are processing.
     * @param clientIds The client ids to be visited, on which the described action will be taken.
     */

    public void resetValues(FacesContext context, Collection<String> clientIds) {
        visitTree(VisitContext.createVisitContext(context, clientIds, null), new DoResetValues());
    }

    private static class DoResetValues implements VisitCallback {
        @Override
        public VisitResult visit(VisitContext context, UIComponent target) {
            if (target instanceof EditableValueHolder) {
                ((EditableValueHolder) target).resetValue();
            }
            return VisitResult.ACCEPT;
        }
    }

    /**
     * <p>
     * <span class="changed_added_2_0 changed_modified_2_3"> Override</span> the default {@link UIComponentBase#encodeBegin}
     * behavior. If {@link #getBeforePhaseListener} returns non-<code>null</code>, invoke it, passing a {@link PhaseEvent}
     * for the {@link PhaseId#RENDER_RESPONSE} phase. If the internal list populated by calls to {@link #addPhaseListener}
     * is non-empty, any listeners in that list must have their {@link PhaseListener#beforePhase} method called, passing the
     * <code>PhaseEvent</code>. Any {@code Exception}s that occur during invocation of any of the beforePhase listeners must
     * be logged and swallowed, <span class="changed_added_2_3"> unless the
     * {@link #VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME} parameter is set. In that case, the {@code Exception}
     * must be passed to the {@link jakarta.faces.context.ExceptionHandler} as well.</span>
     * </p>
     */
    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        initState();
        notifyBefore(context, PhaseId.RENDER_RESPONSE);

        if (!context.getResponseComplete()) {
            super.encodeBegin(context);
        }
    }

    /**
     * <p class="changed_added_2_0">
     * If {@link jakarta.faces.context.PartialViewContext#isAjaxRequest} returns <code>true</code>, perform partial
     * rendering by calling {@link jakarta.faces.context.PartialViewContext#processPartial} with
     * {@link PhaseId#RENDER_RESPONSE}. If {@link jakarta.faces.context.PartialViewContext#isAjaxRequest} returns
     * <code>false</code>, delegate to the parent {@link jakarta.faces.component.UIComponentBase#encodeChildren} method.
     * </p>
     *
     * <p class="changed_added_2_3">
     * If this {@link UIViewRoot} is an instance of {@link NamingContainer}, then the Jakarta Faces implementation
     * must ensure that all encoded POST request parameter names are prefixed with
     * {@link UIViewRoot#getContainerClientId(FacesContext)} as per rules of {@link UIComponent#getClientId(FacesContext)}.
     * This also covers all predefined POST request parameters which are listed below:
     * </p>
     * <ul class="changed_added_2_3">
     * <li>{@link ResponseStateManager#VIEW_STATE_PARAM}</li>
     * <li>{@link ResponseStateManager#CLIENT_WINDOW_PARAM}</li>
     * <li>{@link ResponseStateManager#RENDER_KIT_ID_PARAM}</li>
     * <li>{@link ClientBehaviorContext#BEHAVIOR_SOURCE_PARAM_NAME}</li>
     * <li>{@link ClientBehaviorContext#BEHAVIOR_EVENT_PARAM_NAME}</li>
     * <li>{@link PartialViewContext#PARTIAL_EVENT_PARAM_NAME}</li>
     * <li>{@link PartialViewContext#PARTIAL_EXECUTE_PARAM_NAME}</li>
     * <li>{@link PartialViewContext#PARTIAL_RENDER_PARAM_NAME}</li>
     * <li>{@link PartialViewContext#RESET_VALUES_PARAM_NAME}</li>
     * </ul>
     *
     * @since 2.0
     */
    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        if (context.getPartialViewContext().isAjaxRequest()) {
            context.getPartialViewContext().processPartial(PhaseId.RENDER_RESPONSE);
        } else {
            super.encodeChildren(context);
        }
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_3">If</span> {@link #getAfterPhaseListener} returns non-<code>null</code>, invoke it,
     * passing a {@link PhaseEvent} for the {@link PhaseId#RENDER_RESPONSE} phase. Any {@code Exception}s that occur during
     * invocation of the afterPhase listener must be logged and swallowed, <span class="changed_added_2_3"> unless the
     * {@link #VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME} parameter is set. In that case, the {@code Exception}
     * must be passed to the {@link jakarta.faces.context.ExceptionHandler} as well.</span>. If the current view has view
     * parameters, as indicated by a non-empty and non-<code>UnsupportedOperationException</code> throwing return from
     * {@link jakarta.faces.view.ViewDeclarationLanguage#getViewMetadata(jakarta.faces.context.FacesContext, String)}, call
     * {@link UIViewParameter#encodeAll} on each parameter. If calling <code>getViewParameters()</code> causes
     * <code>UnsupportedOperationException</code> to be thrown, the exception must be silently swallowed.
     * </p>
     */
    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        super.encodeEnd(context);
        encodeViewParameters(context);
        notifyAfter(context, PhaseId.RENDER_RESPONSE);
    }

    /**
     * <p class="changed_added_2_0">
     * Call {@link UIComponentBase#getRendersChildren} If {@link jakarta.faces.context.PartialViewContext#isAjaxRequest}
     * returns <code>true</code> this method must return <code>true</code>.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public boolean getRendersChildren() {
        boolean value = super.getRendersChildren();
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getPartialViewContext().isAjaxRequest()) {
            value = true;
        }
        return value;
    }

    /**
     * <p>
     * Utility method that notifies phaseListeners for the given phaseId. Assumes that either or both the MethodExpression
     * or phaseListeners data structure are non-null.
     * </p>
     *
     * @param context the context for this request
     * @param phaseId the {@link PhaseId} of the current phase
     * @param isBefore if true, notify beforePhase listeners. Notify afterPhase listeners otherwise.
     */
    private void notifyPhaseListeners(FacesContext context, PhaseId phaseId, boolean isBefore) {
        PhaseEvent event = createPhaseEvent(context, phaseId);

        MethodExpression beforePhase = getBeforePhaseListener();
        MethodExpression afterPhase = getAfterPhaseListener();
        boolean hasPhaseMethodExpression = isBefore && null != beforePhase || !isBefore && null != afterPhase && !beforeMethodException;
        MethodExpression expression = isBefore ? beforePhase : afterPhase;

        if (hasPhaseMethodExpression) {
            try {
                expression.invoke(context.getELContext(), new Object[] { event });
                skipPhase = context.getResponseComplete() || context.getRenderResponse();
            } catch (Exception e) {
                if (isBefore) {
                    beforeMethodException = true;
                }
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "severe.component.unable_to_process_expression",
                            new Object[] { expression.getExpressionString(), isBefore ? "beforePhase" : "afterPhase" });
                }
                if (context.getAttributes().containsKey(VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME)) {
                    ExceptionQueuedEventContext extx = new ExceptionQueuedEventContext(context, e);
                    String booleanKey = isBefore ? ExceptionQueuedEventContext.IN_BEFORE_PHASE_KEY : ExceptionQueuedEventContext.IN_AFTER_PHASE_KEY;
                    extx.getAttributes().put(booleanKey, Boolean.TRUE);
                    context.getApplication().publishEvent(context, ExceptionQueuedEvent.class, extx);

                }
                return;
            }
        }
        if (phaseListenerIterator != null && !beforeMethodException) {
            while (isBefore ? phaseListenerIterator.hasNext() : phaseListenerIterator.hasPrevious()) {
                PhaseListener curListener = isBefore ? phaseListenerIterator.next() : phaseListenerIterator.previous();
                if (phaseId == curListener.getPhaseId() || PhaseId.ANY_PHASE == curListener.getPhaseId()) {
                    try {
                        if (isBefore) {
                            curListener.beforePhase(event);
                        } else {
                            curListener.afterPhase(event);
                        }
                        skipPhase = context.getResponseComplete() || context.getRenderResponse();
                    } catch (Exception e) {
                        if (isBefore && phaseListenerIterator.hasPrevious()) {
                            phaseListenerIterator.previous();
                        }
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, "severe.component.uiviewroot_error_invoking_phaselistener", curListener.getClass().getName());
                        }
                        if (context.getAttributes().containsKey(VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME)
                                && (Boolean) context.getAttributes().get(VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME)) {
                            ExceptionQueuedEventContext extx = new ExceptionQueuedEventContext(context, e);
                            String booleanKey = isBefore ? ExceptionQueuedEventContext.IN_BEFORE_PHASE_KEY : ExceptionQueuedEventContext.IN_AFTER_PHASE_KEY;
                            extx.getAttributes().put(booleanKey, Boolean.TRUE);
                            context.getApplication().publishEvent(context, ExceptionQueuedEvent.class, extx);

                        }
                        return;
                    }
                }
            }
        }
    }

    private static PhaseEvent createPhaseEvent(FacesContext context, PhaseId phaseId) throws FacesException {
        if (lifecycle == null) {
            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            String lifecycleId = context.getExternalContext().getInitParameter(FacesServlet.LIFECYCLE_ID_ATTR);
            if (lifecycleId == null) {
                lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
            }
            lifecycle = lifecycleFactory.getLifecycle(lifecycleId);
        }

        return new PhaseEvent(context, phaseId, lifecycle);
    }

    /**
     * <div class="changed_added_2_0">
     * <p>
     * Perform partial processing by calling {@link jakarta.faces.context.PartialViewContext#processPartial} with
     * {@link PhaseId#PROCESS_VALIDATIONS} if:
     * <ul>
     * <li>{@link jakarta.faces.context.PartialViewContext#isPartialRequest} returns <code>true</code> and we don't have a
     * request to process all components in the view ({@link jakarta.faces.context.PartialViewContext#isExecuteAll} returns
     * <code>false</code>)</li>
     * </ul>
     * Perform full processing by calling {@link UIComponentBase#processValidators} if one of the following conditions are
     * met:
     * <ul>
     * <li>{@link jakarta.faces.context.PartialViewContext#isPartialRequest} returns <code>true</code> and we have a request
     * to process all components in the view ({@link jakarta.faces.context.PartialViewContext#isExecuteAll} returns
     * <code>true</code>)</li>
     * <li>{@link jakarta.faces.context.PartialViewContext#isPartialRequest} returns <code>false</code></li>
     * </ul>
     * </div>
     * <p class="changed_modified_2_0">
     * Override the default {@link UIComponentBase#processValidators} behavior to broadcast any queued events after the
     * default processing or partial processing has been completed and to clear out any events for later phases if the event
     * processing for this phase caused {@link FacesContext#renderResponse} or {@link FacesContext#responseComplete} to be
     * called.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    public void processValidators(FacesContext context) {
        initState();
        notifyBefore(context, PhaseId.PROCESS_VALIDATIONS);

        try {
            if (!skipPhase) {
                if (context.getPartialViewContext().isPartialRequest() && !context.getPartialViewContext().isExecuteAll()) {
                    context.getPartialViewContext().processPartial(PhaseId.PROCESS_VALIDATIONS);
                } else {
                    super.processValidators(context);
                }
                broadcastEvents(context, PhaseId.PROCESS_VALIDATIONS);
            }
        } finally {
            clearFacesEvents(context);
            notifyAfter(context, PhaseId.PROCESS_VALIDATIONS);
        }
    }

    /**
     * <div class="changed_added_2_0">
     * <p>
     * Perform partial processing by calling {@link jakarta.faces.context.PartialViewContext#processPartial} with
     * {@link PhaseId#UPDATE_MODEL_VALUES} if:
     * <ul>
     * <li>{@link jakarta.faces.context.PartialViewContext#isPartialRequest} returns <code>true</code> and we don't have a
     * request to process all components in the view ({@link jakarta.faces.context.PartialViewContext#isExecuteAll} returns
     * <code>false</code>)</li>
     * </ul>
     * Perform full processing by calling {@link UIComponentBase#processUpdates} if one of the following conditions are met:
     * <ul>
     * <li>{@link jakarta.faces.context.PartialViewContext#isPartialRequest} returns <code>true</code> and we have a request
     * to process all components in the view ({@link jakarta.faces.context.PartialViewContext#isExecuteAll} returns
     * <code>true</code>)</li>
     * <li>{@link jakarta.faces.context.PartialViewContext#isPartialRequest} returns <code>false</code></li>
     * </ul>
     * </div>
     * <p class="changed_modified_2_0">
     * Override the default {@link UIComponentBase} behavior to broadcast any queued events after the default processing or
     * partial processing has been completed and to clear out any events for later phases if the event processing for this
     * phase caused {@link FacesContext#renderResponse} or {@link FacesContext#responseComplete} to be called.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    public void processUpdates(FacesContext context) {
        initState();
        notifyBefore(context, PhaseId.UPDATE_MODEL_VALUES);

        try {
            if (!skipPhase) {
                if (context.getPartialViewContext().isPartialRequest() && !context.getPartialViewContext().isExecuteAll()) {
                    context.getPartialViewContext().processPartial(PhaseId.UPDATE_MODEL_VALUES);
                } else {
                    super.processUpdates(context);
                }
                broadcastEvents(context, PhaseId.UPDATE_MODEL_VALUES);
            }
        } finally {
            clearFacesEvents(context);
            notifyAfter(context, PhaseId.UPDATE_MODEL_VALUES);
        }
    }

    /**
     * <p>
     * Broadcast any events that have been queued for the <em>Invoke Application</em> phase of the request processing
     * lifecycle and to clear out any events for later phases if the event processing for this phase caused
     * {@link FacesContext#renderResponse} or {@link FacesContext#responseComplete} to be called.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public void processApplication(FacesContext context) {
        initState();
        notifyBefore(context, PhaseId.INVOKE_APPLICATION);
        try {
            if (!skipPhase) {
                // NOTE - no tree walk is performed; this is a UIViewRoot-only operation
                broadcastEvents(context, PhaseId.INVOKE_APPLICATION);
            }
        } finally {
            clearFacesEvents(context);
            notifyAfter(context, PhaseId.INVOKE_APPLICATION);
        }
    }

    // clear out the events if we're skipping to render-response
    // or if there is a response complete signal.
    private void clearFacesEvents(FacesContext context) {
        if (context.getRenderResponse() || context.getResponseComplete()) {
            if (events != null) {
                for (List<FacesEvent> eventList : events) {
                    if (eventList != null) {
                        eventList.clear();
                    }
                }
                events = null;
            }
        }
    }

    /**
     * <p>
     * <span class="changed_modified_2_2">Generate</span> an identifier for a component. The identifier will be prefixed
     * with UNIQUE_ID_PREFIX, and will be unique within <span class="changed_added_2_2">the non-{@link NamingContainer}
     * child sub-trees of</span> this UIViewRoot.
     * </p>
     *
     * @return the identifier.
     */
    public String createUniqueId() {
        return createUniqueId(getFacesContext(), null);
    }

    /**
     * <p>
     * Generate an identifier for a component. The identifier will be prefixed with UNIQUE_ID_PREFIX, and will be unique
     * within this UIViewRoot. Optionally, a unique seed value can be supplied by component creators which should be
     * included in the generated unique id.
     * </p>
     *
     * @param context FacesContext
     * @param seed an optional seed value - e.g. based on the position of the component in the VDL-template
     * @return a unique-id in this component-container
     */
    @Override
    public String createUniqueId(FacesContext context, String seed) {
        if (seed != null) {
            return UIViewRoot.UNIQUE_ID_PREFIX + seed;
        }

        Integer i = (Integer) getStateHelper().get(PropertyKeys.lastId);
        int lastId = i != null ? i : 0;
        getStateHelper().put(PropertyKeys.lastId, ++lastId);

        return UIViewRoot.UNIQUE_ID_PREFIX + lastId;
    }

    /**
     * <p>
     * Return the <code>Locale</code> to be used in localizing the response being created for this view.
     * </p>
     * <p>
     * Algorithm:
     * </p>
     * <p>
     * If we have a <code>locale</code> ivar, return it. If we have a value expression for "locale", get its value. If the
     * value is <code>null</code>, return the result of calling
     * {@link jakarta.faces.application.ViewHandler#calculateLocale}. If the value is an instance of
     * <code>java.util.Locale</code> return it. If the value is a String, convert it to a <code>java.util.Locale</code> and
     * return it. If there is no value expression for "locale", return the result of calling
     * {@link jakarta.faces.application.ViewHandler#calculateLocale}.
     * </p>
     *
     * @return The current <code>Locale</code> obtained by executing the above algorithm.
     */
    public Locale getLocale() {
        Object result = getStateHelper().eval(PropertyKeys.locale);

        if (result != null) {
            Locale locale = null;
            if (result instanceof Locale) {
                locale = (Locale) result;
            } else if (result instanceof String) {
                locale = getLocaleFromString((String) result);
            }

            return locale;
        }

        FacesContext context = getFacesContext();
        return context.getApplication().getViewHandler().calculateLocale(context);
    }

    // W3C XML specification refers to IETF RFC 1766 for language code
    // structure, therefore the value for the xml:lang attribute should
    // be in the form of language or language-country or
    // language-country-variant.

    private static Locale getLocaleFromString(String localeStr) throws IllegalArgumentException {
        // length must be at least 2.
        if (localeStr == null || localeStr.length() < 2) {
            throw new IllegalArgumentException("Illegal locale String: " + localeStr);
        }

        Locale result = null;
        String lang = null;
        String country = null;
        String variant = null;
        char[] seps = { '-', '_' };
        int inputLength = localeStr.length();
        int i = 0;
        int j = 0;

        // to have a language, the length must be >= 2
        if (inputLength >= 2 && (i = indexOfSet(localeStr, seps, 0)) == -1) {
            // we have only Language, no country or variant
            if (2 != localeStr.length()) {
                throw new IllegalArgumentException("Illegal locale String: " + localeStr);
            }
            lang = localeStr.toLowerCase();
        }

        // we have a separator, it must be either '-' or '_'
        if (i != -1) {
            lang = localeStr.substring(0, i);
            // look for the country sep.
            // to have a country, the length must be >= 5
            if (inputLength >= 5 && -1 == (j = indexOfSet(localeStr, seps, i + 1))) {
                // no further separators, length must be 5
                if (inputLength != 5) {
                    throw new IllegalArgumentException("Illegal locale String: " + localeStr);
                }
                country = localeStr.substring(i + 1);
            }
            if (j != -1) {
                country = localeStr.substring(i + 1, j);
                // if we have enough separators for language, locale,
                // and variant, the length must be >= 8.
                if (inputLength >= 8) {
                    variant = localeStr.substring(j + 1);
                } else {
                    throw new IllegalArgumentException("Illegal locale String: " + localeStr);
                }
            }
        }
        if (variant != null && country != null && lang != null) {
            result = new Locale(lang, country, variant);
        } else if (lang != null && country != null) {
            result = new Locale(lang, country);
        } else if (lang != null) {
            result = new Locale(lang, "");
        }

        return result;
    }

    /**
     * @param str local string
     * @param set the substring
     * @param fromIndex starting index
     * @return starting at <code>fromIndex</code>, the index of the first occurrence of any substring from <code>set</code>
     * in <code>toSearch</code>, or -1 if no such match is found
     */
    private static int indexOfSet(String str, char[] set, int fromIndex) {
        int result = -1;
        for (int i = fromIndex, len = str.length(); i < len; i++) {
            for (int j = 0, innerLen = set.length; j < innerLen; j++) {
                if (str.charAt(i) == set[j]) {
                    result = i;
                    break;
                }
            }
            if (-1 != result) {
                break;
            }
        }

        return result;
    }

    /**
     * <p>
     * Set the <code>Locale</code> to be used in localizing the response being created for this view.
     * </p>
     *
     * @param locale The new localization Locale
     */
    public void setLocale(Locale locale) {
        getStateHelper().put(PropertyKeys.locale, locale);
        // Make sure to appraise the Jakarta Expression Language of this switch in Locale.
        FacesContext.getCurrentInstance().getELContext().setLocale(locale);
    }

    /**
     * <p class="changed_added_2_0">
     * This implementation simply calls through to {@link #getViewMap(boolean)}, passing <code>true</code> as the argument,
     * and returns the result.
     * </p>
     * <div class="changed_added_2_0"></div>
     *
     * @return the view map, or <code>null</code>.
     * @since 2.0
     */
    public Map<String, Object> getViewMap() {
        return getViewMap(true);
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2 changed_modified_2_3">Returns</span> a <code>Map</code> that acts as the interface
     * to the data store that is the "view scope", or, if this instance does not have such a <code>Map</code> and the
     * <code>create</code> argument is <code>true</code>, creates one and returns it. This map must be instantiated lazily
     * and cached for return from subsequent calls to this method on this <code>UIViewRoot</code> instance.
     * {@link jakarta.faces.application.Application#publishEvent} must be called, passing
     * <span class="changed_added_2_3">the current <code>FacesContext</code> as the first argument</span>,
     * {@link PostConstructViewMapEvent}<code>.class</code> as the second argument,
     * <span class="changed_added_2_3"><code>UIViewRoot.class</code> as the third argument</span> and this
     * <code>UIViewRoot</code> instance as the fourth argument. <span class="changed_added_2_3">It is necessary to pass the
     * <code>UIViewRoot.class</code> argument to account for cases when the <code>UIViewRoot</code> has been extended with a
     * custom class.</span>
     * </p>
     *
     * <p>
     * The returned <code>Map</code> must be implemented such that calling <code>clear()</code> on the <code>Map</code>
     * causes {@link jakarta.faces.application.Application#publishEvent} to be called, passing
     * {@link PreDestroyViewMapEvent}<code>.class</code> as the first argument and this <code>UIViewRoot</code> instance as
     * the second argument.
     * </p>
     *
     * <p class="changed_modified_2_0_rev_a">
     * Depending upon application configuration, objects stored in the view map may need to be <code>Serializable</code>. In
     * general, it is a good idea to ensure that any objects stored in the view map are <code>Serializable</code>.
     * </p>
     *
     * <p class="changed_added_2_2">
     * For reasons made clear in {@link jakarta.faces.view.ViewScoped}, this map must ultimately be stored in the session.
     * For this reason, a {@code true} value for the {@code create} argument will force the session to be created with a
     * call to {@link jakarta.faces.context.ExternalContext#getSession(boolean)}.
     *
     * </p>
     *
     * <p>
     * See {@link FacesContext#setViewRoot} for the specification of when the <code>clear()</code> method must be called.
     * </p>
     *
     * @param create <code>true</code> to create a new <code>Map</code> for this instance if necessary; <code>false</code>
     * to return <code>null</code> if there's no current <code>Map</code>.
     * @return the view map, or <code>null</code>.
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getViewMap(boolean create) {
        Map<String, Object> viewMap = (Map<String, Object>) getTransientStateHelper().getTransient("com.sun.faces.application.view.viewMap");

        if (create && viewMap == null) {
            viewMap = new ViewMap(getFacesContext().getApplication().getProjectStage());
            getTransientStateHelper().putTransient("com.sun.faces.application.view.viewMap", viewMap);
            getFacesContext().getApplication().publishEvent(getFacesContext(), PostConstructViewMapEvent.class, UIViewRoot.class, this);
        }

        return viewMap;
    }

    Map<Class<? extends SystemEvent>, List<SystemEventListener>> viewListeners;

    /**
     * <p class="changed_added_2_0">
     * Install the listener instance referenced by argument <code>listener</code> into the <code>UIViewRoot</code> as a
     * listener for events of type <code>systemEventClass</code>.
     * </p>
     *
     * <p>
     * Note that installed listeners are not maintained as part of the <code>UIViewRoot</code>'s state.
     * </p>
     *
     * @param systemEvent the <code>Class</code> of event for which <code>listener</code> must be fired.
     *
     * @param listener the implementation of {@link jakarta.faces.event.SystemEventListener} whose
     * {@link jakarta.faces.event.SystemEventListener#processEvent} method must be called when events of type
     * <code>systemEventClass</code> are fired.
     *
     * @throws NullPointerException if <code>systemEventClass</code> or <code>listener</code> are <code>null</code>.
     *
     * @since 2.0
     */
    public void subscribeToViewEvent(Class<? extends SystemEvent> systemEvent, SystemEventListener listener) {
        if (systemEvent == null) {
            throw new NullPointerException();
        }
        if (listener == null) {
            throw new NullPointerException();
        }

        if (viewListeners == null) {
            viewListeners = new HashMap<>(4, 1.0f);
        }

        viewListeners.computeIfAbsent(systemEvent, k -> new CopyOnWriteArrayList<>()).add(listener);

    }

    /**
     * <p class="changed_added_2_0">
     * Remove the listener instance referenced by argument <code>listener</code> from the <code>UIViewRoot</code> as a
     * listener for events of type <code>systemEventClass</code>.
     *
     * @param systemEvent the <code>Class</code> of event for which <code>listener</code> must be fired.
     * @param listener the implementation of {@link jakarta.faces.event.SystemEventListener} whose
     * {@link jakarta.faces.event.SystemEventListener#processEvent} method must be called when events of type
     * <code>systemEventClass</code> are fired.
     *
     * @throws NullPointerException if <code>systemEventClass</code> or <code>listener</code> are <code>null</code>.
     *
     * @since 2.0
     */
    public void unsubscribeFromViewEvent(Class<? extends SystemEvent> systemEvent, SystemEventListener listener) {
        if (systemEvent == null) {
            throw new NullPointerException();
        }
        if (listener == null) {
            throw new NullPointerException();
        }

        if (viewListeners != null) {
            List<SystemEventListener> listeners = viewListeners.get(systemEvent);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>SystemEventListener</code> instances registered on this <code>UIComponent</code> instance that are
     * interested in events of type <code>eventClass</code>.
     * </p>
     *
     * @param systemEvent the <code>Class</code> of event for which the listeners must be returned.
     *
     * @return Collection of view listeners.
     *
     * @throws NullPointerException if argument <code>systemEvent</code> is <code>null</code>.
     *
     * @since 2.0
     *
     */
    public List<SystemEventListener> getViewListenersForEventClass(Class<? extends SystemEvent> systemEvent) {
        if (systemEvent == null) {
            throw new NullPointerException();
        }
        if (viewListeners != null) {
            return viewListeners.get(systemEvent);
        }

        return null;
    }

    private void encodeViewParameters(FacesContext context) {
        ViewDeclarationLanguage vdl = context.getApplication().getViewHandler().getViewDeclarationLanguage(context, getViewId());
        if (vdl == null) {
            return;
        }

        ViewMetadata metadata = vdl.getViewMetadata(context, getViewId());
        if (metadata != null) { // perhaps it's not supported
            Collection<UIViewParameter> params = ViewMetadata.getViewParameters(this);
            if (params.isEmpty()) {
                return;
            }

            try {
                for (UIViewParameter param : params) {
                    param.encodeAll(context);
                }
            } catch (IOException e) {
                // IOException is forced by contract and is not expected to be thrown in this case
                throw new RuntimeException("Unexpected IOException", e);
            }
        }
    }

    /**
     *
     * <p class="changed_added_2_2">
     * Restore ViewScope state. This is needed to allow the use of view scoped beans for EL-expressions in the template from
     * which the component tree is built. For example: <code>&lt;ui:include
     * src="#{viewScopedBean.includeFileName}"/&gt;</code>.
     * </p>
     *
     *
     * @param context current FacesContext.
     * @param state the state object.
     */
    public void restoreViewScopeState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }

        values = (Object[]) state;
        super.restoreState(context, values[0]);
    }

    // END TENATIVE

    // ----------------------------------------------------- StateHolder Methods

    private Object[] values;

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        String viewMapId = (String) getTransientStateHelper().getTransient("com.sun.faces.application.view.viewMapId");
        Object superState = super.saveState(context);

        if (superState != null || viewMapId != null) {
            values = new Object[] { superState, viewMapId };
        }

        return values;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }

        values = (Object[]) state;

        if (!context.getAttributes().containsKey("com.sun.faces.application.view.restoreViewScopeOnly")) {
            super.restoreState(context, values[0]);
        }

        String viewMapId = (String) values[1];

        getTransientStateHelper().putTransient("com.sun.faces.application.view.viewMapId", viewMapId);

        Map<String, Object> viewMaps = (Map<String, Object>) context.getExternalContext().getSessionMap().get("com.sun.faces.application.view.activeViewMaps");

        if (viewMaps != null) {
            Map<String, Object> viewMap = (Map<String, Object>) viewMaps.get(viewMapId);
            getTransientStateHelper().putTransient("com.sun.faces.application.view.viewMap", viewMap);
        }
    }

    // --------------------------------------------------------- Private Methods

    private static String getIdentifier(String target) {
        // check map
        String id = LOCATION_IDENTIFIER_MAP.computeIfAbsent(target, t -> LOCATION_IDENTIFIER_PREFIX + t);

        return id;
    }

    private List<UIComponent> getComponentResources(FacesContext context, String target, boolean create) {
        String location = getIdentifier(target);
        UIComponent facet = getFacet(location);
        if (facet == null && create) {
            // Using an implementation specific component type to prevent
            // component resources being rendered at the incorrect time if
            // a caller calls UIViewRoot.encodeAll().
            facet = context.getApplication().createComponent("jakarta.faces.ComponentResourceContainer");
            facet.setId(location);
            getFacets().put(location, facet);
        }

        return facet != null ? facet.getChildren() : null;
    }

    private static final class ViewMap extends HashMap<String, Object> {

        private static final long serialVersionUID = -1L;

        private final ProjectStage stage;

        // -------------------------------------------------------- Constructors

        ViewMap(ProjectStage stage) {
            this.stage = stage;
        }

        // ---------------------------------------------------- Methods from Map

        @Override
        public void clear() {
            FacesContext context = FacesContext.getCurrentInstance();
            context.getApplication().publishEvent(context, PreDestroyViewMapEvent.class, UIViewRoot.class, context.getViewRoot());
            super.clear();
        }

        @Override
        public Object put(String key, Object value) {
            if (value != null && ProjectStage.Development.equals(stage) && !(value instanceof Serializable)) {
                LOGGER.log(WARNING, "warning.component.uiviewroot_non_serializable_attribute_viewmap", new Object[] { key, value.getClass().getName() });
            }
            return super.put(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            for (Map.Entry<? extends String, ?> entry : m.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                put(k, v);
            }
        }

    } // END ViewMap

}
