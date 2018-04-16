/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.faces.context.ExternalContext;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.config.WebConfiguration;

/**
 * Validate the ResourceManager.
 *
 * @since 2.0
 */
public class TestResourceManager extends ServletFacesTestCase {

    ResourceManager manager;

    public TestResourceManager() {
        super("TestResourceManager");
    }


    public TestResourceManager(String name) {
        super(name);
    }


    @Override public void setUp() {
        super.setUp();
        manager = new ResourceManager(null);
    }


    @Override public void tearDown() {
        super.tearDown();
        manager = null;
    }

    // ------------------------------------------------------------ Test Methods


    public void testWebappNonVersionedResource() throws Exception {
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource(null, "duke-nv.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);
        assertTrue(resource.getLibraryInfo() == null);
        assertTrue(resource.getHelper() instanceof WebappResourceHelper);
        assertTrue(resource.getVersion() == null);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue("duke-nv.gif".equals(resource.getName()));
        assertTrue("/resources/duke-nv.gif".equals(resource.getPath()));
    }

    public void testWebappVersionedResource() throws Exception {
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource(null, "duke.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);
        assertTrue(resource.getLibraryInfo() == null);
        assertTrue(resource.getHelper() instanceof WebappResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue("1_1".equals(resource.getVersion().toString()));
        assertTrue("duke.gif".equals(resource.getName()));
        assertTrue("/resources/duke.gif/1_1.gif".equals(resource.getPath()));
    }

    public void testWebappNonVersionedLibraryVersionedResource() throws Exception {
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource("nvLibrary", "duke.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);

        // validate the library
        assertTrue(resource.getLibraryInfo() != null);
        assertTrue("nvLibrary".equals(resource.getLibraryInfo().getName()));
        assertTrue(resource.getLibraryInfo().getVersion() == null);
        assertTrue(resource.getLibraryInfo().getHelper() instanceof WebappResourceHelper);
        assertTrue("/resources/nvLibrary".equals(resource.getLibraryInfo().getPath()));

        // validate the resource
        assertTrue(resource.getHelper() instanceof WebappResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue("1_1".equals(resource.getVersion().toString()));
        assertTrue("duke.gif".equals(resource.getName()));
        assertTrue("/resources/nvLibrary/duke.gif/1_1.gif".equals(resource.getPath()));
    }

    public void testWebappNonVersionedLibraryNonVersionedResource() throws Exception {
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource("nvLibrary", "duke-nv.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);

        // validate the library
        assertTrue(resource.getLibraryInfo() != null);
        assertTrue("nvLibrary".equals(resource.getLibraryInfo().getName()));
        assertTrue(resource.getLibraryInfo().getVersion() == null);
        assertTrue(resource.getLibraryInfo().getHelper() instanceof WebappResourceHelper);
        assertTrue("/resources/nvLibrary".equals(resource.getLibraryInfo().getPath()));

        // validate the resource
        assertTrue(resource.getHelper() instanceof WebappResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue(resource.getVersion() == null);
        assertTrue("duke-nv.gif".equals(resource.getName()));
        assertTrue("/resources/nvLibrary/duke-nv.gif".equals(resource.getPath()));
    }

    public void testWebappVersionedLibraryNonVersionedResource() throws Exception {
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource("vLibrary", "duke-nv.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);

        // validate the library
        assertTrue(resource.getLibraryInfo() != null);
        assertTrue("vLibrary".equals(resource.getLibraryInfo().getName()));
        assertTrue("2_0".equals(resource.getLibraryInfo().getVersion().toString()));
        assertTrue(resource.getLibraryInfo().getHelper() instanceof WebappResourceHelper);
        assertTrue("/resources/vLibrary/2_0".equals(resource.getLibraryInfo().getPath()));

        // validate the resource
        assertTrue(resource.getHelper() instanceof WebappResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue(resource.getVersion() == null);
        assertTrue("duke-nv.gif".equals(resource.getName()));
        assertTrue("/resources/vLibrary/2_0/duke-nv.gif".equals(resource.getPath()));
    }

    public void testWebappVersionedLibraryVersionedResource() throws Exception {
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource("vLibrary", "duke.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);

        // validate the library
        assertTrue(resource.getLibraryInfo() != null);
        assertTrue("vLibrary".equals(resource.getLibraryInfo().getName()));
        assertTrue("2_0".equals(resource.getLibraryInfo().getVersion().toString()));
        assertTrue(resource.getLibraryInfo().getHelper() instanceof WebappResourceHelper);
        assertTrue("/resources/vLibrary/2_0".equals(resource.getLibraryInfo().getPath()));

        // validate the resource
        assertTrue(resource.getHelper() instanceof WebappResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue("1_1".equals(resource.getVersion().toString()));
        assertTrue("duke.gif".equals(resource.getName()));
        assertTrue("/resources/vLibrary/2_0/duke.gif/1_1.gif".equals(resource.getPath()));
    }


    public void testJarNonVersionedResources() throws Exception {
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource(null, "duke-jar-nv.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);
        assertTrue(resource.getLibraryInfo() == null);
        assertTrue(resource.getHelper() instanceof ClasspathResourceHelper);
        assertTrue(resource.getVersion() == null);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue("duke-jar-nv.gif".equals(resource.getName()));
        assertTrue("META-INF/resources/duke-jar-nv.gif".equals(resource.getPath()));
    }

    /*
    public void testJarVersionedResource() throws Exception {
        ClientResourceInfo resource = manager.findResource(null, "duke-jar.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);
        assertTrue(resource.getLibraryInfo() == null);
        assertTrue(resource.getHelper() instanceof ClasspathResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue("1_1".equals(resource.getVersion().toString()));
        assertTrue("duke-jar.gif".equals(resource.getName()));
        assertTrue("META-INF/resources/duke-jar.gif/1_1.gif".equals(resource.getPath()));
    }
    */

    /*
    public void testJarNonVersionedLibraryVersionedResource() throws Exception {
        ClientResourceInfo resource = manager.findResource("nvLibrary-jar", "duke.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);

        // validate the library
        assertTrue(resource.getLibraryInfo() != null);
        assertTrue("nvLibrary-jar".equals(resource.getLibraryInfo().getName()));
        assertTrue(resource.getLibraryInfo().getVersion() == null);
        assertTrue(resource.getLibraryInfo().getHelper() instanceof ClasspathResourceHelper);
        assertTrue("META-INF/resources/nvLibrary-jar".equals(resource.getLibraryInfo().getPath()));

        // validate the resource
        assertTrue(resource.getHelper() instanceof ClasspathResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue("1_1".equals(resource.getVersion().toString()));
        assertTrue("duke.gif".equals(resource.getName()));
        assertTrue("META-INF/resources/nvLibrary-jar/duke.gif/1_1.gif".equals(resource.getPath()));
    }
    */

    public void testJarNonVersionedLibraryNonVersionedResource() throws Exception {
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource("nvLibrary-jar", "duke-nv.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);

        // validate the library
        assertTrue(resource.getLibraryInfo() != null);
        assertTrue("nvLibrary-jar".equals(resource.getLibraryInfo().getName()));
        assertTrue(resource.getLibraryInfo().getVersion() == null);
        assertTrue(resource.getLibraryInfo().getHelper() instanceof ClasspathResourceHelper);
        assertTrue("META-INF/resources/nvLibrary-jar".equals(resource.getLibraryInfo().getPath()));

        // validate the resource
        assertTrue(resource.getHelper() instanceof ClasspathResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue(resource.getVersion() == null);
        assertTrue("duke-nv.gif".equals(resource.getName()));
        assertTrue("META-INF/resources/nvLibrary-jar/duke-nv.gif".equals(resource.getPath()));
    }

    /*
    public void testJarVersionedLibraryNonVersionedResource() throws Exception {
        ClientResourceInfo resource = manager.findResource("vLibrary-jar", "duke-nv.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);

        // validate the library
        assertTrue(resource.getLibraryInfo() != null);
        assertTrue("vLibrary-jar".equals(resource.getLibraryInfo().getName()));
        assertTrue("2_0".equals(resource.getLibraryInfo().getVersion().toString()));
        assertTrue(resource.getLibraryInfo().getHelper() instanceof ClasspathResourceHelper);
        assertTrue("META-INF/resources/vLibrary-jar/2_0".equals(resource.getLibraryInfo().getPath()));

        // validate the resource
        assertTrue(resource.getHelper() instanceof ClasspathResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue(resource.getVersion() == null);
        assertTrue("duke-nv.gif".equals(resource.getName()));
        assertTrue("META-INF/resources/vLibrary-jar/2_0/duke-nv.gif".equals(resource.getPath()));
    }
    */

    /*
    public void testJarVersionedLibraryVersionedResource() throws Exception {
        ClientResourceInfo resource = manager.findResource("vLibrary-jar", "duke.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);

        // validate the library
        assertTrue(resource.getLibraryInfo() != null);
        assertTrue("vLibrary-jar".equals(resource.getLibraryInfo().getName()));
        assertTrue("2_0".equals(resource.getLibraryInfo().getVersion().toString()));
        assertTrue(resource.getLibraryInfo().getHelper() instanceof ClasspathResourceHelper);
        assertTrue("META-INF/resources/vLibrary-jar/2_0".equals(resource.getLibraryInfo().getPath()));

        // validate the resource
        assertTrue(resource.getHelper() instanceof ClasspathResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue("1_1".equals(resource.getVersion().toString()));
        assertTrue("duke.gif".equals(resource.getName()));
        assertTrue("META-INF/resources/vLibrary-jar/2_0/duke.gif/1_1.gif".equals(resource.getPath()));
    }
    */

    public void testNoExtensionVersionedResource() throws Exception {
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource("vLibrary", "duke2.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);

        // validate the library
        assertTrue(resource.getLibraryInfo() != null);
        assertTrue("vLibrary".equals(resource.getLibraryInfo().getName()));
        assertTrue("2_0".equals(resource.getLibraryInfo().getVersion().toString()));
        assertTrue(resource.getLibraryInfo().getHelper() instanceof WebappResourceHelper);
        assertTrue("/resources/vLibrary/2_0".equals(resource.getLibraryInfo().getPath()));

        // validate the resource
        assertTrue(resource.getHelper() instanceof WebappResourceHelper);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);
        assertTrue("1_1".equals(resource.getVersion().toString()));
        assertTrue(resource.getVersion().getExtension() == null);
        assertTrue("duke2.gif".equals(resource.getName()));
        assertTrue("/resources/vLibrary/2_0/duke2.gif/1_1".equals(resource.getPath()));   
    }


    public void testInvalidLibraryName() throws Exception {
        assertTrue(manager.findResource("noSuchLibrary", "duke.gif", "image/gif", getFacesContext()) == null);
    }

    public void testInvalidResourceName() throws Exception {
        assertTrue(manager.findResource(null, "duke.fig", null, getFacesContext()) == null);
        assertTrue(manager.findResource("nvLibrary", "duke.fig", null, getFacesContext()) == null);
    }

    public void testClientResourceInfoCompression() throws Exception {
        WebConfiguration config = WebConfiguration.getInstance();
        config.overrideContextInitParameter(WebConfiguration.WebContextInitParameter.CompressableMimeTypes, "image/gif,text/css,text/plain");
        // create a new ResourceManager so that the mime type configuration is picked up
        ResourceManager manager = new ResourceManager(null);
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource("nvLibrary", "duke-nv.gif", "image/gif", getFacesContext());
        assertTrue(resource != null);
        assertTrue(resource.isCompressable());
        assertTrue(compressionPathIsValid(resource));
        
        // ensure compression disabled for a content type that is null
        resource = (ClientResourceInfo) manager.findResource("nvLibrary", "duke-nv.gif", "text/javascript", getFacesContext());
        assertTrue(resource != null);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);

        // if a resource is compressable, but the compressed result is larger
        // than the original resource, the returned ClientResourceInfo shouldn't
        // be marked as compressable and getCompressedPath() will be null
        resource = (ClientResourceInfo) manager.findResource(null, "simple.txt", "text/plain", getFacesContext());
        assertTrue(resource != null);
        assertTrue(!resource.isCompressable());
        assertTrue(resource.getCompressedPath() == null);

        // if a resource is compressable, but the compressed result is larger
        // than the original resource, the returned ClientResourceInfo should be
        // marked compressable.  However, since css files may have EL expressions
        // embedded within, the the resource will be marked as supporting such.
        resource = (ClientResourceInfo) manager.findResource(null, "simple.css", "text/plain", getFacesContext());
        assertTrue(resource != null);
        assertTrue(resource.isCompressable());
        assertTrue(resource.supportsEL());
        assertTrue(resource.getCompressedPath() == null);

    }


    public void testELEvalDisabledIfNoExpressionEvaluated() throws Exception {

        ResourceManager manager = new ResourceManager(null);
        ClientResourceInfo resource = (ClientResourceInfo) manager.findResource(null, "simple.css", "text/css", getFacesContext());
        assertNotNull(resource);
        assertTrue(resource.supportsEL());
        ResourceImpl resImpl = new ResourceImpl(resource, "text/css", 0, 0);
        InputStream in = resImpl.getInputStream();
        for (int i = in.read(); i != -1; i = in.read()) { }
        try {
            in.close();
        } catch (Exception ioe) {
            fail(ioe.toString());
        }
        assertTrue(!resource.supportsEL());

        resource = (ClientResourceInfo) manager.findResource(null, "simple-with-el.css", "text/css", getFacesContext());

        assertNotNull(resource);
        assertTrue(resource.supportsEL());
        resImpl = new ResourceImpl(resource, "text/css", 0, 0);
        in = resImpl.getInputStream();
        for (int i = in.read(); i != -1; i = in.read()) { }
        try {
            in.close();
        } catch (Exception ioe) {
            fail(ioe.toString());
        }
        assertTrue(resource.supportsEL());

    }


    // --------------------------------------------------------- Private Methods


    private boolean compressionPathIsValid(ClientResourceInfo resource)
    throws IOException {

        ExternalContext extContext = getFacesContext().getExternalContext();
        File tempDir = (File) extContext.getApplicationMap().get("javax.servlet.context.tempdir");
        File expected = new File(tempDir, "/jsf-compressed" + File.separatorChar + resource.getPath());
        return expected.getCanonicalPath().equals(resource.getCompressedPath());

    }
    



} // END TestResourceManager
