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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * <p>
 * Abstract base class for {@link DataModel} tests.</p>
 */
public abstract class DataModelTestCaseBase extends TestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public DataModelTestCaseBase(String name) {

        super(name);

    }

    // ------------------------------------------------------ Instance Variables
    // The array of beans we will be wrapping (must be initialized before setUp)
    protected BeanTestImpl beans[] = new BeanTestImpl[0];

    // The DataModel we are testing
    protected DataModel<?> model = null;

    // ---------------------------------------------------- Overall Test Methods
    // Configure the properties of the beans we will be wrapping
    protected void configure() {
        for (int i = 0; i < beans.length; i++) {
            BeanTestImpl bean = beans[i];
            bean.setBooleanProperty((i % 2) == 0);
            bean.setBooleanSecond(!bean.getBooleanProperty());
            bean.setByteProperty((byte) i);
            bean.setDoubleProperty(i * 100.0);
            bean.setFloatProperty(i * ((float) 10.0));
            bean.setIntProperty(1000 * i);
            bean.setLongProperty((long) 10000 * (long) i);
            bean.setStringProperty("This is string " + i);
        }
    }

    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        // Subclasses must create "beans", call "configure()", create "model"
        super.setUp();
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(DataModelTestCaseBase.class));
    }

    // Tear down instance variables required by ths test case
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        beans = null;
        model = null;
    }

    // ------------------------------------------------- Individual Test Methods
    // Test invalid arguments to listener methods
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
    public void testPositionAscending() throws Exception {
        StringBuilder sb = new StringBuilder();
        model.setRowIndex(-1);
        model.addDataModelListener(new ListenerTestImpl());
        ListenerTestImpl.trace(null);

        int n = model.getRowCount();
        for (int i = 0; i < n; i++) {
            checkRow(i);
            sb.append('/').append(i);
        }
        assertEquals(sb.toString(), ListenerTestImpl.trace());
    }

    // Test positioning to all rows in descending order
    public void testPositionDescending() throws Exception {
        StringBuilder sb = new StringBuilder();
        model.setRowIndex(-1);
        model.addDataModelListener(new ListenerTestImpl());
        ListenerTestImpl.trace(null);

        int n = model.getRowCount();
        for (int i = (n - 1); i >= 0; i--) {
            checkRow(i);
            sb.append('/').append(i);
        }
        assertEquals(sb.toString(), ListenerTestImpl.trace());
    }

    // Test a pristine DataModel instance
    public void testPristine() throws Exception {
        // Unopened instance
        assertNotNull("beans exists", beans);
        assertNotNull("model exists", model);

        // Correct row count
        if (model instanceof ResultSetDataModel) {
            assertEquals("correct row count", -1, model.getRowCount());
        } else {
            assertEquals("correct row count", beans.length,
                    model.getRowCount());
        }

        // Correct row index
        assertEquals("correct row index", 0, model.getRowIndex());
    }

    // Test removing listener
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
    public void testReset() throws Exception {
        ListenerTestImpl listener = new ListenerTestImpl();
        ListenerTestImpl.trace(null);
        model.addDataModelListener(listener);

        assertEquals(0, model.getRowIndex());
        model.setWrappedData(model.getWrappedData());
        assertEquals("/0", ListenerTestImpl.trace());
    }

    // Test row available manipulations
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
    @SuppressWarnings("unchecked")
    public void testRowData() throws Exception {
        // Retrieve the row data for row zero
        model.setRowIndex(0);
        Object data = model.getRowData();
        assertNotNull(data);

        // Modify several property values
        BeanTestImpl bean = beans[0];
        bean.setBooleanProperty(!bean.getBooleanProperty());
        if (data instanceof Map) {
            ((Map<String, Boolean>) data).put("booleanProperty",
                    bean.getBooleanProperty()
                    ? Boolean.TRUE : Boolean.FALSE);
        } else {
            Method m = data.getClass().getMethod("setBooleanProperty", Boolean.TYPE);
            m.invoke(data, bean.getBooleanProperty() ? Boolean.TRUE : Boolean.FALSE);
        }
        bean.setIntProperty(bean.getIntProperty() + 5);
        if (data instanceof Map) {
            ((Map<String, Integer>) data).put("intProperty",
                    bean.getIntProperty());
        } else {
            Method m = data.getClass().getMethod("setIntProperty", Integer.TYPE);
            m.invoke(data, bean.getIntProperty());
        }
        bean.setStringProperty(bean.getStringProperty() + "XYZ");
        if (data instanceof Map) {
            ((Map<String, String>) data).put("stringProperty",
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
    public void testRowIndex() throws Exception {
        assertEquals("correct row index", 0, model.getRowIndex());

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

    public void testIterator() {
        Iterator<?> iterator = model.iterator();
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
        assertNotNull("Row " + i + " data", bean);
        assertEquals(prompt + "booleanProperty",
                beans[i].getBooleanProperty(),
                bean.getBooleanProperty());
        assertEquals(prompt + "booleanSecond",
                beans[i].isBooleanSecond(),
                bean.isBooleanSecond());
        assertEquals(prompt + "byteProperty",
                beans[i].getByteProperty(),
                bean.getByteProperty());
        assertEquals(prompt + "doubleProperty",
                "" + beans[i].getDoubleProperty(),
                "" + bean.getDoubleProperty());
        assertEquals(prompt + "floatProperty",
                "" + beans[i].getFloatProperty(),
                "" + bean.getFloatProperty());
        assertEquals(prompt + "intProperty",
                beans[i].getIntProperty(),
                bean.getIntProperty());
        assertEquals(prompt + "longProperty",
                beans[i].getLongProperty(),
                bean.getLongProperty());
        assertEquals(prompt + "nullProperty",
                beans[i].getNullProperty(),
                bean.getNullProperty());
        assertEquals(prompt + "readOnlyProperty",
                beans[i].getReadOnlyProperty(),
                bean.getReadOnlyProperty());
        assertEquals(prompt + "shortProperty",
                beans[i].getShortProperty(),
                bean.getShortProperty());
        assertEquals(prompt + "stringProperty",
                beans[i].getStringProperty(),
                bean.getStringProperty());
        assertEquals(prompt + "writeOnlyProperty",
                beans[i].getWriteOnlyPropertyValue(),
                bean.getWriteOnlyPropertyValue());
    }
}
