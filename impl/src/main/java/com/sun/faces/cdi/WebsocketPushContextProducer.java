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

package com.sun.faces.cdi;

import static com.sun.faces.cdi.CdiUtils.getQualifier;

import com.sun.faces.push.WebsocketPushContext;
import com.sun.faces.push.WebsocketSessionManager;
import com.sun.faces.push.WebsocketUserManager;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.inject.Inject;

/**
 * <p class="changed_added_2_3">
 * This producer prepares the {@link WebsocketPushContext} instance for injection by <code>&#64;</code>{@link Push}.
 * </p>
 *
 * @since 2.3
 * @see PushContext
 */
@Dependent
public class WebsocketPushContextProducer {

    // Variables ------------------------------------------------------------------------------------------------------

    @SuppressWarnings("unused") // Workaround for OpenWebBeans not properly passing it as produce() method argument.
    @Inject
    private InjectionPoint injectionPoint;

    @Inject
    private WebsocketSessionManager socketSessions;

    @Inject
    private WebsocketUserManager socketUsers;

    // Actions --------------------------------------------------------------------------------------------------------

    @Produces
    @Push
    public PushContext produce(InjectionPoint injectionPoint) {
        Push push = getQualifier(injectionPoint, Push.class);
        String channel = push.channel().isEmpty() ? injectionPoint.getMember().getName() : push.channel();
        return new WebsocketPushContext(channel, socketSessions, socketUsers);
    }

}
