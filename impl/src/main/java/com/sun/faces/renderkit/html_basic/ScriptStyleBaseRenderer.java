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

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.render.Renderer;

/**
 * <p>
 * Base class for shared behavior between Script and Stylesheet renderers. Maybe composition would be better, but
 * inheritance is easier
 * </p>
 */
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public abstract class ScriptStyleBaseRenderer extends Renderer implements ComponentSystemEventListener {

    private static final String COMP_KEY = ScriptStyleBaseRenderer.class.getName() + "_COMPOSITE_COMPONENT";

    // Log instance for this class
    protected static final Logger logger = FacesLogger.RENDERKIT.getLogger();

    /*
     * Indicates that the component associated with this Renderer has already been added to the facet in the view.
     */

    /*
     * When this method is called, we know that there is a component with a script renderer somewhere in the view. We need
     * to make it so that when an element with a name given by the value of the optional "target" component attribute is
     * encountered, this component can be called upon to render itself. This method will add the component (associated with
     * this Renderer) to a facet in the view only if a "target" component attribute is set.
     *
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        UIComponent component = event.getComponent();
        FacesContext context = FacesContext.getCurrentInstance();

        String target = verifyTarget((String) component.getAttributes().get("target"));
        if (target != null) {
            // We're checking for a composite component here as if the resource
            // is relocated, it may still require it's composite component context
            // in order to properly render. Store it for later use by
            // encodeBegin() and encodeEnd().
            UIComponent cc = UIComponent.getCurrentCompositeComponent(context);
            if (cc != null) {
                component.getAttributes().put(COMP_KEY, cc.getClientId(context));
            }
            context.getViewRoot().addComponentResource(context, component, target);

        }
    }

    @Override
    public final void decode(FacesContext context, UIComponent component) {
        // no-op
    }

    @Override
    public final boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        String ccID = (String) component.getAttributes().get(COMP_KEY);
        if (null != ccID) {
            char sep = UINamingContainer.getSeparatorChar(context);
            UIComponent cc;
            // If the composite component id includes the separator char...
            if (-1 != ccID.indexOf(sep)) {
                // use the UIViewRoot's findComponent.
                cc = context.getViewRoot().findComponent(':' + ccID);
            } else {
                // ... otherwise use our special findComponent.
                cc = findComponentIgnoringNamingContainers(context.getViewRoot(), ccID, true);
            }
            UIComponent curCC = UIComponent.getCurrentCompositeComponent(context);
            if (cc != curCC) {
                // the first pop maps to the component we're rendering.
                // push the composite component to the 'stack' and then re-push
                // the component we're rendering so the current component is
                // correct.
                component.popComponentFromEL(context);
                component.pushComponentToEL(context, cc);
                component.pushComponentToEL(context, component);
            }
        }

    }

    @Override
    public final void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        Map<String, Object> attributes = component.getAttributes();

        String name = (String) attributes.get("name");
        int childCount = component.getChildCount();
        boolean renderChildren = 0 < childCount;

        // If we have no "name" attribute...
        if (null == name) {
            // and no child content...
            if (0 == childCount) {
                // this is user error, so put up a message if desired
                if (context.isProjectStage(ProjectStage.Development)) {
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "outputScript or outputStylesheet with no library, no name, and no body content", "Is body content intended?");
                    context.addMessage(component.getClientId(context), message);
                }
                // We have no children, but don't bother with the method
                // invocation anyway.
                renderChildren = false;
            }
        } else if (0 < childCount) {
            // If we have a "name" and also have child content, ignore
            // the child content and log a message.
            logger.info("outputScript or outputStylesheet with name attribute and nested content. Ignoring nested content.");
            renderChildren = false;
        }

        if (renderChildren) {
            ResponseWriter writer = context.getResponseWriter();
            startInlineElement(context, writer, component);
            super.encodeChildren(context, component);
            endInlineElement(writer, component);
        }

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Map<String, Object> attributes = component.getAttributes();
        String name = (String) attributes.get("name");

        if (null == name) {
            return;
        }

        // Special case of resource names that have query strings.
        // These resources actually use their query strings internally, not externally, so we don't need the resource to know
        // about them.
        int queryPos = name.indexOf("?");
        String query = null;
        if (queryPos > -1 && name.length() > queryPos) {
            query = name.substring(queryPos + 1);
            name = name.substring(0, queryPos);
        }

        String library = (String) attributes.get("library");

        // Ensure this resource is not rendered more than once per request.
        ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
        if (resourceHandler.isResourceRendered(context, name, library)) {
            return;
        }

        Resource resource = resourceHandler.createResource(name, library);
        String resourceUrl = "RES_NOT_FOUND";

        ResponseWriter writer = context.getResponseWriter();
        startExternalElement(context, writer, component);

        if (library == null && name != null && ApplicationAssociate.getInstance(context).getResourceManager().isContractsResource(name)) {
            if (context.isProjectStage(ProjectStage.Development)) {
                String msg = "Illegal path, direct contract references are not allowed: " + name;
                context.addMessage(component.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
            }
            resource = null;
        }

        if (resource == null) {
            if (context.isProjectStage(ProjectStage.Development)) {
                String msg = "Unable to find resource " + (library == null ? "" : library + ", ") + name;
                context.addMessage(component.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
            }
        } else {
            resourceUrl = resource.getRequestPath();
            if (query != null) {
                resourceUrl = resourceUrl + (resourceUrl.indexOf("?") > -1 ? "&amp;" : "?") + query;
            }
            resourceUrl = context.getExternalContext().encodeResourceURL(resourceUrl);
        }

        endExternalElement(writer, component, resourceUrl);
        resourceHandler.markResourceRendered(context, name, library);

        // Remove the key to prevent issues with state saving.
        String ccID = (String) component.getAttributes().get(COMP_KEY);
        if (ccID != null) {
            // the first pop maps to the component we're rendering.
            // the second pop maps to the composite component that was pushed
            // in this renderer's encodeBegin implementation.
            // re-push the current component to reset the original context
            component.popComponentFromEL(context);
            component.popComponentFromEL(context);
            component.pushComponentToEL(context, component);
        }
    }

    private static UIComponent findComponentIgnoringNamingContainers(UIComponent base, String id, boolean checkId) {
        if (checkId && id.equals(base.getId())) {
            return base;
        }
        // Search through our facets and children
        UIComponent result = null;
        for (Iterator<UIComponent> i = base.getFacetsAndChildren(); i.hasNext();) {
            UIComponent kid = i.next();
            if (checkId && id.equals(kid.getId())) {
                result = kid;
                break;
            }
            result = findComponentIgnoringNamingContainers(kid, id, true);
            if (result != null) {
                break;
            } else if (id.equals(kid.getId())) {
                result = kid;
                break;
            }
        }
        return result;

    }

    // ------------------------------------------------------- Protected Methods

    /**
     * <p>
     * Allow the subclass to customize the start inline element content.
     * </p>
     */
    protected abstract void startInlineElement(FacesContext context, ResponseWriter writer, UIComponent component) throws IOException;

    /**
     * <p>
     * Allow the subclass to customize the end inline element content.
     * </p>
     */
    protected abstract void endInlineElement(ResponseWriter writer, UIComponent component) throws IOException;

    /**
     * <p>
     * Allow the subclass to customize the start external element content.
     * </p>
     */
    protected abstract void startExternalElement(FacesContext context, ResponseWriter writer, UIComponent component) throws IOException;

    /**
     * <p>
     * Allow the subclass to customize the end external element content.
     * </p>
     */
    protected abstract void endExternalElement(ResponseWriter writer, UIComponent component, String resourceUrl) throws IOException;

    /**
     * <p>
     * Allow a subclass to control what's a valid value for "target".
     */
    protected String verifyTarget(String toVerify) {
        return toVerify;
    }

}
