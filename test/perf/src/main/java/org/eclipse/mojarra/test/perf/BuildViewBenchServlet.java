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
import java.util.Iterator;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Isolates the Facelets {@code buildView} cost — the component-tree construction the Restore View
 * phase pays on every postback under partial state saving — apart from state restore, render and
 * state save. Uses only standard {@code jakarta.faces} API, so the same endpoint runs on Eclipse
 * Mojarra and Apache MyFaces, giving an apples-to-apples buildView comparison.
 *
 * <p>{@code GET /buildview-bench?scenario=composite-unrolled&warmup=50&runs=2000} creates a fresh
 * {@link UIViewRoot} and calls {@link ViewDeclarationLanguage#buildView} that many times (the
 * compiled Facelet is cached after warmup, so each measured run rebuilds the component tree without
 * recompiling), then reports ns per build and the component count of the last tree.
 */
@WebServlet("/buildview-bench")
public class BuildViewBenchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String scenario = request.getParameter("scenario");
        if (scenario == null || scenario.isBlank()) {
            scenario = "composite-unrolled";
        }
        int warmup = intParam(request, "warmup", 50);
        int runs = intParam(request, "runs", 2000);
        String viewId = "/" + scenario + ".xhtml";

        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);

        for (int i = 0; i < warmup; i++) {
            buildOnce(facesContextFactory, lifecycle, request, response, viewId);
        }

        int count = 0;
        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            count = buildOnce(facesContextFactory, lifecycle, request, response, viewId);
        }
        long elapsedNanos = System.nanoTime() - start;

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().printf(
                "# buildview-bench scenario=%s warmup=%d runs=%d components=%d ns_per_build=%d total_ms=%d%n",
                scenario, warmup, runs, count, elapsedNanos / runs, elapsedNanos / 1_000_000L);
    }

    /**
     * One build under its own FacesContext — mirrors a real request, so each build's per-view state and
     * events are released and collectible rather than accumulating across the loop.
     */
    private int buildOnce(FacesContextFactory facesContextFactory, Lifecycle lifecycle,
            HttpServletRequest request, HttpServletResponse response, String viewId) throws IOException {
        FacesContext context = facesContextFactory.getFacesContext(getServletContext(), request, response, lifecycle);
        try {
            ViewDeclarationLanguage vdl = context.getApplication().getViewHandler().getViewDeclarationLanguage(context, viewId);
            UIViewRoot root = vdl.createView(context, viewId);
            context.setViewRoot(root);
            vdl.buildView(context, root);
            return countComponents(root);
        } finally {
            context.release();
        }
    }

    private static int countComponents(UIComponent component) {
        int count = 1;
        for (Iterator<UIComponent> it = component.getFacetsAndChildren(); it.hasNext();) {
            count += countComponents(it.next());
        }
        return count;
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
}
