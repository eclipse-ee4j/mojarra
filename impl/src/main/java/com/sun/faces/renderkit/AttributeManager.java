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

    public enum Key {
        COMMANDBUTTON, COMMANDLINK, DATATABLE, FORMFORM, GRAPHICIMAGE,
        INPUTFILE, INPUTSECRET, INPUTTEXT, INPUTTEXTAREA, MESSAGEMESSAGE,
        MESSAGESMESSAGES, OUTCOMETARGETBUTTON, OUTCOMETARGETLINK, OUTPUTFORMAT,
        OUTPUTLABEL, OUTPUTLINK, OUTPUTTEXT, PANELGRID, PANELGROUP,
        SELECTBOOLEANCHECKBOX, SELECTMANYCHECKBOX, SELECTMANYLISTBOX,
        SELECTMANYMENU, SELECTONELISTBOX, SELECTONEMENU, SELECTONERADIO,
        OUTPUTBODY, OUTPUTDOCTYPE, OUTPUTHEAD;
    }

    private static final Map<Key, Attribute[]> ATTRIBUTE_LOOKUP = CollectionsUtils.<Key, Attribute[]>map()
            .add(Key.COMMANDBUTTON,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onchange", "change"), attr("ondblclick", "dblclick"),
                        attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                        attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"),
                        attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add(Key.COMMANDLINK,
                    ar(attr("accesskey"), attr("charset"), attr("coords"), attr("dir"), attr("hreflang"), attr("lang"), attr("onblur", "blur"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("rel"), attr("rev"), attr("role"), attr("shape"),
                            attr("style"), attr("tabindex"), attr("title"), attr("type")))
            .add(Key.DATATABLE,
                    ar(attr("bgcolor"), attr("border"), attr("cellpadding"), attr("cellspacing"), attr("dir"), attr("frame"), attr("lang"),
                            attr("onclick", "click"), attr("ondblclick", "dblclick"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("rules"), attr("style"), attr("summary"),
                            attr("title"), attr("width")))
            .add(Key.FORMFORM,
                    ar(attr("accept"), attr("dir"), attr("enctype"), attr("lang"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"),
                            attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"),
                            attr("onreset"), attr("onsubmit"), attr("role"), attr("style"), attr("target"), attr("title")))
            .add(Key.GRAPHICIMAGE,
                    ar(attr("alt"), attr("dir"), attr("height"), attr("lang"), attr("longdesc"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"),
                            attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"),
                            attr("role"), attr("style"), attr("title"), attr("usemap"), attr("width")))
            .add(Key.INPUTFILE,
                    ar(attr("accesskey"), attr("alt"), attr("dir"), attr("lang"), attr("maxlength"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("size"),
                            attr("style"), attr("tabindex"), attr("title")))
            .add(Key.INPUTSECRET,
                    ar(attr("accesskey"), attr("alt"), attr("dir"), attr("lang"), attr("maxlength"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("size"),
                            attr("style"), attr("tabindex"), attr("title")))
            .add(Key.INPUTTEXT,
                    ar(attr("accesskey"), attr("alt"), attr("dir"), attr("lang"), attr("maxlength"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("size"),
                            attr("style"), attr("tabindex"), attr("title")))
            .add(Key.INPUTTEXTAREA,
                    ar(attr("accesskey"), attr("cols"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("rows"),
                            attr("style"), attr("tabindex"), attr("title")))
            .add(Key.MESSAGEMESSAGE,
                    ar(attr("dir"), attr("lang"), attr("role"), attr("style"), attr("title")))
            .add(Key.MESSAGESMESSAGES,
                    ar(attr("dir"), attr("lang"), attr("role"), attr("style"), attr("title")))
            .add(Key.OUTCOMETARGETBUTTON,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add(Key.OUTCOMETARGETLINK,
                    ar(attr("accesskey"), attr("charset"), attr("coords"), attr("dir"), attr("hreflang"), attr("lang"), attr("onblur", "blur"),
                            attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("rel"), attr("rev"), attr("role"), attr("shape"),
                            attr("style"), attr("tabindex"), attr("title"), attr("type")))
            .add(Key.OUTPUTFORMAT,
                    ar(attr("dir"), attr("lang"), attr("role"), attr("style"), attr("title")))
            .add(Key.OUTPUTLABEL,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add(Key.OUTPUTLINK,
                    ar(attr("accesskey"), attr("charset"), attr("coords"), attr("dir"), attr("hreflang"), attr("lang"), attr("onblur", "blur"),
                            attr("onclick", "click", "action"), attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"),
                            attr("onkeypress", "keypress"), attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"),
                            attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("rel"), attr("rev"),
                            attr("role"), attr("shape"), attr("style"), attr("tabindex"), attr("title"), attr("type")))
            .add(Key.OUTPUTTEXT,
                    ar(attr("dir"), attr("lang"), attr("role"), attr("style"), attr("title")))
            .add(Key.PANELGRID,
                    ar(attr("bgcolor"), attr("border"), attr("cellpadding"), attr("cellspacing"), attr("dir"), attr("frame"), attr("lang"),
                            attr("onclick", "click"), attr("ondblclick", "dblclick"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("rules"), attr("style"), attr("summary"),
                            attr("title"), attr("width")))
            .add(Key.PANELGROUP,
                    ar(attr("onclick", "click"), attr("ondblclick", "dblclick"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                            attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("style")))
            .add(Key.SELECTBOOLEANCHECKBOX,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onchange", "change"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("style"),
                            attr("tabindex"), attr("title")))
            .add(Key.SELECTMANYCHECKBOX,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onchange", "change"),
                        attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                        attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                        attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("tabindex"), attr("title")))
            .add(Key.SELECTMANYLISTBOX,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add(Key.SELECTMANYMENU,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add(Key.SELECTONELISTBOX,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add(Key.SELECTONEMENU,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onclick", "click"), attr("ondblclick", "dblclick"),
                            attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"), attr("onkeyup", "keyup"),
                            attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                            attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("role"), attr("style"), attr("tabindex"), attr("title")))
            .add(Key.SELECTONERADIO,
                    ar(attr("accesskey"), attr("dir"), attr("lang"), attr("onblur", "blur"), attr("onchange", "change"),
                        attr("ondblclick", "dblclick"), attr("onfocus", "focus"), attr("onkeydown", "keydown"), attr("onkeypress", "keypress"),
                        attr("onkeyup", "keyup"), attr("onmousedown", "mousedown"), attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"),
                        attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"), attr("onselect", "select"), attr("role"), attr("tabindex"), attr("title")))
            .add(Key.OUTPUTBODY,
                    ar(attr("dir"), attr("lang"), attr("onclick", "click"), attr("ondblclick", "dblclick"), attr("onkeydown", "keydown"),
                            attr("onkeypress", "keypress"), attr("onkeyup", "keyup"), attr("onload", "load"), attr("onmousedown", "mousedown"),
                            attr("onmousemove", "mousemove"), attr("onmouseout", "mouseout"), attr("onmouseover", "mouseover"), attr("onmouseup", "mouseup"),
                            attr("onunload", "unload"), attr("role"), attr("style"), attr("title"), attr("xmlns")))
            .add(Key.OUTPUTDOCTYPE,
                    ar(attr("public"), attr("rootElement"), attr("system")))
            .add(Key.OUTPUTHEAD,
                    ar(attr("dir"), attr("lang"), attr("xmlns")))
            .fix();

    public static Attribute[] getAttributes(Key key) {
        return ATTRIBUTE_LOOKUP.get(key);
    }
}
