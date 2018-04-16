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

package com.sun.faces.test.cluster.servlet25.flash.reaper;

import com.sun.faces.test.util.ClusterUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import org.junit.Ignore;

/**
  *
 */
public class FlashReaperIT {

    private WebClient webClient;

    @Before
    public void setUp() {
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }


    // ------------------------------------------------------------ Test Methods


    @Test
    @Ignore
    public void testFlashesAreReaped() throws Exception {
        
        doTestFlashesAreReaped(0);
        doTestFlashesAreReaped(1);
                
    }
    
    public void doTestFlashesAreReaped(int instanceNumber) throws Exception {
        
        String [] baseUrls = ClusterUtils.getBaseUrls();
        
        HtmlPage page;
        int numberOfReaps = 0, numberEntriesInInnerMap = 0;
        boolean didReap = false;

        for (int i = 0; i < 50; i++) {
            WebClient zombieClient = new WebClient();
            HtmlPage zombiePage = zombieClient.getPage(baseUrls[instanceNumber] + "faces/flashReaper.xhtml");
            System.out.println(zombiePage.asXml());
            
            page = webClient.getPage(baseUrls[instanceNumber] + "faces/flashReaper.xhtml");

            numberEntriesInInnerMap = Integer.parseInt(page.asText().trim());
            // When we move across instance numbers as done in this test, the
            // entries from the first instance are never reaped because 
            // we are not interleaving requests to each instance number.
            // Instead we are making N requests to instance1 and then N
            // requests to instance2.  Therefore, consider the reaping
            // boundary based on the instance number
            if (numberEntriesInInnerMap <= 
                ((1+instanceNumber)*FlashReaperBean.NUMBER_OF_ZOMBIES)) {
                didReap = true;
                numberOfReaps++;
            }
        }

        assertTrue(didReap);
        assertTrue(2 < numberOfReaps);
    }
}
