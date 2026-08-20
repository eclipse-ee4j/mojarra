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
package com.sun.faces.application.view;

import static com.sun.faces.RIConstants.BUILD_TIME_DECISIONS;
import static com.sun.faces.RIConstants.DYNAMIC_ACTIONS;
import static com.sun.faces.RIConstants.RENDERED_TAGS;
import static com.sun.faces.application.view.FaceletPartialStateManagementStrategy.NOT_REBUILT_REPORT;
import static com.sun.faces.application.view.FaceletPartialStateManagementStrategy.REBUILT_FROM_ANOTHER_TAG_REPORT;
import static com.sun.faces.application.view.FaceletPartialStateManagementStrategy.clientIdsNotRebuilt;
import static com.sun.faces.application.view.FaceletPartialStateManagementStrategy.clientIdsRebuiltFromAnotherTag;
import static com.sun.faces.application.view.FaceletPartialStateManagementStrategy.isViewRootOnlyState;
import static com.sun.faces.application.view.FaceletPartialStateManagementStrategy.tagOf;
import static com.sun.faces.application.view.FaceletPartialStateManagementStrategy.truncated;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.RestoreBuildTimeDecisions;
import static com.sun.faces.facelets.tag.faces.ComponentSupport.MARK_CREATED;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;

/**
 * A postback rebuilds its view from the markup rather than from the response it was submitted from, so a build time
 * conditional evaluating to another value than it did leaves the state of a rendered component with nothing to be
 * restored into, or with the wrong component. Under {@code ProjectStage.Development} the tag each client id was built
 * from is saved with the state, and comparing it against the rebuilt view is what turns that into a diagnosable
 * report. This holds what the comparison reports.
 */
class FaceletPartialStateManagementStrategyTest {

    private static final Pattern LONE_APOSTROPHE = Pattern.compile("(?<!\')\'(?!\')");

    private static Map<String, String> tags(String... clientIdsAndTags) {
        Map<String, String> tags = new HashMap<>();
        for (int i = 0; i < clientIdsAndTags.length; i += 2) {
            tags.put(clientIdsAndTags[i], clientIdsAndTags[i + 1]);
        }
        return tags;
    }

    private static Map<String, Object> state(String... keys) {
        Map<String, Object> state = new HashMap<>();
        for (String key : keys) {
            state.put(key, new Object());
        }
        return state;
    }

    @Test
    void aClientIdTheRebuildDidNotProduceIsReportedAsNotRebuilt() {
        Map<String, String> rendered = tags("form", "1a", "form:input", "1b");
        Map<String, String> rebuilt = tags("form", "1a");

        assertEquals(singletonList("form:input"), clientIdsNotRebuilt(rendered, rebuilt));
    }

    @Test
    void aClientIdTheRebuildProducedFromAnotherTagIsNotReportedAsNotRebuilt() {
        Map<String, String> rendered = tags("form:value", "1b");
        Map<String, String> rebuilt = tags("form:value", "1c");

        assertEquals(emptyList(), clientIdsNotRebuilt(rendered, rebuilt));
    }

    /**
     * Two tags may carry the same component id as long as only one of them is ever built, which is what sibling
     * {@code c:if} tags and the branches of a {@code c:choose} do. Their components then share a client id and, when
     * they are of the same type, everything else the state knows about them, so the tag is what tells them apart.
     */
    @Test
    void aClientIdTheRebuildProducedFromAnotherTagIsReportedAsRebuiltFromAnotherTag() {
        Map<String, String> rendered = tags("form", "1a", "form:value", "1b");
        Map<String, String> rebuilt = tags("form", "1a", "form:value", "1c");

        assertEquals(singletonList("form:value"), clientIdsRebuiltFromAnotherTag(rendered, rebuilt));
    }

    @Test
    void aClientIdTheRebuildDidNotProduceIsNotReportedAsRebuiltFromAnotherTag() {
        Map<String, String> rendered = tags("form:input", "1b");
        Map<String, String> rebuilt = tags();

        assertEquals(emptyList(), clientIdsRebuiltFromAnotherTag(rendered, rebuilt));
    }

    @Test
    void aRebuildReproducingTheRenderedViewReportsNothing() {
        Map<String, String> rendered = tags("form", "1a", "form:input", "1b");
        Map<String, String> rebuilt = tags("form", "1a", "form:input", "1b");

        assertEquals(emptyList(), clientIdsNotRebuilt(rendered, rebuilt));
        assertEquals(emptyList(), clientIdsRebuiltFromAnotherTag(rendered, rebuilt));
    }

    /**
     * A rebuild adding components the render did not produce is what a build time conditional turning true does, and
     * it costs nothing: they hold no state to be restored and no value was submitted for them.
     */
    @Test
    void aClientIdOnlyTheRebuildProducedReportsNothing() {
        Map<String, String> rendered = tags("form", "1a");
        Map<String, String> rebuilt = tags("form", "1a", "form:input", "1b");

        assertEquals(emptyList(), clientIdsNotRebuilt(rendered, rebuilt));
        assertEquals(emptyList(), clientIdsRebuiltFromAnotherTag(rendered, rebuilt));
    }

