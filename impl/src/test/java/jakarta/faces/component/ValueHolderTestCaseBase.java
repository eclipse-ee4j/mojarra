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

package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.convert.LongConverter;
import jakarta.faces.convert.NumberConverter;
import jakarta.faces.convert.ShortConverter;

/**
 * <p>
 * Unit tests for {@link ValueHolder}. Any test case for a component class that implements {@link ValueHolder} should
 * extend this class.
 * </p>
 */
public abstract class ValueHolderTestCaseBase extends UIComponentBaseTestCase {

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        component = new UIOutput();
        expectedId = null;
        expectedRendererType = "Text";
    }

    // ------------------------------------------------- Individual Test Methods
    @Test
    public void testAttributesTransparencyNonDeterministic() throws Exception {
        final int numThreads = 30;
        final Boolean outcomes[] = new Boolean[numThreads];
        Runnable runnables[] = new Runnable[numThreads];
        int i = 0;

        for (i = 0; i < outcomes.length; i++) {
            outcomes[i] = null;
        }

        for (i = 0; i < runnables.length; i++) {
            runnables[i] = new Runnable() {
                @Override
                public void run() {
                    int threadNum = 0;
                    try {
                        threadNum = Integer.valueOf(Thread.currentThread().getName()).intValue();
                    } catch (NumberFormatException ex) {
                        fail("Expected thread name to be an integer");
                    }
                    // Even threadNums use HtmlInputText, odd use this component
                    boolean isEven = threadNum % 2 == 0;
                    ValueHolder vh = null;
                    UIComponent newComp = null;
                    if (isEven) {
                        newComp = new HtmlInputText();
                        vh = (ValueHolder) newComp;
                    } else {
                        try {
                            newComp = ValueHolderTestCaseBase.this.component.getClass().getDeclaredConstructor()
                                    .newInstance();
                            vh = (ValueHolder) newComp;

                        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException ex) {
                            fail("Can't instantiate class of " + ValueHolderTestCaseBase.this.component.getClass().getName());
                        }
                    }
                    try {
                        boolean result = doTestAttributesTransparency(vh, newComp);
                        outcomes[threadNum] = Boolean.valueOf(result);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        outcomes[threadNum] = Boolean.FALSE;
                    }
                }
            };
        }
//        clearDescriptors();
        Thread thread = null;
        for (i = 0; i < runnables.length; i++) {
            thread = new Thread(runnables[i], "" + i);
            thread.start();
        }

        // Keep polling the outcomes array until there are no nulls.
        boolean foundNull = false;
        while (!foundNull) {
            for (i = 0; i < outcomes.length; i++) {
                if (null != outcomes[i]) {
                    foundNull = true;
                }
            }
            Thread.sleep(500);
        }

        for (i = 0; i < outcomes.length; i++) {
            if (!outcomes[i].booleanValue()) {
                fail("Thread " + i + " failed");
            }
        }
    }

    @Override
    @Test
    public void testAttributesTransparency() {
        super.testAttributesTransparency();
        ValueHolder vh = (ValueHolder) component;
        doTestAttributesTransparency(vh, component);
    }

    // Test attribute-property transparency
    public boolean doTestAttributesTransparency(ValueHolder vh, UIComponent newComp) {
        assertEquals(vh.getValue(), newComp.getAttributes().get("value"));
        vh.setValue("foo");
        assertEquals("foo", newComp.getAttributes().get("value"));
        vh.setValue(null);
        assertNull(newComp.getAttributes().get("value"));
        newComp.getAttributes().put("value", "bar");
        assertEquals("bar", vh.getValue());
        newComp.getAttributes().put("value", null);
        assertNull(vh.getValue());

        assertEquals(vh.getConverter(), newComp.getAttributes().get("converter"));
        vh.setConverter(new LongConverter());
        assertNotNull(newComp.getAttributes().get("converter"));
        assertTrue(newComp.getAttributes().get("converter") instanceof LongConverter);
        vh.setConverter(null);
        assertNull(newComp.getAttributes().get("converter"));
        newComp.getAttributes().put("converter", new ShortConverter());
        assertNotNull(vh.getConverter());
        assertTrue(vh.getConverter() instanceof ShortConverter);
        newComp.getAttributes().put("converter", null);
        assertNull(vh.getConverter());

        return true;
    }

    // Suppress lifecycle tests since we do not have a renderer
    @Override
    @Test
    public void testLifecycleManagement() {
    }

    // Test a pristine ValueHolderBase instance
    @Override
    @Test
    public void testPristine() {
        super.testPristine();
        ValueHolder vh = (ValueHolder) component;

        // Validate properties
        assertNull(vh.getValue());
        assertNull(vh.getConverter());
    }

    // Test setting properties to valid values
    @Override
    @Test
    public void testPropertiesValid() throws Exception {
        super.testPropertiesValid();
        ValueHolder vh = (ValueHolder) component;

        // value
        vh.setValue("foo.bar");
        assertEquals("foo.bar", vh.getValue());
        vh.setValue(null);
        assertNull(vh.getValue());

        // converter
        vh.setConverter(new LongConverter());
        assertTrue(vh.getConverter() instanceof LongConverter);
        vh.setConverter(null);
        assertNull(vh.getConverter());
    }

    // --------------------------------------------------------- Support Methods
    // Check that the properties of the NumberConverters are equal
    protected void checkNumberConverter(NumberConverter nc1, NumberConverter nc2) {
        assertEquals(nc1.getCurrencyCode(), nc2.getCurrencyCode());
        assertEquals(nc1.getCurrencySymbol(), nc2.getCurrencySymbol());
        assertEquals(nc1.isGroupingUsed(), nc2.isGroupingUsed());
        assertEquals(nc1.isIntegerOnly(), nc2.isIntegerOnly());
        assertEquals(nc1.getMaxFractionDigits(), nc2.getMaxFractionDigits());
        assertEquals(nc1.getMaxIntegerDigits(), nc2.getMaxIntegerDigits());
        assertEquals(nc1.getMinFractionDigits(), nc2.getMinFractionDigits());
        assertEquals(nc1.getMinIntegerDigits(), nc2.getMinIntegerDigits());
        assertEquals(nc1.getLocale(), nc2.getLocale());
        assertEquals(nc1.getPattern(), nc2.getPattern());
        assertEquals(nc1.getType(), nc2.getType());
    }

    // Check that the properties on the specified components are equal
    @Override
    protected void checkProperties(UIComponent comp1, UIComponent comp2) {
        super.checkProperties(comp1, comp2);
        ValueHolder vh1 = (ValueHolder) comp1;
        ValueHolder vh2 = (ValueHolder) comp2;
        assertEquals(vh1.getValue(), vh2.getValue());
        checkNumberConverter((NumberConverter) vh1.getConverter(), (NumberConverter) vh2.getConverter());
    }

    // Create and configure a NumberConverter
    protected NumberConverter createNumberConverter() {
        NumberConverter nc = new NumberConverter();
        nc.setCurrencyCode("USD");
        nc.setCurrencySymbol("$");
        nc.setGroupingUsed(false);
        nc.setIntegerOnly(true);
        nc.setMaxFractionDigits(2);
        nc.setMaxIntegerDigits(10);
        nc.setMinFractionDigits(2);
        nc.setMinIntegerDigits(5);
        nc.setType("currency");
        return nc;
    }

    protected void checkNumberConverters(NumberConverter nc1, NumberConverter nc2) {
        assertNotNull(nc1);
        assertNotNull(nc2);
        assertEquals(nc1.getCurrencyCode(), nc2.getCurrencyCode());
        assertEquals(nc1.getCurrencySymbol(), nc2.getCurrencySymbol());
        assertEquals(nc1.isGroupingUsed(), nc2.isGroupingUsed());
        assertEquals(nc1.isIntegerOnly(), nc2.isIntegerOnly());
        assertEquals(nc1.getMaxFractionDigits(), nc2.getMaxFractionDigits());
        assertEquals(nc1.getMaxIntegerDigits(), nc2.getMaxIntegerDigits());
        assertEquals(nc1.getMinFractionDigits(), nc2.getMinFractionDigits());
        assertEquals(nc1.getMinIntegerDigits(), nc2.getMinIntegerDigits());
        assertEquals(nc1.getLocale(), nc2.getLocale());
        assertEquals(nc1.getPattern(), nc2.getPattern());
        assertEquals(nc1.getType(), nc2.getType());
    }
}
