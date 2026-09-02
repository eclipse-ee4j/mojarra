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
package org.glassfish.mojarra.application.view;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.glassfish.mojarra.RIConstants.BUILD_TIME_DECISIONS;
import static org.glassfish.mojarra.RIConstants.DYNAMIC_ACTIONS;
import static org.glassfish.mojarra.RIConstants.RENDERED_TAGS;
import static org.glassfish.mojarra.application.view.FaceletStateManagementStrategy.NOT_REBUILT_REPORT;
import static org.glassfish.mojarra.application.view.FaceletStateManagementStrategy.NOT_REBUILT_WITH_SUBMITTED_REPORT;
import static org.glassfish.mojarra.application.view.FaceletStateManagementStrategy.REBUILT_FROM_ANOTHER_TAG_REPORT;
import static org.glassfish.mojarra.application.view.FaceletStateManagementStrategy.clientIdsNotRebuilt;
import static org.glassfish.mojarra.application.view.FaceletStateManagementStrategy.clientIdsRebuiltFromAnotherTag;
import static org.glassfish.mojarra.application.view.FaceletStateManagementStrategy.holdsStableClientId;
import static org.glassfish.mojarra.application.view.FaceletStateManagementStrategy.isViewRootOnlyState;
import static org.glassfish.mojarra.application.view.FaceletStateManagementStrategy.tagOf;
import static org.glassfish.mojarra.application.view.FaceletStateManagementStrategy.truncated;
import static org.glassfish.mojarra.config.MojarraContextParam.RESTORE_BUILD_TIME_DECISIONS;
import static org.glassfish.mojarra.facelets.tag.faces.ComponentSupport.MARK_CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.Test;

/**
 * A postback rebuilds its view from the markup rather than from the response it was submitted from, so a build time
 * conditional evaluating to another value than it did leaves the state of a rendered component with nothing to be
 * restored into, or with the wrong component. Under {@code ProjectStage.Development} the tag each client id was built
 * from is saved with the state, and comparing it against the rebuilt view is what turns that into a diagnosable
 * report. This holds what the comparison reports.
 */
class FaceletStateManagementStrategyTest {

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
     * A component no facelet built - the view root, a component resource, anything added programmatically - is not
     * compared across the two builds at all. No build time condition governs whether it is there, and one left
     * without an id of its own is numbered from a counter which the build leaves where the previous request's state
     * then moves it, so it holds another client id on every request and comparing it by client id would report it as
     * vanished on every postback.
     */
    @Test
    void aComponentBuiltByNoTagIsNotCompared() {
        assertNull(tagOf(new UIOutput()));
    }

    /**
     * A naming container positioned on no row hands its children the client id they hold on every other request too.
     */
    @Test
    void aComponentUnderANamingContainerPositionedOnNoRowIsCompared() {
        UIComponent component = new UIOutput();
        container("form:rows", "form:rows").getChildren().add(component);

        assertTrue(holdsStableClientId(null, component));
    }

    /**
     * A container left positioned on a row by a render which iterated it hands its children a client id no request
     * which does not iterate it reproduces, the rebuild of the postback included.
     */
    @Test
    void aComponentUnderANamingContainerPositionedOnARowIsNotCompared() {
        UIComponent component = new UIOutput();
        container("form:rows", "form:rows:1").getChildren().add(component);

        assertFalse(holdsStableClientId(null, component));
    }

    /**
     * A container nested in a positioned one is itself positioned on no row, yet holds the row of the one above it in
     * its own client id, so the whole chain decides.
     */
    @Test
    void aComponentUnderANamingContainerNestedInAPositionedOneIsNotCompared() {
        UIComponent component = new UIOutput();
        UIComponent nested = container("form:rows:1:group", "form:rows:1:group");
        container("form:rows", "form:rows:1").getChildren().add(nested);
        nested.getChildren().add(component);

        assertFalse(holdsStableClientId(null, component));
    }

    /**
     * A component which is held by nothing that can stand on a row holds the client id it holds on every request.
     */
    @Test
    void aComponentHeldByNoNamingContainerIsCompared() {
        assertTrue(holdsStableClientId(null, new UIOutput()));
    }

    /**
     * A plain component between the two decides nothing: the container above it is the one standing on a row.
     */
    @Test
    void aComponentUnderAPlainComponentUnderAPositionedNamingContainerIsNotCompared() {
        UIComponent component = new UIOutput();
        UIComponent plain = new UIPanel();
        container("form:rows", "form:rows:1").getChildren().add(plain);
        plain.getChildren().add(component);

        assertFalse(holdsStableClientId(null, component));
    }

