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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.mock.MockResultSet;

/**
 * <p>
 * Unit tests for {@link ArrayDataModel}.</p>
 */
public class ResultSetDataModelTestCase extends DataModelTestCaseBase {

    // ------------------------------------------------------ Instance Variables
    // The ResultSet passed to our ResultSetDataModel
    protected MockResultSet result = null;

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @BeforeEach
    public void setUp() throws Exception {
        beans = new BeanTestImpl[5];
        for (int i = 0; i < beans.length; i++) {
            beans[i] = new BeanTestImpl();
        }
        configure();
        result = new MockResultSet(beans);
        model = new ResultSetDataModel(result);
    }

    // ------------------------------------------------- Individual Test Methods
    // Test ((Map) getRowData()).containsKey()
    @Test
    public void testRowDataContainsKey() throws Exception {
        // Position to row 1 and retrieve the corresponding Map
        model.setRowIndex(1);
        assertTrue(model.isRowAvailable());
        Object data = model.getRowData();
        assertNotNull(data);
        assertTrue(data instanceof Map);
        Map map = (Map) data;

        // Test exact match on column names
        assertTrue(map.containsKey("booleanProperty"));
        assertTrue(map.containsKey("booleanSecond"));
        assertTrue(map.containsKey("byteProperty"));
        assertTrue(map.containsKey("doubleProperty"));
        assertTrue(map.containsKey("floatProperty"));
        assertTrue(map.containsKey("intProperty"));
        assertTrue(map.containsKey("longProperty"));
        assertTrue(map.containsKey("stringProperty"));

        // Test inexact match on column names
        assertTrue(map.containsKey("booleanPROPERTY"));
        assertTrue(map.containsKey("booleanSECOND"));
        assertTrue(map.containsKey("bytePROPERTY"));
        assertTrue(map.containsKey("doublePROPERTY"));
        assertTrue(map.containsKey("floatPROPERTY"));
        assertTrue(map.containsKey("intPROPERTY"));
        assertTrue(map.containsKey("longPROPERTY"));
        assertTrue(map.containsKey("stringPROPERTY"));

        // Test false return on invalid column names
        assertTrue(!map.containsKey("foo"));
        assertTrue(!map.containsKey("FOO"));
        assertTrue(!map.containsKey("bar"));
        assertTrue(!map.containsKey("BAR"));
    }

    // Test ((Map) getRowData()).containsValue()
    @Test
    public void testRowDataContainsValue() throws Exception {
        // Position to row 1 and retrieve the corresponding Map
        model.setRowIndex(1);
        assertTrue(model.isRowAvailable());
        Object data = model.getRowData();
        assertNotNull(data);
        assertTrue(data instanceof Map);
        Map map = (Map) data;

        // Test positive results
        assertTrue(map.containsValue(Boolean.TRUE));
        assertTrue(map.containsValue(Boolean.FALSE));
        assertTrue(map.containsValue(Byte.valueOf((byte) 1)));
        assertTrue(map.containsValue(Double.valueOf(100.0)));
        assertTrue(map.containsValue(Float.valueOf((float) 10.0)));
        assertTrue(map.containsValue(Integer.valueOf(1000)));
        assertTrue(map.containsValue(Long.valueOf(10000l)));
        assertTrue(map.containsValue("This is string 1"));

        // Test negative results
        assertTrue(!map.containsValue("foo"));
        assertTrue(!map.containsValue(Integer.valueOf(654321)));
    }

