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
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableMissingResourceLibraryDetection;
import static com.sun.faces.config.WebConfiguration.META_INF_CONTRACTS_DIR;
import static jakarta.faces.application.ProjectStage.Development;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.Util;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * A {@link ResourceHelper} implementation for finding/serving resources found on the classpath within the
 * <code>META-INF/resources directory</code>.
 * </p>
 *
 * @since 2.0
 */
public class ClasspathResourceHelper extends ResourceHelper {

    private static final String BASE_RESOURCE_PATH = "META-INF/resources";
    private final boolean cacheTimestamp;
    private volatile ZipDirectoryEntryScanner libraryScanner;
    private final boolean enableMissingResourceLibraryDetection;

    // ------------------------------------------------------------ Constructors

    public ClasspathResourceHelper() {
        WebConfiguration webconfig = WebConfiguration.getInstance();
        cacheTimestamp = webconfig.isOptionEnabled(CacheResourceModificationTimestamp);
        enableMissingResourceLibraryDetection = webconfig.isOptionEnabled(EnableMissingResourceLibraryDetection);

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClasspathResourceHelper other = (ClasspathResourceHelper) obj;
        if (cacheTimestamp != other.cacheTimestamp) {
            return false;
        }
        if (enableMissingResourceLibraryDetection != other.enableMissingResourceLibraryDetection) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (cacheTimestamp ? 1 : 0);
        hash = 67 * hash + (enableMissingResourceLibraryDetection ? 1 : 0);
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
        return META_INF_CONTRACTS_DIR;
    }

    /**
     * @see ResourceHelper#getNonCompressedInputStream(com.sun.faces.application.resource.ResourceInfo,
     * jakarta.faces.context.FacesContext)
     */
    @Override
    protected InputStream getNonCompressedInputStream(ResourceInfo resource, FacesContext ctx) throws IOException {

        InputStream in = null;

        if (ctx.isProjectStage(Development)) {
            ClassLoader loader = Util.getCurrentLoader(getClass());
            String path = resource.getPath();
            URL url = getResourceURL(loader, path, ctx);
            if (url != null) {
            	in = url.openStream();
            }
        } else {
            ClassLoader loader = Util.getCurrentLoader(getClass());
            String path = resource.getPath();
            in = getResourceAsStream(loader, path, ctx);
        }
        return in;
    }

    /**
     * @see ResourceHelper#getURL(com.sun.faces.application.resource.ResourceInfo, jakarta.faces.context.FacesContext)
     */
    @Override
    public URL getURL(ResourceInfo resource, FacesContext ctx) {
        String path = resource.getPath();
        ClassLoader loader = Util.getCurrentLoader(this.getClass());
        URL url = loader.getResource(path);
        if (url == null) {
            // try using this class' loader (necessary when running in OSGi)
            url = this.getClass().getClassLoader().getResource(resource.getPath());
        }
        return url;

    }

    /**
     * @see ResourceHelper#findLibrary(String, String, String, jakarta.faces.context.FacesContext)
     */
    @Override
    public LibraryInfo findLibrary(String libraryName, String localePrefix, String contract, FacesContext ctx) {

        ClassLoader loader = Util.getCurrentLoader(this);
        String basePath;
        if (localePrefix == null) {
            basePath = getBasePath(contract) + '/' + libraryName + '/';
        } else {
            basePath = getBasePath(contract) + '/' + localePrefix + '/' + libraryName + '/';
        }

        URL basePathURL = loader.getResource(basePath);
        if (basePathURL == null) {
            // try using this class' loader (necessary when running in OSGi)
            basePathURL = this.getClass().getClassLoader().getResource(basePath);
            if (basePathURL == null) {
                return null;
            }
        }

        return new LibraryInfo(libraryName, null, localePrefix, contract, this);

    }

    public LibraryInfo findLibraryWithZipDirectoryEntryScan(String libraryName, String localePrefix, String contract, FacesContext ctx, boolean forceScan) {

        ClassLoader loader = Util.getCurrentLoader(this);
        String basePath;
        if (localePrefix == null) {
            basePath = getBasePath(contract) + '/' + libraryName + '/';
        } else {
            basePath = getBasePath(contract) + '/' + localePrefix + '/' + libraryName + '/';
        }

        URL basePathURL = loader.getResource(basePath);
        if (basePathURL == null) {
            // try using this class' loader (necessary when running in OSGi)
            basePathURL = this.getClass().getClassLoader().getResource(basePath);
            if (basePathURL == null) {
                if (null != localePrefix && libraryName.equals("jakarta.faces")) {
                    return null;
                }
                if (enableMissingResourceLibraryDetection || forceScan) {
                    if (null == libraryScanner) {
                        libraryScanner = new ZipDirectoryEntryScanner();
                    }
                    if (!libraryScanner.libraryExists(libraryName, localePrefix)) {
                        return null;
                    }
                }
            }
        }

        return new LibraryInfo(libraryName, null, localePrefix, contract, this);
    }

