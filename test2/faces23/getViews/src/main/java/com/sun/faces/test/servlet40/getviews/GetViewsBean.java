/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

/**
 *
 * @author mmueller
 */
import static jakarta.faces.annotation.FacesConfig.Version.JSF_2_3;
import static jakarta.faces.application.ViewVisitOption.RETURN_AS_MINIMAL_IMPLICIT_OUTCOME;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.annotation.FacesConfig;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.application.ViewVisitOption;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.inject.Inject;
import jakarta.inject.Named;


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
