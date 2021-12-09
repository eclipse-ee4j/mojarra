/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Contributors to Eclipse Foundation.
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

package com.sun.faces.test.servlet40.systemevent;

import java.io.IOException;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.annotation.FacesConfig;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class PostRenderViewEventBean {

    @Inject
    private ExternalContext externalContext;

    public void pre(ComponentSystemEvent event) throws IOException {
        externalContext.getResponseOutputWriter().write("<!-- pre -->");
    }

    public String getRender() {
        return "render";
    }

    public void post(ComponentSystemEvent event) throws IOException {
        externalContext.getResponseOutputWriter().write("<!-- post -->");
    }
}
