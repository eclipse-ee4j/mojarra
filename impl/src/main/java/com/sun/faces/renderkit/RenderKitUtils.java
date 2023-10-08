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

package com.sun.faces.renderkit;

import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.BEHAVIOR_EVENT_PARAM;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.BEHAVIOR_SOURCE_PARAM;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.PARTIAL_EVENT_PARAM;
import static jakarta.faces.application.ResourceHandler.FACES_SCRIPT_LIBRARY_NAME;
import static jakarta.faces.application.ResourceHandler.FACES_SCRIPT_RESOURCE_NAME;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.el.ELUtils;
import com.sun.faces.facelets.util.DevTools;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.ActionSource2;
import jakarta.faces.component.Doctype;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHint;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.html.HtmlMessages;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.model.SelectItem;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;
import jakarta.faces.render.ResponseStateManager;

/**
 * <p>
 * A set of utilities for use in {@link RenderKit}s.
 * </p>
 */
public class RenderKitUtils {

    /**
     * <p>
     * The prefix to append to certain attributes when renderking <code>XHTML Transitional</code> content.
     */
    private static final String XHTML_ATTR_PREFIX = "xml:";

    /**
     * <p>
     * <code>Boolean</code> attributes to be rendered using <code>XHMTL</code> semantics.
     */
    private static final String[] BOOLEAN_ATTRIBUTES = { "disabled", "ismap", "readonly", "multiple" };

    /**
     * <p>
     * An array of attributes that must be prefixed by {@link #XHTML_ATTR_PREFIX} when rendering
     * <code>XHTML Transitional</code> content.
     */
    private static final String[] XHTML_PREFIX_ATTRIBUTES = { "lang" };

    /**
     * <p>
     * The maximum number of content type parts. For example: for the type: "text/html; level=1; q=0.5" The parts of this
     * type would be: "text" - type "html; level=1" - subtype "0.5" - quality value "1" - level value
     * </p>
     */
    private final static int MAX_CONTENT_TYPE_PARTS = 4;

    /**
     * The character that is used to delimit content types in an accept String.
     * </p>
     */
    private final static String CONTENT_TYPE_DELIMITER = ",";

    /**
     * The character that is used to delimit the type and subtype portions of a content type in an accept String. Example:
     * text/html
     * </p>
     */
    private final static String CONTENT_TYPE_SUBTYPE_DELIMITER = "/";

    /**
     * This represents the base package that can leverage the <code>attributesThatAreSet</code> List for optimized attribute
     * rendering.
     *
     * IMPLEMENTATION NOTE: This must be kept in sync with the array in UIComponentBase$AttributesMap and
     * HtmlComponentGenerator.
     *
     * Hopefully Faces X will remove the need for this.
     */
    private static final String OPTIMIZED_PACKAGE = "jakarta.faces.component.";

    /**
     * IMPLEMENTATION NOTE: This must be kept in sync with the Key in UIComponentBase$AttributesMap and
     * HtmlComponentGenerator.
     *
     * Hopefully Faces X will remove the need for this.
     */
    private static final String ATTRIBUTES_THAT_ARE_SET_KEY = UIComponentBase.class.getName() + ".attributesThatAreSet";

    /**
     * UIViewRoot attribute key of a boolean value which remembers whether the view will be rendered with a HTML5 doctype.
     */
    private static final String VIEW_ROOT_ATTRIBUTES_DOCTYPE_KEY = RenderKitUtils.class.getName() + ".isOutputHtml5Doctype";


    protected static final Logger LOGGER = FacesLogger.RENDERKIT.getLogger();

    public static final String DEVELOPMENT_STAGE_MESSAGES_ID = "jakarta_faces_developmentstage_messages";

    /**
     * @see UIViewRoot#encodeChildren(FacesContext)
     */
    public enum PredefinedPostbackParameter {

        VIEW_STATE_PARAM(ResponseStateManager.VIEW_STATE_PARAM), CLIENT_WINDOW_PARAM(ResponseStateManager.CLIENT_WINDOW_PARAM),
        RENDER_KIT_ID_PARAM(ResponseStateManager.RENDER_KIT_ID_PARAM), BEHAVIOR_SOURCE_PARAM(ClientBehaviorContext.BEHAVIOR_SOURCE_PARAM_NAME),
        BEHAVIOR_EVENT_PARAM(ClientBehaviorContext.BEHAVIOR_EVENT_PARAM_NAME), PARTIAL_EVENT_PARAM(PartialViewContext.PARTIAL_EVENT_PARAM_NAME),
        PARTIAL_EXECUTE_PARAM(PartialViewContext.PARTIAL_EXECUTE_PARAM_NAME), PARTIAL_RENDER_PARAM(PartialViewContext.PARTIAL_RENDER_PARAM_NAME),
        PARTIAL_RESET_VALUES_PARAM(PartialViewContext.RESET_VALUES_PARAM_NAME);

        private String name;

        private PredefinedPostbackParameter(String name) {
            this.name = name;
        }

        public String getValue(FacesContext context) {
            return context.getExternalContext().getRequestParameterMap().get(getName(context));
        }

        public String getName(FacesContext context) {
            return getParameterName(context, name);
        }
    }

    // ------------------------------------------------------------ Constructors

