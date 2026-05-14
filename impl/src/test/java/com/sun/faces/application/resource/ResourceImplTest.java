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

import static jakarta.faces.application.ProjectStage.Development;
import static jakarta.faces.application.ResourceHandler.FACES_SCRIPT_LIBRARY_NAME;
import static jakarta.faces.application.ResourceHandler.FACES_SCRIPT_RESOURCE_NAME;
import static jakarta.faces.application.ResourceHandler.RESOURCE_IDENTIFIER;
import static jakarta.servlet.http.MappingMatch.EXACT;
import static jakarta.servlet.http.MappingMatch.EXTENSION;
import static jakarta.servlet.http.MappingMatch.PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockApplication;
import com.sun.faces.mock.MockFacesMappingSupport;
import com.sun.faces.util.MojarraVersion;

import jakarta.faces.context.FacesContext;

public class ResourceImplTest extends JUnitFacesTestCaseBase {

    private static final String CONTEXT_PATH = "/app";
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

    @Test
    public void getRequestPathReturnsExactHitForExactMappedResource() {
        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact", RESOURCE_IDENTIFIER + "/theme.css", "*.xhtml", "/faces/*");

        assertEquals("/app/jakarta.faces.resource/theme.css", createResource("theme.css", null, null, null, null, null).getRequestPath());
    }

    @Test
    public void getRequestPathKeepsExtensionFallbackForExactRequests() {
        ResourceImpl resource = createResource("theme.css", null, null, null, null, null);

        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact", "*.xhtml");
        String exactRequestPath = resource.getRequestPath();

        configureRequest(MockFacesMappingSupport.mapping(EXTENSION, "*.xhtml", "theme"), "/exact", "*.xhtml");
        String extensionBaselinePath = resource.getRequestPath();

        assertEquals("/app/jakarta.faces.resource/theme.css.xhtml", exactRequestPath);
        assertEquals(extensionBaselinePath, exactRequestPath);
    }

    @Test
    public void getRequestPathKeepsDirectPathBehavior() {
        ResourceImpl resource = createResource("theme.css", "layout", "1_0", null, "en", "blue");

        configureRequest(MockFacesMappingSupport.mapping(PATH, "/faces/*", "theme"), "/exact", "/faces/*");
        String directPathRequestPath = resource.getRequestPath();

        assertEquals("/app/faces/jakarta.faces.resource/theme.css?ln=layout&v=1_0&loc=en&con=blue", directPathRequestPath);
    }

    @Test
    public void getRequestPathPrefersExtensionFallbackWhenBothWildcardsExist() {
        ResourceImpl resource = createResource("theme.css", "layout", "1_0", null, "en", "blue");

        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact", "*.jsf", "/faces/*");
        String exactRequestPath = resource.getRequestPath();

        assertEquals("/app/jakarta.faces.resource/theme.css.jsf?ln=layout&v=1_0&loc=en&con=blue", exactRequestPath);
        assertEquals(exactRequestPath.indexOf("ln="), exactRequestPath.lastIndexOf("ln="));
        assertEquals(exactRequestPath.indexOf("v="), exactRequestPath.lastIndexOf("v="));
        assertEquals(exactRequestPath.indexOf("loc="), exactRequestPath.lastIndexOf("loc="));
        assertEquals(exactRequestPath.indexOf("con="), exactRequestPath.lastIndexOf("con="));
    }

    @Test
    public void getRequestPathPrefersExtensionFallbackForFacesScriptInDevelopment() {
        MockApplication developmentApplication = new MockApplication() {

            @Override
            public jakarta.faces.application.ProjectStage getProjectStage() {
                return Development;
            }
        };
        developmentApplication.setViewHandler(application.getViewHandler());
        application = developmentApplication;
        facesContext.setApplication(application);

        ResourceImpl resource = createResource(FACES_SCRIPT_RESOURCE_NAME, FACES_SCRIPT_LIBRARY_NAME, null, null, null, null);

        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact", "*.jsf", "/faces/*");
        String exactRequestPath = resource.getRequestPath();

        String versionParam = MojarraVersion.IMPLEMENTATION_VERSION != null ? "&v=" + MojarraVersion.IMPLEMENTATION_VERSION : "";
        assertEquals("/app/jakarta.faces.resource/faces.js.jsf?ln=jakarta.faces" + versionParam + "&stage=Development", exactRequestPath);
    }

    @Test
    public void getRequestPathDoesNotAppendImplVersionForOtherLibrariesWithoutVersion() {
        ResourceImpl resource = createResource("theme.css", "layout", null, null, null, null);

        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact", "*.jsf", "/faces/*");
        String exactRequestPath = resource.getRequestPath();

        assertEquals("/app/jakarta.faces.resource/theme.css.jsf?ln=layout", exactRequestPath);
    }

    @Test
    public void getRequestPathThrowsWhenNoWildcardFallbackExists() {
        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact");

        assertThrows(IllegalStateException.class, () -> createResource("theme.css", null, null, null, null, null).getRequestPath());
    }

    private void configureRequest(jakarta.servlet.http.HttpServletMapping mapping, String... servletMappings) {
        request = MockFacesMappingSupport.request(CONTEXT_PATH, mapping, session);
        externalContext = MockFacesMappingSupport.externalContext(servletContext, request, response);
        facesContext.setExternalContext(externalContext);
        MockFacesMappingSupport.setFacesServletMappings(servletContext, servletMappings);
    }

    private ResourceImpl createResource(String resourceName, String libraryName, String libraryVersion, String resourceVersion, String localePrefix, String contract) {
        ContractInfo contractInfo = contract != null ? new ContractInfo(contract) : null;
        VersionInfo libraryVersionInfo = libraryVersion != null ? new VersionInfo(libraryVersion, null) : null;
        VersionInfo resourceVersionInfo = resourceVersion != null ? new VersionInfo(resourceVersion, extensionOf(resourceName)) : null;

        ResourceInfo resourceInfo;
        if (libraryName != null) {
            LibraryInfo libraryInfo = new LibraryInfo(libraryName, libraryVersionInfo, localePrefix, contract, RESOURCE_HELPER);
            resourceInfo = new ResourceInfo(libraryInfo, contractInfo, resourceName, resourceVersionInfo);
        } else {
            resourceInfo = new ResourceInfo(contractInfo, resourceName, resourceVersionInfo, RESOURCE_HELPER);
        }

        return new ResourceImpl(resourceInfo, "text/css", 0, 0);
    }

    private String extensionOf(String resourceName) {
        int extensionIndex = resourceName.lastIndexOf('.');
        return extensionIndex >= 0 ? resourceName.substring(extensionIndex + 1) : null;
    }
}
