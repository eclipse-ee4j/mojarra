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

package jakarta.faces.event;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Objects;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.faces.push.Push;
import jakarta.inject.Qualifier;
import jakarta.websocket.CloseReason.CloseCode;

/**
 * <p class="changed_added_2_3">
 * This web socket event will be fired when a new <code>&lt;f:websocket&gt;</code> has been
 * <code>&#64;</code>{@link Opened} or <code>&#64;</code>{@link Closed}. An application scoped CDI bean can
 * <code>&#64;</code>{@link Observes} them.
 * <p>
 * For detailed usage instructions, see <code>&#64;</code>{@link Push} javadoc.
 *
 * @see Push
 * @see Opened
 * @see Closed
 * @since 2.3
 */
public final class WebsocketEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String channel;
    private final Serializable user;
    private final CloseCode code;

    public WebsocketEvent(String channel, Serializable user, CloseCode code) {
        this.channel = channel;
        this.user = user;
        this.code = code;
    }

    /**
     * Returns the <code>&lt;f:websocket channel&gt;</code>.
     *
     * @return The web socket channel name.
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Returns the <code>&lt;f:websocket user&gt;</code>, if any.
     *
     * @param <S> The generic type of the user identifier.
     * @return The web socket user identifier, if any.
     * @throws ClassCastException When <code>S</code> is of wrong type.
     */
    @SuppressWarnings("unchecked")
    public <S extends Serializable> S getUser() {
        return (S) user;
    }

    /**
     * Returns the close code. If this returns <code>null</code>, then it was {@link Opened}. If this returns
     * non-<code>null</code>, then it was {@link Closed}.
     *
     * @return The close code.
     */
    public CloseCode getCloseCode() {
        return code;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(channel, user, code);
    }

    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass() && Objects.equals(channel, ((WebsocketEvent) other).channel)
                && Objects.equals(user, ((WebsocketEvent) other).user) && Objects.equals(code, ((WebsocketEvent) other).code);
    }

    @Override
    public String toString() {
        return String.format("WebsocketEvent[channel=%s, user=%s, closeCode=%s]", channel, user, code);
    }

    /**
     * <p class="changed_added_2_3">
     * Indicates that a <code>&lt;f:websocket&gt;</code> has opened.
     * <p>
     * For detailed usage instructions, see <code>&#64;</code>{@link Push} javadoc.
     *
     * @see Push
     * @since 2.3
     */
    @Qualifier
    @Target(PARAMETER)
    @Retention(RUNTIME)
    @Documented
    public @interface Opened {

        /**
         * <p class="changed_added_4_0">
         * Supports inline instantiation of the {@link Opened} qualifier.
         * </p>
         *
         * @since 4.0
         */
        public static final class Literal extends AnnotationLiteral<Opened> implements Opened {

            private static final long serialVersionUID = 1L;

            /**
             * Instance of the {@link Opened} qualifier.
             */
            public static final Literal INSTANCE = new Literal();
        }
    }

    /**
     * <p class="changed_added_2_3">
     * Indicates that a <code>&lt;f:websocket&gt;</code> has closed.
     * <p>
     * For detailed usage instructions, see <code>&#64;</code>{@link Push} javadoc.
     *
     * @see Push
     * @since 2.3
     */
    @Qualifier
    @Target(PARAMETER)
    @Retention(RUNTIME)
    @Documented
    public @interface Closed {

        /**
         * <p class="changed_added_4_0">
         * Supports inline instantiation of the {@link Closed} qualifier.
         * </p>
         *
         * @since 4.0
         */
        public static final class Literal extends AnnotationLiteral<Closed> implements Closed {

            private static final long serialVersionUID = 1L;

            /**
             * Instance of the {@link Closed} qualifier.
             */
            public static final Literal INSTANCE = new Literal();
        }
    }

}
