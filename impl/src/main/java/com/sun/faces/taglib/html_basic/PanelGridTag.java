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

import com.sun.faces.util.Util;
import java.io.IOException;
import javax.el.*;
import javax.faces.*;
import javax.faces.component.*;
import javax.faces.context.*;
import javax.faces.convert.*;
import javax.faces.el.*;
import javax.faces.event.*;
import javax.faces.validator.*;
import javax.faces.webapp.*;
import javax.servlet.jsp.JspException;


/*
 * ******* GENERATED CODE - DO NOT EDIT *******
 */


public class PanelGridTag extends UIComponentELTag {


    // Setter Methods
    // PROPERTY: bgcolor
    private javax.el.ValueExpression bgcolor;
    public void setBgcolor(javax.el.ValueExpression bgcolor) {
        this.bgcolor = bgcolor;
    }

    // PROPERTY: bodyrows
    private javax.el.ValueExpression bodyrows;
    public void setBodyrows(javax.el.ValueExpression bodyrows) {
        this.bodyrows = bodyrows;
    }

    // PROPERTY: border
    private javax.el.ValueExpression border;
    public void setBorder(javax.el.ValueExpression border) {
        this.border = border;
    }

    // PROPERTY: captionClass
    private javax.el.ValueExpression captionClass;
    public void setCaptionClass(javax.el.ValueExpression captionClass) {
        this.captionClass = captionClass;
    }

    // PROPERTY: captionStyle
    private javax.el.ValueExpression captionStyle;
    public void setCaptionStyle(javax.el.ValueExpression captionStyle) {
        this.captionStyle = captionStyle;
    }

    // PROPERTY: cellpadding
    private javax.el.ValueExpression cellpadding;
    public void setCellpadding(javax.el.ValueExpression cellpadding) {
        this.cellpadding = cellpadding;
    }

    // PROPERTY: cellspacing
    private javax.el.ValueExpression cellspacing;
    public void setCellspacing(javax.el.ValueExpression cellspacing) {
        this.cellspacing = cellspacing;
    }

    // PROPERTY: columnClasses
    private javax.el.ValueExpression columnClasses;
    public void setColumnClasses(javax.el.ValueExpression columnClasses) {
        this.columnClasses = columnClasses;
    }

    // PROPERTY: columns
    private javax.el.ValueExpression columns;
    public void setColumns(javax.el.ValueExpression columns) {
        this.columns = columns;
    }

    // PROPERTY: dir
    private javax.el.ValueExpression dir;
    public void setDir(javax.el.ValueExpression dir) {
        this.dir = dir;
    }

    // PROPERTY: footerClass
    private javax.el.ValueExpression footerClass;
    public void setFooterClass(javax.el.ValueExpression footerClass) {
        this.footerClass = footerClass;
    }

    // PROPERTY: frame
    private javax.el.ValueExpression frame;
    public void setFrame(javax.el.ValueExpression frame) {
        this.frame = frame;
    }

    // PROPERTY: headerClass
    private javax.el.ValueExpression headerClass;
    public void setHeaderClass(javax.el.ValueExpression headerClass) {
        this.headerClass = headerClass;
    }

    // PROPERTY: lang
    private javax.el.ValueExpression lang;
    public void setLang(javax.el.ValueExpression lang) {
        this.lang = lang;
    }

    // PROPERTY: onclick
    private javax.el.ValueExpression onclick;
    public void setOnclick(javax.el.ValueExpression onclick) {
        this.onclick = onclick;
    }

    // PROPERTY: ondblclick
    private javax.el.ValueExpression ondblclick;
    public void setOndblclick(javax.el.ValueExpression ondblclick) {
        this.ondblclick = ondblclick;
    }

    // PROPERTY: onkeydown
    private javax.el.ValueExpression onkeydown;
    public void setOnkeydown(javax.el.ValueExpression onkeydown) {
        this.onkeydown = onkeydown;
    }

    // PROPERTY: onkeypress
    private javax.el.ValueExpression onkeypress;
    public void setOnkeypress(javax.el.ValueExpression onkeypress) {
        this.onkeypress = onkeypress;
    }

    // PROPERTY: onkeyup
    private javax.el.ValueExpression onkeyup;
    public void setOnkeyup(javax.el.ValueExpression onkeyup) {
        this.onkeyup = onkeyup;
    }

    // PROPERTY: onmousedown
    private javax.el.ValueExpression onmousedown;
    public void setOnmousedown(javax.el.ValueExpression onmousedown) {
        this.onmousedown = onmousedown;
    }

    // PROPERTY: onmousemove
    private javax.el.ValueExpression onmousemove;
    public void setOnmousemove(javax.el.ValueExpression onmousemove) {
        this.onmousemove = onmousemove;
    }

    // PROPERTY: onmouseout
    private javax.el.ValueExpression onmouseout;
    public void setOnmouseout(javax.el.ValueExpression onmouseout) {
        this.onmouseout = onmouseout;
    }

