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


/**
 * <p>Interface denoting a configuration bean that stores a named collection
 * of properties.</p>
 */

public interface PropertyHolder {


    // ----------------------------------------------------------------- Methods


    /**
     * <p>Add the specified property descriptor, replacing any existing
     * descriptor for this property name.</p>
     *
     * @param descriptor Descriptor to be added
     */
    public void addProperty(PropertyBean descriptor);


    /**
     * <p>Return the property descriptor for the specified property name,
     * if any; otherwise, return <code>null</code>.</p>
     *
     * @param name Name of the property for which to retrieve a descriptor
     * @return the property descriptor
     */
    public PropertyBean getProperty(String name);


    /**
     * <p>Return the descriptors of all properties for which descriptors have
     * been registered, or an empty array if none have been registered.</p>
     * @return the descriptors of all properties
     */
    public PropertyBean[] getProperties();


    /**
     * <p>Deregister the specified property descriptor, if it is registered.
     * </p>
     *
     * @param descriptor Descriptor to be removed
     */
    public void removeProperty(PropertyBean descriptor);


}
