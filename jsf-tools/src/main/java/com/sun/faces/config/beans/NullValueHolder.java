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
 * <p>Interface denoting a configuration bean that stores a
 * <code>nullValue</code> property.
 */

public interface NullValueHolder {


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return a flag indicating that the value of the parent
     * compoennt should be set to <code>null</code>.</p>
     */
    public boolean isNullValue();


    /**
     * <p>Set a flag indicating that the value of the parent
     * component should be set to <code>null</code>.</p>
     *
     * @param nullValue The new null value flag
     */
    public void setNullValue(boolean nullValue);


}
