/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.memory;

import com.opensymphony.module.propertyset.AbstractPropertySetTest;
import com.opensymphony.module.propertyset.IllegalPropertyException;

import javax.xml.parsers.ParserConfigurationException;


/**
 * User: bbulger
 * Date: May 22, 2004
 */
public class SerializablePropertySetTest extends AbstractPropertySetTest {
    //~ Methods ////////////////////////////////////////////////////////////////

    public void testGetTypeForXml() throws ParserConfigurationException {
        try {
            super.testGetTypeForXml();
            fail("Document is not serializable.");
        } catch (IllegalPropertyException e) {
            // success
        }
    }

    public void testSetAsActualTypeGetAsActualTypeForXml() throws ParserConfigurationException {
        try {
            super.testSetAsActualTypeGetAsActualTypeForXml();
            fail("Document is not serializable.");
        } catch (IllegalPropertyException e) {
            // success
        }
    }

    public void testSetXmlGetXml() throws ParserConfigurationException {
        try {
            super.testSetXmlGetXml();
            fail("Document is not serializable.");
        } catch (IllegalPropertyException e) {
            // success
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        ps = new SerializablePropertySet();
        ps.init(null, null);
    }
}