    /**
     * @see ResourceHelper#findResource(LibraryInfo, String, String, boolean, jakarta.faces.context.FacesContext)
     */
    @Override
    public ResourceInfo findResource(LibraryInfo library, String resourceName, String localePrefix, boolean compressable, FacesContext ctx) {

        resourceName = trimLeadingSlash(resourceName);
        ContractInfo[] outContract = new ContractInfo[1];
        outContract[0] = null;
        String[] outBasePath = new String[1];
        outBasePath[0] = null;

        ClassLoader loader = Util.getCurrentLoader(this);
        URL basePathURL = findPathConsideringContracts(loader, library, resourceName, localePrefix, outContract, outBasePath, ctx);
        String basePath = outBasePath[0];
        if (null == basePathURL) {
            basePath = deriveBasePath(library, resourceName, localePrefix);
            basePathURL = loader.getResource(basePath);
        }

        if (null == basePathURL) {
            // try using this class' loader (necessary when running in OSGi)
            basePathURL = this.getClass().getClassLoader().getResource(basePath);
            if (basePathURL == null) {
                // Try it without the localePrefix
                if (library != null) {
                    basePath = library.getPath(null) + '/' + resourceName;
                } else {
                    basePath = getBaseResourcePath() + '/' + resourceName;
                }
                basePathURL = loader.getResource(basePath);
                if (basePathURL == null) {
                    // try using this class' loader (necessary when running in OSGi)
                    basePathURL = this.getClass().getClassLoader().getResource(basePath);
                    if (basePathURL == null) {
                        return null;
                    }
                }

                localePrefix = null;
            }
        }

        ClientResourceInfo value;

        if (library != null) {
            value = new ClientResourceInfo(library, outContract[0], resourceName, null, compressable, resourceSupportsEL(resourceName, library.getName(), ctx),
                    ctx.isProjectStage(ProjectStage.Development), cacheTimestamp);
        } else {
            value = new ClientResourceInfo(outContract[0], resourceName, null, localePrefix, this, compressable, resourceSupportsEL(resourceName, null, ctx),
                    ctx.isProjectStage(ProjectStage.Development), cacheTimestamp);
        }

        if (value.isCompressable()) {
            value = handleCompression(value);
        }
        return value;

    }

    private String deriveBasePath(LibraryInfo library, String resourceName, String localePrefix) {
        String basePath = null;
        if (library != null) {
            basePath = library.getPath(localePrefix) + '/' + resourceName;
        } else {
            if (localePrefix == null) {
                basePath = getBaseResourcePath() + '/' + resourceName;
            } else {
                basePath = getBaseResourcePath() + '/' + localePrefix + '/' + resourceName;
            }
        }
        return basePath;
    }

    private URL findPathConsideringContracts(ClassLoader loader, LibraryInfo library, String resourceName, String localePrefix, ContractInfo[] outContract,
            String[] outBasePath, FacesContext ctx) {
        UIViewRoot root = ctx.getViewRoot();
        List<String> contracts = null;
        URL result = null;

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

            if (null != (result = loader.getResource(basePath))) {
                outContract[0] = new ContractInfo(curContract);
                outBasePath[0] = basePath;
                break;
            } else {
                basePath = null;
            }
        }

        return result;
    }

    private InputStream getResourceAsStream(ClassLoader loader, String path, FacesContext ctx) {
    	InputStream in = null;
      	List<String> localizedPaths = getLocalizedPaths(path, ctx);
      	for (String path_: localizedPaths) {
      		in = getResourceAsStream(loader, path_);
      		if (in != null) {
      			break;
      		}
      	}
      	return in;
    }
    
    private URL getResourceURL(ClassLoader loader, String path, FacesContext ctx) {
    	List<String> localizedPaths = getLocalizedPaths(path, ctx);
    	URL url = null;
    	for (String path_: localizedPaths) {
    		url = getResource_(loader, path_);
    		if (url != null) {
    			break;
    		}
    	}
    	return url;
    }
    
    private InputStream getResourceAsStream(ClassLoader loader, String path) {
    	InputStream in = loader.getResourceAsStream(path);
    	if (in == null) {
    		in = getClass().getClassLoader().getResourceAsStream(path);
    	}
    	return in;
    }

    private URL getResource_(ClassLoader loader, String path) {
    	URL res = loader.getResource(path);
    	if (res == null) {
    		res = getClass().getClassLoader().getResource(path);
    	}
    	return res;
    }
    
}
