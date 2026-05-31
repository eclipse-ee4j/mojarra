/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package com.sun.faces.perf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import java.util.EnumSet;
import java.util.Set;

import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.Renderer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.sun.faces.facelets.component.UIRepeat;
import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;

/**
 * Manual performance harness for the component-tree hot paths exercised during
 * Restore View and Render Response: {@code visitTree}, {@code processSaveState},
 * {@code processRestoreState}, and {@code encodeAll} (the full encode walk).
 *
 * <p>Builds representative synthetic view trees with the actual Mojarra
 * {@link UIComponent} implementations (no facelets needed -- works against
 * {@link com.sun.faces.mock.MockFacesContext} from the existing test infra).
 * Each scenario reports the median ns/op over {@value #RUNS} measurement runs
 * of {@value #ITERATIONS} iterations each (after {@value #WARMUP} warmup
 * iterations).
 *
 * <p>Disabled by default. To run:
 * {@code mvn -pl impl test -Dtest=ComponentTreePerfHarness -Dperf=true}.
 * Output goes to stdout in a tabular form; paste into a JIRA/PR comment to
 * track before/after numbers for each phase of the perf rework.
 *
 * <p>Scenarios:
 * <ul>
 *   <li><b>flat</b> &mdash; UIViewRoot with N {@link UIOutput} children, no
 *       NamingContainers. Exercises base {@code visitTree}/iteration overhead.</li>
 *   <li><b>form_inputs</b> &mdash; UIViewRoot &gt; UIForm with N {@link UIInput}
 *       children. Exercises NamingContainer-aware {@code getClientId} and EVH
 *       processing.</li>
 *   <li><b>deep_panels</b> &mdash; UIViewRoot &gt; UIForm &gt; nested UIPanel
 *       structure. Exercises deep tree traversal.</li>
 *   <li><b>uidata</b> &mdash; UIViewRoot &gt; UIForm &gt; {@link UIData} with R
 *       rows x C columns of {@link UIInput}. Exercises per-row state save/restore
 *       on the iteration component.</li>
 * </ul>
 */
@EnabledIfSystemProperty(named = "perf", matches = "true")
public class ComponentTreePerfHarness extends JUnitFacesTestCaseBase {

    private static final int WARMUP = 100;
    private static final int ITERATIONS = 1_000;
    private static final int RUNS = 5;

