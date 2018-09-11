/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.requestcharencodingnosession;

import static javax.faces.application.ViewHandler.CHARACTER_ENCODING_KEY;

import java.io.Serializable;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class NoCharEncBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public String getMessage() {
        String result = "";
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        String extContextCharEnc = extContext.getResponseCharacterEncoding();

        boolean hasSession = extContext.getSession(false) != null;

        result = "extContextCharEnc: " + extContextCharEnc + " hasSession: " + hasSession;
        if (hasSession) {
            result = result + " sessionCharEnc: " + extContext.getSessionMap().get(CHARACTER_ENCODING_KEY);
        }

        if (extContext.getRequestParameterMap().containsKey("makeSession")) {
            extContext.getSession(true);
        }

        if (extContext.getRequestParameterMap().containsKey("invalidateSession")) {
            extContext.invalidateSession();
        }

        return result;
    }

}
