/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.database;

import com.opensymphony.module.propertyset.BasePropertySetTest;
import com.opensymphony.module.propertyset.DatabaseHelper;


/**
 * This test case attempts to verify the database implementation.  This is also a good resource for beginners
 * to PropertySet.  This leverages straight JDBC as the persistence mechanism which requires
 * fewer dependencies then hibernate..
 *
 * @author Eric Pugh (epugh@upstate.com)
 */
public class JDBCPropertySetTest extends BasePropertySetTest {
    //~ Constructors ///////////////////////////////////////////////////////////

    public JDBCPropertySetTest(String s) {
        super(s);
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public String getType() {
        return "jdbc";
    }

    public void setUp() throws Exception {
        //ok so this code usually goes in the setUp but...
        DatabaseHelper.exportSchemaForJDBC();
        args.put("globalKey", "test");
        super.setUp();
    }
}
