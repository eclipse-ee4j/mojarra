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
 * <p>Configuration bean for <code>&lt;factory&gt; element.</p>
 */

public class FactoryBean {


    // -------------------------------------------------------------- Properties


    private List<String> applicationFactories = new ArrayList<String>();
    public List<String> getApplicationFactories() { return applicationFactories; }
    public void addApplicationFactory(String applicationFactory)
    { 
        VersionListener listener = DigesterFactory.getVersionListener();
        if (null != listener) {
            listener.takeActionOnArtifact(applicationFactory);
        }
        applicationFactories.add(applicationFactory); }


    private List<String> facesContextFactories = new ArrayList<String>();
    public List<String> getFacesContextFactories() { return facesContextFactories; }
    public void addFacesContextFactory(String facesContextFactory)
    { 
        VersionListener listener = DigesterFactory.getVersionListener();
        if (null != listener) {
            listener.takeActionOnArtifact(facesContextFactory);
        }
        facesContextFactories.add(facesContextFactory); }


    private List<String> lifecycleFactories = new ArrayList<String>();
    public List<String> getLifecycleFactories() { return lifecycleFactories; }
    public void addLifecycleFactory(String lifecycleFactory)
    { 
        VersionListener listener = DigesterFactory.getVersionListener();
        if (null != listener) {
            listener.takeActionOnArtifact(lifecycleFactory);
        }
        lifecycleFactories.add(lifecycleFactory); }


    private List<String> renderKitFactories = new ArrayList<String>();
    public List<String> getRenderKitFactories() { return renderKitFactories; }
    public void addRenderKitFactory(String renderKitFactory)
    { 
        VersionListener listener = DigesterFactory.getVersionListener();
            if (null != listener) {
                listener.takeActionOnArtifact(renderKitFactory);
            }
        renderKitFactories.add(renderKitFactory); }


}
