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

package com.sun.faces.config.configprovider;

import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.JakartaFacesConfigFiles;
import static com.sun.faces.util.Util.split;
import static java.util.Arrays.binarySearch;
import static java.util.logging.Level.WARNING;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.WebContextInitParameter;
import com.sun.faces.spi.ConfigurationResourceProvider;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.servlet.ServletContext;

/**
 *
 */
public abstract class BaseWebConfigResourceProvider implements ConfigurationResourceProvider {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    // ------------------------------ Methods from ConfigurationResourceProvider

    @Override
    public Collection<URI> getResources(ServletContext context) {

        WebConfiguration webConfig = WebConfiguration.getInstance(context);
        String paths = webConfig.getOptionValue(getParameter());
        Set<URI> urls = new LinkedHashSet<>(6);

        if (paths != null) {
            for (String token : split(context, paths.trim(), getSeparatorRegex())) {
                String path = token.trim();
                if (!isExcluded(path) && path.length() != 0) {
                    URI u = getContextURLForPath(context, path);
                    if (u != null) {
                        urls.add(u);
                    } else {
                        if (LOGGER.isLoggable(WARNING)) {
                            LOGGER.log(WARNING, "faces.config.web_resource_not_found", new Object[] { path, JakartaFacesConfigFiles.getQualifiedName() });
                        }
                    }
                }

            }
        }

        return urls;
    }

    // ------------------------------------------------------- Protected Methods

    protected abstract WebContextInitParameter getParameter();

    protected abstract String[] getExcludedResources();

    protected abstract String getSeparatorRegex();

    protected URI getContextURLForPath(ServletContext context, String path) {
        try {
            URL url = context.getResource(path);
            if (url != null) {
                return new URI(url.toExternalForm().replaceAll(" ", "%20"));
            }
        } catch (MalformedURLException | URISyntaxException mue) {
            throw new FacesException(mue);
        }

        return null;
    }

    protected boolean isExcluded(String path) {
        return binarySearch(getExcludedResources(), path) >= 0;
    }

}
