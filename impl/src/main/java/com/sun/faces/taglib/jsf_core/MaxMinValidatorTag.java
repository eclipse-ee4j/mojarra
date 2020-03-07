/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

// MaxMinValidatorTag.java

package com.sun.faces.taglib.jsf_core;

/**
 * <B>MaxMinValidatorTag</B> contains ivars for maximumSet and minimumSet.
 * <p/>
 * <B>Lifetime And Scope</B>
 * <P>
 *
 */

public abstract class MaxMinValidatorTag extends AbstractValidatorTag {

    private static final long serialVersionUID = 5666097564448276941L;

    /**
     * <p>
     * Flag indicating whether a maximum limit has been set.
     * </p>
     */
    protected boolean maximumSet = false;

    /**
     * <p>
     * Flag indicating whether a minimum limit has been set.
     * </p>
     */
    protected boolean minimumSet = false;

} // end of class MaxMinValidatorTag
