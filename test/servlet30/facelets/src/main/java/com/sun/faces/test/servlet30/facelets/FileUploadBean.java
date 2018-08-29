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

package com.sun.faces.test.servlet30.facelets;

import static javax.faces.application.ProjectStage.Development;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;
import javax.servlet.http.Part;

@Named
@RequestScoped
public class FileUploadBean {

    private Part uploadedFile;
    private String text;

    public void processEvent(AjaxBehaviorEvent event) throws AbortProcessingException {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("ajax listener was called"));
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileText() {
        String text = "";

        if (uploadedFile != null) {
            try (
                InputStream is = uploadedFile.getInputStream();
                Scanner scanner = new Scanner(is).useDelimiter("\\A");
            ) {
                text = scanner.next();
            } catch (IOException ex) {
            }
        }
        return text;
    }

    public String getProjectStage() {
        if (FacesContext.getCurrentInstance().isProjectStage(Development)) {
            return "ProjectStage.Development";
        }

        return null;
    }


}
