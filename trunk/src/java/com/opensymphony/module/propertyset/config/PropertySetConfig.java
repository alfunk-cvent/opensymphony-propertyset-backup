/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.config;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.*;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class PropertySetConfig {
    //~ Static fields/initializers /////////////////////////////////////////////

    private static PropertySetConfig config;
    private static final Object lock = new Object();

    //~ Instance fields ////////////////////////////////////////////////////////

    private HashMap propertySetArgs = new HashMap();
    private HashMap propertySets = new HashMap();

    //~ Constructors ///////////////////////////////////////////////////////////

    private PropertySetConfig() {
        InputStream is = load();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = null;

        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Document doc = null;

        try {
            doc = db.parse(is);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get propertysets
        Element root = (Element) doc.getElementsByTagName("propertysets").item(0);
        NodeList propertySets = root.getElementsByTagName("propertyset");

        for (int i = 0; i < propertySets.getLength(); i++) {
            Element propertySet = (Element) propertySets.item(i);
            String name = propertySet.getAttribute("name");
            String clazz = propertySet.getAttribute("class");
            this.propertySets.put(name, clazz);

            // get args now
            NodeList args = propertySet.getElementsByTagName("arg");
            HashMap argsMap = new HashMap();

            for (int j = 0; j < args.getLength(); j++) {
                Element arg = (Element) args.item(j);
                String argName = arg.getAttribute("name");
                String argValue = arg.getAttribute("value");
                argsMap.put(argName, argValue);
            }

            this.propertySetArgs.put(name, argsMap);
        }
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public static PropertySetConfig getConfig() {
        // check one more time, another thread may have finished
        synchronized (lock) {
            if (config == null) {
                config = new PropertySetConfig();
            }

            return config;
        }
    }

    public Map getArgs(String name) {
        return (Map) propertySetArgs.get(name);
    }

    public String getClassName(String name) {
        return (String) propertySets.get(name);
    }

    private InputStream load() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = null;

        try {
            is = classLoader.getResourceAsStream("propertyset.xml");
        } catch (Exception e) {
        }

        if (is == null) {
            try {
                is = classLoader.getResourceAsStream("/propertyset.xml");
            } catch (Exception e) {
            }
        }

        if (is == null) {
            try {
                is = classLoader.getResourceAsStream("META-INF/propertyset.xml");
            } catch (Exception e) {
            }
        }

        if (is == null) {
            try {
                is = classLoader.getResourceAsStream("/META-INF/propertyset.xml");
            } catch (Exception e) {
            }
        }

        if (is == null) {
            try {
                is = classLoader.getResourceAsStream("META-INF/propertyset-default.xml");
            } catch (Exception e) {
            }
        }

        if (is == null) {
            try {
                is = classLoader.getResourceAsStream("/META-INF/propertyset-default.xml");
            } catch (Exception e) {
            }
        }

        return is;
    }
}
