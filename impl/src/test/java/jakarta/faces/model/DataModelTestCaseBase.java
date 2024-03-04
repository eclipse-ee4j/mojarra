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

package jakarta.faces.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

/**
 * <p>
 * Abstract base class for {@link DataModel} tests.</p>
 */
public abstract class DataModelTestCaseBase {

    // ------------------------------------------------------ Instance Variables
    // The array of beans we will be wrapping (must be initialized before setUp)
    protected BeanTestImpl beans[] = new BeanTestImpl[0];

    // The DataModel we are testing
    protected DataModel model = null;

    // ---------------------------------------------------- Overall Test Methods
    // Configure the properties of the beans we will be wrapping
    protected void configure() {
        for (int i = 0; i < beans.length; i++) {
            BeanTestImpl bean = beans[i];
            bean.setBooleanProperty((i % 2) == 0);
            bean.setBooleanSecond(!bean.getBooleanProperty());
            bean.setByteProperty((byte) i);
            bean.setDoubleProperty((i) * 100.0);
            bean.setFloatProperty((i) * ((float) 10.0));
            bean.setIntProperty(1000 * i);
            bean.setLongProperty((long) 10000 * (long) i);
            bean.setStringProperty("This is string " + i);
        }
    }

    // ------------------------------------------------- Individual Test Methods

