/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.module.propertyset.hibernate;

import java.util.Collection;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
 */
public interface HibernatePropertySetDAO {
    //~ Methods ////////////////////////////////////////////////////////////////

    void setImpl(PropertySetItem item);

    Collection getKeys(String entityName, Long entityId, String prefix, int type);

    PropertySetItem findByKey(String entityName, Long entityId, String key);

    void remove(String entityName, Long entityId, String key);
}
