/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.map;


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
import com.opensymphony.module.propertyset.AbstractPropertySet;

import java.util.*;


/**
 * The MapPropertySet is an UNTYPED PropertySet implementation that
 * acts as a wrapper around a standard {@link java.util.Map} .
 *
 * <p>Because Map's will only store the value but not the type, this
 * is untyped. See {@link com.opensymphony.module.propertyset.PropertySet}
 * for explanation.</p>
 *
 * <b>Optional Args</b>
 * <ul>
 *  <li><b>map</b> - the map that will back this PropertySet</li>
 * </ul>
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision$
 *
 * @see com.opensymphony.module.propertyset.PropertySet
 */
public class MapPropertySet extends AbstractPropertySet {
    //~ Instance fields ////////////////////////////////////////////////////////

    /**
    * Underlying Map storing properties.
    */
    protected Map map;

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
    * The type parameter is ignored.
    */
    public synchronized Collection getKeys(String prefix, int type) {
        Iterator keys = map.keySet().iterator();
        List result = new LinkedList();

        while (keys.hasNext()) {
            String key = (String) keys.next();

            if ((prefix == null) || key.startsWith(prefix)) {
                result.add(key);
            }
        }

        Collections.sort(result);

        return result;
    }

    /**
    * Set underlying map.
    */
    public synchronized void setMap(Map map) {
        if (map == null) {
            throw new NullPointerException("Map cannot be null.");
        }

        this.map = map;
    }

    /**
    * Retrieve underlying map.
    */
    public synchronized Map getMap() {
        return map;
    }

    /**
    * This is an untyped PropertySet implementation so this method will always
    * throw {@link java.lang.UnsupportedOperationException} .
    */
    public int getType(String key) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("PropertySet does not support types");
    }

    public synchronized boolean exists(String key) {
        return map.containsKey(key);
    }

    public void init(Map config, Map args) {
        map = (Map) args.get("map");

        if (map == null) {
            map = new HashMap();
        }
    }

    public synchronized void remove(String key) {
        map.remove(key);
    }

    /**
    * Returns false.
    */
    public boolean supportsType(int type) {
        return false;
    }

    /**
    * Returns false.
    */
    public boolean supportsTypes() {
        return false;
    }

    /**
    * The type parameter is ignored.
    */
    protected synchronized void setImpl(int type, String key, Object value) {
        map.put(key, value);
    }

    /**
    * The type parameter is ignored.
    */
    protected synchronized Object get(int type, String key) {
        return exists(key) ? map.get(key) : null;
    }
}
