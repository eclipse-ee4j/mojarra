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

package com.sun.faces.facelets;

import java.io.IOException;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;

/**
 *
 * @author edburns
 */
public abstract class FaceletContextImplBase extends FaceletContext {

    /**
     * Push the passed TemplateClient onto the stack for Definition Resolution
     *
     * @param client
     * @see TemplateClient
     */
    public abstract void pushClient(TemplateClient client);

    /**
     * Pop the last added TemplateClient
     *
     * @see TemplateClient
     */
    public abstract void popClient(TemplateClient client);

    public abstract void extendClient(TemplateClient client);

    /**
     * This method will walk through the TemplateClient stack to resolve and apply the definition for the passed name. If
     * it's been resolved and applied, this method will return true.
     *
     * @param parent the UIComponent to apply to
     * @param name name or null of the definition you want to apply
     * @return true if successfully applied, otherwise false
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    public abstract boolean includeDefinition(UIComponent parent, String name) throws IOException, FaceletException, FacesException, ELException;

}
