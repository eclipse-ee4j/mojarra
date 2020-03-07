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

import static com.sun.faces.cdi.CdiUtils.getBeanInstance;
import static java.util.Collections.emptySet;
import static java.util.Collections.synchronizedSet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.push.Push;

/**
 * <p class="changed_added_2_3">
 * This web socket user manager holds all web socket users registered by <code>&lt;f:websocket&gt;</code>.
 *
 * @author Bauke Scholtz
 * @see Push
 * @since 2.3
 */
@ApplicationScoped
public class WebsocketUserManager {

    // Constants ------------------------------------------------------------------------------------------------------

    private static final int ESTIMATED_USER_CHANNELS_PER_APPLICATION = 1;
    private static final int ESTIMATED_USER_CHANNELS_PER_SESSION = 1;
    private static final int ESTIMATED_SESSIONS_PER_USER = 2;
    private static final int ESTIMATED_CHANNELS_IDS_PER_USER = ESTIMATED_SESSIONS_PER_USER * ESTIMATED_USER_CHANNELS_PER_APPLICATION
            * ESTIMATED_USER_CHANNELS_PER_SESSION;

    // Properties -----------------------------------------------------------------------------------------------------

    private final ConcurrentMap<String, ConcurrentMap<String, Set<String>>> userChannels = new ConcurrentHashMap<>();
    private final ConcurrentMap<Serializable, Set<String>> applicationUsers = new ConcurrentHashMap<>();

    // Actions --------------------------------------------------------------------------------------------------------

    /**
     * Register application user based on given user and session based user ID.
     *
     * @param user The user.
     * @param userId The session based user ID.
     */
    protected void register(Serializable user, String userId) {
        synchronized (applicationUsers) {
            if (!applicationUsers.containsKey(user)) {
                applicationUsers.putIfAbsent(user, synchronizedSet(new HashSet<String>(ESTIMATED_SESSIONS_PER_USER)));
            }

            applicationUsers.get(user).add(userId);
        }
    }

    /**
     * Add user channel ID associated with given session based user ID and channel name.
     *
     * @param userId The session based user ID.
     * @param channel The channel name.
     * @param channelId The channel identifier.
     */
    protected void addChannelId(String userId, String channel, String channelId) {
        if (!userChannels.containsKey(userId)) {
            userChannels.putIfAbsent(userId, new ConcurrentHashMap<String, Set<String>>(ESTIMATED_USER_CHANNELS_PER_APPLICATION));
        }

        ConcurrentMap<String, Set<String>> channelIds = userChannels.get(userId);

        if (!channelIds.containsKey(channel)) {
            channelIds.putIfAbsent(channel, synchronizedSet(new HashSet<String>(ESTIMATED_USER_CHANNELS_PER_SESSION)));
        }

        channelIds.get(channel).add(channelId);
    }

    /**
     * Resolve the user associated with given channel name and ID.
     *
     * @param channel The channel name.
     * @param channelId The channel identifier.
     * @return The user associated with given channel name and ID.
     */
    protected Serializable getUser(String channel, String channelId) {
        for (Entry<Serializable, Set<String>> applicationUser : applicationUsers.entrySet()) {
            for (String userId : applicationUser.getValue()) { // "Normally" this contains only 1 entry, so it isn't that inefficient as it looks like.
                if (getApplicationUserChannelIds(userId, channel).contains(channelId)) {
                    return applicationUser.getKey();
                }
            }
        }

        return null;
    }

    /**
     * Resolve the user-specific channel IDs associated with given user and channel name.
     *
     * @param user The user.
     * @param channel The channel name.
     * @return The user-specific channel IDs associated with given user and channel name.
     */
    protected Set<String> getChannelIds(Serializable user, String channel) {
        Set<String> channelIds = new HashSet<>(ESTIMATED_CHANNELS_IDS_PER_USER);
        Set<String> userIds = applicationUsers.get(user);

        if (userIds != null) {
            for (String userId : userIds) {
                channelIds.addAll(getApplicationUserChannelIds(userId, channel));
            }
        }

        return channelIds;
    }

    /**
     * Deregister application user associated with given user and session based user ID.
     *
     * @param user The user.
     * @param userId The session based user ID.
     */
    protected void deregister(Serializable user, String userId) {
        userChannels.remove(userId);

        synchronized (applicationUsers) {
            Set<String> userIds = applicationUsers.get(user);
            userIds.remove(userId);

            if (userIds.isEmpty()) {
                applicationUsers.remove(user);
            }
        }
    }

    // Internal (static because package private methods in CDI beans are subject to memory leaks) ---------------------

    /**
     * For internal usage only. This makes it possible to save and restore user specific channels during server
     * restart/failover in {@link WebsocketChannelManager}.
     */
    static ConcurrentMap<String, ConcurrentMap<String, Set<String>>> getUserChannels() {
        return getBeanInstance(WebsocketUserManager.class, true).userChannels;
    }

    // Helpers --------------------------------------------------------------------------------------------------------

    private Set<String> getApplicationUserChannelIds(String userId, String channel) {
        Map<String, Set<String>> channels = userChannels.get(userId);

        if (channels != null) {
            Set<String> channelIds = channels.get(channel);

            if (channelIds != null) {
                return channelIds;
            }
        }

        return emptySet();
    }

}
