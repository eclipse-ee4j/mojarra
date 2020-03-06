/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
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

package com.sun.faces.taglib.html_basic;

import jakarta.servlet.jsp.JspException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.event.MethodExpressionActionListener;
import jakarta.faces.webapp.UIComponentELTag;

/*
 * ******* GENERATED CODE - DO NOT EDIT *******
 */

public class CommandLinkTag extends UIComponentELTag {

    // Setter Methods
    // PROPERTY: action
    private jakarta.el.MethodExpression action;

    public void setAction(jakarta.el.MethodExpression action) {
        this.action = action;
    }

    // PROPERTY: actionListener
    private jakarta.el.MethodExpression actionListener;

    public void setActionListener(jakarta.el.MethodExpression actionListener) {
        this.actionListener = actionListener;
    }

    // PROPERTY: immediate
    private jakarta.el.ValueExpression immediate;

    public void setImmediate(jakarta.el.ValueExpression immediate) {
        this.immediate = immediate;
    }

    // PROPERTY: value
    private jakarta.el.ValueExpression value;

    public void setValue(jakarta.el.ValueExpression value) {
        this.value = value;
    }

    // PROPERTY: accesskey
    private jakarta.el.ValueExpression accesskey;

    public void setAccesskey(jakarta.el.ValueExpression accesskey) {
        this.accesskey = accesskey;
    }

    // PROPERTY: charset
    private jakarta.el.ValueExpression charset;

    public void setCharset(jakarta.el.ValueExpression charset) {
        this.charset = charset;
    }

    // PROPERTY: coords
    private jakarta.el.ValueExpression coords;

    public void setCoords(jakarta.el.ValueExpression coords) {
        this.coords = coords;
    }

    // PROPERTY: dir
    private jakarta.el.ValueExpression dir;

    public void setDir(jakarta.el.ValueExpression dir) {
        this.dir = dir;
    }

    // PROPERTY: disabled
    private jakarta.el.ValueExpression disabled;

    public void setDisabled(jakarta.el.ValueExpression disabled) {
        this.disabled = disabled;
    }

    // PROPERTY: hreflang
    private jakarta.el.ValueExpression hreflang;

    public void setHreflang(jakarta.el.ValueExpression hreflang) {
        this.hreflang = hreflang;
    }

    // PROPERTY: lang
    private jakarta.el.ValueExpression lang;

    public void setLang(jakarta.el.ValueExpression lang) {
        this.lang = lang;
    }

    // PROPERTY: onblur
    private jakarta.el.ValueExpression onblur;

    public void setOnblur(jakarta.el.ValueExpression onblur) {
        this.onblur = onblur;
    }

    // PROPERTY: onclick
    private jakarta.el.ValueExpression onclick;

    public void setOnclick(jakarta.el.ValueExpression onclick) {
        this.onclick = onclick;
    }

    // PROPERTY: ondblclick
    private jakarta.el.ValueExpression ondblclick;

    public void setOndblclick(jakarta.el.ValueExpression ondblclick) {
        this.ondblclick = ondblclick;
    }

    // PROPERTY: onfocus
    private jakarta.el.ValueExpression onfocus;

    public void setOnfocus(jakarta.el.ValueExpression onfocus) {
        this.onfocus = onfocus;
    }

    // PROPERTY: onkeydown
    private jakarta.el.ValueExpression onkeydown;

    public void setOnkeydown(jakarta.el.ValueExpression onkeydown) {
        this.onkeydown = onkeydown;
    }

    // PROPERTY: onkeypress
    private jakarta.el.ValueExpression onkeypress;

    public void setOnkeypress(jakarta.el.ValueExpression onkeypress) {
        this.onkeypress = onkeypress;
    }

    // PROPERTY: onkeyup
    private jakarta.el.ValueExpression onkeyup;

    public void setOnkeyup(jakarta.el.ValueExpression onkeyup) {
        this.onkeyup = onkeyup;
    }

    // PROPERTY: onmousedown
    private jakarta.el.ValueExpression onmousedown;

    public void setOnmousedown(jakarta.el.ValueExpression onmousedown) {
        this.onmousedown = onmousedown;
    }

    // PROPERTY: onmousemove
    private jakarta.el.ValueExpression onmousemove;

    public void setOnmousemove(jakarta.el.ValueExpression onmousemove) {
        this.onmousemove = onmousemove;
    }

    // PROPERTY: onmouseout
    private jakarta.el.ValueExpression onmouseout;

    public void setOnmouseout(jakarta.el.ValueExpression onmouseout) {
        this.onmouseout = onmouseout;
    }

    // PROPERTY: onmouseover
    private jakarta.el.ValueExpression onmouseover;

    public void setOnmouseover(jakarta.el.ValueExpression onmouseover) {
        this.onmouseover = onmouseover;
    }

    // PROPERTY: onmouseup
    private jakarta.el.ValueExpression onmouseup;

    public void setOnmouseup(jakarta.el.ValueExpression onmouseup) {
        this.onmouseup = onmouseup;
    }

    // PROPERTY: rel
    private jakarta.el.ValueExpression rel;

    public void setRel(jakarta.el.ValueExpression rel) {
        this.rel = rel;
    }

    // PROPERTY: rev
    private jakarta.el.ValueExpression rev;

    public void setRev(jakarta.el.ValueExpression rev) {
        this.rev = rev;
    }

    // PROPERTY: role
    private jakarta.el.ValueExpression role;

    public void setRole(jakarta.el.ValueExpression role) {
        this.role = role;
    }

    // PROPERTY: shape
    private jakarta.el.ValueExpression shape;

