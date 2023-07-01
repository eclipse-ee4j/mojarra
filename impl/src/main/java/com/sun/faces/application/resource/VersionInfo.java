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

/**
 * Metadata pertaining to versions.
 */
public class VersionInfo implements Comparable<VersionInfo> {

    private final String version;
    private final String extension;

    // ------------------------------------------------------------ Constructors

    /**
     * Constructs a new VersionInfo instance.
     *
     * @param version the version
     * @param extension the extension (only pertains to versioned resources, not libraries, and thus may be
     * <code>null</code>)
     */
    public VersionInfo(String version, String extension) {
        this.version = version;
        this.extension = extension;
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * @return the version
     */
    public String getVersion() {

        return version;

    }

    /**
     * @return the extension of the resource at processing time, or null if this version is associated with a library
     */
    public String getExtension() {

        return extension;

    }

    @Override
    public String toString() {

        return version;

    }

    @Override
    public int hashCode() {

        return version.hashCode() ^ (extension != null ? extension.hashCode() : 0);

    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof VersionInfo)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        VersionInfo passed = (VersionInfo) obj;
        boolean versionsEqual = version.equals(passed.version);
        boolean extensionEqual;
        if (extension == null) {
            extensionEqual = passed.extension == null;
        } else {
            extensionEqual = extension.equals(passed.extension);
        }
        return versionsEqual && extensionEqual;

    }

    // ------------------------------------------------- Methods from Comparable

    @Override
    public int compareTo(VersionInfo versionInfo) {
        assert versionInfo != null;
        return version.compareTo(versionInfo.version);
    }
}
