/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset;

import java.io.Serializable;


/**
 * User: bbulger
 * Date: May 22, 2004
 */
public class TestObject implements Serializable {
    //~ Instance fields ////////////////////////////////////////////////////////

    private long id;

    //~ Constructors ///////////////////////////////////////////////////////////

    public TestObject(long id) {
        this.id = id;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        TestObject testObject = (TestObject) obj;

        return id == testObject.getId();
    }

    public int hashCode() {
        return (int) (id ^ id >>> 32);
    }
}
