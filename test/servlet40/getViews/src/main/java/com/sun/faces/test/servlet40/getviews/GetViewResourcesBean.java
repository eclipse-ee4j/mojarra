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

package com.sun.faces.test.servlet40.getviews;

import static java.util.stream.Collectors.toList;
import static javax.faces.application.ResourceVisitOption.TOP_LEVEL_VIEWS_ONLY;

import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.ManagedProperty;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceVisitOption;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class GetViewResourcesBean {
    
    @Inject
    private FacesContext context;
    
    @Inject @ManagedProperty("#{param['path']}")
    private String path;
    
    @Inject @ManagedProperty("#{param['maxDepth']}")
    private Integer maxDepth;
    
    @Inject @ManagedProperty("#{param['topLevel']}")
    private boolean topLevel;
    
    public List<String> getViewResources() {
        
        ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
        
        path = path != null && !path.isEmpty() ? path : "/";
        ResourceVisitOption[] options = topLevel? new ResourceVisitOption[] {TOP_LEVEL_VIEWS_ONLY} : new ResourceVisitOption[] {};
        Stream<String> viewResources;
        
        if (maxDepth != null) {
            viewResources = resourceHandler.getViewResources(context, path, maxDepth, options);
        } else {
            viewResources = resourceHandler.getViewResources(context, path, options);
        }
        
        return viewResources.collect(toList());
    }

}
