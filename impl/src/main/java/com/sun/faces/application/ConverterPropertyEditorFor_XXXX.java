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

package com.sun.faces.application;

import java.util.Date;

/**
 * Default template class for the dynamic generation of target-class specific PropertyEditor implementations.
 */
public class ConverterPropertyEditorFor_XXXX extends ConverterPropertyEditorBase {
    @Override
    protected Class<?> getTargetClass() {
        // Doesn't really matter what this is, since it get's replaced when the
        // concrete PropertyEditor class is generated (it can be any valid class
        // reference that is not otherwise refered to in this class -- so don't
        // make it ConverterPropertyEditorBase or
        // ConverterPropertyEditorFor_XXXX).
        return Date.class;
    }
}
