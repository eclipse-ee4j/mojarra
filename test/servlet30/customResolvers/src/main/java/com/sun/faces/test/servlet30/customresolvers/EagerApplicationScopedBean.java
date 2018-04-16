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

package com.sun.faces.test.servlet30.customresolvers;

import javax.annotation.PostConstruct;
import javax.el.ELResolver;
import javax.faces.application.Application;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

@ManagedBean(eager=true)
@ApplicationScoped
public class EagerApplicationScopedBean {

    @PostConstruct
    public void installProgrammaticListener() {
        Application app = null;
        FacesContext context = FacesContext.getCurrentInstance();
        app = context.getApplication();
        VariableResolver oldVr = app.getVariableResolver();
        VariableResolver newVr = new NewVariableResolver(oldVr, context);
        app.setVariableResolver(newVr);
        ELResolver newER = new NewELResolver(context);
        app.addELResolver(newER);

    }

}
