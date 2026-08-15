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
package org.eclipse.mojarra.test.restorebuildtimedecisions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The webapp declares <code>com.sun.faces.restoreBuildTimeDecisions</code>, so the build which restores a postback
 * reproduces the view that was submitted rather than the one the current state of the model asks for. Every view here
 * guards its input with a build time condition over a request scoped flag which the action of the previous request
 * set, so the condition holds while the response is rendered and no longer holds while the postback is restored.
 *
 * <p>
 * Without the parameter each of these submits is silently dropped: the guarded component is not rebuilt, the state
 * saved for it is restored into nothing and the value submitted for it is decoded by nothing. With it the value
 * reaches the model, and the re-apply of the facelet which precedes rendering evaluates the condition again, so the
 * response no longer holds the component.
 */
class RestoreBuildTimeDecisionsIT extends BaseIT {

    @FindBy(id = "form:show")
    private WebElement show;

    @FindBy(id = "form:input")
    private WebElement input;

    @FindBy(id = "form:input0")
    private WebElement firstInput;

    @FindBy(id = "form:input1")
    private WebElement secondInput;

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:ajaxSubmit")
    private WebElement ajaxSubmit;

    @FindBy(id = "form:value")
    private WebElement value;

    @FindBy(id = "form:otherValue")
    private WebElement otherValue;

    @FindBy(id = "form:values")
    private WebElement values;

    @FindBy(id = "form:names")
    private WebElement names;

    /**
     * A value submitted for an input the response held inside a <code>c:if</code> reaches the model, even though the
     * test no longer holds while the postback is restored.
     */
    @Test
    void testConditional() {
        open("conditional.xhtml");
        guardHttp(show::click);
        input.sendKeys("test");
        guardHttp(submit::click);
        assertEquals("test", value.getText());
        assertFalse(getPageSource().contains("form:input"), "the re-apply which precedes rendering drops the input");
    }

    /**
     * An ajax postback restores its view the same way, so a value submitted for an input the response held inside a
     * <code>c:if</code> reaches the model there as well.
     */
    @Test
    void testConditionalAjax() {
        open("conditional.xhtml");
        guardHttp(show::click);
        input.sendKeys("test");
        guardAjax(ajaxSubmit::click);
        assertEquals("test", value.getText());
    }

    /**
     * A value submitted for an input the response held inside one <code>c:when</code> reaches the property that branch
     * binds, rather than the property of the branch the restore would otherwise have taken. Both branches carry the
     * same component id, which is what makes this the quietest variant: nothing but the tag tells them apart.
     */
    @Test
    void testChoose() {
        open("choose.xhtml");
        guardHttp(show::click);
        input.sendKeys("test");
        guardHttp(submit::click);
        assertEquals("test", value.getText());
        assertEquals("", otherValue.getText());
    }

    /**
     * The values submitted for the rows a <code>c:forEach</code> over a build time range rendered reach the model,
     * even though the range no longer covers them while the postback is restored.
     */
    @Test
    void testIteration() {
        open("iteration.xhtml");
        guardHttp(show::click);
        firstInput.sendKeys("first");
        secondInput.sendKeys("second");
        guardHttp(submit::click);
        assertEquals("{0=first, 1=second}", values.getText());
    }

    /**
     * The items of an iteration are not saved, only the range and the row count, since each var reference reads its
     * own element live by position. An iteration whose items no longer supply the rows that were rendered is
     * therefore built from the items as they are now: the values submitted for those rows are dropped, exactly as
     * without the parameter, and the postback still completes. Materializing a row over items which no longer hold it
     * is what must not happen, since that row resolves its var to null and fails the postback outright.
     */
    @Test
    void testIterationOverVanishedItems() {
        open("vanisheditems.xhtml");
        guardHttp(show::click);
        firstInput.sendKeys("first");
        secondInput.sendKeys("second");
        guardHttp(submit::click);
        assertEquals("a,b", names.getText());
    }

    /**
     * A decision is keyed by the id its tag generates for the build, which an iteration generates anew per round, so a
     * <code>c:if</code> nested in a <code>c:forEach</code> replays the value it had in its own round.
     */
    @Test
    void testNestedInIteration() {
        open("nested.xhtml");
        guardHttp(show::click);
        firstInput.sendKeys("first");
        secondInput.sendKeys("second");
        guardHttp(submit::click);
        assertEquals("{0=first, 1=second}", values.getText());
    }

    /**
     * A value submitted for an input the response held in a <code>ui:include</code> reaches the model, even though the
     * path resolves to another facelet while the postback is restored.
     */
    @Test
    void testInclude() {
        open("include.xhtml");
        guardHttp(show::click);
        assertTrue(getPageSource().contains("form:input"));
        input.sendKeys("test");
        guardHttp(submit::click);
        assertEquals("test", value.getText());
    }
}
