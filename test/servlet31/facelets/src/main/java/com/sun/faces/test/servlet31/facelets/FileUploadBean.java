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

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.Part;

@Named
@RequestScoped
public class FileUploadBean {

    private Part uploadedFile;

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getFileText() {
        String fileText = "";

        if (null != uploadedFile) {
            try {
                InputStream is = uploadedFile.getInputStream();
                fileText = new Scanner(is).useDelimiter("\\A").next();
            } catch (IOException ex) {

            }
        }
        return fileText;
    }

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getProjectStage() {
        String projectStage = null;
        if (FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Development)) {
            projectStage = "ProjectStage.Development";
        }

        return projectStage;
    }
}
