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
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.webapp.UIComponentELTag;

/*
 * ******* GENERATED CODE - DO NOT EDIT *******
 */

public class SelectManyListboxTag extends UIComponentELTag {

    // Setter Methods
    // PROPERTY: converter
    private jakarta.el.ValueExpression converter;

    public void setConverter(jakarta.el.ValueExpression converter) {
        this.converter = converter;
    }

    // PROPERTY: converterMessage
    private jakarta.el.ValueExpression converterMessage;

    public void setConverterMessage(jakarta.el.ValueExpression converterMessage) {
        this.converterMessage = converterMessage;
    }

    // PROPERTY: immediate
    private jakarta.el.ValueExpression immediate;

    public void setImmediate(jakarta.el.ValueExpression immediate) {
        this.immediate = immediate;
    }

    // PROPERTY: required
    private jakarta.el.ValueExpression required;

    public void setRequired(jakarta.el.ValueExpression required) {
        this.required = required;
    }

    // PROPERTY: requiredMessage
    private jakarta.el.ValueExpression requiredMessage;

    public void setRequiredMessage(jakarta.el.ValueExpression requiredMessage) {
        this.requiredMessage = requiredMessage;
    }

    // PROPERTY: validator
    private jakarta.el.MethodExpression validator;

    public void setValidator(jakarta.el.MethodExpression validator) {
        this.validator = validator;
    }

    // PROPERTY: validatorMessage
    private jakarta.el.ValueExpression validatorMessage;

    public void setValidatorMessage(jakarta.el.ValueExpression validatorMessage) {
        this.validatorMessage = validatorMessage;
    }

    // PROPERTY: value
    private jakarta.el.ValueExpression value;

    public void setValue(jakarta.el.ValueExpression value) {
        this.value = value;
    }

    // PROPERTY: valueChangeListener
    private jakarta.el.MethodExpression valueChangeListener;

