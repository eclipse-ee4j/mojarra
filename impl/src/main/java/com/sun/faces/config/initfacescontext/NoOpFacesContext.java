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

package com.sun.faces.config.initfacescontext;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;

import java.util.Iterator;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;

public abstract class NoOpFacesContext extends FacesContext {

    @Override
    public Lifecycle getLifecycle() {
        return null;
    }

    @Override
    public Iterator<String> getClientIdsWithMessages() {
        return emptyIterator();
    }

    @Override
    public FacesMessage.Severity getMaximumSeverity() {
        return FacesMessage.SEVERITY_INFO;
    }

    @Override
    public Iterator<FacesMessage> getMessages() {
        return emptyIterator();
    }

    @Override
    public Iterator<FacesMessage> getMessages(String clientId) {
        return getMessages();
    }

    @Override
    public List<FacesMessage> getMessageList() {
        return emptyList();
    }

    @Override
    public List<FacesMessage> getMessageList(String clientId) {
        return emptyList();
    }

    @Override
    public RenderKit getRenderKit() {
        return null;
    }

    @Override
    public boolean getRenderResponse() {
        return true;
    }

    @Override
    public boolean getResponseComplete() {
        return true;
    }

    @Override
    public boolean isValidationFailed() {
        return false;
    }

    @Override
    public ResponseStream getResponseStream() {
        return null;
    }

    @Override
    public void setResponseStream(ResponseStream responseStream) {
    }

    @Override
    public ResponseWriter getResponseWriter() {
        return null;
    }

    @Override
    public void setResponseWriter(ResponseWriter responseWriter) {
    }

    @Override
    public void setViewRoot(UIViewRoot root) {
    }

    @Override
    public void addMessage(String clientId, FacesMessage message) {
    }

    @Override
    public void renderResponse() {
    }

    @Override
    public void responseComplete() {
    }

    @Override
    public void validationFailed() {
    }

}
