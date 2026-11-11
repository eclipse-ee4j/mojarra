/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.facelets.tag.faces.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import jakarta.el.BeanNameELResolver;
import jakarta.el.BeanNameResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.StandardELContext;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.PostAddToViewEvent;

import org.glassfish.expressly.ExpressionFactoryImpl;
import org.glassfish.mojarra.facelets.tag.faces.ListenerMethodInvoker;
import org.glassfish.mojarra.mock.MockFacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that the listeners of <code>f:event</code> and <code>f:ajax</code> still invoke the listener method through {@link ListenerMethodInvoker} after a
 * serialization round trip of the state.
 */
@SuppressWarnings("auxiliaryclass")
class DeserializedListenerTest {

    private static final String LISTEN = "#{bean.listen}";
    private static final String NO_ARG_ONLY = "#{bean.noArgOnly}";

    private final ExpressionFactory factory = new ExpressionFactoryImpl();
    private final ListenerBean bean = new ListenerBean();
    private MockFacesContext facesContext;
    private StandardELContext elContext;

    @BeforeEach
    void setUp() {
        facesContext = new MockFacesContext();
        elContext = new StandardELContext(factory);
        elContext.addELResolver(new BeanNameELResolver(new BeanNameResolver() {

            @Override
            public boolean isNameResolved(String beanName) {
                return "bean".equals(beanName);
            }

            @Override
            public Object getBean(String beanName) {
                return bean;
            }

        }));
        facesContext.setELContext(elContext);
        facesContext.setApplication(mock(Application.class));
    }

    @AfterEach
    void tearDown() {
        facesContext.release();
    }

    @Test
    void deserializedSystemEventListenerInvokesOneArgMethodWithEvent() throws Exception {
        DeclarativeSystemEventListener listener = roundTrip(
            new DeclarativeSystemEventListener(oneArg(LISTEN, PostAddToViewEvent.class), noArg(LISTEN), LISTEN)
        );
        PostAddToViewEvent event = new PostAddToViewEvent(new UIOutput());

        listener.processEvent(event);

        assertSame(event, bean.received);
    }

    @Test
    void deserializedAjaxBehaviorListenerInvokesOneArgMethodWithEvent() throws Exception {
        AjaxBehaviorListenerImpl listener = roundTrip(
            new AjaxBehaviorListenerImpl(oneArg(LISTEN, AjaxBehaviorEvent.class), noArg(LISTEN), LISTEN)
        );
        AjaxBehaviorEvent event = new AjaxBehaviorEvent(new UIOutput(), new AjaxBehavior());

        listener.processAjaxBehavior(event);

        assertSame(event, bean.received);
    }

    @Test
    void deserializedListenerInvokesNoArgMethod() throws Exception {
        DeclarativeSystemEventListener listener = roundTrip(
            new DeclarativeSystemEventListener(oneArg(NO_ARG_ONLY, PostAddToViewEvent.class), noArg(NO_ARG_ONLY), NO_ARG_ONLY)
        );

        listener.processEvent(new PostAddToViewEvent(new UIOutput()));

        assertEquals(1, bean.noArgInvocations);
    }

    private MethodExpression oneArg(String expression, Class<?> eventType) {
        return factory.createMethodExpression(elContext, expression, Object.class, new Class<?>[] { eventType });
    }

    private MethodExpression noArg(String expression) {
        return factory.createMethodExpression(elContext, expression, Object.class, new Class<?>[] {});
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(object);
        }

        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) input.readObject();
        }
    }

    public static class ListenerBean {

        Object received;
        int noArgInvocations;

        public void listen(Object event) {
            received = event;
        }

        public void noArgOnly() {
            noArgInvocations++;
        }

    }

}
