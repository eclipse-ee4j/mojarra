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

import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlColumn;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.component.html.HtmlCommandScript;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.component.html.HtmlDoctype;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlGraphicImage;
import jakarta.faces.component.html.HtmlInputFile;
import jakarta.faces.component.html.HtmlInputHidden;
import jakarta.faces.component.html.HtmlInputSecret;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlInputTextarea;
import jakarta.faces.component.html.HtmlMessage;
import jakarta.faces.component.html.HtmlMessages;
import jakarta.faces.component.html.HtmlOutcomeTargetButton;
import jakarta.faces.component.html.HtmlOutcomeTargetLink;
import jakarta.faces.component.html.HtmlOutputFormat;
import jakarta.faces.component.html.HtmlOutputLabel;
import jakarta.faces.component.html.HtmlOutputLink;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.component.html.HtmlPanelGrid;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.component.html.HtmlSelectBooleanCheckbox;
import jakarta.faces.component.html.HtmlSelectManyCheckbox;
import jakarta.faces.component.html.HtmlSelectManyListbox;
import jakarta.faces.component.html.HtmlSelectManyMenu;
import jakarta.faces.component.html.HtmlSelectOneListbox;
import jakarta.faces.component.html.HtmlSelectOneMenu;
import jakarta.faces.component.html.HtmlSelectOneRadio;

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

        addHtmlComponent("body", HtmlBody.COMPONENT_TYPE, "jakarta.faces.Body");

        addHtmlComponent("button", HtmlOutcomeTargetButton.COMPONENT_TYPE, "jakarta.faces.Button");

        addHtmlComponent("column", HtmlColumn.COMPONENT_TYPE, null);

        addHtmlComponent("commandButton", HtmlCommandButton.COMPONENT_TYPE, "jakarta.faces.Button");

        addHtmlComponent("commandLink", HtmlCommandLink.COMPONENT_TYPE, "jakarta.faces.Link");

        addHtmlComponent("commandScript", HtmlCommandScript.COMPONENT_TYPE, "jakarta.faces.Script");

        addHtmlComponent("dataTable", HtmlDataTable.COMPONENT_TYPE, "jakarta.faces.Table");

        addHtmlComponent("form", HtmlForm.COMPONENT_TYPE, "jakarta.faces.Form");

        addHtmlComponent("graphicImage", HtmlGraphicImage.COMPONENT_TYPE, "jakarta.faces.Image");

        addHtmlComponent("head", UIOutput.COMPONENT_TYPE, "jakarta.faces.Head");

        addHtmlComponent("html", UIOutput.COMPONENT_TYPE, "jakarta.faces.Html");

        addHtmlComponent("doctype", HtmlDoctype.COMPONENT_TYPE, "jakarta.faces.Doctype");

        addHtmlComponent("inputFile", HtmlInputFile.COMPONENT_TYPE, "jakarta.faces.File");

        addHtmlComponent("inputHidden", HtmlInputHidden.COMPONENT_TYPE, "jakarta.faces.Hidden");

        addHtmlComponent("inputSecret", HtmlInputSecret.COMPONENT_TYPE, "jakarta.faces.Secret");

        addHtmlComponent("inputText", HtmlInputText.COMPONENT_TYPE, "jakarta.faces.Text");

        addHtmlComponent("inputTextarea", HtmlInputTextarea.COMPONENT_TYPE, "jakarta.faces.Textarea");

        addHtmlComponent("link", HtmlOutcomeTargetLink.COMPONENT_TYPE, "jakarta.faces.Link");

        addHtmlComponent("message", HtmlMessage.COMPONENT_TYPE, "jakarta.faces.Message");

        addHtmlComponent("messages", HtmlMessages.COMPONENT_TYPE, "jakarta.faces.Messages");

        addHtmlComponent("outputFormat", HtmlOutputFormat.COMPONENT_TYPE, "jakarta.faces.Format");

        addHtmlComponent("outputLabel", HtmlOutputLabel.COMPONENT_TYPE, "jakarta.faces.Label");

        addHtmlComponent("outputLink", HtmlOutputLink.COMPONENT_TYPE, "jakarta.faces.Link");

        addHtmlComponent("outputText", HtmlOutputText.COMPONENT_TYPE, "jakarta.faces.Text");

        this.addComponent("outputScript", UIOutput.COMPONENT_TYPE, "jakarta.faces.resource.Script", ScriptResourceHandler.class);

        this.addComponent("outputStylesheet", UIOutput.COMPONENT_TYPE, "jakarta.faces.resource.Stylesheet", StylesheetResourceHandler.class);

        addHtmlComponent("panelGrid", HtmlPanelGrid.COMPONENT_TYPE, "jakarta.faces.Grid");

        addHtmlComponent("panelGroup", HtmlPanelGroup.COMPONENT_TYPE, "jakarta.faces.Group");

        addHtmlComponent("selectBooleanCheckbox", HtmlSelectBooleanCheckbox.COMPONENT_TYPE, "jakarta.faces.Checkbox");

        addHtmlComponent("selectManyCheckbox", HtmlSelectManyCheckbox.COMPONENT_TYPE, "jakarta.faces.Checkbox");

        addHtmlComponent("selectManyListbox", HtmlSelectManyListbox.COMPONENT_TYPE, "jakarta.faces.Listbox");

        addHtmlComponent("selectManyMenu", HtmlSelectManyMenu.COMPONENT_TYPE, "jakarta.faces.Menu");

        addHtmlComponent("selectOneListbox", HtmlSelectOneListbox.COMPONENT_TYPE, "jakarta.faces.Listbox");

        addHtmlComponent("selectOneMenu", HtmlSelectOneMenu.COMPONENT_TYPE, "jakarta.faces.Menu");

        addHtmlComponent("selectOneRadio", HtmlSelectOneRadio.COMPONENT_TYPE, "jakarta.faces.Radio");

        addHtmlComponent("title", UIOutput.COMPONENT_TYPE, "jakarta.faces.Title");
    }

}
