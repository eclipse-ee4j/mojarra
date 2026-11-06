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
package org.glassfish.mojarra.test.impl.restorebuildtimedecisionsafteredit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import ee.jakarta.tck.faces.util.selenium.BaseITNG;
import ee.jakarta.tck.faces.util.selenium.WebPage;

/**
 * The webapp declares <code>com.sun.faces.restoreBuildTimeDecisions</code>, so the build which restores a postback replays the decisions of the build which
 * rendered the view, each keyed by the id its tag generates. Each test edits the facelet between the render and the postback, by swapping two blocks of it
 * which decide differently, so that a tag gets the id another tag had. A tag applied under an edited facelet decides again rather than replay what another tag
 * decided, so a value submitted for an input of the view which was rendered still reaches the model.
 * <p>
 * The webapp sets the facelet refresh period to 0, so every request compiles the edited facelet again. Each test asserts that the view built for the postback
 * is the one of the edited facelet.
 */
class RestoreBuildTimeDecisionsAfterEditIT extends BaseITNG {

    @FindBy(id = "form:inputA")
    private WebElement inputA;

    @FindBy(id = "form:input")
    private WebElement input;

    @FindBy(id = "form:show")
    private WebElement show;

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:value")
    private WebElement value;

    @FindBy(id = "form:values")
    private WebElement values;

    private WebPage page;

    private String editedPage;

    /**
     * Puts back the edited page, so that a rerun of a failed test starts from the page as deployed.
     */
    @AfterEach
    void restoreEditedPage() {
        if (editedPage != null) {
            assertEquals(SwapBlocksServlet.EDITED, getResponseBody("swapblocks?restore=true&page=" + editedPage));
        }
    }

    /**
     * The blocks of two <code>c:if</code> which decide differently swap places.
     */
    @Test
    void testConditional() {
        page = getPage("swappedconditional.xhtml");
        inputA.sendKeys("test");
        swapBlocks("swappedconditional.xhtml");
        page.guardHttp(submit::click);
        assertSwapped();
        assertEquals("test", value.getText());
    }

    /**
     * The blocks of two <code>c:forEach</code> over another number of items swap places. Each iteration produces its own rows rather than the row count of the
     * other one.
     */
    @Test
    void testIteration() {
        page = getPage("swappediteration.xhtml");
        for (String id : List.of("foo_f0", "foo_f1", "bar_b0", "bar_b1", "bar_b2")) {
            page.findElement(By.id("form:" + id)).sendKeys(id);
        }
        swapBlocks("swappediteration.xhtml");
        page.guardHttp(submit::click);
        assertSwapped();
        assertEquals("{b0=bar_b0, b1=bar_b1, b2=bar_b2, f0=foo_f0, f1=foo_f1}", values.getText());
    }

    /**
     * Two includes of the same facelet, whose <code>c:if</code> decides differently per include, swap places. The included facelet is not edited, but the id
     * its tags generate follows the order of the includes in the edited facelet.
     */
    @Test
    void testInclude() {
        page = getPage("swappedinclude.xhtml");
        input.sendKeys("test");
        swapBlocks("swappedinclude.xhtml");
        page.guardHttp(submit::click);
        assertSwapped();
        assertEquals("{a=test}", values.getText());
    }

    /**
     * A facelet applied before another one is edited, here so that it iterates fewer items over an include of the facelet the other one includes as well.
     * Neither the other facelet nor its include is edited, but the ids the tags of the included facelet generate under it follow what was applied before.
     */
    @Test
    void testEditedBefore() {
        page = getPage("appliedbefore.xhtml");
        assertTrue(isPresent("form:input_o0"));
        page.findElement(By.id("form:input_d")).sendKeys("test");
        swapBlocks("WEB-INF/includes/before.xhtml");
        page.guardHttp(submit::click);
        assertFalse(isPresent("form:input_o0"), "the view built for the postback is the one of the edited facelet");
        assertEquals("{a=, d=test}", values.getText());
    }

    /**
     * A decision taken before the edited facelet is applied still replays, so a value submitted for an input the response held inside a <code>c:if</code>
     * reaches the model, even though the test no longer holds while the postback is restored.
     */
    @Test
    void testReplayedBeforeEdit() {
        page = getPage("replayedbefore.xhtml");
        page.guardHttp(show::click);
        inputA.sendKeys("test");
        swapBlocks("WEB-INF/includes/edited.xhtml");
        page.guardHttp(submit::click);
        assertSwapped();
        assertEquals("test", value.getText());
    }

    /**
     * Asserts that the blocks of the page are in swapped order, which they are only in the edited facelet.
     */
    private void assertSwapped() {
        String source = page.getSource();
        int second = source.indexOf("id=\"form:second\"");
        assertTrue(second >= 0 && second < source.indexOf("id=\"form:first\""), "the view built for the postback is the one of the edited facelet");
    }

    private boolean isPresent(String clientId) {
        return !page.findElements(By.id(clientId)).isEmpty();
    }

    private void swapBlocks(String page) {
        editedPage = page;
        assertEquals(SwapBlocksServlet.EDITED, getResponseBody("swapblocks?page=" + page));
    }

}
