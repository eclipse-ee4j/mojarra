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

package com.sun.faces.facelets.tag;

import java.util.HashMap;
import java.util.Map;

import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter;

import jakarta.faces.context.FacesContext;

/**
 * What the build which rendered the view decided, carried in the saved state so that the build which restores the view
 * reproduces the view that was submitted.
 *
 * <p>
 * A postback rebuilds its view from the markup, so a build time condition evaluating to another value than it did
 * while the response was rendered produces another view than the one that was submitted: the state saved for a
 * component the rebuild does not produce is restored into nothing, and a value submitted for it is decoded by nothing.
 * Replaying the decision instead of evaluating it lets the restore reproduce the rendered view, and the re-apply of
 * the facelet which precedes rendering evaluates it again, so the response still follows the current state of the
 * model - one phase later.
 *
 * <p>
 * A decision is keyed by the id its tag generates for the build, which is unique per tag and stable across the builds
 * of one view, and holds a value of a type the JDK declares, so that a runtime which does not know this state entry
 * can still deserialize the state that carries it. What a handler cannot express in one - the keys of an iteration
 * over a map, which name the entries its rows were rendered for - is carried in a JDK typed array of the application's
 * own values, which must be {@link java.io.Serializable} like everything else the state holds. What is replayed and
 * what is saved are two maps: the builds of this request record into the second, so that a decision an earlier
 * response took and this one does not is gone from the state rather than replayed by the next restore. They record
 * into it together rather than one build at a time, since the re-apply which precedes rendering is skipped for a view
 * whose decisions still hold and would otherwise leave nothing to save, which is also why a postback which navigates
 * saves what the view it restored decided beside what the view it renders did: telling the two apart costs the build
 * it was restored by.
 *
 * <p>
 * Nothing of this happens unless {@code com.sun.faces.restoreBuildTimeDecisions} is enabled.
 *
 * @see com.sun.faces.facelets.tag.jstl.core.IfHandler
 */
public final class SavedBuildTimeDecisions {

    private static final String KEY = RIConstants.FACES_PREFIX + "savedBuildTimeDecisions";

    /**
     * What is held under {@link #KEY} for a request which saves no decisions at all, so that the parameter is read
     * once per request rather than once per condition.
     */
    private static final Object DISABLED = new Object();

    private final Map<String, Object> restored = new HashMap<>();
    private final Map<String, Object> recorded = new HashMap<>();
    private boolean replaying;
    private boolean suspended;

    private SavedBuildTimeDecisions() {
    }

    /**
     * Whether the decisions of the build which rendered a view are saved with its state and replayed by the build
     * which restores it.
     *
     * @param context the {@link FacesContext} for the current request
     * @return whether build time decisions are saved and replayed
     */
    public static boolean isEnabled(FacesContext context) {
        return active(context) != null;
    }

    /**
     * Stops the builds of this request from deciding anything, to be resumed with {@link #resume(FacesContext)}. A
     * build which contributes nothing to the response has nothing to replay and nothing worth saving: the view it
     * produces is thrown away, and the ids its tags generate are of a facelet of its own.
     *
     * @param context the {@link FacesContext} for the current request
     */
    public static void suspend(FacesContext context) {
        SavedBuildTimeDecisions instance = of(context);

        if (instance != null) {
            instance.suspended = true;
        }
    }

    /**
     * Lets the builds of this request decide again, after {@link #suspend(FacesContext)}.
     *
     * @param context the {@link FacesContext} for the current request
     */
    public static void resume(FacesContext context) {
        SavedBuildTimeDecisions instance = of(context);

        if (instance != null) {
            instance.suspended = false;
        }
    }

    /**
     * Takes the decisions of the build which rendered the view from the state being restored, and starts replaying
     * them, to be stopped with {@link #stopReplaying(FacesContext)} once the view is rebuilt.
     *
     * @param context the {@link FacesContext} for the current request
     * @param state the state being restored, may be {@code null}
     */
    @SuppressWarnings("unchecked")
    public static void startReplaying(FacesContext context, Map<String, Object> state) {
        SavedBuildTimeDecisions instance = of(context);

        if (instance != null) {
            Object entry = state == null ? null : state.get(RIConstants.BUILD_TIME_DECISIONS);
            Map<String, Object> saved = entry instanceof Map ? (Map<String, Object>) entry : null;

            if (saved != null) {
                instance.restored.putAll(saved);
                instance.replaying = true;
            }
        }
    }

    /**
     * Stops replaying the decisions of the build which rendered the view, so that every build after the one which
     * restored it decides for itself again.
     *
     * @param context the {@link FacesContext} for the current request
     */
    public static void stopReplaying(FacesContext context) {
        SavedBuildTimeDecisions instance = of(context);

        if (instance != null) {
            instance.replaying = false;
        }
    }

    /**
     * Puts the decisions the builds of this request recorded into the state being saved, so that the build which
     * restores this view can replay them.
     *
     * @param context the {@link FacesContext} for the current request
     * @param state the state being saved
     */
    public static void save(FacesContext context, Map<String, Object> state) {
        SavedBuildTimeDecisions instance = of(context);

        if (instance != null && !instance.recorded.isEmpty()) {
            state.put(RIConstants.BUILD_TIME_DECISIONS, new HashMap<>(instance.recorded));
        }
    }

    /**
     * The value the build which rendered the view decided on for the given key, or {@code null} when this build is not
     * the one which restores the view, when that build decided nothing under this key, or when the feature is off.
     *
     * @param context the {@link FacesContext} for the current request
     * @param key the key of the decision, as generated by {@link TagHandlerImpl#buildTimeDecisionKey}
     * @return the value the build which rendered the view decided on
     */
    public static Object replay(FacesContext context, String key) {
        SavedBuildTimeDecisions instance = active(context);

        return instance != null && instance.replaying ? instance.restored.get(key) : null;
    }

    /**
     * Records the value this build decided on for the given key, to be saved with the state.
     *
     * @param context the {@link FacesContext} for the current request
     * @param key the key of the decision, as generated by {@link TagHandlerImpl#buildTimeDecisionKey}
     * @param value the value this build decided on, of a type the JDK declares, or an array of serializable values of
     * the application's own where a handler cannot express its decision in one
     */
    public static void record(FacesContext context, String key, Object value) {
        SavedBuildTimeDecisions instance = active(context);

        if (instance != null) {
            instance.recorded.put(key, value);
        }
    }

    /**
     * @return the instance for the current request, or {@code null} when the feature is off or the build in progress
     * decides nothing.
     */
    private static SavedBuildTimeDecisions active(FacesContext context) {
        SavedBuildTimeDecisions instance = of(context);
        return instance == null || instance.suspended ? null : instance;
    }

    /**
     * @return the instance for the current request, or {@code null} when the feature is off.
     */
    private static SavedBuildTimeDecisions of(FacesContext context) {
        Map<Object, Object> attributes = context.getAttributes();
        Object instance = attributes.get(KEY);

        if (instance == null) {
            instance = WebConfiguration.getInstance(context.getExternalContext())
                    .isOptionEnabled(BooleanWebContextInitParameter.RestoreBuildTimeDecisions)
                            ? new SavedBuildTimeDecisions()
                            : DISABLED;
            attributes.put(KEY, instance);
        }

        return instance == DISABLED ? null : (SavedBuildTimeDecisions) instance;
    }
}
