/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset;

import junit.framework.TestCase;

import java.util.HashMap;


/**
 * This test case is attempts to execise a the property set API.
 * This is also a good resource for beginners
 * to PropertySet.  This class is extended to test the various SPI's.
 *
 * @author Eric Pugh (epugh@upstate.com)
 */
public abstract class BasePropertySetTest extends TestCase {
    //~ Instance fields ////////////////////////////////////////////////////////

    protected HashMap args = new HashMap();
    protected PropertySet propertySet;

    //~ Constructors ///////////////////////////////////////////////////////////

    public BasePropertySetTest(String s) {
        super(s);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setUp() throws Exception {
        super.setUp();
        propertySet = PropertySetManager.getInstance(getType(), args);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        args.clear();
    }

    public void testPropertySet() throws Exception {
        propertySet.setString("aString", "aString value");
        assertEquals("aString value", propertySet.getString("aString"));
        propertySet.setBoolean("aBoolean", true);
        assertEquals(true, propertySet.getBoolean("aBoolean"));
        propertySet.setDouble("aDouble", 10.2342);
        assertEquals(10.2342, propertySet.getDouble("aDouble"), 0);
    }

    protected abstract String getType();
}
