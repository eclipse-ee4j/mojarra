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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jakarta.faces.FacesException;

/**
 * <p>
 * <strong>ResultSetDataModel</strong> is a convenience implementation of {@link DataModel} that wraps a
 * <code>ResultSet</code> of Java objects. Note that the specified <code>ResultSet</code> <strong>MUST</strong> be
 * scrollable. In addition, if input components (that will be updating model values) reference this object in value
 * binding expressions, the specified <code>ResultSet</code> <strong>MUST</strong> be updatable.
 * </p>
 */

public class ResultSetDataModel extends DataModel<Map<String, Object>> {

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Construct a new {@link ResultSetDataModel} with no specified wrapped data.
     * </p>
     */
    public ResultSetDataModel() {

        this(null);

    }

    /**
     * <p>
     * Construct a new {@link ResultSetDataModel} wrapping the specified <code>ResultSet</code>.
     * </p>
     *
     * @param resultSet <code>ResultSet</code> to be wrapped (if any)
     */
    public ResultSetDataModel(ResultSet resultSet) {

        super();
        setWrappedData(resultSet);

    }

    // ------------------------------------------------------ Instance Variables

    // The current row index (zero relative)
    private int index = -1;

    // The metadata for the ResultSet we are wrapping (lazily instantiated)
    private ResultSetMetaData metadata = null;

    // The ResultSet we are wrapping
    private ResultSet resultSet = null;

    // Has the row at the current index been updated?
    private boolean updated = false;

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return <code>true</code> if there is <code>wrappedData</code> available, and the result of calling
     * <code>absolute()</code> on the underlying <code>ResultSet</code>, passing the current value of <code>rowIndex</code>
     * plus one (to account for the fact that <code>ResultSet</code> uses one-relative indexing), returns <code>true</code>.
     * Otherwise, return <code>false</code>.
     * </p>
     *
     * @throws FacesException if an error occurs getting the row availability
     */
    @Override
    public boolean isRowAvailable() {

        if (resultSet == null) {
            return false;
        } else if (index < 0) {
            return false;
        }
        try {
            if (resultSet.absolute(index + 1)) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new FacesException(e);
        }

    }

    /**
     * <p>
     * Return -1, since <code>ResultSet</code> does not provide a standard way to determine the number of available rows
     * without scrolling through the entire <code>ResultSet</code>, and this can be very expensive if the number of rows is
     * large.
     * </p>
     *
     * @throws FacesException if an error occurs getting the row count
     */
    @Override
    public int getRowCount() {

        return -1;

    }

    /**
     * <p>
     * If row data is available, return a <code>Map</code> representing the values of the columns for the row specified by
     * <code>rowIndex</code>, keyed by the corresponding column names. If no wrapped data is available, return
     * <code>null</code>.
     * </p>
     *
     * <p>
     * If a non-<code>null</code> <code>Map</code> is returned, its behavior must correspond to the contract for a mutable
     * <code>Map</code> as described in the JavaDocs for <code>AbstractMap</code>, with the following exceptions and
     * specialized behavior:
     * </p>
     * <ul>
     *
     * <li>The <code>Map</code>, and any supporting objects it returns, must perform all column name comparisons in a
     * case-insensitive manner. This case-insensitivity must be implemented using a case-insensitive
     * <code>Comparator</code>, such as <code>String.CASE_INSENSITIVE_ORDER</code>.</li>
     *
     * <li>The following methods must throw <code>UnsupportedOperationException</code>: <code>clear()</code>,
     * <code>remove()</code>.</li>
     * <li>The <code>entrySet()</code> method must return a <code>Set</code> that has the following behavior:
     * <ul>
     * <li>Throw <code>UnsupportedOperationException</code> for any attempt to add or remove entries from the
     * <code>Set</code>, either directly or indirectly through an <code>Iterator</code> returned by the
     * <code>Set</code>.</li>
     * <li>Updates to the <code>value</code> of an entry in this <code>set</code> must write through to the corresponding
     * column value in the underlying <code>ResultSet</code>.</li>
     * </ul>
     * </li>
     * <li>The <code>keySet()</code> method must return a <code>Set</code> that throws
     * <code>UnsupportedOperationException</code> on any attempt to add or remove keys, either directly or through an
     * <code>Iterator</code> returned by the <code>Set</code>.</li>
     * <li>The <code>put()</code> method must throw <code>IllegalArgumentException</code> if a key value for which
     * <code>containsKey()</code> returns <code>false</code> is specified. However, if a key already present in the
     * <code>Map</code> is specified, the specified value must write through to the corresponding column value in the
     * underlying <code>ResultSet</code>.</li>
     * <li>The <code>values()</code> method must return a <code>Collection</code> that throws
     * <code>UnsupportedOperationException</code> on any attempt to add or remove values, either directly or through an
     * <code>Iterator</code> returned by the <code>Collection</code>.</li>
     * </ul>
     *
     * @throws FacesException if an error occurs getting the row data
     * @throws IllegalArgumentException if now row data is available at the currently specified row index
     */
    @Override
    public Map<String, Object> getRowData() {

        if (resultSet == null) {
            return null;
        } else if (!isRowAvailable()) {
            throw new NoRowAvailableException();
        }
        try {
            getMetaData();
            return new ResultSetMap(this, String.CASE_INSENSITIVE_ORDER);
        } catch (SQLException e) {
            throw new FacesException(e);
        }

    }

