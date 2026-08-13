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

import static jakarta.faces.application.ResourceHandler.FACES_SCRIPT_LIBRARY_NAME;
import static jakarta.faces.application.ResourceHandler.FACES_SCRIPT_RESOURCE_NAME;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.context.FacesContext;

/**
 * 
 * <code>ClientResourceInfo</code> is a simple wrapper class for information pertinent to building a complete resource
 * path using a Library.
 * 
 */
public class ClientResourceInfo extends ResourceInfo {

    private static final Logger LOGGER = FacesLogger.RESOURCE.getLogger();
    private static final String COMPRESSED_CONTENT_DIRECTORY = "faces-compressed";
    boolean cacheTimestamp;
    boolean isDevStage;
    String compressedPath;
    boolean compressible;
    boolean supportsEL;
    private volatile long lastModified = Long.MIN_VALUE;

    /**
     * Constructs a new <code>ClientResourceInfo</code> using the specified details. The {@link ResourceHelper} of the
     * resource will be the same as the {@link ResourceHelper} of the {@link LibraryInfo}.
     *
     * @param library the library containing this resource
     * @param contract the contract info
     * @param name the resource name
     * @param version the version of this resource (if any)
     * @param compressible if this resource should be compressed
     * @param supportsEL <code>true</code> if this resource may contain EL expressions
     * @param isDevStage true if this context is development stage
     * @param cacheTimestamp <code>true</code> if the modification time of the resource should be cached. The value of this
     * parameter will be ignored when {@link #isDevStage} is <code>true</code>
     */
    public ClientResourceInfo(LibraryInfo library, ContractInfo contract, String name, VersionInfo version, boolean compressible, boolean supportsEL,
            boolean isDevStage, boolean cacheTimestamp) {
        super(library, contract, name, version);
        this.compressible = compressible;
        this.supportsEL = supportsEL;
        this.isDevStage = isDevStage;
        this.cacheTimestamp = !isDevStage && cacheTimestamp;
        initPath(isDevStage);
    }

    /**
     * Constructs a new <code>ClientResourceInfo</code> using the specified details.
     *
     * @param name the resource name
     * @param version the version of the resource
     * @param localePrefix the locale prefix for this resource (if any)
     * @param helper helper the helper class for this resource
     * @param compressible if this resource should be compressed
     * @param supportsEL <code>true</code> if this resource may contain EL expressions
     * @param isDevStage true if this context is development stage
     * @param cacheTimestamp <code>true</code> if the modification time of the resource should be cached. The value of this
     * parameter will be ignored when {@link #isDevStage} is <code>true</code>
     */
    ClientResourceInfo(ContractInfo contract, String name, VersionInfo version, String localePrefix, ResourceHelper helper, boolean compressible,
            boolean supportsEL, boolean isDevStage, boolean cacheTimestamp) {
        super(contract, name, version, helper);
        this.name = name;
        this.version = version;
        this.localePrefix = localePrefix;
        this.helper = helper;
        this.compressible = compressible;
        this.supportsEL = supportsEL;
        this.isDevStage = isDevStage;
        this.cacheTimestamp = !isDevStage && cacheTimestamp;
        initPath(isDevStage);
    }

    ClientResourceInfo(ClientResourceInfo other, boolean copyLocalePrefix) {
        super(other, copyLocalePrefix);
        cacheTimestamp = other.cacheTimestamp;
        compressedPath = other.compressedPath;
        compressible = other.compressible;
        isDevStage = other.isDevStage;
        lastModified = other.lastModified;
        supportsEL = other.supportsEL;
        initPath(isDevStage);
    }

