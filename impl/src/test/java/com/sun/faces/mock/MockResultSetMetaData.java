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

package com.sun.faces.mock;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * <p>
 * Mock object that implements enough of <code>java.sql.ResultSetMetaData</code>
 * to exercise the <code>ResultSetDataModel</code> functionality.</p>
 */
public class MockResultSetMetaData implements ResultSetMetaData {

    // ------------------------------------------------------------ Constructors
    /**
     * <p>
     * Construct a new <code>ResultSetMetaData</code> object wrapping the
     * properties of the specified Java class.</p>
     *
     * @param clazz Class whose properties we treat like columns
     */
    public MockResultSetMetaData(Class<?> clazz) throws SQLException {
        try {
            descriptors
                    = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    // ------------------------------------------------------ Instance Variables

    // The PropertyDescriptors for the Class we are wrapping
    private PropertyDescriptor descriptors[] = null;

    // ---------------------------------------------------------- Public Methods
    public PropertyDescriptor getDescriptor(int columnIndex)
            throws SQLException {
        try {
            return (descriptors[columnIndex - 1]);
        } catch (IndexOutOfBoundsException e) {
            throw new SQLException("Invalid columnIndex " + columnIndex);
        }
    }

    // ----------------------------------------------------- Implemented Methods
    @Override
    public String getColumnClassName(int columnIndex) throws SQLException {
        return (getDescriptor(columnIndex).getPropertyType().getName());
    }

    @Override
    public int getColumnCount() throws SQLException {
        return (descriptors.length);
    }

    @Override
    public String getColumnName(int columnIndex) throws SQLException {
        return (getDescriptor(columnIndex).getName());
    }

    @Override
    public boolean isReadOnly(int columnIndex) throws SQLException {
        return (getDescriptor(columnIndex).getWriteMethod() == null);
    }

    // --------------------------------------------------- Unimplemented Methods
    @Override
    public String getCatalogName(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnDisplaySize(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getColumnLabel(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnType(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getColumnTypeName(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPrecision(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getScale(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSchemaName(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTableName(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAutoIncrement(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCaseSensitive(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCurrency(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDefinitelyWritable(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int isNullable(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSearchable(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSigned(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWritable(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