    public void setShape(jakarta.el.ValueExpression shape) {
        this.shape = shape;
    }

    // PROPERTY: style
    private jakarta.el.ValueExpression style;

    public void setStyle(jakarta.el.ValueExpression style) {
        this.style = style;
    }

    // PROPERTY: styleClass
    private jakarta.el.ValueExpression styleClass;

    public void setStyleClass(jakarta.el.ValueExpression styleClass) {
        this.styleClass = styleClass;
    }

    // PROPERTY: tabindex
    private jakarta.el.ValueExpression tabindex;

    public void setTabindex(jakarta.el.ValueExpression tabindex) {
        this.tabindex = tabindex;
    }

    // PROPERTY: target
    private jakarta.el.ValueExpression target;

    public void setTarget(jakarta.el.ValueExpression target) {
        this.target = target;
    }

    // PROPERTY: title
    private jakarta.el.ValueExpression title;

    public void setTitle(jakarta.el.ValueExpression title) {
        this.title = title;
    }

    // PROPERTY: type
    private jakarta.el.ValueExpression type;

    public void setType(jakarta.el.ValueExpression type) {
        this.type = type;
    }

    // General Methods
    @Override
    public String getRendererType() {
        return "jakarta.faces.Link";
    }

    @Override
    public String getComponentType() {
        return "jakarta.faces.HtmlCommandLink";
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        jakarta.faces.component.UICommand command = null;
        try {
            command = (jakarta.faces.component.UICommand) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException(
                    "Component " + component.toString() + " not expected type.  Expected: jakarta.faces.component.UICommand.  Perhaps you're missing a tag?");
        }

        if (action != null) {
            command.setActionExpression(action);
        }
        if (actionListener != null) {
            command.addActionListener(new MethodExpressionActionListener(actionListener));
        }
        if (immediate != null) {
            command.setValueExpression("immediate", immediate);
        }
        if (value != null) {
            command.setValueExpression("value", value);
        }
        if (accesskey != null) {
            command.setValueExpression("accesskey", accesskey);
        }
        if (charset != null) {
            command.setValueExpression("charset", charset);
        }
        if (coords != null) {
            command.setValueExpression("coords", coords);
        }
        if (dir != null) {
            command.setValueExpression("dir", dir);
        }
        if (disabled != null) {
            command.setValueExpression("disabled", disabled);
        }
        if (hreflang != null) {
            command.setValueExpression("hreflang", hreflang);
        }
        if (lang != null) {
            command.setValueExpression("lang", lang);
        }
        if (onblur != null) {
            command.setValueExpression("onblur", onblur);
        }
        if (onclick != null) {
            command.setValueExpression("onclick", onclick);
        }
        if (ondblclick != null) {
            command.setValueExpression("ondblclick", ondblclick);
        }
        if (onfocus != null) {
            command.setValueExpression("onfocus", onfocus);
        }
        if (onkeydown != null) {
            command.setValueExpression("onkeydown", onkeydown);
        }
        if (onkeypress != null) {
            command.setValueExpression("onkeypress", onkeypress);
        }
        if (onkeyup != null) {
            command.setValueExpression("onkeyup", onkeyup);
        }
        if (onmousedown != null) {
            command.setValueExpression("onmousedown", onmousedown);
        }
        if (onmousemove != null) {
            command.setValueExpression("onmousemove", onmousemove);
        }
        if (onmouseout != null) {
            command.setValueExpression("onmouseout", onmouseout);
        }
        if (onmouseover != null) {
            command.setValueExpression("onmouseover", onmouseover);
        }
        if (onmouseup != null) {
            command.setValueExpression("onmouseup", onmouseup);
        }
        if (rel != null) {
            command.setValueExpression("rel", rel);
        }
        if (rev != null) {
            command.setValueExpression("rev", rev);
        }
        if (role != null) {
            command.setValueExpression("role", role);
        }
        if (shape != null) {
            command.setValueExpression("shape", shape);
        }
        if (style != null) {
            command.setValueExpression("style", style);
        }
        if (styleClass != null) {
            command.setValueExpression("styleClass", styleClass);
        }
        if (tabindex != null) {
            command.setValueExpression("tabindex", tabindex);
        }
        if (target != null) {
            command.setValueExpression("target", target);
        }
        if (title != null) {
            command.setValueExpression("title", title);
        }
        if (type != null) {
            command.setValueExpression("type", type);
        }
    }

    // Methods From TagSupport
    @Override
    public int doStartTag() throws JspException {
        try {
            return super.doStartTag();
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new JspException(root);
        }
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            return super.doEndTag();
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new JspException(root);
        }
    }

    // RELEASE
    @Override
    public void release() {
        super.release();

        // component properties
        this.action = null;
        this.actionListener = null;
        this.immediate = null;
        this.value = null;

        // rendered attributes
        this.accesskey = null;
        this.charset = null;
        this.coords = null;
        this.dir = null;
        this.disabled = null;
        this.hreflang = null;
        this.lang = null;
        this.onblur = null;
        this.onclick = null;
        this.ondblclick = null;
        this.onfocus = null;
        this.onkeydown = null;
        this.onkeypress = null;
        this.onkeyup = null;
        this.onmousedown = null;
        this.onmousemove = null;
        this.onmouseout = null;
        this.onmouseover = null;
        this.onmouseup = null;
        this.rel = null;
        this.rev = null;
        this.role = null;
        this.shape = null;
        this.style = null;
        this.styleClass = null;
        this.tabindex = null;
        this.target = null;
        this.title = null;
        this.type = null;
    }

    public String getDebugString() {
        return "id: " + this.getId() + " class: " + this.getClass().getName();
    }

}
