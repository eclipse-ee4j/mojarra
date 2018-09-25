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

package com.sun.faces.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;

/**
 * <p>Base task for generators.</p>
 */
public abstract class AbstractGeneratorTask extends Java {

    /**
     * <p>The fully qualified path to the properties file to drive the
     * Generator.</p>
     */
    protected String generatorConfig;

    /**
     * <p>The fully qualified <code>Generator</code> class.</p>
     */
    private String generatorClass;

    /**
     * <p>The fully qualified path to the faces-config.xml to serve
     * as the model for the <code>Generator</code>.</p>
     */
    private String facesConfig;


    // ---------------------------------------------------------- Public Methods


    public void setGeneratorConfig(String generatorConfig) {
        this.generatorConfig = generatorConfig;
    }

    public void setFacesConfig(String facesConfig) {
        this.facesConfig = facesConfig;
    }


    public void setGeneratorClass(String generatorClass) {
        this.generatorClass = generatorClass;
    }


    @Override
    public void execute() throws BuildException {
        super.createArg().setValue(generatorConfig);
        super.createArg().setValue(facesConfig);
        super.setClassname(generatorClass);

        super.execute();
    }

}
