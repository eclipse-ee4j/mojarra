/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.perf;

import java.io.IOException;
import java.io.Writer;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Isolates the Render Response encode cost — the {@code encodeAll} tree walk — apart from buildView,
 * state restore and state save. The scenario view is built once, then re-rendered to a discarding
 * counting writer; uses only standard {@code jakarta.faces} API, so the same endpoint runs on Mojarra
 * and MyFaces for an apples-to-apples render comparison.
 *
 * <p>{@code GET /encodeall-bench?scenario=composite-build&warmup=50&runs=2000} reports ns per
 * render and the output character count.
 */
@WebServlet("/encodeall-bench")
public class EncodeAllBenchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String scenario = request.getParameter("scenario");
        if (scenario == null || scenario.isBlank()) {
            scenario = "composite-build";
        }
        int warmup = intParam(request, "warmup", 50);
        int runs = intParam(request, "runs", 2000);
        String viewId = "/" + scenario + ".xhtml";

        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        FacesContext context = facesContextFactory.getFacesContext(getServletContext(), request, response, lifecycle);

        long elapsedNanos;
        long chars;
        try {
            ViewHandler viewHandler = context.getApplication().getViewHandler();
            ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(context, viewId);

            UIViewRoot root = vdl.createView(context, viewId);
            context.setViewRoot(root);
            vdl.buildView(context, root); // build once; the loop measures only the encode walk

            CountingWriter sink = new CountingWriter();
            ResponseWriter writer = context.getRenderKit().createResponseWriter(sink, "text/html", "UTF-8");
            context.setResponseWriter(writer);

            for (int i = 0; i < warmup; i++) {
                sink.count = 0;
                root.encodeAll(context);
            }

            long start = System.nanoTime();
            for (int i = 0; i < runs; i++) {
                sink.count = 0;
                root.encodeAll(context);
            }
            elapsedNanos = System.nanoTime() - start;
            chars = sink.count;
        } finally {
            context.release();
        }

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().printf(
                "# encodeall-bench scenario=%s warmup=%d runs=%d chars=%d ns_per_render=%d total_ms=%d%n",
                scenario, warmup, runs, chars, elapsedNanos / runs, elapsedNanos / 1_000_000L);
    }

    private static int intParam(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Discards rendered output, counting characters so the work isn't dead-code-eliminated. */
    private static final class CountingWriter extends Writer {
        private long count;

        @Override
        public void write(char[] cbuf, int off, int len) {
            count += len;
        }

        @Override
        public void write(int c) {
            count++;
        }

        @Override
        public void write(String str, int off, int len) {
            count += len;
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() {
            // no-op
        }
    }
}
