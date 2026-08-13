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

import java.util.Objects;

/**
 * <p>
 * <code>LibraryInfo</code> is a simple wrapper class for information pertinent to building a complete resource path
 * using a Library and/or Contract.
 * </p>
 */
public class LibraryInfo {

    private final String name;
    private final VersionInfo version;
    private String localePrefix;
    private String contract;
    private final ResourceHelper helper;
    private String path;
    private String nonLocalizedPath;

    /**
     * Constructs a new <code>LibraryInfo</code> using the specified details.
     *
     * @param name the name of the library
     * @param version the version of the library, if any
     * @param contract
     * @param helper the helper class for this resource
     */
    LibraryInfo(String name, VersionInfo version, String localePrefix, String contract, ResourceHelper helper) {
        this.name = name;
        this.version = version;
        this.localePrefix = localePrefix;
        this.contract = contract;
        this.helper = helper;
        initPath();
    }

    LibraryInfo(LibraryInfo other, boolean copyLocalePrefix) {
        name = other.name;
        version = other.version;
        if (copyLocalePrefix) {
            contract = other.contract;

            // http://java.net/jira/browse/JAVASERVERFACES_SPEC_PUBLIC-548 http://java.net/jira/browse/JAVASERVERFACES-2348
            localePrefix = other.localePrefix;
        }
        helper = other.helper;
        initPath();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        LibraryInfo other = (LibraryInfo) obj;

        return Objects.equals(name, other.name)
            && Objects.equals(version, other.version)
            && Objects.equals(localePrefix, other.localePrefix)
            && Objects.equals(contract, other.contract)
            && Objects.equals(path, other.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, localePrefix, contract, path);
    }

    /**
     * @return return the library name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return return the version of the library, or <code>null</code> if the library isn't versioned.
     */
    public VersionInfo getVersion() {
        return version;
    }

    /**
     * @return return the {@link ResourceHelper} for this resource
     */
    public ResourceHelper getHelper() {
        return helper;
    }

    /**
     * @return the base path of the library.
     */
    public String getPath() {
        return path;
    }

    public String getPath(String localePrefix) {
        return localePrefix == null ? nonLocalizedPath : path;
    }

    /**
     * @return the Locale prefix, if any.
     */
    public String getLocalePrefix() {
        return localePrefix;
    }

    /**
     * @return active contract or null
     */
    public String getContract() {
        return contract;
    }

    @Override
    public String toString() {
        return "LibraryInfo{" + "name='" + (name != null ? name : "NONE") + '\'' + ", version=" + (version != null ? version : "NONE") + '\''
                + ", localePrefix='" + (localePrefix != null ? localePrefix : "NONE") + '\'' + ", contract='" + (contract != null ? contract : "NONE")
                + '\'' + ", path='" + path + '\'' + '}';
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Construct the full path to the base directory of the library's resources.
     */
    private void initPath() {

        StringBuilder builder = new StringBuilder(64), noLocaleBuilder = new StringBuilder(64);

        appendBasePath(builder);
        appendBasePath(noLocaleBuilder);

        if (localePrefix != null) {
            builder.append('/').append(localePrefix);
        }
        if (name != null) {
            builder.append('/').append(name);
            noLocaleBuilder.append('/').append(name);
        }
        if (version != null) {
            builder.append('/').append(version.getVersion());
            noLocaleBuilder.append('/').append(version.getVersion());
        }
        path = builder.toString();
        nonLocalizedPath = noLocaleBuilder.toString();
    }

    private void appendBasePath(StringBuilder builder) {
        if (contract == null) {
            builder.append(helper.getBaseResourcePath());
        } else {
            builder.append(helper.getBaseContractsPath()).append('/').append(contract);
        }
    }

}
