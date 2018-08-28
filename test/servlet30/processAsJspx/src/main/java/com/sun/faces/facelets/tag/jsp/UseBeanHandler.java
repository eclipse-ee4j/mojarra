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

package com.sun.faces.facelets.tag.jsp;

import com.sun.faces.util.Util;
import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

public class UseBeanHandler extends TagHandler {

    private final TagAttribute id;

    private final TagAttribute scope;

    private final TagAttribute classAttr;

    private final TagAttribute type;

    private final TagAttribute beanName;

    public UseBeanHandler(TagConfig config) {
        super(config);

        this.id = this.getRequiredAttribute("id");
        this.scope = this.getAttribute("scope");
        this.classAttr = this.getAttribute("class");
        this.type = this.getAttribute("type");
        this.beanName = this.getAttribute("beanName");
    }

    @Override
    public void apply(FaceletContext fc, UIComponent uic) throws IOException {
        FacesContext facesContext = fc.getFacesContext();
        ExternalContext extContext = facesContext.getExternalContext();
        // view scope is defined as equivalent to page scope
        Map<String, Object> scopeMap = facesContext.getViewRoot().getViewMap();
        boolean isRequestScoped = false;

        // Find the correct scope
        String scopeName = (null != this.scope) ? this.scope.getValue(fc) : "";
        if ("session".equals(scopeName)) {
            scopeMap = extContext.getSessionMap();
        } else if ("application".equals(scopeName)) {
            scopeMap = extContext.getApplicationMap();
        } else if ("request".equals(scopeName)) {
            isRequestScoped = true;
            scopeMap = extContext.getRequestMap();
        } else {
            if ((!"".equals(scopeName)) && !("page".equals(scopeName))) {
                throw new FaceletException("Invalid scope name " + scopeName + ".");
            }
        }
        assert (null != scopeMap);
        String idVal = this.id.getValue(fc);
        Object bean = scopeMap.get(idVal);
        boolean instantiatedByThisMetod = null != bean;

        if (!instantiatedByThisMetod) {
            // case 1 we have a class with an optional type
            // case 2 we have a beanName and type
            // case 3 we can have just a type

            if (null == beanName && null == classAttr && null != type) {
                // this is case 3
            } else if ((null != beanName) && (null != type)) {
                // this is case 2
                String beanNameVal = this.beanName.getValue(fc);
                try {
                    bean = java.beans.Beans.instantiate(Util.getCurrentLoader(this), beanNameVal);
                    instantiatedByThisMetod = true;
                } catch (ClassNotFoundException ex) {
                    throw new FaceletException(ex);
                }
            } else if (null != classAttr) {
                // this is case 1
                String className = this.classAttr.getValue(fc);
                try {
                    Class clazz = Util.loadClass(className, this);
                    bean = clazz.newInstance();
                    instantiatedByThisMetod = true;
                } catch (IllegalAccessException ie) {
                    throw new FaceletException(ie);
                } catch (InstantiationException ie) {
                    throw new FaceletException(ie);
                } catch (ClassNotFoundException ex) {
                    throw new FaceletException(ex);
                }
            }
        }

        // The "object reference variable" concept does not exist
        // in Facelets because there is no concept of scriptlets.
        // Therefore, request scope stands in for the object reference variable
        // concept
        if (!isRequestScoped && instantiatedByThisMetod) {
            extContext.getRequestMap().put(idVal, bean);
        }

        if (instantiatedByThisMetod) {
            nextHandler.apply(fc, uic);
        }
    }

}
