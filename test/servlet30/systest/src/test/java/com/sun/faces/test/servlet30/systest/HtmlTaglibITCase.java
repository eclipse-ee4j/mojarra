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

package com.sun.faces.test.servlet30.systest;

import org.junit.Ignore;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import junit.framework.Test;
import junit.framework.TestSuite;


@Ignore("Does not really test key aspects but compares against somewhat arbitrarily rendendered output")
public class HtmlTaglibITCase extends HtmlUnitFacesITCase {


    public HtmlTaglibITCase(String name) {
        super(name);
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "test04");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "test04");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "test04");


    }

    public static Test suite() {
        return (new TestSuite(HtmlTaglibITCase.class));
    }

    public void test01() throws Exception {

        HtmlPage page = getPage("/faces/taglib/commandButton_test.jsp");
        assertTrue(page.asXml().matches("(?s).*\\s*<body>\\s*<form\\s*id=\"form01\"\\s*name=\"form01\"\\s*method=\"post\"\\saction=\"/jsf-systest/faces/taglib/commandButton_test.jsp.*enctype=\"application/x-www-form-urlencoded\">\\s*<input\\s*type=\"hidden\"\\s*name=\"form01\"\\s*value=\"form01\"\\s*/>\\s*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\"\\s*id=\".*javax.faces.ViewState.*\"\\s*value=\".*\"\\s*/>\\s*<input\\s*id=\"form01:button01\"\\s*type=\"submit\"\\s*name=\"form01:button01\"\\s*value=\"My Label\"\\s*/>\\s*<input\\s*id=\"form01:button02\"\\s*type=\"reset\"\\s*name=\"form01:button02\"\\s*value=\"This\\s*is\\s*a\\s*String\\s*property\"\\s*/>\\s*<input\\s*id=\"form01:button03\"\\s*type=\"submit\"\\s*name=\"form01:button03\"\\s*value=\"RES-BUNDLE\\s*KEY\"\\s*/>\\s*<input\\s*id=\"form01:button04\"\\s*type=\"image\"\\s*src=\"duke.gif.*name=\"form01:button04\"\\s*/>\\s*<input\\s*id=\"form01:button05\"\\s*type=\"image\"\\s*src=\"resbundle_image.gif.*name=\"form01:button05\"\\s*/>\\s*<input\\s*id=\"form01:button06\"\\s*type=\"image\"\\s*src=\"My.*name=\"form01:button06\"\\s*onclick=\"hello().*/>\\s*</form>\\s*</body>.*"));

    }

    public void test02() throws Exception {

        HtmlPage page = getPage("/faces/taglib/commandButton_param_test.jsp");
        assertTrue(page.asXml().matches("(?s).*\\s*<body>\\s*<form\\s*id=\"form01\"\\s*name=\"form01\"\\s*method=\"post\"\\saction=\"/jsf-systest/faces/taglib/commandButton_param_test.jsp.*enctype=\"application/x-www-form-urlencoded\">\\s*<input\\s*type=\"hidden\"\\s*name=\"form01\"\\s*value=\"form01\"\\s*/>\\s*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\"\\s*id=\".*javax.faces.ViewState.*\"\\s*value=\".*\"\\s*/>\\s*<script\\s*type=\"text/javascript\"\\s*src=\"/jsf-systest/faces/javax.faces.resource/jsf.js.*</script>\\s*<input\\s*id=\"form01:button01\"\\s*type=\"submit\"\\s*name=\"form01:button01\"\\s*value=\"Label\"\\s*onclick=\"mojarra.jsfcljs\\(document.getElementById\\(&apos;form01&apos;\\),\\{&apos;form01:button01&apos;:&apos;form01:button01&apos;,&apos;testname&apos;:&apos;testval&apos;\\},&apos;&apos;\\);return false\"\\s*/>\\s*Test Link\\s*<p>\\s*<input\\s*id=\"form01:button02\"\\s*type=\"submit\"\\s*name=\"form01:button02\"\\s*value=\"Label\"\\s*/>\\s*Test Link\\s*</p>\\s*<p>\\s*<input\\s*id=\"form01:button03\"\\s*type=\"submit\"\\s*name=\"form01:button03\"\\s*value=\"Label\"\\s*onclick=\"mojarra.jsfcljs\\(document.getElementById\\(&apos;form01&apos;\\),\\{&apos;form01:button03&apos;:&apos;form01:button03&apos;,&apos;testname2&apos;:&apos;This\\s*is\\s*a\\s*String\\s*property&apos;\\},&apos;&apos;\\);return false\"\\s*/>\\s*Test Link\\s*</p>\\s*<p>\\s*<input\\s*id=\"form01:button04\"\\s*type=\"submit\"\\s*name=\"form01:button04\"\\s*value=\"Label\"\\s*onclick=\"jsf.util.chain\\(this,event,&apos;hello\\(\\);&apos;,&apos;mojarra.jsfcljs\\(document.getElementById\\(.*&apos;form01.*&apos;\\),\\{.*&apos;form01:button04.*&apos;:.*&apos;form01:button04.*&apos;,.*&apos;testname.*&apos;:.*&apos;testval.*&apos;\\},.*&apos;.*&apos;\\)&apos;\\);return false\"\\s*/>\\s*Test Link\\s*</p>\\s*</form>\\s*</body>.*"));

    }

    public void test03() throws Exception {

        HtmlPage page = getPage("/faces/taglib/commandLink_test.jsp");
        assertTrue(page.asXml().matches("(?s).*\\s*<body>\\s*<form\\s*id=\"form01\"\\s*name=\"form01\"\\s*method=\"post\"\\saction=\"/jsf-systest/faces/taglib/commandLink_test.jsp.*enctype=\"application/x-www-form-urlencoded\">\\s*<input\\s*type=\"hidden\"\\s*name=\"form01\"\\s*value=\"form01\"\\s*/>\\s*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\"\\s*id=\".*javax.faces.ViewState.*\"\\s*value=\".*\"\\s*/>\\s*<script\\s*type=\"text/javascript\"\\s*src=\"/jsf-systest/faces/javax.faces.resource/jsf.js.*</script>\\s*<a\\s*id=\"form01:hyperlink01\"\\s*href=\"#\"\\s*onclick=\"mojarra.jsfcljs\\(document.getElementById\\(&apos;form01&apos;\\),\\{&apos;form01:hyperlink01&apos;:&apos;form01:hyperlink01&apos;\\},&apos;&apos;\\);return false\"\\s*>\\s*My Link\\s*</a>\\s*<a\\s*id=\"form01:hyperlink02\"\\s*href=\"#\"\\s*onclick=\"mojarra.jsfcljs\\(document.getElementById\\(&apos;form01&apos;\\),\\{&apos;form01:hyperlink02&apos;:&apos;form01:hyperlink02&apos;\\},&apos;&apos;\\);return false\">\\s*This\\s*is\\s*a\\s*String\\s*property\\s*</a>\\s*<a\\s*id=\"form01:hyperlink03\"\\s*href=\"#\"\\s*onclick=\"mojarra.jsfcljs\\(document.getElementById\\(&apos;form01&apos;\\),\\{&apos;form01:hyperlink03&apos;:&apos;form01:hyperlink03&apos;\\},&apos;&apos;\\);return false\">\\s*RES-BUNDLE LINK\\s*</a>\\s*<a\\s*id=\"form01:hyperlink04\"\\s*href=\"#\"\\s*onclick=\"mojarra.jsfcljs\\(document.getElementById\\(&apos;form01&apos;\\),\\{&apos;form01:hyperlink04&apos;:&apos;form01:hyperlink04&apos;\\},&apos;&apos;\\);return false\">\\s*<img\\s*src=\"duke.gif\"\\s*/>\\s*</a>\\s*<a\\s*id=\"form01:hyperlink05\"\\s*href=\"#\"\\s*onclick=\"mojarra.jsfcljs\\(document.getElementById\\(&apos;form01&apos;\\),\\{&apos;form01:hyperlink05&apos;:&apos;form01:hyperlink05&apos;\\},&apos;&apos;\\);return false\">\\s*<img\\s*src=\"resbundle_image.*/>\\s*</a>\\s*<a\\s*id=\"form01:hyperlink06\"\\s*href=\"#\"\\s*onclick=\"mojarra.jsfcljs\\(document.getElementById\\(&apos;form01&apos;\\),\\{&apos;form01:hyperlink06&apos;:&apos;form01:hyperlink06&apos;,&apos;param1&apos;:&apos;value1&apos;\\},&apos;&apos;\\);return false\">\\s*Paramter Link\\s*</a>\\s*</form>\\s*</body>.*"));


    }

    public void test04() throws Exception {

        HtmlPage page = getPage("/faces/taglib/commandLink_multiform_test.jsp");
        assertTrue(page.asXml().matches("(?s).*\\s*<body>\\s*<form\\s*id=\"form01\"\\s*name=\"form01\".*action.*commandLink_multiform_test.jsp.*<input\\s*type=\"hidden\"\\s*name=\"form01\"\\s*value=\"form01\"\\s*/>\\s*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState.*<script.*src=.*jsf.js.*<a\\s*id=\"form01:Link1.*onclick=\"mojarra.jsfcljs\\(document.getElementById.*form01.*form01:Link1.*form01:Link1.*;param1.*value1.*param2.*value2.*return false.*Link1.*</a>\\s*<a\\s*id=\"form01:Link2.*onclick=\"mojarra.jsfcljs\\(document.getElementById.*form01.*,.*form01:Link2.*form01:Link2.*.*param1.*value1.*,.*param2.*value2.*Link2.*</a>.*</form>.*<form\\s*id=\"form02\"\\s*name=\"form02\".*action.*commandLink_multiform_test.jsp.*<input\\s*type=\"hidden\"\\s*name=\"form02\"\\s*value=\"form02\"\\s*/>\\s*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState.*<a\\s*id=\"form02:Link3.*onclick=\"mojarra.jsfcljs\\(document.getElementById.*form02.*form02:Link3.*form02:Link3.*,.*param3.*value3.*,.*param4.*value4.*Link3.*</a>.*<a\\s*id=\"form02:Link4.*onclick=\"mojarra.jsfcljs\\(document.getElementById.*form02.*form02:Link4.*form02:Link4.*.*Link4.*</a>.*<a.*onclick=\"mojarra.jsfcljs\\(document.getElementById.*form02.*form02:.*form02:.*param5.*Link5.*</a>.*"));

    }

    public void test05() throws Exception {

        HtmlPage page = getPage("/faces/taglib/attributeTest.jsp");
        assertTrue(page.asXml().matches("(?s).*\\s*<body>.*f:attribute.*<span\\s*style=\"color:\\s*red\">.*This Should Be Red.*New String Value.*"));

    }
}
