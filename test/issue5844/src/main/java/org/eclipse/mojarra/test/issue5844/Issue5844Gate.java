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

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Sequences wave B, which deliberately has no f:viewParam: an f:viewParam would already create the @ViewScoped bean
 * during PROCESS_VALIDATIONS, when UIInput#validate() evaluates the value expression, and hence register the view map
 * before any gate bound to UPDATE_MODEL could hold it back.
 */
@Named
@RequestScoped
public class Issue5844Gate {

    @Inject
    private Issue5844Latch latch;

    /**
     * Bound to f:viewAction, so it runs in INVOKE_APPLICATION, well before render creates the @ViewScoped bean.
     */
    public void awaitWaveA() {
        latch.awaitWaveA();
    }

    /**
     * Rendered after the @ViewScoped bean, hence after this request has registered its view map and thereby evicted
     * wave A.
     */
    public String getSignal() {
        latch.signalWaveBRegistered();
        return "";
    }
}
