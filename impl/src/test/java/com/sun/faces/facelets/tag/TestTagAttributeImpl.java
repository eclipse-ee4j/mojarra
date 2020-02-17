package com.sun.faces.facelets.tag;

import com.sun.faces.facelets.el.ContextualCompositeValueExpression;
import com.sun.faces.facelets.el.TagValueExpression;
import com.sun.faces.facelets.tag.TagAttributeImpl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;

import junit.framework.TestCase;

public class TestTagAttributeImpl extends TestCase {

    private final static Random rnd = new Random();

    public void testGetValueExpressionFaceletContextClass() {

        FaceletContext fc = mockFaceletContext();

        TagAttributeImpl tai = new TagAttributeImpl(null, null, null, null, "#{cc.foo}");
        ValueExpression ve = tai.getValueExpression(fc, String.class);
        assertNotNull(ve);
        assertTrue(ve instanceof TagValueExpression);
        assertTrue(((TagValueExpression) ve).getWrapped() instanceof ContextualCompositeValueExpression);

        tai = new TagAttributeImpl(null, null, null, null, "#{cc.attr.method}");
        ve = tai.getValueExpression(fc, String.class);
        assertNotNull(ve);
        assertTrue(ve instanceof TagValueExpression);

        // tai = new TagAttributeImpl(null, null, null, null, "#{cc.attrs.label('foo')}");
        // tai.getValueExpression(fc, String.class);
        // -> exception

    }

    public void testPerformance() {

        // Arrange
        int loop = 100000;
        int expectedUsage = 10;

        List<String> ccExpressions = new ArrayList<String>();
        List<String> methExpressions = new ArrayList<String>();
        for (int i = 64; i > 0; i--) {
            ccExpressions.add("#{cc." + rndString(64) + "}");
            methExpressions.add("#{cc.attrs." + rndString(64) + "}");
        }

        FaceletContext fc = mockFaceletContext();
        TagAttributeImpl tai, tai2;
        ValueExpression ve;
        long start;

        // Act & Assert

        // warmup
        for (int i = 0; i < loop; i++) {
            int n = rnd.nextInt(64);
            tai = new TagAttributeImpl(null, null, null, null, ccExpressions.get(n));
            tai2 = new TagAttributeImpl(null, null, null, null, methExpressions.get(n));

            for (int u = 0; u < expectedUsage; u++) {
                ve = tai.getValueExpression(fc, String.class);
                assertNotNull(ve);
                ve = tai2.getValueExpression(fc, String.class);
                assertNotNull(ve);
            }
        }

        // no usage
        start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            int n = rnd.nextInt(64);
            tai = new TagAttributeImpl(null, null, null, null, ccExpressions.get(n));
            tai2 = new TagAttributeImpl(null, null, null, null, methExpressions.get(n));

        }
        long durationUsageNone = System.currentTimeMillis() - start;

        // used once
        start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            int n = rnd.nextInt(64);
            tai = new TagAttributeImpl(null, null, null, null, ccExpressions.get(n));
            tai2 = new TagAttributeImpl(null, null, null, null, methExpressions.get(n));

            ve = tai.getValueExpression(fc, String.class);
            assertNotNull(ve);
            ve = tai2.getValueExpression(fc, String.class);
            assertNotNull(ve);
        }
        long durationUsageOnce = System.currentTimeMillis() - start;

        // used multiple times
        start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            int n = rnd.nextInt(64);
            tai = new TagAttributeImpl(null, null, null, null, ccExpressions.get(n));
            tai2 = new TagAttributeImpl(null, null, null, null, methExpressions.get(n));

            for (int u = 0; u < expectedUsage; u++) {
                ve = tai.getValueExpression(fc, String.class);
                assertNotNull(ve);
                ve = tai2.getValueExpression(fc, String.class);
                assertNotNull(ve);
            }
        }
        long durationUsageMultiple = System.currentTimeMillis() - start;

        // Assert
        assertTrue(durationUsageNone + " > " + durationUsageOnce, durationUsageNone < durationUsageOnce);
        assertTrue(durationUsageMultiple + " > " + durationUsageOnce + " + 50%", durationUsageMultiple < durationUsageOnce * 1.5);
    }

    private final static String rndString(int length) {
        final CharSequence chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0, cl = chars.length(); i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(cl)));
        }
        return sb.toString();
    }

    private FaceletContext mockFaceletContext() {
        FaceletContext fc = new FaceletContext() {

            @Override
            public FacesContext getFacesContext() {
                return null;
            }

            @Override
            public String generateUniqueId(String base) {
                return null;
            }

            @Override
            public ExpressionFactory getExpressionFactory() {
                return new ExpressionFactory() {

                    @Override
                    public Object coerceToType(Object arg0, Class<?> arg1) throws ELException {
                        return null;
                    }

                    @Override
                    public MethodExpression createMethodExpression(ELContext arg0, String arg1, Class<?> arg2, Class<?>[] arg3) throws ELException, NullPointerException {
                        return null;
                    }

                    @Override
                    public ValueExpression createValueExpression(Object arg0, Class<?> arg1) {
                        return null;
                    }

                    @Override
                    public ValueExpression createValueExpression(ELContext arg0, String arg1, Class<?> arg2) throws NullPointerException, ELException {
                        return null;
                    }

                };
            }

            @Override
            public void setVariableMapper(VariableMapper varMapper) {
            }

            @Override
            public void setFunctionMapper(FunctionMapper fnMapper) {
            }

            @Override
            public void setAttribute(String name, Object value) {
            }

            @Override
            public Object getAttribute(String name) {
                return null;
            }

            @Override
            public void includeFacelet(UIComponent parent, String relativePath) throws IOException {
            }

            @Override
            public void includeFacelet(UIComponent parent, URL absolutePath) throws IOException {
            }

            @Override
            public ELResolver getELResolver() {
                return null;
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return null;
            }

            @Override
            public VariableMapper getVariableMapper() {
                return null;
            }

        };
        return fc;
    }

}