    /**
     * @throws FacesException {@inheritDoc}
     */
    @Override
    public int getRowIndex() {

        return index;

    }

    /**
     * @throws FacesException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public void setRowIndex(int rowIndex) {

        if (rowIndex < -1) {
            throw new IllegalArgumentException();
        }

        // Tell the ResultSet that the previous row was updated if necessary
        if (updated && resultSet != null) {
            try {
                if (!resultSet.rowDeleted()) {
                    resultSet.updateRow();
                }
                updated = false;
            } catch (SQLException e) {
                throw new FacesException(e);
            }
        }

        int old = index;
        index = rowIndex;
        if (resultSet == null) {
            return;
        }
        DataModelListener[] listeners = getDataModelListeners();
        if (old != index && listeners != null) {
            Object rowData = null;
            if (isRowAvailable()) {
                rowData = getRowData();
            }
            DataModelEvent event = new DataModelEvent(this, index, rowData);
            int n = listeners.length;
            for (int i = 0; i < n; i++) {
                if (null != listeners[i]) {
                    listeners[i].rowSelected(event);
                }
            }
        }

    }

    @Override
    public Object getWrappedData() {

        return resultSet;

    }

    /**
     * @throws ClassCastException {@inheritDoc}
     */
    @Override
    public void setWrappedData(Object data) {

        if (data == null) {
            metadata = null;
            resultSet = null;
            setRowIndex(-1);
        } else {
            metadata = null;
            resultSet = (ResultSet) data;
            index = -1;
            setRowIndex(0);
        }
    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Return the <code>ResultSetMetaData</code> for the <code>ResultSet</code> we are wrapping, caching it the first time
     * it is returned.
     * </p>
     *
     * @throws FacesException if the <code>ResultSetMetaData</code> cannot be acquired
     */
    private ResultSetMetaData getMetaData() {

        if (metadata == null) {
            try {
                metadata = resultSet.getMetaData();
            } catch (SQLException e) {
                throw new FacesException(e);
            }
        }
        return metadata;

    }

    /**
     * <p>
     * Mark the current row as having been updated, so that we will call <code>updateRow()</code> before moving elsewhere.
     * </p>
     */
    private void updated() {

        updated = true;

    }

    // --------------------------------------------------------- Private Classes

    // Private implementation of Map that delegates column get and put
    // operations to the underlying ResultSet, after setting the required
    // row index
    // NOT SERIALIZABLE
    private static class ResultSetMap extends TreeMap<String, Object> {

        private ResultSetDataModel model;

        public ResultSetMap(ResultSetDataModel model, Comparator<String> comparator) throws SQLException {

            super(comparator);
            this.model = model;
            index = model.index;
            model.resultSet.absolute(index + 1);
            int n = model.metadata.getColumnCount();
            for (int i = 1; i <= n; i++) {
                super.put(model.metadata.getColumnName(i), model.metadata.getColumnName(i));
            }
        }

        // The zero-relative row index of our row
        private int index;

        // Removing entries is not allowed
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value) {
            for (Iterator i = entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                Object contained = entry.getValue();
                if (value == null) {
                    if (contained == null) {
                        return true;
                    }
                } else {
                    if (value.equals(contained)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Set<Map.Entry<String, Object>> entrySet() {
            return new ResultSetEntries(this);
        }

        @Override
        public Object get(Object key) {
            if (!containsKey(key)) {
                return null;
            }
            try {
                model.resultSet.absolute(index + 1);
                return model.resultSet.getObject((String) realKey(key));
            } catch (SQLException e) {
                throw new FacesException(e);
            }
        }

        @Override
        public Set<String> keySet() {
            return new ResultSetKeys(this);
        }

        @Override
        public Object put(String key, Object value) {
            if (!containsKey(key)) {
                throw new IllegalArgumentException();
            }

            try {
                model.resultSet.absolute(index + 1);
                Object previous = model.resultSet.getObject((String) realKey(key));
                if (previous == null && value == null) {
                    return previous;
                } else if (previous != null && value != null && previous.equals(value)) {
                    return previous;
                }
                model.resultSet.updateObject((String) realKey(key), value);
                model.updated();
                return previous;
            } catch (SQLException e) {
                throw new FacesException(e);
            }
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> map) {
            for (Map.Entry<? extends String, ? extends Object> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }

        // Removing entries is not allowed
        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Object> values() {
            return new ResultSetValues(this);
        }

        Object realKey(Object key) {
            return super.get(key);
        }

        Iterator<String> realKeys() {
            return super.keySet().iterator();
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            throw new NotSerializableException();
        }

        private void readObject(ObjectInputStream in) throws IOException {
            throw new NotSerializableException();
        }

    }

    // Private implementation of Set that implements the entrySet() behavior
    // for ResultSetMap
    private static class ResultSetEntries extends AbstractSet<Map.Entry<String, Object>> {

        public ResultSetEntries(ResultSetMap map) {
            this.map = map;
        }

        private ResultSetMap map;

        // Adding entries is not allowed
        @Override
        public boolean add(Map.Entry<String, Object> o) {
            throw new UnsupportedOperationException();
        }

        // Adding entries is not allowed
        @Override
        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        // Removing entries is not allowed
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                throw new NullPointerException();
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            Object k = e.getKey();
            Object v = e.getValue();
            if (!map.containsKey(k)) {
                return false;
            }
            if (v == null) {
                return map.get(k) == null;
            } else {
                return v.equals(map.get(k));
            }
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Iterator<Map.Entry<String, Object>> iterator() {
            return new ResultSetEntriesIterator(map);
        }

        // Removing entries is not allowed
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        // Removing entries is not allowed
        @Override
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        // Removing entries is not allowed
        @Override
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return map.size();
        }

    }

    // Private implementation of Iterator that implements the iterator()
    // behavior for the Set returned by entrySet() from ResultSetMap
    private static class ResultSetEntriesIterator implements Iterator<Map.Entry<String, Object>> {

        public ResultSetEntriesIterator(ResultSetMap map) {
            this.map = map;
            keys = map.keySet().iterator();
        }

        private ResultSetMap map;
        private Iterator<String> keys;

        @Override
        public boolean hasNext() {
            return keys.hasNext();
        }

        @Override
        public Map.Entry<String, Object> next() {
            String key = keys.next();
            return new ResultSetEntry(map, key);
        }

        // Removing entries is not allowed
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    // Private implementation of Map.Entry that implements the behavior for
    // a single entry from the Set returned by entrySet() from ResultSetMap
    private static class ResultSetEntry implements Map.Entry<String, Object> {

        public ResultSetEntry(ResultSetMap map, String key) {
            this.map = map;
            this.key = key;
        }

        private ResultSetMap map;
        private String key;

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            if (key == null) {
                if (e.getKey() != null) {
                    return false;
                }
            } else {
                if (!key.equals(e.getKey())) {
                    return false;
                }
            }
            Object v = map.get(key);
            if (v == null) {
                if (e.getValue() != null) {
                    return false;
                }
            } else {
                if (!v.equals(e.getValue())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return map.get(key);
        }

        @Override
        public int hashCode() {
            Object value = map.get(key);
            return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }

        @Override
        public Object setValue(Object value) {
            Object previous = map.get(key);
            map.put(key, value);
            return previous;
        }

    }

    // Private implementation of Set that implements the keySet() behavior
    // for ResultSetMap
    private static class ResultSetKeys extends AbstractSet<String> {

        public ResultSetKeys(ResultSetMap map) {
            this.map = map;
        }

        private ResultSetMap map;

        // Adding keys is not allowed
        @Override
        public boolean add(String o) {
            throw new UnsupportedOperationException();
        }

        // Adding keys is not allowed
        @Override
        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        // Removing keys is not allowed
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            return map.containsKey(o);
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Iterator<String> iterator() {
            return new ResultSetKeysIterator(map);
        }

        // Removing keys is not allowed
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        // Removing keys is not allowed
        @Override
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        // Removing keys is not allowed
        @Override
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return map.size();
        }

    }

    // Private implementation of Iterator that implements the iterator()
    // behavior for the Set returned by keySet() from ResultSetMap
    private static class ResultSetKeysIterator implements Iterator<String> {

        public ResultSetKeysIterator(ResultSetMap map) {
            keys = map.realKeys();
        }

        private Iterator<String> keys;

        @Override
        public boolean hasNext() {
            return keys.hasNext();
        }

        @Override
        public String next() {
            return keys.next();
        }

        // Removing keys is not allowed
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    // Private implementation of Collection that implements the behavior
    // for the Collection returned by values() from ResultSetMap
    private static class ResultSetValues extends AbstractCollection<Object> {

        public ResultSetValues(ResultSetMap map) {
            this.map = map;
        }

        private ResultSetMap map;

        @Override
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object value) {
            return map.containsValue(value);
        }

        @Override
        public Iterator<Object> iterator() {
            return new ResultSetValuesIterator(map);
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return map.size();
        }

    }

    // Private implementation of Iterator that implements the behavior
    // for the Iterator returned by values().iterator() from ResultSetMap
    private static class ResultSetValuesIterator implements Iterator<Object> {

        public ResultSetValuesIterator(ResultSetMap map) {
            this.map = map;
            keys = map.keySet().iterator();
        }

        private ResultSetMap map;
        private Iterator<String> keys;

        @Override
        public boolean hasNext() {
            return keys.hasNext();
        }

        @Override
        public Object next() {
            return map.get(keys.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
