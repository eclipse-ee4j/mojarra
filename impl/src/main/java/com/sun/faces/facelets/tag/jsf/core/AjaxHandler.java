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

package com.sun.faces.facelets.tag.jsf.core;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodNotFoundException;
import javax.faces.application.Application;
import javax.faces.application.ResourceHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;
import javax.faces.view.AttachedObjectTarget;
import javax.faces.view.BehaviorHolderAttachedObjectHandler;
import javax.faces.view.BehaviorHolderAttachedObjectTarget;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.CompositeFaceletHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

import com.sun.faces.component.behavior.AjaxBehaviors;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.jsf.CompositeComponentTagHandler;
import com.sun.faces.renderkit.RenderKitUtils;



/**
 * <p class="changed_added_2_0"><span class="changed_modified_2_2">Enable</span>
 * one or more components in the view
 * to perform Ajax operations.  This tag handler must create an instance
 * of {@link javax.faces.component.behavior.AjaxBehavior} using the tag attribute
 * values.  <div class="changed_modified_2_2">The <code>events</code> attribute for
 * this tag that can be a <code>ValueExpression</code> must be evaluated at tag
 * execution time since the event name is used in the process of <code>Behavior</code>
 * creation.</div>  If this tag is nested within a single
 * {@link ClientBehaviorHolder} component:
 * <ul>
 * <li>If the <code>events</code> attribute value is not specified, 
 * obtain the default event name by calling {@link ClientBehaviorHolder#getDefaultEventName}.
 * If that returns <code>null</code> throw an <code>exception</code>.</li>
 * <li>If the <code>events</code> attribute value is specified, ensure it
 * that it exists in the <code>Collection</code> returned from a call to
 * {@link ClientBehaviorHolder#getEventNames} and throw an <code>exception</code> if
 * it doesn't exist.</li>
 * <li>Add the {@link AjaxBehavior} instance to the {@link ClientBehaviorHolder}
 * component by calling {@link ClientBehaviorHolder#addClientBehavior} passing <code>event</code>
 * and the {@link AjaxBehavior} instance.</li> 
 * </ul>
 * <br/><br/>
 * Check for the existence of the Ajax resource by calling 
 * <code>UIViewRoot.getComponentResources()</code>.  If
 * the Ajax resource does not exist, create a <code>UIOutput</code> component
 * instance and set the renderer type to <code>javax.faces.resource.Script</code>.
 * Set the the following attributes in the component's attribute <code>Map</code>:
 * <code>library</code> with the value {@value ResourceHandler#JSF_SCRIPT_LIBRARY_NAME} and 
 * <code>name</code> with the value {@value ResourceHandler#JSF_SCRIPT_RESOURCE_NAME}. Install the component
 * resource using <code>UIViewRoot.addComponentResource()</code> and specifying
 * <code>head</code> as the <code>target</code> argument.</p> 
 *
 * If this tag has component children, add the {@link AjaxBehavior} to 
 * {@link AjaxBehaviors} by calling {@link AjaxBehaviors#pushBehavior}. As 
 * subsequent child components that implement the {@link ClientBehaviorHolder} interface 
 * are evaluated this {@link AjaxBehavior} instance must be added as a behavior to
 * the component.
 * </p>
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
        this.event = this.getAttribute("event");
        this.execute = this.getAttribute("execute");
        this.render = this.getAttribute("render");
        this.onevent = this.getAttribute("onevent");
        this.onerror = this.getAttribute("onerror");
        this.disabled = this.getAttribute("disabled");
        this.immediate = this.getAttribute("immediate");
        this.resetValues = this.getAttribute("resetValues");
        this.listener = this.getAttribute("listener");
        this.delay = this.getAttribute("delay");

        this.wrapping = isWrapping();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
     *      javax.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
          throws IOException {

        String eventName = getEventName();

        if (this.wrapping) {
            applyWrapping(ctx, parent, eventName);
        }  else {
            applyNested(ctx, parent, eventName);
        }
    }
    
    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent) {
        FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        applyAttachedObject(ctx, parent, getEventName());
    }

    /* (non-Javadoc)
     * @see javax.faces.view.AttachedObjectHandler#getFor()
     */
    @Override
    public String getFor() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see javax.faces.view.BehaviorHolderAttachedObjectHandler#getEventName()
     */
    @Override
    public String getEventName() {
        FacesContext context = FacesContext.getCurrentInstance();
        FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        return (this.event != null) ? this.event.getValue(ctx) : null;
    }

    // Tests whether the <f:ajax> is wrapping other tags.
    private boolean isWrapping() {

        // Would be nice if there was some easy way to determine whether
        // we are a leaf handler.  However, even leaf handlers have a
        // non-null nextHandler - the CompilationUnit.LEAF instance.
        // We assume that if we've got a TagHandler or CompositeFaceletHandler
        // as our nextHandler, we are not a leaf.
        return ((this.nextHandler instanceof TagHandler) || 
                (this.nextHandler instanceof CompositeFaceletHandler));
    }

    // Applies a wrapping AjaxHandler by pushing/popping the AjaxBehavior
    // to the AjaxBeahviors object.
    private void applyWrapping(FaceletContext ctx, 
                               UIComponent parent,
                               String eventName) throws IOException {

        // In the wrapping case, we assume that some wrapped component
        // is going to be Ajax enabled and install the Ajax resource.
        RenderKitUtils.installJsfJsIfNecessary(ctx.getFacesContext());

        AjaxBehavior ajaxBehavior = createAjaxBehavior(ctx, eventName);

        // We leverage AjaxBehaviors to support the wrapping case.  We
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
    private void applyNested(FaceletContext ctx, 
                             UIComponent parent,
                             String eventName) {
        
        if (!ComponentHandler.isNew(parent)) {
            return;
        }

        // Composite component case
        if (UIComponent.isCompositeComponent(parent)) {
            // Check composite component event name:
            boolean tagApplied = false;
            if (parent instanceof ClientBehaviorHolder) {
                applyAttachedObject(ctx, parent, eventName);  // error here will propagate up
                tagApplied = true;
            }
            BeanInfo componentBeanInfo = (BeanInfo) parent.getAttributes().get(
                  UIComponent.BEANINFO_KEY);
            if (null == componentBeanInfo) {
                throw new TagException(
                      tag,
                      "Error: enclosing composite component does not have BeanInfo attribute");
            }
            BeanDescriptor componentDescriptor = componentBeanInfo.getBeanDescriptor();
            if (null == componentDescriptor) {
                throw new TagException(
                      tag,
                      "Error: enclosing composite component BeanInfo does not have BeanDescriptor");
            }
            List<AttachedObjectTarget> targetList = (List<AttachedObjectTarget>)
                  componentDescriptor
                        .getValue(AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY);
            if (null == targetList && !tagApplied) {
                throw new TagException(
                      tag,
                      "Error: enclosing composite component does not support behavior events");
            }
            boolean supportedEvent = false;
            for (AttachedObjectTarget target : targetList) {
                if (target instanceof BehaviorHolderAttachedObjectTarget) {
                    BehaviorHolderAttachedObjectTarget behaviorTarget = (BehaviorHolderAttachedObjectTarget) target;
                    if ((null != eventName && eventName.equals(behaviorTarget.getName()))
                        || (null == eventName && behaviorTarget.isDefaultEvent())) {
                        supportedEvent = true;
                        break;
                    }
                }
            }
            if (supportedEvent) {
                CompositeComponentTagHandler.getAttachedObjectHandlers(parent)
                      .add(this);
            } else {
                if (!tagApplied) {
                    throw new TagException(
                            tag,
                            "Error: enclosing composite component does not support event "
                            + eventName);
                }
            }
        } else if (parent instanceof ClientBehaviorHolder) {
            applyAttachedObject(ctx, parent, eventName);
        } else {
            throw new TagException(this.tag,
                                   "Unable to attach <f:ajax> to non-ClientBehaviorHolder parent");
        }

    }

    /**
     * <p class="changed_added_2_0"></p>
     * @param ctx
     * @param parent
     * @param eventName
     */
    private void applyAttachedObject(FaceletContext ctx,
                                     UIComponent parent,
                                     String eventName) {
        ClientBehaviorHolder bHolder = (ClientBehaviorHolder) parent;

        if (null == eventName) {
            eventName = bHolder.getDefaultEventName();
            if (null == eventName) {
                throw new TagException(this.tag,
                    "Event attribute could not be determined: "
                        + eventName);
            }
        } else {
            Collection<String> eventNames = bHolder.getEventNames();
            if (!eventNames.contains(eventName)) {
                throw new TagException(this.tag, 
                    getUnsupportedEventMessage(eventName, eventNames, parent));
            }
        }

        AjaxBehavior ajaxBehavior = createAjaxBehavior(ctx, eventName);
        bHolder.addClientBehavior(eventName, ajaxBehavior);
        RenderKitUtils.installJsfJsIfNecessary(ctx.getFacesContext());
    }

    // Construct our AjaxBehavior from tag parameters.
    private AjaxBehavior createAjaxBehavior(FaceletContext ctx, String eventName) {
        Application application = ctx.getFacesContext().getApplication();
        AjaxBehavior behavior = (AjaxBehavior)application.createBehavior(
                                                  AjaxBehavior.BEHAVIOR_ID);

        setBehaviorAttribute(ctx, behavior, this.onevent, String.class);
        setBehaviorAttribute(ctx, behavior, this.onerror, String.class);
        setBehaviorAttribute(ctx, behavior, this.disabled, Boolean.class);
        setBehaviorAttribute(ctx, behavior, this.immediate, Boolean.class);
        setBehaviorAttribute(ctx, behavior, this.resetValues, Boolean.class);
        setBehaviorAttribute(ctx, behavior, this.execute, Object.class);
        setBehaviorAttribute(ctx, behavior, this.render, Object.class);
        setBehaviorAttribute(ctx, behavior, this.delay, String.class);

        if (null != listener) {
            behavior.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(
                this.listener.getMethodExpression(ctx, Object.class, new Class[] { AjaxBehaviorEvent.class }),
                this.listener.getMethodExpression(ctx, Object.class, new Class[] { })));
        }

        return behavior;
    }

    // Sets the value from the TagAttribute on the behavior
    private void setBehaviorAttribute(FaceletContext ctx,
                                      AjaxBehavior behavior,
                                      TagAttribute attr,
                                      Class type) {

        if (attr != null) {
            behavior.setValueExpression(attr.getLocalName(),
                                        attr.getValueExpression(ctx, type));
        }    
    }

    // Returns an error message for the case where the <f:ajax> event
    // attribute specified an unknown/unsupported event.
    private String getUnsupportedEventMessage(String             eventName,
                                              Collection<String> eventNames,
                                              UIComponent        parent) {
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
    public AjaxBehaviorListenerImpl() {}

    public AjaxBehaviorListenerImpl(MethodExpression oneArg, MethodExpression noArg) {
        this.oneArgListener = oneArg;
        this.noArgListener = noArg;
    }

    @Override
    public void processAjaxBehavior(AjaxBehaviorEvent event) throws AbortProcessingException {
        final ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        try{
            noArgListener.invoke(elContext, new Object[]{});
        } catch (MethodNotFoundException | IllegalArgumentException mnfe) {
            // Attempt to call public void method(AjaxBehaviorEvent event)
            oneArgListener.invoke(elContext, new Object[]{event});
        }
    }
}
