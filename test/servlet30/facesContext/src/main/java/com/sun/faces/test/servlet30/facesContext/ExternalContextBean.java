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

package com.sun.faces.test.servlet30.facesContext;

import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.*;

@ManagedBean(name = "extContextBean")
@RequestScoped
public class ExternalContextBean implements Serializable {

    public String getEncodeResourceURLNPE() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            externalContext.encodeResourceURL(null); 
        } catch (NullPointerException exception) {
            return "PASSED";
        }

        return "FAILED";
    }
    public String getEncodePartialActionURLNPE() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            externalContext.encodePartialActionURL(null);
        } catch (NullPointerException exception) {
            return "PASSED";
        }

        return "FAILED";
    }
    public String getIsUserInRoleNPE() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            externalContext.isUserInRole(null);
        } catch (NullPointerException exception) {
            return "PASSED";
        }

        return "FAILED";
    }
    public String getLog1NPE() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            externalContext.log(null);
        } catch (NullPointerException exception) {
            return "PASSED";
        }

        return "FAILED";
    }
    public String getLog2NPE() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            externalContext.log(null, new RuntimeException("Exception"));
        } catch (NullPointerException exception) {
            return "PASSED";
        }

        return "FAILED";
    }
    public String getLog3NPE() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            String msg = "A message";
            externalContext.log(msg, null);
        } catch (NullPointerException exception) {
            return "PASSED";
        }

        return "FAILED";
    }
    public String getResourceNPE() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            externalContext.getResource(null);
        } catch (NullPointerException exception) {
            return "PASSED";
        }

        return "FAILED";
    }
    public String getResourceAsStreamNPE() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            externalContext.getResourceAsStream(null);
        } catch (NullPointerException exception) {
            return "PASSED";
        }

        return "FAILED";
    }
    public String getResourcePathsNPE() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            externalContext.getResourcePaths(null);
        } catch (NullPointerException exception) {
            return "PASSED";
        }

        return "FAILED";
    }

}
