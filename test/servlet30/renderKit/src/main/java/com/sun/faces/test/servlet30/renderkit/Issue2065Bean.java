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

import java.io.Serializable;
import java.util.Iterator;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.FactoryFinder;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

@ManagedBean(name = "issue2065Bean")
@RequestScoped
public class Issue2065Bean implements Serializable {

    private RenderKit renderkit;

    public Issue2065Bean() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        RenderKitFactory renderFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderkit = renderFactory.getRenderKit(facesContext, facesContext.getViewRoot().getRenderKitId());
        Issue2065ClientBehaviorRenderer clientBehaviorRenderer = new Issue2065ClientBehaviorRenderer();
        renderkit.addClientBehaviorRenderer("com.sun.faces.test.agnostic.renderKit.basic.Issue2065ClientBehaviorRenderer", clientBehaviorRenderer);
    }

    public String getClientBehaviorRendererTypes() {
        String types = "";
        for (Iterator<String> iter = renderkit.getClientBehaviorRendererTypes(); iter.hasNext();) {
            types += iter.next() + ";";
        }
        return types;
    }
}
