/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.memory;

import com.opensymphony.module.propertyset.BasePropertySetTest;


/**
 * This test case is functional in that it attempts to validate the entire
 * lifecycle of a workflow.  This is also a good resource for beginners
 * to OSWorkflow.
 *
 * @author Eric Pugh (epugh@upstate.com)
 */
public class MemoryBasePropertySetTest extends BasePropertySetTest {
    //~ Constructors ///////////////////////////////////////////////////////////

    public MemoryBasePropertySetTest(String s) {
        super(s);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public String getType() {
        return "memory";
    }
}
