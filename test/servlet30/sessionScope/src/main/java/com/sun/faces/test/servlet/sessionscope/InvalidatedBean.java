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

package com.sun.faces.test.servlet.sessionscope;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * A SessionScoped bean testing session invalidation functionality.
 */
@Named
@SessionScoped
public class InvalidatedBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ApplicationScopedBean applicationScopedBean;

    private String text = "This is from the initialiser";

    @PostConstruct
    public void init() {
        applicationScopedBean.setCount(0);
        text = "This is from the @PostConstruct";
    }

    @PreDestroy
    public void destroy() {
        applicationScopedBean.setCount(applicationScopedBean.getCount() + 1);
    }

    public String getText() {
        return text;
    }
}
