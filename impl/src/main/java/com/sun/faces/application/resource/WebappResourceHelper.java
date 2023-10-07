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

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.CacheResourceModificationTimestamp;
import static com.sun.faces.util.Util.ensureLeadingSlash;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * A {@link ResourceHelper} implementation for finding/serving resources found within
 * <code>&lt;contextroot&gt;/resources</code> directory of a web application.
 * </p>
 *
 * @since 2.0
 */
public class WebappResourceHelper extends ResourceHelper {

    private static final Logger LOGGER = FacesLogger.RESOURCE.getLogger();

    private final String BASE_RESOURCE_PATH;

    private final String BASE_CONTRACTS_PATH;

    private final boolean cacheTimestamp;

    // ------------------------------------------------------------ Constructors

    public WebappResourceHelper() {

        WebConfiguration webconfig = WebConfiguration.getInstance();
        FacesContext context = FacesContext.getCurrentInstance();
        cacheTimestamp = webconfig.isOptionEnabled(CacheResourceModificationTimestamp);
        BASE_RESOURCE_PATH = ensureLeadingSlash(ContextParam.WEBAPP_RESOURCES_DIRECTORY.getValue(context));
        BASE_CONTRACTS_PATH = ensureLeadingSlash(ContextParam.WEBAPP_CONTRACTS_DIRECTORY.getValue(context));

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebappResourceHelper other = (WebappResourceHelper) obj;
        if (BASE_RESOURCE_PATH == null ? other.BASE_RESOURCE_PATH != null : !BASE_RESOURCE_PATH.equals(other.BASE_RESOURCE_PATH)) {
            return false;
        }
        if (BASE_CONTRACTS_PATH == null ? other.BASE_CONTRACTS_PATH != null : !BASE_CONTRACTS_PATH.equals(other.BASE_CONTRACTS_PATH)) {
            return false;
        }
        if (cacheTimestamp != other.cacheTimestamp) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (BASE_RESOURCE_PATH != null ? BASE_RESOURCE_PATH.hashCode() : 0);
        hash = 37 * hash + (BASE_CONTRACTS_PATH != null ? BASE_CONTRACTS_PATH.hashCode() : 0);
        hash = 37 * hash + (cacheTimestamp ? 1 : 0);
        return hash;
    }

    // --------------------------------------------- Methods from ResourceHelper

    /**
     * @see com.sun.faces.application.resource.ResourceHelper#getBaseResourcePath()
     */
    @Override
    public String getBaseResourcePath() {

        return BASE_RESOURCE_PATH;

    }

    @Override
    public String getBaseContractsPath() {
        return BASE_CONTRACTS_PATH;
    }

    /**	
     * @see ResourceHelper#getNonCompressedInputStream(com.sun.faces.application.resource.ResourceInfo,
     * jakarta.faces.context.FacesContext)
     */
    @Override
    protected InputStream getNonCompressedInputStream(ResourceInfo resource, FacesContext ctx) throws IOException {
       	List<String> localizedPaths = getLocalizedPaths(resource.getPath(), ctx);
    	InputStream in = null;
    	for (String path_: localizedPaths) {
    		in = ctx.getExternalContext().getResourceAsStream(path_);
    		if (in != null) {
    			break;
    		}
    	}
    	return in;
    }

    /**
     * @see ResourceHelper#getURL(com.sun.faces.application.resource.ResourceInfo, jakarta.faces.context.FacesContext)
     */
    @Override
    public URL getURL(ResourceInfo resource, FacesContext ctx) {
        String path = resource.getPath();

        try {
            return ctx.getExternalContext().getResource(path);
        } catch (MalformedURLException e) {
            return null;
        }

    }

    /**
     * @see ResourceHelper#findLibrary(String, String, String, jakarta.faces.context.FacesContext)
     */
    @Override
    public LibraryInfo findLibrary(String libraryName, String localePrefix, String contract, FacesContext ctx) {

        String path;

        if (localePrefix == null) {
            path = getBasePath(contract) + '/' + libraryName;
        } else {
            path = getBasePath(contract) + '/' + localePrefix + '/' + libraryName;
        }
        Set<String> resourcePaths = ctx.getExternalContext().getResourcePaths(path);
        // it could be possible that there exists an empty directory
        // that is representing the library, but if it's empty, treat it
        // as non-existent and return null.
        if (resourcePaths != null && !resourcePaths.isEmpty()) {
            VersionInfo version = getVersion(resourcePaths, false);
            return new LibraryInfo(libraryName, version, localePrefix, contract, this);
        }

        return null;
    }

