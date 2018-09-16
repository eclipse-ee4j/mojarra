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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sun.faces.config.DigesterFactory;
import com.sun.faces.config.DigesterFactory.VersionListener;


/**
 * <p>Configuration bean for <code>&lt;application&gt;</code> element.</p>
 */

// IMPLEMENTATION NOTE:  It is necessary to collect the class names of the
// sub-elements representing pluggable components, so we can chain them together
// if the implementation classes have appropriate constructors.

public class ApplicationBean {


    // -------------------------------------------------------------- Properties


    private LocaleConfigBean localeConfig;
    public LocaleConfigBean getLocaleConfig() { return localeConfig; }
    public void setLocaleConfig(LocaleConfigBean localeConfig)
    { this.localeConfig = localeConfig; }


    private String messageBundle;
    public String getMessageBundle() { return messageBundle; }
    public void setMessageBundle(String messageBundle)
    { this.messageBundle = messageBundle; }

    private String defaultRenderKitId;
    public String getDefaultRenderKitId() { return defaultRenderKitId; }
    public void setDefaultRenderKitId(String defaultRenderKitId)
    { this.defaultRenderKitId = defaultRenderKitId; }

    // -------------------------------------------- ActionListenerHolder Methods


    private List<String> actionListeners = new ArrayList<String>();


    public void addActionListener(String actionListener) {
        if (!actionListeners.contains(actionListener)) {
            VersionListener listener = DigesterFactory.getVersionListener();
            if (null != listener) {
                listener.takeActionOnArtifact(actionListener);
            }
            actionListeners.add(actionListener);
        }
    }


    public String[] getActionListeners() {
        String results[] = new String[actionListeners.size()];
        return (actionListeners.toArray(results));
    }


    public void removeActionListener(String actionListener) {
        actionListeners.remove(actionListener);
    }


    // ----------------------------------------- NavigationHandlerHolder Methods


    private List<String> navigationHandlers = new ArrayList<String>();


    public void addNavigationHandler(String navigationHandler) {
        if (!navigationHandlers.contains(navigationHandler)) {
            VersionListener listener = DigesterFactory.getVersionListener();
            if (null != listener) {
                listener.takeActionOnArtifact(navigationHandler);
            }
            navigationHandlers.add(navigationHandler);
        }
    }


    public String[] getNavigationHandlers() {
        String results[] = new String[navigationHandlers.size()];
        return (navigationHandlers.toArray(results));
    }


    public void removeNavigationHandler(String navigationHandler) {
        navigationHandlers.remove(navigationHandler);
    }


    // ------------------------------------------ PropertyResolverHolder Methods


    private List<String> propertyResolvers = new ArrayList<String>();


    public void addPropertyResolver(String propertyResolver) {
        if (!propertyResolvers.contains(propertyResolver)) {
            VersionListener listener = DigesterFactory.getVersionListener();
            if (null != listener) {
                listener.takeActionOnArtifact(propertyResolver);
            }
            propertyResolvers.add(propertyResolver);
        }
    }


    public String[] getPropertyResolvers() {
        String results[] = new String[propertyResolvers.size()];
        return (propertyResolvers.toArray(results));
    }


    public void removePropertyResolver(String propertyResolver) {
        propertyResolvers.remove(propertyResolver);
    }

    // ---------------------------------------------- StateManagerHolder Methods

    private Map<String,ResourceBundleBean> resourceBundles = new TreeMap<String, ResourceBundleBean>();


    public void addResourceBundle(ResourceBundleBean descriptor) {
        resourceBundles.put(descriptor.getVar(), descriptor);
    }

    public ResourceBundleBean getResourceBundle(String name) {
        return (resourceBundles.get(name));
    }


    public ResourceBundleBean[] getResourceBundles() {
        ResourceBundleBean results[] =
            new ResourceBundleBean[resourceBundles.size()];
        return (resourceBundles.values().toArray(results));
    }

    public void clearResourceBundles() {
        resourceBundles.clear();
    }

    public void removeResourceBundle(ResourceBundleBean descriptor) {
        resourceBundles.remove(descriptor.getVar());
    }

    // ---------------------------------------------- StateManagerHolder Methods


    private List<String> stateManagers = new ArrayList<String>();


    public void addStateManager(String stateManager) {
        if (!stateManagers.contains(stateManager)) {
            VersionListener listener = DigesterFactory.getVersionListener();
            if (null != listener) {
                listener.takeActionOnArtifact(stateManager);
            }
            stateManagers.add(stateManager);
        }
    }


    public String[] getStateManagers() {
        String results[] = new String[stateManagers.size()];
        return (stateManagers.toArray(results));
    }


    public void removeStateManager(String stateManager) {
        stateManagers.remove(stateManager);
    }


    // ------------------------------------------ VariableResolverHolder Methods


    private List<String> variableResolvers = new ArrayList<String>();


    public void addVariableResolver(String variableResolver) {
        if (!variableResolvers.contains(variableResolver)) {
            VersionListener listener = DigesterFactory.getVersionListener();
            if (null != listener) {
                listener.takeActionOnArtifact(variableResolver);
            }
            variableResolvers.add(variableResolver);
        }
    }


    public String[] getVariableResolvers() {
        String results[] = new String[variableResolvers.size()];
        return (variableResolvers.toArray(results));
    }


    public void removeVariableResolver(String variableResolver) {
        variableResolvers.remove(variableResolver);
    }

    // ------------------------------------------ ELResolver Holder Methods


    private List<String> elResolvers = new ArrayList<String>();


    public void addELResolver(String elResolver) {
        if (!elResolvers.contains(elResolver)) {
            VersionListener listener = DigesterFactory.getVersionListener();
            if (null != listener) {
                listener.takeActionOnArtifact(elResolver);
            }
            elResolvers.add(elResolver);
        }
    }


    public String[] getELResolvers() {
        String results[] = new String[elResolvers.size()];
        return (elResolvers.toArray(results));
    }


    public void removeELResolver(String elResolver) {
        elResolvers.remove(elResolver);
    }


    // ------------------------------------------ ViewHandlerHolder Methods


    private List<String> viewHandlers = new ArrayList<String>();


    public void addViewHandler(String viewHandler) {
        if (!viewHandlers.contains(viewHandler)) {
            VersionListener listener = DigesterFactory.getVersionListener();
            if (null != listener) {
                listener.takeActionOnArtifact(viewHandler);
            }
            viewHandlers.add(viewHandler);
        }
    }


    public String[] getViewHandlers() {
        String results[] = new String[viewHandlers.size()];
        return (viewHandlers.toArray(results));
    }


    public void removeViewHandler(String viewHandler) {
        viewHandlers.remove(viewHandler);
    }


    // ----------------------------------------------------------------- Methods


}
