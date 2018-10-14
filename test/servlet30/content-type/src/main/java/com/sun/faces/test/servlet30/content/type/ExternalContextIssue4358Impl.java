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

package com.sun.faces.test.servlet30.content.type;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextWrapper;

/**
 * @author Kyle Stiemann
 */
public class ExternalContextIssue4358Impl extends ExternalContextWrapper {

    static final String GET_RESPONSE_OUTPUT_WRITER = "getResponseOutputWriter()";
    static final String GET_RESPONSE_OUTPUT_STREAM = "getResponseOutputStream()";
    private final ExternalContext wrappedExternalContext;
    private final CopyOnWriteArrayList<String> externalContextCalls;

    public ExternalContextIssue4358Impl(ExternalContext wrappedExternalContext) {
        this.wrappedExternalContext = wrappedExternalContext;
        this.externalContextCalls = new CopyOnWriteArrayList<String>();
    }

    @Override
    public void setResponseContentType(String contentType) {
        externalContextCalls.add("setResponseContentType(\"" + contentType + "\")");
        super.setResponseContentType(contentType);
    }

    @Override
    public Writer getResponseOutputWriter() throws IOException {
        externalContextCalls.add(GET_RESPONSE_OUTPUT_WRITER);
        return super.getResponseOutputWriter();
    }

    @Override
    public OutputStream getResponseOutputStream() throws IOException {
        externalContextCalls.add(GET_RESPONSE_OUTPUT_STREAM);
        return super.getResponseOutputStream();
    }

    public List<String> getExternalContextCalls() {
        return externalContextCalls;
    }

    @Override
    public ExternalContext getWrapped() {
        return wrappedExternalContext;
    }
}