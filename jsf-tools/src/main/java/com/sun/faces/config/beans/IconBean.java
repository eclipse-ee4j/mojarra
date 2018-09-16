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
 * <p>Configuration bean for <code>&lt;icon&gt;</code> element.</p>
 */

public class IconBean {


    // -------------------------------------------------------------- Properties


    private String lang;
    public String getLang() { return lang; }
    public void setLang(String lang)
    { this.lang = lang; }


    private String largeIcon;
    public String getLargeIcon() { return largeIcon; }
    public void setLargeIcon(String largeIcon)
    { this.largeIcon = largeIcon; }


    private String smallIcon;
    public String getSmallIcon() { return smallIcon; }
    public void setSmallIcon(String smallIcon)
    { this.smallIcon = smallIcon; }


}
