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

public class MessageTag extends UIComponentELTag {

    // Setter Methods
    // PROPERTY: for
    private jakarta.el.ValueExpression _for;

    public void setFor(jakarta.el.ValueExpression _for) {
        this._for = _for;
    }

    // PROPERTY: showDetail
    private jakarta.el.ValueExpression showDetail;

    public void setShowDetail(jakarta.el.ValueExpression showDetail) {
        this.showDetail = showDetail;
    }

    // PROPERTY: showSummary
    private jakarta.el.ValueExpression showSummary;

    public void setShowSummary(jakarta.el.ValueExpression showSummary) {
        this.showSummary = showSummary;
    }

    // PROPERTY: dir
    private jakarta.el.ValueExpression dir;

    public void setDir(jakarta.el.ValueExpression dir) {
        this.dir = dir;
    }

    // PROPERTY: errorClass
    private jakarta.el.ValueExpression errorClass;

    public void setErrorClass(jakarta.el.ValueExpression errorClass) {
        this.errorClass = errorClass;
    }

    // PROPERTY: errorStyle
    private jakarta.el.ValueExpression errorStyle;

    public void setErrorStyle(jakarta.el.ValueExpression errorStyle) {
        this.errorStyle = errorStyle;
    }

    // PROPERTY: fatalClass
    private jakarta.el.ValueExpression fatalClass;

    public void setFatalClass(jakarta.el.ValueExpression fatalClass) {
        this.fatalClass = fatalClass;
    }

    // PROPERTY: fatalStyle
    private jakarta.el.ValueExpression fatalStyle;

    public void setFatalStyle(jakarta.el.ValueExpression fatalStyle) {
        this.fatalStyle = fatalStyle;
    }

    // PROPERTY: infoClass
    private jakarta.el.ValueExpression infoClass;

    public void setInfoClass(jakarta.el.ValueExpression infoClass) {
        this.infoClass = infoClass;
    }

    // PROPERTY: infoStyle
    private jakarta.el.ValueExpression infoStyle;

    public void setInfoStyle(jakarta.el.ValueExpression infoStyle) {
        this.infoStyle = infoStyle;
    }

    // PROPERTY: lang
    private jakarta.el.ValueExpression lang;

    public void setLang(jakarta.el.ValueExpression lang) {
        this.lang = lang;
    }

    // PROPERTY: role
    private jakarta.el.ValueExpression role;

    public void setRole(jakarta.el.ValueExpression role) {
        this.role = role;
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

    // PROPERTY: title
    private jakarta.el.ValueExpression title;

    public void setTitle(jakarta.el.ValueExpression title) {
        this.title = title;
    }

    // PROPERTY: tooltip
    private jakarta.el.ValueExpression tooltip;

    public void setTooltip(jakarta.el.ValueExpression tooltip) {
        this.tooltip = tooltip;
    }

    // PROPERTY: warnClass
    private jakarta.el.ValueExpression warnClass;

    public void setWarnClass(jakarta.el.ValueExpression warnClass) {
        this.warnClass = warnClass;
    }

    // PROPERTY: warnStyle
    private jakarta.el.ValueExpression warnStyle;

    public void setWarnStyle(jakarta.el.ValueExpression warnStyle) {
        this.warnStyle = warnStyle;
    }

    // General Methods
    public String getRendererType() {
        return "jakarta.faces.Message";
    }

    public String getComponentType() {
        return "jakarta.faces.HtmlMessage";
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        jakarta.faces.component.UIMessage message = null;
        try {
            message = (jakarta.faces.component.UIMessage) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException(
                    "Component " + component.toString() + " not expected type.  Expected: jakarta.faces.component.UIMessage.  Perhaps you're missing a tag?");
        }

        if (_for != null) {
            message.setValueExpression("for", _for);
        }
        if (showDetail != null) {
            message.setValueExpression("showDetail", showDetail);
        }
        if (showSummary != null) {
            message.setValueExpression("showSummary", showSummary);
        }
        if (dir != null) {
            message.setValueExpression("dir", dir);
        }
        if (errorClass != null) {
            message.setValueExpression("errorClass", errorClass);
        }
        if (errorStyle != null) {
            message.setValueExpression("errorStyle", errorStyle);
        }
        if (fatalClass != null) {
            message.setValueExpression("fatalClass", fatalClass);
        }
        if (fatalStyle != null) {
            message.setValueExpression("fatalStyle", fatalStyle);
        }
        if (infoClass != null) {
            message.setValueExpression("infoClass", infoClass);
        }
        if (infoStyle != null) {
            message.setValueExpression("infoStyle", infoStyle);
        }
        if (lang != null) {
            message.setValueExpression("lang", lang);
        }
        if (role != null) {
            message.setValueExpression("role", role);
        }
        if (style != null) {
            message.setValueExpression("style", style);
        }
        if (styleClass != null) {
            message.setValueExpression("styleClass", styleClass);
        }
        if (title != null) {
            message.setValueExpression("title", title);
        }
        if (tooltip != null) {
            message.setValueExpression("tooltip", tooltip);
        }
        if (warnClass != null) {
            message.setValueExpression("warnClass", warnClass);
        }
        if (warnStyle != null) {
            message.setValueExpression("warnStyle", warnStyle);
        }
    }

    // Methods From TagSupport
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
    public void release() {
        super.release();

        // component properties
        this._for = null;
        this.showDetail = null;
        this.showSummary = null;

        // rendered attributes
        this.dir = null;
        this.errorClass = null;
        this.errorStyle = null;
        this.fatalClass = null;
        this.fatalStyle = null;
        this.infoClass = null;
        this.infoStyle = null;
        this.lang = null;
        this.role = null;
        this.style = null;
        this.styleClass = null;
        this.title = null;
        this.tooltip = null;
        this.warnClass = null;
        this.warnStyle = null;
    }

    public String getDebugString() {
        return "id: " + this.getId() + " class: " + this.getClass().getName();
    }

}
