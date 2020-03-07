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

import static jakarta.faces.push.PushContext.URI_PREFIX;
import static jakarta.websocket.CloseReason.CloseCodes.GOING_AWAY;
import static jakarta.websocket.CloseReason.CloseCodes.VIOLATED_POLICY;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.push.Push;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

/**
 * <p class="changed_added_2_3">
 * This web socket server endpoint handles web socket requests coming from <code>&lt;f:websocket&gt;</code>.
 *
 * @author Bauke Scholtz
 * @see Push
 * @since 2.3
 */
public class WebsocketEndpoint extends Endpoint {

    // Constants ------------------------------------------------------------------------------------------------------

    /** The URI path parameter name of the web socket channel. */
    static final String PARAM_CHANNEL = "channel";

    /** The context-relative URI template where the web socket endpoint should listen on. */
    public static final String URI_TEMPLATE = URI_PREFIX + "/{" + PARAM_CHANNEL + "}";

    private static final Logger logger = Logger.getLogger(WebsocketEndpoint.class.getName());
    private static final CloseReason REASON_UNKNOWN_CHANNEL = new CloseReason(VIOLATED_POLICY, "Unknown channel");
    private static final String ERROR_EXCEPTION = "WebsocketEndpoint: An exception occurred during processing web socket request.";

    // Actions --------------------------------------------------------------------------------------------------------

    /**
     * Add given web socket session to the {@link WebocketSessionManager}. If web socket session is not accepted (i.e. the
     * channel identifier is unknown), then immediately close with reason VIOLATED_POLICY (close code 1008).
     *
     * @param session The opened web socket session.
     * @param config The endpoint configuration.
     */
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        if (WebsocketSessionManager.getInstance().add(session)) { // @Inject in Endpoint doesn't work in Tomcat+Weld/OWB.
            session.setMaxIdleTimeout(0);
        } else {
            try {
                session.close(REASON_UNKNOWN_CHANNEL);
            } catch (IOException e) {
                onError(session, e);
            }
        }
    }

    /**
     * Delegate exception to onClose.
     *
     * @param session The errored web socket session.
     * @param throwable The cause.
     */
    @Override
    public void onError(Session session, Throwable throwable) {
        if (session.isOpen()) {
            session.getUserProperties().put(Throwable.class.getName(), throwable);
        }
    }

    /**
     * Remove given web socket session from the {@link WebsocketSessionManager}. If there is any exception from onError
     * which was not caused by GOING_AWAY, then log it. Tomcat &lt;= 8.0.30 is known to throw an unnecessary exception when
     * client abruptly disconnects, see also <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=57489">issue 57489</a>.
     *
     * @param session The closed web socket session.
     * @param reason The close reason.
     */
    @Override
    public void onClose(Session session, CloseReason reason) {
        WebsocketSessionManager.getInstance().remove(session, reason); // @Inject in Endpoint doesn't work in Tomcat+Weld/OWB and CDI.current() during WS close
                                                                       // doesn't work in WildFly.

        Throwable throwable = (Throwable) session.getUserProperties().remove(Throwable.class.getName());

        if (throwable != null && reason.getCloseCode() != GOING_AWAY) {
            logger.log(Level.SEVERE, ERROR_EXCEPTION, throwable);
        }
    }

}
