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
import java.util.HashSet;
import java.util.Set;


/**
 * Handles verification of Strings.
 * Can be configured to only accept only strings within a given
 * length range. Omitted values are assumed to be unconstrained.
 * For example:<br><code>
 * StringVerifier sv = new StringVerifier();
 * sv.setMaxLength(50);
 * </code><br>
 * Will accept any string that is less than 50 characters in length.<p>
 * Note though that the default max length of a string is 255 chars.
 *
 * @author <a href="mailto:hani@fate.demon.co.uk">Hani Suleiman</a>
 * @version $Revision$
 */
public class StringVerifier implements PropertyVerifier {
    //~ Instance fields ////////////////////////////////////////////////////////

    private Set allowableStrings;
    private String contains;
    private String prefix;
    private String suffix;
    private int max = 255;
    private int min = 0;

    //~ Constructors ///////////////////////////////////////////////////////////

    public StringVerifier() {
    }

    /**
     * Create a StringVerifier with the specified min and max lengths.
     * @param min The minimum allowable string length.
     * @param max The maximum allowable string length.
     */
    public StringVerifier(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public StringVerifier(String[] allowable) {
        setAllowableValues(allowable);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setAllowableValues(String[] vals) {
        allowableStrings = new HashSet();

        //Store the array in a set, since all we'll be doing is lookups.
        for (int i = 0; i < vals.length; i++) {
            allowableStrings.add(vals[i]);
        }
    }

    public void setContains(String s) {
        contains = s;
    }

    public String getContains() {
        return contains;
    }

    public void setMaxLength(int max) {
        this.max = max;
    }

    public int getMaxLength() {
        return max;
    }

    public void setMinLength(int min) {
        this.min = min;
    }

    public int getMinLength() {
        return min;
    }

    public void setPrefix(String s) {
        prefix = s;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setSuffix(String s) {
        suffix = s;
    }

    public String getSuffix() {
        return suffix;
    }

    public void verify(Object o) throws VerifyException {
        String s = (String) o;

        if (s.length() < min) {
            throw new VerifyException("String " + s + " too short, min length=" + min);
        }

        if (s.length() > max) {
            throw new VerifyException("String " + s + " too long, max length=" + max);
        }

        if ((suffix != null) && !s.endsWith(suffix)) {
            throw new VerifyException("String " + s + " has invalid suffix (suffix must be \"" + suffix + "\")");
        }

        if ((prefix != null) && !s.startsWith(prefix)) {
            throw new VerifyException("String " + s + " has invalid prefix (prefix must be \"" + prefix + "\")");
        }

        if ((contains != null) && (s.indexOf(contains) == -1)) {
            throw new VerifyException("String " + s + " does not contain required string \"" + contains + "\"");
        }

        if ((allowableStrings != null) && !allowableStrings.contains(s)) {
            throw new VerifyException("String " + s + " not in allowed set for this property");
        }
    }
}
