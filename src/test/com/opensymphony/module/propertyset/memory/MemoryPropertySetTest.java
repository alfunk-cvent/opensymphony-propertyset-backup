/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.memory;

import junit.framework.TestCase;


/**
 * Simple MemoryPropertySet test.
 *
 * @author $Author$
 * @version $Revision$
 */
public class MemoryPropertySetTest extends TestCase {
    //~ Methods ////////////////////////////////////////////////////////////////

    public void testBasic() {
        MemoryPropertySet ps = new MemoryPropertySet();
        ps.init(null, null);
        ps.setString("aString", "aString value");
        assertEquals("aString value", ps.getString("aString"));
    }
}
