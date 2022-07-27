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

package com.sun.faces.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.Renderer;
import jakarta.faces.render.ResponseStateManager;

public class MockRenderKit extends RenderKit {

    public MockRenderKit() {
        addRenderer(UIData.COMPONENT_FAMILY,
                "jakarta.faces.Table", new TestRenderer());
        addRenderer(UIInput.COMPONENT_FAMILY,
                "TestRenderer", new TestRenderer());
        addRenderer(UIInput.COMPONENT_FAMILY,
                "jakarta.faces.Text", new TestRenderer());
        addRenderer(UIOutput.COMPONENT_FAMILY,
                "TestRenderer", new TestRenderer());
        addRenderer(UIOutput.COMPONENT_FAMILY,
                "jakarta.faces.Text", new TestRenderer());
        addRenderer(UIPanel.COMPONENT_FAMILY,
                "jakarta.faces.Grid", new TestRenderer());
        responseStateManager = new MockResponseStateManager();
    }

    private Map<String, Renderer> renderers = new HashMap<>();
    private ResponseStateManager responseStateManager = null;

    @Override
    public void addRenderer(String family, String rendererType,
            Renderer renderer) {
        if ((family == null) || (rendererType == null) || (renderer == null)) {
            throw new NullPointerException();
        }
        renderers.put(family + "|" + rendererType, renderer);
    }

    @Override
    public Renderer getRenderer(String family, String rendererType) {
        if ((family == null) || (rendererType == null)) {
            throw new NullPointerException();
        }
        return (renderers.get(family + "|" + rendererType));
    }

    @Override
    public ResponseWriter createResponseWriter(Writer writer,
            String contentTypeList,
            String characterEncoding) {
        return new MockResponseWriter(writer, characterEncoding);
    }

    @Override
    public ResponseStream createResponseStream(OutputStream out) {
        final OutputStream os = out;
        return new ResponseStream() {
            @Override
            public void close() throws IOException {
                os.close();
            }

            @Override
            public void flush() throws IOException {
                os.flush();
            }

            @Override
            public void write(byte[] b) throws IOException {
                os.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                os.write(b, off, len);
            }

            @Override
            public void write(int b) throws IOException {
                os.write(b);
            }
        };
    }

    @Override
    public ResponseStateManager getResponseStateManager() {
        return responseStateManager;
    }

    class TestRenderer extends Renderer<UIComponent> {

        public TestRenderer() {
        }

        @Override
        public void decode(FacesContext context, UIComponent component) {

            if ((context == null) || (component == null)) {
                throw new NullPointerException();
            }

            if (!(component instanceof UIInput)) {
                return;
            }
            UIInput input = (UIInput) component;
            String clientId = input.getClientId(context);
            // System.err.println("decode(" + clientId + ")");

            // Decode incoming request parameters
            Map<String, String> params = context.getExternalContext().getRequestParameterMap();
            if (params.containsKey(clientId)) {
                // System.err.println("  '" + input.currentValue(context) +
                //                    "' --> '" + params.get(clientId) + "'");
                input.setSubmittedValue(params.get(clientId));
            }
        }

        @Override
        public void encodeBegin(FacesContext context, UIComponent component)
                throws IOException {

            if ((context == null) || (component == null)) {
                throw new NullPointerException();
            }
            ResponseWriter writer = context.getResponseWriter();
            writer.write("<text id='" + component.getClientId(context) + "' value='"
                    + component.getAttributes().get("value") + "'/>\n");
        }

        @Override
        public void encodeChildren(FacesContext context, UIComponent component)
                throws IOException {
            if ((context == null) || (component == null)) {
                throw new NullPointerException();
            }
        }

        @Override
        public void encodeEnd(FacesContext context, UIComponent component)
                throws IOException {
            if ((context == null) || (component == null)) {
                throw new NullPointerException();
            }
        }
    }
}
