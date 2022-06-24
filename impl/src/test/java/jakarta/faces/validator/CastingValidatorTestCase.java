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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for JLS casting rules</p>
 */
public class CastingValidatorTestCase extends ValidatorTestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public CastingValidatorTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(CastingValidatorTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    public void testWithGenericCanCastToRaw() {
    	
    	Validator<?> validatorWithGeneric = (context, component, value) -> {};
    	
    	Validator validatorRaw = validatorWithGeneric;
    }
    
}
