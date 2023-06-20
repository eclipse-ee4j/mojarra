/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.event;

import jakarta.faces.FacesException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * <p>
 * <span class="changed_modified_2_2">Typesafe</span> enumeration of the legal values that may be returned by the
 * <code>getPhaseId()</code> method of the {@link FacesEvent} interface.
 */

public class PhaseId implements Comparable<PhaseId> {

    // ----------------------------------------------------------- Constructors

    /**
     * <p>
     * Private constructor to disable the creation of new instances.
     * </p>
     */
    private PhaseId(String newPhaseName) {
        phaseName = newPhaseName;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * <p>
     * The ordinal value assigned to this instance.
     * </p>
     */
    private final int ordinal = nextOrdinal++;

    /**
     * <p>
     * The name for this phase. (can't be null)
     * The constructor is private and all the values has a name, furthermore
     * inside the method {@link PhaseId#phaseIdValueOf(String)} the phase name it's required not null
     *
     * Even more the name is used inside UIViewAction
     * {@link jakarta.faces.component.UIViewAction#setPhase}
     * </p>
     */

    private final String phaseName;

    // --------------------------------------------------------- Public Methods

    /**
     * <p>
     * Compare this {@link PhaseId} instance to the specified one. Returns a negative integer, zero, or a positive integer
     * if this object is less than, equal to, or greater than the specified object.
     * </p>
     *
     * @param phaseId The other {@link PhaseId} to be compared to
     */
    @Override
    public int compareTo(PhaseId phaseId) {

        return ordinal - phaseId.ordinal;

    }

    /**
     * <p>
     * Return the ordinal value of this {@link PhaseId} instance.
     * </p>
     *
     * @return the ordinal
     */
    public int getOrdinal() {

        return ordinal;

    }

    /**
     * <p>
     * Return a String representation of this {@link PhaseId} instance.
     * </p>
     */
    @Override
    public String toString() {
        return phaseName + ' ' + ordinal;
    }

    /**
     * <p class="changed_added_2_2">
     * Return the name of this phase.
     * </p>
     *
     * @since 2.2
     *
     * @return the name
     */

    public String getName() {
        return phaseName;
    }

    /**
     * <p class="changed_added_2_2">
     * Return a <code>PhaseId</code> representation of the argument <code>phase</code>.
     * </p>
     *
     * @param phase the String for which the corresponding <code>PhaseId</code> should be returned.
     *
     * @throws NullPointerException if argument <code>phase</code> is <code>null</code>.
     *
     * @throws FacesException if the <code>PhaseId</code> corresponding to the argument <code>phase</code> cannot be found.
     *
     * @since 2.2
     *
     * @return the phase id corresponding to the argument {@code phase}
     */

    public static PhaseId phaseIdValueOf(String phase) {
        Objects.requireNonNull(phase);

        final PhaseId result = VALUES_BY_NAME.get( phase.toUpperCase() );

        if ( result == null) {
            throw new FacesException("Not a valid phase [" + phase + "]");
        }

        return result;
    }

    // ------------------------------------------------------- Static Variables

    /**
     * <p>
     * Static counter returning the ordinal value to be assigned to the next instance that is created.
     * </p>
     */
    private static int nextOrdinal = 0;

    // ------------------------------------------------------ Create Instances

    // Any new Phase values must go at the end of the list, or we will break
    // backwards compatibility on serialized instances

    private static final String ANY_PHASE_NAME = "ANY";
    /**
     * <p>
     * Identifier that indicates an interest in events, no matter which request processing phase is being performed.
     * </p>
     */
    public static final PhaseId ANY_PHASE = new PhaseId(ANY_PHASE_NAME);

    private static final String RESTORE_VIEW_NAME = "RESTORE_VIEW";
    /**
     * <p>
     * Identifier that indicates an interest in events queued for the <em>Restore View</em> phase of the request processing
     * lifecycle.
     * </p>
     */
    public static final PhaseId RESTORE_VIEW = new PhaseId(RESTORE_VIEW_NAME);

    private static final String APPLY_REQUEST_VALUES_NAME = "APPLY_REQUEST_VALUES";
    /**
     * <p>
     * Identifier that indicates an interest in events queued for the <em>Apply Request Values</em> phase of the request
     * processing lifecycle.
     * </p>
     */
    public static final PhaseId APPLY_REQUEST_VALUES = new PhaseId(APPLY_REQUEST_VALUES_NAME);

    private static final String PROCESS_VALIDATIONS_NAME = "PROCESS_VALIDATIONS";
    /**
     * <p>
     * Identifier that indicates an interest in events queued for the <em>Process Validations</em> phase of the request
     * processing lifecycle.
     * </p>
     */
    public static final PhaseId PROCESS_VALIDATIONS = new PhaseId(PROCESS_VALIDATIONS_NAME);

    private static final String UPDATE_MODEL_VALUES_NAME = "UPDATE_MODEL_VALUES";
    /**
     * <p>
     * Identifier that indicates an interest in events queued for the <em>Update Model Values</em> phase of the request
     * processing lifecycle.
     * </p>
     */
    public static final PhaseId UPDATE_MODEL_VALUES = new PhaseId(UPDATE_MODEL_VALUES_NAME);

    private static final String INVOKE_APPLICATION_NAME = "INVOKE_APPLICATION";
    /**
     * <p>
     * Identifier that indicates an interest in events queued for the <em>Invoke Application</em> phase of the request
     * processing lifecycle.
     * </p>
     */
    public static final PhaseId INVOKE_APPLICATION = new PhaseId(INVOKE_APPLICATION_NAME);

    private static final String RENDER_RESPONSE_NAME = "RENDER_RESPONSE";
    /**
     * <p>
     * Identifier for the <em>Render Response</em> phase of the request processing lifecycle.
     * </p>
     */
    public static final PhaseId RENDER_RESPONSE = new PhaseId(RENDER_RESPONSE_NAME);

    /**
     * <p>
     * Array of all defined values, ascending order of ordinal value. Be sure you include any new instances created above,
     * in the same order.
     * </p>
     */
    private static final PhaseId[] values = { ANY_PHASE, RESTORE_VIEW, APPLY_REQUEST_VALUES, PROCESS_VALIDATIONS, UPDATE_MODEL_VALUES, INVOKE_APPLICATION,
            RENDER_RESPONSE };

    /**
     * <p>
     * List of valid {@link PhaseId} instances, in ascending order of their ordinal value.
     * </p>
     */
    public static final List<PhaseId> VALUES = List.of(values);

    /**
     * <p>
     * Valid {@link PhaseId} instances, mapped by their uppercase name
     * </p>
     */
    public static final Map<String,PhaseId> VALUES_BY_NAME = unmodifiableMap(Stream.of(values).collect(toMap( phase -> phase.getName().toUpperCase() , identity())));

}
