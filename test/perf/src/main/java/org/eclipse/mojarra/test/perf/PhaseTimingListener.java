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

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

/**
 * Records per-phase wall-clock time per scenario (view-id). Scenario name is
 * derived from the view-id: {@code /form-inputs.xhtml} → {@code form-inputs}.
 */
public class PhaseTimingListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    private static final String START_KEY = "perf.phaseStart.";

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        event.getFacesContext().getAttributes().put(key(event.getPhaseId()), System.nanoTime());
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        long now = System.nanoTime();
        FacesContext context = event.getFacesContext();
        Object start = context.getAttributes().remove(key(event.getPhaseId()));
        if (!(start instanceof Long)) {
            return;
        }
        PerfStats.INSTANCE.record(resolveScenario(context), event.getPhaseId(), now - (Long) start);
    }

    private static String key(PhaseId phaseId) {
        return START_KEY + phaseId.getOrdinal();
    }

    private static String resolveScenario(FacesContext context) {
        String viewId = context.getViewRoot() != null ? context.getViewRoot().getViewId() : null;
        if (viewId == null) {
            return "<no-view>";
        }
        int start = viewId.startsWith("/") ? 1 : 0;
        int dot = viewId.lastIndexOf('.');
        return viewId.substring(start, dot > start ? dot : viewId.length());
    }
}
