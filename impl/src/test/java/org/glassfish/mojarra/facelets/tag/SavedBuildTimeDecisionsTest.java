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

package org.glassfish.mojarra.facelets.tag;

import static org.glassfish.mojarra.RIConstants.BUILD_TIME_DECISIONS;
import static org.glassfish.mojarra.facelets.tag.SavedBuildTimeDecisions.sourceFingerprintKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.mojarra.config.MojarraContextParam;
import org.glassfish.mojarra.config.WebConfiguration;
import org.glassfish.mojarra.junit.JUnitFacesTestCaseBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The build which restores a view replays the decisions of the build which rendered it as long as every facelet it applies had the same fingerprint then, since
 * the key of a decision follows everything the build applied before.
 */
class SavedBuildTimeDecisionsTest extends JUnitFacesTestCaseBase {

    private static final String KEY = "1_decision";
    private static final String PAGE = sourceFingerprintKey("/page.xhtml");
    private static final String INCLUDE = sourceFingerprintKey("/include.xhtml");
    private static final int FINGERPRINT = 1;
    private static final int EDITED_FINGERPRINT = 2;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        servletContext.removeAttribute(WebConfiguration.class.getName());
        servletContext.addInitParameter(MojarraContextParam.RESTORE_BUILD_TIME_DECISIONS.getName(), "true");
    }

    @Test
    void replaysWhileEveryAppliedFaceletHasItsRenderedFingerprint() {
        startReplaying(Map.of(KEY, Boolean.TRUE, PAGE, FINGERPRINT, INCLUDE, FINGERPRINT));
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, FINGERPRINT);
        SavedBuildTimeDecisions.applyFacelet(facesContext, INCLUDE, FINGERPRINT);

        assertEquals(Boolean.TRUE, SavedBuildTimeDecisions.replay(facesContext, KEY));
    }

    @Test
    void stopsReplayingForTheRestOfTheBuildAtAFaceletWithAnotherFingerprint() {
        startReplaying(Map.of(KEY, Boolean.TRUE, PAGE, FINGERPRINT, INCLUDE, FINGERPRINT));
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, EDITED_FINGERPRINT);
        SavedBuildTimeDecisions.applyFacelet(facesContext, INCLUDE, FINGERPRINT);

        assertNull(SavedBuildTimeDecisions.replay(facesContext, KEY));
    }

    @Test
    void stopsReplayingAtAFaceletWhichWasNotAppliedWhenTheViewWasRendered() {
        startReplaying(Map.of(KEY, Boolean.TRUE, PAGE, FINGERPRINT));
        SavedBuildTimeDecisions.applyFacelet(facesContext, INCLUDE, FINGERPRINT);

        assertNull(SavedBuildTimeDecisions.replay(facesContext, KEY));
    }

    @Test
    void savesTheFingerprintsOfTheAppliedFaceletsBesideTheDecisions() {
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, FINGERPRINT);
        SavedBuildTimeDecisions.record(facesContext, KEY, Boolean.TRUE);

        assertEquals(Map.of(KEY, Boolean.TRUE, PAGE, FINGERPRINT), save());
    }

    @Test
    void savesNoFingerprintsWithoutDecisions() {
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, FINGERPRINT);

        assertEquals(Map.of(), save());
    }

    /**
     * The two builds of one postback apply other facelets when a decision changed in between, which does not change their sources, so the next postback still
     * replays.
     */
    @Test
    void replaysAfterTwoBuildsWhichAppliedOtherFaceletsOfTheSameSources() {
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, FINGERPRINT);
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, FINGERPRINT);
        SavedBuildTimeDecisions.applyFacelet(facesContext, INCLUDE, FINGERPRINT);
        SavedBuildTimeDecisions.record(facesContext, KEY, Boolean.TRUE);
        Map<String, Object> saved = save();

        startReplaying(saved);
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, FINGERPRINT);
        SavedBuildTimeDecisions.applyFacelet(facesContext, INCLUDE, FINGERPRINT);

        assertEquals(Boolean.TRUE, SavedBuildTimeDecisions.replay(facesContext, KEY));
    }

    /**
     * The builds of one request can apply a facelet with two fingerprints when it is edited in between, and the decisions saved for it can then be of either,
     * so the build which restores the view replays none of them.
     */
    @Test
    void stopsReplayingAtAFaceletAppliedWithTwoFingerprintsWhenTheViewWasRendered() {
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, FINGERPRINT);
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, EDITED_FINGERPRINT);
        SavedBuildTimeDecisions.record(facesContext, KEY, Boolean.TRUE);
        Map<String, Object> saved = save();

        startReplaying(saved);
        SavedBuildTimeDecisions.applyFacelet(facesContext, PAGE, EDITED_FINGERPRINT);

        assertNull(SavedBuildTimeDecisions.replay(facesContext, KEY));
    }

    /**
     * A suspended build, such as the one of the view metadata, applies a facelet which contributes nothing to the view, so it neither stops the replay nor is
     * saved.
     */
    @Test
    void ignoresAFaceletAppliedWhileSuspended() {
        startReplaying(Map.of(KEY, Boolean.TRUE, PAGE, FINGERPRINT));
        SavedBuildTimeDecisions.suspend(facesContext);
        SavedBuildTimeDecisions.applyFacelet(facesContext, INCLUDE, EDITED_FINGERPRINT);
        SavedBuildTimeDecisions.resume(facesContext);
        SavedBuildTimeDecisions.record(facesContext, KEY, Boolean.TRUE);

        assertEquals(Boolean.TRUE, SavedBuildTimeDecisions.replay(facesContext, KEY));
        assertFalse(save().containsKey(INCLUDE));
    }

    private void startReplaying(Map<String, Object> decisions) {
        SavedBuildTimeDecisions.startReplaying(facesContext, Map.of(BUILD_TIME_DECISIONS, decisions));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> save() {
        Map<String, Object> state = new HashMap<>();
        SavedBuildTimeDecisions.save(facesContext, state);
        return (Map<String, Object>) state.getOrDefault(BUILD_TIME_DECISIONS, Map.of());
    }

}