    public void setValueChangeListener(jakarta.el.MethodExpression valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    // PROPERTY: accesskey
    private jakarta.el.ValueExpression accesskey;

    public void setAccesskey(jakarta.el.ValueExpression accesskey) {
        this.accesskey = accesskey;
    }

    // PROPERTY: collectionType
    private jakarta.el.ValueExpression collectionType;

    public void setCollectionType(jakarta.el.ValueExpression collectionType) {
        this.collectionType = collectionType;
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

    // PROPERTY: disabledClass
    private jakarta.el.ValueExpression disabledClass;

    public void setDisabledClass(jakarta.el.ValueExpression disabledClass) {
        this.disabledClass = disabledClass;
    }

    // PROPERTY: enabledClass
    private jakarta.el.ValueExpression enabledClass;

    public void setEnabledClass(jakarta.el.ValueExpression enabledClass) {
        this.enabledClass = enabledClass;
    }

    // PROPERTY: hideNoSelectionOption
    private jakarta.el.ValueExpression hideNoSelectionOption;

    public void setHideNoSelectionOption(jakarta.el.ValueExpression hideNoSelectionOption) {
        this.hideNoSelectionOption = hideNoSelectionOption;
    }

    // PROPERTY: label
    private jakarta.el.ValueExpression label;

    public void setLabel(jakarta.el.ValueExpression label) {
        this.label = label;
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

    // PROPERTY: onchange
    private jakarta.el.ValueExpression onchange;

    public void setOnchange(jakarta.el.ValueExpression onchange) {
        this.onchange = onchange;
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

    // PROPERTY: onselect
    private jakarta.el.ValueExpression onselect;

    public void setOnselect(jakarta.el.ValueExpression onselect) {
        this.onselect = onselect;
    }

    // PROPERTY: readonly
    private jakarta.el.ValueExpression readonly;

    public void setReadonly(jakarta.el.ValueExpression readonly) {
        this.readonly = readonly;
    }

    // PROPERTY: role
    private jakarta.el.ValueExpression role;

    public void setRole(jakarta.el.ValueExpression role) {
        this.role = role;
    }

    // PROPERTY: size
    private jakarta.el.ValueExpression size;

    public void setSize(jakarta.el.ValueExpression size) {
        this.size = size;
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

    // PROPERTY: title
    private jakarta.el.ValueExpression title;

    public void setTitle(jakarta.el.ValueExpression title) {
        this.title = title;
    }

    // General Methods
    @Override
    public String getRendererType() {
        return "jakarta.faces.Listbox";
    }

    @Override
    public String getComponentType() {
        return "jakarta.faces.HtmlSelectManyListbox";
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        jakarta.faces.component.UISelectMany selectmany = null;
        try {
            selectmany = (jakarta.faces.component.UISelectMany) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException("Component " + component.toString()
                    + " not expected type.  Expected: jakarta.faces.component.UISelectMany.  Perhaps you're missing a tag?");
        }

        if (converter != null) {
            if (!converter.isLiteralText()) {
                selectmany.setValueExpression("converter", converter);
            } else {
                Converter conv = FacesContext.getCurrentInstance().getApplication().createConverter(converter.getExpressionString());
                selectmany.setConverter(conv);
            }
        }

        if (converterMessage != null) {
            selectmany.setValueExpression("converterMessage", converterMessage);
        }
        if (immediate != null) {
            selectmany.setValueExpression("immediate", immediate);
        }
        if (required != null) {
            selectmany.setValueExpression("required", required);
        }
        if (requiredMessage != null) {
            selectmany.setValueExpression("requiredMessage", requiredMessage);
        }
        if (validator != null) {
            selectmany.addValidator(new MethodExpressionValidator(validator));
        }
        if (validatorMessage != null) {
            selectmany.setValueExpression("validatorMessage", validatorMessage);
        }
        if (value != null) {
            selectmany.setValueExpression("value", value);
        }
        if (valueChangeListener != null) {
            selectmany.addValueChangeListener(new MethodExpressionValueChangeListener(valueChangeListener));
        }
        if (accesskey != null) {
            selectmany.setValueExpression("accesskey", accesskey);
        }
        if (collectionType != null) {
            selectmany.setValueExpression("collectionType", collectionType);
        }
        if (dir != null) {
            selectmany.setValueExpression("dir", dir);
        }
        if (disabled != null) {
            selectmany.setValueExpression("disabled", disabled);
        }
        if (disabledClass != null) {
            selectmany.setValueExpression("disabledClass", disabledClass);
        }
        if (enabledClass != null) {
            selectmany.setValueExpression("enabledClass", enabledClass);
        }
        if (hideNoSelectionOption != null) {
            selectmany.setValueExpression("hideNoSelectionOption", hideNoSelectionOption);
        }
        if (label != null) {
            selectmany.setValueExpression("label", label);
        }
        if (lang != null) {
            selectmany.setValueExpression("lang", lang);
        }
        if (onblur != null) {
            selectmany.setValueExpression("onblur", onblur);
        }
        if (onchange != null) {
            selectmany.setValueExpression("onchange", onchange);
        }
        if (onclick != null) {
            selectmany.setValueExpression("onclick", onclick);
        }
        if (ondblclick != null) {
            selectmany.setValueExpression("ondblclick", ondblclick);
        }
        if (onfocus != null) {
            selectmany.setValueExpression("onfocus", onfocus);
        }
        if (onkeydown != null) {
            selectmany.setValueExpression("onkeydown", onkeydown);
        }
        if (onkeypress != null) {
            selectmany.setValueExpression("onkeypress", onkeypress);
        }
        if (onkeyup != null) {
            selectmany.setValueExpression("onkeyup", onkeyup);
        }
        if (onmousedown != null) {
            selectmany.setValueExpression("onmousedown", onmousedown);
        }
        if (onmousemove != null) {
            selectmany.setValueExpression("onmousemove", onmousemove);
        }
        if (onmouseout != null) {
            selectmany.setValueExpression("onmouseout", onmouseout);
        }
        if (onmouseover != null) {
            selectmany.setValueExpression("onmouseover", onmouseover);
        }
        if (onmouseup != null) {
            selectmany.setValueExpression("onmouseup", onmouseup);
        }
        if (onselect != null) {
            selectmany.setValueExpression("onselect", onselect);
        }
        if (readonly != null) {
            selectmany.setValueExpression("readonly", readonly);
        }
        if (role != null) {
            selectmany.setValueExpression("role", role);
        }
        if (size != null) {
            selectmany.setValueExpression("size", size);
        }
        if (style != null) {
            selectmany.setValueExpression("style", style);
        }
        if (styleClass != null) {
            selectmany.setValueExpression("styleClass", styleClass);
        }
        if (tabindex != null) {
            selectmany.setValueExpression("tabindex", tabindex);
        }
        if (title != null) {
            selectmany.setValueExpression("title", title);
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
        converter = null;
        converterMessage = null;
        immediate = null;
        required = null;
        requiredMessage = null;
        validator = null;
        validatorMessage = null;
        value = null;
        valueChangeListener = null;

        // rendered attributes
        accesskey = null;
        collectionType = null;
        dir = null;
        disabled = null;
        disabledClass = null;
        enabledClass = null;
        hideNoSelectionOption = null;
        label = null;
        lang = null;
        onblur = null;
        onchange = null;
        onclick = null;
        ondblclick = null;
        onfocus = null;
        onkeydown = null;
        onkeypress = null;
        onkeyup = null;
        onmousedown = null;
        onmousemove = null;
        onmouseout = null;
        onmouseover = null;
        onmouseup = null;
        onselect = null;
        readonly = null;
        role = null;
        size = null;
        style = null;
        styleClass = null;
        tabindex = null;
        title = null;
    }

    public String getDebugString() {
        return "id: " + getId() + " class: " + this.getClass().getName();
    }

}
