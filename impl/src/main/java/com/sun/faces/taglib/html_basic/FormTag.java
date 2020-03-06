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

public class FormTag extends UIComponentELTag {

    // Setter Methods
    // PROPERTY: prependId
    private jakarta.el.ValueExpression prependId;

    public void setPrependId(jakarta.el.ValueExpression prependId) {
        this.prependId = prependId;
    }

    // PROPERTY: accept
    private jakarta.el.ValueExpression accept;

    public void setAccept(jakarta.el.ValueExpression accept) {
        this.accept = accept;
    }

    // PROPERTY: acceptcharset
    private jakarta.el.ValueExpression acceptcharset;

    public void setAcceptcharset(jakarta.el.ValueExpression acceptcharset) {
        this.acceptcharset = acceptcharset;
    }

    // PROPERTY: dir
    private jakarta.el.ValueExpression dir;

    public void setDir(jakarta.el.ValueExpression dir) {
        this.dir = dir;
    }

    // PROPERTY: enctype
    private jakarta.el.ValueExpression enctype;

    public void setEnctype(jakarta.el.ValueExpression enctype) {
        this.enctype = enctype;
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

    // PROPERTY: onreset
    private jakarta.el.ValueExpression onreset;

    public void setOnreset(jakarta.el.ValueExpression onreset) {
        this.onreset = onreset;
    }

    // PROPERTY: onsubmit
    private jakarta.el.ValueExpression onsubmit;

    public void setOnsubmit(jakarta.el.ValueExpression onsubmit) {
        this.onsubmit = onsubmit;
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

    // General Methods
    public String getRendererType() {
        return "jakarta.faces.Form";
    }

    public String getComponentType() {
        return "jakarta.faces.HtmlForm";
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        jakarta.faces.component.UIForm form = null;
        try {
            form = (jakarta.faces.component.UIForm) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException(
                    "Component " + component.toString() + " not expected type.  Expected: jakarta.faces.component.UIForm.  Perhaps you're missing a tag?");
        }

        if (prependId != null) {
            form.setValueExpression("prependId", prependId);
        }
        if (accept != null) {
            form.setValueExpression("accept", accept);
        }
        if (acceptcharset != null) {
            form.setValueExpression("acceptcharset", acceptcharset);
        }
        if (dir != null) {
            form.setValueExpression("dir", dir);
        }
        if (enctype != null) {
            form.setValueExpression("enctype", enctype);
        }
        if (lang != null) {
            form.setValueExpression("lang", lang);
        }
        if (onclick != null) {
            form.setValueExpression("onclick", onclick);
        }
        if (ondblclick != null) {
            form.setValueExpression("ondblclick", ondblclick);
        }
        if (onkeydown != null) {
            form.setValueExpression("onkeydown", onkeydown);
        }
        if (onkeypress != null) {
            form.setValueExpression("onkeypress", onkeypress);
        }
        if (onkeyup != null) {
            form.setValueExpression("onkeyup", onkeyup);
        }
        if (onmousedown != null) {
            form.setValueExpression("onmousedown", onmousedown);
        }
        if (onmousemove != null) {
            form.setValueExpression("onmousemove", onmousemove);
        }
        if (onmouseout != null) {
            form.setValueExpression("onmouseout", onmouseout);
        }
        if (onmouseover != null) {
            form.setValueExpression("onmouseover", onmouseover);
        }
        if (onmouseup != null) {
            form.setValueExpression("onmouseup", onmouseup);
        }
        if (onreset != null) {
            form.setValueExpression("onreset", onreset);
        }
        if (onsubmit != null) {
            form.setValueExpression("onsubmit", onsubmit);
        }
        if (role != null) {
            form.setValueExpression("role", role);
        }
        if (style != null) {
            form.setValueExpression("style", style);
        }
        if (styleClass != null) {
            form.setValueExpression("styleClass", styleClass);
        }
        if (target != null) {
            form.setValueExpression("target", target);
        }
        if (title != null) {
            form.setValueExpression("title", title);
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
        this.prependId = null;

        // rendered attributes
        this.accept = null;
        this.acceptcharset = null;
        this.dir = null;
        this.enctype = null;
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
        this.onreset = null;
        this.onsubmit = null;
        this.role = null;
        this.style = null;
        this.styleClass = null;
        this.target = null;
        this.title = null;
    }

    public String getDebugString() {
        return "id: " + this.getId() + " class: " + this.getClass().getName();
    }

}
