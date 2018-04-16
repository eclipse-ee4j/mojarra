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

package javax.faces.push;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import javax.enterprise.util.Nonbinding;
import javax.faces.component.UIWebsocket;
import javax.faces.event.WebsocketEvent;
import javax.faces.event.WebsocketEvent.Closed;
import javax.faces.event.WebsocketEvent.Opened;
import javax.inject.Qualifier;
import javax.websocket.CloseReason.CloseCodes;

/**
 * <p class="changed_added_2_3">
 * The CDI annotation <code>&#64;</code>{@link Push} allows you to inject a {@link PushContext} associated with a given
 *  <code>&lt;f:websocket&gt;</code> channel in any container managed artifact in WAR.
 * <pre>
 * &#64;Inject &#64;Push
 * private PushContext channelName;
 * </pre>
 * 
 *
 * <h3 id="configuration"><a href="#configuration">Configuration</a></h3>
 * <p>
 * First enable the web socket endpoint by below boolean context parameter in <code>web.xml</code>.
 * <pre>
 * &lt;context-param&gt;
 *     &lt;param-name&gt;javax.faces.ENABLE_WEBSOCKET_ENDPOINT&lt;/param-name&gt;
 *     &lt;param-value&gt;true&lt;/param-value&gt;
 * &lt;/context-param&gt;
 * </pre>
 *
 *
 * <h3 id="usage-client"><a href="#usage-client">Usage (client)</a></h3>
 * <p>
 * Declare <strong><code>&lt;f:websocket&gt;</code></strong> tag in the JSF view with at least a
 * <strong><code>channel</code></strong> name and an <strong><code>onmessage</code></strong> JavaScript listener
 * function. The channel name may not be an EL expression and it may only contain alphanumeric characters, hyphens,
 * underscores and periods.
 * <p>
 * Here's an example which refers an existing JavaScript listener function.
 * <pre>
 * &lt;f:websocket channel="someChannel" onmessage="someWebsocketListener" /&gt;
 * </pre>
 * <pre>
 * function someWebsocketListener(message, channel, event) {
 *     console.log(message);
 * }
 * </pre>
 * <p>
 * Here's an example which declares an inline JavaScript listener function.
 * <pre>
 * &lt;f:websocket channel="someChannel" onmessage="function(message) { console.log(message); }" /&gt;
 * </pre>
 * <p>
 * The <code>onmessage</code> JavaScript listener function will be invoked with three arguments:
 * <ul>
 * <li><code>message</code>: the push message as JSON object.</li>
 * <li><code>channel</code>: the channel name.</li>
 * <li><code>event</code>: the raw <a href="https://developer.mozilla.org/en-US/docs/Web/API/MessageEvent"><code>
 * MessageEvent</code></a> instance.</li>
 * </ul>
 * <p>
 * In case your server is configured to run WS container on a different TCP port than the HTTP container, then you can
 * use the optional <strong><code>javax.faces.WEBSOCKET_ENDPOINT_PORT</code></strong> integer context parameter in
 * <code>web.xml</code> to explicitly specify the port.
 * <pre>
 * &lt;context-param&gt;
 *     &lt;param-name&gt;javax.faces.WEBSOCKET_ENDPOINT_PORT&lt;/param-name&gt;
 *     &lt;param-value&gt;8000&lt;/param-value&gt;
 * &lt;/context-param&gt;
 * </pre>
 * <p>
 * When successfully connected, the web socket is by default open as long as the document is open, and it will
 * auto-reconnect at increasing intervals when the connection is closed/aborted as result of e.g. a network error or
 * server restart. It will not auto-reconnect when the very first connection attempt already fails. The web socket will
 * be implicitly closed once the document is unloaded.
 *
 *
 * <h3 id="usage-server"><a href="#usage-server">Usage (server)</a></h3>
 * <p>
 * In WAR side, you can inject <strong>{@link PushContext}</strong> via <strong><code>&#64;</code>{@link Push}</strong>
 * annotation on the given channel name in any CDI/container managed artifact such as <code>@Named</code>,
 * <code>@WebServlet</code>, etc wherever you'd like to send a push message and then invoke
 * <strong>{@link PushContext#send(Object)}</strong> with any Java object representing the push message.
 * <pre>
 * &#64;Inject &#64;Push
 * private PushContext someChannel;
 *
 * public void sendMessage(Object message) {
 *     someChannel.send(message);
 * }
 * </pre>
 * <p>
 * By default the name of the channel is taken from the name of the variable into which injection takes place. The
 * channel name can be optionally specified via the <code>channel</code> attribute. The example below injects the push
 * context for channel name <code>foo</code> into a variable named <code>bar</code>.
 * <pre>
 * &#64;Inject &#64;Push(channel="foo")
 * private PushContext bar;
 * </pre>
 * <p>
 * The message object will be encoded as JSON and be delivered as <code>message</code> argument of the
 * <code>onmessage</code> JavaScript listener function associated with the <code>channel</code> name. It can be a
 * plain vanilla <code>String</code>, but it can also be a collection, map and even a javabean.
 * <p>
 * Although web sockets support two-way communication, the <code>&lt;f:websocket&gt;</code> push is designed for one-way
 * communication, from server to client. In case you intend to send some data from client to server, continue
 * using JSF ajax the usual way. This has among others the advantage of maintaining the JSF view
 * state, the HTTP session and, importantingly, all security constraints on business service methods.
 *
 *
 * <h3 id="scopes-and-users"><a href="#scopes-and-users">Scopes and users</a></h3>
 * <p>
 * By default the web socket is <code>application</code> scoped, i.e. any view/session throughout the web application
 * having the same web socket channel open will receive the same push message. The push message can be sent by all users
 * and the application itself.
 * <p>
 * The optional <strong><code>scope</code></strong> attribute can be set to <code>session</code> to restrict the push
 * messages to all views in the current user session only. The push message can only be sent by the user itself and not
 * by the application.
 * <pre>
 * &lt;f:websocket channel="someChannel" scope="session" ... /&gt;
 * </pre>
 * <p>
 * The <code>scope</code> attribute can also be set to <code>view</code> to restrict the push messages to the current
 * view only. The push message will not show up in other views in the same session even if it's the same URL. The push
 * message can only be sent by the user itself and not by the application.
 * <pre>
 * &lt;f:websocket channel="someChannel" scope="view" ... /&gt;
 * </pre>
 * <p>
 * The <code>scope</code> attribute may not be an EL expression and allowed values are <code>application</code>,
 * <code>session</code> and <code>view</code>, case insensitive.
 * <p>
 * Additionally, the optional <strong><code>user</code></strong> attribute can be set to the unique identifier of the
 * logged-in user, usually the login name or the user ID. This way the push message can be targeted to a specific user
 * and can also be sent by other users and the application itself. The value of the <code>user</code> attribute must at
 * least implement {@link Serializable} and have a low memory footprint, so putting entire user entity is not
 * recommended.
 * <p>
 * E.g. when you're using container managed authentication or a related framework/library:
 * <pre>
 * &lt;f:websocket channel="someChannel" user="#{request.remoteUser}" ... /&gt;
 * </pre>
 * <p>
 * Or when you have a custom user entity around in EL as <code>#{someLoggedInUser}</code> which has an <code>id</code>
 * property representing its identifier:
 * <pre>
 * &lt;f:websocket channel="someChannel" user="#{someLoggedInUser.id}" ... /&gt;
 * </pre>
 * <p>
 * When the <code>user</code> attribute is specified, then the <code>scope</code> defaults to <code>session</code> and
 * cannot be set to <code>application</code>.
 * <p>
 * In the server side, the push message can be targeted to the user specified in the <code>user</code> attribute via
 * <strong>{@link PushContext#send(Object, Serializable)}</strong>. The push message can be sent by all users and the
 * application itself.
 * <pre>
 * &#64;Inject &#64;Push
 * private PushContext someChannel;
 *
 * public void sendMessage(Object message, User recipientUser) {
 *     Long recipientUserId = recipientUser.getId();
 *     someChannel.send(message, recipientUserId);
 * }
 * </pre>
 * <p>
 * Multiple users can be targeted by passing a {@link Collection} holding user identifiers to
 * <strong>{@link PushContext#send(Object, Collection)}</strong>.
 * <pre>
 * public void sendMessage(Object message, Group recipientGroup) {
 *     Collection&lt;Long&gt; recipientUserIds = recipientGroup.getUserIds();
 *     someChannel.send(message, recipientUserIds);
 * }
 * </pre>
 *
 *
 * <h3 id="connecting"><a href="#connecting">Conditionally connecting</a></h3>
 * <p>
 * You can use the optional <strong><code>connected</code></strong> attribute to control whether to auto-connect the web
 * socket or not.
 * <pre>
 * &lt;f:websocket ... connected="#{bean.pushable}" /&gt;
 * </pre>
 * <p>
 * It defaults to <code>true</code> and it's under the covers interpreted as a JavaScript instruction whether to open or
 * close the web socket push connection. If the value is an EL expression and it becomes <code>false</code> during an
 * ajax request, then the push connection will explicitly be closed during oncomplete of that ajax request.
 * <p>
 * You can also explicitly set it to <code>false</code> and manually open the push connection in client side by
 * invoking <strong><code>jsf.push.open(clientId)</code></strong>, passing the component's client ID.
 * <pre>
 * &lt;h:commandButton ... onclick="jsf.push.open('foo')"&gt;
 *     &lt;f:ajax ... /&gt;
 * &lt;/h:commandButton&gt;
 * &lt;f:websocket id="foo" channel="bar" scope="view" ... connected="false" /&gt;
 * </pre>
 * <p>
 * In case you intend to have an one-time push and don't expect more messages,
 * you can optionally explicitly close the push connection from client side by invoking
 * <strong><code>jsf.push.close(clientId)</code></strong>, passing the component's client ID. For example, in the
 * <code>onmessage</code> JavaScript listener function as below:
 * <pre>
 * function someWebsocketListener(message) {
 *     // ...
 *     jsf.push.close('foo');
 * }
 * </pre>
 *
 *
 * <h3 id="events-client"><a href="#events-client">Events (client)</a></h3>
 * <p>
 * The optional <strong><code>onopen</code></strong> JavaScript listener function can be used to listen on open of a web
 * socket in client side. This will be invoked on the very first connection attempt, regardless of whether it will be
 * successful or not. This will not be invoked when the web socket auto-reconnects a broken connection after the first
 * successful connection.
 * <pre>
 * &lt;f:websocket ... onopen="websocketOpenListener" /&gt;
 * </pre>
 * <pre>
 * function websocketOpenListener(channel) {
 *     // ...
 * }
 * </pre>
 * <p>
 * The <code>onopen</code> JavaScript listener function will be invoked with one argument:
 * <ul>
 * <li><code>channel</code>: the channel name, useful in case you intend to have a global listener.</li>
 * </ul>
 * <p>
 * The optional <strong><code>onclose</code></strong> JavaScript listener function can be used to listen on (ab)normal
 * close of a web socket. This will be invoked when the very first connection attempt fails, or the server has returned
 * close reason code <code>1000</code> (normal closure) or <code>1008</code> (policy violated), or the maximum reconnect
 * attempts has exceeded. This will not be invoked when the web socket can make an auto-reconnect attempt on a broken
 * connection after the first successful connection.
 * <pre>
 * &lt;f:websocket ... onclose="websocketCloseListener" /&gt;
 * </pre>
 * <pre>
 * function websocketCloseListener(code, channel, event) {
 *     if (code == -1) {
 *         // Web sockets not supported by client.
 *     } else if (code == 1000) {
 *         // Normal close (as result of expired session or view).
 *     } else {
 *         // Abnormal close reason (as result of an error).
 *     }
 * }
 * </pre>
 * <p>
 * The <code>onclose</code> JavaScript listener function will be invoked with three arguments:
 * <ul>
 * <li><code>code</code>: the close reason code as integer. If this is <code>-1</code>, then the web socket
 * is simply not <a href="http://caniuse.com/websockets">supported</a> by the client. If this is <code>1000</code>,
 * then it was normally closed. Else if this is not <code>1000</code>, then there may be an error. See also
 * <a href="http://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455 section 7.4.1</a> and {@link CloseCodes} API for
 * an elaborate list of all close codes.</li>
 * <li><code>channel</code>: the channel name.</li>
 * <li><code>event</code>: the raw <a href="https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent"><code>
 * CloseEvent</code></a> instance.</li>
 * </ul>
 * <p>
 * When a session or view scoped socket is automatically closed with close reason code <code>1000</code> by the server
 * (and thus not manually by the client via <code>jsf.push.close(clientId)</code>), then it means that the session
 * or view has expired.
 *
 *
 * <h3 id="events-server"><a href="#events-server">Events (server)</a></h3>
 * <p>
 * When a web socket has been opened, a new CDI <strong>{@link WebsocketEvent}</strong> will be fired with
 * <strong><code>&#64;</code>{@link Opened}</strong> qualifier. When a web socket has been closed, a new CDI
 * {@link WebsocketEvent} will be fired with <strong><code>&#64;</code>{@link Closed}</strong> qualifier. They can only be
 * observed and collected in an application scoped CDI bean as below.
 * <pre>
 * &#64;ApplicationScoped
 * public class WebsocketObserver {
 *
 *     public void onOpen(&#64;Observes &#64;Opened WebsocketEvent event) {
 *         String channel = event.getChannel(); // Returns &lt;f:websocket channel&gt;.
 *         Long userId = event.getUser(); // Returns &lt;f:websocket user&gt;, if any.
 *         // ...
 *     }
 *
 *     public void onClose(&#64;Observes &#64;Closed WebsocketEvent event) {
 *         String channel = event.getChannel(); // Returns &lt;f:websocket channel&gt;.
 *         Long userId = event.getUser(); // Returns &lt;f:websocket user&gt;, if any.
 *         CloseCode code = event.getCloseCode(); // Returns close reason code.
 *         // ...
 *     }
 *
 * }
 * </pre>
 *
 *
 * <h3 id="security"><a href="#security">Security considerations</a></h3>
 * <p>
 * If the socket is declared in a page which is only restricted to logged-in users with a specific role, then you may
 * want to add the URL of the push handshake request URL to the set of restricted URLs.
 * <p>
 * The push handshake request URL is composed of the URI prefix <strong><code>/javax.faces.push/</code></strong>, followed
 * by channel name. So, in case of for example container managed security which has already restricted an example page
 * <code>/user/foo.xhtml</code> to logged-in users with the example role <code>USER</code> on the example URL pattern
 * <code>/user/*</code> in <code>web.xml</code> like below,
 * <pre>
 * &lt;security-constraint&gt;
 *     &lt;web-resource-collection&gt;
 *         &lt;web-resource-name&gt;Restrict access to role USER.&lt;/web-resource-name&gt;
 *         &lt;url-pattern&gt;/user/*&lt;/url-pattern&gt;
 *     &lt;/web-resource-collection&gt;
 *     &lt;auth-constraint&gt;
 *         &lt;role-name&gt;USER&lt;/role-name&gt;
 *     &lt;/auth-constraint&gt;
 * &lt;/security-constraint&gt;
 * </pre>
 * <p>
 * .. and the page <code>/user/foo.xhtml</code> in turn contains a <code>&lt;f:websocket channel="foo"&gt;</code>, then you
 * need to add a restriction on push handshake request URL pattern of <code>/javax.faces.push/foo</code> like below.
 * <pre>
 * &lt;security-constraint&gt;
 *     &lt;web-resource-collection&gt;
 *         &lt;web-resource-name&gt;Restrict access to role USER.&lt;/web-resource-name&gt;
 *         &lt;url-pattern&gt;/user/*&lt;/url-pattern&gt;
 *         &lt;url-pattern&gt;/javax.faces.push/foo&lt;/url-pattern&gt;
 *     &lt;/web-resource-collection&gt;
 *     &lt;auth-constraint&gt;
 *         &lt;role-name&gt;USER&lt;/role-name&gt;
 *     &lt;/auth-constraint&gt;
 * &lt;/security-constraint&gt;
 * </pre>
 * <p>
 * As extra security, particularly for those public channels which can't be restricted by security constraints, the
 * <code>&lt;f:websocket&gt;</code> will register all so far declared channels in the current HTTP session, and any
 * incoming web socket open request will be checked whether they match the so far registered channels in the current
 * HTTP session. In case the channel is unknown (e.g. randomly guessed or spoofed by endusers or manually reconnected
 * after the session is expired), then the web socket will immediately be closed with close reason code
 * {@link CloseCodes#VIOLATED_POLICY} (<code>1008</code>). Also, when the HTTP session gets destroyed, all session and
 * view scoped channels which are still open will explicitly be closed from server side with close reason code
 * {@link CloseCodes#NORMAL_CLOSURE} (<code>1000</code>). Only application scoped sockets remain open and are still
 * reachable from server end even when the session or view associated with the page in client side is expired.
 * 
 * 
 * <h3 id="ui"><a href="#ui">Ajax support</a></h3>
 * <p>
 * In case you'd like to perform complex UI updates depending on the received push message, then you can nest 
 * <code>&lt;f:ajax&gt;</code> inside <code>&lt;f:websocket&gt;</code>. Here's an example:
 * <pre>
 * &lt;h:panelGroup id="foo"&gt;
 *     ... (some complex UI here) ...
 * &lt;/h:panelGroup&gt;
 *
 * &lt;h:form&gt;
 *     &lt;f:websocket channel="someChannel" scope="view"&gt;
 *         &lt;f:ajax event="someEvent" listener="#{bean.pushed}" render=":foo" /&gt;
 *     &lt;/f:websocket&gt;
 * &lt;/h:form&gt;
 * </pre>
 * <p>
 * Here, the push message simply represents the ajax event name. You can use any custom event name.
 * <pre>
 * someChannel.send("someEvent");
 * </pre>
 * <p>
 * An alternative is to combine <code>&lt;w:websocket&gt;</code> with <code>&lt;h:commandScript&gt;</code>. E.g.
 * <pre>
 * &lt;h:panelGroup id="foo"&gt;
 *     ... (some complex UI here) ...
 * &lt;/h:panelGroup&gt;
 *
 * &lt;f:websocket channel="someChannel" scope="view" onmessage="someCommandScript" /&gt;
 * &lt;h:form&gt;
 *     &lt;h:commandScript name="someCommandScript" action="#{bean.pushed}" render=":foo" /&gt;
 * &lt;/h:form&gt;
 * </pre>
 * <p>
 * If you pass a <code>Map&lt;String,V&gt;</code> or a JavaBean as push message object, then all entries/properties will
 * transparently be available as request parameters in the command script method <code>#{bean.pushed}</code>.
 *
 *
 * @see PushContext
 * @see UIWebsocket
 * @see WebsocketEvent
 * @since 2.3
 */
@Qualifier
@Retention(RUNTIME)
@Target({ METHOD, FIELD, PARAMETER })
public @interface Push {

    /**
     * (Optional) The name of the push channel. If not specified the name of the injection target field will be used.
     *
     * @return The name of the push channel.
     */
    @Nonbinding String channel() default "";

}
