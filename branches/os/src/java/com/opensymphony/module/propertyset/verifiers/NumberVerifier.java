/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.verifiers;


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

/**
 * Handles verification of numbers.
 * Can be configured to only accept specific numeric types (int, float, etc)
 * as well as a range for the specified number. All constraints are
 * optional. If not specified, then any number is accepted.
 *
 * @author <a href="mailto:hani@fate.demon.co.uk">Hani Suleiman</a>
 * @version $Revision$
 */
public class NumberVerifier implements PropertyVerifier {
    //~ Instance fields ////////////////////////////////////////////////////////

    private Class type;
    private Number max;
    private Number min;

    //~ Constructors ///////////////////////////////////////////////////////////

    public NumberVerifier() {
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setMax(Number num) {
        //Should we check if(type!=null && num.getClass()==type) ? Also ensure min/max classes match?
        max = num;
    }

    public Number getMax() {
        return max;
    }

    public void setMin(Number num) {
        //Should we check if(type!=null && num.getClass()==type) ? Also ensure min/max classes match?
        min = num;
    }

    public Number getMin() {
        return min;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }

    public void verify(Object value) throws VerifyException {
        //Should we wrap up a ClassCastException here?
        Number num = (Number) value;

        if (num.getClass() != type) {
            throw new VerifyException("value is of type " + num.getClass() + " expected type is " + type);
        }

        //Hmm, should we convert everything to doubles (performance?) or deal with every possible
        //Number subclass that we support?
        if ((min != null) && (value != null) && (min.doubleValue() > num.doubleValue())) {
            throw new VerifyException("value " + num.doubleValue() + " < min limit " + min.doubleValue());
        }

        if ((max != null) && (value != null) && (max.doubleValue() < num.doubleValue())) {
            throw new VerifyException("value " + num.doubleValue() + " > max limit " + max.doubleValue());
        }

        //Fall through case, allow any Number object.
    }
}
