/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.ofbiz;

import com.opensymphony.module.propertyset.*;


/**
 * @author <a href="mailto:salaman@qoretech.com">Victor Salaman</a>
 * $Revision$
 */
public class DatePropertyHandler implements PropertyHandler {
    //~ Methods ////////////////////////////////////////////////////////////////

    // Attributes ----------------------------------------------------
    // Static --------------------------------------------------------
    // Constructors --------------------------------------------------
    // Public --------------------------------------------------------
    public Object processGet(int type, Object input) throws PropertyException {
        if (type == PropertySet.DATE) {
            return input;
        }

        throw new InvalidPropertyTypeException();
    }

    public Object processSet(int type, Object input) throws PropertyException {
        if (type == PropertySet.DATE) {
            if (input instanceof java.util.Date) {
                return new java.sql.Timestamp(((java.util.Date) input).getTime());
            } else if (input instanceof java.sql.Date) {
                return new java.sql.Timestamp(((java.sql.Date) input).getTime());
            } else if (input instanceof java.sql.Timestamp) {
                return input;
            }
        }

        throw new InvalidPropertyTypeException();
    }

    // Package protected ---------------------------------------------
    // Protected -----------------------------------------------------
    // Private -------------------------------------------------------
    // Inner classes -------------------------------------------------
}
