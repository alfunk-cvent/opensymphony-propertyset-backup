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

import com.opensymphony.util.DataUtil;
import com.opensymphony.util.EJBUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.Collection;
import java.util.Map;

import javax.ejb.CreateException;

import javax.naming.NamingException;


/**
 * The EJBPropertySet is an implementation of
 * {@link com.opensymphony.module.propertyset.PropertySet} that
 * uses Enterprise JavaBeans to store and retrieve Properties.
 *
 * <p>This class is a proxy to the
 * {@link com.opensymphony.module.propertyset.ejb.PropertyStore}
 * Session Bean that handles the PropertySet and behind the scenes
 * delegates to various Entity Beans to persist the data in an
 * efficient way.</p>
 *
 * <p>Each method in the proxy will catch any thrown
 * {@link java.rmi.RemoteException} and rethrow it wrapped in a
 * {@link com.opensymphony.module.propertyset.PropertyImplementationException} .</p>
 *
 * <h3>Usage</h3>
 *
 * <p>In order to use an EJBPropertySet, a PropertyStore Session Bean
 * must first be retrieved that represents the PropertySet data. This
 * is typically either returned by another EJB, or looked up using
 * an JNDI location for PropertyStoreHome and an int ID for the actual
 * PropertySet used.</p>
 *
 * <b>Required Args</b>
 * <ul>
 *  <li><b>entityId</b> - Long that holds the ID of this entity</li>
 *  <li><b>entityName</b> - String that holds the name of this entity type</li>
 * </ul>
 * <p>
 *
 * <b>Optional Configuration</b>
 * <ul>
 *  <li><b>storeLocation</b> - the JNDI name for the PropertyStore EJB lookup (defaults to os.PropertyStore)</li>
 * </ul>
 *
 * @author <a href="mailto:joe@wirestation.co.uk">
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 * @version $Revision$
 *
 * @see com.opensymphony.module.propertyset.PropertySet
 * @see com.opensymphony.module.propertyset.ejb.PropertyStore
 * @see com.opensymphony.module.propertyset.ejb.PropertyStoreHome
 */
public class EJBPropertySet extends AbstractPropertySet implements Serializable {
    //~ Static fields/initializers /////////////////////////////////////////////

    private static final Log logger = LogFactory.getLog(EJBPropertySet.class);

    //~ Instance fields ////////////////////////////////////////////////////////

    private PropertyStore store;
    private String entityName;
    private long entityId;

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
    * Proxy to {@link com.opensymphony.module.propertyset.ejb.PropertyStore#getKeys(java.lang.String,long,java.lang.String,int)}
    */
    public Collection getKeys(String prefix, int type) throws PropertyException {
        try {
            return store.getKeys(entityName, entityId, prefix, type);
        } catch (RemoteException re) {
            throw new PropertyImplementationException(re);
        }
    }

    /**
    * Proxy to {@link com.opensymphony.module.propertyset.ejb.PropertyStore#getType(java.lang.String,long,java.lang.String)}
    */
    public int getType(String key) throws PropertyException {
        try {
            return store.getType(entityName, entityId, key);
        } catch (RemoteException re) {
            throw new PropertyImplementationException(re);
        }
    }

    /**
    * Proxy to {@link com.opensymphony.module.propertyset.ejb.PropertyStore#exists(java.lang.String,long,java.lang.String)}
    */
    public boolean exists(String key) throws PropertyException {
        try {
            return store.exists(entityName, entityId, key);
        } catch (RemoteException re) {
            throw new PropertyImplementationException(re);
        }
    }

    public void init(Map config, Map args) {
        entityId = DataUtil.getLong((Long) args.get("entityId"));
        entityName = (String) args.get("entityName");

        String storeLocation = (String) config.get("storeLocation");

        if (storeLocation == null) {
            storeLocation = "PropertyStore";
        }

        try {
            PropertyStoreHome home = (PropertyStoreHome) EJBUtils.lookup(storeLocation, PropertyStoreHome.class);
            store = home.create();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (CreateException e) {
            e.printStackTrace();
        }
    }

    /**
    * Proxy to {@link com.opensymphony.module.propertyset.ejb.PropertyStore#remove(java.lang.String,long,java.lang.String)}
    */
    public void remove(String key) throws PropertyException {
        try {
            store.removeEntry(entityName, entityId, key);
        } catch (RemoteException re) {
            throw new PropertyImplementationException(re);
        }
    }

    /**
    * Proxy to {@link com.opensymphony.module.propertyset.ejb.PropertyStore#set(java.lang.String,long,int,java.lang.String,java.io.Serializable)}
    */
    protected void setImpl(int type, String key, Object value) throws PropertyException {
        try {
            store.set(entityName, entityId, type, key, (Serializable) value);
        } catch (RemoteException re) {
            logger.error("RemoteExecption while setting property", re);
            throw new PropertyImplementationException(re);
        }
    }

    /**
    * Proxy to {@link com.opensymphony.module.propertyset.ejb.PropertyStore#get(java.lang.String,long,int,java.lang.String)}
    */
    protected Object get(int type, String key) throws PropertyException {
        try {
            return store.get(entityName, entityId, type, key);
        } catch (RemoteException re) {
            throw new PropertyImplementationException(re);
        }
    }
}
