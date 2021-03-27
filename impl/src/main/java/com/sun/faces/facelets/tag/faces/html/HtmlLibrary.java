/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.tag.faces.html;

import static com.sun.faces.util.Util.unmodifiableSet;

import java.util.Set;

/**
 * @author Jacob Hookom
 */
public final class HtmlLibrary extends AbstractHtmlLibrary {

    private final static String SunNamespace = "http://java.sun.com/jsf/html";
    private final static String JcpNamespace = "http://xmlns.jcp.org/jsf/html";
    private final static String JakartaNamespace = "jakarta.faces.html";

    public final static Set<String> NAMESPACES = unmodifiableSet(JakartaNamespace, JcpNamespace, SunNamespace);
    public final static String DEFAULT_NAMESPACE = JakartaNamespace;

    public HtmlLibrary(String namespace) {
        super(namespace);

        addHtmlComponent("body", "jakarta.faces.OutputBody", "jakarta.faces.Body");

        addHtmlComponent("button", "jakarta.faces.HtmlOutcomeTargetButton", "jakarta.faces.Button");

        addHtmlComponent("column", "jakarta.faces.Column", null);

        addHtmlComponent("commandButton", "jakarta.faces.HtmlCommandButton", "jakarta.faces.Button");

        addHtmlComponent("commandLink", "jakarta.faces.HtmlCommandLink", "jakarta.faces.Link");

        addHtmlComponent("commandScript", "jakarta.faces.HtmlCommandScript", "jakarta.faces.Script");

        addHtmlComponent("dataTable", "jakarta.faces.HtmlDataTable", "jakarta.faces.Table");

        addHtmlComponent("form", "jakarta.faces.HtmlForm", "jakarta.faces.Form");

        addHtmlComponent("graphicImage", "jakarta.faces.HtmlGraphicImage", "jakarta.faces.Image");

        addHtmlComponent("head", "jakarta.faces.Output", "jakarta.faces.Head");

        addHtmlComponent("html", "jakarta.faces.Output", "jakarta.faces.Html");

        addHtmlComponent("doctype", "jakarta.faces.Output", "jakarta.faces.Doctype");

        addHtmlComponent("inputFile", "jakarta.faces.HtmlInputFile", "jakarta.faces.File");

        addHtmlComponent("inputHidden", "jakarta.faces.HtmlInputHidden", "jakarta.faces.Hidden");

        addHtmlComponent("inputSecret", "jakarta.faces.HtmlInputSecret", "jakarta.faces.Secret");

        addHtmlComponent("inputText", "jakarta.faces.HtmlInputText", "jakarta.faces.Text");

        addHtmlComponent("inputTextarea", "jakarta.faces.HtmlInputTextarea", "jakarta.faces.Textarea");

        addHtmlComponent("link", "jakarta.faces.HtmlOutcomeTargetLink", "jakarta.faces.Link");

        addHtmlComponent("message", "jakarta.faces.HtmlMessage", "jakarta.faces.Message");

        addHtmlComponent("messages", "jakarta.faces.HtmlMessages", "jakarta.faces.Messages");

        addHtmlComponent("outputFormat", "jakarta.faces.HtmlOutputFormat", "jakarta.faces.Format");

        addHtmlComponent("outputLabel", "jakarta.faces.HtmlOutputLabel", "jakarta.faces.Label");

        addHtmlComponent("outputLink", "jakarta.faces.HtmlOutputLink", "jakarta.faces.Link");

        addHtmlComponent("outputText", "jakarta.faces.HtmlOutputText", "jakarta.faces.Text");

        this.addComponent("outputScript", "jakarta.faces.Output", "jakarta.faces.resource.Script", ScriptResourceHandler.class);

        this.addComponent("outputStylesheet", "jakarta.faces.Output", "jakarta.faces.resource.Stylesheet", StylesheetResourceHandler.class);

        addHtmlComponent("panelGrid", "jakarta.faces.HtmlPanelGrid", "jakarta.faces.Grid");

        addHtmlComponent("panelGroup", "jakarta.faces.HtmlPanelGroup", "jakarta.faces.Group");

        addHtmlComponent("selectBooleanCheckbox", "jakarta.faces.HtmlSelectBooleanCheckbox", "jakarta.faces.Checkbox");

        addHtmlComponent("selectManyCheckbox", "jakarta.faces.HtmlSelectManyCheckbox", "jakarta.faces.Checkbox");

        addHtmlComponent("selectManyListbox", "jakarta.faces.HtmlSelectManyListbox", "jakarta.faces.Listbox");

        addHtmlComponent("selectManyMenu", "jakarta.faces.HtmlSelectManyMenu", "jakarta.faces.Menu");

        addHtmlComponent("selectOneListbox", "jakarta.faces.HtmlSelectOneListbox", "jakarta.faces.Listbox");

        addHtmlComponent("selectOneMenu", "jakarta.faces.HtmlSelectOneMenu", "jakarta.faces.Menu");

        addHtmlComponent("selectOneRadio", "jakarta.faces.HtmlSelectOneRadio", "jakarta.faces.Radio");

        addHtmlComponent("title", "jakarta.faces.Output", "jakarta.faces.Title");
    }

}
