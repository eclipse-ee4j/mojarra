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

package com.sun.faces.application.resource;

import static com.sun.faces.RIConstants.FLOW_IN_JAR_PREFIX;
import static com.sun.faces.config.WebConfiguration.META_INF_CONTRACTS_DIR;
import static com.sun.faces.util.Util.ensureLeadingSlash;
import static jakarta.faces.application.ResourceVisitOption.TOP_LEVEL_VIEWS_ONLY;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.util.Util;

import jakarta.enterprise.inject.Any;
import jakarta.faces.FacesException;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.annotation.View;
import jakarta.faces.application.ResourceVisitOption;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.Flow;

public class FaceletWebappResourceHelper extends ResourceHelper {

    private static final String[] RESTRICTED_DIRECTORIES = { "/WEB-INF/", "/META-INF/" };

    private final ResourceHelper webappResourceHelper;
    private final String[] configuredExtensions;

    public FaceletWebappResourceHelper(WebappResourceHelper webappResourceHelper) {
        this.webappResourceHelper = webappResourceHelper;
        FacesContext context = FacesContext.getCurrentInstance();
        configuredExtensions = new String[] { ContextParam.FACELETS_SUFFIX.getValue(context) };
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FaceletWebappResourceHelper;
    }

    @Override
    public int hashCode() {
        return 3;
    }

    @Override
    public LibraryInfo findLibrary(String libraryName, String localePrefix, String contract, FacesContext ctx) {
        // FCAPUTO libraries are handled by WebappResourceHelper
        return null;
    }

    @Override
    public ResourceInfo findResource(LibraryInfo library, String resourceName, String localePrefix, boolean compressable, FacesContext ctx) {
        if (localePrefix != null) {
            // FCAPUTO localized facelets are not yet allowed
            return null;
        }

        FaceletResourceInfo result = null;
        try {

            List<String> contracts = ctx.getResourceLibraryContracts();
            ContractInfo[] outContract = new ContractInfo[1];
            boolean[] outDoNotCache = new boolean[1];

            URL url = null;

            // if the library is not null, we must not consider contracts here!
            if (library == null && !contracts.isEmpty()) {
                url = findResourceInfoConsideringContracts(ctx, resourceName, outContract, contracts);
            }

            if (url == null) {
                url = Resource.getResourceUrl(ctx, createPath(library, resourceName));
            }

            if (url == null) {
                url = findResourceUrlConsideringFlows(resourceName, outDoNotCache);
            }

            if (url != null) {
                result = new FaceletResourceInfo(outContract[0], resourceName, null, this, url);
                result.setDoNotCache(outDoNotCache[0]);
            }
        } catch (IOException ex) {
            throw new FacesException(ex);
        }

        return result;
    }

    public Stream<String> getViewResources(FacesContext facesContext, String path, int maxDepth, ResourceVisitOption... options) {
        Stream<String> physicalViewResources = stream(spliteratorUnknownSize(
                new ResourcePathsIterator(path, maxDepth, configuredExtensions, getRestrictedDirectories(options), facesContext.getExternalContext()),
                DISTINCT), false);
        Stream<String> programmaticViewResources = Util.getCdiBeanManager(facesContext)
                .getBeans(Object.class, Any.Literal.INSTANCE).stream()
                .map(bean -> bean.getBeanClass().getAnnotation(View.class))
                .filter(Objects::nonNull)
                .map(View::value);

        return Stream.concat(physicalViewResources, programmaticViewResources);
    }

    private static String[] getRestrictedDirectories(final ResourceVisitOption... options) {
        for (ResourceVisitOption option : options) {
            if (option == TOP_LEVEL_VIEWS_ONLY) {
                return RESTRICTED_DIRECTORIES;
            }
        }

        return null;
    }

    private String createPath(LibraryInfo library, String resourceName) {
        String path = resourceName;
        if (library != null) {
            path = library.getPath() + "/" + resourceName;
        } else {
            // prepend the leading '/' if necessary.
            if (path.charAt(0) != '/') {
                path = "/" + path;
            }
        }

        return path;
    }

    private URL findResourceInfoConsideringContracts(FacesContext ctx, String baseResourceName, ContractInfo[] outContract, List<String> contracts)
            throws MalformedURLException {
        URL url = null;
        String resourceName;

        for (String contract : contracts) {
            if (baseResourceName.startsWith("/")) {
                resourceName = getBaseContractsPath() + "/" + contract + baseResourceName;
            } else {
                resourceName = getBaseContractsPath() + "/" + contract + "/" + baseResourceName;
            }

            url = Resource.getResourceUrl(ctx, resourceName);

            if (url != null) {
                outContract[0] = new ContractInfo(contract);
                break;
            } else {
                if (baseResourceName.startsWith("/")) {
                    resourceName = META_INF_CONTRACTS_DIR + "/" + contract + baseResourceName;
                } else {
                    resourceName = META_INF_CONTRACTS_DIR + "/" + contract + "/" + baseResourceName;
                }
                url = Util.getCurrentLoader(this).getResource(resourceName);
                if (url != null) {
                    outContract[0] = new ContractInfo(contract);
                    break;
                }
            }

        }

        return url;
    }

    private URL findResourceUrlConsideringFlows(String resourceName, boolean[] outDoNotCache) throws IOException {

        URL url = null;

        ClassLoader cl = Util.getCurrentLoader(this);
        Enumeration<URL> matches = cl.getResources(FLOW_IN_JAR_PREFIX + resourceName);
        try {
            url = matches.nextElement();
        } catch (NoSuchElementException nsee) {
            url = null;
        }

        if (url != null && matches.hasMoreElements()) {
            boolean keepGoing = true;
            FacesContext context = FacesContext.getCurrentInstance();
            Flow currentFlow = context.getApplication().getFlowHandler().getCurrentFlow(context);

            do {
                if (currentFlow != null && 0 < currentFlow.getDefiningDocumentId().length()) {
                    String definingDocumentId = currentFlow.getDefiningDocumentId();
                    ExternalContext extContext = context.getExternalContext();
                    ApplicationAssociate associate = ApplicationAssociate.getInstance(extContext);
                    if (associate.urlIsRelatedToDefiningDocumentInJar(url, definingDocumentId)) {
                        keepGoing = false;
                        outDoNotCache[0] = true;
                    } else {
                        if (matches.hasMoreElements()) {
                            url = matches.nextElement();
                        } else {
                            keepGoing = false;
                        }
                    }
                } else {
                    keepGoing = false;
                }
            } while (keepGoing);
        }

        return url;
    }

    @Override
    public String getBaseResourcePath() {
        return "";
    }

    @Override
    public String getBaseContractsPath() {
        return webappResourceHelper.getBaseContractsPath();
    }

    @Override
    protected InputStream getNonCompressedInputStream(ResourceInfo info, FacesContext ctx) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URL getURL(ResourceInfo resource, FacesContext ctx) {
        return ((FaceletResourceInfo) resource).getUrl();
    }

}
