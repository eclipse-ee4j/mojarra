/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Contributors to Eclipse Foundation.
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

import static jakarta.faces.application.ResourceVisitOption.TOP_LEVEL_VIEWS_ONLY;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.ResourceVisitOption;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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
