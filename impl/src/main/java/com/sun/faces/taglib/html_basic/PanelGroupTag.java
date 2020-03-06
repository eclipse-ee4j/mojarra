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
import jakarta.faces.webapp.UIComponentELTag;

/*
 * ******* GENERATED CODE - DO NOT EDIT *******
 */

public class PanelGroupTag extends UIComponentELTag {

    // Setter Methods
    // PROPERTY: layout
    private jakarta.el.ValueExpression layout;

    public void setLayout(jakarta.el.ValueExpression layout) {
        this.layout = layout;
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

    // General Methods
    @Override
    public String getRendererType() {
        return "jakarta.faces.Group";
    }

    @Override
    public String getComponentType() {
        return "jakarta.faces.HtmlPanelGroup";
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        jakarta.faces.component.UIPanel panel = null;
        try {
            panel = (jakarta.faces.component.UIPanel) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException(
                    "Component " + component.toString() + " not expected type.  Expected: jakarta.faces.component.UIPanel.  Perhaps you're missing a tag?");
        }

        if (layout != null) {
            panel.setValueExpression("layout", layout);
        }
        if (onclick != null) {
            panel.setValueExpression("onclick", onclick);
        }
        if (ondblclick != null) {
            panel.setValueExpression("ondblclick", ondblclick);
        }
        if (onkeydown != null) {
            panel.setValueExpression("onkeydown", onkeydown);
        }
        if (onkeypress != null) {
            panel.setValueExpression("onkeypress", onkeypress);
        }
        if (onkeyup != null) {
            panel.setValueExpression("onkeyup", onkeyup);
        }
        if (onmousedown != null) {
            panel.setValueExpression("onmousedown", onmousedown);
        }
        if (onmousemove != null) {
            panel.setValueExpression("onmousemove", onmousemove);
        }
        if (onmouseout != null) {
            panel.setValueExpression("onmouseout", onmouseout);
        }
        if (onmouseover != null) {
            panel.setValueExpression("onmouseover", onmouseover);
        }
        if (onmouseup != null) {
            panel.setValueExpression("onmouseup", onmouseup);
        }
        if (style != null) {
            panel.setValueExpression("style", style);
        }
        if (styleClass != null) {
            panel.setValueExpression("styleClass", styleClass);
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

        // rendered attributes
        layout = null;
        onclick = null;
        ondblclick = null;
        onkeydown = null;
        onkeypress = null;
        onkeyup = null;
        onmousedown = null;
        onmousemove = null;
        onmouseout = null;
        onmouseover = null;
        onmouseup = null;
        style = null;
        styleClass = null;
    }

    public String getDebugString() {
        return "id: " + getId() + " class: " + this.getClass().getName();
    }

}
