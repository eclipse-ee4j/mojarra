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
package org.eclipse.mojarra.test.issue5970;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * A postback rebuilds its view from the markup rather than from the response it was submitted from, so a build time
 * condition evaluating to another value than it did leaves the state of a rendered component with nothing to be
 * restored into. Under {@code ProjectStage.Development} that is reported, which is only worth reading when the
 * components it names are the ones a build time condition governs: the components a facelet built. A component no
 * facelet built is none of the report's business, and one of those left without an id of its own does not even hold
 * a stable client id, so comparing it would report every view holding one on every postback.
 */
class Issue5970IT extends BaseIT {

    @FindBy(id = "form:show")
    private WebElement show;

    @FindBy(id = "form:input")
    private WebElement input;

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:ajaxSubmit")
    private WebElement ajaxSubmit;

    @FindBy(id = "form:reports")
    private WebElement reports;

    /**
     * The input a <code>c:if</code> held while the response was rendered, and no longer holds while the postback is
     * restored, is named by the report.
     */
    @Test
    void testComponentDroppedByABuildTimeCondition() {
        open("conditional.xhtml");
        guardHttp(show::click);
        assertEquals("[]", reports.getText(), "the rebuild reproduces the view the response was rendered from");
        input.sendKeys("test");
        guardHttp(submit::click);
        assertTrue(reports.getText().contains("form:input"), reports.getText());
    }

    /**
     * A view holding no build time condition at all is reported on by neither postback, even though it holds the
     * component resources which <code>f:ajax</code> installs: no facelet built those, and they are numbered from a
     * counter which the build leaves where the state of the previous request then moves it, so they hold another
     * client id on every request.
     */
    @Test
    void testViewHoldingOnlyComponentsNoBuildTimeConditionGoverns() {
        open("resources.xhtml");
        input.sendKeys("test");
        guardHttp(submit::click);
        assertEquals("[]", reports.getText(), reports.getText());
        guardAjax(ajaxSubmit::click);
        assertEquals("[]", reports.getText(), reports.getText());
    }
}
