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

import java.util.Map;

import jakarta.el.MethodExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextWrapper;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.view.ViewMetadata;

/**
 *
 * <p class="changed_added_2_2">
 * <strong class="changed_modified_2_3">UIViewAction</strong> represents a method invocation that occurs during the
 * request processing lifecycle, usually in response to an initial request, as opposed to a postback.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * The {@link jakarta.faces.view.ViewDeclarationLanguage} implementation must cause an instance of this component to be
 * placed in the view for each occurrence of an <code>&lt;f:viewAction
 * /&gt;</code> element placed inside of an <code>&lt;f:metadata
 * /&gt;</code> element. The user must place <code>&lt;f:metadata
 * /&gt;</code> as a direct child of the <code>UIViewRoot</code>.
 * </p>
 *
 * <p>
 * Because this class implements {@link ActionSource2}, any actions that one would normally take on a component that
 * implements <code>ActionSource2</code>, such as {@link UICommand}, are valid for instances of this class. Instances of
 * this class participate in the regular Jakarta Faces lifecycle, including on Ajax requests.
 * </p>
 *
 * <p>
 * The purpose of this component is to provide a light-weight front-controller solution for executing code upon the
 * loading of a Jakarta Faces view to support the integration of system services, content retrieval, view
 * management, and navigation. This functionality is especially useful for non-faces (initial) requests.
 * </p>
 *
 * <p>
 * The most common use case for this component is to take actions necessary for a particular view, often with the help
 * of one or more {@link UIViewParameter}s.
 * </p>
 *
 * <p>
 * The {@link NavigationHandler} is consulted after the action is invoked to carry out the navigation case that matches
 * the action signature and outcome. If a navigation case is matched that causes the new viewId to be different from the
 * current viewId, the runtime must force a redirect to that matched navigation case with different viewId, regardless
 * of whether or not the matched navigation case with different viewId called for a redirect.
 * <span class="changed_added_2_3">If the navigation will result in a flow transition, the appropriate metadata must be
 * included in the query string for the redirect.
 * See section 7.4.2 "Default NavigationHandler Algorithm" of the Jakarta Faces Specification Document, for the
 * specification of how to handle {@code &lt;redirect /&gt;} cases.</span>
 * </p>
 *
 * <p>
 * It's important to note that the full component tree is not built before the UIViewAction components are processed on
 * an non-faces (initial) request. Rather, the component tree only contains the {@link ViewMetadata}, an important part
 * of the optimization of this component and what sets it apart from a {@link PreRenderViewEvent} listener.
 * </p>
 *
 * </div>
 *
 * @since 2.2
 */
public class UIViewAction extends UIComponentBase implements ActionSource2 {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.ViewAction";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.ViewAction";

    private static final String UIVIEWACTION_BROADCAST = "jakarta.faces.ViewAction.broadcast";

    private static final String UIVIEWACTION_EVENT_COUNT = "jakarta.faces.ViewAction.eventCount";

    /**
     * Properties that are tracked by state saving.
     */
    enum PropertyKeys {

        onPostback, actionExpression, immediate, phase, renderedAttr("if");

        private String name;

        PropertyKeys() {
        }

        PropertyKeys(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name != null ? name : super.toString();
        }
    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIViewAction} instance with default property values.
     * </p>
     */
    public UIViewAction() {
        super();
        setRendererType(null);
    }

    // -------------------------------------------------------------- Properties
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    private void incrementEventCount(FacesContext context) {
        Map<Object, Object> attrs = context.getAttributes();
        Integer count = (Integer) attrs.get(UIVIEWACTION_EVENT_COUNT);
        if (null == count) {
            attrs.put(UIVIEWACTION_EVENT_COUNT, 1);
        } else {
            attrs.put(UIVIEWACTION_EVENT_COUNT, count + 1);
        }
    }

    private boolean decrementEventCountAndReturnTrueIfZero(FacesContext context) {
        boolean result = true;
        Map<Object, Object> attrs = context.getAttributes();
        Integer count = (Integer) attrs.get(UIVIEWACTION_EVENT_COUNT);
        if (null != count) {
            count = count - 1;
            if (count < 1) {
                attrs.remove(UIVIEWACTION_EVENT_COUNT);
                result = true;
            } else {
                attrs.put(UIVIEWACTION_EVENT_COUNT, count);
                result = false;
            }
        }

        return result;
    }

    /**
     * <p class="changed_added_2_2">
     * If the value of the component's <code>immediate</code> attribute is <code>true</code>, the action will be invoked
     * during the <em>Apply Request Values</em> Jakarta Faces lifecycle phase. Otherwise, the action will be invoked
     * during the <em>Invoke Application</em> phase, the default behavior. The phase can be set explicitly in the
     * <code>phase</code> attribute, which takes precedence over the <code>immediate</code> attribute.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public boolean isImmediate() {
        return (Boolean) getStateHelper().eval(PropertyKeys.immediate, false);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.2
     */
    @Override
    public void setImmediate(final boolean immediate) {
        getStateHelper().put(PropertyKeys.immediate, immediate);
    }

