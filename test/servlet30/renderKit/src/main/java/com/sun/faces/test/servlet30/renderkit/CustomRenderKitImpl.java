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

// CustomRenderKitImpl.java

package com.sun.faces.test.servlet30.renderkit;

import com.sun.faces.util.MessageUtils;
import com.sun.faces.renderkit.ResponseStateManagerImpl;

import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import javax.faces.render.ResponseStateManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;

/**
 * <B>CustomRenderKitImpl</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class CustomRenderKitImpl extends RenderKit {

//
// Protected Constants
//

//
// Class Variables
//

//
// Instance Variables
//
    // used for ResponseWriter creation;
    private final static String HTML_CONTENT_TYPE = "text/html";
    private final static String CHAR_ENCODING = "ISO-8859-1";
//
// Ivars used during actual client lifetime
//

// Relationship Instance Variables

    /**
     * Keys are String renderer family.  Values are HashMaps.  Nested
     * HashMap keys are Strings for the rendererType, and values are the
     * Renderer instances themselves.
     */

    private HashMap rendererFamilies;

    private ResponseStateManager responseStateManager = null;
//
// Constructors and Initializers    
//

    public CustomRenderKitImpl() {
        super();
	rendererFamilies = new HashMap();
    }


    //
    // Class methods
    //

    //
    // General Methods
    //

    //
    // Methods From RenderKit
    //

    public void addRenderer(String family, String rendererType,
                            Renderer renderer) {
        if (family == null) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "family");
            throw new NullPointerException(message);
                
        }
        if (rendererType == null) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "rendererType");
            throw new NullPointerException(message);
                
        }
        if (renderer == null) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "renderer");
            throw new NullPointerException(message);
                
        }
        HashMap renderers = null;

        synchronized (rendererFamilies) {
	    // PENDING(edburns): generics would be nice here.
	    if (null == (renderers = (HashMap) rendererFamilies.get(family))) {
		rendererFamilies.put(family, renderers = new HashMap());
	    }
            renderers.put(rendererType, renderer);
        }
    }


    public Renderer getRenderer(String family, String rendererType) {

        if (rendererType == null) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "rendererType");
            throw new NullPointerException(message);
        }
        if (family == null) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "family");
            throw new NullPointerException(message);
        }

        assert (rendererFamilies != null);

        HashMap renderers = null;
        Renderer renderer = null;

	if (null != (renderers = (HashMap) rendererFamilies.get(family))) {
	    renderer = (Renderer) renderers.get(rendererType);
	}
	
        return renderer;
    }


    public synchronized ResponseStateManager getResponseStateManager() {
        if (responseStateManager == null) {
            responseStateManager = new ResponseStateManagerImpl();
        }
        return responseStateManager;
    }


    public ResponseWriter createResponseWriter(Writer writer, String contentTypeList,
                                               String characterEncoding) {
        if (writer == null) {
            return null;
        }
        // Set the default content type to html;  However, if a content type list
        // argument was specified, make sure it contains an html content type;
        // PENDING(rogerk) ideally, we want to analyze the content type string
        // in more detail, to determine the preferred content type - as outlined in
        // http://www.ietf.org/rfc/rfc2616.txt?number=2616 - Section 14.1
        // (since this is not an html renderkit);
        //
        String contentType = HTML_CONTENT_TYPE;
        if (contentTypeList != null) {
            if (contentTypeList.indexOf(contentType) < 0) {
                throw new IllegalArgumentException(MessageUtils.getExceptionMessageString(
                    MessageUtils.CONTENT_TYPE_ERROR_MESSAGE_ID));
            }
        }
        if (characterEncoding == null) {
            characterEncoding = CHAR_ENCODING;
        }

        return new CustomResponseWriter(writer, contentType, characterEncoding);
    }


    public ResponseStream createResponseStream(OutputStream out) {
        final OutputStream output = out;
        return new ResponseStream() {
            public void write(int b) throws IOException {
                output.write(b);
            }


            public void write(byte b[]) throws IOException {
                output.write(b);
            }


            public void write(byte b[], int off, int len) throws IOException {
                output.write(b, off, len);
            }


            public void flush() throws IOException {
                output.flush();
            }


            public void close() throws IOException {
                output.close();
            }
        };
    }       
    // The test for this class is in TestRenderKit.java

} // end of class CustomRenderKitImpl