    /**
     * @see ResourceHelper#findResource(LibraryInfo, String, String, boolean, jakarta.faces.context.FacesContext)
     */
    @Override
    public ResourceInfo findResource(LibraryInfo library, String resourceName, String localePrefix, boolean compressable, FacesContext ctx) {

        resourceName = trimLeadingSlash(resourceName);
        ContractInfo[] outContract = new ContractInfo[1];
        outContract[0] = null;

        String basePath = findPathConsideringContracts(library, resourceName, localePrefix, outContract, ctx);

        if (null == basePath) {

            if (library != null) {
                // PENDING(fcaputo) no need to iterate over the contracts, if we have a library
                basePath = library.getPath(localePrefix) + '/' + resourceName;
            } else {
                if (localePrefix == null) {
                    basePath = getBaseResourcePath() + '/' + resourceName;
                } else {
                    basePath = getBaseResourcePath() + '/' + localePrefix + '/' + resourceName;
                }
            }

            // first check to see if the resource exists, if not, return null. Let
            // the caller decide what to do.
            try {
                if (ctx.getExternalContext().getResource(basePath) == null) {
                    return null;
                }
            } catch (MalformedURLException e) {
                throw new FacesException(e);
            }
        }

        // we got to hear, so we know the resource exists (either as a directory
        // or file)
        Set<String> resourcePaths = ctx.getExternalContext().getResourcePaths(basePath);
        // if getResourcePaths returns null or an empty set, this means that we have
        // a non-directory resource, therefor, this resource isn't versioned.
        ClientResourceInfo value;
        if (resourcePaths == null || resourcePaths.size() == 0) {
            if (library != null) {
                value = new ClientResourceInfo(library, outContract[0], resourceName, null, compressable,
                        resourceSupportsEL(resourceName, library.getName(), ctx), ctx.isProjectStage(ProjectStage.Development), cacheTimestamp);
            } else {
                value = new ClientResourceInfo(outContract[0], resourceName, null, localePrefix, this, compressable,
                        resourceSupportsEL(resourceName, null, ctx), ctx.isProjectStage(ProjectStage.Development), cacheTimestamp);
            }
        } else {
            // ok, subdirectories exist, so find the latest 'version' directory
            VersionInfo version = getVersion(resourcePaths, true);
            if (version == null && LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "faces.application.resource.unable_to_determine_resource_version.", resourceName);
            }
            if (library != null) {
                value = new ClientResourceInfo(library, outContract[0], resourceName, version, compressable,
                        resourceSupportsEL(resourceName, library.getName(), ctx), ctx.isProjectStage(ProjectStage.Development), cacheTimestamp);
            } else {
                value = new ClientResourceInfo(outContract[0], resourceName, version, localePrefix, this, compressable,
                        resourceSupportsEL(resourceName, null, ctx), ctx.isProjectStage(ProjectStage.Development), cacheTimestamp);
            }
        }

        if (value.isCompressable()) {
            value = handleCompression(value);
        }
        return value;

    }

    private String findPathConsideringContracts(LibraryInfo library, String resourceName, String localePrefix, ContractInfo[] outContract, FacesContext ctx) {
        UIViewRoot root = ctx.getViewRoot();
        List<String> contracts = null;

        if (library != null) {
            if (library.getContract() == null) {
                contracts = Collections.emptyList();
            } else {
                contracts = new ArrayList<>(1);
                contracts.add(library.getContract());
            }
        } else if (root == null) {
            String contractName = ctx.getExternalContext().getRequestParameterMap().get("con");
            if (null != contractName && 0 < contractName.length() && !ResourceManager.nameContainsForbiddenSequence(contractName)) {
                contracts = new ArrayList<>();
                contracts.add(contractName);
            } else {
                return null;
            }
        } else {
            contracts = ctx.getResourceLibraryContracts();
        }

        String basePath = null;

        for (String curContract : contracts) {

            if (library != null) {
                // PENDING(fcaputo) no need to iterate over the contracts, if we have a library
                basePath = library.getPath(localePrefix) + '/' + resourceName;
            } else {
                if (localePrefix == null) {
                    basePath = getBaseContractsPath() + '/' + curContract + '/' + resourceName;
                } else {
                    basePath = getBaseContractsPath() + '/' + curContract + '/' + localePrefix + '/' + resourceName;
                }
            }

            try {
                if (ctx.getExternalContext().getResource(basePath) != null) {
                    outContract[0] = new ContractInfo(curContract);
                    break;
                } else {
                    basePath = null;
                }
            } catch (MalformedURLException e) {
                throw new FacesException(e);
            }
        }

        return basePath;
    }
}
