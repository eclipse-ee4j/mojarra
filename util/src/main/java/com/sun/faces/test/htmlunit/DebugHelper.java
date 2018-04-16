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

package com.sun.faces.test.htmlunit;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;

public class DebugHelper extends WebConnectionWrapper {

    private final String urlFragmentToMonitor;
    private String rawRequestBody;
    private String rawResponse;
    private WebClient webClient;
    

    public DebugHelper(WebClient webClient, String urlFragmentToMonitor) {
        super(webClient);
        this.urlFragmentToMonitor = urlFragmentToMonitor;
        this.webClient = webClient;
    }

    @Override
    public WebResponse getResponse(WebRequest request) throws IOException {

        if (urlFragmentToMonitor == null || request.getUrl().toString().contains(urlFragmentToMonitor)) {
            rawRequestBody = request.getRequestBody();

            WebResponse response = super.getResponse(request);

            rawResponse = response.getContentAsString();
            
            return response;
        }

        return super.getResponse(request);
    }
    
    public void sleep() {
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void print(HtmlPage page, String description) {
        System.out.println("\n\n\n ************* " + description + " ********************");
        print(page);
    }
    
    public void print(HtmlPage page) {
        System.out.println("\n\n\n RAW REQUEST BODY \n" + getRawRequestBody());
        System.out.println("\n\n\n RAW RESPONSE \n" + getRawResponse());
        
        System.out.println("Response Headers: " + page.getWebResponse().getResponseHeaders());
        
        System.out.println("\n\n\n ********************************* \n" + page.asXml());
        
        System.out.println("\n\n\n **************************** \n" + webClient.getCookieManager().getCookies());
        
        System.out.println("\n\n\n *********************************");
    }
    
    public String getRawRequestBody() {
        return rawRequestBody;
    }

    public String getRawResponse() {
        return rawResponse;
    }
   
}