    // Test invalid arguments to listener methods
    @Test
    public void testInvalidListeners() throws Exception {
        try {
            model.addDataModelListener(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }

        try {
            model.removeDataModelListener(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected result
        }
    }

    // Test positioning to all rows in ascending order
    @Test
    public void testPositionAscending() throws Exception {
        StringBuffer sb = new StringBuffer();
        model.setRowIndex(-1);
        model.addDataModelListener(new ListenerTestImpl());
        ListenerTestImpl.trace(null);

        int n = model.getRowCount();
        for (int i = 0; i < n; i++) {
            checkRow(i);
            sb.append("/").append(i);
        }
        assertEquals(sb.toString(), ListenerTestImpl.trace());
    }

    // Test positioning to all rows in descending order
    @Test
    public void testPositionDescending() throws Exception {
        StringBuffer sb = new StringBuffer();
        model.setRowIndex(-1);
        model.addDataModelListener(new ListenerTestImpl());
        ListenerTestImpl.trace(null);

        int n = model.getRowCount();
        for (int i = (n - 1); i >= 0; i--) {
            checkRow(i);
            sb.append("/").append(i);
        }
        assertEquals(sb.toString(), ListenerTestImpl.trace());
    }

    // Test a pristine DataModel instance
    @Test
    public void testPristine() throws Exception {
        // Unopened instance
        assertNotNull(beans);
        assertNotNull(model);

        // Correct row count
        if (model instanceof ResultSetDataModel) {
            assertEquals(-1, model.getRowCount());
        } else {
            assertEquals(beans.length,
                    model.getRowCount());
        }

        // Correct row index
        assertEquals(0, model.getRowIndex());
    }

    // Test removing listener
    @Test
    public void testRemoveListener() throws Exception {
        ListenerTestImpl listener = new ListenerTestImpl();
        ListenerTestImpl.trace(null);
        model.addDataModelListener(listener);
        model.setRowIndex(-1);
        model.setRowIndex(0);
        model.setRowIndex(0); // No movement so no event
        model.setRowIndex(-1);
        model.removeDataModelListener(listener);
        model.setRowIndex(0);
        assertEquals("/-1/0/-1", ListenerTestImpl.trace());
    }

    // Test resetting the wrapped data (should trigger an event
    @Test
    public void testReset() throws Exception {
        ListenerTestImpl listener = new ListenerTestImpl();
        ListenerTestImpl.trace(null);
        model.addDataModelListener(listener);

        assertEquals(0, model.getRowIndex());
        model.setWrappedData(model.getWrappedData());
        assertEquals("/0", ListenerTestImpl.trace());
    }

    // Test row available manipulations
    @Test
    public void testRowAvailable() throws Exception {
        // Position to the "no current row" position
        model.setRowIndex(-1);
        assertTrue(!model.isRowAvailable());

        // Position to an arbitrarily high row number
        model.setRowIndex(beans.length);
        assertTrue(!model.isRowAvailable());

        // Position to a known good row number
        model.setRowIndex(0);
        assertTrue(model.isRowAvailable());
    }

    // Test the ability to update through the Map returned by getRowData()
    @Test
    public void testRowData() throws Exception {
        // Retrieve the row data for row zero
        model.setRowIndex(0);
        Object data = model.getRowData();
        assertNotNull(data);

        // Modify several property values
        BeanTestImpl bean = beans[0];
        bean.setBooleanProperty(!bean.getBooleanProperty());
        if (data instanceof Map) {
            ((Map) data).put("booleanProperty",
                    bean.getBooleanProperty()
                    ? Boolean.TRUE : Boolean.FALSE);
        } else {
            Method m = data.getClass().getMethod("setBooleanProperty", Boolean.TYPE);
            m.invoke(data, bean.getBooleanProperty() ? Boolean.TRUE : Boolean.FALSE);
        }
        bean.setIntProperty(bean.getIntProperty() + 5);
        if (data instanceof Map) {
            ((Map) data).put("intProperty",
                    bean.getIntProperty());
        } else {
            Method m = data.getClass().getMethod("setIntProperty", Integer.TYPE);
            m.invoke(data, bean.getIntProperty());
        }
        bean.setStringProperty(bean.getStringProperty() + "XYZ");
        if (data instanceof Map) {
            ((Map) data).put("stringProperty",
                    bean.getStringProperty() + "XYZ");
        } else {
            Method m = data.getClass().getMethod("setStringProperty", String.class);
            m.invoke(data, bean.getStringProperty());
        }

        // Ensure that all the modifications flowed through to beans[0]
        assertEquals(bean.getBooleanProperty(),
                beans[0].getBooleanProperty());
        assertEquals(bean.isBooleanSecond(),
                beans[0].isBooleanSecond());
        assertEquals(bean.getByteProperty(),
                beans[0].getByteProperty());
        assertEquals(bean.getDoubleProperty(),
                beans[0].getDoubleProperty(), 0.005);
        assertEquals(bean.getFloatProperty(),
                beans[0].getFloatProperty(), (float) 0.005);
        assertEquals(bean.getIntProperty(),
                beans[0].getIntProperty());
        assertEquals(bean.getLongProperty(),
                beans[0].getLongProperty());
        assertEquals(bean.getStringProperty(),
                beans[0].getStringProperty());
    }

    // Test row index manipulations
    @Test
    public void testRowIndex() throws Exception {
        assertEquals(0, model.getRowIndex());

        // Positive setRowIndex() tests
        model.setRowIndex(0);
        model.setRowIndex(-1);

        // Negative setRowIndex() tests
        try {
            model.setRowIndex(-2);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
    }

    @Test
    public void testIterator() {
        Iterator iterator = model.iterator();
        if (!(model instanceof ScalarDataModel)) {
            for (int i = 0; i < 5; i++) {
                System.out.println("Index: " + i);
                assertTrue(iterator.hasNext());
                assertNotNull(iterator.next());
            }
        } else {
            assertTrue(iterator.hasNext());
            assertNotNull(iterator.next());
        }

        assertTrue(!iterator.hasNext());
        try {
            iterator.next();
            assertTrue(false);
        } catch (NoSuchElementException nsee) {
            // expected
        }
    }

    // Test resetting the wrapped data to null
    @Test
    public void testWrapped() throws Exception {
        model.setWrappedData(null);
        assertTrue(!model.isRowAvailable());
        assertEquals(-1, model.getRowCount());
        assertNull(model.getRowData());
        assertEquals(-1, model.getRowIndex());
        assertNull(model.getWrappedData());
    }

    // ------------------------------------------------------- Protected Methods
    protected BeanTestImpl data() throws Exception {
        Object data = model.getRowData();
        assertNotNull(data);
        assertTrue(data instanceof BeanTestImpl);
        return ((BeanTestImpl) data);
    }

    protected void checkRow(int i) throws Exception {
        model.setRowIndex(i);
        String prompt = "Row " + i + " property ";
        BeanTestImpl bean = data();
        assertNotNull(bean, "Row " + i + " data");
        assertEquals(
                beans[i].getBooleanProperty(),
                bean.getBooleanProperty(), prompt + "booleanProperty");
        assertEquals(
                beans[i].isBooleanSecond(),
                bean.isBooleanSecond(), prompt + "booleanSecond");
        assertEquals(
                beans[i].getByteProperty(),
                bean.getByteProperty(), prompt + "byteProperty");
        assertEquals(
                "" + beans[i].getDoubleProperty(),
                "" + bean.getDoubleProperty(), prompt + "doubleProperty");
        assertEquals(
                "" + beans[i].getFloatProperty(),
                "" + bean.getFloatProperty(), prompt + "floatProperty");
        assertEquals(
                beans[i].getIntProperty(),
                bean.getIntProperty(), prompt + "intProperty");
        assertEquals(
                beans[i].getLongProperty(),
                bean.getLongProperty(), prompt + "longProperty");
        assertEquals(
                beans[i].getNullProperty(),
                bean.getNullProperty(), prompt + "nullProperty");
        assertEquals(
                beans[i].getReadOnlyProperty(),
                bean.getReadOnlyProperty(), prompt + "readOnlyProperty");
        assertEquals(
                beans[i].getShortProperty(),
                bean.getShortProperty(), prompt + "shortProperty");
        assertEquals(
                beans[i].getStringProperty(),
                bean.getStringProperty(), prompt + "stringProperty");
        assertEquals(
                beans[i].getWriteOnlyPropertyValue(),
                bean.getWriteOnlyPropertyValue(), prompt + "writeOnlyProperty");
    }
}