    // PROPERTY: onmouseover
    private javax.el.ValueExpression onmouseover;
    public void setOnmouseover(javax.el.ValueExpression onmouseover) {
        this.onmouseover = onmouseover;
    }

    // PROPERTY: onmouseup
    private javax.el.ValueExpression onmouseup;
    public void setOnmouseup(javax.el.ValueExpression onmouseup) {
        this.onmouseup = onmouseup;
    }

    // PROPERTY: role
    private javax.el.ValueExpression role;
    public void setRole(javax.el.ValueExpression role) {
        this.role = role;
    }

    // PROPERTY: rowClass
    private javax.el.ValueExpression rowClass;
    public void setRowClass(javax.el.ValueExpression rowClass) {
        this.rowClass = rowClass;
    }

    // PROPERTY: rowClasses
    private javax.el.ValueExpression rowClasses;
    public void setRowClasses(javax.el.ValueExpression rowClasses) {
        this.rowClasses = rowClasses;
    }

    // PROPERTY: rules
    private javax.el.ValueExpression rules;
    public void setRules(javax.el.ValueExpression rules) {
        this.rules = rules;
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

    // PROPERTY: summary
    private javax.el.ValueExpression summary;
    public void setSummary(javax.el.ValueExpression summary) {
        this.summary = summary;
    }

    // PROPERTY: title
    private javax.el.ValueExpression title;
    public void setTitle(javax.el.ValueExpression title) {
        this.title = title;
    }

    // PROPERTY: width
    private javax.el.ValueExpression width;
    public void setWidth(javax.el.ValueExpression width) {
        this.width = width;
    }


    // General Methods
    public String getRendererType() {
        return "javax.faces.Grid";
    }

    public String getComponentType() {
        return "javax.faces.HtmlPanelGrid";
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        javax.faces.component.UIPanel panel = null;
        try {
            panel = (javax.faces.component.UIPanel) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException("Component " + component.toString() + " not expected type.  Expected: javax.faces.component.UIPanel.  Perhaps you're missing a tag?");
        }

        if (bgcolor != null) {
            panel.setValueExpression("bgcolor", bgcolor);
        }
        if (bodyrows != null) {
            panel.setValueExpression("bodyrows", bodyrows);
        }
        if (border != null) {
            panel.setValueExpression("border", border);
        }
        if (captionClass != null) {
            panel.setValueExpression("captionClass", captionClass);
        }
        if (captionStyle != null) {
            panel.setValueExpression("captionStyle", captionStyle);
        }
        if (cellpadding != null) {
            panel.setValueExpression("cellpadding", cellpadding);
        }
        if (cellspacing != null) {
            panel.setValueExpression("cellspacing", cellspacing);
        }
        if (columnClasses != null) {
            panel.setValueExpression("columnClasses", columnClasses);
        }
        if (columns != null) {
            panel.setValueExpression("columns", columns);
        }
        if (dir != null) {
            panel.setValueExpression("dir", dir);
        }
        if (footerClass != null) {
            panel.setValueExpression("footerClass", footerClass);
        }
        if (frame != null) {
            panel.setValueExpression("frame", frame);
        }
        if (headerClass != null) {
            panel.setValueExpression("headerClass", headerClass);
        }
        if (lang != null) {
            panel.setValueExpression("lang", lang);
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
        if (role != null) {
            panel.setValueExpression("role", role);
        }
        if (rowClass != null) {
            panel.setValueExpression("rowClass", rowClass);
        }
        if (rowClasses != null) {
            panel.setValueExpression("rowClasses", rowClasses);
        }
        if (rules != null) {
            panel.setValueExpression("rules", rules);
        }
        if (style != null) {
            panel.setValueExpression("style", style);
        }
        if (styleClass != null) {
            panel.setValueExpression("styleClass", styleClass);
        }
        if (summary != null) {
            panel.setValueExpression("summary", summary);
        }
        if (title != null) {
            panel.setValueExpression("title", title);
        }
        if (width != null) {
            panel.setValueExpression("width", width);
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

        // rendered attributes
        this.bgcolor = null;
        this.bodyrows = null;
        this.border = null;
        this.captionClass = null;
        this.captionStyle = null;
        this.cellpadding = null;
        this.cellspacing = null;
        this.columnClasses = null;
        this.columns = null;
        this.dir = null;
        this.footerClass = null;
        this.frame = null;
        this.headerClass = null;
        this.lang = null;
        this.onclick = null;
        this.ondblclick = null;
        this.onkeydown = null;
        this.onkeypress = null;
        this.onkeyup = null;
        this.onmousedown = null;
        this.onmousemove = null;
        this.onmouseout = null;
        this.onmouseover = null;
        this.onmouseup = null;
        this.role = null;
        this.rowClass = null;
        this.rowClasses = null;
        this.rules = null;
        this.style = null;
        this.styleClass = null;
        this.summary = null;
        this.title = null;
        this.width = null;
    }

    public String getDebugString() {
        return "id: " + this.getId() + " class: " + this.getClass().getName();
    }

}