    // Test ((Map) getRowData()).entrySet()
    @Test
    public void testRowDataEntrySet() throws Exception {
        // Position to row 1 and retrieve the corresponding Map
        model.setRowIndex(1);
        assertTrue(model.isRowAvailable());
        Object data = model.getRowData();
        assertNotNull(data);
        assertTrue(data instanceof Map);
        Map map = (Map) data;
        Set set = map.entrySet();

        // Test exact match postive results
        assertTrue(set.contains(new TestEntry("booleanProperty", Boolean.FALSE)));
        assertTrue(set.contains(new TestEntry("booleanSecond", Boolean.TRUE)));
        assertTrue(set.contains(new TestEntry("byteProperty", Byte.valueOf((byte) 1))));
        assertTrue(set.contains(new TestEntry("doubleProperty", Double.valueOf(100.0))));
        assertTrue(set.contains(new TestEntry("floatProperty", Float.valueOf((float) 10.0))));
        assertTrue(set.contains(new TestEntry("intProperty", Integer.valueOf(1000))));
        assertTrue(set.contains(new TestEntry("longProperty", Long.valueOf(10000l))));
        assertTrue(set.contains(new TestEntry("stringProperty", "This is string 1")));

        // Test exact match postive results
        assertTrue(set.contains(new TestEntry("booleanPROPERTY", Boolean.FALSE)));
        assertTrue(set.contains(new TestEntry("booleanSECOND", Boolean.TRUE)));
        assertTrue(set.contains(new TestEntry("bytePROPERTY", Byte.valueOf((byte) 1))));
        assertTrue(set.contains(new TestEntry("doublePROPERTY", Double.valueOf(100.0))));
        assertTrue(set.contains(new TestEntry("floatPROPERTY", Float.valueOf((float) 10.0))));
        assertTrue(set.contains(new TestEntry("intPROPERTY", Integer.valueOf(1000))));
        assertTrue(set.contains(new TestEntry("longPROPERTY", Long.valueOf(10000l))));
        assertTrue(set.contains(new TestEntry("stringPROPERTY", "This is string 1")));

        // Test negative results
        assertTrue(!set.contains(new TestEntry("foo", "bar")));
        assertTrue(!set.contains(new TestEntry("FOO", "bar")));
        assertTrue(!set.contains(new TestEntry("baz", "bop")));
        assertTrue(!set.contains(new TestEntry("BAZ", "bop")));

        // Test other methods
        assertTrue(!set.isEmpty());

        // Test updating through the entry set
        Iterator entries = set.iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            if ("stringProperty".equalsIgnoreCase((String) entry.getKey())) {
                entry.setValue("This is string 1 modified");
            }
        }
        assertEquals("This is string 1 modified",
                beans[1].getStringProperty());
        assertEquals("This is string 1 modified",
                map.get("stringProperty"));
        assertEquals("This is string 1 modified",
                map.get("stringPROPERTY"));
        result.absolute(2); // ResultSet indexing is one-relative
        assertEquals("This is string 1 modified",
                result.getObject("stringProperty"));
    }

    // Test ((Map) getRowData()).get()
    @Test
    public void testRowDataGet() throws Exception {
        // Position to row 1 and retrieve the corresponding Map
        model.setRowIndex(1);
        assertTrue(model.isRowAvailable());
        Object data = model.getRowData();
        assertNotNull(data);
        assertTrue(data instanceof Map);
        Map map = (Map) data;

        // Test exact match on column names
        assertEquals(Boolean.FALSE, map.get("booleanProperty"));
        assertEquals(Boolean.TRUE, map.get("booleanSecond"));
        assertEquals(Byte.valueOf((byte) 1), map.get("byteProperty"));
        assertEquals(Double.valueOf(100.0), map.get("doubleProperty"));
        assertEquals(Float.valueOf((float) 10.0), map.get("floatProperty"));
        assertEquals(Integer.valueOf(1000), map.get("intProperty"));
        assertEquals(Long.valueOf(10000l), map.get("longProperty"));
        assertEquals("This is string 1", map.get("stringProperty"));

        // Test inexact match on column names
        assertEquals(Boolean.FALSE, map.get("booleanPROPERTY"));
        assertEquals(Boolean.TRUE, map.get("booleanSECOND"));
        assertEquals(Byte.valueOf((byte) 1), map.get("bytePROPERTY"));
        assertEquals(Double.valueOf(100.0), map.get("doublePROPERTY"));
        assertEquals(Float.valueOf((float) 10.0), map.get("floatPROPERTY"));
        assertEquals(Integer.valueOf(1000), map.get("intPROPERTY"));
        assertEquals(Long.valueOf(10000l), map.get("longPROPERTY"));
        assertEquals("This is string 1", map.get("stringPROPERTY"));

        // Test null return on non-existent column names
        assertNull(map.get("foo"));
        assertNull(map.get("FOO"));
        assertNull(map.get("bar"));
        assertNull(map.get("bar"));
    }

    // Test ((Map) getRowData()).keySet()
    @Test
    public void testRowDataKeySet() throws Exception {
        // Position to row 1 and retrieve the corresponding Map
        model.setRowIndex(1);
        assertTrue(model.isRowAvailable());
        Object data = model.getRowData();
        assertNotNull(data);
        assertTrue(data instanceof Map);
        Map map = (Map) data;
        Set set = map.keySet();

        // Test exact match postive results
        assertTrue(set.contains("booleanProperty"));
        assertTrue(set.contains("booleanSecond"));
        assertTrue(set.contains("byteProperty"));
        assertTrue(set.contains("doubleProperty"));
        assertTrue(set.contains("floatProperty"));
        assertTrue(set.contains("intProperty"));
        assertTrue(set.contains("longProperty"));
        assertTrue(set.contains("stringProperty"));

        // Test inexact match positive results
        assertTrue(set.contains("booleanPROPERTY"));
        assertTrue(set.contains("booleanSECOND"));
        assertTrue(set.contains("bytePROPERTY"));
        assertTrue(set.contains("doublePROPERTY"));
        assertTrue(set.contains("floatPROPERTY"));
        assertTrue(set.contains("intPROPERTY"));
        assertTrue(set.contains("longPROPERTY"));
        assertTrue(set.contains("stringPROPERTY"));

        // Test negative results
        assertTrue(!set.contains("foo"));
        assertTrue(!set.contains("FOO"));
        assertTrue(!set.contains("bar"));
        assertTrue(!set.contains("BAR"));

        // Test other methods
        assertTrue(!set.isEmpty());
    }

    // Test ((Map) getRowData()).put()
    @Test
    public void testRowDataPut() throws Exception {
        // Position to row 1 and retrieve the corresponding Map
        model.setRowIndex(1);
        assertTrue(model.isRowAvailable());
        Object data = model.getRowData();
        assertNotNull(data);
        assertTrue(data instanceof Map);
        Map map = (Map) data;
    }

    // Test unsupported operations on ((Map) getRowData())
    @Test
    public void testRowDataUnsupported() throws Exception {
        // Position to row 1 and retrieve the corresponding Map
        model.setRowIndex(1);
        assertTrue(model.isRowAvailable());
        Object data = model.getRowData();
        assertNotNull(data);
        assertTrue(data instanceof Map);
        Map map = (Map) data;

        // clear()
        try {
            map.clear();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }

        // entrySet()
        Set entrySet = map.entrySet();
        try {
            entrySet.add(new TestEntry("foo", "bar"));
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        List mapEntries = new ArrayList();
        mapEntries.add(new TestEntry("foo", "bar"));
        mapEntries.add(new TestEntry("baz", "bop"));
        try {
            entrySet.addAll(mapEntries);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            entrySet.clear();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            Iterator iterator = entrySet.iterator();
            iterator.next();
            iterator.remove();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            entrySet.remove(new TestEntry("foo", "bar"));
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            entrySet.removeAll(mapEntries);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            entrySet.retainAll(mapEntries);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }

        // keySet()
        Set keySet = map.keySet();
        try {
            keySet.add("foo");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        List mapKeys = new ArrayList();
        mapKeys.add("foo");
        mapKeys.add("bar");
        try {
            keySet.addAll(mapKeys);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            keySet.clear();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            Iterator iterator = keySet.iterator();
            iterator.next();
            iterator.remove();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            keySet.remove(new TestEntry("foo", "bar"));
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            keySet.removeAll(mapKeys);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            keySet.retainAll(mapKeys);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }

        // remove()
        try {
            map.remove("foo");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }

        // values()
        Collection values = map.values();
        try {
            values.add("foo");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        List list = new ArrayList();
        list.add("foo");
        list.add("bar");
        try {
            values.addAll(list);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            values.clear();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            Iterator iterator = values.iterator();
            iterator.next();
            iterator.remove();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            values.remove("foo");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            values.removeAll(list);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }
        try {
            values.retainAll(list);
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected result
        }

    }

    // Test ((Map) getRowData()).values()
    @Test
    public void testRowDataValues() throws Exception {
        // Position to row 1 and retrieve the corresponding Map
        model.setRowIndex(1);
        assertTrue(model.isRowAvailable());
        Object data = model.getRowData();
        assertNotNull(data);
        assertTrue(data instanceof Map);
        Map map = (Map) data;
        Collection values = map.values();

        // Test positive results
        assertTrue(values.contains(Boolean.TRUE));
        assertTrue(values.contains(Boolean.FALSE));
        assertTrue(values.contains(Byte.valueOf((byte) 1)));
        assertTrue(values.contains(Double.valueOf(100.0)));
        assertTrue(values.contains(Float.valueOf((float) 10.0)));
        assertTrue(values.contains(Integer.valueOf(1000)));
        assertTrue(values.contains(Long.valueOf(10000l)));
        assertTrue(values.contains("This is string 1"));

        // Test negative results
        assertTrue(!values.contains("foo"));
        assertTrue(!values.contains(Integer.valueOf(654321)));

        // Test other methods
        assertTrue(!values.isEmpty());
    }

    // ------------------------------------------------------- Protected Methods
    @Override
    protected BeanTestImpl data() throws Exception {
        Object data = model.getRowData();
        assertTrue(data instanceof Map);
        BeanTestImpl bean = new BeanTestImpl();
        Map map = (Map) data;

        bean.setBooleanProperty(((Boolean) map.get("booleanProperty")).booleanValue());
        bean.setBooleanSecond(((Boolean) map.get("booleanSecond")).booleanValue());
        bean.setByteProperty(((Byte) map.get("byteProperty")).byteValue());
        bean.setDoubleProperty(((Double) map.get("doubleProperty")).doubleValue());
        bean.setFloatProperty(((Float) map.get("floatProperty")).floatValue());
        bean.setIntProperty(((Integer) map.get("intProperty")).intValue());
        bean.setLongProperty(((Long) map.get("longProperty")).longValue());
        bean.setNullProperty((String) map.get("nullProperty"));
        bean.setShortProperty(((Short) map.get("shortProperty")).shortValue());
        bean.setStringProperty((String) map.get("stringProperty"));
        bean.setWriteOnlyProperty((String) map.get("writeOnlyPropertyValue"));

        return (bean);
    }

    class TestEntry implements Map.Entry {

        public TestEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        private Object key;
        private Object value;

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            Object previous = this.value;
            this.value = value;
            return previous;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return (false);
            }
            Map.Entry e = (Map.Entry) o;
            return (key == null
                    ? e.getKey() == null : key.equals(e.getKey()))
                    && (value == null
                    ? e.getValue() == null : value.equals(e.getValue()));
        }
    }
}
