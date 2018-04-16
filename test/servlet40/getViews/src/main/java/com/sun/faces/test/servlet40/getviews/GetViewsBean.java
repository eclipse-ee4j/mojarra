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

package com.sun.faces.test.servlet40.getviews;

import static java.util.stream.Collectors.toList;
/**
 *
 * @author mmueller
 */
import static javax.faces.annotation.FacesConfig.Version.JSF_2_3;
import static javax.faces.application.ViewVisitOption.RETURN_AS_MINIMAL_IMPLICIT_OUTCOME;

import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.FacesConfig;
import javax.faces.annotation.ManagedProperty;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewVisitOption;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.inject.Inject;
import javax.inject.Named;


@FacesConfig(
	version = JSF_2_3 
)
@Named
@RequestScoped
public class GetViewsBean {
    
    @Inject
    private FacesContext context;
   
    @Inject @ManagedProperty("#{param['path']}")
    private String path;
    
    @Inject @ManagedProperty("#{param['maxDepth']}")
    private Integer maxDepth;
    
    @Inject @ManagedProperty("#{param['implicit']}")
    private boolean implicit;
    
    @Inject @ManagedProperty("#{param['fromVDL']}")
    private boolean fromVDL;
    
    public List<String> getViews() {
        
        ViewHandler viewHandler = context.getApplication().getViewHandler();
        
        path = path != null && !path.isEmpty() ? path : "/";
        ViewVisitOption[] options = implicit? new ViewVisitOption[] {RETURN_AS_MINIMAL_IMPLICIT_OUTCOME} : new ViewVisitOption[] {};
        Stream<String> views;
        
        if (fromVDL) {
            // Get Facelets VDL
            ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(context, "/foo.xhtml");
            if (maxDepth != null) {
                views = vdl.getViews(context, path, maxDepth, options);
            } else {
                views = vdl.getViews(context, path, options);
            }
        } else {
            if (maxDepth != null) {
                views = viewHandler.getViews(context, path, maxDepth, options);
            } else {
                views = viewHandler.getViews(context, path, options);
            }
            
        }
        
        return views.collect(toList());
    }
    
  
}
