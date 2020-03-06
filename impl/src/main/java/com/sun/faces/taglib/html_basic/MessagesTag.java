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

import javax.servlet.jsp.JspException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.webapp.UIComponentELTag;


/*
 * ******* GENERATED CODE - DO NOT EDIT *******
 */


public class MessagesTag extends UIComponentELTag {


    // Setter Methods
    // PROPERTY: for
    private javax.el.ValueExpression _for;
    public void setFor(javax.el.ValueExpression _for) {
        this._for = _for;
    }

    // PROPERTY: globalOnly
    private javax.el.ValueExpression globalOnly;
    public void setGlobalOnly(javax.el.ValueExpression globalOnly) {
        this.globalOnly = globalOnly;
    }

    // PROPERTY: showDetail
    private javax.el.ValueExpression showDetail;
    public void setShowDetail(javax.el.ValueExpression showDetail) {
        this.showDetail = showDetail;
    }

    // PROPERTY: showSummary
    private javax.el.ValueExpression showSummary;
    public void setShowSummary(javax.el.ValueExpression showSummary) {
        this.showSummary = showSummary;
    }

    // PROPERTY: dir
    private javax.el.ValueExpression dir;
    public void setDir(javax.el.ValueExpression dir) {
        this.dir = dir;
    }

    // PROPERTY: errorClass
    private javax.el.ValueExpression errorClass;
    public void setErrorClass(javax.el.ValueExpression errorClass) {
        this.errorClass = errorClass;
    }

    // PROPERTY: errorStyle
    private javax.el.ValueExpression errorStyle;
    public void setErrorStyle(javax.el.ValueExpression errorStyle) {
        this.errorStyle = errorStyle;
    }

    // PROPERTY: fatalClass
    private javax.el.ValueExpression fatalClass;
    public void setFatalClass(javax.el.ValueExpression fatalClass) {
        this.fatalClass = fatalClass;
    }

    // PROPERTY: fatalStyle
    private javax.el.ValueExpression fatalStyle;
    public void setFatalStyle(javax.el.ValueExpression fatalStyle) {
        this.fatalStyle = fatalStyle;
    }

    // PROPERTY: infoClass
    private javax.el.ValueExpression infoClass;
    public void setInfoClass(javax.el.ValueExpression infoClass) {
        this.infoClass = infoClass;
    }

    // PROPERTY: infoStyle
    private javax.el.ValueExpression infoStyle;
    public void setInfoStyle(javax.el.ValueExpression infoStyle) {
        this.infoStyle = infoStyle;
    }

    // PROPERTY: lang
    private javax.el.ValueExpression lang;
    public void setLang(javax.el.ValueExpression lang) {
        this.lang = lang;
    }

    // PROPERTY: layout
    private javax.el.ValueExpression layout;
    public void setLayout(javax.el.ValueExpression layout) {
        this.layout = layout;
    }

    // PROPERTY: role
    private javax.el.ValueExpression role;
    public void setRole(javax.el.ValueExpression role) {
        this.role = role;
    }

    // PROPERTY: style
    private javax.el.ValueExpression style;
    public void setStyle(javax.el.ValueExpression style) {
        this.style = style;
    }

    // PROPERTY: styleClass
    private javax.el.ValueExpression styleClass;
    public void setStyleClass(javax.el.ValueExpression styleClass) {
        this.styleClass = styleClass;
    }

    // PROPERTY: title
    private javax.el.ValueExpression title;
    public void setTitle(javax.el.ValueExpression title) {
        this.title = title;
    }

    // PROPERTY: tooltip
    private javax.el.ValueExpression tooltip;
    public void setTooltip(javax.el.ValueExpression tooltip) {
        this.tooltip = tooltip;
    }

    // PROPERTY: warnClass
    private javax.el.ValueExpression warnClass;
    public void setWarnClass(javax.el.ValueExpression warnClass) {
        this.warnClass = warnClass;
    }

    // PROPERTY: warnStyle
    private javax.el.ValueExpression warnStyle;
    public void setWarnStyle(javax.el.ValueExpression warnStyle) {
        this.warnStyle = warnStyle;
    }


    // General Methods
    public String getRendererType() {
        return "jakarta.faces.Messages";
    }

    public String getComponentType() {
        return "jakarta.faces.HtmlMessages";
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        jakarta.faces.component.UIMessages messages = null;
        try {
            messages = (jakarta.faces.component.UIMessages) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException("Component " + component.toString() + " not expected type.  Expected: jakarta.faces.component.UIMessages.  Perhaps you're missing a tag?");
        }

        if (_for != null) {
            messages.setValueExpression("for", _for);
        }
        if (globalOnly != null) {
            messages.setValueExpression("globalOnly", globalOnly);
        }
        if (showDetail != null) {
            messages.setValueExpression("showDetail", showDetail);
        }
        if (showSummary != null) {
            messages.setValueExpression("showSummary", showSummary);
        }
        if (dir != null) {
            messages.setValueExpression("dir", dir);
        }
        if (errorClass != null) {
            messages.setValueExpression("errorClass", errorClass);
        }
        if (errorStyle != null) {
            messages.setValueExpression("errorStyle", errorStyle);
        }
        if (fatalClass != null) {
            messages.setValueExpression("fatalClass", fatalClass);
        }
        if (fatalStyle != null) {
            messages.setValueExpression("fatalStyle", fatalStyle);
        }
        if (infoClass != null) {
            messages.setValueExpression("infoClass", infoClass);
        }
        if (infoStyle != null) {
            messages.setValueExpression("infoStyle", infoStyle);
        }
        if (lang != null) {
            messages.setValueExpression("lang", lang);
        }
        if (layout != null) {
            messages.setValueExpression("layout", layout);
        }
        if (role != null) {
            messages.setValueExpression("role", role);
        }
        if (style != null) {
            messages.setValueExpression("style", style);
        }
        if (styleClass != null) {
            messages.setValueExpression("styleClass", styleClass);
        }
        if (title != null) {
            messages.setValueExpression("title", title);
        }
        if (tooltip != null) {
            messages.setValueExpression("tooltip", tooltip);
        }
        if (warnClass != null) {
            messages.setValueExpression("warnClass", warnClass);
        }
        if (warnStyle != null) {
            messages.setValueExpression("warnStyle", warnStyle);
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
        this.globalOnly = null;
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
        this.layout = null;
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
