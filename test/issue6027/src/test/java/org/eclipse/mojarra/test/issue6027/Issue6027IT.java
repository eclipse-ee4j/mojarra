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
package org.eclipse.mojarra.test.issue6027;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The scope the body of a tag file invocation resolves in: the one the page which wrote it is in, wherever the tag
 * file inserts it.
 * <p>
 * A tag file resolves a parameter name against the attributes of its own invocation, so a name an enclosing invocation
 * was given reaches no further. The body of the invocation is not part of the tag file, and keeps resolving where it
 * was written, which is what tells the two apart: the same name is empty inside the tag file and holds its value in
 * the body the tag file inserts. This holds for both forms of insertion, for markup evaluated while the view is built
 * and for a value expression evaluated when it renders, and it leaves a {@code ui:param} the body carries reaching the
 * body alone.
 * <p>
 * These assertions hold on any implementation which isolates the parameter namespace of a tag file, so they are
 * portable rather than specific to this implementation.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6027">issue 6027</a>
 * @see <a href="https://github.com/jakartaee/faces/issues/2247">specification issue 2247</a>
 */
class Issue6027IT extends BaseIT {

    @FindBy(id = "insertedBody")
    private WebElement insertedBody;

    @FindBy(id = "insertedParameter")
    private WebElement insertedParameter;

    @Test
    void testInsertedBodyResolvesTheParametersOfTheInvocationItWasWrittenIn() {
        open("issue6027.xhtml");
        assertEquals("[O|(I::BO/CO:NO)]", insertedBody.getText(), "The body of the nested invocation must resolve #{outerParameter} to the value the enclosing invocation was given, where the tag file inserting it resolves the name to nothing, both for a nameless ui:insert and for a named one, and both for inline markup and for a value expression rendered later");
    }

    @Test
    void testParameterWrittenInAnInsertedBodyReachesThatBody() {
        open("issue6027.xhtml");
        assertEquals("(I::BP:)", insertedParameter.getText(), "A ui:param written in the body of an invocation must reach the markup it is written among, wherever the tag file inserts it");
    }
}
