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
import com.opensymphony.module.propertyset.verifiers.PropertyVerifier;
import com.opensymphony.module.propertyset.verifiers.VerifyException;

import java.io.Serializable;

import java.util.*;


/**
 * Describes the meta data for a given property.
 * The meta data for a property includes its type as well as
 * any verifiers that constrain it.
 *
 * todo: add multiplicity?
 *
 * @author <a href="mailto:hani@fate.demon.co.uk">Hani Suleiman</a>
 * @version $Revision$
 */
public class PropertySchema implements Serializable {
    //~ Instance fields ////////////////////////////////////////////////////////

    private Collection verifiers;
    private String name;
    private int type;

    //~ Constructors ///////////////////////////////////////////////////////////

    public PropertySchema() {
        this(null);
    }

    public PropertySchema(String name) {
        super();
        this.name = name;
        verifiers = new HashSet();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setPropertyName(String s) {
        name = s;
    }

    public String getPropertyName() {
        return name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    /**
     * Returns unmodifiable List of verifiers.
     */
    public Collection getVerifiers() {
        return Collections.unmodifiableCollection(verifiers);
    }

    public boolean addVerifier(PropertyVerifier pv) {
        return verifiers.add(pv);
    }

    public boolean removeVerifier(PropertyVerifier pv) {
        return verifiers.remove(pv);
    }

    /**
     * Validate a given value against all verifiers.
     * Default behaviour is to AND all verifiers.
     */
    public void validate(Object value) throws PropertyException {
        Iterator i = verifiers.iterator();

        while (i.hasNext()) {
            PropertyVerifier pv = (PropertyVerifier) i.next();

            //Hmm, do we need a try/catch?
            try {
                pv.verify(value);
            } catch (VerifyException ex) {
                throw new IllegalPropertyException(ex.getMessage());
            }
        }
    }
}
