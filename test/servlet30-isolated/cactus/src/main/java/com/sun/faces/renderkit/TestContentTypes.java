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

package com.sun.faces.renderkit;

import com.sun.faces.cactus.ServletFacesTestCase;

import org.apache.cactus.WebRequest;

public class TestContentTypes extends ServletFacesTestCase {

//
// Constructors and Initializers
//
                                                                                                                   
    public TestContentTypes() {
        super("TestContentTypes");
    }
                                                                                                                   
    public TestContentTypes(String name) {
        super(name);
    }

    /**
     * quality test - "text/html" wins
     */
    public void beginAccept1(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/plain; q=0.5, text/html, text/x-dvi; q=0.8, text/x-c");
    }
    public void testAccept1() throws Exception {
        String clientContentType = getFacesContext().getExternalContext().getRequestHeaderMap().get("Accept");
        String serverSupportedContentTypes = "text/html, text/plain";
        String contentType = RenderKitUtils.determineContentType(
            clientContentType, serverSupportedContentTypes, null);
        assertEquals(contentType, "text/html");
    }

    /**
     * quality test - "text/x-dvi" wins
     */
    public void beginAccept2(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/plain; q=0.5, text/html, text/x-dvi; q=0.8, text/x-c");
    }
    public void testAccept2() throws Exception {
        String clientContentType = getFacesContext().getExternalContext().getRequestHeaderMap().get("Accept");
        String serverSupportedContentTypes = "text/x-dvi, text/plain";
        String contentType = RenderKitUtils.determineContentType(
            clientContentType, serverSupportedContentTypes, null);
        assertEquals(contentType, "text/x-dvi");
    }

    /**
     * "level" precedence test - "text/html;level=1" is higher than "text/html" 
     */
    public void beginAccept3(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/plain; q=0.5, text/html, text/html;level=1");
    }
    public void testAccept3() throws Exception {
        String clientContentType = getFacesContext().getExternalContext().getRequestHeaderMap().get("Accept");
        String serverSupportedContentTypes = "text/html, text/html;level=1";
        String contentType = RenderKitUtils.determineContentType(
            clientContentType, serverSupportedContentTypes, null);
        assertEquals(contentType, "text/html;level=1");
    }
 
    /**
     * "level" precedence test - "text/html;level=2" is higher than "text/html;level=1"" 
     */
    public void beginAccept4(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/plain; q=0.5, text/html, text/html;level=1, text/html;level=2");
    }
                                                                                                                           
    public void testAccept4() throws Exception {
        String clientContentType = getFacesContext().getExternalContext().getRequestHeaderMap().get("Accept");
        String serverSupportedContentTypes = "text/html, text/html;level=1, text/html;level=2";
        String contentType = RenderKitUtils.determineContentType(
            clientContentType, serverSupportedContentTypes, null);
        assertEquals(contentType, "text/html;level=2");
    }

    public void beginAccept5(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/html, application/xhtml+xml");
    }

    public void testAccept5() throws Exception {
        String clientContentType = getFacesContext().getExternalContext().getRequestHeaderMap().get("Accept");
        String serverSupportedContentTypes = "text/html, application/xhtml+xml";
        String contentType = RenderKitUtils.determineContentType(
            clientContentType, serverSupportedContentTypes, "application/xhtml+xml");
        assertEquals(contentType, "application/xhtml+xml");
    }

    public void beginAccept6(WebRequest theRequest) {
        theRequest.addHeader("Accept", "text/html, application/xhtml+xml; q=0.5");
    }

    public void testAccept6() throws Exception {
        String clientContentType = getFacesContext().getExternalContext().getRequestHeaderMap().get("Accept");
        String serverSupportedContentTypes = "text/html, application/xhtml+xml";
        String contentType = RenderKitUtils.determineContentType(
            clientContentType, serverSupportedContentTypes, "application/xhtml+xml");
        assertEquals(contentType, "text/html");
    }

     public void testReallyLargeAcceptHeader() throws Exception {
        String clientAcceptHeader = "application/octet-stream, application/smil, application/vnd.oma.drm.content,"
                                    + "application/vnd.oma.drm.message, application/vnd.oma.drm.rights+wbxml,"
                                    + "application/vnd.oma.drm.rights+xml, application/vnd.wap.connectivity-wbxml,"
                                    + "application/vnd.wap.multipart.mixed, application/vnd.wap.multipart.related,"
                                    + "application/vnd.wap.wmlscriptc, application/vnd.wap.xhtml+xml,"
                                    + "application/xhtml+xml;profile=\"http://www.wapforum.org/xhtml\", image/bmp, image/gif,"
                                    + "image/jpeg, image/png, image/vnd.wap.wbmp, multipart/mixed, multipart/related, text/html,"
                                    + "text/plain, text/vnd.wap.connectivity-xml, text/vnd.wap.wml;type=4365, application/java,"
                                    + "application/java-archive, image/wbmp, text/vcalendar, text/vcard, video/3gpp, video/mpeg,"
                                    + "audio/amr, audio/xmf, audio/x-midi, audio/x-mid, audio/x-wav, audio/imelody, text/x-imelody,"
                                    + "audio/mp3, audio/mpeg, audio/mpeg3, audio/mpg3, audio/aac, audio/amr-wb, audio/mp4,"
                                    + "pv-pvx, application/sdp, image/svg+xml, text/vnd.sun.j2me.app-descriptor, \n"
                                    + "video/x-application/vnd.oma.dd+xml,text/vnd.wap.wmlscript,text/vnd.wap.wml";
       String serverSupportedContentTypes = "text/html, application/xhtml+xml";
        String contentType =
              RenderKitUtils.determineContentType(clientAcceptHeader,
                                                  serverSupportedContentTypes,
                                                  "text/html");
        assertEquals(contentType, "text/html");
    }
}