    private static boolean headerPrinted = false;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        // The mock factory finder does not register a VisitContextFactory by default;
        // VisitContext.createVisitContext() needs one. Use the real Mojarra impl.
        jakarta.faces.FactoryFinder.setFactory(jakarta.faces.FactoryFinder.VISIT_CONTEXT_FACTORY,
                "com.sun.faces.component.visit.VisitContextFactoryImpl");
        // Register a no-op render kit so encodeAll has somewhere to dispatch.
        RenderKit renderKit = new MockRenderKit();
        renderKit.addRenderer(UIOutput.COMPONENT_FAMILY, "jakarta.faces.Text", new NoOpRenderer());
        renderKit.addRenderer(UIInput.COMPONENT_FAMILY, "jakarta.faces.Text", new NoOpRenderer());
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY, "jakarta.faces.Form", new NoOpRenderer());
        renderKit.addRenderer(UIPanel.COMPONENT_FAMILY, "jakarta.faces.Group", new NoOpRenderer());
        renderKit.addRenderer(UIData.COMPONENT_FAMILY, "jakarta.faces.Table", new NoOpRenderer());
        // UIColumn has no rendererType by default; nothing to register for it.
        facesContext.getApplication(); // ensure Application is initialized
        // Force the id-uniqueness check to run so the walk benchmark below measures the walk,
        // not the default "auto" Production skip.
        servletContext.addInitParameter("com.sun.faces.disableIdUniquenessCheck", "false");
        com.sun.faces.mock.MockRenderKitFactory rkf = (com.sun.faces.mock.MockRenderKitFactory) jakarta.faces.FactoryFinder
                .getFactory(jakarta.faces.FactoryFinder.RENDER_KIT_FACTORY);
        try {
            rkf.addRenderKit(jakarta.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT, renderKit);
        } catch (IllegalArgumentException alreadyAdded) {
            // ignore -- shared across tests
        }
        if (!headerPrinted) {
            System.out.println();
            System.out.println("ComponentTreePerfHarness (warmup=" + WARMUP + ", iterations=" + ITERATIONS + ", runs=" + RUNS + ")");
            System.out.println();
            System.out.printf("%-55s %12s %12s %12s %12s%n",
                    "Scenario", "visitTree", "saveState", "restoreState", "encodeAll");
            System.out.printf("%-55s %12s %12s %12s %12s%n",
                    "-".repeat(55), "-".repeat(12), "-".repeat(12), "-".repeat(12), "-".repeat(12));
            headerPrinted = true;
        }
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // -------- Scenarios -----------------------------------------------------

    @Test
    void flat_tree_100_outputs() {
        runScenario("flat tree (100 UIOutput children)", () -> buildFlatTree(100));
    }

    @Test
    void flat_tree_500_outputs() {
        runScenario("flat tree (500 UIOutput children)", () -> buildFlatTree(500));
    }

    @Test
    void form_with_50_inputs() {
        runScenario("form with 50 UIInput children", () -> buildFormWithInputs(50));
    }

    @Test
    void form_with_200_inputs() {
        runScenario("form with 200 UIInput children", () -> buildFormWithInputs(200));
    }

    @Test
    void deep_panels_5_levels_x_10_inputs() {
        runScenario("deep panels (5 levels x 10 UIInputs each level)", () -> buildDeepPanelTree(5, 10));
    }

    @Test
    void uidata_20_rows_3_cols() {
        runScenario("UIData (20 rows x 3 columns of UIInput)", () -> buildUIDataTree(20, 3));
    }

    @Test
    void uidata_100_rows_5_cols() {
        runScenario("UIData (100 rows x 5 columns of UIInput)", () -> buildUIDataTree(100, 5));
    }

    @Test
    void uidata_100_rows_5_cols_readonly() {
        runScenario("UIData (100 rows x 5 cols, UIOutput / read-only)",
                () -> buildUIDataTreeReadOnly(100, 5));
    }

    @Test
    void uidata_100_rows_5_cols_setRowIndex_cycle_readonly() {
        UIViewRoot view = buildUIDataTreeReadOnly(100, 5);
        facesContext.setViewRoot(view);
        UIData data = (UIData) view.getChildren().get(0).getChildren().get(0);
        Runnable cycle = () -> {
            data.setRowIndex(-1);
            for (int r = 0; r < 100; r++) {
                data.setRowIndex(r);
            }
            data.setRowIndex(-1);
        };
        warmUp(cycle);
        long median = medianRun(cycle);
        System.out.printf("%-55s %12s %12s %12s %12s%n",
                "UIData setRowIndex cycle read-only (100 rows)", "-", "-", "-", median);
    }

    @Test
    void uirepeat_iterate_100_rows_5_inputs() {
        UIViewRoot view = buildUIRepeatTree(100, 5, i -> {
            UIInput in = new UIInput();
            in.setId("ri" + i);
            return in;
        });
        measureRowIteration(view, "UIRepeat (100 rows x 5 UIInput) -- iterate rows");
    }

    @Test
    void uirepeat_iterate_100_rows_5_outputs_readonly() {
        UIViewRoot view = buildUIRepeatTree(100, 5, i -> {
            UIOutput out = new UIOutput();
            out.setId("ro" + i);
            return out;
        });
        measureRowIteration(view, "UIRepeat (100 rows x 5 UIOutput, read-only) -- iterate rows");
    }

    /**
     * Compares the pre-Phase-F walk (recurses into every node, including Facelets-compiled
     * UILeaf wrappers for static text) against the patched walk that skips
     * UntargetableComponent descendants. Models a realistic Facelets-compiled view where
     * every line of whitespace and every literal text fragment between tags becomes a UILeaf.
     */
    @Test
    void checkIdUniqueness_50_real_x_4_uileaf_each() {
        UIViewRoot view = buildTreeWithUILeafFillers(50, 4);
        facesContext.setViewRoot(view);

        Runnable rawWalk = () -> rawCheckIdUniqueness(
                facesContext, view, new java.util.HashSet<>(view.getChildCount() << 1));
        Runnable patchedWalk = () -> com.sun.faces.util.Util.checkIdUniqueness(
                facesContext, view, new java.util.HashSet<>(64));

        warmUp(rawWalk);
        warmUp(patchedWalk);
        long rawMedian = medianRun(rawWalk);
        long patchedMedian = medianRun(patchedWalk);
        System.out.printf("%-55s %12s %12s %12s %12s%n",
                "checkIdUniqueness (raw walk)", "-", rawMedian, "-", "-");
        System.out.printf("%-55s %12s %12s %12s %12s%n",
                "checkIdUniqueness (patched)", "-", patchedMedian, "-", "-");
    }

    /** Pre-Phase-F reproduction of Util.checkIdUniqueness without the UILeaf skip. */
    private static void rawCheckIdUniqueness(jakarta.faces.context.FacesContext context, UIComponent component, java.util.Set<String> ids) {
        for (java.util.Iterator<UIComponent> kids = component.getFacetsAndChildren(); kids.hasNext();) {
            UIComponent kid = kids.next();
            String id = kid.getClientId(context);
            if (!ids.add(id)) {
                throw new IllegalStateException("duplicate id: " + id);
            }
            rawCheckIdUniqueness(context, kid, ids);
        }
    }

    private UIViewRoot buildTreeWithUILeafFillers(int realComponents, int uileavesPerReal) {
        UIViewRoot view = new UIViewRoot();
        view.setId("v");
        view.setRenderKitId(jakarta.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT);
        UIForm form = new UIForm();
        form.setId("f");
        view.getChildren().add(form);
        for (int i = 0; i < realComponents; i++) {
            for (int j = 0; j < uileavesPerReal; j++) {
                com.sun.faces.facelets.compiler.UILeaf leaf = new com.sun.faces.facelets.compiler.UILeaf();
                leaf.setId("leaf" + i + "_" + j);
                form.getChildren().add(leaf);
            }
            UIInput in = new UIInput();
            in.setId("in" + i);
            form.getChildren().add(in);
        }
        return view;
    }

    // Per-row setRowIndex cycle is the dominant cost for data tables; measure it
    // separately so we can see the impact of UIData-specific optimizations.
    @Test
    void uidata_100_rows_5_cols_setRowIndex_cycle() {
        UIViewRoot view = buildUIDataTree(100, 5);
        facesContext.setViewRoot(view);
        UIData data = (UIData) view.getChildren().get(0).getChildren().get(0);
        Runnable cycle = () -> {
            data.setRowIndex(-1);
            for (int r = 0; r < 100; r++) {
                data.setRowIndex(r);
            }
            data.setRowIndex(-1);
        };
        warmUp(cycle);
        long median = medianRun(cycle);
        System.out.printf("%-55s %12s %12s %12s %12s%n",
                "UIData setRowIndex cycle (100 rows)", "-", "-", "-", median);
    }

    // -------- Workload builders --------------------------------------------

    /**
     * SKIP_ITERATION matches what Mojarra's own state-management strategies pass to
     * visitTree (see FaceletPartialStateManagementStrategy), so the visitTree
     * measurement reflects what production state save/restore actually sees rather
     * than the worst-case full row iteration on UIData.
     */
    private static final Set<VisitHint> STATE_VISIT_HINTS = EnumSet.of(VisitHint.SKIP_ITERATION);

    private void runScenario(String label, java.util.function.Supplier<UIViewRoot> treeBuilder) {
        UIViewRoot view = treeBuilder.get();
        facesContext.setViewRoot(view);

        VisitCallback noopCallback = (ctx, target) -> VisitResult.ACCEPT;
        VisitContext visitContext = VisitContext.createVisitContext(facesContext, null, STATE_VISIT_HINTS);

        Runnable visitTree = () -> view.visitTree(visitContext, noopCallback);
        Runnable encodeAll = () -> {
            try {
                view.encodeAll(facesContext);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        // saveState / restoreState are measured as a pair: save the current state,
        // then immediately restore it. This is what one Render+Restore cycle costs.
        Runnable saveState = () -> {
            Object state = view.processSaveState(facesContext);
            // intentionally don't restore -- saveState benchmark only
            view.getAttributes().put("_lastSaved", state);
        };

        // Pre-capture a saved state for restore measurement.
        view.markInitialState();
        Object savedState = view.processSaveState(facesContext);
        Runnable restoreState = () -> view.processRestoreState(facesContext, savedState);

        warmUp(visitTree);
        warmUp(saveState);
        warmUp(restoreState);
        warmUp(encodeAll);

        long visitMedian = medianRun(visitTree);
        long saveMedian = medianRun(saveState);
        long restoreMedian = medianRun(restoreState);
        long encodeMedian = medianRun(encodeAll);

        System.out.printf("%-55s %12d %12d %12d %12d%n",
                label, visitMedian, saveMedian, restoreMedian, encodeMedian);
    }

    private UIViewRoot buildFlatTree(int childCount) {
        UIViewRoot view = new UIViewRoot();
        view.setId("v");
        view.setRenderKitId(jakarta.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT);
        for (int i = 0; i < childCount; i++) {
            UIOutput out = new UIOutput();
            out.setId("o" + i);
            out.setValue("text-" + i);
            view.getChildren().add(out);
        }
        return view;
    }

    private UIViewRoot buildFormWithInputs(int inputCount) {
        UIViewRoot view = new UIViewRoot();
        view.setId("v");
        view.setRenderKitId(jakarta.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT);
        UIForm form = new UIForm();
        form.setId("f");
        view.getChildren().add(form);
        for (int i = 0; i < inputCount; i++) {
            UIInput in = new UIInput();
            in.setId("i" + i);
            in.setValue("v-" + i);
            form.getChildren().add(in);
        }
        return view;
    }

    private UIViewRoot buildDeepPanelTree(int depth, int inputsPerLevel) {
        UIViewRoot view = new UIViewRoot();
        view.setId("v");
        view.setRenderKitId(jakarta.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT);
        UIForm form = new UIForm();
        form.setId("f");
        view.getChildren().add(form);
        UIComponent parent = form;
        for (int d = 0; d < depth; d++) {
            UIPanel panel = new UIPanel();
            panel.setId("p" + d);
            parent.getChildren().add(panel);
            for (int i = 0; i < inputsPerLevel; i++) {
                UIInput in = new UIInput();
                in.setId("i_d" + d + "_n" + i);
                in.setValue("v-" + d + "-" + i);
                panel.getChildren().add(in);
            }
            parent = panel;
        }
        return view;
    }

    private UIViewRoot buildUIDataTree(int rows, int cols) {
        return buildUIDataTree(rows, cols, i -> {
            UIInput in = new UIInput();
            in.setId("ci" + i);
            return in;
        });
    }

    /** Read-only variant used to exercise the "no stateful descendants" fast path. */
    private UIViewRoot buildUIDataTreeReadOnly(int rows, int cols) {
        return buildUIDataTree(rows, cols, i -> {
            UIOutput out = new UIOutput();
            out.setId("co" + i);
            return out;
        });
    }

    private UIViewRoot buildUIDataTree(int rows, int cols, java.util.function.IntFunction<UIComponent> cellFactory) {
        UIViewRoot view = new UIViewRoot();
        view.setId("v");
        view.setRenderKitId(jakarta.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT);
        UIForm form = new UIForm();
        form.setId("f");
        view.getChildren().add(form);
        UIData data = new UIData();
        data.setId("data");
        List<String> rowValues = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            rowValues.add("row-" + r);
        }
        data.setValue(rowValues);
        data.setVar("row");
        form.getChildren().add(data);
        for (int c = 0; c < cols; c++) {
            UIColumn col = new UIColumn();
            col.setId("c" + c);
            col.getChildren().add(cellFactory.apply(c));
            data.getChildren().add(col);
        }
        return view;
    }

    private UIViewRoot buildUIRepeatTree(int rows, int children, java.util.function.IntFunction<UIComponent> cellFactory) {
        UIViewRoot view = new UIViewRoot();
        view.setId("v");
        view.setRenderKitId(jakarta.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT);
        UIForm form = new UIForm();
        form.setId("f");
        view.getChildren().add(form);
        UIRepeat repeat = new UIRepeat();
        repeat.setId("repeat");
        List<String> rowValues = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            rowValues.add("row-" + r);
        }
        repeat.setValue(rowValues);
        repeat.setVar("row");
        form.getChildren().add(repeat);
        for (int i = 0; i < children; i++) {
            repeat.getChildren().add(cellFactory.apply(i));
        }
        return view;
    }

    /**
     * Measures full row-iteration cost: visits the tree with iteration enabled (no
     * SKIP_ITERATION hint), so UIRepeat actually calls setIndex per row and triggers
     * the per-row save/restore machinery. The visit callback is a no-op so the
     * measurement isolates iteration overhead from rendering.
     */
    private void measureRowIteration(UIViewRoot view, String label) {
        facesContext.setViewRoot(view);
        VisitCallback noopCallback = (ctx, target) -> VisitResult.ACCEPT;
        VisitContext fullIterationVisitContext = VisitContext.createVisitContext(facesContext);

        Runnable iterate = () -> view.visitTree(fullIterationVisitContext, noopCallback);
        warmUp(iterate);
        long median = medianRun(iterate);
        System.out.printf("%-55s %12s %12s %12s %12d%n", label, "-", "-", "-", median);
    }

    // -------- Timing helpers -----------------------------------------------

    private static void warmUp(Runnable r) {
        for (int i = 0; i < WARMUP; i++) {
            r.run();
        }
    }

    private static long medianRun(Runnable r) {
        long[] times = new long[RUNS];
        for (int run = 0; run < RUNS; run++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                r.run();
            }
            times[run] = (System.nanoTime() - t0) / ITERATIONS;
        }
        Arrays.sort(times);
        return times[RUNS / 2];
    }

    // -------- No-op renderer ------------------------------------------------

    private static final class NoOpRenderer extends Renderer {
        @Override
        public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
            // no-op
        }

        @Override
        public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
            if (component.getChildCount() == 0) {
                return;
            }
            for (UIComponent child : component.getChildren()) {
                child.encodeAll(context);
            }
        }

        @Override
        public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
            // no-op
        }

        @Override
        public boolean getRendersChildren() {
            return true;
        }
    }

}
