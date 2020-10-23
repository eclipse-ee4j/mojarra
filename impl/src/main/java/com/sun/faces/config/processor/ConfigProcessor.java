/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.config.processor;

import com.sun.faces.config.manager.documents.DocumentInfo;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This interface provides a CoR structure for processing JSF configuration resources.
 * </p>
 */
public interface ConfigProcessor {

    /**
     * Called to initialize the per-application metadata used by the ConfigProcessor
     */
    void initializeClassMetadataMap(ServletContext servletContext, FacesContext facesContext);

    /**
     * <p>
     * Process the array of <code>Document</code>s.
     * </p>
     *
     * @param sc the <code>ServletContext</code> for the application being configured
     * @param documentInfos @throws Exception if an error occurs during processing
     */
    void process(ServletContext servletContext, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception;

    void destroy(ServletContext servletContext, FacesContext facesContext);

}
