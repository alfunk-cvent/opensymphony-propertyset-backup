/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.database;

import com.opensymphony.module.propertyset.*;

import com.opensymphony.util.Data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

import java.sql.*;

import java.util.*;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;

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
    private String driverName;
    private String productName;

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
        String jndi = (String) config.get("datasource");

        if (jndi != null) {
            try {
                ds = (DataSource) lookup(jndi);

                if (ds == null) {
                    ds = (DataSource) new javax.naming.InitialContext().lookup(jndi);
                }
            } catch (Exception e) {
                log.fatal("Error looking up DataSource at " + jndi, e);

                return;
            }
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

    public boolean supportsType(int type) {
        switch (type) {
        case PropertySet.PROPERTIES:
        case PropertySet.TEXT:
        case PropertySet.XML:
            return false;
        }

        return true;
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

                case PropertySet.OBJECT:

                    ByteArrayInputStream bis = new ByteArrayInputStream(rs.getBytes(colData));

                    try {
                        ObjectInputStream is = new ObjectInputStream(bis);
                        o = is.readObject();
                    } catch (IOException e) {
                        throw new PropertyException("Error de-serializing object for key '" + key + "' from store:" + e);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

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
        if ((driverName == null) || (productName == null)) {
            try {
                this.driverName = ps.getConnection().getMetaData().getDriverName().toUpperCase();
                this.productName = ps.getConnection().getMetaData().getDatabaseProductName().toUpperCase();
            } catch (Exception e) {
                this.driverName = "";
            }
        }

        ps.setNull(1, Types.VARCHAR);
        ps.setNull(2, Types.TIMESTAMP);

        // Patched by Edson Richter for MS SQL Server JDBC Support!
        // Oracle support suggestion also Michael G. Slack
        if ((this.driverName.indexOf("SQLSERVER") > -1) || (this.driverName.indexOf("ORACLE") > -1)) {
            ps.setNull(3, Types.BINARY);
        } else if ((this.driverName.indexOf("SYBASE") > -1) || (this.productName.indexOf("SYBASE") > -1)) {
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

            if (value instanceof Data) {
                Data data = (Data) value;
                ps.setBytes(3, data.getBytes());
            }

            if (value instanceof byte[]) {
                ps.setBytes(3, (byte[]) value);
            }

            break;

        case PropertySet.OBJECT:

            if (!(value instanceof Serializable)) {
                throw new PropertyException(value.getClass() + " does not implement java.io.Serializable");
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            try {
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(value);
                ps.setBytes(3, bos.toByteArray());
            } catch (IOException e) {
                throw new PropertyException("I/O Error when serializing object:" + e);
            }

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

    private Object lookup(String location) throws NamingException {
        try {
            InitialContext context = new InitialContext();

            try {
                return context.lookup(location);
            } catch (NamingException e) {
                //ok, couldn't find it, look in env
                return context.lookup("java:comp/env/" + location);
            }
        } catch (NamingException e) {
            throw e;
        }
    }
}
