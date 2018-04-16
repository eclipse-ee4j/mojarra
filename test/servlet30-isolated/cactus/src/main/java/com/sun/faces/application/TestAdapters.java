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

package com.sun.faces.application;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.el.ValueBinding;
import javax.faces.el.PropertyNotFoundException;

import com.sun.faces.cactus.ServletFacesTestCase;

/**
 * This test case will validate  the various adapter classes
 * found in jsf-api/template-src
 */
 @SuppressWarnings("deprecation")
public class TestAdapters extends ServletFacesTestCase {

    ExpressionFactory factory;
    
    public TestAdapters() {
        super("TestAdapters");
    }


    public TestAdapters(String name) {
        super(name);
    }


    @Override public void setUp() {
        try {
            factory = (ExpressionFactory) 
                  Class.forName("com.sun.el.ExpressionFactoryImpl").newInstance();
        } catch (Exception e) {
            System.out.println(e);
        }
        super.setUp();
    }


    @Override public void tearDown() {
        super.tearDown();
    }
    
    // ------------------------------------------------------------ Test Methods
    
    
    public void testMEMBAdapterTest() throws Exception {
        // Phase 1
        //   - validate NPEs are thrown as expected
        TestMethodBinding binding =  new TestMethodBinding("#{simple.invoke}", 
                                                           Double.class, 
                                                           new MySimpleBean());        
        MethodExpressionMethodBindingAdapter meAdapter = 
              new MethodExpressionMethodBindingAdapter(binding);
        FacesContext fContext = getFacesContext();
        ExternalContext extContext = fContext.getExternalContext();
        TestMethodExpression methodExpr = new TestMethodExpression("invoke",
                                                                   "#{foo.invoke}",
                                                                   Double.class,
                                                                   new Class[] { String.class },
                                                                   new MySimpleBean());
        TestMethodExpression falseExpr = new TestMethodExpression("invoke",
                                                                  "#{foo.invoke}",
                                                                  String.class,
                                                                  new Class[] { String.class },
                                                                  new MySimpleBean());
        extContext.getRequestMap().put("simple", new MySimpleBean());
        
        // Phase 1
        //   - validate NPEs are thrown as expected
        try {
            meAdapter.getMethodInfo(null);
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
        try {
            meAdapter.invoke(null, new Object[] { "1.4" });
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
        // Phase 2
        //   - validate methods with correct input
        MethodInfo info = meAdapter.getMethodInfo(fContext.getELContext());
        assertTrue(Double.class.equals(info.getReturnType()));
        
        Object obj = meAdapter.invoke(fContext.getELContext(), new Object[] { "1.3" });
        assertTrue (obj instanceof Double);
        assertTrue ("1.3".equals(obj.toString()));
        
        // Phase 3
        //   - validate the equals() method
        
        assertTrue(!meAdapter.equals(null));
        // if the reference is to the same object, it should return true
        assertTrue(meAdapter.equals(meAdapter));        
        
        // if the argument passed is another MethodExpressionMethodBindingAdapter
        // with the same binding, it should return true
        MethodExpressionMethodBindingAdapter meTrue = 
              new MethodExpressionMethodBindingAdapter(binding);
        MethodExpressionMethodBindingAdapter meFalse =
              new MethodExpressionMethodBindingAdapter(new TestMethodBinding("#{foo.invoke}", 
                                                                             String.class, 
                                                                             new MySimpleBean()));
         assertTrue(meAdapter.equals(meTrue));
         assertTrue(!meAdapter.equals(meFalse));
        
         // if a MethodBinding is provided, then a little more work will
         // be performed - ensure this works
         if (factory != null) {
            ApplicationAssociate.getInstance(extContext).setExpressionFactory(factory);            
            assertTrue(meAdapter.equals(methodExpr));
            assertTrue(!meAdapter.equals(falseExpr));
         }
    }
    
    
    public void testMBMEAdapterTest() throws Exception {
        
        TestMethodExpression expression = new TestMethodExpression("invoke",
                                                                   "#{simple.invoke}",
                                                                   Double.class,
                                                                   new Class[] { String.class },
                                                                   new MySimpleBean());
        MethodBindingMethodExpressionAdapter mbAdapter = 
              new MethodBindingMethodExpressionAdapter(expression);
        FacesContext fContext = getFacesContext();
        ExternalContext extContext = fContext.getExternalContext();
        TestMethodBinding methodBinding = new TestMethodBinding("#{foo.invoke}",
                                                                Double.class, 
                                                                new MySimpleBean());
        TestMethodBinding falseBinding = new TestMethodBinding("#{foo.invoke}",
                                                                String.class,
                                                                new MySimpleBean());
        extContext.getRequestMap().put("foo", new MySimpleBean());
        
        // Phase 1
        //   - validate NPEs are thrown as expected
        try {
            mbAdapter.getType(null);
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
        try {
            mbAdapter.invoke(null, new Object[]{ "" });
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
        // Phase 2
        //   - validate methods with correct input        
        assertTrue(Double.class.equals(mbAdapter.getType(fContext)));
        
        Object obj = mbAdapter.invoke(fContext, new Object[] { "1.3" });
        assertTrue (obj instanceof Double);
        assertTrue ("1.3".equals(obj.toString()));
        
        // Phase 3
        //   - validate the equals() method
        
        assertTrue(!mbAdapter.equals(null));
        // if the reference is to the same object, it should return true
        assertTrue(mbAdapter.equals(mbAdapter));        
        
        // if the argument passed is another MethodExpressionMethodBindingAdapter
        // with the same binding, it should return true
        MethodBindingMethodExpressionAdapter mbTrue = 
              new MethodBindingMethodExpressionAdapter(expression);
        MethodBindingMethodExpressionAdapter mbFalse =
              new MethodBindingMethodExpressionAdapter(new TestMethodExpression("invoke",
                                                                   "#{foo.invoke}",
                                                                   String.class,
                                                                   new Class[] { Double.class },
                                                                   new MySimpleBean()));
         assertTrue(mbAdapter.equals(mbTrue));
         assertTrue(!mbAdapter.equals(mbFalse));
        
         // if a MethodBinding is provided, then a little more work will
         // be performed - ensure this works
         if (factory != null) {
            ApplicationAssociate.getInstance(extContext).setExpressionFactory(factory);            
            assertTrue(mbAdapter.equals(methodBinding));
            assertTrue(!mbAdapter.equals(falseBinding));
         }
    }
    
    
    public void testVEVBAdapterTest() throws Exception {
        TestValueBinding binding = new TestValueBinding("#{simple.double}",
                                                        new MySimpleBean(),
                                                        Double.class);
        ValueExpressionValueBindingAdapter veAdapter =
              new ValueExpressionValueBindingAdapter(binding);
        FacesContext fContext = getFacesContext();
        ELContext elContext = fContext.getELContext();
        
        // Phase 1
        //   - validate NPEs are thrown as expected
        try {
            veAdapter.getType(null);
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
        try {
            veAdapter.getValue(null);
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
        try {
            veAdapter.setValue(null, "string");
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
        try {
            veAdapter.isReadOnly(null);
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
        // Phase 2
        //   - validate methods with correct input
        assertTrue(Double.class.equals(veAdapter.getType(elContext)));
        assertTrue(Double.valueOf("1.5").equals(veAdapter.getValue(elContext)));
        assertTrue(veAdapter.isReadOnly(elContext));
        
        // Phase 3
        //   - validate the equals() method
        assertTrue(veAdapter.equals(veAdapter));
        ValueExpressionValueBindingAdapter trueAdapter =
            new ValueExpressionValueBindingAdapter(binding);
        ValueExpressionValueBindingAdapter falseAdapter =
           new ValueExpressionValueBindingAdapter(
                 new TestValueBinding("#{simple.double}",
                                      new MySimpleBean(),
                                      String.class));
        assertTrue(veAdapter.equals(trueAdapter));
        assertTrue(!veAdapter.equals(falseAdapter));

        ValueExpression trueVE = new TestValueExpression("#{ping.double}",
                                                         Double.class,
                                                         new MySimpleBean());
        ValueExpression falseVE = new TestValueExpression("#{foo.double}",
                                                          String.class,
                                                          new MySimpleBean());
        assertTrue(veAdapter.equals(trueVE));
        assertTrue(!veAdapter.equals(falseVE));

    }
    
   
    public void testVBVEAdapterTest() throws Exception {
        ValueExpression expression = new TestValueExpression("#{simple.double}",
                                                             Double.class,
                                                             new MySimpleBean());
         ValueBindingValueExpressionAdapter vbAdapter =
              new ValueBindingValueExpressionAdapter(expression);
        FacesContext fContext = getFacesContext();        
        
        // Phase 1
        //   - validate NPEs are thrown as expected
        try {
            vbAdapter.getType(null);
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
        try {
            vbAdapter.getValue(null);
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
         try {
            vbAdapter.isReadOnly(null);
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
         try {
            vbAdapter.setValue(null, "string");
            assertTrue(false);
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                assertTrue(false);
            }
        }
        
         // Phase 2
        //   - validate methods with correct input
        assertTrue(Double.class.equals(vbAdapter.getType(fContext)));
        assertTrue(new Double(1.5).equals(vbAdapter.getValue(fContext)));
        assertTrue(vbAdapter.isReadOnly(fContext));
        
        // Phase 3
        //   - validate the equals() method
        assertTrue(vbAdapter.equals(vbAdapter));
        ValueBindingValueExpressionAdapter trueAdapter =
            new ValueBindingValueExpressionAdapter(expression);
        ValueBindingValueExpressionAdapter falseAdapter =
           new ValueBindingValueExpressionAdapter(
                 new TestValueExpression("#{foo.double}",
                                      String.class,
                                      new MySimpleBean()));
        assertTrue(vbAdapter.equals(trueAdapter));
        assertTrue(!vbAdapter.equals(falseAdapter));

        ValueBinding trueVB = new TestValueBinding("#{ping.double}",
                                                   new MySimpleBean(),
                                                   Double.class);
        ValueBinding falseVB = new TestValueBinding("#{foo.double}",
                                                    new MySimpleBean(),
                                                    String.class);
        assertTrue(vbAdapter.equals(trueVB));
        assertTrue(!vbAdapter.equals(falseVB));
    }
    
    // ----------------------------------------------------------- Inner Classes
    
    private static class MySimpleBean {
        
        Double value = 1.5;
        
        public Double getDouble() {
            return value;
        }
        
        public Double invoke(String value) {
            return Double.valueOf(value);
        }
        
        public String invoked(String value) {
            return value;
        }
    }
    
    
    private static class TestValueExpression extends ValueExpression {
        
        private String expr;
        private Class<?> returnType;
        private MySimpleBean bean;
        
        public TestValueExpression(String expr,
                                   Class<?> returnType,
                                   MySimpleBean bean) {
            this.expr = expr;
            this.returnType = returnType;
            this.bean = bean;
        }

        public Object getValue(ELContext elContext) {
            if (elContext == null) {
                throw new NullPointerException();
            }
            return bean.getDouble();
        }

        public void setValue(ELContext elContext, Object object) {
            if (elContext == null) {
                throw new NullPointerException();
            }
        }

        public boolean isReadOnly(ELContext elContext) {
            if (elContext == null) {
                throw new NullPointerException();
            }
            return true;
        }

        public Class<?> getType(ELContext elContext) {
            if (elContext == null) {
                throw new NullPointerException();
            }
            return returnType;
        }

        public Class<?> getExpectedType() {
            return returnType;    
        }

        public String getExpressionString() {
            return expr;
        }

        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            
            if (object instanceof TestValueExpression) {
                TestValueExpression v = (TestValueExpression) object;
                return v.getExpressionString().equals(expr)
                       && v.getExpectedType().equals(returnType);
            }
            
            return false;
        }

        public int hashCode() {
            return (expr.hashCode() ^ returnType.hashCode());            
        }

        public boolean isLiteralText() {
            return false;  
        }
    }
    
       
    private static class TestValueBinding extends ValueBinding {

        String expr;
        MySimpleBean bean;
        Class<?> returnType;
        
        public TestValueBinding(String expr,
                                MySimpleBean bean,
                                Class<?> returnType) {
            this.expr = expr;
            this.bean = bean;
            this.returnType = returnType;
        }
       
        public Object getValue(FacesContext context)
        throws EvaluationException, PropertyNotFoundException {
            if (context == null) {
                throw new NullPointerException();
            }
            return bean.getDouble();
        }

       
        public void setValue(FacesContext context, Object value)
        throws EvaluationException, PropertyNotFoundException {
            if (context == null) {
                throw new NullPointerException();
            }            
        }

       
        public boolean isReadOnly(FacesContext context)
        throws EvaluationException, PropertyNotFoundException {
            if (context == null) {
                throw new NullPointerException();
            }
            return true;
        }
        
               
        public Class getType(FacesContext context)
        throws EvaluationException, PropertyNotFoundException {
            if (context == null) {
                throw new NullPointerException();
            }
            return returnType;
        }
    }
    
   
    private static class TestMethodBinding extends MethodBinding {
        
        String exprString;
        Class<?> returnType;
        MySimpleBean bean;
        
        public TestMethodBinding(String exprString,
                                 Class<?> returnType, 
                                 MySimpleBean bean) {
            this.exprString = exprString;
            this.returnType = returnType;
            this.bean = bean;
        }

       
        public Object invoke(FacesContext context, Object[] params)
        throws EvaluationException, MethodNotFoundException {
            if (context == null) {
                throw new NullPointerException();
            }
            return bean.invoke((String) params[0]);
        }

        
        public Class getType(FacesContext context) throws MethodNotFoundException {
            if (context == null) {
                throw new NullPointerException();
            }
            return returnType;
        }

        
        @Override public String getExpressionString() {
            return exprString;
        }
    }

    
    private static class TestMethodExpression extends MethodExpression {

        private String methodName;
        private String exprString;
        private Class<?> returnType;
        private Class<?>[] params;
        private MySimpleBean bean;
        private MethodInfo info;
        
        public TestMethodExpression(String methodName,
                                    String exprString,
                                    Class<?> returnType,
                                    Class<?>[] params,
                                    MySimpleBean bean) {
            this.methodName = methodName;
            this.exprString = exprString;
            this.returnType = returnType;
            this.params = params;
            this.bean = bean;
            info = new MethodInfo(methodName, returnType, params);
        }
        public MethodInfo getMethodInfo(ELContext elContext) {
            if (elContext == null) {
                throw new NullPointerException();
            }
            return info;
        }

        public Object invoke(ELContext elContext, Object[] objects) {
            if (elContext == null) {
                throw new NullPointerException();
            }
            return bean.invoke((String) objects[0]);
        }

        public String getExpressionString() {
            return exprString;
        }

        public boolean equals(Object object) {
            return this == object
                   || object instanceof TestMethodExpression
                      && (exprString.equals(
                  ((TestMethodExpression) object).getExpressionString()));

        }

        public int hashCode() {
            return exprString.hashCode(); 
        }

        public boolean isLiteralText() {
            return false;
        }
    }
   


} // END TestAdapters
