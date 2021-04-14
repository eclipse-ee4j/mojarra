/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.rest;

import java.io.IOException;
import java.io.Writer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

/**
 * The JSON (application/json) response writer.
 */
@ApplicationScoped
@RestResponseWriterContentType("application/json")
public class DefaultJsonResponseWriter implements RestResponseWriter {

    /**
     * Stores the JSON-B builder.
     */
    private final JsonbBuilder jsonbBuilder;

    /**
     * Stores the JSON-B context.
     */
    private final Jsonb jsonb;

    /**
     * Constructor.
     */
    public DefaultJsonResponseWriter() {
        jsonbBuilder = JsonbBuilder.newBuilder();
        jsonb = jsonbBuilder.build();
    }
    
    @Override
    public void writeResponse(FacesContext facesContext) {
        Object result = facesContext.getAttributes().get(
                RestLifecycle.class.getPackage().getName() + ".RestResult");
        if (result == null) {
            try {
                facesContext.getExternalContext().responseSendError(204, "No content");
                facesContext.responseComplete();
            } catch (IOException ioe) {
                throw new FacesException(ioe);
            }
        } else {
            try {
                Writer writer = facesContext.getExternalContext().getResponseOutputWriter();
                writer.write(jsonb.toJson(result));
                writer.flush();
                facesContext.responseComplete();
            } catch (IOException ioe) {
                throw new FacesException(ioe);
            }
        }
    }
}
