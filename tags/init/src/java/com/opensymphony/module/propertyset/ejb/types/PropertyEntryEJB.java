/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.ejb.types;


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
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.sequence.SequenceGenerator;
import com.opensymphony.module.sequence.SequenceGeneratorHome;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

import java.sql.Timestamp;

import javax.ejb.*;

import javax.naming.InitialContext;

import javax.rmi.PortableRemoteObject;


/**
 * @ejb.bean
 *  type="CMP"
 *  view-type="local"
 *  name="PropertyEntry"
 *  reentrant="False"
 *  schema="PropertyEntry"
 *  primkey-field="id"
 *
 * @ejb.pk class="java.lang.Long" extends="java.lang.Object"
 *
 * @ejb.persistence table-name="OS_PROPERTYENTRY"
 *
 * @ejb.ejb-ref
 *  ejb-name="PropertyNumber"
 *  view-type="local"
 * @ejb.ejb-ref
 *  ejb-name="PropertyDate"
 *  view-type="local"
 * @ejb.ejb-ref
 *  ejb-name="PropertyData"
 *  view-type="local"
 * @ejb.ejb-ref
 *  ejb-name="PropertyString"
 *  view-type="local"
 * @ejb.ejb-ref
 *  ejb-name="PropertyDecimal"
 *  view-type="local"
 *
 * @ejb.ejb-external-ref
 *  ref-name="ejb/SequenceGenerator"
 *  type="Session"
 *  view-type="remote"
 *  link="SequenceGenerator"
 *  home="com.opensymphony.module.sequence.SequenceGeneratorHome"
 *  business="com.opensymphony.module.sequence.SequenceGenerator"
 *
 * @ejb.finder
 *  signature="java.util.Collection findByNameAndId(java.lang.String entityName, long entityId)"
 *  query="SELECT DISTINCT OBJECT(o) FROM PropertyEntry o WHERE o.entityName = ?1 AND o.entityId = ?2"
 *
 * @ejb.finder
 *  signature="com.opensymphony.module.propertyset.ejb.types.PropertyEntry findByEntity(java.lang.String entityName, long entityId, java.lang.String key)"
 *  query="SELECT DISTINCT OBJECT(o) FROM PropertyEntry o WHERE o.entityName = ?1 AND o.entityId = ?2 AND o.key = ?3"
 *
 * @ejb.permission unchecked="true"
 * @ejb.transaction type="Supports"
 */
public abstract class PropertyEntryEJB implements EntityBean {
    //~ Static fields/initializers /////////////////////////////////////////////

    private static final Log logger = LogFactory.getLog(PropertyEntryEJB.class);

    //~ Instance fields ////////////////////////////////////////////////////////

    private EntityContext context;

    //~ Methods ////////////////////////////////////////////////////////////////

    public abstract void setEntityId(long entityId);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="entityid"
     */
    public abstract long getEntityId();

    public abstract void setEntityName(String entityName);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="entityname"
     */
    public abstract String getEntityName();

    public abstract void setId(Long id);

    /**
     * @ejb.pk-field
     * @ejb.interface-method
     * @ejb.persistence column-name="id"
     */
    public abstract Long getId();

    public abstract void setKey(String key);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="keyvalue"
     */
    public abstract String getKey();

    public abstract void setType(int type);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="type"
     */
    public abstract int getType();

    public void setEntityContext(EntityContext context) {
        this.context = context;
    }

