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

import static jakarta.faces.component.UINamingContainer.getSeparatorChar;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.facelets.tag.composite.RetargetedAjaxBehavior;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.html.HtmlCommandScript;
import jakarta.faces.component.search.ComponentNotFoundException;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.component.search.SearchExpressionHint;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.render.ClientBehaviorRenderer;

/*
 *<b>AjaxBehaviorRenderer</b> renders Ajax behavior for a component.
 * It also
 */

public class AjaxBehaviorRenderer extends ClientBehaviorRenderer {

    // Log instance for this class
    protected static final Logger logger = FacesLogger.RENDERKIT.getLogger();

    // ------------------------------------------------------ Rendering Methods

    @Override
    public String getScript(ClientBehaviorContext behaviorContext, ClientBehavior behavior) {
        if (!(behavior instanceof AjaxBehavior)) {
            // TODO: use MessageUtils for this error message?
            throw new IllegalArgumentException("Instance of jakarta.faces.component.behavior.AjaxBehavior required: " + behavior);
        }

        if (((AjaxBehavior) behavior).isDisabled()) {
            return null;
        }
        return buildAjaxCommand(behaviorContext, (AjaxBehavior) behavior);
    }

    @Override
    public void decode(FacesContext context, UIComponent component, ClientBehavior behavior) {
        if (null == context || null == component || null == behavior) {
            throw new NullPointerException();
        }

        if (!(behavior instanceof AjaxBehavior)) {
            // TODO: use MessageUtils for this error message?
            throw new IllegalArgumentException("Instance of jakarta.faces.component.behavior.AjaxBehavior required: " + behavior);
        }

        AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;

        // First things first - if AjaxBehavior is disabled, we are done.
        if (ajaxBehavior.isDisabled()) {
            return;
        }

        component.queueEvent(createEvent(context, component, ajaxBehavior));

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("This command resulted in form submission " + " AjaxBehaviorEvent queued.");
            logger.log(Level.FINE, "End decoding component {0}", component.getId());
        }

    }

    // Creates an AjaxBehaviorEvent for the specified component/behavior
    private static AjaxBehaviorEvent createEvent(FacesContext facesContext, UIComponent component, AjaxBehavior ajaxBehavior) {

        AjaxBehaviorEvent event = new AjaxBehaviorEvent(facesContext, component, ajaxBehavior);

        PhaseId phaseId = isImmediate(component, ajaxBehavior) ? PhaseId.APPLY_REQUEST_VALUES : PhaseId.INVOKE_APPLICATION;

        event.setPhaseId(phaseId);

        return event;
    }

    // Tests whether we should perform immediate processing. Note
    // that we "inherit" immediate from the parent if not specified
    // on the behavior.
    private static boolean isImmediate(UIComponent component, AjaxBehavior ajaxBehavior) {

        boolean immediate = false;

        if (ajaxBehavior.isImmediateSet()) {
            immediate = ajaxBehavior.isImmediate();
        } else if (component instanceof EditableValueHolder) {
            immediate = ((EditableValueHolder) component).isImmediate();
        } else if (component instanceof ActionSource) {
            immediate = ((ActionSource) component).isImmediate();
        }

        return immediate;
    }

    private static String buildAjaxCommand(ClientBehaviorContext behaviorContext, AjaxBehavior ajaxBehavior) {

        // First things first - if AjaxBehavior is disabled, we are done.
        if (ajaxBehavior.isDisabled()) {
            return null;
        }

        UIComponent component = behaviorContext.getComponent();
        String eventName = behaviorContext.getEventName();

        StringBuilder ajaxCommand = new StringBuilder(256);
        Collection<String> execute = ajaxBehavior.getExecute();
        Collection<String> render = ajaxBehavior.getRender();
        String onevent = ajaxBehavior.getOnevent();
        String onerror = ajaxBehavior.getOnerror();
        String sourceId = behaviorContext.getSourceId();
        String delay = ajaxBehavior.getDelay();
        Boolean resetValues = null;
        if (ajaxBehavior.isResetValuesSet()) {
            resetValues = ajaxBehavior.isResetValues();
        }
        Collection<ClientBehaviorContext.Parameter> params = behaviorContext.getParameters();

        // Needed workaround for SelectManyCheckbox - if execute doesn't have sourceId,
        // we need to add it - otherwise, we use the default, which is sourceId:child, which
        // won't work.
        ClientBehaviorContext.Parameter foundparam = null;
        for (ClientBehaviorContext.Parameter param : params) {
            if (param.getName().equals("incExec") && (Boolean) param.getValue()) {
                foundparam = param;
            }
        }
        if (foundparam != null && !execute.contains(sourceId)) {
            execute = new LinkedList<>(execute);
            execute.add(component.getClientId());
        }
        if (foundparam != null) {
            try {
                // And since this is a hack, we now try to remove the param
                params.remove(foundparam);
            } catch (UnsupportedOperationException uoe) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Unsupported operation", uoe);
                }
            }
        }

        HtmlCommandScript commandScript = component instanceof HtmlCommandScript ? (HtmlCommandScript) component : null;

        if (commandScript != null) {
            String name = commandScript.getName();

            if (!name.contains(".")) {
                ajaxCommand.append("var ");
            }

            ajaxCommand.append(name).append('=').append("function(o){var o=(typeof o==='object')&&o?o:{};");

            for (ClientBehaviorContext.Parameter param : params) {
                ajaxCommand.append("o[");
                RenderKitUtils.appendQuotedValue(ajaxCommand, param.getName());
                ajaxCommand.append("]=");
                Object paramValue = param.getValue();

                if (paramValue == null) {
                    ajaxCommand.append("null");
                } else {
                    RenderKitUtils.appendQuotedValue(ajaxCommand, paramValue.toString());
                }

                ajaxCommand.append(";");
            }

            params = Collections.singleton(new ClientBehaviorContext.Parameter("o", null));
        }

        ajaxCommand.append("mojarra.ab(");

        if (sourceId == null) {
            ajaxCommand.append("this");
        } else {
            ajaxCommand.append("'");
            ajaxCommand.append(sourceId);
            ajaxCommand.append("'");
        }

        ajaxCommand.append(",");
        ajaxCommand.append(commandScript == null ? "event" : "null");
        ajaxCommand.append(",'");
        ajaxCommand.append(eventName);
        ajaxCommand.append("',");

        appendIds(behaviorContext.getFacesContext(), component, ajaxBehavior, ajaxCommand, execute);
        ajaxCommand.append(",");
        appendIds(behaviorContext.getFacesContext(), component, ajaxBehavior, ajaxCommand, render);

        if (onevent != null || onerror != null || delay != null || resetValues != null || !params.isEmpty()) {

            ajaxCommand.append(",{");

            if (onevent != null) {
                RenderKitUtils.appendProperty(ajaxCommand, "onevent", onevent, false);
            }

            if (onerror != null) {
                RenderKitUtils.appendProperty(ajaxCommand, "onerror", onerror, false);
            }

            if (delay != null) {
                RenderKitUtils.appendProperty(ajaxCommand, "delay", delay, true);
            }

            if (resetValues != null) {
                RenderKitUtils.appendProperty(ajaxCommand, "resetValues", resetValues, false);
            }

            if (!params.isEmpty()) {
                if (commandScript != null) {
                    RenderKitUtils.appendProperty(ajaxCommand, "params", params.iterator().next().getName(), false);
                } else {
                    RenderKitUtils.appendProperty(ajaxCommand, "params", "{", false);

                    for (ClientBehaviorContext.Parameter param : params) {
                        RenderKitUtils.appendProperty(ajaxCommand, param.getName(), param.getValue());
                    }

                    ajaxCommand.append("}");
                }

            }

            ajaxCommand.append("}");
        }

        ajaxCommand.append(")");

        if (commandScript != null) {
            ajaxCommand.append("}");

            if (commandScript.isAutorun()) {
                ajaxCommand.append(";mojarra.l(").append(commandScript.getName()).append(")");
            }
        }

        return ajaxCommand.toString();
    }

    private static final Set<SearchExpressionHint> EXPRESSION_HINTS = EnumSet.of(SearchExpressionHint.RESOLVE_CLIENT_SIDE,
            SearchExpressionHint.RESOLVE_SINGLE_COMPONENT);

    // Appends an ids argument to the ajax command
    private static void appendIds(FacesContext facesContext, UIComponent component, AjaxBehavior ajaxBehavior, StringBuilder builder, Collection<String> ids) {

        if (null == ids || ids.isEmpty()) {
            builder.append('0');
            return;
        }

        builder.append("'");

        SearchExpressionHandler handler = null;
        SearchExpressionContext searchExpressionContext = null;

        boolean first = true;

        UIComponent composite = UIComponent.getCompositeComponentParent(component);
        String separatorChar = String.valueOf(getSeparatorChar(facesContext));

        for (String id : ids) {
            String expression = id.trim();

            if (expression.length() == 0) {
                continue;
            }
            if (!first) {
                builder.append(' ');
            } else {
                first = false;
            }

            boolean clientResolveableExpression = expression.equals("@all") || expression.equals("@none") || expression.equals("@form") || expression.equals("@this");

            if (composite != null && (ajaxBehavior instanceof RetargetedAjaxBehavior) && (expression.equals("@this") || expression.startsWith("@this" + separatorChar))) {
                expression = expression.replaceFirst("@this", separatorChar + composite.getClientId(facesContext));
                clientResolveableExpression = false;
            }

            if (clientResolveableExpression) {
                builder.append(expression);
            } else {
                if (searchExpressionContext == null) {
                    searchExpressionContext = SearchExpressionContext.createSearchExpressionContext(facesContext, component, EXPRESSION_HINTS, null);
                }
                if (handler == null) {
                    handler = facesContext.getApplication().getSearchExpressionHandler();
                }
                String resolvedClientId = null;
                try {
                    resolvedClientId = handler.resolveClientId(searchExpressionContext, expression);
                } catch (ComponentNotFoundException cnfe) {
                    if (composite != null && !expression.startsWith(separatorChar) && composite.getParent() != null && composite.getParent().getNamingContainer() != null) {
                        expression = composite.getParent().getNamingContainer().getClientId(facesContext) + separatorChar + expression;

                        try {
                            resolvedClientId = handler.resolveClientId(searchExpressionContext, expression);
                        } catch (ComponentNotFoundException ignore) {
                            resolvedClientId = getResolvedId(component, expression);
                        }
                    } else {
                        resolvedClientId = getResolvedId(component, expression);
                    }
                }
                builder.append(resolvedClientId);
            }
        }
        builder.append("'");
    }

    // Returns the resolved (client id) for a particular id.
    private static String getResolvedId(UIComponent component, String id) {

        UIComponent resolvedComponent = component.findComponent(id);
        if (resolvedComponent == null) {
            if (id.charAt(0) == UINamingContainer.getSeparatorChar(FacesContext.getCurrentInstance())) {
                return id.substring(1);
            }
            return id;
        }

        return resolvedComponent.getClientId();
    }
}
