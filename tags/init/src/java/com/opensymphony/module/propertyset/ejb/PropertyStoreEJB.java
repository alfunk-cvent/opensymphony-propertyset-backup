/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.ejb;


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
import com.opensymphony.module.propertyset.ejb.types.PropertyEntryHomeFactory;
import com.opensymphony.module.propertyset.ejb.types.PropertyEntryLocal;
import com.opensymphony.module.propertyset.ejb.types.PropertyEntryLocalHome;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

import java.util.*;

import javax.ejb.*;

import javax.naming.NamingException;


/**
 * Session bean implementation of PropertyStore.
 *
 * <p>Makes use of ValueEntityDelegator to determine which entity beans to use for
 * appropriate types.</p>
 *
 * @ejb.bean
 *  type="Stateless"
 *  name="PropertyStore"
 *  view-type="remote"
 *  transaction-type="Container"
 *
 * @ejb.ejb-ref
 *  ejb-name="PropertyEntry"
 *  view-type="local"
 *
 * @ejb.permission unchecked="true"
 * @ejb.transaction type="Supports"
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @author <a href="mailto:hani@formicary.net">Hani Suleiman</a>
 * @version $Revision$
 *
 * @see com.opensymphony.module.propertyset.ejb.PropertyStore
 * @see com.opensymphony.module.propertyset.ejb.PropertyStoreHome
 */
public class PropertyStoreEJB implements SessionBean {
    //~ Static fields/initializers /////////////////////////////////////////////

    private static final Log logger = LogFactory.getLog(PropertyStoreEJB.class);

    //~ Instance fields ////////////////////////////////////////////////////////

    private PropertyEntryLocalHome entryHome;

    /*public void ejbPostCreate() throws CreateException {}*/
    private SessionContext context;

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     * @ejb.interface-method
     */
    public Collection getKeys(String entityName, long entityId, String prefix, int type) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("getKeys(" + entityName + "," + entityId + ")");
            }

            List results = new ArrayList();
            Iterator entries = entryHome.findByNameAndId(entityName, entityId).iterator();

            while (entries.hasNext()) {
                PropertyEntryLocal entry = (PropertyEntryLocal) entries.next();
                String key = entry.getKey();

                if (((prefix == null) || key.startsWith(prefix)) && ((type == 0) || (type == entry.getType()))) {
                    results.add(key);
                }
            }

            Collections.sort(results);

            return results;
        } catch (FinderException e) {
            logger.error("Could not find keys.", e);
            throw new PropertyImplementationException(e);
        }
    }

    public void setSessionContext(SessionContext ctx) {
        try {
            entryHome = PropertyEntryHomeFactory.getLocalHome();
        } catch (NamingException e) {
            logger.fatal("Could not lookup PropertyEntryHome.", e);
            throw new EJBException(e);
        }

        this.context = ctx;
    }

    /**
     * @ejb.interface-method
     */
    public int getType(String entityName, long entityId, String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("getType(" + entityName + "," + entityId + ",\"" + key + "\")");
        }

        try {
            return entryHome.findByEntity(entityName, entityId, key).getType();
        } catch (ObjectNotFoundException e) {
            return 0;
        } catch (FinderException e) {
            logger.error("Could not find type.", e);
            throw new PropertyImplementationException(e);
        }
    }

    public void ejbActivate() {
    }

    /**
     * @ejb.create-method
     */
    public void ejbCreate() throws CreateException {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    /**
     * @ejb.interface-method
     */
    public boolean exists(String entityName, long entityId, String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("exists(" + entityName + "," + entityId + ",\"" + key + "\")");
        }

        return getType(entityName, entityId, key) != 0;
    }

    /**
     * @ejb.interface-method
     */
    public Serializable get(String entityName, long entityId, int type, String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("get(" + entityName + "," + entityId + "," + type + ",\"" + key + "\")");
        }

        try {
            PropertyEntryLocal entry = entryHome.findByEntity(entityName, entityId, key);

            if (type != entry.getType()) {
                // type does not match
                if (logger.isDebugEnabled()) {
                    logger.debug("wrong property type");
                }

                throw new InvalidPropertyTypeException();
            }

            return entry.getValue(); // found it
        } catch (ObjectNotFoundException e) {
            // Property does not exist.
            if (logger.isDebugEnabled()) {
                logger.debug("no property found");
            }

            return null;
        } catch (PropertyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Could not retrieve value.", e);
            throw new PropertyImplementationException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void removeEntry(String entityName, long entityId, String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("remove(" + entityName + "," + entityId + ",\"" + key + "\")");
        }

        try {
            entryHome.findByEntity(entityName, entityId, key).remove();
        } catch (ObjectNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Value did not exist anyway.");
            }
        } catch (PropertyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Could not remove value.", e);
            throw new PropertyImplementationException("Could not remove value.", e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void set(String entityName, long entityId, int type, String key, Serializable value) {
        if (logger.isDebugEnabled()) {
            logger.debug("set(" + entityName + "," + entityId + "," + type + ",\"" + key + "\", [" + value + "] )");
        }

        // If null, remove value.
        if (value == null) {
            removeEntry(entityName, entityId, key);

            return;
        }

        PropertyEntryLocal entry;

        try {
            entry = entryHome.findByEntity(entityName, entityId, key);

            // if we get here, then a property with that key already exists
            if (entry.getType() != type) { // verify existing property is of same type

                if (logger.isWarnEnabled()) {
                    logger.warn("property is of different type");
                }

                throw new DuplicatePropertyKeyException();
            }
        } catch (ObjectNotFoundException e) {
            // property with that key does not yet exist
            try {
                entry = entryHome.create(entityName, entityId, type, key);
            } catch (CreateException ce) {
                logger.error("Could not create new property.", ce);
                throw new PropertyImplementationException("Could not create new property.", ce);
            }
        } catch (PropertyException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Could not set property.", e);
            throw new PropertyImplementationException("Could not set property.", e);
        }

        entry.setValue(value);
    }
}
