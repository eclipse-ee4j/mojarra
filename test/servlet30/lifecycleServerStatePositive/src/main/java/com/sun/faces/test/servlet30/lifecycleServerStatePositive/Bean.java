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

package com.sun.faces.test.servlet30.lifecycleServerStatePositive;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.event.ActionEvent;

@Named
@RequestScoped
public class Bean {

    public void putNonSerializableDataInState(ActionEvent ae) {
        ae.getComponent().getAttributes().put("myAttribute", new NotSerializableClass());
    }

    private static class NotSerializableClass implements Serializable {
        private void writeObject(ObjectOutputStream out) throws IOException {
            throw new NotSerializableException("This class is not really Serializable");
        }

        private void readObject(ObjectInputStream in) throws IOException {
            throw new NotSerializableException("This class is not really Serializable");
        }
    }

}
