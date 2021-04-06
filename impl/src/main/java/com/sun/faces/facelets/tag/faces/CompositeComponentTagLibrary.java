/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.tag.faces;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableMissingResourceLibraryDetection;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.facelets.tag.composite.CompositeLibrary;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

public class CompositeComponentTagLibrary extends LazyTagLibrary {

    private static final Logger LOGGER = FacesLogger.FACELETS_COMPONENT.getLogger();

    public CompositeComponentTagLibrary(String ns) {
        super(ns);
        if (null == ns) {
            throw new NullPointerException();
        }
        this.ns = ns;
        init();
    }

    public CompositeComponentTagLibrary(String ns, String compositeLibraryName) {
        super(ns);
        if (null == ns) {
            throw new NullPointerException();
        }
        this.ns = ns;
        if (null == compositeLibraryName) {
            throw new NullPointerException();
        }
        this.compositeLibraryName = compositeLibraryName;
        init();

    }

    private void init() {
        WebConfiguration webconfig = WebConfiguration.getInstance();
        enableMissingResourceLibraryDetection = webconfig.isOptionEnabled(EnableMissingResourceLibraryDetection);
    }

    private String ns = null;
    private String compositeLibraryName;
    private boolean enableMissingResourceLibraryDetection;

    @Override
    public boolean containsTagHandler(String ns, String localName) {
        boolean result = false;

        Resource ccResource = null;

        if (null != (ccResource = getCompositeComponentResource(ns, localName))) {

            try (InputStream componentStream = ccResource.getInputStream();) {
                result = componentStream != null;
            } catch (IOException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, ex.toString(), ex);
                }
            }
        }
        return result || super.containsTagHandler(ns, localName);
    }

    private Resource getCompositeComponentResource(String ns, String localName) {
        Resource ccResource = null;
        if (ns.equals(this.ns)) {
            FacesContext context = FacesContext.getCurrentInstance();
            String libraryName = getCompositeComponentLibraryName(this.ns);
            if (null != libraryName) {
                String ccName = localName + ".xhtml";
                // PENDING: there has to be a cheaper way to test for existence
                ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
                ccResource = resourceHandler.createResource(ccName, libraryName);
            }
        }
        return ccResource;
    }

    @Override
    public TagHandler createTagHandler(String ns, String localName, TagConfig tag) throws FacesException {

        TagHandler result = super.createTagHandler(ns, localName, tag);

        if (result == null) {
            ComponentConfig componentConfig = new ComponentConfigWrapper(tag, CompositeComponentImpl.TYPE, null);
            result = new CompositeComponentTagHandler(getCompositeComponentResource(ns, localName), componentConfig);
        }

        return result;
    }

    @Override
    public boolean tagLibraryForNSExists(String toTest) {
        boolean result = false;

        String resourceId = null;
        if (null != (resourceId = getCompositeComponentLibraryName(toTest))) {
            if (enableMissingResourceLibraryDetection) {
                result = FacesContext.getCurrentInstance().getApplication().getResourceHandler().libraryExists(resourceId);
            } else {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Skipping call to libraryExists().  Please set context-param {0} to true to verify if library {1} actually exists",
                            new Object[] { EnableMissingResourceLibraryDetection.getQualifiedName(), toTest });
                }
                result = true;
            }
        }

        return result;
    }

    public static boolean scriptComponentForResourceExists(FacesContext context, Resource componentResource) {
        boolean result = false;

        Resource scriptComponentResource = context.getApplication().getViewHandler().getViewDeclarationLanguage(context, context.getViewRoot().getViewId())
                .getScriptComponentResource(context, componentResource);
        InputStream is = null;
        try {
            is = scriptComponentResource.getInputStream();
            result = null != scriptComponentResource && null != is;
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, ex.toString(), ex);
                }
            }
        }

        return result;
    }

    private String getCompositeComponentLibraryName(String toTest) {
        String resourceId = null;
        if (null != compositeLibraryName) {
            resourceId = compositeLibraryName;
        } else {
            int resourceIdIndex;
            for (String namespace : CompositeLibrary.NAMESPACES) {
                String prefix = namespace + "/";
                if (-1 != (resourceIdIndex = toTest.indexOf(prefix))) {
                    resourceIdIndex += prefix.length();
                    if (resourceIdIndex < toTest.length()) {
                        resourceId = toTest.substring(resourceIdIndex);
                        break;
                    }
                }
            }
        }

        return resourceId;
    }

}
