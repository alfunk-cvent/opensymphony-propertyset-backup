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
import com.opensymphony.module.propertyset.*;

import com.opensymphony.util.Data;
import com.opensymphony.util.XMLUtils;

import org.w3c.dom.Document;

import java.io.*;

import java.util.Properties;

import javax.ejb.*;


/**
 * AbstractValueEntityEJB concrete implementation optimized for storing binary data.
 * This can be used to store TEXT, OBJECT, DATA, XML, PROPERTIES or anything else
 * that cannot be stored elsewhere.
 *
 * @ejb.bean
 *  type="CMP"
 *  name="PropertyData"
 *  view-type="local"
 *  reentrant="False"
 *  primkey-field="id"
 *
 * @ejb.pk class="java.lang.Long" extends="java.lang.Object"
 *
 * @ejb.persistence table-name="OS_PROPERTYDATA"
 * @ejb.permission unchecked="true"
 * @ejb.transaction type="Supports"
 *
 * @author <a href="mailto:hani@formicary.net">Hani Suleiman</a>
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision$
 *
 */
public abstract class DataEntityEJB implements EntityBean {
    //~ Static fields/initializers /////////////////////////////////////////////

    /**
     * Flag to signify no value in a property. Some db's can't store nulls or 0 length fields in LOBs properly.
     */
    private static final byte[] NULL_DATA = "hello world".getBytes();

    //~ Instance fields ////////////////////////////////////////////////////////

    private EntityContext context;

    //~ Methods ////////////////////////////////////////////////////////////////

    public abstract void setBytes(byte[] data);

    /**
     * @ejb.persistence column-name="value"
     */
    public abstract byte[] getBytes();

    public abstract void setId(Long id);

    /**
     * @ejb.pk-field
     * @ejb.interface-method
     * @ejb.persistence column-name="id"
     */
    public abstract Long getId();

    public void setEntityContext(EntityContext context) {
        this.context = context;
    }

    /**
     * Set the value. Depending on the type, different casting serializing will occur.
     *
     * <p>For TEXT, value must be java.lang.String.<br>
     * For OBJECT, value must be Serializable object.<br>
     * For XML, value must be org.w3c.dom.Document.<br>
     * For DATA, value must be com.opensymphony.util.Data<br>
     * For PROPERTIES, value must be java.util.Properties<br>
     *
     * @ejb.interface-method
     */
    public void setValue(int type, Serializable value) {
        if (value == null) {
            setBytes(NULL_DATA);

            return;
        }

        try {
            switch (type) {
            case PropertySet.TEXT:
                setBytes(((String) value).getBytes());

                break;

            case PropertySet.OBJECT:

                if (!(value instanceof Serializable)) {
                    throw new IllegalPropertyException("Object not serializable.");
                }

                setBytes(writeObject(value));

                break;

            case PropertySet.XML:
                setBytes(writeXML((Document) value));

                break;

            case PropertySet.DATA:
                setBytes(((Data) value).getBytes());

                break;

            case PropertySet.PROPERTIES:
                setBytes(writeProperties((Properties) value));

                break;

            default:

                // this should never happen.
                throw new PropertyImplementationException("Cannot store this type of property.");
            }

            if (getBytes().length == 0) {
                setBytes(NULL_DATA); // some db's have problems storing nothing
            }
        } catch (ClassCastException ce) {
            throw new IllegalPropertyException("Cannot cast value to appropriate type for persistence.");
        }
    }

    /**
     * Create appropriate wrapper object around value (String, Object, Document, Data, Properties).
     *
     * @ejb.interface-method
     */
    public Serializable getValue(int type) {
        byte[] value = getBytes();

        switch (type) {
        case PropertySet.TEXT:
            return new String(value);

        case PropertySet.OBJECT:
            return (Serializable) readObject(value);

        case PropertySet.XML:
            return (Serializable) readXML(value);

        case PropertySet.DATA:
            return new Data(value);

        case PropertySet.PROPERTIES:
            return readProperties(value);

        default:

            // this should never happen.
            throw new PropertyImplementationException("Cannot retrieve this type of property.");
        }
    }

    public void ejbActivate() {
    }

    /**
     * @ejb.create-method
     */
    public Long ejbCreate(int type, long id) throws CreateException {
        setId(new Long(id));

        // the concrete setValue() method shall set value to default (not null).
        setValue(type, null);

        return null;
    }

    public void ejbLoad() {
    }

    public void ejbPassivate() {
    }

    public void ejbPostCreate(int type, long id) throws CreateException {
    }

    public void ejbRemove() throws RemoveException {
    }

    public void ejbStore() {
    }

    public void unsetEntityContext() {
        context = null;
    }

    /**
     * Returns { PropertySet.TEXT, PropertySet.OBJECT, PropertySet.XML, PropertySet.DATA, PropertySet.PROPERTIES }
     */
    protected int[] allowedTypes() {
        return new int[] {
            PropertySet.TEXT, PropertySet.OBJECT, PropertySet.XML,
            PropertySet.DATA, PropertySet.PROPERTIES
        };
    }

    /**
     * DeSerialize an Object from byte array.
     */
    private Object readObject(byte[] data) {
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(data);
            ObjectInputStream stream = new ObjectInputStream(bytes);
            Object result = stream.readObject();
            stream.close();

            return result;
        } catch (IOException e) {
            throw new PropertyImplementationException("Cannot deserialize Object", e);
        } catch (ClassNotFoundException e) {
            throw new PropertyImplementationException("Class not found for Object", e);
        }
    }

    /**
     * Load java.util.Properties from byte array.
     */
    private Properties readProperties(byte[] data) {
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(data);
            Properties result = new Properties();
            result.load(bytes);

            return result;
        } catch (Exception e) {
            throw new PropertyImplementationException("Cannot load Properties.", e);
        }
    }

    /**
     * Parse XML document from String in byte array.
     */
    private Document readXML(byte[] data) {
        try {
            ByteArrayInputStream bytes = new ByteArrayInputStream(data);

            return XMLUtils.parse(bytes);
        } catch (Exception e) {
            throw new PropertyImplementationException("Cannot parse XML data.", e);
        }
    }

    /**
     * Serialize an Object to byte array.
     */
    private byte[] writeObject(Object o) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(bytes);
            stream.writeObject(o);
            stream.close();
            bytes.flush();

            return bytes.toByteArray();
        } catch (IOException e) {
            throw new PropertyImplementationException("Cannot serialize Object", e);
        }
    }

    /**
     * Store java.util.Properties to byte array.
     */
    private byte[] writeProperties(Properties p) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            p.store(bytes, null);
            bytes.flush();

            return bytes.toByteArray();
        } catch (IOException e) {
            throw new PropertyImplementationException("Cannot store Properties.", e);
        }
    }

    /**
     * Serialize (print) XML document to byte array (as String).
     */
    private byte[] writeXML(Document doc) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            XMLUtils.print(doc, bytes);
            bytes.flush();

            return bytes.toByteArray();
        } catch (IOException e) {
            throw new PropertyImplementationException("Cannot serialize XML", e);
        }
    }
}
