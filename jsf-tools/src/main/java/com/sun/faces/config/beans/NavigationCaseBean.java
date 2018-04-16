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
 * <p>Configuration bean for <code>&lt;navigation-case&gt; element.</p>
 */

public class NavigationCaseBean extends FeatureBean {


    // -------------------------------------------------------------- Properties


    private String fromAction = null;
    public String getFromAction() { return fromAction; }
    public void setFromAction(String fromAction)
    { this.fromAction = fromAction; }


    private String fromOutcome = null;
    public String getFromOutcome() { return fromOutcome; }
    public void setFromOutcome(String fromOutcome)
    { this.fromOutcome = fromOutcome; }


    private boolean redirect = false;
    public boolean isRedirect() { return redirect; }
    public void setRedirect(boolean redirect) { this.redirect = redirect; }
    public void setRedirectTrue(String dummy) { this.redirect = true; }

    private String toViewId = "*";
    public String getToViewId() { return toViewId; }
    public void setToViewId(String toViewId)
    { this.toViewId = toViewId; }


    // -------------------------------------------------------------- Extensions


    // ----------------------------------------------------------------- Methods


}
