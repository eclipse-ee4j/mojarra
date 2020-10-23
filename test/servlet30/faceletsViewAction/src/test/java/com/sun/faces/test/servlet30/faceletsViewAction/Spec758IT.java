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

package com.sun.faces.test.servlet30.faceletsViewAction;

import static java.lang.System.getProperty;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * Test cases for Facelets functionality
 */
public class Spec758IT {


    /**
     * Stores the web URL.
     */
    private String webUrl;
    
    /**
     * Stores the web webClient.
     */
    private WebClient webClient;
    

    /**
     * Setup before testing.
     * 
     * @throws Exception when a serious error occurs.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * Cleanup after testing.
     * 
     * @throws Exception when a serious error occurs.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Setup before testing.
     */
    @Before
    public void setUp() {
        webUrl = getProperty("integration.url");
        webClient = new WebClient();
    }


    // ------------------------------------------------------------ Test Methods


    /*
     * Added for issue 917.
     */
    @Test
    public void testViewParameters() throws Exception {
        doTestExtensionMapped(0);
        doTestExtensionMapped(1);
    }

    @Test
    public void testViewParametersValidation() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "page02.faces?id=0");
        assertTrue(page.asText().contains("Invalid headline. (The id parameter is not a positive number)"));
    }
    
    
    private void doTestExtensionMapped(int i) throws Exception {

        int storyNum = i + 1;
        HtmlPage page = null;
        
        page = fetchHomePageAndClickStoryLink(i);

        page = fetchHomePageAndClickStoryLink(i);
        
        page = doRefreshButton(page, storyNum);
        
        page = doRefreshClearParamButton(page, storyNum);

        page = fetchHomePageAndClickStoryLink(i);
        
        page = doRefreshWithRedirectParamsButton(page, storyNum);
        
        page = fetchHomePageAndClickStoryLink(i);
        
        page = doRefreshWithoutRedirectParamsButton(page, storyNum);
        
        page = fetchHomePageAndClickStoryLink(i);

        page = doHomeButton(page, storyNum);
        
        page = fetchHomePageAndClickStoryLink(i);

        page = doHomeKeepSelectionButton(page, i);
        
        page = fetchHomePageAndClickStoryLink(i);

        page = doHomeKeepSelectionNavCaseButton(page, i);
        
        page = fetchHomePageAndClickStoryLink(i);

        page = doStory2Button(page, i);
        
        
    }
    
    private HtmlPage fetchHomePageAndClickStoryLink(int i) throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "page01.faces") ;
        String pageText = page.asText();

        assertOnHomePage(pageText);
        
        List<HtmlAnchor> anchors = page.getByXPath("//a");
        HtmlAnchor toClick = anchors.get(i);
        page = (HtmlPage) toClick.click();
        
        int storyNum = i+1;
        
        // Assert some things about the content of the page
        pageText = page.asText();
        assertTrue(-1 != pageText.indexOf(getTitleContains(storyNum)));
        assertTrue(-1 != pageText.indexOf(getContentContains(storyNum)));
        
        return page;
    }
    
    private String getTitleContains(int storyNum) {
        String titleContains = "Story " + storyNum + " Headline:";
        return titleContains;
    }
    
    private String getContentContains(int storyNum) {
        String contentContains = "Story " + storyNum + " Content:";
        return contentContains;
    }
    
    private HtmlPage doRefreshButton(HtmlPage page, int storyNum) throws Exception {
        String pageText = null;
        
        // Click the "refresh" button, ensure the page refreshes properly
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("refresh");
        page = (HtmlPage) button.click();
        pageText = page.asText();
        assertTrue(-1 != pageText.indexOf(getTitleContains(storyNum)));
        assertTrue(-1 != pageText.indexOf(getContentContains(storyNum)));

        return page;
    }
    
    private HtmlPage doRefreshClearParamButton(HtmlPage page, int storyNum) throws Exception {
        String pageText = null;
        // Click the "refreshClearParam" button, ensure you get back
        // to the home page
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("refreshClearParam");
        page = (HtmlPage) button.click();
        pageText = page.asText();
        
        // no story content on home page
        assertTrue(-1 == pageText.indexOf(getContentContains(storyNum)));
        assertTrue(-1 != pageText.indexOf("You did not specify a headline. (The id parameter is missing)"));
        assertOnHomePage(pageText);
        return page;
    }
    
    private HtmlPage doRefreshWithRedirectParamsButton(HtmlPage page, int storyNum) throws Exception {
        // click the "refreshWithRedirectParams" button and make sure we're still
        // on the same page.
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("refreshWithRedirectParams");
        page = (HtmlPage) button.click();
        String pageText = page.asText();
        assertTrue(-1 != pageText.indexOf(getTitleContains(storyNum)));
        assertTrue(-1 != pageText.indexOf(getContentContains(storyNum)));
        
        return page;
    }
    
    private HtmlPage doRefreshWithoutRedirectParamsButton(HtmlPage page, int storyNum) throws Exception {
        String pageText = null;
        // Click the "refreshWithRedirect" button, ensure you get back
        // to the home page
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("refreshWithRedirect");
        page = (HtmlPage) button.click();
        pageText = page.asText();
        
        // no story content on home page
        assertTrue(-1 == pageText.indexOf(getContentContains(storyNum)));
        assertTrue(-1 != pageText.indexOf("You did not specify a headline. (The id parameter is missing)"));
        assertOnHomePage(pageText);
        return page;
    }

    private HtmlPage doHomeButton(HtmlPage page, int storyNum) throws Exception {
        String pageText = null;
        // Click the "home" button, ensure you get back
        // to the home page
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("home");
        page = (HtmlPage) button.click();
        pageText = page.asText();
        
        // no story content on the page, and no messages either
        assertTrue(-1 == pageText.indexOf(getContentContains(storyNum)));
        assertTrue(-1 == pageText.indexOf("The headline you requested does not exist."));
        assertTrue(-1 == pageText.indexOf("You did not specify a headline. (The id parameter is missing)"));
        assertOnHomePage(pageText);

        return page;
    }
    
    private HtmlPage doHomeKeepSelectionButton(HtmlPage page, int storyNum) throws Exception {
        String pageText = null;
        // Click the "homeKeepSelection" button, ensure you get back
        // to the home page with the proper story number
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("homeRememberSelection");
        page = (HtmlPage) button.click();
        pageText = page.asText();
        
        assertOnHomePage(pageText);
        assertTrue(-1 == pageText.indexOf("You just looked at story #" + storyNum + "."));
        
        
        return page;
    }
    
    private HtmlPage doHomeKeepSelectionNavCaseButton(HtmlPage page, int storyNum) throws Exception {
        String pageText = null;
        // Click the "homeKeepSelectionNavCase" button, ensure you get back
        // to the home page with the proper story number
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("homeRememberSelectionNavCase");
        page = (HtmlPage) button.click();
        pageText = page.asText();
        
        assertOnHomePage(pageText);
        assertTrue(-1 == pageText.indexOf("You just looked at story #" + storyNum + "."));
        
        
        return page;
    }
    
    private HtmlPage doStory2Button(HtmlPage page, int storyNum) throws Exception {
        String pageText = null;
        // Click the "story2" button, ensure you get
        // to the story 2 page
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("story2RememberSelectionNavCase");
        page = (HtmlPage) button.click();
        pageText = page.asText();
        
        assertTrue(-1 != pageText.indexOf("Story 2"));
        assertTrue(-1 != pageText.indexOf("bar is: foo"));
        
        return page;
    }
    
    private void assertOnHomePage(String pageText) throws Exception {
        assertTrue(-1 != pageText.indexOf("The big news stories of the day"));
    }

}
