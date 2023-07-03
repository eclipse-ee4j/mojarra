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

package com.sun.faces.config.manager.tasks;

import static java.util.Collections.emptyList;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

import com.sun.faces.spi.ConfigurationResourceProvider;

import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>Callable</code> will be used by <code>getXMLDocuments</code>
 * It represents one or more URIs to configuration resources that require processing.
 * </p>
 */
public class FindConfigResourceURIsTask implements Callable<Collection<URI>> {

    private final ConfigurationResourceProvider provider;
    private final ServletContext servletContext;

    // -------------------------------------------------------- Constructors

    /**
     * Constructs a new <code>URITask</code> instance.
     *
     * @param provider the <code>ConfigurationResourceProvider</code> from which zero or more <code>URL</code>s will be
     * returned
     * @param servletContext the <code>ServletContext</code> of the current application
     */
    public FindConfigResourceURIsTask(ConfigurationResourceProvider provider, ServletContext servletContext) {
        this.provider = provider;
        this.servletContext = servletContext;
    }

    // ----------------------------------------------- Methods from Callable

    /**
     * @return zero or more <code>URL</code> instances
     * @throws Exception if an Exception is thrown by the underlying <code>ConfigurationResourceProvider</code>
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<URI> call() throws Exception {
        Collection<URI> untypedCollection = provider.getResources(servletContext);
        Iterator<?> untypedCollectionIterator = untypedCollection.iterator();

        Collection<URI> result = emptyList();

        if (untypedCollectionIterator.hasNext()) {
            Object cur = untypedCollectionIterator.next();

            // Account for older versions of the provider that return Collection<URL>.
            if (cur instanceof URL) {
                result = new ArrayList<>(untypedCollection.size());
                result.add(new URI(((URL) cur).toExternalForm()));
                while (untypedCollectionIterator.hasNext()) {
                    cur = untypedCollectionIterator.next();
                    result.add(new URI(((URL) cur).toExternalForm()));
                }
            } else {
                result = untypedCollection;
            }
        }

        return result;
    }

}
