/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.action;

import java.lang.reflect.Method;
import jakarta.enterprise.inject.spi.Bean;

/**
 * The ActionMappingMatch class.
 */
public class ActionMappingMatch {

    /**
     * Stores the action mapping.
     */
    private String actionMapping;

    /**
     * Stores the mapping type.
     */
    private ActionMappingType mappingType;

    /**
     * Stores the bean.
     */
    private Bean<?> bean;

    /**
     * Stores the method.
     */
    private Method method;
    
    /**
     * Stores the path info.
     */
    private String pathInfo;

    /**
     * Get the action mapping.
     * 
     * @return the action mapping.
     */
    public String getActionMapping() {
        return actionMapping;
    }

    /**
     * Get the bean.
     *
     * @return the bean.
     */
    public Bean<?> getBean() {
        return bean;
    }

    /**
     * Get the length.
     *
     * @return the length.
     */
    public int getLength() {
        return actionMapping.length();
    }

    /**
     * Get the mapping type.
     *
     * @return the mapping type.
     */
    public ActionMappingType getMappingType() {
        return mappingType;
    }

    /**
     * Get the method.
     *
     * @return the method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Get the path info.
     * 
     * @return the path info.
     */
    public String getPathInfo() {
        return pathInfo;
    }

    /**
     * Set the action mapping.
     *
     * @param actionMApping the action mapping.
     */
    public void setActionMapping(String actionMApping) {
        this.actionMapping = actionMApping;
    }

    /**
     * Set the bean.
     *
     * @param bean the bean.
     */
    public void setBean(Bean<?> bean) {
        this.bean = bean;
    }

    /**
     * Set the mapping type.
     *
     * @param mappingType the mapping type.
     */
    public void setMappingType(ActionMappingType mappingType) {
        this.mappingType = mappingType;
    }

    /**
     * Set the method.
     *
     * @param method the method.
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Set the path info.
     * 
     * @param pathInfo the path info.
     */
    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }
}