    /**
     * @ejb.interface-method
     */
    public void setValue(Serializable value) {
        int type = getType();
        Long id = getId();

        try {
            if ((type == PropertySet.BOOLEAN) || (type == PropertySet.INT) || (type == PropertySet.LONG)) {
                PropertyNumberLocalHome home = PropertyNumberHomeFactory.getLocalHome();
                home.findByPrimaryKey(id).setValue(type, value);
            } else if (type == PropertySet.DATE) {
                PropertyDateLocalHome home = PropertyDateHomeFactory.getLocalHome();
                home.findByPrimaryKey(id).setValue(type, (Timestamp) value);
            } else if (type == PropertySet.DOUBLE) {
                PropertyDecimalLocalHome home = PropertyDecimalHomeFactory.getLocalHome();
                home.findByPrimaryKey(id).setValue(type, (Double) value);
            } else if (type == PropertySet.STRING) {
                PropertyStringLocalHome home = PropertyStringHomeFactory.getLocalHome();
                home.findByPrimaryKey(id).setValue(type, (String) value);
            } else {
                PropertyDataLocalHome home = PropertyDataHomeFactory.getLocalHome();
                home.findByPrimaryKey(id).setValue(type, value);
            }
        } catch (Exception e) {
            logger.error("Error setting value in PropertySet", e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public Serializable getValue() {
        int type = getType();
        Long id = getId();

        try {
            if ((type == PropertySet.BOOLEAN) || (type == PropertySet.INT) || (type == PropertySet.LONG)) {
                PropertyNumberLocalHome home = PropertyNumberHomeFactory.getLocalHome();

                return home.findByPrimaryKey(id).getValue(type);
            } else if (type == PropertySet.DATE) {
                PropertyDateLocalHome home = PropertyDateHomeFactory.getLocalHome();

                return home.findByPrimaryKey(id).getValue(type);
            } else if (type == PropertySet.DOUBLE) {
                PropertyDecimalLocalHome home = PropertyDecimalHomeFactory.getLocalHome();

                return home.findByPrimaryKey(id).getValue(type);
            } else if (type == PropertySet.STRING) {
                PropertyStringLocalHome home = PropertyStringHomeFactory.getLocalHome();

                return home.findByPrimaryKey(id).getValue(type);
            } else {
                PropertyDataLocalHome home = PropertyDataHomeFactory.getLocalHome();

                return home.findByPrimaryKey(id).getValue(type);
            }
        } catch (Exception e) {
            logger.warn("Error getting value from PropertySet", e);

            return null;
        }
    }

    public void ejbActivate() {
    }

    /**
     * @ejb.create-method
     */
    public Long ejbCreate(String entityName, long entityId, int type, String key) throws CreateException {
        Long id = null;

        try {
            InitialContext ctx = new InitialContext();

            SequenceGeneratorHome genHome = (SequenceGeneratorHome) PortableRemoteObject.narrow(ctx.lookup("java:comp/env/ejb/SequenceGenerator"), SequenceGeneratorHome.class);
            SequenceGenerator gen = genHome.create();
            id = new Long(gen.getCount("os.PropertyEntry"));

            setId(id);
            setEntityName(entityName);
            setEntityId(entityId);
            setType(type);
            setKey(key);

            if ((type == PropertySet.BOOLEAN) || (type == PropertySet.INT) || (type == PropertySet.LONG)) {
                PropertyNumberLocalHome home = PropertyNumberHomeFactory.getLocalHome();
                home.create(type, id.longValue());
            } else if (type == PropertySet.DATE) {
                PropertyDateLocalHome home = PropertyDateHomeFactory.getLocalHome();
                home.create(type, id.longValue());
            } else if (type == PropertySet.DOUBLE) {
                PropertyDecimalLocalHome home = PropertyDecimalHomeFactory.getLocalHome();
                home.create(type, id.longValue());
            } else if (type == PropertySet.STRING) {
                PropertyStringLocalHome home = PropertyStringHomeFactory.getLocalHome();
                home.create(type, id.longValue());
            } else {
                PropertyDataLocalHome home = PropertyDataHomeFactory.getLocalHome();
                home.create(type, id.longValue());
            }
        } catch (Exception e) {
            logger.error("Error creating new PropertyEntry", e);
            throw new CreateException(e.toString());
        }

        return id;
    }

    public void ejbLoad() {
    }

    public void ejbPassivate() {
    }

    public void ejbPostCreate(String entityName, long entityId, int type, String key) throws CreateException {
    }

    public void ejbRemove() throws RemoveException {
        int type = getType();
        Long id = getId();

        try {
            InitialContext ctx = new InitialContext();

            if ((type == PropertySet.BOOLEAN) || (type == PropertySet.INT) || (type == PropertySet.LONG)) {
                PropertyNumberLocalHome home = (PropertyNumberLocalHome) PortableRemoteObject.narrow(ctx.lookup(PropertyNumberLocalHome.COMP_NAME), PropertyNumberLocalHome.class);
                home.findByPrimaryKey(id).remove();
            } else if (type == PropertySet.DATE) {
                PropertyDateLocalHome home = (PropertyDateLocalHome) PortableRemoteObject.narrow(ctx.lookup(PropertyDateLocalHome.COMP_NAME), PropertyDateLocalHome.class);
                home.findByPrimaryKey(id).remove();
            } else if (type == PropertySet.DOUBLE) {
                PropertyDecimalLocalHome home = (PropertyDecimalLocalHome) PortableRemoteObject.narrow(ctx.lookup(PropertyDecimalLocalHome.COMP_NAME), PropertyDecimalLocalHome.class);
                home.findByPrimaryKey(id).remove();
            } else if (type == PropertySet.STRING) {
                PropertyStringLocalHome home = (PropertyStringLocalHome) PortableRemoteObject.narrow(ctx.lookup(PropertyStringLocalHome.COMP_NAME), PropertyStringLocalHome.class);
                home.findByPrimaryKey(id).remove();
            } else {
                PropertyDataLocalHome home = (PropertyDataLocalHome) PortableRemoteObject.narrow(ctx.lookup(PropertyDataLocalHome.COMP_NAME), PropertyDataLocalHome.class);
                home.findByPrimaryKey(id).remove();
            }
        } catch (Exception e) {
            logger.error("Error removing PropertySet", e);
        }
    }

    public void ejbStore() {
    }

    public void unsetEntityContext() {
        context = null;
    }
}
