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
import java.util.Iterator;


/**
 * The PropertySetCloner is used to copy all the properties from one PropertySet into another.
 *
 * <h3>Example</h3>
 *
 * <blockquote><code>
 *   EJBPropertySet source = new EJBPropertySet("ejb/PropertyStore","MyEJB",7);<br>
 *   XMLPropertySet dest   = new XMLPropertySet();<br>
 *   <br>
 *   PropertySetCloner cloner = new PropertySetCloner();<br>
 *   cloner.setSource( source );<br>
 *   cloner.setDestination( dest );<br>
 *   <br>
 *   cloner.cloneProperties();<br>
 *   dest.save( new FileWriter("propertyset-MyEJB-7.xml") );<br>
 * </code></blockquote>
 *
 * <p>The above example demonstrates how a PropertySetCloner can be used to export properties
 * stores in an EJBPropertySet to an XML file.</p>
 *
 * <p>If the destination PropertySet contains any properties, they will be cleared before
 * the source properties are copied across.</p>
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision$
 */
public class PropertySetCloner {
    //~ Instance fields ////////////////////////////////////////////////////////

    private PropertySet destination;
    private PropertySet source;

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setDestination(PropertySet destination) {
        this.destination = destination;
    }

    public PropertySet getDestination() {
        return destination;
    }

    public void setSource(PropertySet source) {
        this.source = source;
    }

    public PropertySet getSource() {
        return source;
    }

    public void cloneProperties() throws PropertyException {
        clearDestination();

        Iterator keys = source.getKeys().iterator();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            cloneProperty(key);
        }
    }

    /**
     * Clear all properties that already exist in destination PropertySet.
     */
    private void clearDestination() throws PropertyException {
        Iterator keys = destination.getKeys().iterator();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            destination.remove(key);
        }
    }

    /**
     * Copy individual property from source to destination.
     */
    private void cloneProperty(String key) throws PropertyException {
        switch (source.getType(key)) {
        case PropertySet.BOOLEAN:
            destination.setBoolean(key, source.getBoolean(key));

            break;

        case PropertySet.INT:
            destination.setInt(key, source.getInt(key));

            break;

        case PropertySet.LONG:
            destination.setLong(key, source.getLong(key));

            break;

        case PropertySet.DOUBLE:
            destination.setDouble(key, source.getDouble(key));

            break;

        case PropertySet.STRING:
            destination.setString(key, source.getString(key));

            break;

        case PropertySet.TEXT:
            destination.setText(key, source.getText(key));

            break;

        case PropertySet.DATE:
            destination.setDate(key, source.getDate(key));

            break;

        case PropertySet.OBJECT:
            destination.setObject(key, source.getObject(key));

            break;

        case PropertySet.XML:
            destination.setXML(key, source.getXML(key));

            break;

        case PropertySet.DATA:
            destination.setData(key, source.getData(key));

            break;

        case PropertySet.PROPERTIES:
            destination.setProperties(key, source.getProperties(key));

            break;
        }
    }
}
