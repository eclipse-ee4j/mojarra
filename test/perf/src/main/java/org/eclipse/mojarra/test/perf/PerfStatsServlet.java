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
import java.io.PrintWriter;
import java.util.Map;

import org.eclipse.mojarra.test.perf.PerfStats.Snapshot;

import jakarta.faces.event.PhaseId;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Dumps accumulated phase timings.
 *
 * <ul>
 *   <li>{@code GET /perf-stats} → fixed-width plain-text table.</li>
 *   <li>{@code GET /perf-stats?format=json} → JSON (easier to diff between runs).</li>
 *   <li>{@code GET /perf-stats?reset=1} → clear all and return {@code RESET OK}.</li>
 * </ul>
 */
@WebServlet("/perf-stats")
public class PerfStatsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final PhaseId[] MATRIX_PHASES = {
            PhaseId.RESTORE_VIEW, PhaseId.APPLY_REQUEST_VALUES, PhaseId.PROCESS_VALIDATIONS,
            PhaseId.UPDATE_MODEL_VALUES, PhaseId.INVOKE_APPLICATION, PhaseId.RENDER_RESPONSE };

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");

        if ("1".equals(request.getParameter("reset"))) {
            PerfStats.INSTANCE.reset();
            response.setContentType("text/plain");
            response.getWriter().write("RESET OK\n");
            return;
        }

        Map<String, Map<PhaseId, Snapshot>> snapshot = PerfStats.INSTANCE.snapshot();
        Map<PhaseId, Snapshot> totals = PerfStats.INSTANCE.totalsByPhase();

        if ("json".equals(request.getParameter("format"))) {
            response.setContentType("application/json");
            writeJson(response.getWriter(), snapshot, totals);
        }
        else {
            response.setContentType("text/plain");
            writeText(response.getWriter(), snapshot, totals);
        }
    }

    private static void writeText(PrintWriter out, Map<String, Map<PhaseId, Snapshot>> data, Map<PhaseId, Snapshot> totals) {
        out.println("# Faces perf stats (times in microseconds)");
        out.println();
        out.printf("%-22s %-26s %8s %12s %10s %10s %10s%n",
                "scenario", "phase", "count", "total_us", "avg_us", "min_us", "max_us");
        out.println("--------------------------------------------------------------------------------------------------------");

        for (Map.Entry<String, Map<PhaseId, Snapshot>> scenarioEntry : data.entrySet()) {
            String scenario = scenarioEntry.getKey();
            Map<PhaseId, Snapshot> byPhase = scenarioEntry.getValue();
            for (PhaseId phaseId : PhaseId.VALUES) {
                Snapshot s = byPhase.get(phaseId);
                if (s == null) {
                    continue;
                }
                out.printf("%-22s %-26s %8d %12d %10d %10d %10d%n",
                        scenario,
                        phaseId.getName(),
                        s.count,
                        s.totalNanos / 1000,
                        s.avgNanos() / 1000,
                        s.minNanos / 1000,
                        s.maxNanos / 1000);
            }
        }

        out.println("--------------------------------------------------------------------------------------------------------");
        out.println("# Totals by phase (across all scenarios)");
        for (PhaseId phaseId : PhaseId.VALUES) {
            Snapshot s = totals.get(phaseId);
            if (s == null) {
                continue;
            }
            out.printf("%-22s %-26s %8d %12d %10d %10d %10d%n",
                    "<TOTAL>",
                    phaseId.getName(),
                    s.count,
                    s.totalNanos / 1000,
                    s.avgNanos() / 1000,
                    s.minNanos / 1000,
                    s.maxNanos / 1000);
        }

        out.println("--------------------------------------------------------------------------------------------------------");
        out.println("# Averages by scenario (avg_us per phase; total_us = summed total across all phases)");
        out.printf("%-22s %8s %8s %8s %8s %8s %8s %12s%n",
                "scenario", "RV", "ARV", "PV", "UMV", "IA", "RR", "total_us");
        for (Map.Entry<String, Map<PhaseId, Snapshot>> scenarioEntry : data.entrySet()) {
            Map<PhaseId, Snapshot> byPhase = scenarioEntry.getValue();
            StringBuilder row = new StringBuilder(String.format("%-22s", scenarioEntry.getKey()));
            long totalUs = 0;
            for (PhaseId phaseId : MATRIX_PHASES) {
                Snapshot s = byPhase.get(phaseId);
                if (s == null) {
                    row.append(String.format(" %8s", "-"));
                }
                else {
                    row.append(String.format(" %8d", s.avgNanos() / 1000));
                    totalUs += s.totalNanos / 1000;
                }
            }
            row.append(String.format(" %12d", totalUs));
            out.println(row);
        }
    }

    private static void writeJson(PrintWriter out, Map<String, Map<PhaseId, Snapshot>> data, Map<PhaseId, Snapshot> totals) {
        out.write('{');
        out.write("\"scenarios\":{");
        boolean firstScenario = true;
        for (Map.Entry<String, Map<PhaseId, Snapshot>> scenarioEntry : data.entrySet()) {
            if (!firstScenario) {
                out.write(',');
            }
            firstScenario = false;
            out.write('"');
            out.write(scenarioEntry.getKey());
            out.write("\":{");
            boolean firstPhase = true;
            for (Map.Entry<PhaseId, Snapshot> phaseEntry : scenarioEntry.getValue().entrySet()) {
                if (!firstPhase) {
                    out.write(',');
                }
                firstPhase = false;
                writePhaseJson(out, phaseEntry.getKey(), phaseEntry.getValue());
            }
            out.write('}');
        }
        out.write("},\"totals\":{");
        boolean firstPhase = true;
        for (Map.Entry<PhaseId, Snapshot> phaseEntry : totals.entrySet()) {
            if (!firstPhase) {
                out.write(',');
            }
            firstPhase = false;
            writePhaseJson(out, phaseEntry.getKey(), phaseEntry.getValue());
        }
        out.write("}}");
    }

    private static void writePhaseJson(PrintWriter out, PhaseId phaseId, Snapshot s) {
        out.write('"');
        out.write(phaseId.getName());
        out.write("\":{\"count\":");
        out.print(s.count);
        out.write(",\"totalNanos\":");
        out.print(s.totalNanos);
        out.write(",\"minNanos\":");
        out.print(s.minNanos);
        out.write(",\"maxNanos\":");
        out.print(s.maxNanos);
        out.write('}');
    }
}
