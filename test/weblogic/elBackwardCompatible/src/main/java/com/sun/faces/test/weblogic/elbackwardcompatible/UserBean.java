/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.weblogic.elbackwardcompatible;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.el.ExpressionFactory;
import javax.enterprise.context.RequestScoped;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

@Named
@RequestScoped
public class UserBean implements Serializable {
    
    private final Map<String, Object> m_oNumberFilterBetweenOperatorMinValue;
    private final Map<String, Object> m_oNumberFilterBetweenOperatorMaxValue;
    
    public Map<String, Object> getNumberFilterBetweenOperatorMinValue() {
        return m_oNumberFilterBetweenOperatorMinValue;
    }
    
    public Map<String, Object> getNumberFilterBetweenOperatorMaxValue() {
        return m_oNumberFilterBetweenOperatorMaxValue;
    }
    
    private Integer intProp;

    public Integer getIntProp() {
        return intProp;
    }

    public void setIntProp(Integer intProp) {
        this.intProp = intProp;
    }
    
    private final List<String> listValues;

    public List<String> getListValues() {
        return listValues;
    }
    
    public UserBean() {
        this.listValues = new ArrayList<>();
        listValues.add("1");
        listValues.add("2");
        listValues.add("3");
        
        m_oNumberFilterBetweenOperatorMinValue = new HashMap<>();        
        m_oNumberFilterBetweenOperatorMaxValue = new HashMap<>();
        
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContext = context.getExternalContext();
        final String flagName = "forceSetFlag";

        Map<String, String> params = extContext.getRequestParameterMap();
        boolean forceSetFlag = false;
        if (params.containsKey(flagName)) {
            forceSetFlag = Boolean.valueOf(params.get(flagName));
        }
        
        if (forceSetFlag) {
            try {
                forceSetFlag();
            } catch (Exception ex) {
                throw new FacesException(ex);
            }
        }
    }
    
    private void forceSetFlag() throws Exception{
        final String flagName = "isBackwardCompatible22";
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContext = context.getExternalContext();
        ServletContext sc = (ServletContext) extContext.getContext();
        JspApplicationContext jspAppContext = JspFactory.getDefaultFactory()
                .getJspApplicationContext(sc);
        ExpressionFactory ef = jspAppContext.getExpressionFactory();
        
        Class weldExpFactory = Class.forName("org.jboss.weld.el.WeldExpressionFactory");
        Field delegateField = weldExpFactory.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        Object delegateInstance = delegateField.get(ef);
  
        Class expFactoryImp = Class.forName("com.sun.el.ExpressionFactoryImpl");        
        if (!(expFactoryImp.isInstance(delegateInstance))) {
        
            // dereference twice to get the true ExpressionFactoryImpl instance
            delegateInstance = delegateField.get(delegateInstance);
        }
        
        Field isBackwardCompatible22Field = expFactoryImp.getDeclaredField(flagName);
        isBackwardCompatible22Field.setAccessible(true);
        
        boolean flagValue = true;
        Map<String, String> params = extContext.getRequestParameterMap();
        if (params.containsKey(flagName)) {
            flagValue = Boolean.valueOf(params.get(flagName));
        }
        
        isBackwardCompatible22Field.setBoolean(delegateInstance, flagValue);
        
    }
    
}

