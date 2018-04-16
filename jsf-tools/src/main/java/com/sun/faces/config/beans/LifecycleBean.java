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

package com.sun.faces.config.beans;


import com.sun.faces.config.DigesterFactory;
import com.sun.faces.config.DigesterFactory.VersionListener;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Configuration bean for <code>&lt;lifecycle&gt; element.</p>
 */

public class LifecycleBean {


    // -------------------------------------------------------------- Properties


    // --------------------------------------------- PhaseListenerHolder Methods


    private List<String> phaseListeners = new ArrayList<String>();


    public void addPhaseListener(String phaseListener) {
        if (!phaseListeners.contains(phaseListener)) {
            VersionListener listener = DigesterFactory.getVersionListener();
            if (null != listener) {
                listener.takeActionOnArtifact(phaseListener);
            }
            phaseListeners.add(phaseListener);
        }
    }


    public String[] getPhaseListeners() {
        String results[] = new String[phaseListeners.size()];
        return (phaseListeners.toArray(results));
    }


    public void removePhaseListener(String phaseListener) {
        phaseListeners.remove(phaseListener);
    }


    // ----------------------------------------------------------------- Methods




}
