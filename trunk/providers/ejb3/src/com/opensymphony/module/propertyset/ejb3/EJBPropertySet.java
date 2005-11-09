/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.ejb3;

import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertyImplementationException;

import com.opensymphony.util.Data;
import com.opensymphony.util.XMLUtils;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.Serializable;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;


/**
 * EJB3 propertyset implementation.
 * The only additional init arg required over entityId and entityName is 'manager', which is the
 * entity manager to use.
 * @author Hani Suleiman
 *         Date: Nov 8, 2005
 *         Time: 4:51:53 PM
 */
public class EJBPropertySet extends AbstractPropertySet {
    //~ Instance fields ////////////////////////////////////////////////////////

    private EntityManager em;
    private Long entityId;
    private String entityName;

    //~ Methods ////////////////////////////////////////////////////////////////

    public Collection getKeys(String prefix, int type) throws PropertyException {
        Query q;

        if ((type == 0) && (prefix == null)) {
            q = em.createNamedQuery("keys");
        }
        //all types with the specified prefix
        else if ((type == 0) && (prefix != null)) {
            q = em.createNamedQuery("keys.prefix");
            q.setParameter("prefix", prefix + '%');
        }
        //type and prefix
        else if ((prefix == null) && (type != 0)) {
            q = em.createNamedQuery("keys.type");
            q.setParameter("type", type);
        } else {
            q = em.createNamedQuery("keys.prefixAndType");
            q.setParameter("prefix", prefix + '%');
            q.setParameter("type", type);
        }

        q.setParameter("entityId", entityId);
        q.setParameter("entityName", entityName);

        return q.getResultList();
    }

    public int getType(String key) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry entry = em.find(PropertyEntry.class, pk);

        if (entry == null) {
            return 0;
        }

        return entry.getType();
    }

    public boolean exists(String key) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry entry = em.find(PropertyEntry.class, pk);

        return entry != null;
    }

    public void init(Map config, Map args) {
        Object obj = args.get("manager");

        if (obj == null) {
            throw new IllegalArgumentException("no manager argument specified");
        }

        if (!(obj instanceof EntityManager)) {
            throw new IllegalArgumentException("factory specifies is of type '" + obj.getClass() + "' which does not implement " + EntityManager.class.getName());
        }

        this.em = (EntityManager) obj;
        this.entityId = (Long) args.get("entityId");
        this.entityName = (String) args.get("entityName");
    }

    public void remove(String key) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry entry = em.find(PropertyEntry.class, pk);

        if (entry != null) {
            em.remove(entry);
        }
    }

    public void remove() throws PropertyException {
        Query q = em.createQuery("delete PropertyEntry p where p.primaryKey.entityId=:entityId and p.primaryKey.entityName=:entityName");
        q.setParameter("entityId", entityId);
        q.setParameter("entityName", entityName);
        q.executeUpdate();
    }

    public boolean supportsType(int type) {
        if (type == PROPERTIES) {
            return false;
        }

        return true;
    }

    protected void setImpl(int type, String key, Object value) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry item;

        item = em.find(PropertyEntry.class, pk);

        boolean update = item != null;

        if (item == null) {
            item = new PropertyEntry();
            item.setPrimaryKey(pk);
            item.setType(type);
        } else if (item.getType() != type) {
            throw new PropertyException("Existing key '" + key + "' does not have matching type of " + type(type));
        }

        switch (type) {
        case BOOLEAN:
            item.setBoolValue(((Boolean) value).booleanValue());

            break;

        case INT:
            item.setIntValue(((Number) value).intValue());

            break;

        case LONG:
            item.setLongValue(((Number) value).longValue());

            break;

        case DOUBLE:
            item.setDoubleValue(((Number) value).doubleValue());

            break;

        case STRING:
            item.setStringValue((String) value);

            break;

        case TEXT:
            item.setTextValue((String) value);

            break;

        case DATE:
            item.setDateValue((Date) value);

        case OBJECT:
            item.setSerialized((Serializable) value);

            break;

        case DATA:

            if (value instanceof Data) {
                item.setData(((Data) value).getBytes());
            } else {
                item.setData((byte[]) value);
            }

            break;

        case XML:

            String text = writeXML((Document) value);
            item.setTextValue(text);

            break;

        default:
            throw new PropertyException("type " + type + " not supported");
        }

        if (update) {
            em.merge(item);
        } else {
            em.persist(item);
        }
    }

    protected Object get(int type, String key) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry entry = em.find(PropertyEntry.class, pk);

        if (entry == null) {
            return null;
        }

        if (entry.getType() != type) {
            throw new PropertyException("key '" + key + "' does not have matching type of " + type(type));
        }

        switch (type) {
        case BOOLEAN:
            return Boolean.valueOf(entry.getBoolValue());

        case DOUBLE:
            return entry.getDoubleValue();

        case STRING:
            return entry.getStringValue();

        case TEXT:
            return entry.getTextValue();

        case LONG:
            return entry.getLongValue();

        case INT:
            return entry.getIntValue();

        case DATE:
            return entry.getDateValue();

        case OBJECT:
            return entry.getSerialized();

        case DATA:
            return entry.getData();

        case XML:
            return readXML(entry.getTextValue());
        }

        throw new PropertyException("type " + type(type) + " not supported");
    }

    /**
     * Parse XML document from String in byte array.
     */
    private Document readXML(String data) {
        try {
            return XMLUtils.parse(data);
        } catch (Exception e) {
            throw new PropertyImplementationException("Cannot parse XML data", e);
        }
    }

    /**
     * Serialize (print) XML document to byte array (as String).
     */
    private String writeXML(Document doc) {
        try {
            return XMLUtils.print(doc);
        } catch (IOException e) {
            throw new PropertyImplementationException("Cannot serialize XML", e);
        }
    }
}
