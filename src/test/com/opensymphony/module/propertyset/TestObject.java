package com.opensymphony.module.propertyset;

import java.io.Serializable;

/**
 * User: bbulger
 * Date: May 22, 2004
 */
public class TestObject implements Serializable {

    private long id;

    public TestObject(long id) {
        this.id = id;
    }

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
