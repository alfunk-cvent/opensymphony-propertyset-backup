/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.memory;


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

import java.io.Serializable;

import java.util.*;


/**
 * The MemoryPropertySet is a PropertySet implementation that
 * will store any primitive or object in an internal Map
 * that is stored in memory.
 *
 * <p>An alternative to MemoryPropertySet is SerializablePropertySet
 * which can be Serialized to/from a stream.</p>
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision$
 *
 * @see com.opensymphony.module.propertyset.PropertySet
 */
public class MemoryPropertySet extends AbstractPropertySet {
    //~ Instance fields ////////////////////////////////////////////////////////

    private HashMap map;

    //~ Methods ////////////////////////////////////////////////////////////////

    public synchronized Collection getKeys(String prefix, int type) {
        Iterator keys = getMap().keySet().iterator();
        List result = new LinkedList();

        while (keys.hasNext()) {
            String key = (String) keys.next();

            if ((prefix == null) || key.startsWith(prefix)) {
                if (type == 0) {
                    result.add(key);
                } else {
                    ValueEntry v = (ValueEntry) getMap().get(key);

                    if (v.type == type) {
                        result.add(key);
                    }
                }
            }
        }

        Collections.sort(result);

        return result;
    }

    public synchronized int getType(String key) {
        if (getMap().containsKey(key)) {
            return ((ValueEntry) getMap().get(key)).type;
        } else {
            return 0;
        }
    }

    public synchronized boolean exists(String key) {
        return getType(key) > 0;
    }

    public void init(Map config, Map args) {
        map = new HashMap();
    }

    public synchronized void remove(String key) {
        getMap().remove(key);
    }

    protected synchronized void setImpl(int type, String key, Object value) throws DuplicatePropertyKeyException {
        if (exists(key)) {
            ValueEntry v = (ValueEntry) getMap().get(key);

            if (v.type != type) {
                throw new DuplicatePropertyKeyException();
            }

            v.value = value;
        } else {
            getMap().put(key, new ValueEntry(type, value));
        }

        return;
    }

    protected HashMap getMap() {
        return map;
    }

    protected synchronized Object get(int type, String key) throws InvalidPropertyTypeException {
        if (exists(key)) {
            ValueEntry v = (ValueEntry) getMap().get(key);

            if (v.type != type) {
                throw new InvalidPropertyTypeException();
            }

            return v.value;
        } else {
            return null;
        }
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    public static final class ValueEntry implements Serializable {
        Object value;
        int type;

        public ValueEntry() {
        }

        public ValueEntry(int type, Object value) {
            this.type = type;
            this.value = value;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
