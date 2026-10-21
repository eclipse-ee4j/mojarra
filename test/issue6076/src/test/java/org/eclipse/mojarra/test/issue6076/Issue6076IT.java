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
package org.eclipse.mojarra.test.issue6076;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The scope the body of a tag file invocation falls back to: the one in effect where the tag file inserts it.
 * <p>
 * The body resolves its names where the page wrote it first. A name that scope does not hold resolves against the scope
 * of the tag file at the point of insertion, which holds the parameters of the invocation and whatever the tag file set
 * before inserting the body. This holds for markup evaluated while the view is built and for a value expression
 * evaluated when it renders.
 * <p>
 * The specification does not define what the body of a tag file invocation resolves, so this fallback is specific to
 * this implementation.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6076">issue 6076</a>
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6027">issue 6027</a>
 */
class Issue6076IT extends BaseIT {

    @FindBy(id = "insertionScope")
    private WebElement insertionScope;

    @FindBy(id = "writtenScope")
    private WebElement writtenScope;

    @FindBy(id = "namedInsertion")
    private WebElement namedInsertion;

    @FindBy(id = "parameterInBody")
    private WebElement parameterInBody;

    @FindBy(id = "invocationInBody")
    private WebElement invocationInBody;

    @Test
    void testInsertedBodyFallsBackToTheScopeAtTheInsertion() {
        open("issue6076.xhtml");
        assertEquals("[H:L1:P/L1][H:L2:P/L2]", insertionScope.getText(), "A name the page does not hold where it wrote the body must resolve to what the tag file holds at each insertion, which is the variable it set before that insertion and the parameter of the invocation");
    }

    @Test
    void testInsertedBodyPrefersTheScopeItWasWrittenIn() {
        open("issue6076.xhtml");
        assertEquals("[H:W/W][H:W/W]", writtenScope.getText(), "A name the page holds where it wrote the body must resolve to that value, even where the tag file inserting the body holds the same name");
    }

    @Test
    void testNamedInsertionFallsBackToTheScopeAtTheInsertion() {
        open("issue6076.xhtml");
        assertEquals("(N)", namedInsertion.getText(), "A ui:define in the body must fall back to what the tag file holds where a named ui:insert inserts it");
    }

    @Test
    void testParameterInBodyCombinesWithTheFallbackAndReachesTheBodyAlone() {
        open("issue6076.xhtml");
        assertEquals("[O:L1][O:L2]()", parameterInBody.getText(), "A ui:param written in the body must reach the body next to a name resolved by the fallback, and not the page around the tag");
    }

    @Test
    void testTagFileInvokedInBodyInheritsNoParameterOfEitherScope() {
        open("issue6076.xhtml");
        assertEquals("[O{:}][O{:}]", invocationInBody.getText(), "A tag file invoked from the body must resolve neither a parameter of the invocation which wrote the body nor one of the invocation inserting it");
    }
}
