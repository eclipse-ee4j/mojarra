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

package jakarta.faces.validator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * <p>
 * Unit tests for JLS casting rules</p>
 */
public class CastingValidatorTestCase extends ValidatorTestCase {

    // ------------------------------------------------- Individual Test Methods
    @Test
    @SuppressWarnings("rawtypes")
    public void testWithGenericCanCastToRaw() {
        Validator<?> validatorWithGeneric = (context, component, value) -> {};
        Validator validatorRaw = validatorWithGeneric;
        assertNotNull(validatorRaw);
    }

}