    private RenderKitUtils() {
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * <p>
     * Return the {@link RenderKit} for the current request.
     * </p>
     *
     * @param context the {@link FacesContext} of the current request
     * @return the {@link RenderKit} for the current request.
     */
    public static RenderKit getCurrentRenderKit(FacesContext context) {

        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        return renderKitFactory.getRenderKit(context, context.getViewRoot().getRenderKitId());

    }

    /**
     * <p>
     * Obtain and return the {@link ResponseStateManager} for the specified #renderKitId.
     * </p>
     *
     * @param context the {@link FacesContext} of the current request
     * @param renderKitId {@link RenderKit} ID
     * @return the {@link ResponseStateManager} for the specified #renderKitId
     * @throws FacesException if an exception occurs while trying to obtain the <code>ResponseStateManager</code>
     */
    public static ResponseStateManager getResponseStateManager(FacesContext context, String renderKitId) throws FacesException {

        assert null != renderKitId;
        assert null != context;

        RenderKit renderKit = context.getRenderKit();
        if (renderKit == null) {
            // check request scope for a RenderKitFactory implementation
            RenderKitFactory factory = (RenderKitFactory) RequestStateManager.get(context, RequestStateManager.RENDER_KIT_IMPL_REQ);
            if (factory != null) {
                renderKit = factory.getRenderKit(context, renderKitId);
            } else {
                factory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
                if (factory == null) {
                    throw new FacesException("Unable to locate RenderKitFactory for " + FactoryFinder.RENDER_KIT_FACTORY);
                } else {
                    RequestStateManager.set(context, RequestStateManager.RENDER_KIT_IMPL_REQ, factory);
                }
                renderKit = factory.getRenderKit(context, renderKitId);
                if (renderKit == null) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, "Unable to locate renderkit " + "instance for render-kit-id {0}.  Using {1} instead.",
                                new String[] { renderKitId, RenderKitFactory.HTML_BASIC_RENDER_KIT });
                    }
                    renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
                    UIViewRoot root = context.getViewRoot();
                    if (null != root) {
                        root.setRenderKitId(renderKitId);
                    }
                }
                renderKit = factory.getRenderKit(context, renderKitId);
                if (renderKit == null) {
                    throw new FacesException("Unable to locate renderkit instance for render-kit-id " + renderKitId);
                }
            }
        }
        return renderKit.getResponseStateManager();

    }

    /**
     * <p>
     * Return a List of {@link jakarta.faces.model.SelectItem} instances representing the available options for this
     * component, assembled from the set of {@link jakarta.faces.component.UISelectItem} and/or
     * {@link jakarta.faces.component.UISelectItems} components that are direct children of this component. If there are no
     * such children, an empty <code>Iterator</code> is returned.
     * </p>
     *
     * @param context The {@link jakarta.faces.context.FacesContext} for the current request. If null, the UISelectItems
     * behavior will not work.
     * @param component the component
     * @throws IllegalArgumentException if <code>context</code> is <code>null</code>
     * @return a List of the select items for the specified component
     */
    public static SelectItemsIterator<SelectItem> getSelectItems(FacesContext context, UIComponent component) {

        Util.notNull("context", context);
        Util.notNull("component", component);

        return new SelectItemsIterator<>(context, component);

    }

    /**
     * <p>
     * Render any "passthru" attributes, where we simply just output the raw name and value of the attribute. This method is
     * aware of the set of HTML4 attributes that fall into this bucket. Examples are all the javascript attributes, alt,
     * rows, cols, etc.
     * </p>
     *
     * @param context the FacesContext for this request
     * @param writer writer the {@link jakarta.faces.context.ResponseWriter} to be used when writing the attributes
     * @param component the component
     * @param attributes an array of attributes to be processed
     * @throws IOException if an error occurs writing the attributes
     */
    public static void renderPassThruAttributes(FacesContext context, ResponseWriter writer, UIComponent component, Attribute[] attributes) throws IOException {

        assert null != context;
        assert null != writer;
        assert null != component;

        Map<String, List<ClientBehavior>> behaviors = null;

        if (component instanceof ClientBehaviorHolder) {
            behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
        }

        // Don't render behavior scripts if component is disabled
        if (null != behaviors && behaviors.size() > 0 && Util.componentIsDisabled(component)) {
            behaviors = null;
        }

        renderPassThruAttributes(context, writer, component, attributes, behaviors);
    }

    /**
     * <p>
     * Render any "passthru" attributes, where we simply just output the raw name and value of the attribute. This method is
     * aware of the set of HTML4 attributes that fall into this bucket. Examples are all the javascript attributes, alt,
     * rows, cols, etc.
     * </p>
     *
     * @param context the FacesContext for this request
     * @param writer writer the {@link jakarta.faces.context.ResponseWriter} to be used when writing the attributes
     * @param component the component
     * @param attributes an array of attributes to be processed
     * @param behaviors the behaviors for this component, or null if component is not a ClientBehaviorHolder
     * @throws IOException if an error occurs writing the attributes
     */
    @SuppressWarnings("unchecked")
    public static void renderPassThruAttributes(FacesContext context, ResponseWriter writer, UIComponent component, Attribute[] attributes,
            Map<String, List<ClientBehavior>> behaviors) throws IOException {

        assert null != writer;
        assert null != component;

        if (behaviors == null) {
            behaviors = Collections.emptyMap();
        }

        if (canBeOptimized(component, behaviors)) {
            List<String> setAttributes = (List<String>) component.getAttributes().get(ATTRIBUTES_THAT_ARE_SET_KEY);
            if (setAttributes != null) {
                renderPassThruAttributesOptimized(context, writer, component, attributes, setAttributes, behaviors);
            }
        } else {

            // this block should only be hit by custom components leveraging
            // the RI's rendering code, or in cases where we have behaviors
            // attached to multiple events. We make no assumptions and loop
            // through
            renderPassThruAttributesUnoptimized(context, writer, component, attributes, behaviors);
        }
    }

    // Renders the onchange handler for input components. Handles
    // chaining together the user-provided onchange handler with
    // any Behavior scripts.
    public static void renderOnchange(FacesContext context, UIComponent component, boolean incExec) throws IOException {

        final String handlerName = "onchange";
        final Object userHandler = component.getAttributes().get(handlerName);
        String behaviorEventName = "valueChange";
        if (component instanceof ClientBehaviorHolder) {
            Map<?, ?> behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
            if (null != behaviors && behaviors.containsKey("change")) {
                behaviorEventName = "change";
            }
        }

        List<ClientBehaviorContext.Parameter> params;
        if (!incExec) {
            params = Collections.emptyList();
        } else {
            params = new LinkedList<>();
            params.add(new ClientBehaviorContext.Parameter("incExec", true));
        }
        renderHandler(context, component, params, handlerName, userHandler, behaviorEventName, null, false, incExec);
    }

    // Renders onclick handler for SelectRaidio and SelectCheckbox
    public static void renderSelectOnclick(FacesContext context, UIComponent component, boolean incExec) throws IOException {

        final String handlerName = "onclick";
        final Object userHandler = component.getAttributes().get(handlerName);
        String behaviorEventName = "valueChange";
        if (component instanceof ClientBehaviorHolder) {
            Map<?, ?> behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
            if (null != behaviors && behaviors.containsKey("click")) {
                behaviorEventName = "click";
            }
        }

        List<ClientBehaviorContext.Parameter> params;
        if (!incExec) {
            params = Collections.emptyList();
        } else {
            params = new LinkedList<>();
            params.add(new ClientBehaviorContext.Parameter("incExec", true));
        }
        renderHandler(context, component, params, handlerName, userHandler, behaviorEventName, null, false, incExec);
    }

    // Renders the onclick handler for command buttons. Handles
    // chaining together the user-provided onclick handler, any
    // Behavior scripts, plus the default button submit script.
    public static void renderOnclick(FacesContext context, UIComponent component, Collection<ClientBehaviorContext.Parameter> params, String submitTarget,
            boolean needsSubmit) throws IOException {

        final String handlerName = "onclick";
        final Object userHandler = component.getAttributes().get(handlerName);
        String behaviorEventName = "action";
        if (component instanceof ClientBehaviorHolder) {
            Map<String, List<ClientBehavior>> behaviors = ((ClientBehaviorHolder) component).getClientBehaviors();
            boolean mixed = null != behaviors && behaviors.containsKey("click") && behaviors.containsKey("action");
            if (mixed) {
                behaviorEventName = "click";
                List<ClientBehavior> clickBehaviors = behaviors.get("click");
                List<ClientBehavior> actionBehaviors = behaviors.get("action");
                clickBehaviors.addAll(actionBehaviors);
                actionBehaviors.clear();
            } else if (null != behaviors && behaviors.containsKey("click")) {
                behaviorEventName = "click";
            }
        }

        renderHandler(context, component, params, handlerName, userHandler, behaviorEventName, submitTarget, needsSubmit, false);
    }

    // Renders the script function for command scripts.
    public static void renderFunction(FacesContext context, UIComponent component, Collection<ClientBehaviorContext.Parameter> params, String submitTarget)
            throws IOException {

        ClientBehaviorContext behaviorContext = ClientBehaviorContext.createClientBehaviorContext(context, component, "action", submitTarget, params);
        AjaxBehavior behavior = (AjaxBehavior) context.getApplication().createBehavior(AjaxBehavior.BEHAVIOR_ID);
        mapAttributes(component, behavior, "execute", "render", "onerror", "onevent", "resetValues");

        context.getResponseWriter().append(behavior.getScript(behaviorContext));
    }

    private static void mapAttributes(UIComponent component, AjaxBehavior behavior, String... attributeNames) {
        for (String attributeName : attributeNames) {
            ValueExpression binding = component.getValueExpression(attributeName);

            if (binding == null) {
                Object value = component.getAttributes().get(attributeName);

                if (value != null) {
                    binding = ELUtils.createValueExpression(value.toString(), value.getClass());
                }
            }

            behavior.setValueExpression(attributeName, binding);
        }
    }

    public static String prefixAttribute(final String attrName, final ResponseWriter writer) {

        return prefixAttribute(attrName, RIConstants.XHTML_CONTENT_TYPE.equals(writer.getContentType()));

    }

    public static String prefixAttribute(final String attrName, boolean isXhtml) {
        if (isXhtml) {
            if (Arrays.binarySearch(XHTML_PREFIX_ATTRIBUTES, attrName) > -1) {
                return XHTML_ATTR_PREFIX + attrName;
            } else {
                return attrName;
            }
        } else {
            return attrName;
        }

    }

    /**
     * <p>
     * Renders the attributes from {@link #BOOLEAN_ATTRIBUTES} using <code>XHMTL</code> semantics (i.e.,
     * disabled="disabled").
     * </p>
     *
     * @param writer writer the {@link ResponseWriter} to be used when writing the attributes
     * @param component the component
     * @throws IOException if an error occurs writing the attributes
     */
    public static void renderXHTMLStyleBooleanAttributes(ResponseWriter writer, UIComponent component) throws IOException {

        assert writer != null;
        assert component != null;

        List<String> excludedAttributes = null;

        renderXHTMLStyleBooleanAttributes(writer, component, excludedAttributes);
    }

    /**
     * <p>
     * Renders the attributes from {@link #BOOLEAN_ATTRIBUTES} using <code>XHMTL</code> semantics (i.e.,
     * disabled="disabled").
     * </p>
     *
     * @param writer writer the {@link ResponseWriter} to be used when writing the attributes
     * @param component the component
     * @param excludedAttributes a <code>List</code> of attributes that are to be excluded from rendering
     * @throws IOException if an error occurs writing the attributes
     */
    public static void renderXHTMLStyleBooleanAttributes(ResponseWriter writer, UIComponent component, List<String> excludedAttributes) throws IOException {

        assert writer != null;
        assert component != null;

        Map<?, ?> attrMap = component.getAttributes();
        for (String attrName : BOOLEAN_ATTRIBUTES) {
            if (isExcludedAttribute(attrName, excludedAttributes)) {
                continue;
            }
            Object val = attrMap.get(attrName);
            if (val == null) {
                continue;
            }

            if (Boolean.valueOf(val.toString())) {
                writer.writeAttribute(attrName, true, attrName);
            }
        }

    }

    /**
     * <p>
     * Given an accept String from the client, and a <code>String</code> of server supported content types, determine the
     * best qualified content type for the client. If no match is found, or either of the arguments are <code>null</code>,
     * <code>null</code> is returned.
     * </p>
     *
     * @param accept The client accept String
     * @param serverSupportedTypes The types that the server supports
     * @param preferredType The preferred content type if another type is found with the same highest quality factor.
     * @return The content type <code>String</code>
     */
    public static String determineContentType(String accept, String serverSupportedTypes, String preferredType) {
        String contentType = null;

        if (null == accept || null == serverSupportedTypes) {
            return contentType;
        }

        String[][] clientContentTypes = buildTypeArrayFromString(accept);
        String[][] serverContentTypes = buildTypeArrayFromString(serverSupportedTypes);
        String[][] preferredContentType = buildTypeArrayFromString(preferredType);
        String[][] matchedInfo = findMatch(clientContentTypes, serverContentTypes, preferredContentType);

        // if best match exits and best match is not some wildcard,
        // return best match
        if (matchedInfo[0][1] != null && !matchedInfo[0][2].equals("*")) {
            contentType = matchedInfo[0][1] + CONTENT_TYPE_SUBTYPE_DELIMITER + matchedInfo[0][2];
        }

        return contentType;
    }

    /**
     * @param contentType the content type in question
     * @return <code>true</code> if the content type is a known XML-based content type, otherwise, <code>false</code>
     */
    public static boolean isXml(String contentType) {
        return RIConstants.XHTML_CONTENT_TYPE.equals(contentType) || RIConstants.APPLICATION_XML_CONTENT_TYPE.equals(contentType)
                || RIConstants.TEXT_XML_CONTENT_TYPE.equals(contentType);
    }

    // --------------------------------------------------------- Private Methods

    /**
     * @param component the UIComponent in question
     * @return <code>true</code> if the component is within the <code>jakarta.faces.component</code> or
     * <code>jakarta.faces.component.html</code> packages, otherwise return <code>false</code>
     */
    private static boolean canBeOptimized(UIComponent component, Map<String, List<ClientBehavior>> behaviors) {
        assert component != null;
        assert behaviors != null;

        String name = component.getClass().getName();
        if (name != null && name.startsWith(OPTIMIZED_PACKAGE)) {

            // If we've got behaviors attached to multiple events
            // it is difficult to optimize, so fall back to the
            // non-optimized code path. Behaviors attached to
            // multiple event handlers should be a fairly rare case.
            return behaviors.size() < 2;
        }

        return false;
    }

    /**
     * <p>
     * For each attribute in <code>setAttributes</code>, perform a binary search against the array of
     * <code>knownAttributes</code> If a match is found and the value is not <code>null</code>, render the attribute.
     *
     * @param context the {@link FacesContext} of the current request
     * @param writer the current writer
     * @param component the component whose attributes we're rendering
     * @param knownAttributes an array of pass-through attributes supported by this component
     * @param setAttributes a <code>List</code> of attributes that have been set on the provided component
     * @param behaviors the non-null behaviors map for this request.
     * @throws IOException if an error occurs during the write
     */
    private static void renderPassThruAttributesOptimized(FacesContext context, ResponseWriter writer, UIComponent component, Attribute[] knownAttributes,
            List<String> setAttributes, Map<String, List<ClientBehavior>> behaviors) throws IOException {

        // We should only come in here if we've got zero or one behavior event
        assert behaviors != null && behaviors.size() < 2;
        String behaviorEventName = getSingleBehaviorEventName(behaviors);
        boolean renderedBehavior = false;

        Collections.sort(setAttributes);
        boolean isXhtml = RIConstants.XHTML_CONTENT_TYPE.equals(writer.getContentType());
        Map<String, Object> attrMap = component.getAttributes();
        for (String name : setAttributes) {

            // Note that this search can be optimized by switching from
            // an array to a Map<String, Attribute>. This would change
            // the search time from O(log n) to O(1).
            int index = Arrays.binarySearch(knownAttributes, Attribute.attr(name));
            if (index >= 0) {
                Object value = attrMap.get(name);
                if (value != null && shouldRenderAttribute(value)) {

                    Attribute attr = knownAttributes[index];

                    if (isBehaviorEventAttribute(attr, behaviorEventName)) {
                        renderHandler(context, component, null, name, value, behaviorEventName, null, false, false);

                        renderedBehavior = true;
                    } else {
                        writer.writeAttribute(prefixAttribute(name, isXhtml), value, name);
                    }
                }
            }
        }

        // We did not render out the behavior as part of our optimized
        // attribute rendering. Need to manually render it out now.
        if (behaviorEventName != null && !renderedBehavior) {

            // Note that we can optimize this search by providing
            // an event name -> Attribute inverse look up map.
            // This would change the search time from O(n) to O(1).
            for (int i = 0; i < knownAttributes.length; i++) {
                Attribute attr = knownAttributes[i];
                String[] events = attr.getEvents();
                if (events != null && events.length > 0 && behaviorEventName.equals(events[0])) {

                    renderHandler(context, component, null, attr.getName(), null, behaviorEventName, null, false, false);
                }
            }

        }
    }

    /**
     * <p>
     * Loops over all known attributes and attempts to render each one.
     *
     * @param context the {@link FacesContext} of the current request
     * @param writer the current writer
     * @param component the component whose attributes we're rendering
     * @param knownAttributes an array of pass-through attributes supported by this component
     * @param behaviors the non-null behaviors map for this request.
     * @throws IOException if an error occurs during the write
     */
    private static void renderPassThruAttributesUnoptimized(FacesContext context, ResponseWriter writer, UIComponent component, Attribute[] knownAttributes,
            Map<String, List<ClientBehavior>> behaviors) throws IOException {

        boolean isXhtml = RIConstants.XHTML_CONTENT_TYPE.equals(writer.getContentType());

        Map<String, Object> attrMap = component.getAttributes();

        for (Attribute attribute : knownAttributes) {
            String attrName = attribute.getName();
            String[] events = attribute.getEvents();
            boolean hasBehavior = events != null && events.length > 0 && behaviors.containsKey(events[0]);

            Object value = attrMap.get(attrName);

            if (value != null && shouldRenderAttribute(value) && !hasBehavior) {
                writer.writeAttribute(prefixAttribute(attrName, isXhtml), value, attrName);
            } else if (hasBehavior) {

                // If we've got a behavior for this attribute,
                // we may need to chain scripts together, so use
                // renderHandler().
                renderHandler(context, component, null, attrName, value, events[0], null, false, false);
            }
        }
    }

    /**
     * <p>
     * Determines if an attribute should be rendered based on the specified #attributeVal.
     * </p>
     *
     * @param attributeVal the attribute value
     * @return <code>true</code> if and only if #attributeVal is an instance of a wrapper for a primitive type and its value
     * is equal to the default value for that type as given in the specification.
     */
    private static boolean shouldRenderAttribute(Object attributeVal) {

        if (attributeVal instanceof String) {
            return true;
        } else if (attributeVal instanceof Boolean && Boolean.FALSE.equals(attributeVal)) {
            return false;
        } else if (attributeVal instanceof Integer && (Integer) attributeVal == Integer.MIN_VALUE) {
            return false;
        } else if (attributeVal instanceof Double && (Double) attributeVal == Double.MIN_VALUE) {
            return false;
        } else if (attributeVal instanceof Character && (Character) attributeVal == Character.MIN_VALUE) {
            return false;
        } else if (attributeVal instanceof Float && (Float) attributeVal == Float.MIN_VALUE) {
            return false;
        } else if (attributeVal instanceof Short && (Short) attributeVal == Short.MIN_VALUE) {
            return false;
        } else if (attributeVal instanceof Byte && (Byte) attributeVal == Byte.MIN_VALUE) {
            return false;
        } else if (attributeVal instanceof Long && (Long) attributeVal == Long.MIN_VALUE) {
            return false;
        }
        return true;

    }

    /**
     * <p>
     * This method expects a <code>List</code> of attribute names that are to be excluded from rendering. A
     * <code>Renderer</code> may include an attribute name in this list for exclusion. For example, <code>h:link</code> may
     * use the <code>disabled</code> attribute with a value of <code>true</code>. However we don't want
     * <code>disabled</code> passed through and rendered on the <code>span</code> element as it is invalid HTML.
     * </p>
     *
     * @param attributeName the attribute name that is to be tested for exclusion
     * @param excludedAttributes the list of attribute names that are to be excluded from rendering
     * @return <code>true</code> if the attribute name is not in the exclude list.
     */
    private static boolean isExcludedAttribute(String attributeName, List<String> excludedAttributes) {
        if (null == excludedAttributes) {
            return false;
        }
        if (excludedAttributes.contains(attributeName)) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * This method builds a two element array structure as follows: Example: Given the following accept string: text/html;
     * level=1, text/plain; q=0.5 [0][0] 1 (quality is 1 if none specified) [0][1] "text" (type) [0][2] "html; level=1"
     * (subtype) [0][3] 1 (level, if specified; null if not)
     *
     * [1][0] .5 [1][1] "text" [1][2] "plain" [1][3] (level, if specified; null if not)
     *
     * The array is used for comparison purposes in the findMatch method.
     * </p>
     *
     * @param accept An accept <code>String</code>
     * @return an two dimensional array containing content-type/quality info
     */
    private static String[][] buildTypeArrayFromString(String accept) {
        // return if empty
        if (accept == null || accept.length() == 0) {
            return new String[0][0];
        }
        // some helper variables
        StringBuilder typeSubType;
        String type;
        String subtype;
        String level = null;
        String quality = null;
        Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();

        // Parse "types"
        String[] types = Util.split(appMap, accept, CONTENT_TYPE_DELIMITER);
        String[][] arrayAccept = new String[types.length][MAX_CONTENT_TYPE_PARTS];
        int index = -1;
        for (int i = 0; i < types.length; i++) {
            String token = types[i].trim();
            index += 1;
            // Check to see if our accept string contains the delimiter that is used
            // to add uniqueness to a type/subtype, and/or delimits a qualifier value:
            // Example: text/html;level=1,text/html;level=2; q=.5
            if (token.contains(";")) {
                String[] typeParts = Util.split(appMap, token, ";");
                typeSubType = new StringBuilder(typeParts[0].trim());
                for (int j = 1; j < typeParts.length; j++) {
                    quality = "not set";
                    token = typeParts[j].trim();
                    // if "level" is present, make sure it gets included in the "type/subtype"
                    if (token.contains("level")) {
                        typeSubType.append(';').append(token);
                        String[] levelParts = Util.split(appMap, token, "=");
                        level = levelParts[0].trim();
                        if (level.equalsIgnoreCase("level")) {
                            level = levelParts[1].trim();
                        }
                    } else {
                        quality = token;
                        String[] qualityParts = Util.split(appMap, quality, "=");
                        quality = qualityParts[0].trim();
                        if (quality.equalsIgnoreCase("q")) {
                            quality = qualityParts[1].trim();
                            break;
                        } else {
                            quality = "not set"; // to identifiy that no quality was supplied
                        }
                    }
                }
            } else {
                typeSubType = new StringBuilder(token);
                quality = "not set"; // to identifiy that no quality was supplied
            }
            // now split type and subtype
            if (typeSubType.indexOf(CONTENT_TYPE_SUBTYPE_DELIMITER) >= 0) {
                String[] typeSubTypeParts = Util.split(appMap, typeSubType.toString(), CONTENT_TYPE_SUBTYPE_DELIMITER);
                // Apparently there are user-agents that send invalid
                // Accept headers containing no subtype (i.e. text/).
                // For those cases, assume "*" for the subtype.
                if (typeSubTypeParts.length == 1) {
                    type = typeSubTypeParts[0].trim();
                    subtype = "*";
                } else if (typeSubTypeParts.length == 0) {
                    type = typeSubType.toString();
                    subtype = "";
                } else {
                    type = typeSubTypeParts[0].trim();
                    subtype = typeSubTypeParts[1].trim();
                }

            } else {
                type = typeSubType.toString();
                subtype = "";
            }
            // check quality and assign values
            if ("not set".equals(quality)) {
                if (type.equals("*") && subtype.equals("*")) {
                    quality = "0.01";
                } else if (!type.equals("*") && subtype.equals("*")) {
                    quality = "0.02";
                } else if (type.equals("*") && subtype.length() == 0) {
                    quality = "0.01";
                } else {
                    quality = "1";
                }
            }
            arrayAccept[index][0] = quality;
            arrayAccept[index][1] = type;
            arrayAccept[index][2] = subtype;
            arrayAccept[index][3] = level;
        }
        return arrayAccept;
    }

    /**
     * <p>
     * For each server supported type, compare client (browser) specified types. If a match is found, keep track of the
     * highest quality factor. The end result is that for all matches, only the one with the highest quality will be
     * returned.
     * </p>
     *
     * @param clientContentTypes An <code>array</code> of accept <code>String</code> information for the client built
     * from @{link #buildTypeArrayFromString}.
     * @param serverSupportedContentTypes An <code>array</code> of accept <code>String</code> information for the server
     * supported types built from @{link #buildTypeArrayFromString}.
     * @param preferredContentType An <code>array</code> of preferred content type information.
     * @return An <code>array</code> containing the parts of the preferred content type for the client. The information is
     * stored as outlined in @{link #buildTypeArrayFromString}.
     */
    private static String[][] findMatch(String[][] clientContentTypes, String[][] serverSupportedContentTypes, String[][] preferredContentType) {

        List<String[]> resultList = new ArrayList<>(serverSupportedContentTypes.length);

        // the highest quality
        double highestQFactor = 0;
        // the record with the highest quality
        int idx = 0;
        for (int sidx = 0, slen = serverSupportedContentTypes.length; sidx < slen; sidx++) {
            // get server type
            String serverType = serverSupportedContentTypes[sidx][1];
            if (serverType != null) {
                for (int cidx = 0, clen = clientContentTypes.length; cidx < clen; cidx++) {
                    // get browser type
                    String browserType = clientContentTypes[cidx][1];
                    if (browserType != null) {
                        // compare them and check for wildcard
                        if (browserType.equalsIgnoreCase(serverType) || browserType.equals("*")) {
                            // types are equal or browser type is wildcard - compare subtypes
                            if (clientContentTypes[cidx][2].equalsIgnoreCase(serverSupportedContentTypes[sidx][2])
                                    || clientContentTypes[cidx][2].equals("*")) {
                                // subtypes are equal or browser subtype is wildcard
                                // found match: multiplicate qualities and add to result array
                                // if there was a level associated, this gets higher precedence, so
                                // factor in the level in the calculation.
                                double cLevel = 0.0;
                                double sLevel = 0.0;
                                if (clientContentTypes[cidx][3] != null) {
                                    cLevel = Double.parseDouble(clientContentTypes[cidx][3]) * .10;
                                }
                                if (serverSupportedContentTypes[sidx][3] != null) {
                                    sLevel = Double.parseDouble(serverSupportedContentTypes[sidx][3]) * .10;
                                }
                                double cQfactor = Double.parseDouble(clientContentTypes[cidx][0]) + cLevel;
                                double sQfactor = Double.parseDouble(serverSupportedContentTypes[sidx][0]) + sLevel;
                                double resultQuality = cQfactor * sQfactor;

                                String[] curResult = new String[MAX_CONTENT_TYPE_PARTS];
                                resultList.add(curResult);
                                curResult[0] = String.valueOf(resultQuality);

                                if (clientContentTypes[cidx][2].equals("*")) {
                                    // browser subtype is wildcard
                                    // return type and subtype (wildcard)
                                    curResult[1] = clientContentTypes[cidx][1];
                                    curResult[2] = clientContentTypes[cidx][2];
                                } else {
                                    // return server type and subtype
                                    curResult[1] = serverSupportedContentTypes[sidx][1];
                                    curResult[2] = serverSupportedContentTypes[sidx][2];
                                    curResult[3] = serverSupportedContentTypes[sidx][3];
                                }
                                // check if this was the highest factor
                                if (resultQuality > highestQFactor) {
                                    idx = resultList.size() - 1;
                                    highestQFactor = resultQuality;
                                }
                            }
                        }
                    }
                }
            }
        }

        // First, determine if we have a type that has the highest quality factor that
        // also matches the preferred type (if there is one):
        String[][] match = new String[1][3];
        if (preferredContentType.length != 0 && preferredContentType[0][0] != null) {
            BigDecimal highestQual = BigDecimal.valueOf(highestQFactor);
            for (int i = 0, len = resultList.size(); i < len; i++) {
                String[] result = resultList.get(i);
                if (BigDecimal.valueOf(Double.parseDouble(result[0])).compareTo(highestQual) == 0 && result[1].equals(preferredContentType[0][1])
                        && result[2].equals(preferredContentType[0][2])) {
                    match[0][0] = result[0];
                    match[0][1] = result[1];
                    match[0][2] = result[2];
                    return match;
                }
            }
        }

        if (!resultList.isEmpty()) {
            String[] fallBack = resultList.get(idx);
            match[0][0] = fallBack[0];
            match[0][1] = fallBack[1];
            match[0][2] = fallBack[2];
        }

        return match;
    }

    /**
     * <p>
     * Replaces all occurrences of <code>-</code> with <code>$_</code>.
     * </p>
     *
     * @param origIdentifier the original identifer that needs to be 'ECMA-ized'
     * @return an ECMA valid identifer
     */
    public static String createValidECMAIdentifier(String origIdentifier) {
        return origIdentifier.replace("-", "$_");
    }

    private static UIComponent createFacesJs() {

        UIOutput output = new UIOutput();
        output.setRendererType("jakarta.faces.resource.Script");
        output.getAttributes().put("name", FACES_SCRIPT_RESOURCE_NAME);
        output.getAttributes().put("library", FACES_SCRIPT_LIBRARY_NAME);
        return output;
    }

    /**
     * <p>
     * Only install the Faces script resource if it doesn't exist. The resource component will be installed with the target
     * "head".
     *
     * @param context the <code>FacesContext</code> for the current request
     */
    public static void installFacesJsIfNecessary(FacesContext context) {

        if (isFacesJsInstalled(context)) {
            return;
        }

        ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
        if (resourceHandler.isResourceRendered(context, FACES_SCRIPT_RESOURCE_NAME, FACES_SCRIPT_LIBRARY_NAME)) {
            return;
        }

        context.getViewRoot().addComponentResource(context, createFacesJs(), "head");
    }

    /**
     * <p>
     * Renders the Javascript necessary to add and remove request parameters to the current form.
     * </p>
     *
     * @param context the <code>FacesContext</code> for the current request
     * @throws java.io.IOException if an error occurs writing to the response
     */
    public static void renderFacesJsIfNecessary(FacesContext context) throws IOException {

        if (isFacesJsInstalled(context)) {
            return;
        }

        ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
        if (resourceHandler.isResourceRendered(context, FACES_SCRIPT_RESOURCE_NAME, FACES_SCRIPT_LIBRARY_NAME)) {
            return;
        }

        // Since we've now determined that it's not in the page, we need to manually render it.
        createFacesJs().encodeAll(context);
        resourceHandler.markResourceRendered(context, FACES_SCRIPT_RESOURCE_NAME, FACES_SCRIPT_LIBRARY_NAME);
    }

    public static boolean isFacesJsInstalled(FacesContext context) {

        if (RequestStateManager.containsKey(context, RequestStateManager.SCRIPT_STATE)) {
            return true;
        }

        UIViewRoot viewRoot = context.getViewRoot();

        for (UIComponent resource : viewRoot.getComponentResources(context)) {
            Object name = resource.getAttributes().get("name");
            Object library = resource.getAttributes().get("library");

            if (FACES_SCRIPT_RESOURCE_NAME.equals(name) && FACES_SCRIPT_LIBRARY_NAME.equals(library)) {
                RequestStateManager.set(context, RequestStateManager.SCRIPT_STATE, true);
                return true;
            }
        }

        return false;
    }

    public static void renderUnhandledMessages(FacesContext ctx) {

        if (ctx.isProjectStage(ProjectStage.Development)) {
            Application app = ctx.getApplication();
            HtmlMessages messages = (HtmlMessages) app.createComponent(HtmlMessages.COMPONENT_TYPE);
            messages.setId(DEVELOPMENT_STAGE_MESSAGES_ID);
            Renderer messagesRenderer = ctx.getRenderKit().getRenderer(HtmlMessages.COMPONENT_FAMILY, "jakarta.faces.Messages");
            messages.setErrorStyle("Color: red");
            messages.setWarnStyle("Color: orange");
            messages.setInfoStyle("Color: blue");
            messages.setFatalStyle("Color: red");
            messages.setTooltip(true);
            messages.setTitle("Project Stage[Development]: Unhandled Messages");
            messages.setRedisplay(false);
            try {
                messagesRenderer.encodeBegin(ctx, messages);
                messagesRenderer.encodeEnd(ctx, messages);
            } catch (IOException ioe) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, ioe.toString(), ioe);
                }
            }
        } else {
            Iterator<String> clientIds = ctx.getClientIdsWithMessages();
            int messageCount = 0;
            if (clientIds.hasNext()) {
                // Display each message possibly not displayed.
                StringBuilder builder = new StringBuilder();
                while (clientIds.hasNext()) {
                    String clientId = clientIds.next();
                    Iterator<FacesMessage> messages = ctx.getMessages(clientId);
                    while (messages.hasNext()) {
                        FacesMessage message = messages.next();
                        if (message.isRendered()) {
                            continue;
                        }
                        messageCount++;
                        builder.append("\n");
                        builder.append("sourceId=").append(clientId);
                        builder.append("[severity=(").append(message.getSeverity());
                        builder.append("), summary=(").append(message.getSummary());
                        builder.append("), detail=(").append(message.getDetail()).append(")]");
                    }
                }
                if (messageCount > 0) {
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.log(Level.INFO, "faces.non_displayed_message", builder.toString());
                    }
                }
            }
        }

    }

    public static void renderHtmlErrorPage(FacesContext ctx, FacesException fe) {

        ExternalContext extContext = ctx.getExternalContext();
        if (!extContext.isResponseCommitted()) {
            extContext.setResponseContentType("text/html; charset=UTF-8");
            try {
                Writer w = extContext.getResponseOutputWriter();
                if (ctx.isProjectStage(ProjectStage.Development)) {
                    DevTools.debugHtml(w, ctx, fe.getCause());
                } else {
                    w.write("Please see your server log for the actual error");
                }
                w.flush();
            } catch (IOException ioe) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "Unable to generate Facelets error page.", ioe);
                }
            }
            ctx.responseComplete();
        } else {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "faces.facelets.error.page.response.committed");
            }
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, fe.toString(), fe);
            }
        }

    }

    // Check the request parameters to see whether an action event has
    // been triggered either via faces.ajax.request() or via a submitting
    // behavior.
    public static boolean isPartialOrBehaviorAction(FacesContext context, String clientId) {
        if (clientId == null || clientId.length() == 0) {
            return false;
        }

        String source = BEHAVIOR_SOURCE_PARAM.getValue(context);
        if (!clientId.equals(source)) {
            return false;
        }

        // First check for a Behavior action event.
        String behaviorEvent = BEHAVIOR_EVENT_PARAM.getValue(context);
        if (null != behaviorEvent) {
            return "action".equals(behaviorEvent);
        }

        // Not a Behavior-related request. Check for faces.ajax.request()
        // request params.
        String partialEvent = PARTIAL_EVENT_PARAM.getValue(context);

        return "click".equals(partialEvent);
    }

    /**
     * <p>
     * Utility method to return the client ID of the parent form.
     * </p>
     *
     * @param component typically a command component
     * @param context the <code>FacesContext</code> for the current request
     *
     * @return the client ID of the parent form, if any
     */
    public static String getFormClientId(UIComponent component, FacesContext context) {
        UIForm form = getForm(component, context);
        if (form != null) {
            return form.getClientId(context);
        }

        return null;
    }

    /**
     * <p>
     * Utility method to return the client ID of the parent form.
     * </p>
     *
     * @param component typically a command component
     * @param context the <code>FacesContext</code> for the current request
     *
     * @return the parent form, if any
     */
    public static UIForm getForm(UIComponent component, FacesContext context) {

        UIComponent parent = component.getParent();
        while (parent != null) {
            if (parent instanceof UIForm) {
                break;
            }
            parent = parent.getParent();
        }

        UIForm form = (UIForm) parent;
        if (form != null) {
            return form;
        }

        return null;
    }

    /**
     * <p>
     * Determine the path value of an image value for a component such as UIGraphic or UICommand.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request.
     * @param component the component to obtain the image information from
     * @param attrName the attribute name that needs to be queried if the name and library attributes are not specified
     *
     * @return the encoded path to the image source
     */
    public static String getImageSource(FacesContext context, UIComponent component, String attrName) {

        String resName = (String) component.getAttributes().get("name");
        ResourceHandler handler = context.getApplication().getResourceHandler();
        if (resName != null) {
            String libName = (String) component.getAttributes().get("library");
            
            if (libName == null && ApplicationAssociate.getInstance(context).getResourceManager().isContractsResource(resName)) {
                if (context.isProjectStage(ProjectStage.Development)) {
                    String msg = "Illegal path, direct contract references are not allowed: " + resName;
                    context.addMessage(component.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
                }
                return "RES_NOT_FOUND";
            }

            Resource res = handler.createResource(resName, libName);
            if (res == null) {
                if (context.isProjectStage(ProjectStage.Development)) {
                    String msg = "Unable to find resource " + (libName == null ? "" : libName + ", ") + resName;
                    context.addMessage(component.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
                }
                return "RES_NOT_FOUND";
            } else {
                String requestPath = res.getRequestPath();
                return context.getExternalContext().encodeResourceURL(requestPath);
            }
        } else {

            String value = (String) component.getAttributes().get(attrName);
            if (value == null || value.length() == 0) {
                return "";
            }
            WebConfiguration webConfig = WebConfiguration.getInstance();
            if (value.startsWith(webConfig.getOptionValue(WebConfiguration.WebContextInitParameter.WebAppContractsDirectory))) {
                if (context.isProjectStage(ProjectStage.Development)) {
                    String msg = "Illegal path, direct contract references are not allowed: " + value;
                    context.addMessage(component.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
                }
                return "RES_NOT_FOUND";
            }

            if (handler.isResourceURL(value)) {
                return value;
            } else {
                value = context.getApplication().getViewHandler().getResourceURL(context, value);
                return context.getExternalContext().encodeResourceURL(value);
            }
        }

    }

    /**
     * If view root is instance of naming container, prepend its container client id to namespace given parameter name.
     *
     * @param context Involved faces context.
     * @param name Request parameter name.
     * @return The request parameter name, if necessary namespaced.
     */
    public static String getParameterName(FacesContext context, String name) {
        return Util.getNamingContainerPrefix(context) + name;
    }

    /**
     * Returns <code>true</code> if the view root associated with the given faces context will be rendered with a HTML5 doctype.
     * @param context Involved faces context.
     * @return <code>true</code> if the view root associated with the given faces context will be rendered with a HTML5 doctype.
     */
    public static boolean isOutputHtml5Doctype(FacesContext context) {
        UIViewRoot viewRoot = context.getViewRoot();

        if (viewRoot == null) {
            return false;
        }

        Doctype doctype = viewRoot.getDoctype();

        if (doctype == null) {
            return false;
        }

        return "html".equalsIgnoreCase(doctype.getRootElement())
            && doctype.getPublic() == null
            && doctype.getSystem() == null;
    }

    // --------------------------------------------------------- Private Methods

    // Appends a script to a faces.util.chain() call
    private static void appendScriptToChain(StringBuilder builder, String script) {

        if (script == null || script.length() == 0) {
            return;
        }

        if (builder.length() == 0) {
            builder.append("faces.util.chain(this,event,");
        }

        if (builder.charAt(builder.length() - 1) != ',') {
            builder.append(',');
        }

        appendQuotedValue(builder, script);
    }

    // Appends an name/value property pair to a JSON object. Assumes
    // object has already been opened by the caller. The value will
    // be quoted (ie. wrapped in single quotes and escaped appropriately).
    public static void appendProperty(StringBuilder builder, String name, Object value) {
        appendProperty(builder, name, value, true);
    }

    // Appends an name/value property pair to a JSON object. Assumes
    // object has already been opened by the caller.
    public static void appendProperty(StringBuilder builder, String name, Object value, boolean quoteValue) {

        if (null == name || name.length() == 0) {
            throw new IllegalArgumentException();
        }

        char lastChar = builder.charAt(builder.length() - 1);
        if (lastChar != ',' && lastChar != '{') {
            builder.append(',');
        }

        RenderKitUtils.appendQuotedValue(builder, name);
        builder.append(":");

        if (value == null) {
            builder.append("''");
        } else if (quoteValue) {
            RenderKitUtils.appendQuotedValue(builder, value.toString());
        } else {
            builder.append(value.toString());
        }
    }

    // Append a script to the chain, escaping any single quotes, since
    // our script content is itself nested within single quotes.
    public static void appendQuotedValue(StringBuilder builder, String script) {

        builder.append("'");

        int length = script.length();

        for (int i = 0; i < length; i++) {
            char c = script.charAt(i);

            if (c == '\'' || c == '\\') {
                builder.append('\\');
            }

            builder.append(c);
        }

        builder.append("'");
    }

    // Appends one or more behavior scripts a faces.util.chain() call
    private static boolean appendBehaviorsToChain(StringBuilder builder, FacesContext context, UIComponent component, List<ClientBehavior> behaviors,
            String behaviorEventName, Collection<ClientBehaviorContext.Parameter> params) {

        if (behaviors == null || behaviors.isEmpty()) {
            return false;
        }

        ClientBehaviorContext bContext = createClientBehaviorContext(context, component, behaviorEventName, params);

        boolean submitting = false;

        for (ClientBehavior behavior : behaviors) {
            String script = behavior.getScript(bContext);
            if (script != null && script.length() > 0) {
                appendScriptToChain(builder, script);

                if (isSubmitting(behavior)) {
                    submitting = true;
                }
            }
        }

        return submitting;
    }

    // Given a behaviors Map with a single entry, returns the event name
    // for that entry. Or, if no entries, returns null. Used by
    // renderPassThruAttributesOptimized.
    private static String getSingleBehaviorEventName(Map<String, List<ClientBehavior>> behaviors) {
        assert behaviors != null;

        int size = behaviors.size();
        if (size == 0) {
            return null;
        }

        // If we made it this far, we should have a single
        // entry in the behaviors map.
        assert size == 1;

        Iterator<String> keys = behaviors.keySet().iterator();
        assert keys.hasNext();

        return keys.next();
    }

    // Tests whether the specified Attribute matches to specified
    // behavior event name. Used by renderPassThruAttributesOptimized.
    private static boolean isBehaviorEventAttribute(Attribute attr, String behaviorEventName) {

        String[] events = attr.getEvents();

        return behaviorEventName != null && events != null && events.length > 0 && behaviorEventName.equals(events[0]);
    }

    // Ensures that the user-specified DOM event handler script
    // is non-empty, and trimmed if necessary.
    private static String getNonEmptyUserHandler(Object handlerObject) {

        String handler = null;

        if (null != handlerObject) {
            handler = handlerObject.toString();
            handler = handler.trim();

            if (handler.length() == 0) {
                handler = null;
            }
        }

        return handler;
    }

    // Returns the Behaviors for the specified component/event name,
    // or null if no Behaviors are available
    private static List<ClientBehavior> getClientBehaviors(UIComponent component, String behaviorEventName) {

        if (component instanceof ClientBehaviorHolder) {
            ClientBehaviorHolder bHolder = (ClientBehaviorHolder) component;
            Map<String, List<ClientBehavior>> behaviors = bHolder.getClientBehaviors();
            if (null != behaviors) {
                return behaviors.get(behaviorEventName);
            }
        }

        return null;
    }

    // Returns a submit handler - ie. a script that calls
    // mojara.cljs()
    private static String getSubmitHandler(FacesContext context, UIComponent component, Collection<ClientBehaviorContext.Parameter> params, String submitTarget,
            boolean preventDefault) {

        StringBuilder builder = new StringBuilder(256);

        String formClientId = getFormClientId(component, context);
        String componentClientId = component.getClientId(context);

        builder.append("mojarra.cljs(document.getElementById('");
        builder.append(formClientId);
        builder.append("'),{");

        appendProperty(builder, componentClientId, componentClientId);

        if (null != params && !params.isEmpty()) {
            for (ClientBehaviorContext.Parameter param : params) {
                appendProperty(builder, getParameterName(context, param.getName()), param.getValue());
            }
        }

        builder.append("},'");

        if (submitTarget != null) {
            builder.append(submitTarget);
        }

        builder.append("')");

        if (preventDefault) {
            builder.append(";return false");
        }

        return builder.toString();
    }

    // Chains together a number of Behavior scripts with a user handler
    // script.
    private static String getChainedHandler(FacesContext context, UIComponent component, List<ClientBehavior> behaviors,
            Collection<ClientBehaviorContext.Parameter> params, String behaviorEventName, String userHandler, String submitTarget, boolean needsSubmit) {

        // Hard to pre-compute builder initial capacity
        StringBuilder builder = new StringBuilder(100);

        appendScriptToChain(builder, userHandler);

        boolean submitting = appendBehaviorsToChain(builder, context, component, behaviors, behaviorEventName, params);

        boolean hasParams = null != params && !params.isEmpty();

        // If we've got parameters but we didn't render a "submitting"
        // behavior script, we need to explicitly render a submit script.
        if (!submitting && (hasParams || needsSubmit)) {
            String submitHandler = getSubmitHandler(context, component, params, submitTarget, false);

            appendScriptToChain(builder, submitHandler);

            // We are now submitting since we've rendered a submit script.
            submitting = true;
        }

        if (builder.length() == 0) {
            return null;
        }

        builder.append(")");

        // If we're submitting (either via a behavior, or by rendering
        // a submit script), we need to return false to prevent the
        // default button/link action.
        if (submitting && ("action".equals(behaviorEventName) || "click".equals(behaviorEventName))) {
            builder.append(";return false");
        }

        return builder.toString();
    }

    // Returns the script for a single Behavior
    private static String getSingleBehaviorHandler(FacesContext context, UIComponent component, ClientBehavior behavior,
            Collection<ClientBehaviorContext.Parameter> params, String behaviorEventName, String submitTarget, boolean needsSubmit) {

        ClientBehaviorContext bContext = createClientBehaviorContext(context, component, behaviorEventName, params);

        String script = behavior.getScript(bContext);

        boolean preventDefault = (needsSubmit || isSubmitting(behavior)) && (component instanceof ActionSource || component instanceof ActionSource2);

        if (script == null) {
            if (needsSubmit) {
                script = getSubmitHandler(context, component, params, submitTarget, preventDefault);
            }
        } else if (preventDefault) {
            script = script + ";return false";
        }

        return script;
    }

    // Creates a ClientBehaviorContext with the specified properties.
    private static ClientBehaviorContext createClientBehaviorContext(FacesContext context, UIComponent component, String behaviorEventName,
            Collection<ClientBehaviorContext.Parameter> params) {

        return ClientBehaviorContext.createClientBehaviorContext(context, component, behaviorEventName, null, params);
    }

    // Tests whether the specified behavior is submitting
    private static boolean isSubmitting(ClientBehavior behavior) {
        return behavior.getHints().contains(ClientBehaviorHint.SUBMITTING);
    }

    /**
     * Renders a handler script, which may require chaining together the user-specified event handler, any scripts required
     * by attached Behaviors, and also possibly the mojarra.cljs() "submit" script.
     *
     * @param context the FacesContext for this request.
     * @param component the UIComponent that we are rendering
     * @param params any parameters that should be included by "submitting" scripts.
     * @param handlerName the name of the handler attribute to render (eg. "onclick" or "ommouseover")
     * @param handlerValue the user-specified value for the handler attribute
     * @param behaviorEventName the name of the behavior event that corresponds to this handler (eg. "action" or
     * "mouseover").
     * @param needsSubmit indicates whether the mojarra.cljs() "submit" script is required by the component. Most
     * components do not need this, either because they submit themselves (eg. commandButton), or because they do not
     * perform submits (eg. non-command components). This flag is mainly here for the commandLink case, where we need to
     * render the submit script to make the link submit.
     */
    private static void renderHandler(FacesContext context, UIComponent component, Collection<ClientBehaviorContext.Parameter> params, String handlerName,
            Object handlerValue, String behaviorEventName, String submitTarget, boolean needsSubmit, boolean includeExec) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        String userHandler = getNonEmptyUserHandler(handlerValue);
        List<ClientBehavior> behaviors = getClientBehaviors(component, behaviorEventName);

        // Don't render behavior scripts if component is disabled
        if (null != behaviors && behaviors.size() > 0 && Util.componentIsDisabled(component)) {
            behaviors = null;
        }

        if (params == null) {
            params = Collections.emptyList();
        }
        String handler = null;
        switch (getHandlerType(behaviors, params, userHandler, needsSubmit, includeExec)) {

        case USER_HANDLER_ONLY:
            handler = userHandler;
            break;

        case SINGLE_BEHAVIOR_ONLY:
            handler = getSingleBehaviorHandler(context, component, behaviors.get(0), params, behaviorEventName, submitTarget, needsSubmit);
            break;

        case SUBMIT_ONLY:
            handler = getSubmitHandler(context, component, params, submitTarget, true);
            break;

        case CHAIN:
            handler = getChainedHandler(context, component, behaviors, params, behaviorEventName, userHandler, submitTarget, needsSubmit);
            break;
        default:
            assert false;
        }

        writer.writeAttribute(handlerName, handler, null);
    }

    // Determines the type of handler to render based on what sorts of
    // scripts we need to render/chain.
    private static HandlerType getHandlerType(List<ClientBehavior> behaviors, Collection<ClientBehaviorContext.Parameter> params, String userHandler,
            boolean needsSubmit, boolean includeExec) {

        if (behaviors == null || behaviors.isEmpty()) {

            // No behaviors and no params means user handler only,
            // if we have a param only because of includeExec while having
            // no behaviors, also, user handler only
            if (params.isEmpty() && !needsSubmit || includeExec) {
                return HandlerType.USER_HANDLER_ONLY;
            }

            // We've got params. If we've also got a user handler, we need
            // to chain. Otherwise, we only render the submit script.
            return userHandler == null ? HandlerType.SUBMIT_ONLY : HandlerType.CHAIN;
        }

        // We've got behaviors. See if we can optimize for the single
        // behavior case. We can only do this if we don't have a user
        // handler.
        if (behaviors.size() == 1 && userHandler == null) {
            ClientBehavior behavior = behaviors.get(0);

            // If we've got a submitting behavior, then it will handle
            // submitting the params. If not, then we need to use
            // a submit script to handle the params.
            if (isSubmitting(behavior) || params.isEmpty() && !needsSubmit) {
                return HandlerType.SINGLE_BEHAVIOR_ONLY;
            }
        }

        return HandlerType.CHAIN;
    }

    // Little utility enum that we use to identify the type of
    // handler that we are going to render.
    private enum HandlerType {

        // Indicates that we only have a user handler - nothing else
        USER_HANDLER_ONLY,

        // Indicates that we only have a single behavior - no chaining
        SINGLE_BEHAVIOR_ONLY,

        // Indicates that we only render the mojarra.cljs() script
        SUBMIT_ONLY,

        // Indicates that we've got a chain
        CHAIN
    }

    // ---------------------------------------------------------- Nested Classes

} // END RenderKitUtils
