/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.database;


/* ====================================================================
 * The OpenSymphony Software License, Version 1.1
 *
 * (this license is derived and fully compatible with the Apache Software
 * License - see http://www.apache.org/LICENSE.txt)
 *
 * Copyright (c) 2001 The OpenSymphony Group. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        OpenSymphony Group (http://www.opensymphony.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "OpenSymphony" and "The OpenSymphony Group"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact license@opensymphony.com .
 *
 * 5. Products derived from this software may not be called "OpenSymphony"
 *    or "OSCore", nor may "OpenSymphony" or "OSCore" appear in their
 *    name, without prior written permission of the OpenSymphony Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
import com.opensymphony.module.propertyset.*;

import com.opensymphony.util.Data;
import com.opensymphony.util.EJBUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

import java.util.*;
import java.util.Date;

import javax.sql.DataSource;


/**
 * This is an implementation of a property set manager for JDBC. It relies on
 * one table, called "os_propertyset" that has four columns: "type" (integer),
 * "keyValue" (string), "globalKey" (string), and "value" (string). This is not
 * likely to be enough for people who store BLOBS as properties. Of course,
 * those people need to get a life.
 * <p>
 *
 * For Postgres(?):<br>
 * CREATE TABLE OS_PROPERTYENTRY (GLOBAL_KEY varchar(255), ITEM_KEY varchar(255), ITEM_TYPE smallint, STRING_VALUE varchar(255), DATE_VALUE timestamp, DATA_VALUE oid, FLOAT_VALUE float8, NUMBER_VALUE numeric, primary key (GLOBAL_KEY, ITEM_KEY));
 * <p>
 *
 * For Oracle (Thanks to Michael G. Slack!):<br>
 * CREATE TABLE OS_PROPERTYENTRY (GLOBAL_KEY varchar(255), ITEM_KEY varchar(255), ITEM_TYPE smallint, STRING_VALUE varchar(255), DATE_VALUE date, DATA_VALUE long raw, FLOAT_VALUE float, NUMBER_VALUE numeric, primary key (GLOBAL_KEY, ITEM_KEY));
 * <p>
 *
 * Other databases may require small tweaks to the table creation scripts!
 *
 * <p>
 *
 * <b>Required Args</b>
 * <ul>
 *  <li><b>globalKey</b> - the globalKey to use with this PropertySet</li>
 * </ul>
 * <p>
 *
 * <b>Required Configuration</b>
 * <ul>
 *  <li><b>datasource</b> - JNDI path for the DataSource</li>
 *  <li><b>table.name</b> - the table name</li>
 *  <li><b>col.globalKey</b> - column name for the globalKey</li>
 *  <li><b>col.itemKey</b> - column name for the itemKey</li>
 *  <li><b>col.itemType</b> - column name for the itemType</li>
 *  <li><b>col.string</b> - column name for the string value</li>
 *  <li><b>col.date</b> - column name for the date value</li>
 *  <li><b>col.data</b> - column name for the data value</li>
 *  <li><b>col.float</b> - column name for the float value</li>
 *  <li><b>col.number</b> - column name for the number value</li>
 * </ul>
 *
 * @version $Revision$
 * @author <a href="mailto:epesh@hotmail.com">Joseph B. Ottinger</a>
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class JDBCPropertySet extends AbstractPropertySet {
    //~ Static fields/initializers /////////////////////////////////////////////

    private static final Log log = LogFactory.getLog(JDBCPropertySet.class);

    //~ Instance fields ////////////////////////////////////////////////////////

    // config
    DataSource ds;
    String colData;
    String colDate;
    String colFloat;
    String colGlobalKey;
    String colItemKey;
    String colItemType;
    String colNumber;
    String colString;

    // args
    String globalKey;
    String tableName;

    //~ Methods ////////////////////////////////////////////////////////////////

    public Collection getKeys(String prefix, int type) throws PropertyException {
        if (prefix == null) {
            prefix = "";
        }

        Connection conn = null;

        try {
            conn = ds.getConnection();

            PreparedStatement ps = null;
            String sql = "SELECT " + colItemKey + " FROM " + tableName + " WHERE " + colItemKey + " LIKE ? AND " + colGlobalKey + " = ?";

            if (type == 0) {
                ps = conn.prepareStatement(sql);
                ps.setString(1, prefix + "%");
                ps.setString(2, globalKey);
            } else {
                sql = sql + " AND " + colItemType + " = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, prefix + "%");
                ps.setString(2, globalKey);
                ps.setInt(3, type);
            }

            ArrayList list = new ArrayList();
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getString(colItemKey));
            }

            rs.close();
            ps.close();

            return list;
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    public int getType(String key) throws PropertyException {
        Connection conn = null;

        try {
            conn = ds.getConnection();

            String sql = "SELECT " + colItemType + " FROM " + tableName + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, globalKey);
            ps.setString(2, key);

            ResultSet rs = ps.executeQuery();
            int type = 0;

            if (rs.next()) {
                type = rs.getInt(colItemType);
            }

            rs.close();
            ps.close();

            return type;
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    public boolean exists(String key) throws PropertyException {
        return getType(key) != 0;
    }

    public void init(Map config, Map args) {
        // args
        globalKey = (String) args.get("globalKey");

        // config
        try {
            ds = (DataSource) EJBUtils.lookup((String) config.get("datasource"));
        } catch (Exception e) {
            log.fatal("Could not get DataSource", e);
        }

        tableName = (String) config.get("table.name");
        colGlobalKey = (String) config.get("col.globalKey");
        colItemKey = (String) config.get("col.itemKey");
        colItemType = (String) config.get("col.itemType");
        colString = (String) config.get("col.string");
        colDate = (String) config.get("col.date");
        colData = (String) config.get("col.data");
        colFloat = (String) config.get("col.float");
        colNumber = (String) config.get("col.number");
    }

    public void remove() throws PropertyException {
        Connection conn = null;

        try {
            conn = ds.getConnection();

            String sql = "DELETE FROM " + tableName + " WHERE " + colGlobalKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, globalKey);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    public void remove(String key) throws PropertyException {
        Connection conn = null;

        try {
            conn = ds.getConnection();

            String sql = "DELETE FROM " + tableName + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, globalKey);
            ps.setString(2, key);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    protected void setImpl(int type, String key, Object value) throws PropertyException {
        if (value == null) {
            throw new PropertyException("JDBCPropertySet does not allow for null values to be stored");
        }

        Connection conn = null;

        try {
            conn = ds.getConnection();

            String sql = "UPDATE " + tableName + " SET " + colString + " = ?, " + colDate + " = ?, " + colData + " = ?, " + colFloat + " = ?, " + colNumber + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            setValues(ps, type, key, value);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows != 1) {
                // ok, this is a new value, insert it
                sql = "INSERT INTO " + tableName + " (" + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);
                setValues(ps, type, key, value);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    protected Object get(int type, String key) throws PropertyException {
        String sql = "SELECT " + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName + " WHERE " + colItemKey + " = ? AND " + colGlobalKey + " = ?";

        Object o = null;
        Connection conn = null;

        try {
            conn = ds.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, key);
            ps.setString(2, globalKey);

            int propertyType;
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                propertyType = rs.getInt(colItemType);

                if (propertyType != type) {
                    throw new InvalidPropertyTypeException();
                }

                switch (type) {
                case PropertySet.BOOLEAN:

                    int boolVal = rs.getInt(colNumber);
                    o = new Boolean(boolVal == 1);

                    break;

                case PropertySet.DATA:
                    o = rs.getBytes(colData);

                    break;

                case PropertySet.DATE:
                    o = rs.getTimestamp(colDate);

                    break;

                case PropertySet.DOUBLE:
                    o = new Double(rs.getDouble(colFloat));

                    break;

                case PropertySet.INT:
                    o = new Integer(rs.getInt(colNumber));

                    break;

                case PropertySet.LONG:
                    o = new Long(rs.getLong(colNumber));

                    break;

                case PropertySet.STRING:
                    o = rs.getString(colString);

                    break;

                default:
                    throw new InvalidPropertyTypeException("JDBCPropertySet doesn't support this type yet.");
                }
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }

        return o;
    }

    private void setValues(PreparedStatement ps, int type, String key, Object value) throws SQLException, PropertyException {
        // Patched by Edson Richter for MS SQL Server JDBC Support!
        String driverName;

        try {
            driverName = ps.getConnection().getMetaData().getDriverName().toUpperCase();
        } catch (Exception e) {
            driverName = "";
        }

        ps.setNull(1, Types.VARCHAR);
        ps.setNull(2, Types.TIMESTAMP);

        // Patched by Edson Richter for MS SQL Server JDBC Support!
        // Oracle support suggestion also Michael G. Slack
        if ((driverName.indexOf("SQLSERVER") >= 0) || (driverName.indexOf("ORACLE") >= 0)) {
            ps.setNull(3, Types.BINARY);
        } else if (driverName.indexOf("SYBASE") >= 0) {
            ps.setNull(3, Types.VARBINARY);
        } else {
            ps.setNull(3, Types.BLOB);
        }

        ps.setNull(4, Types.FLOAT);
        ps.setNull(5, Types.NUMERIC);
        ps.setInt(6, type);
        ps.setString(7, globalKey);
        ps.setString(8, key);

        switch (type) {
        case PropertySet.BOOLEAN:

            Boolean boolVal = (Boolean) value;
            ps.setInt(5, boolVal.booleanValue() ? 1 : 0);

            break;

        case PropertySet.DATA:

            Data data = (Data) value;
            ps.setBytes(3, data.getBytes());

            break;

        case PropertySet.DATE:

            Date date = (Date) value;
            ps.setTimestamp(2, new Timestamp(date.getTime()));

            break;

        case PropertySet.DOUBLE:

            Double d = (Double) value;
            ps.setDouble(4, d.doubleValue());

            break;

        case PropertySet.INT:

            Integer i = (Integer) value;
            ps.setInt(5, i.intValue());

            break;

        case PropertySet.LONG:

            Long l = (Long) value;
            ps.setLong(5, l.longValue());

            break;

        case PropertySet.STRING:
            ps.setString(1, (String) value);

            break;

        default:
            throw new PropertyException("This type isn't supported!");
        }
    }

    private void closeConnection(Connection conn) {
        try {
            if ((conn != null) && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            log.error("Could not close connection");
        }
    }
}