    public void copy(ClientResourceInfo other) {
        super.copy(other);
        cacheTimestamp = other.cacheTimestamp;
        compressedPath = other.compressedPath;
        compressible = other.compressible;
        isDevStage = other.isDevStage;
        lastModified = other.lastModified;
        supportsEL = other.supportsEL;
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * @return the path to which the compressed bits for this resource reside. If this resource isn't compressible and this
     * method is called, it will return <code>null</code>
     */
    public String getCompressedPath() {
        return compressedPath;
    }

    /**
     * @return <code>true</code> if this resource should be compressed, otherwise <code>false</code>
     */
    public boolean isCompressable() {
        return compressible;
    }

    /**
     * @return <code>true</code> if the this resource may contain EL expressions that should be evaluated, otherwise, return
     * <code>false</code>
     */
    public boolean supportsEL() {
        return supportsEL;
    }

    /**
     * Disables EL evaluation for this resource.
     */
    public void disableEL() {
        supportsEL = false;
    }

    /**
     * Returns the time this resource was last modified. If
     * {@link com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter#CacheResourceModificationTimestamp} is
     * true, the value will be cached for the lifetime if this <code>ClientResourceInfo</code> instance.
     *
     * @param ctx the {@link FacesContext} for the current request
     *
     * @return the time this resource was last modified (number of milliseconds since January 1, 1970 GMT).
     *
     */
    public long getLastModified(FacesContext ctx) {

        if (cacheTimestamp) {
            if (lastModified == Long.MIN_VALUE) {
                synchronized (this) {
                    if (lastModified == Long.MIN_VALUE) {
                        lastModified = helper.getLastModified(this, ctx);
                    }
                }
            }
            return lastModified;
        } else {
            return helper.getLastModified(this, ctx);
        }

    }

    @Override
    public String toString() {
        return "ResourceInfo{" + "name='" + name + '\'' + ", version=\'" + (version != null ? version : "NONE") + '\'' + ", libraryName='" + libraryName
                + '\'' + ", contractInfo='" + (contract != null ? contract.contract : "NONE") + '\'' + ", libraryVersion='"
                + (library != null ? library.getVersion() : "NONE") + '\'' + ", localePrefix='" + (localePrefix != null ? localePrefix : "NONE") + '\''
                + ", path='" + path + '\'' + ", compressible='" + compressible + '\'' + ", compressedPath=" + compressedPath + '}';
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Create the full path to the resource. If the resource can be compressed, setup the compressedPath ivar so that the
     * path refers to the directory referenced by the context attribute <code>jakarta.servlet.context.tempdir</code>.
     */
    private void initPath(boolean isDevStage) {

        StringBuilder sb = new StringBuilder(32);
        if (library != null) {
            sb.append(library.getPath());
        } else {
            if (null != contract) {
                sb.append(helper.getBaseContractsPath());
                sb.append("/").append(contract);
            } else {
                sb.append(helper.getBaseResourcePath());
            }
        }
        if (library == null && localePrefix != null) {
            sb.append('/').append(localePrefix);
        }
        // Specialcasing for handling Faces script in uncompressed state
        if (isDevStage && FACES_SCRIPT_LIBRARY_NAME.equals(libraryName) && FACES_SCRIPT_RESOURCE_NAME.equals(name)) {
            sb.append('/').append("faces-uncompressed.js");
        } else {
            sb.append('/').append(name);
        }
        if (version != null) {
            sb.append('/').append(version.getVersion());
            String extension = version.getExtension();
            if (extension != null) {
                sb.append('.').append(extension);
            }
        }
        path = sb.toString();

        if (compressible && !supportsEL) { // compression for static resources
            FacesContext ctx = FacesContext.getCurrentInstance();
            File servletTmpDir = (File) ctx.getExternalContext().getApplicationMap().get("jakarta.servlet.context.tempdir");
            if (servletTmpDir == null || !servletTmpDir.isDirectory()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                            "File ({0}) referenced by jakarta.servlet.context.tempdir attribute is null, or was is not a directory.  Compression for {1} will be unavailable.",
                            new Object[] { servletTmpDir == null ? "null" : servletTmpDir.toString(), path });
                }
                compressible = false;
            } else {
                String tPath = path.charAt(0) == '/' ? path : '/' + path;
                File newDir = new File(servletTmpDir, COMPRESSED_CONTENT_DIRECTORY + tPath);

                try {
                    if (!newDir.exists()) {
                        if (newDir.mkdirs()) {
                            compressedPath = newDir.getCanonicalPath();
                        } else {
                            compressible = false;
                            if (LOGGER.isLoggable(Level.WARNING)) {
                                LOGGER.log(Level.WARNING, "faces.application.resource.unable_to_create_compression_directory", newDir.getCanonicalPath());
                            }
                        }
                    } else {
                        compressedPath = newDir.getCanonicalPath();
                    }
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, e.toString(), e);
                    }
                    compressible = false;
                }
            }
        }

    }

}
