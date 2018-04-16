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

package com.sun.faces.facelets.tag.jsf.html;

/**
 * @author Jacob Hookom
 */
public final class HtmlLibrary extends AbstractHtmlLibrary {

    public final static String Namespace = "http://java.sun.com/jsf/html";
    public final static String XMLNSNamespace = "http://xmlns.jcp.org/jsf/html";

    public final static HtmlLibrary Instance = new HtmlLibrary();

    public HtmlLibrary() {
        this(Namespace);
    }
    
    public HtmlLibrary(String namespace) {
        super(namespace);
        
        this.addHtmlComponent("body", "javax.faces.OutputBody",
                "javax.faces.Body");

        this.addHtmlComponent("button", "javax.faces.HtmlOutcomeTargetButton",
                "javax.faces.Button");

        this.addHtmlComponent("column", "javax.faces.Column", null);

        this.addHtmlComponent("commandButton", "javax.faces.HtmlCommandButton",
                "javax.faces.Button");

        this.addHtmlComponent("commandLink", "javax.faces.HtmlCommandLink",
                "javax.faces.Link");

        this.addHtmlComponent("commandScript", "javax.faces.HtmlCommandScript",
                "javax.faces.Script");

        this.addHtmlComponent("dataTable", "javax.faces.HtmlDataTable",
                "javax.faces.Table");

        this.addHtmlComponent("form", "javax.faces.HtmlForm",
                "javax.faces.Form");

        this.addHtmlComponent("graphicImage", "javax.faces.HtmlGraphicImage",
                "javax.faces.Image");

        this.addHtmlComponent("head", "javax.faces.Output",
                "javax.faces.Head");

        this.addHtmlComponent("html", "javax.faces.Output",
                "javax.faces.Html");

        this.addHtmlComponent("doctype", "javax.faces.Output",
                "javax.faces.Doctype");

        this.addHtmlComponent("inputFile", "javax.faces.HtmlInputFile",
                "javax.faces.File");

        this.addHtmlComponent("inputHidden", "javax.faces.HtmlInputHidden",
                "javax.faces.Hidden");

        this.addHtmlComponent("inputSecret", "javax.faces.HtmlInputSecret",
                "javax.faces.Secret");

        this.addHtmlComponent("inputText", "javax.faces.HtmlInputText",
                "javax.faces.Text");

        this.addHtmlComponent("inputTextarea", "javax.faces.HtmlInputTextarea",
                "javax.faces.Textarea");

        this.addHtmlComponent("link", "javax.faces.HtmlOutcomeTargetLink",
                "javax.faces.Link");

        this.addHtmlComponent("message", "javax.faces.HtmlMessage",
                "javax.faces.Message");

        this.addHtmlComponent("messages", "javax.faces.HtmlMessages",
                "javax.faces.Messages");

        this.addHtmlComponent("outputFormat", "javax.faces.HtmlOutputFormat",
                "javax.faces.Format");

        this.addHtmlComponent("outputLabel", "javax.faces.HtmlOutputLabel",
                "javax.faces.Label");

        this.addHtmlComponent("outputLink", "javax.faces.HtmlOutputLink",
                "javax.faces.Link");

        this.addHtmlComponent("outputText", "javax.faces.HtmlOutputText",
                "javax.faces.Text");
        
        this.addComponent("outputScript",
                          "javax.faces.Output",
                          "javax.faces.resource.Script",
                          ScriptResourceHandler.class);

        this.addComponent("outputStylesheet",
                          "javax.faces.Output",
                          "javax.faces.resource.Stylesheet",
                          StylesheetResourceHandler.class);

        this.addHtmlComponent("panelGrid", "javax.faces.HtmlPanelGrid",
                "javax.faces.Grid");

        this.addHtmlComponent("panelGroup", "javax.faces.HtmlPanelGroup",
                "javax.faces.Group");

        this.addHtmlComponent("selectBooleanCheckbox",
                              "javax.faces.HtmlSelectBooleanCheckbox",
                              "javax.faces.Checkbox");

        this.addHtmlComponent("selectManyCheckbox",
                "javax.faces.HtmlSelectManyCheckbox", "javax.faces.Checkbox");

        this.addHtmlComponent("selectManyListbox",
                "javax.faces.HtmlSelectManyListbox", "javax.faces.Listbox");

        this.addHtmlComponent("selectManyMenu",
                "javax.faces.HtmlSelectManyMenu", "javax.faces.Menu");

        this.addHtmlComponent("selectOneListbox",
                "javax.faces.HtmlSelectOneListbox", "javax.faces.Listbox");

        this.addHtmlComponent("selectOneMenu", "javax.faces.HtmlSelectOneMenu",
                "javax.faces.Menu");

        this.addHtmlComponent("selectOneRadio",
                "javax.faces.HtmlSelectOneRadio", "javax.faces.Radio");

        this.addHtmlComponent("title", "javax.faces.Output",
                "javax.faces.Title");
    }

}
