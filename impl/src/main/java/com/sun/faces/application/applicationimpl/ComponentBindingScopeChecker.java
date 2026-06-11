/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package com.sun.faces.application.applicationimpl;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * Development-stage diagnostic that flags a component {@code binding} whose target retains the bound component across
 * requests &mdash; the tell-tale sign that the binding resolves to a bean broader than request scope (session,
 * application, view, conversation, flow, a {@code static} field, a session map, ...). Component bindings must be request
 * scoped per the Jakarta Faces specification section 3.1.5 (Component Bindings): a {@code UIComponent} instance belongs
 * to a single request's view tree and depends on running inside a single thread, so a wider scope causes duplicate
 * component id errors, stale state and concurrency hazards.
 * <p>
 * Detection is scope-agnostic and symptom-based: every binding-resolved component is stamped with the sequence number
 * of the request that first produced it, and a later request resolving the <em>same</em> instance is what triggers the
 * warning. It never inspects whether the getter returned {@code null}: a correctly request-scoped bean may legitimately
 * build its component in {@code @PostConstruct} (a non-null first getter), and that case must not be flagged. Only
 * instance identity surviving a request boundary is. The stamp is held in the request map (the servlet request, i.e. the
 * CDI request-scope boundary) rather than the {@code FacesContext}, so a single request that spans more than one
 * {@code FacesContext} (servlet forward/include, error dispatch) is correctly treated as one request.
 */
final class ComponentBindingScopeChecker {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    /**
     * Maps each binding-resolved component to the sequence number of the request that first produced it. A weak key
     * does not contribute to reachability, so this map never retains a component (or the tree it points at): a correctly
     * request-scoped binding's component dies with its view at end of request exactly as it would without the map, and
     * its now-cleared entry is expunged on the next access; only a too-broadly-scoped binding leaves a live entry, and
     * then because the offending bean keeps the component alive, not this map. The value is a plain {@code Long} so it
     * cannot resurrect the key. Comparison is by identity because {@link UIComponent} does not override
     * {@code equals}/{@code hashCode}. The map and the sequence are intentionally JVM-global (shared across web
     * applications); a dev diagnostic needs nothing more, since identity comparison and a monotonic sequence stay
     * correct regardless.
     */
    private static final Map<UIComponent, Long> FIRST_SEEN_REQUEST = Collections.synchronizedMap(new WeakHashMap<>());

    private static final AtomicLong REQUEST_SEQUENCE = new AtomicLong();

    private static final String REQUEST_SEQUENCE_KEY = ComponentBindingScopeChecker.class.getName() + ".REQUEST_SEQUENCE";

    private static final String MESSAGE_ID = "faces.component.binding.not.request.scoped";
    private static final String MESSAGE_SUMMARY_ID = MESSAGE_ID + "_summary";
    private static final String MESSAGE_DETAIL_ID = MESSAGE_ID + "_detail";

    private ComponentBindingScopeChecker() {
    }

    /**
     * Warns, in {@link ProjectStage#Development} only, when {@code component} (resolved from a binding expression) was
     * already produced by an earlier request. The warning is logged and, unless an identical one is already queued,
     * enqueued as a {@link FacesMessage} so it also surfaces on the page through {@code <h:messages>}.
     *
     * @param context the current faces context
     * @param bindingExpression the {@code binding} value expression that produced the component
     * @param component the resolved component, possibly {@code null}
     */
    static void check(FacesContext context, ValueExpression bindingExpression, UIComponent component) {
        if (component == null || !context.isProjectStage(ProjectStage.Development)) {
            return;
        }

        long currentRequest = currentRequestSequence(context);
        Long firstSeenRequest = FIRST_SEEN_REQUEST.put(component, currentRequest);

        if (firstSeenRequest != null && firstSeenRequest != currentRequest) {
            String expression = bindingExpression.getExpressionString();
            String summary = FacesLogger.APPLICATION.interpolateMessage(context, MESSAGE_SUMMARY_ID, new Object[] { expression });
            String detail = FacesLogger.APPLICATION.interpolateMessage(context, MESSAGE_DETAIL_ID, new Object[0]);
            LOGGER.log(Level.WARNING, MESSAGE_ID, new Object[] { summary, detail });

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, summary, detail);
            if (!alreadyQueued(context, message)) {
                context.addMessage(null, message);
            }
        }
    }

    private static boolean alreadyQueued(FacesContext context, FacesMessage message) {
        for (FacesMessage queued : context.getMessageList()) {
            if (message.getSummary().equals(queued.getSummary())) {
                return true;
            }
        }
        return false;
    }

    private static long currentRequestSequence(FacesContext context) {
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        Long sequence = (Long) requestMap.get(REQUEST_SEQUENCE_KEY);

        if (sequence == null) {
            sequence = REQUEST_SEQUENCE.incrementAndGet();
            requestMap.put(REQUEST_SEQUENCE_KEY, sequence);
        }

        return sequence;
    }
}
