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

public class DataTableTag extends UIComponentELTag {

    // Setter Methods
    // PROPERTY: first
    private jakarta.el.ValueExpression first;

    public void setFirst(jakarta.el.ValueExpression first) {
        this.first = first;
    }

    // PROPERTY: rowStatePreserved
    private jakarta.el.ValueExpression rowStatePreserved;

    public void setRowStatePreserved(jakarta.el.ValueExpression rowStatePreserved) {
        this.rowStatePreserved = rowStatePreserved;
    }

    // PROPERTY: rows
    private jakarta.el.ValueExpression rows;

    public void setRows(jakarta.el.ValueExpression rows) {
        this.rows = rows;
    }

    // PROPERTY: value
    private jakarta.el.ValueExpression value;

    public void setValue(jakarta.el.ValueExpression value) {
        this.value = value;
    }

    // PROPERTY: var
    private java.lang.String _var;

    public void setVar(java.lang.String _var) {
        this._var = _var;
    }

    // PROPERTY: bgcolor
    private jakarta.el.ValueExpression bgcolor;

    public void setBgcolor(jakarta.el.ValueExpression bgcolor) {
        this.bgcolor = bgcolor;
    }

    // PROPERTY: bodyrows
    private jakarta.el.ValueExpression bodyrows;

    public void setBodyrows(jakarta.el.ValueExpression bodyrows) {
        this.bodyrows = bodyrows;
    }

    // PROPERTY: border
    private jakarta.el.ValueExpression border;

    public void setBorder(jakarta.el.ValueExpression border) {
        this.border = border;
    }

    // PROPERTY: captionClass
    private jakarta.el.ValueExpression captionClass;

    public void setCaptionClass(jakarta.el.ValueExpression captionClass) {
        this.captionClass = captionClass;
    }

    // PROPERTY: captionStyle
    private jakarta.el.ValueExpression captionStyle;

    public void setCaptionStyle(jakarta.el.ValueExpression captionStyle) {
        this.captionStyle = captionStyle;
    }

    // PROPERTY: cellpadding
    private jakarta.el.ValueExpression cellpadding;

    public void setCellpadding(jakarta.el.ValueExpression cellpadding) {
        this.cellpadding = cellpadding;
    }

    // PROPERTY: cellspacing
    private jakarta.el.ValueExpression cellspacing;

    public void setCellspacing(jakarta.el.ValueExpression cellspacing) {
        this.cellspacing = cellspacing;
    }

    // PROPERTY: columnClasses
    private jakarta.el.ValueExpression columnClasses;

    public void setColumnClasses(jakarta.el.ValueExpression columnClasses) {
        this.columnClasses = columnClasses;
    }

    // PROPERTY: dir
    private jakarta.el.ValueExpression dir;

    public void setDir(jakarta.el.ValueExpression dir) {
        this.dir = dir;
    }

    // PROPERTY: footerClass
    private jakarta.el.ValueExpression footerClass;

    public void setFooterClass(jakarta.el.ValueExpression footerClass) {
        this.footerClass = footerClass;
    }

    // PROPERTY: frame
    private jakarta.el.ValueExpression frame;

    public void setFrame(jakarta.el.ValueExpression frame) {
        this.frame = frame;
    }

    // PROPERTY: headerClass
    private jakarta.el.ValueExpression headerClass;

    public void setHeaderClass(jakarta.el.ValueExpression headerClass) {
        this.headerClass = headerClass;
    }

    // PROPERTY: lang
    private jakarta.el.ValueExpression lang;

