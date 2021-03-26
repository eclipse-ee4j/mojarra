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

package com.sun.faces.test.servlet30.runtimeconfig;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.PropertyResolver;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.Validator;
import java.util.Iterator;
import java.util.Locale;

import static org.junit.Assert.*;

@ManagedBean
@SessionScoped
public class ApplicationConfigBean {

    private String title = "Test Application Config";
    public String getTitle() {
        return title; 
    }

    public ApplicationConfigBean() {
    }

    private String getUpdateRuntimeComponents() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application app = fc.getApplication();

        ActionListener actionListener = null;
        NavigationHandler navHandler = null;
        PropertyResolver propResolver = null;
        VariableResolver varResolver = null;
        ViewHandler viewHandler = null;
        StateManager stateManager = null;

        actionListener = app.getActionListener();
        assertTrue(null != actionListener && actionListener instanceof com.sun.faces.test.servlet30.runtimeconfig.TestActionListener);

        navHandler = app.getNavigationHandler();
        assertTrue(null != navHandler && navHandler instanceof com.sun.faces.test.servlet30.runtimeconfig.TestNavigationHandler);

        // JSF1.2 BI: application.getPropertyResolver() no longer returns the
        // head of the PropertyResolver. Instead returns the head of the
        // ELResolver stack wrapped in a PropertyResolver.This also applies to
        // VariableResolver
        propResolver = app.getPropertyResolver();
        assertTrue(null != propResolver && propResolver instanceof javax.faces.el.PropertyResolver);

        varResolver = app.getVariableResolver();
        assertTrue(null != varResolver && varResolver instanceof javax.faces.el.VariableResolver);

        viewHandler = app.getViewHandler();
        assertTrue(null != viewHandler && viewHandler instanceof javax.faces.application.ViewHandler);

        stateManager = app.getStateManager();
        assertTrue(null != stateManager && stateManager instanceof javax.faces.application.StateManager);

/*
        if (app.getDefaultRenderKitId().equals("WackyRenderKit")) {
            status += "SUCCESS: Default RenderKitId**";
        } else {
            status += "FAIL: Default RenderKitId:"+app.getDefaultRenderKitId()+"**";
        }
*/

        return "SUCCESS";
    }

    private String status="";

    public String getStatus() {
        return status;
    }
}

