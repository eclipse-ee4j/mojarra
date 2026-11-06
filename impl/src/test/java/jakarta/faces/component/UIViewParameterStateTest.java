/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package jakarta.faces.component;

import static com.sun.faces.junit.Serialization.serializeAndDeserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;

/**
 * A {@link UIViewParameter} keeps its submitted value in its saved state, so that it survives a postback.
 */
class UIViewParameterStateTest extends JUnitFacesTestCaseBase {

    @Test
    void submittedValueSurvivesSaveAndRestore() throws Exception {
        UIViewParameter parameter = new UIViewParameter();
        parameter.markInitialState();
        parameter.setSubmittedValue("submitted");
        Object state = serializeAndDeserialize(parameter.saveState(facesContext));

        UIViewParameter restored = new UIViewParameter();
        restored.markInitialState();
        restored.restoreState(facesContext, state);

        assertEquals("submitted", restored.getSubmittedValue());
    }
}
