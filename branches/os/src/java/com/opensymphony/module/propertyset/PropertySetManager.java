/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset;

import com.opensymphony.module.propertyset.config.PropertySetConfig;

import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public class PropertySetManager {
    //~ Methods ////////////////////////////////////////////////////////////////

    public static PropertySet getInstance(String name, Map args) {
        PropertySet ps = getInstance(name, args, PropertySetManager.class.getClassLoader());

        if (ps == null) {
            ps = getInstance(name, args, Thread.currentThread().getContextClassLoader());
        }

        return ps;
    }

    public static PropertySet getInstance(String name, Map args, ClassLoader loader) {
        PropertySetConfig psc = PropertySetConfig.getConfig();
        String clazz = psc.getClassName(name);
        Map config = psc.getArgs(name);
        Class psClass = null;

        try {
            psClass = loader.loadClass(clazz);
        } catch (ClassNotFoundException ex) {
            return null;
        }

        try {
            PropertySet ps = (PropertySet) psClass.newInstance();
            ps.init(config, args);

            return ps;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void clone(PropertySet src, PropertySet dest) {
        PropertySetCloner cloner = new PropertySetCloner();
        cloner.setSource(src);
        cloner.setDestination(dest);
        cloner.cloneProperties();
    }
}
