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

package com.sun.faces.push;

import static com.sun.faces.cdi.CdiUtils.getBeanReference;
import static com.sun.faces.push.WebsocketEndpoint.PARAM_CHANNEL;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.logging.Level.WARNING;
import static javax.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.util.AnnotationLiteral;
import javax.faces.context.FacesContext;
import javax.faces.event.WebsocketEvent;
import javax.faces.event.WebsocketEvent.Closed;
import javax.faces.event.WebsocketEvent.Opened;
import javax.faces.push.Push;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.Session;

import com.sun.faces.util.Util;

/**
 * <p class="changed_added_2_3">
 * This web socket session manager holds all web socket sessions by their channel identifier.
 *
 * @author Bauke Scholtz
 * @see Push
 * @since 2.3
 */
@ApplicationScoped
public class WebsocketSessionManager {

    // Constants ------------------------------------------------------------------------------------------------------

    private static final Logger logger = Logger.getLogger(WebsocketSessionManager.class.getName());

    private static final CloseReason REASON_EXPIRED = new CloseReason(NORMAL_CLOSURE, "Expired");
    private static final AnnotationLiteral<Opened> SESSION_OPENED = new AnnotationLiteral<Opened>() {
        private static final long serialVersionUID = 1L;
    };
    private static final AnnotationLiteral<Closed> SESSION_CLOSED = new AnnotationLiteral<Closed>() {
        private static final long serialVersionUID = 1L;
    };

    private static final long TOMCAT_WEB_SOCKET_RETRY_TIMEOUT = 10; // Milliseconds.
    private static final long TOMCAT_WEB_SOCKET_MAX_RETRIES = 100; // So, that's retrying for about 1 second.
    private static final String WARNING_TOMCAT_WEB_SOCKET_BOMBED =
        "Tomcat cannot handle concurrent push messages."
            + " A push message has been sent only after %s retries of " + TOMCAT_WEB_SOCKET_RETRY_TIMEOUT + "ms apart."
            + " Consider rate limiting sending push messages. For example, once every 500ms.";
    private static final String ERROR_TOMCAT_WEB_SOCKET_BOMBED =
        "Tomcat cannot handle concurrent push messages."
            + " A push message could NOT be sent after %s retries of " + TOMCAT_WEB_SOCKET_RETRY_TIMEOUT + "ms apart."
            + " Consider rate limiting sending push messages. For example, once every 500ms.";

    // Properties -----------------------------------------------------------------------------------------------------

    private final ConcurrentMap<String, Collection<Session>> socketSessions = new ConcurrentHashMap<>();

    @Inject
    private WebsocketUserManager socketUsers;

    // Actions --------------------------------------------------------------------------------------------------------

    /**
     * Register given channel identifier.
     * @param channelId The channel identifier to register.
     */
    protected void register(String channelId) {
        if (!socketSessions.containsKey(channelId)) {
            socketSessions.putIfAbsent(channelId, new ConcurrentLinkedQueue<Session>());
        }
    }

    /**
     * Register given channel identifiers.
     * @param channelIds The channel identifiers to register.
     */
    protected void register(Iterable<String> channelIds) {
        for (String channelId : channelIds) {
            register(channelId);
        }
    }

    /**
     * On open, add given web socket session to the mapping associated with its channel identifier and returns
     * <code>true</code> if it's accepted (i.e. the channel identifier is known) and the same session hasn't been added
     * before, otherwise <code>false</code>.
     * @param session The opened web socket session.
     * @return <code>true</code> if given web socket session is accepted and is new, otherwise <code>false</code>.
     */
    protected boolean add(Session session) {
        String channelId = getChannelId(session);
        Collection<Session> sessions = socketSessions.get(channelId);

        if (sessions != null && sessions.add(session)) {
            Serializable user = socketUsers.getUser(getChannel(session), channelId);

            if (user != null) {
                session.getUserProperties().put("user", user);
            }

            fireEvent(session, null, SESSION_OPENED);
            return true;
        }

        return false;
    }

