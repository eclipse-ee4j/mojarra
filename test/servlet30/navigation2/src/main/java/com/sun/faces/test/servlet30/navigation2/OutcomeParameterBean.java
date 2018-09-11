/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.navigation2;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.ServletContext;

/**
 * Backing Bean class that redirects on startSearch() to an other JSF page and tries to transmit the
 * content of the searchTerm property as a URL HTTP-GET parameter.
 *
 * @author deconstruct
 */
@Named
@ViewScoped
public class OutcomeParameterBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String searchTermA = "Laurel & Hardy";
    private String searchTermB = "Laurel & Hardy";
    private String searchTermC = "Laurel & Hardy";
    private String searchTermD = "Laurel & Hardy";
    private String searchTermE = "Laurel & Hardy";

    public String startSearchWithUrlEncode() throws UnsupportedEncodingException {
        String queryUrlParameter = java.net.URLEncoder.encode(searchTermA, "UTF-8");
        String redirectTarget = "/outcomeParameterResults.xhtml?query=" + queryUrlParameter
                + "&otherParameter=someValue&faces-redirect=true";
        return redirectTarget;
    }

    public String startSearchWithoutUrlEncode() throws UnsupportedEncodingException {
        String redirectTarget = "/outcomeParameterResults.xhtml?query=" + searchTermB + "&otherParameter=someValue&faces-redirect=true";
        return redirectTarget;
    }

    public void startSearchViaExternalContext() throws UnsupportedEncodingException, IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String contextPath = ((ServletContext) externalContext.getContext()).getContextPath();
        String redirectTarget = contextPath + "/faces/outcomeParameterResults.xhtml?query="
                + java.net.URLEncoder.encode(searchTermC, "UTF-8") + "&otherParameter=someValue";

        FacesContext.getCurrentInstance().getExternalContext().redirect(redirectTarget);
    }

    public String getSearchTermA() {
        return searchTermA;
    }

    public void setSearchTermA(String searchTermA) {
        this.searchTermA = searchTermA;
    }

    public String getSearchTermB() {
        return searchTermB;
    }

    public void setSearchTermB(String searchTermB) {
        this.searchTermB = searchTermB;
    }

    public String getSearchTermC() {
        return searchTermC;
    }

    public void setSearchTermC(String searchTermC) {
        this.searchTermC = searchTermC;
    }

    public String getSearchTermD() {
        return searchTermD;
    }

    public void setSearchTermD(String searchTermD) {
        this.searchTermD = searchTermD;
    }

    public String getSearchTermE() {
        return searchTermD;
    }

    public void setSearchTermE(String searchTermE) {
        this.searchTermE = searchTermE;
    }
}
