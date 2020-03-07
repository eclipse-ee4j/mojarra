/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
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

package com.sun.faces.renderkit;

import static com.sun.faces.renderkit.Attribute.attr;
import static com.sun.faces.util.CollectionsUtils.ar;

import java.util.Map;

import com.sun.faces.util.CollectionsUtils;

/**
 * This class contains mappings between the standard components and the passthrough attributes associated with them.
 */
public class AttributeManager {

    private static Map<String, Attribute[]> ATTRIBUTE_LOOKUP = CollectionsUtils.<String, Attribute[]>map().add("CommandButton",
            ar(attr("accesskey"), attr("alt"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onchange", "change"), attr("ondblclick", "dblclick"),
                    attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                    attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"),
                    attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add("CommandLink",
                    ar(attr("accesskey"), attr("charset"), attr("coords"), attr("dir"), attr("hreflang"), attr("lang"), attr("onblur", "blur"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("rel"), attr("rev"), attr("role"), attr("shape"),
                            attr("style"), attr("tabindex"), attr("title"), attr("type")))
            .add("DataTable",
                    ar(attr("bgcolor"), attr("border"), attr("cellpadding"), attr("cellspacing"), attr("dir"), attr("frame"), attr("lang"),
                            attr("onclick", "click"), attr("ondblclick", "dblclick"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("rules"), attr("style"), attr("summary"),
                            attr("title"), attr("width")))
            .add("FormForm",
                    ar(attr("accept"), attr("dir"), attr("enctype"), attr("lang"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"),
                            attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"),
                            attr("onreset"), attr("onsubmit"), attr("role"), attr("style"), attr("target"), attr("title")))
            .add("GraphicImage",
                    ar(attr("alt"), attr("dir"), attr("height"), attr("lang"), attr("longdesc"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"),
                            attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"),
                            attr("role"), attr("style"), attr("title"), attr("usemap"), attr("width")))
            .add("InputFile",
                    ar(attr("accesskey"), attr("alt"), attr("dir"), attr("lang"), attr("maxlength"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("size"),
                            attr("style"), attr("tabindex"), attr("title")))
            .add("InputSecret",
                    ar(attr("accesskey"), attr("alt"), attr("dir"), attr("lang"), attr("maxlength"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("size"),
                            attr("style"), attr("tabindex"), attr("title")))
            .add("InputText",
                    ar(attr("accesskey"), attr("alt"), attr("dir"), attr("lang"), attr("maxlength"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("size"),
                            attr("style"), attr("tabindex"), attr("title")))
            .add("InputTextarea",
                    ar(attr("accesskey"), attr("cols"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("rows"),
                            attr("style"), attr("tabindex"), attr("title")))
            .add("MessageMessage", ar(attr("dir"), attr("lang"), attr("role"), attr("style"), attr("title")))
            .add("MessagesMessages", ar(attr("dir"), attr("lang"), attr("role"), attr("style"), attr("title")))
            .add("OutcomeTargetButton",
                    ar(attr("accesskey"), attr("alt"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add("OutcomeTargetLink",
                    ar(attr("accesskey"), attr("charset"), attr("coords"), attr("dir"), attr("hreflang"), attr("lang"), attr("onblur", "blur"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("rel"), attr("rev"), attr("role"), attr("shape"),
                            attr("style"), attr("tabindex"), attr("title"), attr("type")))
            .add("OutputFormat", ar(attr("dir"), attr("lang"), attr("role"), attr("style"), attr("title")))
            .add("OutputLabel",
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add("OutputLink",
                    ar(attr("accesskey"), attr("charset"), attr("coords"), attr("dir"), attr("hreflang"), attr("lang"), attr("onblur", "blur"),
                            attr("onclick", "click", "action"), attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"),
                            attr("onkeypress", "keypress"), attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"),
                            attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("rel"), attr("rev"),
                            attr("role"), attr("shape"), attr("style"), attr("tabindex"), attr("title"), attr("type")))
            .add("OutputText", ar(attr("dir"), attr("lang"), attr("role"), attr("style"), attr("title")))
            .add("PanelGrid",
                    ar(attr("bgcolor"), attr("border"), attr("cellpadding"), attr("cellspacing"), attr("dir"), attr("frame"), attr("lang"),
                            attr("onclick", "click"), attr("ondblclick", "dblclick"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("rules"), attr("style"), attr("summary"),
                            attr("title"), attr("width")))
            .add("PanelGroup",
                    ar(attr("onclick", "click"), attr("ondblclick", "dblclick"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("style")))
            .add("SelectBooleanCheckbox",
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onchange", "change"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("style"),
                            attr("tabindex"), attr("title")))
            .add("SelectManyCheckbox", ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onchange", "change"),
                    attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                    attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                    attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("tabindex"), attr("title")))
            .add("SelectManyListbox",
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add("SelectManyMenu",
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add("SelectOneListbox",
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add("SelectOneMenu",
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add("SelectOneRadio", ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onchange", "change"),
                    attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                    attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                    attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("tabindex"), attr("title")))
            .add("OutputBody",
                    ar(attr("dir"), attr("lang"), attr("onclick", "click"), attr("ondblclick", "dblclick"), attr("onkeydown", "keydown"),
                            attr("onkeypress", "keypress"), attr("onkeyup", "keyup"), attr("onload", "load"), attr("onmousedown", "mousedown"),
                            attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"),
                            attr("onunload", "unload"), attr("role"), attr("style"), attr("title"), attr("xmlns")))
            .add("OutputDoctype", ar(attr("public"), attr("rootElement"), attr("system"))).add("OutputHead", ar(attr("dir"), attr("lang"), attr("xmlns")))
            .fix();

    public enum Key {
        COMMANDBUTTON("CommandButton"), COMMANDLINK("CommandLink"), DATATABLE("DataTable"), FORMFORM("FormForm"), GRAPHICIMAGE("GraphicImage"),
        INPUTFILE("InputFile"), INPUTSECRET("InputSecret"), INPUTTEXT("InputText"), INPUTTEXTAREA("InputTextarea"), MESSAGEMESSAGE("MessageMessage"),
        MESSAGESMESSAGES("MessagesMessages"), OUTCOMETARGETBUTTON("OutcomeTargetButton"), OUTCOMETARGETLINK("OutcomeTargetLink"), OUTPUTFORMAT("OutputFormat"),
        OUTPUTLABEL("OutputLabel"), OUTPUTLINK("OutputLink"), OUTPUTTEXT("OutputText"), PANELGRID("PanelGrid"), PANELGROUP("PanelGroup"),
        SELECTBOOLEANCHECKBOX("SelectBooleanCheckbox"), SELECTMANYCHECKBOX("SelectManyCheckbox"), SELECTMANYLISTBOX("SelectManyListbox"),
        SELECTMANYMENU("SelectManyMenu"), SELECTONELISTBOX("SelectOneListbox"), SELECTONEMENU("SelectOneMenu"), SELECTONERADIO("SelectOneRadio"),
        OUTPUTBODY("OutputBody"), OUTPUTDOCTYPE("OutputDoctype"), OUTPUTHEAD("OutputHead");

        private String key;

        Key(String key) {
            this.key = key;
        }

        public String value() {
            return key;
        }
    }

    public static Attribute[] getAttributes(Key key) {
        return ATTRIBUTE_LOOKUP.get(key.value());
    }
}
