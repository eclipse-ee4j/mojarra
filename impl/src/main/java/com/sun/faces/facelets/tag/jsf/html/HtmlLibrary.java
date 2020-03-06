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

        this.addHtmlComponent("body", "jakarta.faces.OutputBody", "jakarta.faces.Body");

        this.addHtmlComponent("button", "jakarta.faces.HtmlOutcomeTargetButton", "jakarta.faces.Button");

        this.addHtmlComponent("column", "jakarta.faces.Column", null);

        this.addHtmlComponent("commandButton", "jakarta.faces.HtmlCommandButton", "jakarta.faces.Button");

        this.addHtmlComponent("commandLink", "jakarta.faces.HtmlCommandLink", "jakarta.faces.Link");

        this.addHtmlComponent("commandScript", "jakarta.faces.HtmlCommandScript", "jakarta.faces.Script");

        this.addHtmlComponent("dataTable", "jakarta.faces.HtmlDataTable", "jakarta.faces.Table");

        this.addHtmlComponent("form", "jakarta.faces.HtmlForm", "jakarta.faces.Form");

        this.addHtmlComponent("graphicImage", "jakarta.faces.HtmlGraphicImage", "jakarta.faces.Image");

        this.addHtmlComponent("head", "jakarta.faces.Output", "jakarta.faces.Head");

        this.addHtmlComponent("html", "jakarta.faces.Output", "jakarta.faces.Html");

        this.addHtmlComponent("doctype", "jakarta.faces.Output", "jakarta.faces.Doctype");

        this.addHtmlComponent("inputFile", "jakarta.faces.HtmlInputFile", "jakarta.faces.File");

        this.addHtmlComponent("inputHidden", "jakarta.faces.HtmlInputHidden", "jakarta.faces.Hidden");

        this.addHtmlComponent("inputSecret", "jakarta.faces.HtmlInputSecret", "jakarta.faces.Secret");

        this.addHtmlComponent("inputText", "jakarta.faces.HtmlInputText", "jakarta.faces.Text");

        this.addHtmlComponent("inputTextarea", "jakarta.faces.HtmlInputTextarea", "jakarta.faces.Textarea");

        this.addHtmlComponent("link", "jakarta.faces.HtmlOutcomeTargetLink", "jakarta.faces.Link");

        this.addHtmlComponent("message", "jakarta.faces.HtmlMessage", "jakarta.faces.Message");

        this.addHtmlComponent("messages", "jakarta.faces.HtmlMessages", "jakarta.faces.Messages");

        this.addHtmlComponent("outputFormat", "jakarta.faces.HtmlOutputFormat", "jakarta.faces.Format");

        this.addHtmlComponent("outputLabel", "jakarta.faces.HtmlOutputLabel", "jakarta.faces.Label");

        this.addHtmlComponent("outputLink", "jakarta.faces.HtmlOutputLink", "jakarta.faces.Link");

        this.addHtmlComponent("outputText", "jakarta.faces.HtmlOutputText", "jakarta.faces.Text");

        this.addComponent("outputScript", "jakarta.faces.Output", "jakarta.faces.resource.Script", ScriptResourceHandler.class);

        this.addComponent("outputStylesheet", "jakarta.faces.Output", "jakarta.faces.resource.Stylesheet", StylesheetResourceHandler.class);

        this.addHtmlComponent("panelGrid", "jakarta.faces.HtmlPanelGrid", "jakarta.faces.Grid");

        this.addHtmlComponent("panelGroup", "jakarta.faces.HtmlPanelGroup", "jakarta.faces.Group");

        this.addHtmlComponent("selectBooleanCheckbox", "jakarta.faces.HtmlSelectBooleanCheckbox", "jakarta.faces.Checkbox");

        this.addHtmlComponent("selectManyCheckbox", "jakarta.faces.HtmlSelectManyCheckbox", "jakarta.faces.Checkbox");

        this.addHtmlComponent("selectManyListbox", "jakarta.faces.HtmlSelectManyListbox", "jakarta.faces.Listbox");

        this.addHtmlComponent("selectManyMenu", "jakarta.faces.HtmlSelectManyMenu", "jakarta.faces.Menu");

        this.addHtmlComponent("selectOneListbox", "jakarta.faces.HtmlSelectOneListbox", "jakarta.faces.Listbox");

        this.addHtmlComponent("selectOneMenu", "jakarta.faces.HtmlSelectOneMenu", "jakarta.faces.Menu");

        this.addHtmlComponent("selectOneRadio", "jakarta.faces.HtmlSelectOneRadio", "jakarta.faces.Radio");

        this.addHtmlComponent("title", "jakarta.faces.Output", "jakarta.faces.Title");
    }

}
