/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.config.beans;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * <p>Configuration bean for <code>&lt;managed-bean&gt;</code> element.</p>
 */

public class ManagedBeanBean extends FeatureBean
    implements ListEntriesHolder, MapEntriesHolder {


    // -------------------------------------------------------------- Properties


    private String managedBeanClass;
    public String getManagedBeanClass() { return managedBeanClass; }
    public void setManagedBeanClass(String managedBeanClass)
    { this.managedBeanClass = managedBeanClass; }


    private String managedBeanName;
    public String getManagedBeanName() { return managedBeanName; }
    public void setManagedBeanName(String managedBeanName)
    { this.managedBeanName = managedBeanName; }


    private String managedBeanScope;
    public String getManagedBeanScope() { return managedBeanScope; }
    public void setManagedBeanScope(String managedBeanScope)
    { this.managedBeanScope = managedBeanScope; }


    // -------------------------------------------------------------- Extensions


    // ----------------------------------------------- ListEntriesHolder Methods

    private ListEntriesBean listEntries;
    @Override
    public ListEntriesBean getListEntries() { return listEntries; }
    @Override
    public void setListEntries(ListEntriesBean listEntries)
    { this.listEntries = listEntries; }


    // ------------------------------------------- ManagedPropertyHolder Methods


    private List<ManagedPropertyBean> managedProperties = new ArrayList<ManagedPropertyBean>();


    public void addManagedProperty(ManagedPropertyBean descriptor) {
        managedProperties.add(descriptor);
    }


    public ManagedPropertyBean getManagedProperty(String name) {
	Iterator<ManagedPropertyBean> iter = managedProperties.iterator();
	ManagedPropertyBean cur = null;
	String  curName = null;
	while (iter.hasNext()) {
	    cur = iter.next();
	    if (null == cur) {
		continue;
	    }
	    curName = cur.getPropertyName();
	    // if the name is null, and we're looking for null
	    if (null == curName && null == name) {
		return cur;
	    }
	    // not a match
	    if (null == curName || null == name) {
		continue;
	    }
	    // guaranteed that both are non-null
	    if (curName.equals(name)) {
		return cur;
	    }
	}

        return null;
    }


    public ManagedPropertyBean[] getManagedProperties() {
        ManagedPropertyBean results[] =
            new ManagedPropertyBean[managedProperties.size()];
        return (managedProperties.toArray(results));
    }


    public void removeManagedProperty(ManagedPropertyBean descriptor) {
	if (null == descriptor) {
	    return;
	}
	ManagedPropertyBean toRemove =
	    getManagedProperty(descriptor.getPropertyName());
	if (null != toRemove) {
	    managedProperties.remove(toRemove);
	}
    }

    // ------------------------------------------------ MapEntriesHolder Methods

    private MapEntriesBean mapEntries;
    @Override
    public MapEntriesBean getMapEntries() { return mapEntries; }
    @Override
    public void setMapEntries(MapEntriesBean mapEntries)
    { this.mapEntries = mapEntries; }



    // ----------------------------------------------------------------- Methods


}
