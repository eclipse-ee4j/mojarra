/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2005-2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.faces.application.resource;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * @author Roland Huss
 *
 */
public final class Resource {

    private static final Logger LOGGER = FacesLogger.FACELETS_FACTORY.getLogger();

    /**
     * Get an URL of an internal resource.
     *
     * <p>
     * First, {@link jakarta.faces.context.ExternalContext#getResource(String)} is checked for an non-null URL return value.
     * In the case of a null return value (as it is the case for Weblogic 8.1 for a packed war), a URL with a special URL
     * handler is constructed, which can be used for <em>opening</em> a servlet resource later.
     *
     * <p>
     * Internally, this special URL handler will call {@link ServletContext#getResourceAsStream(String)} when an inputstream
     * is requested. This even works on Weblogic 8.1
     *
     * @param ctx the faces context from which to retrieve the resource
     * @param path an URL path
     *
     * @return an url representing the URL and on which getInputStream() can be called to get the resource
     * @throws MalformedURLException
     */
    static URL getResourceUrl(FacesContext ctx, String path) throws MalformedURLException {
        ExternalContext externalContext = ctx.getExternalContext();
        URL url = externalContext.getResource(path);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine("Resource-Url from external context: " + url);
        }

        // This might happen on a Servlet container which does not return anything
        // for getResource() (like weblogic 8.1 for packaged wars) we are trying
        // to use an own URL protocol in order to use ServletContext.getResourceAsStream()
        // when opening the url
        if (url == null && resourceExist(externalContext, path)) {
            url = getUrlForResourceAsStream(externalContext, path);
        }

        return url;
    }

    static Set<String> getViewResourcePaths(FacesContext ctx, String path) {
        return ctx.getExternalContext().getResourcePaths(path);
    }

    // This method could be used above to provide a 'fail fast' if a resource
    // doesn't exist. Otherwise, the URL will fail on the first access.
    private static boolean resourceExist(ExternalContext externalContext, String path) {
        if ("/".equals(path)) {
            // The root context exists always
            return true;
        }

        Object ctx = externalContext.getContext();
        if (ctx instanceof ServletContext) {
            ServletContext servletContext = (ServletContext) ctx;
            InputStream stream = servletContext.getResourceAsStream(path);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    if (LOGGER.isLoggable(FINEST)) {
                        LOGGER.log(FINEST, "Closing stream", e);
                    }
                }
                return true;
            }
        }

        return false;
    }

    // Construct URL with special URLStreamHandler for proxying
    // ServletContext.getResourceAsStream()
    private static URL getUrlForResourceAsStream(final ExternalContext externalContext, String path) throws MalformedURLException {
        URLStreamHandler handler = new URLStreamHandler() {

            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                final String file = u.getFile();
                return new URLConnection(u) {

                    @Override
                    public void connect() throws IOException {
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        if (LOGGER.isLoggable(FINE)) {
                            LOGGER.fine("Opening internal url to " + file);
                        }
                        Object ctx = externalContext.getContext();
                        // Or maybe fetch the external context afresh ?
                        // Object ctx =
                        // FacesContext.getCurrentInstance().getExternalContext().getContext();

                        if (ctx instanceof ServletContext) {
                            ServletContext servletContext = (ServletContext) ctx;
                            InputStream stream = servletContext.getResourceAsStream(file);
                            if (stream == null) {
                                throw new FileNotFoundException("Cannot open resource " + file);
                            }
                            return stream;
                        } else {
                            throw new IOException("Cannot open resource for an context of " + (ctx != null ? ctx.getClass() : null));
                        }
                    }
                };
            }
        };

        return new URL("internal", null, 0, path, handler);
    }
}
