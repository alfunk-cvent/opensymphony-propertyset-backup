/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.hibernate;

import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.PropertyException;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.type.Type;

import java.io.Serializable;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This is the property set implementation for storing properties using Hibernate.
 * <p>
 *
 * <b>Required Args</b>
 * <ul>
 *  <li><b>entityId</b> - Long that holds the ID of this entity</li>
 *  <li><b>entityName</b> - String that holds the name of this entity type</li>
 * </ul>
 *
 * if "<b>sessionFactory</b> - hibornate sessionFactory" is not passed as an arg then init will use: <br />
 *  <b>hibernate.*</b> - config params needed to create a hibernate sessionFactory in the propertyset config xml.
 * <br />
 * This can be any of the configs avail from hibernate.
 * <p>
 *
 * @author $Author$
 * @version $Revision$
 */
public class HibernatePropertySet extends AbstractPropertySet {
    //~ Instance fields ////////////////////////////////////////////////////////

    private Long entityId;
    private SessionFactory sessionFactory;
    private String entityName;

    //~ Methods ////////////////////////////////////////////////////////////////

    public Collection getKeys(String prefix, int type) throws PropertyException {
        Session session = null;
        Query query = null;
        List list = null;

        try {
            session = this.sessionFactory.openSession();

            if ((prefix != null) && (type > 0)) {
                query = session.getNamedQuery("all_keys_with_type_like");
                query.setString("like", prefix + '%');
                query.setInteger("type", type);
            } else if (prefix != null) {
                query = session.getNamedQuery("all_keys_like");
                query.setString("like", prefix + '%');
            } else if (type > 0) {
                query = session.getNamedQuery("all_keys_with_type");
                query.setInteger("type", type);
            } else {
                query = session.getNamedQuery("all_keys");
            }

            query.setString("entityName", entityName);
            query.setLong("entityId", entityId.longValue());

            list = query.list();
        } catch (HibernateException e) {
            list = Collections.EMPTY_LIST;
        } finally {
            try {
                if (session != null) {
                    session.flush();
                    session.close();
                }
            } catch (Exception e) {
            }
        }

        return list;
    }

    public int getType(String key) throws PropertyException {
        return findByKey(key).getType();
    }

    public boolean exists(String key) throws PropertyException {
        try {
            findByKey(key);

            return true;
        } catch (PropertyException e) {
            return false;
        }
    }

    public void init(Map config, Map args) {
        super.init(config, args);
        this.entityId = (Long) args.get("entityId");
        this.entityName = (String) args.get("entityName");
        this.sessionFactory = (SessionFactory) args.get("sessionFactory");

        if (this.sessionFactory == null) {
            // loaded hibernate config
            try {
                Configuration cfg = new Configuration().addClass(PropertySetItem.class);
                Iterator itr = config.keySet().iterator();

                while (itr.hasNext()) {
                    String key = (String) itr.next();

                    if (key.startsWith("hibernate")) {
                        cfg.setProperty(key, (String) args.get(key));
                    }
                }

                this.sessionFactory = cfg.buildSessionFactory();
            } catch (HibernateException e) {
            }
        }
    }

    public void remove(String key) throws PropertyException {
        Session session = null;

        try {
            session = this.sessionFactory.openSession();
            session.delete(findByKey(key));
            session.flush();
        } catch (HibernateException e) {
            throw new PropertyException("Could not remove key '" + key + "': " + e.getMessage());
        } finally {
            try {
                if (session != null) {
                    if (!session.connection().getAutoCommit()) {
                        session.connection().commit();
                    }

                    session.close();
                }
            } catch (Exception e) {
            }
        }
    }

    protected void setImpl(int type, String key, Object value) throws PropertyException {
        Session session = null;
        PropertySetItem item = new PropertySetItem(entityName, entityId.longValue(), key);

        switch (type) {
        case BOOLEAN:
            item.setBooleanVal(((Boolean) value).booleanValue());

            break;

        case DOUBLE:
            item.setDoubleVal(((Double) value).doubleValue());

            break;

        case STRING:
        case TEXT:
            item.setStringVal((String) value);

            break;

        case LONG:
            item.setLongVal(((Long) value).longValue());

            break;

        case INT:
            item.setIntVal(((Integer) value).intValue());

            break;

        case DATE:
            item.setDateVal((Date) value);

            break;

        default:
            throw new PropertyException("type " + type + " not supported");
        }

        try {
            item.setType(type);
            session = this.sessionFactory.openSession();

            Serializable foo = session.save(item);
            session.flush();
        } catch (HibernateException he) {
            throw new PropertyException("Could not save key '" + key + "':" + he.getMessage());
        } finally {
            try {
                if (session != null) {
                    if (!session.connection().getAutoCommit()) {
                        session.connection().commit();
                    }

                    session.close();
                }
            } catch (Exception e) {
            }
        }
    }

    protected Object get(int type, String key) throws PropertyException {
        PropertySetItem item = findByKey(key);

        if (item.getType() != type) {
            throw new PropertyException("key '" + key + "' does not have matching type of " + type);
        }

        switch (type) {
        case BOOLEAN:
            return new Boolean(item.getBooleanVal());

        case DOUBLE:
            return new Double(item.getDoubleVal());

        case STRING:
        case TEXT:
            return item.getStringVal();

        case LONG:
            return new Long(item.getLongVal());

        case INT:
            return new Integer(item.getIntVal());

        case DATE:
            return item.getDateVal();
        }

        throw new PropertyException("type " + type + " not supported");
    }

    private PropertySetItem findByKey(String key) throws PropertyException {
        Session session = null;
        PropertySetItem item = null;

        try {
            session = this.sessionFactory.openSession();
            item = (PropertySetItem) session.load(PropertySetItem.class, new PropertySetItem(entityName, entityId.longValue(), key));
            session.flush();
        } catch (HibernateException e) {
            throw new PropertyException("Could not find key '" + key + "': " + e.getMessage());
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
            }
        }

        if (item != null) {
            return item;
        } else {
            throw new PropertyException("Unknown key '" + key + "'");
        }
    }
}
