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

package com.sun.faces.test.servlet31.facelets;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.Part;

@Named
public class FileAjaxUploadBean {

    public Part getFile() {
        return null;
    }

    public void setFile(Part p) {
        FacesMessage msg = new FacesMessage("file 1 is saved");
        msg.setSeverity(SEVERITY_ERROR);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public Part getFile2() {
        return null;
    }

    public void setFile2(Part p) {
        FacesMessage msg = new FacesMessage("file 2 is saved");
        msg.setSeverity(SEVERITY_ERROR);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}
