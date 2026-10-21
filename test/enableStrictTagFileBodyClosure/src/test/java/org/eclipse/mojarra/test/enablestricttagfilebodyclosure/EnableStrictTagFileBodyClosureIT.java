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
package org.eclipse.mojarra.test.enablestricttagfilebodyclosure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The webapp declares {@code com.sun.faces.enableStrictTagFileBodyClosure}, so the body of a tag file invocation
 * resolves its names against the scope the page which wrote it is in alone. A name only the tag file holds at the
 * insertion resolves to nothing in the body, while a name the page holds, a {@code ui:param} the body carries, and the
 * isolation of the parameters of a tag file invoked from the body behave as they do without the parameter.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6076">issue 6076</a>
 */
class EnableStrictTagFileBodyClosureIT extends BaseIT {

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
    void testInsertedBodyDoesNotFallBackToTheScopeAtTheInsertion() {
        open("enableStrictTagFileBodyClosure.xhtml");
        assertEquals("[H::/][H::/]", insertionScope.getText(), "A name the page does not hold where it wrote the body must resolve to nothing, even where the tag file holds it at the insertion");
    }

    @Test
    void testInsertedBodyResolvesTheScopeItWasWrittenIn() {
        open("enableStrictTagFileBodyClosure.xhtml");
        assertEquals("[H:W/W][H:W/W]", writtenScope.getText(), "A name the page holds where it wrote the body must resolve to that value");
    }

    @Test
    void testNamedInsertionDoesNotFallBackToTheScopeAtTheInsertion() {
        open("enableStrictTagFileBodyClosure.xhtml");
        assertEquals("()", namedInsertion.getText(), "A ui:define in the body must not resolve what the tag file holds where a named ui:insert inserts it");
    }

    @Test
    void testParameterInBodyReachesTheBodyAlone() {
        open("enableStrictTagFileBodyClosure.xhtml");
        assertEquals("[O:][O:]()", parameterInBody.getText(), "A ui:param written in the body must reach the body, and not the page around the tag");
    }

    @Test
    void testTagFileInvokedInBodyInheritsNoParameter() {
        open("enableStrictTagFileBodyClosure.xhtml");
        assertEquals("[O{:}][O{:}]", invocationInBody.getText(), "A tag file invoked from the body must resolve no parameter of the invocation which wrote the body nor of the invocation inserting it");
    }
}
