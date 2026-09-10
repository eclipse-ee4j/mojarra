/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue3263;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The variable scope of a tag file: what the attributes of an invocation reach, and what a tag file may leave behind.
 * <p>
 * These assertions hold on any implementation which isolates the parameter namespace of a tag file, so they are
 * portable rather than specific to this implementation, though the one about a parameter written inside an invocation
 * states a rule rather than established practice, the other implementation being inconsistent about it. What variables
 * in scope at the invocation site reach inside a tag file is deliberately not asserted, because implementations
 * disagree about it.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/3263">issue 3263</a>
 */
class Issue3263IT extends BaseIT {

    @FindBy(id = "nestedInvocation")
    private WebElement nestedInvocation;

    @FindBy(id = "variableEscape")
    private WebElement variableEscape;

    @FindBy(id = "parameterEscape")
    private WebElement parameterEscape;

    @FindBy(id = "compositeParameterEscape")
    private WebElement compositeParameterEscape;

    @FindBy(id = "scopedVariables")
    private WebElement scopedVariables;

    @Test
    void testNestedInvocationDoesNotInheritTheAttributesOfTheEnclosingOne() {
        open("issue3263.xhtml");
        assertEquals("(A:(:))", nestedInvocation.getText(), "The nested invocation supplies no attribute, so its own #{attribute} must resolve to nothing rather than to the value the enclosing invocation was given");
    }

    @Test
    void testVariableSetInsideATagFileDoesNotOutliveTheInvocation() {
        open("issue3263.xhtml");
        assertEquals("SET[]", variableEscape.getText(), "A variable the tag file sets with c:set must not be visible to the page after the invocation, the same way it is not after a ui:include");
    }

    @Test
    void testParameterWrittenInsideAnInvocationDoesNotOutliveIt() {
        open("issue3263.xhtml");
        assertEquals("IN()[]", parameterEscape.getText(), "A ui:param written as a child of the invocation must not be visible to the page after it, the same way it is not on a ui:include or a ui:decorate");
    }

    @Test
    void testParameterWrittenInsideACompositeComponentReachesNeitherItsImplementationNorThePage() {
        open("issue3263.xhtml");
        assertEquals("(A::[body])[]", compositeParameterEscape.getText(), "A composite component takes its parameters from its declared attributes, so a ui:param written inside its usage must reach neither the implementation nor the page after it, while the children it is written among still see it");
    }

    @Test
    void testScopedVariablesRemainVisibleInsideATagFile() {
        open("issue3263.xhtml");
        assertEquals("[APP|SES|VIEW|REQ]", scopedVariables.getText(), "A c:set carrying an explicit scope writes to that scope, which the scoped attribute EL resolver reaches from anywhere, so isolating the parameters of an invocation must not hide it");
    }
}
