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

package com.sun.faces.test.servlet30.wcagdatatable;

/**
 *
 * @author edburns
 */
public class WindowsCodePageDataBean {

    /** Creates a new instance of WcagTableData */
    public WindowsCodePageDataBean() {
    }

    public WindowsCodePageDataBean(String codePageId, String name, boolean acp, boolean oemcp, boolean winNT31, boolean winNT351,
            boolean win95) {
        this.codePageId = codePageId;
        this.name = name;
        this.ACP = acp;
        this.OEMCP = oemcp;
        this.winNT31 = winNT31;
        this.winNT351 = winNT351;
        this.win95 = win95;
    }

    /**
     * Holds value of property codePageId.
     */
    private String codePageId;

    /**
     * Getter for property codePageId.
     * 
     * @return Value of property codePageId.
     */
    public String getCodePageId() {
        return this.codePageId;
    }

    /**
     * Setter for property codePageId.
     * 
     * @param codePageId New value of property codePageId.
     */
    public void setCodePageId(String codePageId) {
        this.codePageId = codePageId;
    }

    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Getter for property name.
     * 
     * @return Value of property name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for property name.
     * 
     * @param name New value of property name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Holds value of property isOEMCP.
     */
    private String isOEMCP;

    /**
     * Holds value of property ACP.
     */
    private boolean ACP;

    /**
     * Getter for property ACP.
     * 
     * @return Value of property ACP.
     */
    public boolean isACP() {
        return this.ACP;
    }

    /**
     * Setter for property ACP.
     * 
     * @param ACP New value of property ACP.
     */
    public void setACP(boolean ACP) {
        this.ACP = ACP;
    }

    /**
     * Holds value of property OEMCP.
     */
    private boolean OEMCP;

    /**
     * Getter for property OEMCP.
     * 
     * @return Value of property OEMCP.
     */
    public boolean isOEMCP() {
        return this.OEMCP;
    }

    /**
     * Setter for property OEMCP.
     * 
     * @param OEMCP New value of property OEMCP.
     */
    public void setOEMCP(boolean OEMCP) {
        this.OEMCP = OEMCP;
    }

    /**
     * Holds value of property winNT31.
     */
    private boolean winNT31;

    /**
     * Getter for property winNT31.
     * 
     * @return Value of property winNT31.
     */
    public boolean isWinNT31() {
        return this.winNT31;
    }

    /**
     * Setter for property winNT31.
     * 
     * @param winNT31 New value of property winNT31.
     */
    public void setWinNT31(boolean winNT31) {
        this.winNT31 = winNT31;
    }

    /**
     * Holds value of property winNT351.
     */
    private boolean winNT351;

    /**
     * Getter for property winNT351.
     * 
     * @return Value of property winNT351.
     */
    public boolean isWinNT351() {
        return this.winNT351;
    }

    /**
     * Setter for property winNT351.
     * 
     * @param winNT351 New value of property winNT351.
     */
    public void setWinNT351(boolean winNT351) {
        this.winNT351 = winNT351;
    }

    /**
     * Holds value of property win95.
     */
    private boolean win95;

    /**
     * Getter for property win95.
     * 
     * @return Value of property win95.
     */
    public boolean isWin95() {
        return this.win95;
    }

    /**
     * Setter for property win95.
     * 
     * @param win95 New value of property win95.
     */
    public void setWin95(boolean win95) {
        this.win95 = win95;
    }

}