    public void setLang(jakarta.el.ValueExpression lang) {
        this.lang = lang;
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

    // PROPERTY: role
    private jakarta.el.ValueExpression role;

    public void setRole(jakarta.el.ValueExpression role) {
        this.role = role;
    }

    // PROPERTY: rowClass
    private jakarta.el.ValueExpression rowClass;

    public void setRowClass(jakarta.el.ValueExpression rowClass) {
        this.rowClass = rowClass;
    }

    // PROPERTY: rowClasses
    private jakarta.el.ValueExpression rowClasses;

    public void setRowClasses(jakarta.el.ValueExpression rowClasses) {
        this.rowClasses = rowClasses;
    }

    // PROPERTY: rules
    private jakarta.el.ValueExpression rules;

    public void setRules(jakarta.el.ValueExpression rules) {
        this.rules = rules;
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

    // PROPERTY: summary
    private jakarta.el.ValueExpression summary;

    public void setSummary(jakarta.el.ValueExpression summary) {
        this.summary = summary;
    }

    // PROPERTY: title
    private jakarta.el.ValueExpression title;

    public void setTitle(jakarta.el.ValueExpression title) {
        this.title = title;
    }

    // PROPERTY: width
    private jakarta.el.ValueExpression width;

    public void setWidth(jakarta.el.ValueExpression width) {
        this.width = width;
    }

    // General Methods
    @Override
    public String getRendererType() {
        return "jakarta.faces.Table";
    }

    @Override
    public String getComponentType() {
        return "jakarta.faces.HtmlDataTable";
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        jakarta.faces.component.UIData data = null;
        try {
            data = (jakarta.faces.component.UIData) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException(
                    "Component " + component.toString() + " not expected type.  Expected: jakarta.faces.component.UIData.  Perhaps you're missing a tag?");
        }

        if (first != null) {
            data.setValueExpression("first", first);
        }
        if (rowStatePreserved != null) {
            data.setValueExpression("rowStatePreserved", rowStatePreserved);
        }
        if (rows != null) {
            data.setValueExpression("rows", rows);
        }
        if (value != null) {
            data.setValueExpression("value", value);
        }
        if (_var != null) {
            data.setVar(_var);
        }
        if (bgcolor != null) {
            data.setValueExpression("bgcolor", bgcolor);
        }
        if (bodyrows != null) {
            data.setValueExpression("bodyrows", bodyrows);
        }
        if (border != null) {
            data.setValueExpression("border", border);
        }
        if (captionClass != null) {
            data.setValueExpression("captionClass", captionClass);
        }
        if (captionStyle != null) {
            data.setValueExpression("captionStyle", captionStyle);
        }
        if (cellpadding != null) {
            data.setValueExpression("cellpadding", cellpadding);
        }
        if (cellspacing != null) {
            data.setValueExpression("cellspacing", cellspacing);
        }
        if (columnClasses != null) {
            data.setValueExpression("columnClasses", columnClasses);
        }
        if (dir != null) {
            data.setValueExpression("dir", dir);
        }
        if (footerClass != null) {
            data.setValueExpression("footerClass", footerClass);
        }
        if (frame != null) {
            data.setValueExpression("frame", frame);
        }
        if (headerClass != null) {
            data.setValueExpression("headerClass", headerClass);
        }
        if (lang != null) {
            data.setValueExpression("lang", lang);
        }
        if (onclick != null) {
            data.setValueExpression("onclick", onclick);
        }
        if (ondblclick != null) {
            data.setValueExpression("ondblclick", ondblclick);
        }
        if (onkeydown != null) {
            data.setValueExpression("onkeydown", onkeydown);
        }
        if (onkeypress != null) {
            data.setValueExpression("onkeypress", onkeypress);
        }
        if (onkeyup != null) {
            data.setValueExpression("onkeyup", onkeyup);
        }
        if (onmousedown != null) {
            data.setValueExpression("onmousedown", onmousedown);
        }
        if (onmousemove != null) {
            data.setValueExpression("onmousemove", onmousemove);
        }
        if (onmouseout != null) {
            data.setValueExpression("onmouseout", onmouseout);
        }
        if (onmouseover != null) {
            data.setValueExpression("onmouseover", onmouseover);
        }
        if (onmouseup != null) {
            data.setValueExpression("onmouseup", onmouseup);
        }
        if (role != null) {
            data.setValueExpression("role", role);
        }
        if (rowClass != null) {
            data.setValueExpression("rowClass", rowClass);
        }
        if (rowClasses != null) {
            data.setValueExpression("rowClasses", rowClasses);
        }
        if (rules != null) {
            data.setValueExpression("rules", rules);
        }
        if (style != null) {
            data.setValueExpression("style", style);
        }
        if (styleClass != null) {
            data.setValueExpression("styleClass", styleClass);
        }
        if (summary != null) {
            data.setValueExpression("summary", summary);
        }
        if (title != null) {
            data.setValueExpression("title", title);
        }
        if (width != null) {
            data.setValueExpression("width", width);
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
        first = null;
        rowStatePreserved = null;
        rows = null;
        value = null;
        _var = null;

        // rendered attributes
        bgcolor = null;
        bodyrows = null;
        border = null;
        captionClass = null;
        captionStyle = null;
        cellpadding = null;
        cellspacing = null;
        columnClasses = null;
        dir = null;
        footerClass = null;
        frame = null;
        headerClass = null;
        lang = null;
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
        role = null;
        rowClass = null;
        rowClasses = null;
        rules = null;
        style = null;
        styleClass = null;
        summary = null;
        title = null;
        width = null;
    }

    public String getDebugString() {
        return "id: " + getId() + " class: " + this.getClass().getName();
    }

}