    /**
     * Encode the given message object as JSON and send it to all open web socket sessions associated with given web
     * socket channel identifier.
     * @param channelId The web socket channel identifier.
     * @param message The push message string.
     * @return The results of the send operation. If it returns an empty set, then there was no open session associated
     * with given channel identifier. The returned futures will return <code>null</code> on {@link Future#get()} if the
     * message was successfully delivered and otherwise throw {@link ExecutionException}.
     */
    protected Set<Future<Void>> send(String channelId, String message) {
        Collection<Session> sessions = (channelId != null) ? socketSessions.get(channelId) : null;

        if (sessions != null && !sessions.isEmpty()) {
            Set<Future<Void>> results = new HashSet<>(sessions.size());

            for (Session session : sessions) {
                if (session.isOpen()) {
                    results.add(send(session, message, true));
                }
            }

            return results;
        }

        return emptySet();
    }

    private Future<Void> send(Session session, String text, boolean retrySendTomcatWebSocket) {
        try {
            return session.getAsyncRemote().sendText(text);
        }
        catch (IllegalStateException e) {
            // Awkward workaround for Tomcat not willing to queue/synchronize asyncRemote().
            // https://bz.apache.org/bugzilla/show_bug.cgi?id=56026
            if (session.getClass().getName().startsWith("org.apache.tomcat.websocket.") && e.getMessage().contains("[TEXT_FULL_WRITING]")) {
                if (retrySendTomcatWebSocket) {
                    return CompletableFuture.supplyAsync(() -> retrySendTomcatWebSocket(session, text));
                }
                else {
                    return null;
                }
            }
            else {
                throw e;
            }
        }
    }

    private Void retrySendTomcatWebSocket(Session session, String text) {
        int retries = 0;
        Exception cause = null;

        while (++retries < TOMCAT_WEB_SOCKET_MAX_RETRIES) {
            try {
                Thread.sleep(TOMCAT_WEB_SOCKET_RETRY_TIMEOUT);

                if (!session.isOpen()) {
                    cause = new IllegalStateException("Too bad, session is now closed");
                    break;
                }

                Future<Void> result = send(session, text, false);

                if (result == null) {
                    continue;
                }

                if (logger.isLoggable(WARNING)) {
                    logger.log(WARNING, format(WARNING_TOMCAT_WEB_SOCKET_BOMBED, retries));
                }

                return result.get();
            }
            catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                cause = e;
                break;
            }
        }

        throw new UnsupportedOperationException(format(ERROR_TOMCAT_WEB_SOCKET_BOMBED, retries), cause);
    }

    /**
     * On close, remove given web socket session from the mapping.
     * @param session The closed web socket session.
     * @param reason The close reason.
     */
    protected void remove(Session session, CloseReason reason) {
        Collection<Session> sessions = socketSessions.get(getChannelId(session));

        if (sessions != null && sessions.remove(session)) {
            fireEvent(session, reason, SESSION_CLOSED);
        }
    }

    /**
     * Deregister given channel identifiers and explicitly close all open web socket sessions associated with it.
     * @param channelIds The channel identifiers to deregister.
     */
    protected void deregister(Iterable<String> channelIds) {
        for (String channelId : channelIds) {
            Collection<Session> sessions = socketSessions.remove(channelId);

            if (sessions != null) {
                for (Session session : sessions) {
                    if (session.isOpen()) {
                        try {
                            session.close(REASON_EXPIRED);
                        }
                        catch (IOException ignore) {
                            continue;
                        }
                    }
                }
            }
        }
    }

    // Internal -------------------------------------------------------------------------------------------------------

    private static volatile WebsocketSessionManager instance;

    /**
     * Internal usage only. Awkward workaround for it being unavailable via @Inject in endpoint in Tomcat+Weld/OWB.
     */
    static WebsocketSessionManager getInstance() {
        if (instance == null) {
            instance = getBeanReference(WebsocketSessionManager.class);
        }

        return instance;
    }

    // Helpers --------------------------------------------------------------------------------------------------------

    private static String getChannel(Session session) {
        return session.getPathParameters().get(PARAM_CHANNEL);
    }

    private static String getChannelId(Session session) {
        return session.getQueryString();
    }

    private static void fireEvent(Session session, CloseReason reason, AnnotationLiteral<?> qualifier) {
        Serializable user = (Serializable) session.getUserProperties().get("user");
        Util.getCdiBeanManager(FacesContext.getCurrentInstance()).fireEvent(new WebsocketEvent(getChannel(session), user, (reason != null) ? reason.getCloseCode() : null), qualifier);
    }

}
