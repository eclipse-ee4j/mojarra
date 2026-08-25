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

package org.glassfish.mojarra.test.impl.issue5970;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import ee.jakarta.tck.faces.util.selenium.BaseITNG;
import ee.jakarta.tck.faces.util.selenium.WebPage;

/**
 * A postback rebuilds its view from the markup rather than from the response it was submitted from, so a build time
 * condition evaluating to another value than it did leaves the state of a rendered component with nothing to be
 * restored into. Under {@code ProjectStage.Development} that is reported, which is only worth reading when the
 * components it names are the ones a build time condition governs: the components a facelet built. A component no
 * facelet built is none of the report's business, and one of those left without an id of its own does not even hold
 * a stable client id, so comparing it would report every view holding one on every postback.
 *
 * @see org.glassfish.mojarra.application.view.FaceletStateManagementStrategy
 */
class Issue5970IT extends BaseITNG {

    private static final By SHOW = By.id("form:show");
    private static final By INPUT = By.id("form:input");
    private static final By SUBMIT = By.id("form:submit");
    private static final By AJAX_SUBMIT = By.id("form:ajaxSubmit");
    private static final By REPORTS = By.id("form:reports");

    private static final String NO_REPORTS = "[]";

    /**
     * The input a <code>c:if</code> held while the response was rendered, and no longer holds while the postback is
     * restored, is named by the report.
     */
    @Test
    void testComponentDroppedByABuildTimeCondition() {
        WebPage page = getPage("conditional.xhtml");
        page.guardHttp(page.findElement(SHOW)::click);
        assertEquals(NO_REPORTS, page.findElement(REPORTS).getText(), "the rebuild reproduces the view the response was rendered from");

        page.findElement(INPUT).sendKeys("test");
        page.guardHttp(page.findElement(SUBMIT)::click);
        String reports = page.findElement(REPORTS).getText();
        assertTrue(reports.contains("form:input"), reports);
    }

    /**
     * A view holding no build time condition at all is reported on by neither postback, even though it holds the
     * component resources which <code>f:ajax</code> installs: no facelet built those, and they are numbered from a
     * counter which the build leaves where the state of the previous request then moves it, so they hold another
     * client id on every request.
     */
    @Test
    void testViewHoldingOnlyComponentsNoBuildTimeConditionGoverns() {
        WebPage page = getPage("resources.xhtml");
        page.findElement(INPUT).sendKeys("test");
        page.guardHttp(page.findElement(SUBMIT)::click);
        assertEquals(NO_REPORTS, page.findElement(REPORTS).getText(), "no report after a full postback");

        page.guardAjax(page.findElement(AJAX_SUBMIT)::click);
        assertEquals(NO_REPORTS, page.findElement(REPORTS).getText(), "no report after an ajax postback");
    }
}
