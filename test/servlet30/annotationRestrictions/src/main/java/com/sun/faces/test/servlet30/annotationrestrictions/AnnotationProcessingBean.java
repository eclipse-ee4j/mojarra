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

package com.sun.faces.test.servlet30.annotationrestrictions;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.mgbean.BeanManager;

import javax.faces.bean.ManagedBean;

@ManagedBean(name="ap")
public class AnnotationProcessingBean {

    public String getValidationResult() {

        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        BeanManager manager = associate.getBeanManager();
        if (manager.isManaged("notFoundWebInfClasses")) {
            return "FAILED : FOUND NotFoundWebInfClasses";
        }
        if (manager.isManaged("notFoundWebInfLib")) {
            return "FAILED : FOUND NotFoundWebInfLib";
        }
        if (manager.isManaged("notFoundWebInfLib2")) {
            return "FAILED : FOUND NotFoundWebInfLib2";
        }
        if (!manager.isManaged("foundWebInfLib")) {
            return "FAILED : NOT FOUND FoundWebInfLib";
        }
        return "PASSED";
    }
}
