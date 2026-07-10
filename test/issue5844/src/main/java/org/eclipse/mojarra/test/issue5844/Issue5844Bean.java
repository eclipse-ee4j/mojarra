/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue5844;

import java.io.Serializable;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class Issue5844Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private Issue5844Latch latch;

    private String param;

    public String getParam() {
        return param;
    }

    /**
     * Only wave A has an f:viewParam, so only wave A gets here. By now ViewScopeContextManager#createBean() has already
     * registered this request's view map in the session, and this bean holds the value which must survive until render.
     * So this is exactly where wave A must wait for wave B to come and evict it.
     */
    public void setParam(String param) {
        this.param = param;

        if (param != null) {
            latch.awaitWaveB();
        }
    }
}
