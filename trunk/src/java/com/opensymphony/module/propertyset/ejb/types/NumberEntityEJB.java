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

import java.io.Serializable;

import javax.ejb.*;


/**
 * AbstractValueEntityEJB concrete implementation optimized for storing numbers.
 *  This can be used to store BOOLEAN, INT and LONG.
 *
 * @ejb.bean
 *  type="CMP"
 *  name="PropertyNumber"
 *  schema="PropertyNumber"
 *  view-type="local"
 *  reentrant="False"
 *  primkey-field="id"
 *
 * @ejb.pk class="java.lang.Long" extends="java.lang.Object"
 *
 * @ejb.persistence table-name="OS_PROPERTYNUMBER"
 * @ejb.permission unchecked="true"
 * @ejb.transaction type="Supports"
 *
 * @author <a href="mailto:hani@formicary.net">Hani Suleiman</a>
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @version $Revision$
 *
 * @see com.opensymphony.module.propertyset.ejb.PropertyStoreEJB
 */
public abstract class NumberEntityEJB implements EntityBean {
    //~ Instance fields ////////////////////////////////////////////////////////

    private EntityContext context;

    //~ Methods ////////////////////////////////////////////////////////////////

    public abstract void setId(Long id);

    /**
     * @ejb.pk-field
     * @ejb.interface-method
     * @ejb.persistence column-name="id"
     */
    public abstract Long getId();

    public abstract void setNumber(long date);

    /**
     * @ejb.persistence column-name="value"
     */
    public abstract long getNumber();

    public void setEntityContext(EntityContext context) {
        this.context = context;
    }

    /**
     * Set the value. Only java.lang.Number, java.lang.Boolean or java.util.Date can be supplied.
     *
     * @ejb.interface-method
     */
    public void setValue(int type, Serializable value) {
        if (value == null) {
            setNumber(0L);

            return;
        }

        try {
            switch (type) {
            case PropertySet.BOOLEAN:
                setNumber(((Boolean) value).booleanValue() ? 1L : 0L);

                return;

            case PropertySet.INT:
            case PropertySet.LONG:
                setNumber(((Number) value).longValue());

                return;

            default:

                // this should never happen.
                throw new PropertyImplementationException("Cannot store this type of property.");
            }
        } catch (ClassCastException ce) {
            throw new IllegalPropertyException("Cannot cast value to appropriate type for persistence.");
        }
    }

    /**
     * Create appropriate wrapper object around value (Boolean, Integer, Long).
     *
     * @ejb.interface-method
     */
    public Serializable getValue(int type) {
        long value = getNumber();

        switch (type) {
        case PropertySet.BOOLEAN:
            return (value == 0L) ? Boolean.FALSE : Boolean.TRUE;

        case PropertySet.INT:
            return new Integer((int) value);

        case PropertySet.LONG:
            return new Long(value);

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
     * Returns { PropertySet.BOOLEAN, PropertySet.INT, PropertySet.LONG }
     */
    protected int[] allowedTypes() {
        return new int[] {PropertySet.BOOLEAN, PropertySet.INT, PropertySet.LONG};
    }
}
