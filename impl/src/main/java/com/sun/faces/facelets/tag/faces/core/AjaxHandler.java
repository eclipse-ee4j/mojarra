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

package com.sun.faces.facelets.tag.faces.core;

import static jakarta.faces.component.UINamingContainer.getSeparatorChar;
import static java.util.Arrays.stream;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.sun.faces.component.behavior.AjaxBehaviors;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.composite.BehaviorHolderWrapper;
import com.sun.faces.facelets.tag.composite.RetargetedAjaxBehavior;
import com.sun.faces.facelets.tag.faces.CompositeComponentTagHandler;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.AjaxBehaviorListener;
import jakarta.faces.view.AttachedObjectTarget;
import jakarta.faces.view.BehaviorHolderAttachedObjectHandler;
import jakarta.faces.view.BehaviorHolderAttachedObjectTarget;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.CompositeFaceletHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2">Enable</span> one or more components in the view to perform Ajax operations. This
 * tag handler must create an instance of {@link jakarta.faces.component.behavior.AjaxBehavior} using the tag attribute
 * values. <div class="changed_modified_2_2">The <code>events</code> attribute for this tag that can be a
 * <code>ValueExpression</code> must be evaluated at tag execution time since the event name is used in the process of
 * <code>Behavior</code> creation.</div> If this tag is nested within a single {@link ClientBehaviorHolder} component:
 * 
 * <ul>
 * <li>If the <code>events</code> attribute value is not specified, obtain the default event name by calling
 * {@link ClientBehaviorHolder#getDefaultEventName}. If that returns <code>null</code> throw an
 * <code>exception</code>.</li>
 * <li>If the <code>events</code> attribute value is specified, ensure it that it exists in the <code>Collection</code>
 * returned from a call to {@link ClientBehaviorHolder#getEventNames} and throw an <code>exception</code> if it doesn't
 * exist.</li>
 * <li>Add the {@link AjaxBehavior} instance to the {@link ClientBehaviorHolder} component by calling
 * {@link ClientBehaviorHolder#addClientBehavior} passing <code>event</code> and the {@link AjaxBehavior} instance.</li>
 * </ul>
 * <p>
 * Check for the existence of the Ajax resource by calling <code>UIViewRoot.getComponentResources()</code>. If the Ajax
 * resource does not exist, create a <code>UIOutput</code> component instance and set the renderer type to
 * <code>jakarta.faces.resource.Script</code>. Set the the following attributes in the component's attribute
 * <code>Map</code>: <code>library</code> with the value {@value ResourceHandler#FACES_SCRIPT_LIBRARY_NAME} and
 * <code>name</code> with the value {@value ResourceHandler#FACES_SCRIPT_RESOURCE_NAME}. Install the component resource
 * using <code>UIViewRoot.addComponentResource()</code> and specifying <code>head</code> as the <code>target</code>
 * argument.
 * <p>
 * If this tag has component children, add the {@link AjaxBehavior} to {@link AjaxBehaviors} by calling
 * {@link AjaxBehaviors#pushBehavior}. As subsequent child components that implement the {@link ClientBehaviorHolder}
 * interface are evaluated this {@link AjaxBehavior} instance must be added as a behavior to the component.
 *
 * @version $Id: AjaxHandler.java 5369 2008-09-08 19:53:45Z rogerk $
 */
public final class AjaxHandler extends TagHandlerImpl implements BehaviorHolderAttachedObjectHandler {

    private final TagAttribute event;
    private final TagAttribute execute;
    private final TagAttribute render;
    private final TagAttribute onevent;
    private final TagAttribute onerror;
    private final TagAttribute disabled;
    private final TagAttribute immediate;
    private final TagAttribute resetValues;
    private final TagAttribute listener;
    private final TagAttribute delay;

    private final boolean wrapping;

    /**
     * @param config
     */
    public AjaxHandler(TagConfig config) {
        super(config);
        event = getAttribute("event");
        execute = getAttribute("execute");
        render = getAttribute("render");
        onevent = getAttribute("onevent");
        onerror = getAttribute("onerror");
        disabled = getAttribute("disabled");
        immediate = getAttribute("immediate");
        resetValues = getAttribute("resetValues");
        listener = getAttribute("listener");
        delay = getAttribute("delay");

        wrapping = isWrapping();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        String eventName = getEventName();

        if (wrapping) {
            applyWrapping(ctx, parent, eventName);
        } else {
            applyNested(ctx, parent, eventName);
        }
    }

    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent) {
        FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        applyAttachedObject(ctx, parent, getEventName());
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.AttachedObjectHandler#getFor()
     */
    @Override
    public String getFor() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.view.BehaviorHolderAttachedObjectHandler#getEventName()
     */
    @Override
    public String getEventName() {
        FacesContext context = FacesContext.getCurrentInstance();
        FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        return event != null ? event.getValue(ctx) : null;
    }

    // Tests whether the <f:ajax> is wrapping other tags.
    private boolean isWrapping() {

        // Would be nice if there was some easy way to determine whether
        // we are a leaf handler. However, even leaf handlers have a
        // non-null nextHandler - the CompilationUnit.LEAF instance.
        // We assume that if we've got a TagHandler or CompositeFaceletHandler
        // as our nextHandler, we are not a leaf.
        return nextHandler instanceof TagHandler || nextHandler instanceof CompositeFaceletHandler;
    }

    // Applies a wrapping AjaxHandler by pushing/popping the AjaxBehavior
    // to the AjaxBeahviors object.
    private void applyWrapping(FaceletContext ctx, UIComponent parent, String eventName) throws IOException {

        // In the wrapping case, we assume that some wrapped component
        // is going to be Ajax enabled and install the Ajax resource.
        RenderKitUtils.installFacesJsIfNecessary(ctx.getFacesContext());

        AjaxBehavior ajaxBehavior = createAjaxBehavior(ctx, parent, eventName);

        // We leverage AjaxBehaviors to support the wrapping case. We
        // push/pop the AjaxBehavior instance on AjaxBehaviors so that
        // child tags will have access to it.
        FacesContext context = ctx.getFacesContext();
        AjaxBehaviors ajaxBehaviors = AjaxBehaviors.getAjaxBehaviors(context, true);
        ajaxBehaviors.pushBehavior(context, ajaxBehavior, eventName);

        nextHandler.apply(ctx, parent);

        ajaxBehaviors.popBehavior();
    }

    // Applies a nested AjaxHandler by adding the AjaxBehavior to the
    // parent component.
    private void applyNested(FaceletContext ctx, UIComponent parent, String eventName) {

        if (!ComponentHandler.isNew(parent)) {
            return;
        }

        // Composite component case
        if (UIComponent.isCompositeComponent(parent)) {
            // Check composite component event name:
            boolean tagApplied = false;
            if (parent instanceof ClientBehaviorHolder) {
                applyAttachedObject(ctx, parent, eventName); // error here will propagate up
                tagApplied = true;
            }
            BeanInfo componentBeanInfo = (BeanInfo) parent.getAttributes().get(UIComponent.BEANINFO_KEY);
            if (null == componentBeanInfo) {
                throw new TagException(tag, "Error: enclosing composite component does not have BeanInfo attribute");
            }
            BeanDescriptor componentDescriptor = componentBeanInfo.getBeanDescriptor();
            if (null == componentDescriptor) {
                throw new TagException(tag, "Error: enclosing composite component BeanInfo does not have BeanDescriptor");
            }
            List<AttachedObjectTarget> targetList = (List<AttachedObjectTarget>) componentDescriptor.getValue(AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY);
            if (null == targetList && !tagApplied) {
                throw new TagException(tag, "Error: enclosing composite component does not support behavior events");
            }
            boolean supportedEvent = false;
            for (AttachedObjectTarget target : targetList) {
                if (target instanceof BehaviorHolderAttachedObjectTarget) {
                    BehaviorHolderAttachedObjectTarget behaviorTarget = (BehaviorHolderAttachedObjectTarget) target;
                    if (null != eventName && eventName.equals(behaviorTarget.getName()) || null == eventName && behaviorTarget.isDefaultEvent()) {
                        supportedEvent = true;
                        break;
                    }
                }
            }
            if (supportedEvent) {
                CompositeComponentTagHandler.getAttachedObjectHandlers(parent).add(this);
            } else {
                if (!tagApplied) {
                    throw new TagException(tag, "Error: enclosing composite component does not support event " + eventName);
                }
            }
        } else if (parent instanceof ClientBehaviorHolder) {
            applyAttachedObject(ctx, parent, eventName);
        } else {
            throw new TagException(tag, "Unable to attach <f:ajax> to non-ClientBehaviorHolder parent");
        }

    }

    /**
     * <p class="changed_added_2_0">
     * </p>
     *
     * @param ctx
     * @param parent
     * @param eventName
     */
    private void applyAttachedObject(FaceletContext ctx, UIComponent parent, String eventName) {
        ClientBehaviorHolder bHolder = (ClientBehaviorHolder) parent;

        if (null == eventName) {
            eventName = bHolder.getDefaultEventName();
            if (null == eventName) {
                throw new TagException(tag, "Event attribute could not be determined: " + eventName);
            }
        } else {
            Collection<String> eventNames = bHolder.getEventNames();
            if (!eventNames.contains(eventName)) {
                throw new TagException(tag, getUnsupportedEventMessage(eventName, eventNames, parent));
            }
        }

        AjaxBehavior ajaxBehavior = createAjaxBehavior(ctx, parent, eventName);
        bHolder.addClientBehavior(eventName, ajaxBehavior);
        RenderKitUtils.installFacesJsIfNecessary(ctx.getFacesContext());
    }

    // Construct our AjaxBehavior from tag parameters.
    private AjaxBehavior createAjaxBehavior(FaceletContext ctx, UIComponent parent, String eventName) {
        Application application = ctx.getFacesContext().getApplication();
        AjaxBehavior behavior = (AjaxBehavior) application.createBehavior(AjaxBehavior.BEHAVIOR_ID);

        setBehaviorAttribute(ctx, behavior, onevent, String.class);
        setBehaviorAttribute(ctx, behavior, onerror, String.class);
        setBehaviorAttribute(ctx, behavior, disabled, Boolean.class);
        setBehaviorAttribute(ctx, behavior, immediate, Boolean.class);
        setBehaviorAttribute(ctx, behavior, resetValues, Boolean.class);
        setBehaviorAttribute(ctx, behavior, execute, Object.class);
        setBehaviorAttribute(ctx, behavior, render, Object.class);
        setBehaviorAttribute(ctx, behavior, delay, String.class);

        if (parent instanceof BehaviorHolderWrapper) {
            ValueExpression targets = ((BehaviorHolderWrapper) parent).getTargets();
            
            if (targets != null) {
                String targetClientIds = (String) targets.getValue(ctx);
                
                if (targetClientIds != null) {
                    Collection<String> executeClientIds = new ArrayList<>(behavior.getExecute());

                    if (executeClientIds.isEmpty() || executeClientIds.contains("@this")) {
                        String separatorChar = String.valueOf(getSeparatorChar(ctx.getFacesContext()));
                        executeClientIds.remove("@this");
                        stream(targetClientIds.trim().split(" +")).map(id -> "@this" + separatorChar + id).forEach(executeClientIds::add);
                        behavior.setExecute(executeClientIds);
                        behavior = new RetargetedAjaxBehavior(behavior);
                    }
                }
            }
        }

        if (null != listener) {
            behavior.addAjaxBehaviorListener(
                    new AjaxBehaviorListenerImpl(listener.getMethodExpression(ctx, Object.class, new Class<?>[] { AjaxBehaviorEvent.class }),
                            listener.getMethodExpression(ctx, Object.class, new Class<?>[] {})));
        }

        return behavior;
    }

    // Sets the value from the TagAttribute on the behavior
    private void setBehaviorAttribute(FaceletContext ctx, AjaxBehavior behavior, TagAttribute attr, Class<?> type) {

        if (attr != null) {
            behavior.setValueExpression(attr.getLocalName(), attr.getValueExpression(ctx, type));
        }
    }

    // Returns an error message for the case where the <f:ajax> event
    // attribute specified an unknown/unsupported event.
    private String getUnsupportedEventMessage(String eventName, Collection<String> eventNames, UIComponent parent) {
        StringBuilder builder = new StringBuilder(100);
        builder.append("'");
        builder.append(eventName);
        builder.append("' is not a supported event for ");
        builder.append(parent.getClass().getSimpleName());
        builder.append(".  Please specify one of these supported event names: ");

        // Might as well sort the event names for a cleaner error message.
        Collection<String> sortedEventNames = new TreeSet<>(eventNames);
        Iterator<String> iter = sortedEventNames.iterator();

        boolean hasNext = iter.hasNext();
        while (hasNext) {
            builder.append(iter.next());

            hasNext = iter.hasNext();

            if (hasNext) {
                builder.append(", ");
            }
        }

        builder.append(".");

        return builder.toString();
    }
}

class AjaxBehaviorListenerImpl implements AjaxBehaviorListener, Serializable {
    private static final long serialVersionUID = -6056525197409773897L;

    private MethodExpression oneArgListener;
    private MethodExpression noArgListener;

    // Necessary for state saving
    public AjaxBehaviorListenerImpl() {
    }

    public AjaxBehaviorListenerImpl(MethodExpression oneArg, MethodExpression noArg) {
        oneArgListener = oneArg;
        noArgListener = noArg;
    }

    @Override
    public void processAjaxBehavior(AjaxBehaviorEvent event) throws AbortProcessingException {
        final ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        try {
            noArgListener.invoke(elContext, new Object[] {});
        } catch (MethodNotFoundException | IllegalArgumentException mnfe) {
            // Attempt to call public void method(AjaxBehaviorEvent event)
            oneArgListener.invoke(elContext, new Object[] { event });
        }
    }
}
