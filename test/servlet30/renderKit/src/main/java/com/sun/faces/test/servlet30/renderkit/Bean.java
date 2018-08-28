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

package com.sun.faces.test.servlet30.renderkit;

import com.sun.faces.test.servlet30.renderkit.SelectMany05Bean.HobbitBean;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;

@Named
@RequestScoped

public class Bean {

    public Bean() {
        passThroughAttrs = new ConcurrentHashMap<String, Object>();
        passThroughAttrs.put("literalName", "literalValue");
        passThroughAttrs.put("elName", FacesContext.getCurrentInstance().getApplication().getExpressionFactory()
                .createValueExpression(FacesContext.getCurrentInstance().getELContext(), "#{facesContext.viewRoot.viewId}", String.class));

        rendererSpecificAttrs = new ConcurrentHashMap<String, Object>();
        rendererSpecificAttrs.put("styleClass", "a b c");
        rendererSpecificAttrs.put("size", FacesContext.getCurrentInstance().getApplication().getExpressionFactory()
                .createValueExpression(FacesContext.getCurrentInstance().getELContext(), "#{bean.one}", Integer.class));

    }

    private Map<String, Object> passThroughAttrs;
    private Map<String, Object> rendererSpecificAttrs;

    public Map<String, Object> getRendererSpecificAttrs() {
        return rendererSpecificAttrs;
    }

    public Map<String, Object> getPassThroughAttrs() {
        return passThroughAttrs;
    }

    private String nullValue = null;

    public String getNullValue() {
        return nullValue;
    }

    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    public Integer getOne() {
        return 1;
    }

    private String fruitValue;

    public String getFruitValue() {
        return fruitValue;
    }

    public void setFruitValue(String stringValue) {
        this.fruitValue = stringValue;
    }

    private String nameValue;

    public String getNameValue() {
        return nameValue;
    }

    List<String> nameValueList;

    public List<String> getNameValueList() {
        return nameValueList;
    }

    public void setNameValueList(List<String> nameValueList) {
        this.nameValueList = nameValueList;
    }

    public void setNameValue(String nameValue) {
        this.nameValue = nameValue;
    }

    private HobbitBean hobbitBean;

    public HobbitBean getHobbitBean() {
        return hobbitBean;
    }

    public void setHobbitBean(HobbitBean hobbitBean) {
        this.hobbitBean = hobbitBean;
    }

    private List<HobbitBean> hobbitBeanList;

    public List<HobbitBean> getHobbitBeanList() {
        return hobbitBeanList;
    }

    public void setHobbitBeanList(List<HobbitBean> hobbitBeanList) {
        this.hobbitBeanList = hobbitBeanList;
    }

    private String groupedNameValue;

    public String getGroupedNameValue() {
        return groupedNameValue;
    }

    public void setGroupedNameValue(String groupedNameValue) {
        this.groupedNameValue = groupedNameValue;
    }

    private List<String> groupedNameValueList;

    public List<String> getGroupedNameValueList() {
        return groupedNameValueList;
    }

    public void setGroupedNameValueList(List<String> groupedNameValueList) {
        this.groupedNameValueList = groupedNameValueList;
    }

}