    /**
     * <p class="changed_added_2_2">
     * Returns the name of the lifecycle phase in which the action is to be queued.
     * </p>
     *
     * @return the phase (as string).
     * @since 2.2
     */
    public String getPhase() {
        PhaseId myPhaseId = getPhaseId();
        String result = null;
        if (null != myPhaseId) {
            result = myPhaseId.getName();
        }
        return result;
    }

    /**
     * <p class="changed_added_2_2">
     * Attempt to set the lifecycle phase in which this instance will queue its {@link ActionEvent}. Pass the argument
     * <code>phase</code> to {@link PhaseId#phaseIdValueOf}. If the result is not one of the following values,
     * <code>FacesException</code> must be thrown.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * {@link PhaseId#APPLY_REQUEST_VALUES}
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link PhaseId#PROCESS_VALIDATIONS}
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link PhaseId#UPDATE_MODEL_VALUES}
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link PhaseId#INVOKE_APPLICATION}
     * </p>
     * </li>
     *
     * </ul>
     *
     * <p>
     * If set, this value takes precedence over the immediate flag.
     * </p>
     *
     * @param phase the phase id (as string value).
     * @since 2.2
     */

    public void setPhase(final String phase) {
        PhaseId myPhaseId = PhaseId.phaseIdValueOf(phase);
        if (PhaseId.ANY_PHASE.equals(myPhaseId) || PhaseId.RESTORE_VIEW.equals(myPhaseId) || PhaseId.RENDER_RESPONSE.equals(myPhaseId)) {
            throw new FacesException("View actions cannot be executed in specified phase: [" + myPhaseId + "]");
        }
        getStateHelper().put(PropertyKeys.phase, myPhaseId.getName());
    }

    private void setIsProcessingUIViewActionBroadcast(FacesContext context, boolean value) {
        Map<Object, Object> attrs = context.getAttributes();

        if (value) {
            attrs.put(UIVIEWACTION_BROADCAST, Boolean.TRUE);
        } else {
            attrs.remove(UIVIEWACTION_BROADCAST);
        }
    }

    /**
     * <p class="changed_added_2_2">
     * Returns <code>true</code> if the current request processing lifecycle is in the midst of processing the broadcast of
     * an event queued during a call to {@link #decode}. The implementation of {@link #broadcast} is responsible for
     * ensuring that calls to this method accurately reflect this fact.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return <code>true</code> is currently processing broadcast, <code>false</code> otherwise.
     * @since 2.2
     *
     */

    public static boolean isProcessingBroadcast(FacesContext context) {
        boolean result = context.getAttributes().containsKey(UIVIEWACTION_BROADCAST);
        return result;
    }

