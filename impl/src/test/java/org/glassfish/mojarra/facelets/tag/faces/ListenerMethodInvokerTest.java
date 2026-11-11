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

package org.glassfish.mojarra.facelets.tag.faces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jakarta.el.BeanNameELResolver;
import jakarta.el.BeanNameResolver;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.el.StandardELContext;
import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;

import org.glassfish.expressly.ExpressionFactoryImpl;
import org.glassfish.mojarra.util.FacesLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link ListenerMethodInvoker} tries the variant taking the event first on each invocation, and the variant taking no arguments only when that one
 * is not found. An exception thrown by the invoked listener method itself never triggers the other variant. In Development stage, a listener which resolves to
 * both variants logs a warning once.
 */
class ListenerMethodInvokerTest {

    private static final String LISTENER = "/page.xhtml @1,1 listener=\"#{bean.listen}\"";
    private static final ExpressionFactory FACTORY = new ExpressionFactoryImpl();

    private final ELContext elContext = mock(ELContext.class);
    private final Application application = mock(Application.class);
    private final FacesContext context = mock(FacesContext.class);
    private final MethodExpression oneArg = mock(MethodExpression.class);
    private final MethodExpression noArg = mock(MethodExpression.class);
    private final Object event = new Object();
    private final Logger logger = FacesLogger.TAGLIB.getLogger();
    private final List<LogRecord> warnings = new ArrayList<>();
    private final Handler handler = new Handler() {

        @Override
        public void publish(LogRecord record) {
            warnings.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

    };

    @BeforeEach
    void setUp() {
        when(context.getELContext()).thenReturn(elContext);
        when(context.getApplication()).thenReturn(application);
        when(context.isProjectStage(any())).thenCallRealMethod();
        when(application.getProjectStage()).thenReturn(ProjectStage.Production);
        logger.addHandler(handler);
    }

    @AfterEach
    void tearDown() {
        logger.removeHandler(handler);
    }

    @Test
    void oneArgMethodIsInvokedWithEventWithoutTryingNoArgMethod() {
        ListenerMethodInvoker.invoke(context, LISTENER, oneArg, noArg, event);
        ListenerMethodInvoker.invoke(context, LISTENER, oneArg, noArg, event);

        verify(oneArg, times(2)).invoke(elContext, new Object[] { event });
        verify(noArg, never()).invoke(any(), any());
    }

    @Test
    void noArgMethodIsInvokedWhenOneArgMethodIsNotFound() {
        when(oneArg.invoke(any(), any())).thenThrow(new MethodNotFoundException());

        ListenerMethodInvoker.invoke(context, LISTENER, oneArg, noArg, event);
        ListenerMethodInvoker.invoke(context, LISTENER, oneArg, noArg, event);

        verify(oneArg, times(2)).invoke(elContext, new Object[] { event });
        verify(noArg, times(2)).invoke(elContext, new Object[] {});
    }

    @Test
    void noArgMethodIsInvokedWhenOneArgMethodThrowsIllegalArgumentException() {
        when(oneArg.invoke(any(), any())).thenThrow(new IllegalArgumentException());

        ListenerMethodInvoker.invoke(context, LISTENER, oneArg, noArg, event);

        verify(noArg).invoke(elContext, new Object[] {});
    }

    @Test
    void oneArgMethodIsInvokedAfterTargetWithOnlyNoArgMethod() {
        when(oneArg.invoke(any(), any())).thenThrow(new MethodNotFoundException());
        ListenerMethodInvoker.invoke(context, LISTENER, oneArg, noArg, event);

        reset(oneArg, noArg);
        ListenerMethodInvoker.invoke(context, LISTENER, oneArg, noArg, event);

        verify(oneArg).invoke(elContext, new Object[] { event });
        verify(noArg, never()).invoke(any(), any());
    }

    @Test
    void exceptionOfOtherVariantPropagates() {
        MethodNotFoundException missing = new MethodNotFoundException("neither");
        when(oneArg.invoke(any(), any())).thenThrow(new MethodNotFoundException());
        when(noArg.invoke(any(), any())).thenThrow(missing);

        assertSame(missing, assertThrows(MethodNotFoundException.class, () -> ListenerMethodInvoker.invoke(context, LISTENER, oneArg, noArg, event)));
    }

    @Test
    void exceptionThrownByListenerMethodIsNotRetriedWithOtherVariant() {
        BothBean bean = new BothBean(true);
        ELContext realContext = realContext(bean);

        ELException thrown = assertThrows(
            ELException.class,
            () -> ListenerMethodInvoker.invoke(context, LISTENER, oneArg(realContext, "#{bean.listen}"), noArg(realContext, "#{bean.listen}"), event)
        );

        assertEquals(IllegalArgumentException.class, thrown.getCause().getClass());
        assertEquals(0, bean.noArgInvocations);
    }

    @Test
    void warnsOnceInDevelopmentWhenNoArgMethodAlsoExists() {
        String listener = "/warnsOnce.xhtml @1,1 listener=\"#{bean.listen}\"";
        ELContext realContext = realContext(new BothBean(false));
        inStage(ProjectStage.Development);

        ListenerMethodInvoker.invoke(context, listener, oneArg(realContext, "#{bean.listen}"), noArg(realContext, "#{bean.listen}"), event);
        ListenerMethodInvoker.invoke(context, listener, oneArg(realContext, "#{bean.listen}"), noArg(realContext, "#{bean.listen}"), event);

        assertWarning(listener);
    }

    @Test
    void doesNotWarnOutsideDevelopment() {
        ELContext realContext = realContext(new BothBean(false));
        inStage(ProjectStage.Production);

        ListenerMethodInvoker.invoke(context, "/production.xhtml", oneArg(realContext, "#{bean.listen}"), noArg(realContext, "#{bean.listen}"), event);

        assertEquals(0, warnings.size());
    }

    @Test
    void doesNotWarnWhenOnlyOneArgMethodExists() {
        ELContext realContext = realContext(new OneArgBean());
        inStage(ProjectStage.Development);

        ListenerMethodInvoker.invoke(context, "/oneArgOnly.xhtml", oneArg(realContext, "#{bean.listen}"), noArg(realContext, "#{bean.listen}"), event);

        assertEquals(0, warnings.size());
    }

    @Test
    void checksOnlyTheBeanOfTheFirstInvocationTakingTheEvent() {
        String listener = "/firstBean.xhtml @1,1 listener=\"#{bean.listen}\"";
        inStage(ProjectStage.Development);

        ELContext oneArgOnly = realContext(new OneArgBean());
        ListenerMethodInvoker.invoke(context, listener, oneArg(oneArgOnly, "#{bean.listen}"), noArg(oneArgOnly, "#{bean.listen}"), event);
        ELContext both = realContext(new BothBean(false));
        ListenerMethodInvoker.invoke(context, listener, oneArg(both, "#{bean.listen}"), noArg(both, "#{bean.listen}"), event);

        assertEquals(0, warnings.size());
    }

    @Test
    void doesNotWarnWhenExpressionProvidesItsOwnParameters() {
        ELContext realContext = realContext(new BothBean(false));
        inStage(ProjectStage.Development);

        ListenerMethodInvoker.invoke(context, "/ownParameters.xhtml", oneArg(realContext, "#{bean.listen()}"), noArg(realContext, "#{bean.listen()}"), event);

        assertEquals(0, warnings.size());
    }

    private void assertWarning(String listener) {
        assertEquals(1, warnings.size());
        String message = logger.getResourceBundle().getString(warnings.get(0).getMessage());
        assertTrue(message.startsWith("JSF1113:"), message);
        assertEquals(listener, warnings.get(0).getParameters()[0]);
        assertEquals("listen", warnings.get(0).getParameters()[1]);
    }

    private void inStage(ProjectStage stage) {
        when(application.getProjectStage()).thenReturn(stage);
    }

    private ELContext realContext(Object bean) {
        StandardELContext realContext = new StandardELContext(FACTORY);
        realContext.addELResolver(new BeanNameELResolver(new BeanNameResolver() {

            @Override
            public boolean isNameResolved(String beanName) {
                return "bean".equals(beanName);
            }

            @Override
            public Object getBean(String beanName) {
                return bean;
            }

        }));
        when(context.getELContext()).thenReturn(realContext);
        return realContext;
    }

    private static MethodExpression oneArg(ELContext realContext, String expression) {
        return FACTORY.createMethodExpression(realContext, expression, Object.class, new Class<?>[] { Object.class });
    }

    private static MethodExpression noArg(ELContext realContext, String expression) {
        return FACTORY.createMethodExpression(realContext, expression, Object.class, new Class<?>[] {});
    }

    public static class BothBean {

        private final boolean oneArgThrows;
        int noArgInvocations;

        BothBean(boolean oneArgThrows) {
            this.oneArgThrows = oneArgThrows;
        }

        public void listen(Object event) {
            if (oneArgThrows) {
                throw new IllegalArgumentException();
            }
        }

        public void listen() {
            noArgInvocations++;
        }

    }

    public static class OneArgBean {

        public void listen(Object event) {
        }

    }

}
