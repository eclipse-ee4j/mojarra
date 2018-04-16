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
import com.sun.faces.lifecycle.LifecycleImpl;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.*;

/**
 * The managed bean for the constructor tests.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@ManagedBean(name = "constructorBean")
@RequestScoped
public class ConstructorBean implements Serializable {

    public String getConstructorResult1() {
        try {
            FacesContextImpl context = new FacesContextImpl(null, null);
            fail();
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        }
        return "PASSED";
    }

    public String getConstructorResult2() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());

            FacesContextImpl context = new FacesContextImpl(externalContext, null);
            fail();
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        }
        return "PASSED";
    }

    public String getConstructorResult3() {
        try {
            LifecycleImpl lifecycle = new LifecycleImpl();
            FacesContextImpl context = new FacesContextImpl(null, lifecycle);
            fail();
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        }
        return "PASSED";
    }

    public String getConstructorResult4() {
        try {
            FacesContext currentContext = FacesContext.getCurrentInstance();
            ExternalContextImpl externalContext =
                    new ExternalContextImpl(
                    (ServletContext) currentContext.getExternalContext().getContext(),
                    (HttpServletRequest) currentContext.getExternalContext().getRequest(),
                    (HttpServletResponse) currentContext.getExternalContext().getResponse());
            LifecycleImpl lifecycle = new LifecycleImpl();

            FacesContextImpl context = new FacesContextImpl(externalContext, lifecycle);
        } catch (NullPointerException exception) {
            exception.printStackTrace();
            fail();
        }

        return "PASSED";
    }
}
