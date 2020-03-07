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

public class ResourceInfo {

    ResourceHelper helper;
    LibraryInfo library;
    ContractInfo contract;
    String libraryName;
    String localePrefix;
    String name;
    String path;
    VersionInfo version;
    boolean doNotCache = false;

    public ResourceInfo(LibraryInfo library, ContractInfo contract, String name, VersionInfo version) {
        this.contract = contract;
        this.library = library;
        helper = library.getHelper();
        localePrefix = library.getLocalePrefix();
        this.name = name;
        this.version = version;
        libraryName = library.getName();

    }

    public ResourceInfo(ContractInfo contract, String name, VersionInfo version, ResourceHelper helper) {
        this.contract = contract;
        this.name = name;
        this.version = version;
        this.helper = helper;
    }

    public ResourceInfo(ResourceInfo other, boolean copyLocalePrefix) {
        helper = other.helper;
        library = new LibraryInfo(other.library, copyLocalePrefix);
        libraryName = library.getName();
        if (copyLocalePrefix) {
            localePrefix = other.localePrefix;
        }
        name = other.name;
        path = other.path;
        version = other.version;
    }

    public void copy(ResourceInfo other) {
        helper = other.helper;
        library = other.library;
        libraryName = other.libraryName;
        localePrefix = other.localePrefix;
        name = other.name;
        path = other.path;
        version = other.version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceInfo other = (ResourceInfo) obj;
        if (helper != other.helper && (helper == null || !helper.equals(other.helper))) {
            return false;
        }
        if (library != other.library && (library == null || !library.equals(other.library))) {
            return false;
        }
        if (libraryName == null ? other.libraryName != null : !libraryName.equals(other.libraryName)) {
            return false;
        }
        if (localePrefix == null ? other.localePrefix != null : !localePrefix.equals(other.localePrefix)) {
            return false;
        }
        if (name == null ? other.name != null : !name.equals(other.name)) {
            return false;
        }
        if (path == null ? other.path != null : !path.equals(other.path)) {
            return false;
        }
        if (version != other.version && (version == null || !version.equals(other.version))) {
            return false;
        }
        if (doNotCache != other.doNotCache) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (helper != null ? helper.hashCode() : 0);
        hash = 17 * hash + (library != null ? library.hashCode() : 0);
        hash = 17 * hash + (libraryName != null ? libraryName.hashCode() : 0);
        hash = 17 * hash + (localePrefix != null ? localePrefix.hashCode() : 0);
        hash = 17 * hash + (name != null ? name.hashCode() : 0);
        hash = 17 * hash + (path != null ? path.hashCode() : 0);
        hash = 17 * hash + (version != null ? version.hashCode() : 0);
        hash = 17 * hash + (doNotCache ? 1 : 0);
        return hash;
    }

    public boolean isDoNotCache() {
        return doNotCache;
    }

    public void setDoNotCache(boolean doNotCache) {
        this.doNotCache = doNotCache;
    }

    /**
     * @return return the {@link ResourceHelper} for this resource
     */
    public ResourceHelper getHelper() {
        return helper;
    }

    /**
     * @return the Library associated with this resource, if any.
     */
    public LibraryInfo getLibraryInfo() {
        return library;
    }

    /**
     * @return the Locale prefix, if any.
     */
    public String getLocalePrefix() {
        return localePrefix;
    }

    /**
     * @return return the library name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the full path (including the library, if any) of the resource.
     */
    public String getPath() {
        return path;
    }

    public String getContract() {
        return null != contract ? contract.toString() : null;
    }

    /**
     * @return return the version of the resource, or <code>null</code> if the resource isn't versioned.
     */
    public VersionInfo getVersion() {
        return version;
    }

}
