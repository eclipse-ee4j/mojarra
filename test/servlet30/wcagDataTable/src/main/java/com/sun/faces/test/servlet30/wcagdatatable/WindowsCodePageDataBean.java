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

    private String codePageId;
    private String name;
    private String isOEMCP;
    private boolean ACP;
    private boolean OEMCP;
    private boolean winNT31;
    private boolean winNT351;
    private boolean win95;

    public WindowsCodePageDataBean() {}

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

    public String getCodePageId() {
        return this.codePageId;
    }

    public void setCodePageId(String codePageId) {
        this.codePageId = codePageId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public boolean isOEMCP() {
        return this.OEMCP;
    }

    public void setOEMCP(boolean OEMCP) {
        this.OEMCP = OEMCP;
    }

    public boolean isWinNT31() {
        return this.winNT31;
    }

    public void setWinNT31(boolean winNT31) {
        this.winNT31 = winNT31;
    }

    public boolean isWinNT351() {
        return this.winNT351;
    }

    public void setWinNT351(boolean winNT351) {
        this.winNT351 = winNT351;
    }

    public boolean isWin95() {
        return this.win95;
    }

    public void setWin95(boolean win95) {
        this.win95 = win95;
    }

}
