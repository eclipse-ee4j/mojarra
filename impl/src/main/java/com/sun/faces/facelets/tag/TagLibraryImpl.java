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

package com.sun.faces.facelets.tag;

import java.lang.reflect.Method;
import java.net.URL;

import com.sun.faces.util.Util;

/**
 * Concrete implementation for defining Facelet tag libraries in Java.
 */
public class TagLibraryImpl extends AbstractTagLibrary {
    public TagLibraryImpl(String namespace) {
        super(namespace);
    }

    public void putConverter(String name, String id) {
        Util.notNull("name", name);
        Util.notNull("id", id);
        this.addConverter(name, id);
    }

    public void putConverter(String name, String id, Class handlerClass) {
        Util.notNull("name", name);
        Util.notNull("id", id);
        Util.notNull("handlerClass", handlerClass);
        this.addConverter(name, id, handlerClass);
    }

    public void putValidator(String name, String id) {
        Util.notNull("name", name);
        Util.notNull("id", id);
        this.addValidator(name, id);
    }

    public void putValidator(String name, String id, Class handlerClass) {
        Util.notNull("name", name);
        Util.notNull("id", id);
        Util.notNull("handlerClass", handlerClass);
        this.addValidator(name, id, handlerClass);
    }

    public void putBehavior(String name, String id) {
        Util.notNull("name", name);
        Util.notNull("id", id);
        this.addBehavior(name, id);
    }

    public void putBehavior(String name, String id, Class handlerClass) {
        Util.notNull("name", name);
        Util.notNull("id", id);
        Util.notNull("handlerClass", handlerClass);
        this.addBehavior(name, id, handlerClass);
    }

    public void putTagHandler(String name, Class type) {
        Util.notNull("name", name);
        Util.notNull("type", type);
        addTagHandler(name, type);
    }

    public void putComponent(String name, String componentType, String rendererType) {
        Util.notNull("name", name);
        Util.notNull("componentType", componentType);
        this.addComponent(name, componentType, rendererType);
    }

    public void putComponent(String name, String componentType, String rendererType, Class handlerClass) {
        Util.notNull("name", name);
        Util.notNull("handlerClass", handlerClass);
        this.addComponent(name, componentType, rendererType, handlerClass);
    }

    public void putUserTag(String name, URL source) {
        Util.notNull("name", name);
        Util.notNull("source", source);
        addUserTag(name, source);
    }

    public void putCompositeComponentTag(String name, String resourceId) {
        Util.notNull("name", name);
        Util.notNull("resourceId", resourceId);
        addCompositeComponentTag(name, resourceId);
    }

    public void putFunction(String name, Method method) {
        Util.notNull("name", name);
        Util.notNull("method", method);
        addFunction(name, method);
    }
}
