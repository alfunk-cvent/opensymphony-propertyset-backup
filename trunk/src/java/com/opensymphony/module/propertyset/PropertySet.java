/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset;


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
import org.w3c.dom.Document;

import java.util.*;


/**
 * A <code>PropertySet</code> is designed to be associated with other entities
 * in the system for storing key/value property pairs.
 *
 * <p>A key can only contain one value and a key is unique across all types. If
 * a property is set using the same key and an already existing property of the
 * SAME type, the new value will overwrite the old. However, if a property of
 * DIFFERENT type attempts to overwrite the existing value, a
 * {@link com.opensymphony.module.propertyset.DuplicatePropertyKeyException}
 * should be thrown.</p>
 *
 * <p>If a property is set of a type that is not allowed, a
 * {@link com.opensymphony.module.propertyset.IllegalPropertyException}
 * should be thrown.</p>
 *
 * <p>If a property is retrieved that exists but contains a value of different
 * type, a
 * {@link com.opensymphony.module.propertyset.InvalidPropertyTypeException}
 * should be thrown.</p>
 *
 * <p>If a property is retrieved that does not exist, null (or the primitive
 * equivalent) is returned.</p>
 *
 * <p>If an Exception is encountered in the actual implementation of the
 * PropertySet that needs to be rethrown, it should be wrapped in a
 * {@link com.opensymphony.module.propertyset.PropertyImplementationException}
 * .</p>
 *
 * <p>Some PropertySet implementations may not store along side the data the original
 * type it was set as. This means that it could be retrieved using a get method of
 * a different type without throwing an InvalidPropertyTypeException (so long as the
 * original type can be converted to the requested type.</p>
 *
 * <p><b>Typed PropertySet Example</b></p>
 *
 * <p><code>
 * propertySet.setString("something","99");<br>
 * x = propertySet.getString("something"); // throws InvalidPropertyTypeException
 * </code></p>
 *
 * <p><b>Untyped PropertySet Example</b></p>
 *
 * <p><code>
 * propertySet.setString("something","99");<br>
 * x = propertySet.getString("something"); // returns 99.
 * </code></p>
 *
 * <p>Typically (unless otherwise stated), an implementation is typed. This can be
 * checked by calling the {@link #supportsTypes()} method of the implementation.</p>
 *
 * <p>Not all PropertySet implementations need to support setter methods (i.e.
 * they are read only) and not all have to support storage/retrieval of specific
 * types. The capabilities of the specific implementation can be determined by
 * calling {@link #supportsType(int)} and {@link #isSettable(String)} .</p>
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision$
 */
public interface PropertySet {
    //~ Instance fields ////////////////////////////////////////////////////////

    /** Value-type boolean */
    int BOOLEAN = 1;

    /** Value-type byte[] */
    int DATA = 10;

    /** Value-type {@link java.util.Date} */
    int DATE = 7;

    /** Value-type double */
    int DOUBLE = 4;

    /** Value-type int */
    int INT = 2;

    /** Value-type long */
    int LONG = 3;

    /** Value-type serializable {@link java.lang.Object} */
    int OBJECT = 8;

    /** Value-type {@link java.util.Properties} */
    int PROPERTIES = 11;

    /** Value-type {@link java.lang.String} (max length 255) */
    int STRING = 5;

    /** Value-type text (unlimited length {@link java.lang.String})  */
    int TEXT = 6;

    /** Value-type XML {@link org.w3c.dom.Document} */
    int XML = 9;

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setSchema(PropertySetSchema schema) throws PropertyException;

    public PropertySetSchema getSchema() throws PropertyException;

    void setAsActualType(String key, Object value) throws PropertyException;

    Object getAsActualType(String key) throws PropertyException;

    void setBoolean(String key, boolean value) throws PropertyException;

    boolean getBoolean(String key) throws PropertyException;

    void setData(String key, byte[] value) throws PropertyException;

    byte[] getData(String key) throws PropertyException;

    void setDate(String key, Date value) throws PropertyException;

    Date getDate(String key) throws PropertyException;

    void setDouble(String key, double value) throws PropertyException;

    double getDouble(String key) throws PropertyException;

    void setInt(String key, int value) throws PropertyException;

    int getInt(String key) throws PropertyException;

    /**
    * List all keys.
    *
    * @return Unmodifiable {@link java.util.Collection} of
    *         {@link java.lang.String}s.
    */
    Collection getKeys() throws PropertyException;

    /**
    * List all keys of certain type.
    *
    * @param type Type to list. See static class variables. If null, then
    *        all types shall be returned.
    * @return Unmodifiable {@link java.util.Collection} of
    *         {@link java.lang.String}s.
    */
    Collection getKeys(int type) throws PropertyException;

    /**
    * List all keys starting with supplied prefix.
    *
    * @param prefix String that keys must start with. If null, than all
    *        keys shall be returned.
    * @return Unmodifiable {@link java.util.Collection} of
    *         {@link java.lang.String}s.
    */
    Collection getKeys(String prefix) throws PropertyException;

    /**
    * List all keys starting with supplied prefix of certain type. See
    * statics.
    *
    * @param prefix String that keys must start with. If null, than all
    *        keys shall be returned.
    * @param type Type to list. See static class variables. If null, then
    *        all types shall be returned.
    * @return Unmodifiable {@link java.util.Collection} of
    *         {@link java.lang.String}s.
    */
    Collection getKeys(String prefix, int type) throws PropertyException;

    void setLong(String key, long value) throws PropertyException;

    long getLong(String key) throws PropertyException;

    void setObject(String key, Object value) throws PropertyException;

    Object getObject(String key) throws PropertyException;

    void setProperties(String key, Properties value) throws PropertyException;

    Properties getProperties(String key) throws PropertyException;

    /**
    * Whether this PropertySet implementation allows values to be set
    * (as opposed to read-only).
    */
    boolean isSettable(String property);

    void setString(String key, String value) throws PropertyException;

    /**
    * {@link java.lang.String} of maximum 255 chars.
    */
    String getString(String key) throws PropertyException;

    void setText(String key, String value) throws PropertyException;

    /**
    * {@link java.lang.String} of unlimited length.
    */
    String getText(String key) throws PropertyException;

    /**
    * Returns type of value.
    *
    * @return Type of value. See static class variables.
    */
    int getType(String key) throws PropertyException;

    void setXML(String key, Document value) throws PropertyException;

    Document getXML(String key) throws PropertyException;

    /**
    * Determine if property exists.
    */
    boolean exists(String key) throws PropertyException;

    void init(Map config, Map args);

    /**
    * Removes property.
    */
    void remove(String key) throws PropertyException;

    /**
    * Whether this PropertySet implementation allows the type specified
    * to be stored or retrieved.
    */
    boolean supportsType(int type);

    /**
    * Whether this PropertySet implementation supports types when storing values.
     * (i.e. the type of data is stored as well as the actual value).
    */
    boolean supportsTypes();
}