    /**
     * Two components of the same type built from two tags are told apart by the tag, which is what a shared component
     * id leaves as the only difference between them.
     */
    @Test
    void twoComponentsOfTheSameTypeAreIdentifiedByTheTagThatBuiltThem() {
        UIComponent one = new UIOutput();
        one.getAttributes().put(MARK_CREATED, "1b");
        UIComponent other = new UIOutput();
        other.getAttributes().put(MARK_CREATED, "1c");

        assertEquals("1b", tagOf(one));
        assertEquals("1c", tagOf(other));
    }

    /**
     * A component no facelet built carries no tag, so it is identified by its type, which is what the restore
     * reproduces it from.
     */
    @Test
    void aComponentBuiltByNoTagIsIdentifiedByItsType() {
        assertEquals(UIOutput.class.getName(), tagOf(new UIOutput()));
    }

    @Test
    void stateHoldingNoComponentDeltaAtAllNeedsOnlyTheViewRootRestored() {
        assertTrue(isViewRootOnlyState("root", state()));
        assertTrue(isViewRootOnlyState("root", state(DYNAMIC_ACTIONS)));
        assertTrue(isViewRootOnlyState("root", state(RENDERED_TAGS)));
        assertTrue(isViewRootOnlyState("root", state(BUILD_TIME_DECISIONS)));
        assertTrue(isViewRootOnlyState("root", state(DYNAMIC_ACTIONS, RENDERED_TAGS, BUILD_TIME_DECISIONS)));
    }

    @Test
    void stateHoldingOnlyTheViewRootDeltaNeedsOnlyTheViewRootRestored() {
        assertTrue(isViewRootOnlyState("root", state("root")));
        assertTrue(isViewRootOnlyState("root", state("root", DYNAMIC_ACTIONS)));
        assertTrue(isViewRootOnlyState("root", state("root", RENDERED_TAGS)));
        assertTrue(isViewRootOnlyState("root", state("root", BUILD_TIME_DECISIONS)));
        assertTrue(isViewRootOnlyState("root", state("root", DYNAMIC_ACTIONS, RENDERED_TAGS, BUILD_TIME_DECISIONS)));
    }

    @Test
    void stateHoldingAnotherComponentDeltaNeedsTheFullRestoreTraversal() {
        assertFalse(isViewRootOnlyState("root", state("form:input")));
        assertFalse(isViewRootOnlyState("root", state("form:input", DYNAMIC_ACTIONS)));
        assertFalse(isViewRootOnlyState("root", state("form:input", RENDERED_TAGS)));
        assertFalse(isViewRootOnlyState("root", state("form:input", BUILD_TIME_DECISIONS)));
        assertFalse(isViewRootOnlyState("root", state("root", "form:input", DYNAMIC_ACTIONS, RENDERED_TAGS, BUILD_TIME_DECISIONS)));
    }

    @Test
    void aReportNamesAtMostTenClientIdsAndLeavesAShorterListWhole() {
        List<String> ten = clientIds(10);
        assertSame(ten, truncated(ten));
        assertEquals(ten, truncated(clientIds(11)));
    }

    /**
     * Each report is logged with the arguments its placeholders name.
     */
    @Test
    void everyPlaceholderOfBothReportsIsSubstituted() {
        assertEveryPlaceholderSubstituted(NOT_REBUILT_REPORT, 2, clientIds(2), 1, clientIds(1));
        assertEveryPlaceholderSubstituted(REBUILT_FROM_ANOTHER_TAG_REPORT, 2, clientIds(2));
    }

    /**
     * Both reports are {@link MessageFormat} patterns, in which a lone apostrophe quotes the text that follows it
     * rather than fail, so it silently swallows both the placeholders it precedes and itself.
     */
    @Test
    void neitherReportHoldsALoneApostrophe() {
        assertFalse(LONE_APOSTROPHE.matcher(NOT_REBUILT_REPORT).find(), NOT_REBUILT_REPORT);
        assertFalse(LONE_APOSTROPHE.matcher(REBUILT_FROM_ANOTHER_TAG_REPORT).find(), REBUILT_FROM_ANOTHER_TAG_REPORT);
    }

    /**
     * The remedy of either report names the parameter which replays the build time decisions of the render, taken from
     * the parameter itself so that renaming it cannot leave the report naming one which no longer exists.
     */
    @Test
    void bothReportsNameTheParameterWhichReplaysTheBuildTimeDecisionsOfTheRender() {
        String parameterName = RestoreBuildTimeDecisions.getQualifiedName();

        assertTrue(NOT_REBUILT_REPORT.contains(parameterName), NOT_REBUILT_REPORT);
        assertTrue(REBUILT_FROM_ANOTHER_TAG_REPORT.contains(parameterName), REBUILT_FROM_ANOTHER_TAG_REPORT);
    }

    private static List<String> clientIds(int count) {
        List<String> clientIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            clientIds.add("form:input" + i);
        }
        return clientIds;
    }

    private static void assertEveryPlaceholderSubstituted(String report, Object... arguments) {
        String message = MessageFormat.format(report, arguments);
        assertFalse(message.contains("{"), message);
        for (Object argument : arguments) {
            assertTrue(message.contains(String.valueOf(argument)), message);
        }
    }
}
