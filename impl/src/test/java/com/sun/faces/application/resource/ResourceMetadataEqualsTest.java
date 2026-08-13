/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import jakarta.faces.context.FacesContext;

public class ResourceMetadataEqualsTest {

    private static final ResourceHelper RESOURCE_HELPER = new ResourceHelper() {

        @Override
        public String getBaseResourcePath() {
            return "/resources";
        }

        @Override
        public String getBaseContractsPath() {
            return "/contracts";
        }

        @Override
        public URL getURL(ResourceInfo resource, FacesContext ctx) {
            return null;
        }

        @Override
        public LibraryInfo findLibrary(String libraryName, String localePrefix, String contract, FacesContext ctx) {
            return null;
        }

        @Override
        public ResourceInfo findResource(LibraryInfo library, String resourceName, String localePrefix, boolean compressable, FacesContext ctx) {
            return null;
        }

        @Override
        protected InputStream getNonCompressedInputStream(ResourceInfo resource, FacesContext ctx) {
            return null;
        }
    };

    /**
     * A subclass carries state that the base class knows nothing about, so it must never compare equal to a base
     * instance holding the same base state, in either direction. Both hierarchies are therefore safe to mix within one
     * <code>Collection&lt;? extends ResourceInfo&gt;</code> or <code>Collection&lt;? extends LibraryInfo&gt;</code>.
     */
    @Test
    public void subclassIsNeverEqualToBaseClassWithSameBaseState() throws MalformedURLException {
        URL url = new URL("http://localhost/theme.css");
        VersionInfo version = new VersionInfo("1_0", "css");
        ContractInfo contract = new ContractInfo("contract");

        ResourceInfo resourceInfo = new ResourceInfo(contract, "theme.css", version, RESOURCE_HELPER);
        ResourceInfo faceletResourceInfo = new FaceletResourceInfo(contract, "theme.css", version, RESOURCE_HELPER, url);

        assertNotEquals(resourceInfo, faceletResourceInfo);
        assertNotEquals(faceletResourceInfo, resourceInfo);

        LibraryInfo libraryInfo = new LibraryInfo("library", version, "nl", null, RESOURCE_HELPER);
        LibraryInfo faceletLibraryInfo = new FaceletLibraryInfo("library", version, "nl", null, RESOURCE_HELPER, url);

        assertNotEquals(libraryInfo, faceletLibraryInfo);
        assertNotEquals(faceletLibraryInfo, libraryInfo);
    }

    @Test
    public void sameStateIsEqualAndSharesHashCode() {
        VersionInfo version = new VersionInfo("1_0", "css");
        ContractInfo contract = new ContractInfo("contract");

        assertEquals(version, new VersionInfo("1_0", "css"));
        assertEquals(version.hashCode(), new VersionInfo("1_0", "css").hashCode());
        assertNotEquals(version, new VersionInfo("1_0", "js"));

        assertEquals(contract, new ContractInfo("contract"));
        assertEquals(contract.hashCode(), new ContractInfo("contract").hashCode());
        assertNotEquals(contract, new ContractInfo("other"));

        LibraryInfo libraryInfo = new LibraryInfo("library", version, "nl", null, RESOURCE_HELPER);
        LibraryInfo sameLibraryInfo = new LibraryInfo("library", version, "nl", null, RESOURCE_HELPER);

        assertEquals(libraryInfo, sameLibraryInfo);
        assertEquals(libraryInfo.hashCode(), sameLibraryInfo.hashCode());
        assertNotEquals(libraryInfo, new LibraryInfo("other", version, "nl", null, RESOURCE_HELPER));

        ResourceInfo resourceInfo = new ResourceInfo(contract, "theme.css", version, RESOURCE_HELPER);
        ResourceInfo sameResourceInfo = new ResourceInfo(contract, "theme.css", version, RESOURCE_HELPER);

        assertEquals(resourceInfo, sameResourceInfo);
        assertEquals(resourceInfo.hashCode(), sameResourceInfo.hashCode());
        assertNotEquals(resourceInfo, new ResourceInfo(contract, "other.css", version, RESOURCE_HELPER));
    }

    @Test
    public void nullAndUnrelatedTypesAreNotEqual() {
        VersionInfo version = new VersionInfo("1_0", "css");

        assertNotEquals(null, new ContractInfo("contract"));
        assertNotEquals("contract", new ContractInfo("contract"));
        assertNotEquals(null, version);
        assertNotEquals("1_0", version);
        assertNotEquals(null, new LibraryInfo("library", version, "nl", null, RESOURCE_HELPER));
        assertNotEquals(null, new ResourceInfo(null, "theme.css", version, RESOURCE_HELPER));
    }

}
