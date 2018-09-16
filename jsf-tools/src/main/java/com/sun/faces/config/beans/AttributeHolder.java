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
 * of attributes.</p>
 */

public interface AttributeHolder {


    // ----------------------------------------------------------------- Methods


    /**
     * <p>Add the specified attribute descriptor, replacing any existing
     * descriptor for this attribute name.</p>
     *
     * @param descriptor Descriptor to be added
     */
    public void addAttribute(AttributeBean descriptor);


    /**
     * <p>Return the attribute descriptor for the specified attribute name,
     * if any; otherwise, return <code>null</code>.</p>
     *
     * @param name Name of the attribute for which to retrieve a descriptor
     * @return the attribute descriptor
     */
    public AttributeBean getAttribute(String name);


    /**
     * <p>Return the descriptors of all attributes for which descriptors have
     * been registered, or an empty array if none have been registered.</p>
     * @return the descriptors of all attributes
     */
    public AttributeBean[] getAttributes();


    /**
     * <p>Deregister the specified attribute descriptor, if it is registered.
     * </p>
     *
     * @param descriptor Descriptor to be removed
     */
    public void removeAttribute(AttributeBean descriptor);


}
