/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.hibernate;

import com.opensymphony.module.propertyset.PropertyException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class HibernatePropertySetDAOImpl implements HibernatePropertySetDAO {
    //~ Instance fields ////////////////////////////////////////////////////////

    private SessionFactory sessionFactory;

    //~ Constructors ///////////////////////////////////////////////////////////

    public HibernatePropertySetDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setImpl(PropertySetItem item) {
        Session session = null;

        try {
            session = this.sessionFactory.openSession();
            session.save(item);
            session.flush();
        } catch (HibernateException he) {
            throw new PropertyException("Could not save key '" + item.getKey() + "':" + he.getMessage());
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

    public Collection getKeys(String entityName, Long entityId, String prefix, int type) {
        Session session = null;
        List list = null;

        try {
            session = this.sessionFactory.openSession();
            list = HibernatePropertySetDAOUtils.getKeysImpl(session, entityName, entityId, prefix, type);
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

    public PropertySetItem findByKey(String entityName, Long entityId, String key) {
        Session session = null;
        PropertySetItem item = null;

        try {
            session = this.sessionFactory.openSession();
            item = HibernatePropertySetDAOUtils.getItem(session, entityName, entityId, key);
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

        return item;
    }

    public void remove(String entityName, Long entityId, String key) {
        Session session = null;

        try {
            session = this.sessionFactory.openSession();
            session.delete(HibernatePropertySetDAOUtils.getItem(session, entityName, entityId, key));
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
}
