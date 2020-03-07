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

package jakarta.faces.push;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.websocket.RemoteEndpoint.Async;

/**
 * <p class="changed_added_2_3">
 * CDI interface to send a message object to the push socket channel as identified by <code>&#64;</code>{@link Push}.
 * This can be injected via <code>&#64;Push</code> in any container managed artifact in WAR.
 *
 * <pre>
 * &#64;Inject
 * &#64;Push
 * private PushContext channelName;
 * </pre>
 * <p>
 * For detailed usage instructions, see <code>&#64;</code>{@link Push} javadoc.
 *
 * @since 2.3
 * @see Push
 */
public interface PushContext extends Serializable {

    // Constants ------------------------------------------------------------------------------------------------------

    /** The boolean context parameter name to explicitly enable web socket endpoint during startup. */
    String ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME = "jakarta.faces.ENABLE_WEBSOCKET_ENDPOINT";

    /** The integer context parameter name to specify the websocket endpoint port when it's different from HTTP port. */
    String WEBSOCKET_ENDPOINT_PORT_PARAM_NAME = "jakarta.faces.WEBSOCKET_ENDPOINT_PORT";

    /** The context-relative web socket URI prefix where the endpoint should listen on. */
    String URI_PREFIX = "/jakarta.faces.push";

    // Actions --------------------------------------------------------------------------------------------------------

    /**
     * Send given message object to the push socket channel as identified by <code>&#64;</code>{@link Push}. The message
     * object will be encoded as JSON and be available as first argument of the JavaScript listener function declared in
     * <code>&lt;f:websocket onmessage&gt;</code>.
     *
     * @param message The push message object.
     * @return The results of the send operation. If it returns an empty set, then there was no open web socket session
     * associated with given socket channel. The returned futures will return <code>null</code> on {@link Future#get()} if
     * the message was successfully delivered and otherwise throw {@link ExecutionException}.
     * @throws IllegalArgumentException If given message object cannot be encoded as JSON.
     * @see Async#sendText(String)
     */
    Set<Future<Void>> send(Object message);

    /**
     * Send given message object to the push socket channel as identified by <code>&#64;</code>{@link Push}, targeted to the
     * given user as identified by <code>&lt;f:websocket user&gt;</code>. The message object will be encoded as JSON and be
     * available as first argument of the JavaScript listener function declared in
     * <code>&lt;f:websocket onmessage&gt;</code>.
     *
     * @param <S> The generic type of the user identifier.
     * @param message The push message object.
     * @param user The user to which the push message object must be delivered to.
     * @return The results of the send operation. If it returns an empty set, then there was no open web socket session
     * associated with given socket channel and user. The returned futures will return <code>null</code> on
     * {@link Future#get()} if the message was successfully delivered and otherwise throw {@link ExecutionException}.
     * @throws IllegalArgumentException If given message object cannot be encoded as JSON.
     * @see Async#sendText(String)
     */
    <S extends Serializable> Set<Future<Void>> send(Object message, S user);

    /**
     * Send given message object to the push socket channel as identified by <code>&#64;</code>{@link Push}, targeted to the
     * given users as identified by <code>&lt;f:websocket user&gt;</code>. The message object will be encoded as JSON and be
     * available as first argument of the JavaScript listener function declared in
     * <code>&lt;f:websocket onmessage&gt;</code>.
     *
     * @param <S> The generic type of the user identifier.
     * @param message The push message object.
     * @param users The users to which the push message object must be delivered to.
     * @return The results of the send operation grouped by user. If it contains an empty set, then there was no open web
     * socket session associated with given socket channel and user. The returned futures will return <code>null</code> on
     * {@link Future#get()} if the message was successfully delivered and otherwise throw {@link ExecutionException}.
     * @throws IllegalArgumentException If given message object cannot be encoded as JSON.
     * @see Async#sendText(String)
     */
    <S extends Serializable> Map<S, Set<Future<Void>>> send(Object message, Collection<S> users);

}
