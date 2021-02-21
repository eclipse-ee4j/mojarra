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

package com.sun.faces.push;

import static com.sun.faces.push.WebsocketChannelManager.EMPTY_SCOPE;
import static com.sun.faces.push.WebsocketChannelManager.getChannelId;
import static com.sun.faces.push.WebsocketChannelManager.getSessionScope;
import static com.sun.faces.push.WebsocketChannelManager.getViewScope;
import static java.util.Collections.singleton;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.sun.faces.cdi.CdiUtils;
import com.sun.faces.util.Json;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;

/**
 * <p class="changed_added_2_3">
 * This is a concrete implementation of {@link PushContext} interface which is to be injected by
 * <code>&#64;</code>{@link Push}.
 *
 * @author Bauke Scholtz
 * @see Push
 * @since 2.3
 */
public class WebsocketPushContext implements PushContext {

    // Constants ------------------------------------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;

    // Variables ------------------------------------------------------------------------------------------------------

    private String channel;
    private Map<String, String> sessionScope;
    private Map<String, String> viewScope;
    private WebsocketSessionManager socketSessions;
    private WebsocketUserManager socketUsers;

    // Constructors ---------------------------------------------------------------------------------------------------

    /**
     * Creates a socket push context whereby the mutable map of session and view scope channel identifiers is referenced, so
     * it's still available when another thread invokes {@link #send(Object)} during which the session and view scope is not
     * necessarily active anymore.
     */
    public WebsocketPushContext(String channel, WebsocketSessionManager socketSessions, WebsocketUserManager socketUsers) {
        this.channel = channel;
        boolean hasSession = CdiUtils.isScopeActive(SessionScoped.class);
        sessionScope = hasSession ? getSessionScope() : EMPTY_SCOPE;
        viewScope = hasSession && FacesContext.getCurrentInstance() != null ? getViewScope(true) : EMPTY_SCOPE;
        this.socketSessions = socketSessions;
        this.socketUsers = socketUsers;
    }

    // Actions --------------------------------------------------------------------------------------------------------

    @Override
    public Set<Future<Void>> send(Object message) {
        return socketSessions.send(getChannelId(channel, sessionScope, viewScope), Json.encode(message));
    }

    @Override
    public <S extends Serializable> Set<Future<Void>> send(Object message, S user) {
        return send(message, singleton(user)).get(user);
    }

    @Override
    public <S extends Serializable> Map<S, Set<Future<Void>>> send(Object message, Collection<S> users) {
        Map<S, Set<Future<Void>>> resultsByUser = new HashMap<>(users.size());
        String json = Json.encode(message);

        for (S user : users) {
            Set<String> channelIds = socketUsers.getChannelIds(user, channel);
            Set<Future<Void>> results = new HashSet<>(channelIds.size());

            for (String channelId : channelIds) {
                results.addAll(socketSessions.send(channelId, json));
            }

            resultsByUser.put(user, results);
        }

        return resultsByUser;
    }

}
