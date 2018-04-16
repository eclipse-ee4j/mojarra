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

// TestRenderKit.java

package com.sun.faces.renderkit;

import com.sun.faces.cactus.FileOutputResponseWriter;
import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.renderkit.html_basic.FormRenderer;
import com.sun.faces.renderkit.html_basic.TextRenderer;
import com.sun.faces.renderkit.html_basic.HiddenRenderer;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.manager.DbfFactory;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.config.ConfigManager;
import com.sun.faces.config.processor.ConfigProcessor;
import com.sun.faces.config.processor.FactoryConfigProcessor;
import com.sun.faces.config.processor.ApplicationConfigProcessor;
import com.sun.faces.config.processor.RenderKitConfigProcessor;
import com.sun.faces.spi.InjectionProvider;
import com.sun.faces.spi.InjectionProviderFactory;
import com.sun.faces.util.Util;

import javax.faces.FactoryFinder;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.context.FacesContext;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import javax.faces.render.RenderKitWrapper;

import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.net.URL;

import org.apache.cactus.WebRequest;
import org.w3c.dom.Document;

/**
 * <B>TestRenderKit</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class TestRenderKit extends ServletFacesTestCase {

//
// Protected Constants
//

    public static final String OUTPUT_FILENAME =
        FileOutputResponseWriter.FACES_RESPONSE_ROOT + "TestRenderKit_out";

    public static final String CORRECT_OUTPUT_FILENAME =
        FileOutputResponseWriter.FACES_RESPONSE_ROOT + "TestRenderKit_correct";



//
// Class Variables
//

//
// Instance Variables
//
    private RenderKit renderKit = null;

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public TestRenderKit() {
        super("TestRenderKit");
    }


    public TestRenderKit(String name) {
        super(name);
    }
//
// Class methods
//

//
// General Methods
//

    public void testGetRenderer() {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                  RenderKitFactory.HTML_BASIC_RENDER_KIT);

        // 1. Verify "getRenderer()" returns a Renderer instance
        //  
        Renderer renderer = renderKit.getRenderer("javax.faces.Form",
                                                  "javax.faces.Form");
        assertTrue(renderer instanceof FormRenderer);

        // 2. Verify "getRenderer()" returns null
        //
        renderer = renderKit.getRenderer("Foo", "Bar");
        assertTrue(renderer == null);

        // 3. Verify NPE
        //
        boolean exceptionThrown = false;
        try {
            renderer = renderKit.getRenderer(null, null);
        } catch (NullPointerException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }


    public void testAddRenderer() {
        boolean bool = false;
        FormRenderer formRenderer = new FormRenderer();
        TextRenderer textRenderer = new TextRenderer();

        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                  RenderKitFactory.HTML_BASIC_RENDER_KIT);
        // Test to see if addRenderer replaces the renderer if given
        // the same rendererType.
        //
        renderKit.addRenderer("Form", "Form", formRenderer);
        assertTrue(
            renderKit.getRenderer("Form", "Form") instanceof FormRenderer);
        renderKit.addRenderer("Form", "Form", textRenderer);
        assertTrue(
            renderKit.getRenderer("Form", "Form") instanceof TextRenderer);

        bool = false;
        try {
            renderKit.addRenderer("BlahFamily", null, formRenderer);
        } catch (NullPointerException e) {
            bool = true;
        }
        assertTrue(bool);

        bool = false;
        try {
            renderKit.addRenderer(null, "BlahRenderer", formRenderer);
        } catch (NullPointerException e) {
            bool = true;
        }
        assertTrue(bool);

        bool = false;
        try {
            renderKit.addRenderer("BlahFamily", "BlahRenderer", null);
        } catch (NullPointerException e) {
            bool = true;
        }
        assertTrue(bool);

    }


    public void testCreateResponseStream() throws Exception {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                  RenderKitFactory.HTML_BASIC_RENDER_KIT);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResponseStream stream = renderKit.createResponseStream(out);
        stream.write('a');
        stream.write((byte) 'b');
        stream.write(new byte[]{(byte) 'c', (byte) 'd', (byte) 'e'}, 1, 2);
        stream.flush();
        String result = out.toString();
        assertTrue(result.equals("abde"));
        try {
            stream.close();
        } catch (IOException ioe) {
            ; // ignore
        }
    }


    public void testCreateResponseWriter() throws Exception {

        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                            RenderKitFactory.HTML_BASIC_RENDER_KIT);
	// use an invalid encoding
        try {
            renderKit.createResponseWriter(new StringWriter(), null, "foo");

            fail("IllegalArgumentException Should Have Been Thrown!");

        } catch (IllegalArgumentException iae) {
        }
	
	ResponseWriter writer = null;

	// see that the proper content type is picked up based on the
	// contentTypeList param
	writer = renderKit.createResponseWriter(new StringWriter(), 
						"application/xhtml+xml,text/html", 
						"ISO-8859-1");
	assertEquals(writer.getContentType(), "application/xhtml+xml");
	writer = renderKit.createResponseWriter(new StringWriter(), 
						"text/html,application/xhtml+xml",
						"ISO-8859-1");
	assertEquals(writer.getContentType(), "text/html");

	// see that IAE is thrown if the content type isn't known
	try {
	    writer = renderKit.createResponseWriter(new StringWriter(), 
						    "application/pdf",
						    "ISO-8859-1");
	    
            fail("IllegalArgumentException Should Have Been Thrown!");

        } catch (IllegalArgumentException iae) {
        }

    }

    public void beginCreateResponseWriterAllMedia(WebRequest theRequest) {
        theRequest.addHeader("Accept", "*/*");
    }

    public void testCreateResponseWriterAllMedia() throws Exception {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                            RenderKitFactory.HTML_BASIC_RENDER_KIT);
        ResponseWriter writer = null;

        // see that the proper content type is picked up based on the
        // accept header  
        writer = renderKit.createResponseWriter(new StringWriter(), null, "ISO-8859-1");
        assertEquals(writer.getContentType(), "text/html");
        
    	//Ensure we correctly support */* in content type ranges
    	writer = renderKit.createResponseWriter(new StringWriter(), 
    			"application/xhtml+xml,*/*", 
    			"ISO-8859-1");
    	assertEquals(writer.getContentType(), "application/xhtml+xml");

    	writer = renderKit.createResponseWriter(new StringWriter(), 
    			"text/css,*/*",
    			"ISO-8859-1");
    	assertEquals(writer.getContentType(), "text/html");

    	writer = renderKit.createResponseWriter(new StringWriter(), 
    			"*/*",
    			"ISO-8859-1");
    	assertEquals(writer.getContentType(), "text/html");
    }

    public void beginCreateResponseWriter1(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/html; q=0.2, application/xhtml+xml; q=0.8, application/xml; q=0.5, */*");
    }

    public void testCreateResponseWriter1() throws Exception {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                            RenderKitFactory.HTML_BASIC_RENDER_KIT);
        ResponseWriter writer = null;

        // see that the proper content type is picked up based on the
        // accept header  
        writer = renderKit.createResponseWriter(new StringWriter(), null, "ISO-8859-1");
        assertEquals(writer.getContentType(), "application/xhtml+xml");
    }

    public void beginCreateResponseWriter2(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/html; q=0.2, application/xhtml+xml; q=0.8, application/xml; q=0.9, */*");
    }

    public void testCreateResponseWriter2() throws Exception {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                            RenderKitFactory.HTML_BASIC_RENDER_KIT);
        ResponseWriter writer = null;
                                                                                                                   
        // see that the proper content type is picked up based on the
        // accept header
        writer = renderKit.createResponseWriter(new StringWriter(), null, "ISO-8859-1");
        assertEquals(writer.getContentType(), "application/xhtml+xml");
    }

    public void beginCreateResponseWriter3(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/html, application/xhtml+xml; q=0.8, application/xml; q=0.9, */*");
    }
                                                                                                                   
    public void testCreateResponseWriter3() throws Exception {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                            RenderKitFactory.HTML_BASIC_RENDER_KIT);
        ResponseWriter writer = null;
                                                                                                                   
        // see that the proper content type is picked up based on the
        // accept header
        writer = renderKit.createResponseWriter(new StringWriter(), null, "ISO-8859-1");
        assertEquals(writer.getContentType(), "text/html");
    }

    public void beginCreateResponseWriter4(WebRequest theRequest) {
        theRequest.addHeader("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, */*");
        theRequest.addHeader("Accept", "text/html; level=1");
    }
                                                                                                                           
    public void testCreateResponseWriter4() throws Exception {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                            RenderKitFactory.HTML_BASIC_RENDER_KIT);
        ResponseWriter writer = null;
                                                                                                                           
        // see that the proper content type is picked up based on the
        // accept header
        writer = renderKit.createResponseWriter(new StringWriter(), null, "ISO-8859-1");
        assertEquals(writer.getContentType(), "text/html");
    }

    // Response has unsupported content type..
    public void testCreateResponseWriter5() throws Exception {
        ((ServletResponse)getFacesContext().getExternalContext().getResponse()).setContentType("image/png");
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                            RenderKitFactory.HTML_BASIC_RENDER_KIT);
        ResponseWriter writer = null;
                                                                                                                         
        // see that the proper content type is picked up based on the
        // accept header
        writer = renderKit.createResponseWriter(new StringWriter(), null, "ISO-8859-1");
        assertEquals(writer.getContentType(), "text/html");
    }

    // Added for issue 807
    public void beginCreateResponseWriter6(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/");
    }

    public void testCreateResponseWriter6() throws Exception {
        RenderKitFactory renderKitFactory = (RenderKitFactory)
              FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                            RenderKitFactory.HTML_BASIC_RENDER_KIT);
        ResponseWriter writer = null;

        // see that the proper content type is picked up based on the
        // accept header
        writer = renderKit
              .createResponseWriter(new StringWriter(), null, "ISO-8859-1");
        assertEquals(writer.getContentType(), "text/html");
    }



    public void testGetComponentFamilies() {

        RenderKitImpl rk = new RenderKitImpl();
        Iterator empty = rk.getComponentFamilies();
        assertNotNull(empty);
        assertTrue(!empty.hasNext());

        rk.addRenderer("family", "rendererType", new HiddenRenderer());
        Iterator notEmpty = rk.getComponentFamilies();
        assertNotNull(notEmpty);
        assertTrue(notEmpty.hasNext());

    }


    public void testGetRendererTypes() {

        RenderKitImpl rk = new RenderKitImpl();
        rk.addRenderer("family", "rendererType", new HiddenRenderer());

        Iterator empty = rk.getRendererTypes("non-exist");
        assertNotNull(empty);
        assertTrue(!empty.hasNext());

        Iterator notEmpty = rk.getRendererTypes("family");
        assertNotNull(notEmpty);
        assertTrue(notEmpty.hasNext());

    }


    // Added for issue 1106
    public void beginCreateResponseWriter7(WebRequest theRequest) {
        theRequest.addHeader("Accept", "*/*");
    }

    public void testCreateResponseWriter7() throws Exception {
        WebConfiguration webConfig = WebConfiguration.getInstance();
        webConfig.overrideContextInitParameter(WebConfiguration.BooleanWebContextInitParameter.PreferXHTMLContentType, true);
        RenderKitFactory renderKitFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(getFacesContext(),
                                                            RenderKitFactory.HTML_BASIC_RENDER_KIT);
        ResponseWriter writer = null;

        // see that the proper content type is picked up based on the
        // accept header
        writer = renderKit.createResponseWriter(new StringWriter(), null, "ISO-8859-1");
        assertEquals(writer.getContentType(), "application/xhtml+xml");
        webConfig.overrideContextInitParameter(WebConfiguration.BooleanWebContextInitParameter.PreferXHTMLContentType, true);
    }


    public void testRenderKitDecoration() throws Exception {

        FacesContext ctx = getFacesContext();
        ServletContext servletContext = (ServletContext) ctx.getExternalContext().getContext();
        FactoryFinder.releaseFactories();
        servletContext.removeAttribute("com.sun.faces.ApplicationAssociate");
        ConfigManager config = ConfigManager.getInstance(servletContext);

        DocumentBuilderFactory factory = DbfFactory.getFactory();
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        ClassLoader loader = Util.getCurrentLoader(this);
        URL runtime = loader.getResource("com/sun/faces/jsf-ri-runtime.xml");
        URL renderkit = servletContext.getResource("/WEB-INF/renderkit1.xml");
        Document defaultDoc = builder.parse(loader.getResourceAsStream("com/sun/faces/jsf-ri-runtime.xml"));
        Document renderKitDoc = builder.parse(servletContext.getResourceAsStream("/WEB-INF/renderkit1.xml"));

        ConfigProcessor[] configProcessors = {
             new FactoryConfigProcessor(),
             new ApplicationConfigProcessor(),
             new RenderKitConfigProcessor(),
        };
        
        InjectionProvider containerConnector =
                InjectionProviderFactory.createInstance(ctx.getExternalContext());
        ctx.getAttributes().put("com.sun.faces.config.ConfigManager_INJECTION_PROVIDER_TASK", 
                containerConnector);
        
        for (int i=0; i<configProcessors.length; i++)
        configProcessors[i].process(servletContext, ctx, new DocumentInfo[] {
                                           new DocumentInfo(defaultDoc, new URI(runtime.toExternalForm())),
                                           new DocumentInfo(renderKitDoc, new URI(renderkit.toExternalForm())) });

        RenderKitFactory rkf = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk = rkf.getRenderKit(getFacesContext(), RenderKitFactory.HTML_BASIC_RENDER_KIT);
        assertEquals(rk.getClass().getName(), DecoratingRenderKit.class.getName());
        assertEquals(RenderKitImpl.class.getName(), ((RenderKitWrapper) rk).getWrapped().getClass().getName());
    }


    // ---------------------------------------------------------- Nested Classes


    public static class DecoratingRenderKit extends RenderKitWrapper {

        private RenderKit delegate;

        public DecoratingRenderKit(RenderKit delegate) {
            this.delegate = delegate;
        }

        public RenderKit getWrapped() {
            return delegate;
        }
    }


} // end of class TestRenderKit
