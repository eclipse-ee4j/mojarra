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

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;

/**
 * FaceletHandlers can implement this contract and push themselves into the FaceletContext for participating in
 * templating. Templates will attempt to resolve content for a specified name until one of the TemplatClients return
 * 'true'.
 *
 * @author Jacob Hookom
 */
public interface TemplateClient {

    /**
     * This contract is much like the normal FaceletHandler.apply method, but it takes in an optional String name which
     * tells this instance what fragment/definition it's looking for. If you are a match, apply your logic to the passed
     * UIComponent and return true, otherwise do nothing and return false.
     *
     * @param ctx the FaceletContext of <i>your</i> instance, not the templates'
     * @param parent current UIComponent instance to be applied
     * @param name the String name or null if the whole body should be included
     * @return true if this client matched/applied the definition for the passed name
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException;

}
