/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: MockResultSet.java,v 1.2 2006/03/16 19:41:24 edburns Exp $
 */



package com.sun.faces.mock;


import java.io.InputStream;
import java.io.Reader;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLXML;
import java.util.Calendar;
import java.util.Map;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import com.sun.faces.mock.model.TestBean;
import org.apache.commons.beanutils.PropertyUtils;


/**
 * <p>Mock object that implements enough of <code>java.sql.ResultSet</code>
 * to exercise the <code>ResultSetDataModel</code> functionality.  It wraps
 * an array of JavaBeans objects that are passed to the constructor.</p>
 */

public class MockResultSet implements ResultSet {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new <code>MockResultSet</code> instance wrapping the
     * specified array of beans.</p>
     *
     * @param beans Array of beans representing the content of the result set
     */
    public MockResultSet(Object beans[]) {

        if (beans == null) {
            throw new NullPointerException();
        }
        this.beans = beans;
        this.clazz = beans.getClass().getComponentType();

    }


    // ------------------------------------------------------ Instance Variables


    // Array of beans representing our underlying data
    private Object beans[] = null;


    // Class representing the underlying data type we are wrapping
    private Class clazz = null;


    // ResultSetMetaData for this ResultSet
    private MockResultSetMetaData metadata = null;


    // Current row number (0 means "before the first row"
    private int row = 0;


    // ----------------------------------------------------- Implemented Methods


    public boolean absolute(int row) throws SQLException {

        if (row == 0) {
            this.row = 0;
            return (false);
        } else if (row > 0) {
            if (row > beans.length) {
                this.row = beans.length + 1;
                return (false);
            } else {
                this.row = row;
                return (true);
            }
        } else {
            this.row = (beans.length + 1) - row;
            if (row < 1) {
                row = 0;
                return (false);
            } else {
                return (true);
            }
        }

    }


    public void beforeFirst() throws SQLException {

        absolute(0);

    }


    public void close() throws SQLException {

        ; // No action required

    }


    public int getConcurrency() throws SQLException {

        return (ResultSet.CONCUR_UPDATABLE);

    }


    public ResultSetMetaData getMetaData() throws SQLException {

        if (metadata == null) {
            metadata = new MockResultSetMetaData(clazz);
        }
        return (metadata);

    }


    public Object getObject(int columnIndex) throws SQLException {

        return (getObject(getMetaData().getColumnName(columnIndex)));

    }


    public Object getObject(String columnName) throws SQLException {

        if ((row <= 0) || (row > beans.length)) {
            throw new SQLException("Invalid row number " + row);
        }
        try {
            if (columnName.equals("writeOnlyProperty") &&
                (beans[row - 1] instanceof TestBean)) {
                return (((TestBean) beans[row - 1]).getWriteOnlyPropertyValue());
            } else {
                return (PropertyUtils.getSimpleProperty(beans[row - 1],
                                                        columnName));
            }
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }

    }


    public int getRow() throws SQLException {

        return (this.row);

    }


    public int getType() throws SQLException {

        return (ResultSet.TYPE_SCROLL_INSENSITIVE);

    }


    public boolean last() throws SQLException {

        return (absolute(beans.length));

    }


    public void updateObject(int columnIndex, Object value)
        throws SQLException {

        updateObject(getMetaData().getColumnName(columnIndex), value);

    }


    public void updateObject(String columnName, Object value)
        throws SQLException {

        if ((row <= 0) || (row > beans.length)) {
            throw new SQLException("Invalid row number " + row);
        }
        try {
            PropertyUtils.setSimpleProperty(beans[row - 1], columnName, value);
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }

    }


    // --------------------------------------------------- Unimplemented Methods


    public void afterLast() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void cancelRowUpdates() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void deleteRow() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public int findColumn(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean first() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Array getArray(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Array getArray(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public InputStream getAsciiStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    public BigDecimal getBigDecimal(int columnIndex, int scale)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    /** @deprecated */
    public BigDecimal getBigDecimal(String columnName, int scale)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public InputStream getBinaryStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Blob getBlob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Blob getBlob(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean getBoolean(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean getBoolean(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public byte getByte(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public byte getByte(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public byte[] getBytes(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public byte[] getBytes(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Reader getCharacterStream(int columnIndex)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Reader getCharacterStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Clob getClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Clob getClob(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public String getCursorName() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Date getDate(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Date getDate(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Date getDate(String columnName, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public double getDouble(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public double getDouble(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public float getFloat(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public float getFloat(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public int getInt(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public int getInt(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public long getLong(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public long getLong(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Object getObject(int columnIndex, Map map) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Object getObject(String columnName, Map map) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Ref getRef(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Ref getRef(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public short getShort(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public short getShort(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Statement getStatement() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public String getString(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public String getString(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Time getTime(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Time getTime(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Time getTime(String columnName, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Timestamp getTimestamp(int columnIndex, Calendar cal)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Timestamp getTimestamp(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public Timestamp getTimestamp(String columnName, Calendar cal)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    /** @deprecated */
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    /** @deprecated */
    public InputStream getUnicodeStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public URL getURL(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public URL getURL(String columnName) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void insertRow() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean isAfterLast() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean isBeforeFirst() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean isFirst() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean isLast() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void moveToCurrentRow() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void moveToInsertRow() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean next() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean previous() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void refreshRow() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean relative(int rows) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean rowDeleted() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean rowInserted() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean rowUpdated() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void setFetchDirection(int direction) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void setFetchSize(int size) throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateArray(int columnPosition, Array x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateArray(String columnName, Array x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateAsciiStream(int columnPosition, InputStream x, int len)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateAsciiStream(String columnName, InputStream x, int len)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBigDecimal(int columnPosition, BigDecimal x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBigDecimal(String columnName, BigDecimal x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBinaryStream(int columnPosition, InputStream x, int len)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBinaryStream(String columnName, InputStream x, int len)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBlob(int columnPosition, Blob x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBlob(String columnName, Blob x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBoolean(int columnPosition, boolean x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBoolean(String columnName, boolean x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateByte(int columnPosition, byte x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateByte(String columnName, byte x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBytes(int columnPosition, byte x[])
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateBytes(String columnName, byte x[])
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateCharacterStream(int columnPosition, Reader x, int len)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateCharacterStream(String columnName, Reader x, int len)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateClob(int columnPosition, Clob x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateClob(String columnName, Clob x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateDate(int columnPosition, Date x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateDate(String columnName, Date x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateDouble(int columnPosition, double x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateDouble(String columnName, double x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateFloat(int columnPosition, float x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateFloat(String columnName, float x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateInt(int columnPosition, int x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateInt(String columnName, int x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateLong(int columnPosition, long x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateLong(String columnName, long x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateNull(int columnPosition)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateNull(String columnName)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateObject(int columnPosition, Object x, int scale)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateObject(String columnName, Object x, int scale)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateRef(int columnPosition, Ref x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateRef(String columnName, Ref x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateRow() throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateShort(int columnPosition, short x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateShort(String columnName, short x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateString(int columnPosition, String x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateString(String columnName, String x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateTime(int columnPosition, Time x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateTime(String columnName, Time x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateTimestamp(int columnPosition, Timestamp x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public void updateTimestamp(String columnName, Timestamp x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    public boolean wasNull() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHoldability() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isClosed() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    



}
