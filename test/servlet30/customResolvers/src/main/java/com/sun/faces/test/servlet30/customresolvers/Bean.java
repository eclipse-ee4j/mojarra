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

/*
 * Bean.java
 *
 * Created on April 29, 2006, 1:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.faces.test.servlet30.customresolvers;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

/**
 *
 * @author edburns
 */
public class Bean {
    
    /** Creates a new instance of Bean */
    public Bean() {
    }
    
    public String callMethodsOnVariableResolver(FacesContext context, 
            VariableResolver vr) throws EvaluationException {
        Object result = null;
        
        result = vr.resolveVariable(context, "noneBean");
        
        if (!(result instanceof TestBean)) {
            throw new IllegalStateException("Bean not of correct type");
        }
        
        result = vr.resolveVariable(context, "custom");
        
        if (!result.equals("custom")) {
            throw new IllegalStateException("Bean not of correct type");
        }
        
        return "success";
    }

    public void verifyELResolverChainIsCorrectlyConfigured(ActionEvent e) {
        String result = null;
        final FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder message = Bean.getBuilder(context);

        message.append("<br /><br />\n\n  <h1>FacesELResolverForFaces</h1><br /><br />\n\n  ");

        // FacesELResolver Chain
        context.getApplication().getELResolver().getValue(context.getELContext(), null,
                new Object() {

            @Override
            public String toString() {
                Bean.captureStackTrace(context);
                return "traceResolution";
            }

        });

        message.append("<br /><br />\n\n  <h1>FacesELResolverForJsp</h1><br /><br />\n\n  ");

        JspFactory factory = JspFactory.getDefaultFactory();
        JspApplicationContext jspContext = factory.
                getJspApplicationContext((ServletContext)
                context.getExternalContext().getContext());
        ExpressionFactory elFactory = jspContext.getExpressionFactory();
        ValueExpression ve = elFactory.createValueExpression(context.getELContext(), "#{traceResolution}",
                Object.class);
        ve.getValue(context.getELContext());
    }

    public static StringBuilder getBuilder(FacesContext context) {
        StringBuilder result = (StringBuilder) context.getExternalContext().getRequestMap().get("message");
        if (null == result) {
            result = new StringBuilder();
            context.getExternalContext().getRequestMap().put("message", result);
        }
        return result;
    }

    public static void captureStackTrace(FacesContext context) {
        StringBuilder message = getBuilder(context);
        message.append("<h2>toString() invocation</h2>");

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String stackTraceElement;
        for (int i = 0; i < 4; i++) {
            StackTraceElement cur = stackTrace[i];
            stackTraceElement = cur.toString();
            if (!(stackTraceElement.contains("Thread") ||
                  stackTraceElement.contains(Bean.class.getCanonicalName()))) {
                message.append("<p>").append(stackTraceElement).append("</p>");
                break;
            }
        }
    }
    
    public String getInvokeVariableResolverThruChain() throws EvaluationException {
        FacesContext context = FacesContext.getCurrentInstance();
        VariableResolver vr = context.getApplication().getVariableResolver();
        return callMethodsOnVariableResolver(context, vr);
    }
    
    public String getInvokeVariableResolverDirectly() throws EvaluationException {
        FacesContext context = FacesContext.getCurrentInstance();
        VariableResolver vr = (VariableResolver) context.getExternalContext().getApplicationMap().get("newVR");
        return callMethodsOnVariableResolver(context, vr);
    }
    
    public String getInvokeELResolverThruChain() throws EvaluationException {
        FacesContext context = FacesContext.getCurrentInstance();
        ELResolver er = context.getApplication().getELResolver();
        boolean isReadOnly = er.isReadOnly(context.getELContext(), "newERThruChain", null);

        return Boolean.valueOf(isReadOnly).toString();
    }

    public String getInvokeELResolverDirectly() throws EvaluationException {
        FacesContext context = FacesContext.getCurrentInstance();
        ELResolver er = (ELResolver) context.getExternalContext().getApplicationMap().get("newER");
        boolean isReadOnly = er.isReadOnly(context.getELContext(), "newERDirect", null);

        return Boolean.valueOf(isReadOnly).toString();
    }

    public String getInvokeVariableResolverThruChain1() throws EvaluationException {
        FacesContext context = FacesContext.getCurrentInstance();
        VariableResolver vr = context.getApplication().getVariableResolver();
        Object result = vr.resolveVariable(context, "nonmanaged");
        if (!(result instanceof TestBean)) {
            throw new IllegalStateException("Bean not of correct type");
        }
        return "success";
    }
    
    public String getInvokeVariableResolverDirectly1() throws EvaluationException {
        FacesContext context = FacesContext.getCurrentInstance();
        VariableResolver vr = (VariableResolver) context.getExternalContext().getApplicationMap().get("newVR");
        Object result = vr.resolveVariable(context, "nonmanaged");
        if (!(result instanceof TestBean)) {
            throw new IllegalStateException("Bean not of correct type");
        }
        return "success";
    }
}
