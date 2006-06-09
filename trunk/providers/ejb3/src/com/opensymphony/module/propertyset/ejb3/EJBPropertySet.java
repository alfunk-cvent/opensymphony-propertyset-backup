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

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.spi.PersistenceUnitTransactionType;


/**
 * EJB3 propertyset implementation.
 * This implementation requires a couple of extra init args:
 * <li><code>manager</code>: Entity manager to use.
 * <li><code>transaction</code>: Can be either JTA or RESOURCE_LOCAL.
 * @author Hani Suleiman
 *         Date: Nov 8, 2005
 *         Time: 4:51:53 PM
 */
public class EJBPropertySet extends AbstractPropertySet {
    //~ Instance fields ////////////////////////////////////////////////////////

    private EntityManager entityManager;
    private Long entityId;
    private PersistenceUnitTransactionType transactionType;
    private String entityName;

    //~ Constructors ///////////////////////////////////////////////////////////

    public EJBPropertySet() {
    }

    public EJBPropertySet(EntityManager entityManager, PersistenceUnitTransactionType transactionType) {
        this.entityManager = entityManager;
        this.transactionType = transactionType;
    }

    public EJBPropertySet(EntityManager entityManager, PersistenceUnitTransactionType transactionType, Long entityId, String entityName) {
        this.entityManager = entityManager;
        this.entityId = entityId;
        this.entityName = entityName;
        this.transactionType = transactionType;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    public Collection getKeys(String prefix, int type) throws PropertyException {
        return getKeys(entityName, entityId, prefix, type);
    }

    public Collection getKeys(String entityName, long entityId, String prefix, int type) throws PropertyException {
        Query q;

        if ((type == 0) && (prefix == null)) {
            q = entityManager.createNamedQuery("keys");
        }
        //all types with the specified prefix
        else if ((type == 0) && (prefix != null)) {
            q = entityManager.createNamedQuery("keys.prefix");
            q.setParameter("prefix", prefix + '%');
        }
        //type and prefix
        else if ((prefix == null) && (type != 0)) {
            q = entityManager.createNamedQuery("keys.type");
            q.setParameter("type", type);
        } else {
            q = entityManager.createNamedQuery("keys.prefixAndType");
            q.setParameter("prefix", prefix + '%');
            q.setParameter("type", type);
        }

        q.setParameter("entityId", entityId);
        q.setParameter("entityName", entityName);

        return q.getResultList();
    }

    public void setTransactionType(PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    public int getType(String key) throws PropertyException {
        return getType(entityName, entityId, key);
    }

    public int getType(String entityName, long entityId, String key) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry entry = entityManager.find(PropertyEntry.class, pk);

        if (entry == null) {
            return 0;
        }

        return entry.getType();
    }

    public boolean exists(String key) throws PropertyException {
        return exists(entityName, entityId, key);
    }

    public boolean exists(String entityName, long entityId, String key) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry entry = entityManager.find(PropertyEntry.class, pk);

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

        this.entityManager = (EntityManager) obj;
        this.entityId = ((Number) args.get("entityId")).longValue();
        this.entityName = (String) args.get("entityName");

        Object tx = args.get("transaction");
        this.transactionType = (tx == null) ? PersistenceUnitTransactionType.RESOURCE_LOCAL : PersistenceUnitTransactionType.valueOf(tx.toString());
    }

    public void remove(String key) throws PropertyException {
        remove(entityName, entityId, key);
    }

    public void remove() throws PropertyException {
        remove(entityName, entityId);
    }

    public void remove(String entityName, long entityId, String key) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry entry = entityManager.find(PropertyEntry.class, pk);

        if (entry != null) {
            entityManager.remove(entry);
        }
    }

    public void remove(String entityName, long entityId) throws PropertyException {
        boolean mustCommit = joinTransaction();
        Query q = entityManager.createNamedQuery("values");
        q.setParameter("entityId", entityId);
        q.setParameter("entityName", entityName);

        //idiot jalopy blows up on a real man's for loop, so we have to use jdk14 wanky version
        List l = q.getResultList();

        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            entityManager.remove(o);
        }

        if (mustCommit) {
            entityManager.getTransaction().commit();
        }
    }

    public boolean supportsType(int type) {
        if (type == PROPERTIES) {
            return false;
        }

        return true;
    }

    protected void setImpl(int type, String key, Object value) throws PropertyException {
        setImpl(entityName, entityId, type, key, value);
    }

    protected void setImpl(String entityName, long entityId, int type, String key, Object value) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry item;

        boolean mustCommit = joinTransaction();

        item = entityManager.find(PropertyEntry.class, pk);

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
            entityManager.merge(item);
        } else {
            entityManager.persist(item);
        }

        if (mustCommit) {
            entityManager.getTransaction().commit();
        }
    }

    protected Object get(int type, String key) throws PropertyException {
        return get(entityName, entityId, type, key);
    }

    protected Object get(String entityName, long entityId, int type, String key) throws PropertyException {
        EntryPK pk = new EntryPK(entityName, entityId, key);
        PropertyEntry entry = entityManager.find(PropertyEntry.class, pk);

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

    private boolean joinTransaction() {
        boolean mustCommit = false;

        switch (transactionType) {
        case JTA:
            entityManager.joinTransaction();

            break;

        case RESOURCE_LOCAL:

            EntityTransaction tx = entityManager.getTransaction();

            if (!tx.isActive()) {
                tx.begin();
                mustCommit = true;
            }

            break;
        }

        return mustCommit;
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
