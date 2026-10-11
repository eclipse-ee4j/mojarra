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

package com.sun.faces.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import jakarta.faces.component.UIOutput;

class PropertyAccessorsTest {

    public static class Output extends UIOutput {
        public String getText() {
            throw new IllegalStateException("text");
        }
    }

    public static class OtherOutput extends UIOutput {
    }

    @Test
    void getterOfASharedClassHasOneFunctionForAllCallers() throws Exception {
        // As when each deployed application has its own subclass: no new generated class per subclass.
        Function<Object, Object> reader = PropertyAccessors.reader(Output.class.getMethod("getValue"));
        assertSame(reader, PropertyAccessors.reader(OtherOutput.class.getMethod("getValue")));
        assertSame(reader, PropertyAccessors.reader(UIOutput.class.getMethod("getValue")));

        Output output = new Output();
        output.setValue("v");
        assertEquals("v", reader.apply(output));
    }

    @Test
    void getterExceptionPropagatesUnwrapped() throws Exception {
        Function<Object, Object> reader = PropertyAccessors.reader(Output.class.getMethod("getText"));
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> reader.apply(new Output()));
        assertEquals("text", e.getMessage());
    }
}
