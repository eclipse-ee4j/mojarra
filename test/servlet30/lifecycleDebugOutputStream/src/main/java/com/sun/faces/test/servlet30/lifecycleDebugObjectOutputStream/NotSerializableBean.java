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

package com.sun.faces.test.servlet30.lifecycleDebugObjectOutputStream;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;

@Named
@RequestScoped
public class NotSerializableBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        throw new NotSerializableException("Intentional failure");
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException("Intentional failure");

    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new NotSerializableException("Intentional failure");

    }

    private String putBadBeanInViewScope = "";

    public String getPutBadBeanInViewScope() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> queryParams = context.getExternalContext().getRequestParameterMap();
        if (queryParams.containsKey("fail")) {
            context.getViewRoot().getViewMap(true).put("badBean", this);
        }
        return putBadBeanInViewScope;
    }

    public void setPutBadBeanInViewScope(String putBadBeanInViewScope) {
        this.putBadBeanInViewScope = putBadBeanInViewScope;
    }

}