    private PhaseId getPhaseId() {
        PhaseId myPhaseId = null;
        String phaseIdString = (String) getStateHelper().eval(PropertyKeys.phase);
        if (phaseIdString != null) {
            myPhaseId = PhaseId.phaseIdValueOf(phaseIdString);
        }
        return myPhaseId;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.2
     */
    @Override
    public void addActionListener(final ActionListener listener) {
        addFacesListener(listener);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.2
     */
    @Override
    public ActionListener[] getActionListeners() {
        ActionListener[] al = (ActionListener[]) getFacesListeners(ActionListener.class);
        return al;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.2
     */
    @Override
    public void removeActionListener(final ActionListener listener) {
        removeFacesListener(listener);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.2
     */
    @Override
    public MethodExpression getActionExpression() {
        return (MethodExpression) getStateHelper().get(PropertyKeys.actionExpression);
    }

    /**
     * {@inheritDoc}
     *
     * @param actionExpression the action expression.
     * @since 2.2
     */
    @Override
    public void setActionExpression(final MethodExpression actionExpression) {
        getStateHelper().put(PropertyKeys.actionExpression, actionExpression);
    }

    /**
     * <p class="changed_added_2_2">
     * If <code>true</code> this component will operate on postback.
     * </p>
     *
     * @return <code>true</code> if operating upon postback, <code>false</code> otherwise.
     * @since 2.2
     */
    public boolean isOnPostback() {
        return (Boolean) getStateHelper().eval(PropertyKeys.onPostback, false);
    }

    /**
     * <p class="changed_added_2_2">
     * Controls whether or not this component operates on postback.
     * </p>
     *
     * @param onPostback the onPostback flag.
     * @since 2.2
     */
    public void setOnPostback(final boolean onPostback) {
        getStateHelper().put(PropertyKeys.onPostback, onPostback);
    }

    /**
     * <p class="changed_added_2_2">
     * Return <code>true</code> if this component should take the actions specified in the {@link #decode} method.
     * </p>
     *
     * @return <code>true</code> if it should be rendered, <code>false</code> otherwise.
     * @since 2.2
     */

    @Override
    public boolean isRendered() {
        return (Boolean) getStateHelper().eval(PropertyKeys.renderedAttr, true);
    }

    /**
     * <p class="changed_added_2_2">
     * Sets the <code>if</code> property of this component.
     * </p>
     *
     * @param condition the new value of the property.
     *
     * @since 2.2
     */
    @Override
    public void setRendered(final boolean condition) {
        getStateHelper().put(PropertyKeys.renderedAttr, condition);
    }

    // ----------------------------------------------------- UIComponent Methods

    /**
     * <p class="changed_added_2_2">
     * Enable the method invocation specified by this component instance to return a value that performs navigation, similar
     * in spirit to {@link UICommand#broadcast}.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * Take no action and return immediately if any of the following conditions are true.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * The response has already been marked as complete.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * The current <code>UIViewRoot</code> is different from the event's source's <code>UIViewRoot</code>.
     * </p>
     * </li>
     *
     * </ul>
     *
     * <p>
     * Save a local reference to the viewId of the current <code>UIViewRoot</code>. For discussion, let this reference be
     * <em>viewIdBeforeAction</em>.
     * </p>
     *
     * <p>
     * Obtain the {@link ActionListener} from the {@link jakarta.faces.application.Application}. Wrap the current
     * {@link FacesContext} in an implementation of {@link jakarta.faces.context.FacesContextWrapper} that overrides the
     * {@link FacesContext#renderResponse} method such that it takes no action. Set the current <code>FacesContext</code> to
     * be the <code>FacesContextWrapper</code> instance. Make it so a call to {@link #isProcessingBroadcast} on the current
     * FacesContext will return <code>true</code>. This is necessary because the
     * {@link jakarta.faces.application.NavigationHandler} will call this method to determine if the navigation is happening
     * as the result of a <code>UIViewAction</code>. Invoke {@link ActionListener#processAction}. In a <code>finally</code>
     * block, restore the original <code>FacesContext</code>, make it so a call to {@link #isProcessingBroadcast} on the
     * current context will return <code>false</code> and discard the wrapper.
     * </p>
     *
     * <p>
     * If the response has been marked as complete during the invocation of <code>processAction()</code>, take no further
     * action and return. Otherwise, compare <em>viewIdBeforeAction</em> with the viewId of the <code>UIViewRoot</code> on
     * the <code>FacesContext</code> after the invocation of <code>processAction()</code>. If the two viewIds are the same
     * and no more <code>UIViewAction</code> events have been queued by a call to {@link #decode}, call
     * {@link FacesContext#renderResponse} and return. It is possible to detect the case where no more
     * <code>UIViewAction</code> events have been queued because the number of such events queued has been noted in the
     * specification for {@link #decode}. Otherwise, execute the lifecycle on the new <code>UIViewRoot</code>.
     * </p>
     *
     * </div>
     *
     * @param event {@link FacesEvent} to be broadcast
     *
     * @throws AbortProcessingException Signal the Jakarta Faces implementation that no further processing on the
     * current event should be performed
     * @throws IllegalArgumentException if the implementation class of this {@link FacesEvent} is not supported by this
     * component
     * @throws NullPointerException if <code>event</code> is <code>null</code>
     *
     * @since 2.2
     */
    @Override
    public void broadcast(final FacesEvent event) throws AbortProcessingException {
        super.broadcast(event);

        FacesContext context = event.getFacesContext();
        if (!(event instanceof ActionEvent)) {
            throw new IllegalArgumentException();
        }

        // OPEN QUESTION: should we consider a navigation to the same view as a
        // no-op navigation?

        // only proceed if the response has not been marked complete and
        // navigation to another view has not occurred
        if (!context.getResponseComplete() && context.getViewRoot() == getViewRootOf(event)) {
            ActionListener listener = context.getApplication().getActionListener();
            if (listener != null) {
                boolean hasMoreViewActionEvents = false;
                UIViewRoot viewRootBefore = context.getViewRoot();
                assert null != viewRootBefore;
                InstrumentedFacesContext instrumentedContext = null;
                try {
                    instrumentedContext = new InstrumentedFacesContext(context);
                    setIsProcessingUIViewActionBroadcast(context, true);
                    // defer the call to renderResponse() that happens in
                    // ActionListener#processAction(ActionEvent)
                    instrumentedContext.disableRenderResponseControl().set();
                    listener.processAction((ActionEvent) event);
                    hasMoreViewActionEvents = !decrementEventCountAndReturnTrueIfZero(context);
                } finally {
                    setIsProcessingUIViewActionBroadcast(context, false);
                    if (null != instrumentedContext) {
                        instrumentedContext.restore();
                    }
                }
                // if the response is marked complete, the story is over
                if (!context.getResponseComplete()) {
                    UIViewRoot viewRootAfter = context.getViewRoot();
                    assert null != viewRootAfter;

                    // if the view id changed as a result of navigation, then
                    // execute the Jakarta Faces lifecycle for the new view
                    // id
                    String viewIdBefore = viewRootBefore.getViewId();
                    String viewIdAfter = viewRootAfter.getViewId();
                    assert null != viewIdBefore && null != viewIdAfter;
                    boolean viewIdsSame = viewIdBefore.equals(viewIdAfter);

                    if (viewIdsSame && !hasMoreViewActionEvents) {
                        // apply the deferred call (relevant when immediate is true)
                        context.renderResponse();
                    }
                }
            }
        }
    }

    /**
     * <p class="changed_added_2_2">
     * Override behavior from the superclass to queue an {@link ActionEvent} that may result in the invocation of the
     * <code>action</code> or any <code>actionListener</code>s that may be associated with this instance.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * Take no action if any of the following conditions are true:
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * The current request is a postback and the instance has been configured to not operate on postback. See
     * {@link #isOnPostback}.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * The condition stated in the <code>if</code> property evaluates to <code>false</code>. See {@link #isRendered}
     * </p>
     * .</li>
     *
     * </ul>
     *
     * <p>
     * Instantiate an {@link ActionEvent}, passing this component instance as the source. Set the <code>phaseId</code>
     * property of the <code>ActionEvent</code> as follows.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * If this component instance has been configured with a specific lifecycle phase with a call to {@link #setPhase} use
     * that as the <code>phaseId</code>
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * If the value of the <code>immediate</code> property is true, use {@link PhaseId#APPLY_REQUEST_VALUES}.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Otherwise, use {@link PhaseId#INVOKE_APPLICATION}.
     * </p>
     * </li>
     *
     * </ul>
     *
     * <p>
     * Queue the event with a call to {@link #queueEvent}. Keep track of the number of events that are queued in this way on
     * this run through the lifecycle. This information is necessary during processing in {@link #broadcast}.
     * </p>
     *
     * </div>
     *
     * @since 2.2
     *
     */
    @Override
    public void decode(final FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (context.isPostback() && !isOnPostback() || !isRendered()) {
            return;
        }

        ActionEvent e = new ActionEvent(context, this);
        PhaseId phaseId = getPhaseId();
        if (phaseId != null) {
            e.setPhaseId(phaseId);
        } else if (isImmediate()) {
            e.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        } else {
            e.setPhaseId(PhaseId.INVOKE_APPLICATION);
        }
        incrementEventCount(context);
        queueEvent(e);
    }

    private UIViewRoot getViewRootOf(final FacesEvent e) {
        UIComponent c = e.getComponent();
        do {
            if (c instanceof UIViewRoot) {
                return (UIViewRoot) c;
            }
            c = c.getParent();
        } while (c != null);
        return null;
    }

    /**
     * A FacesContext delegator that gives us the necessary controls over the FacesContext to allow the execution of the
     * lifecycle to accomodate the UIViewAction sequence.
     */
    private static class InstrumentedFacesContext extends FacesContextWrapper {

        private boolean viewRootCleared = false;
        private boolean renderedResponseControlDisabled = false;
        private Boolean postback = null;

        public InstrumentedFacesContext(final FacesContext wrapped) {
            super(wrapped);
        }

        @Override
        public UIViewRoot getViewRoot() {
            if (viewRootCleared) {
                return null;
            }

            return super.getViewRoot();
        }

        @Override
        public void setViewRoot(final UIViewRoot viewRoot) {
            viewRootCleared = false;
            super.setViewRoot(viewRoot);
        }

        @Override
        public boolean isPostback() {
            return postback == null ? super.isPostback() : postback;
        }

        @Override
        public void renderResponse() {
            if (!renderedResponseControlDisabled) {
                super.renderResponse();
            }
        }

        /**
         * Make it look like we have dispatched a request using the include method.
         */
        public InstrumentedFacesContext pushViewIntoRequestMap() {
            getExternalContext().getRequestMap().put("jakarta.servlet.include.servlet_path", super.getViewRoot().getViewId());
            return this;
        }

        public InstrumentedFacesContext clearPostback() {
            postback = false;
            return this;
        }

        public InstrumentedFacesContext clearViewRoot() {
            viewRootCleared = true;
            return this;
        }

        public InstrumentedFacesContext disableRenderResponseControl() {
            renderedResponseControlDisabled = true;
            return this;
        }

        public void set() {
            setCurrentInstance(this);
        }

        public void restore() {
            setCurrentInstance(getWrapped());
        }
    }
}