    /**
     * A form which prepends no id hands its children the client id of the container above it rather than one
     * extending its own, and none at all when no container holds it, so it stands on no row either way.
     */
    @Test
    void aComponentUnderAFormWhichPrependsNoIdIsCompared() {
        UIComponent underNestedForm = new UIOutput();
        container("outer:form", "outer").getChildren().add(underNestedForm);

        UIComponent underRootForm = new UIOutput();
        container("form", null).getChildren().add(underRootForm);

        assertTrue(holdsStableClientId(null, underNestedForm));
        assertTrue(holdsStableClientId(null, underRootForm));
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
    void everyPlaceholderOfEveryReportIsSubstituted() {
        assertEveryPlaceholderSubstituted(NOT_REBUILT_REPORT, 2, clientIds(2));
        assertEveryPlaceholderSubstituted(NOT_REBUILT_WITH_SUBMITTED_REPORT, 2, clientIds(2), 1, clientIds(1));
        assertEveryPlaceholderSubstituted(REBUILT_FROM_ANOTHER_TAG_REPORT, 2, clientIds(2));
    }

    /**
     * A report reads as often about one component as about many, so each count it names agrees with the words that
     * follow it.
     */
    @Test
    void everyReportAgreesWithTheCountItNames() {
        assertReportHolds(NOT_REBUILT_REPORT, "so its state is restored", 1, clientIds(1));
        assertReportHolds(NOT_REBUILT_REPORT, "so their state is restored", 2, clientIds(2));
        assertReportHolds(NOT_REBUILT_WITH_SUBMITTED_REPORT, "1 carries a submitted value", 1, clientIds(1), 1, clientIds(1));
        assertReportHolds(NOT_REBUILT_WITH_SUBMITTED_REPORT, "2 carry a submitted value", 2, clientIds(2), 2, clientIds(2));
        assertReportHolds(REBUILT_FROM_ANOTHER_TAG_REPORT, "holds 1 client id built", 1, clientIds(1));
        assertReportHolds(REBUILT_FROM_ANOTHER_TAG_REPORT, "holds 2 client ids built", 2, clientIds(2));
    }

    /**
     * A component the rebuild does not produce is as often an output or a panel as it is an input, so a report which
     * always named the submitted values among them would name a count of zero and an empty list on the majority of
     * postbacks. Only the report logged when there is at least one holds the placeholders for them.
     */
    @Test
    void onlyTheReportLoggedForASubmittedValueHoldsThePlaceholdersForOne() {
        assertFalse(NOT_REBUILT_REPORT.contains("{2}"), NOT_REBUILT_REPORT);
        assertTrue(NOT_REBUILT_WITH_SUBMITTED_REPORT.contains("{2}"), NOT_REBUILT_WITH_SUBMITTED_REPORT);
    }

    /**
     * Every report is a {@link MessageFormat} pattern, in which a lone apostrophe quotes the text that follows it
     * rather than fail, so it silently swallows both the placeholders it precedes and itself.
     */
    @Test
    void noReportHoldsALoneApostrophe() {
        assertFalse(LONE_APOSTROPHE.matcher(NOT_REBUILT_REPORT).find(), NOT_REBUILT_REPORT);
        assertFalse(LONE_APOSTROPHE.matcher(NOT_REBUILT_WITH_SUBMITTED_REPORT).find(), NOT_REBUILT_WITH_SUBMITTED_REPORT);
        assertFalse(LONE_APOSTROPHE.matcher(REBUILT_FROM_ANOTHER_TAG_REPORT).find(), REBUILT_FROM_ANOTHER_TAG_REPORT);
    }

    /**
     * The remedy of every report names the parameter which replays the build time decisions of the render, taken from
     * the parameter itself so that renaming it cannot leave the report naming one which no longer exists.
     */
    @Test
    void everyReportNamesTheParameterWhichReplaysTheBuildTimeDecisionsOfTheRender() {
        String parameterName = RESTORE_BUILD_TIME_DECISIONS.getName();

        assertTrue(NOT_REBUILT_REPORT.contains(parameterName), NOT_REBUILT_REPORT);
        assertTrue(NOT_REBUILT_WITH_SUBMITTED_REPORT.contains(parameterName), NOT_REBUILT_WITH_SUBMITTED_REPORT);
        assertTrue(REBUILT_FROM_ANOTHER_TAG_REPORT.contains(parameterName), REBUILT_FROM_ANOTHER_TAG_REPORT);
    }

    private static UIComponent container(String clientId, String containerClientId) {
        return new Container(clientId, containerClientId);
    }

    /**
     * A naming container which is asked for its client ids alone, so that neither has to be built from a context.
     */
    private static class Container extends UIPanel implements NamingContainer {

        private final String clientId;
        private final String containerClientId;

        private Container(String clientId, String containerClientId) {
            this.clientId = clientId;
            this.containerClientId = containerClientId;
        }

        @Override
        public String getClientId(FacesContext context) {
            return clientId;
        }

        @Override
        public String getContainerClientId(FacesContext context) {
            return containerClientId;
        }
    }

    private static List<String> clientIds(int count) {
        List<String> clientIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            clientIds.add("form:input" + i);
        }
        return clientIds;
    }

    private static void assertReportHolds(String report, String expected, Object... arguments) {
        String message = MessageFormat.format(report, arguments);
        assertTrue(message.contains(expected), message);
    }

    private static void assertEveryPlaceholderSubstituted(String report, Object... arguments) {
        String message = MessageFormat.format(report, arguments);
        assertFalse(message.contains("{"), message);
        for (Object argument : arguments) {
            assertTrue(message.contains(String.valueOf(argument)), message);
        }
    }
}
