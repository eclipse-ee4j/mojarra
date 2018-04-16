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

package com.sun.faces.test.servlet30.eldecoratedfacescontext;

import javax.faces.FacesWrapper;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExceptionHandlerWrapper;

public class MyExceptionHandlerFactory extends ExceptionHandlerFactory implements FacesWrapper<ExceptionHandlerFactory> {
    
    private final ExceptionHandlerFactory parent;

    public MyExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }

    @Override
    public ExceptionHandlerFactory getWrapped() {
        return parent;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new MyExceptionHandler(getWrapped().getExceptionHandler());
    }
    
    private static class MyExceptionHandler extends ExceptionHandlerWrapper implements FacesWrapper<ExceptionHandler> {
        private final ExceptionHandler parent;

        public MyExceptionHandler(ExceptionHandler parent) {
            this.parent = parent;
        }

        @Override
        public ExceptionHandler getWrapped() {
            return parent;
        }
        
    }

}
