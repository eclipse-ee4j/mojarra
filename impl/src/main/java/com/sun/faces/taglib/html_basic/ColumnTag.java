/*
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
import javax.el.ValueExpression;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.webapp.UIComponentELTag;

public class ColumnTag extends UIComponentELTag {

    // Log instance for this class
    private static final Logger logger = FacesLogger.TAGLIB.getLogger();

    //
    // Instance Variables
    //


    //
    // Setter Methods
    //
    // PROPERTY: footerClass
    private ValueExpression footerClass;
    public void setFooterClass(ValueExpression footerClass) {
        this.footerClass = footerClass;
    }

    // PROPERTY: headerClass
    private ValueExpression headerClass;
    public void setHeaderClass(ValueExpression headerClass) {
        this.headerClass = headerClass;
    }

    // PROPERTY: rowHeader
    private ValueExpression rowHeader;
    public void setRowHeader(ValueExpression rowHeader) {
        this.rowHeader = rowHeader;
    }

    //
    // General Methods
    //
    @Override
    public String getRendererType() {
        return null;
    }


    @Override
    public String getComponentType() {
        return "jakarta.faces.Column";
    }


    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        UIColumn column;

        try {
            column = (UIColumn) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException("Component " + component.toString() + " not expected type.  Expected: UIColumn.  Perhaps you're missing a tag?");
        }
        if (footerClass != null) {
            column.setValueExpression("footerClass", footerClass);
        }
        if (headerClass != null) {
            column.setValueExpression("headerClass", headerClass);
        }
        if (rowHeader != null) {
            column.setValueExpression("rowHeader", rowHeader);
        }
    }

    //
    // Methods From TagSupport
    //

    @Override
    public int doStartTag() throws JspException {
        try {
            return super.doStartTag();
        } catch (JspException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, getDebugString(), e);
            }
            throw e;
        } catch (Throwable t) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, getDebugString(), t);
            }
            throw new JspException(t);
        }
    }


    @Override
    public int doEndTag() throws JspException {
        try {
            return super.doEndTag();
        } catch (JspException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, getDebugString(), e);
            }
            throw e;
        } catch (Throwable t) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, getDebugString(), t);
            }
            throw new JspException(t);
        }
    }

    // RELEASE
    @Override
    public void release() {
        super.release();
        this.headerClass = null;
        this.footerClass = null;
    }

    public String getDebugString() {
        return "id: " + this.getId() + " class: " +
            this.getClass().getName();
    }

}

