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

package jakarta.faces.validator;

import java.util.EventListener;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * A <strong class="changed_modified_2_0">Validator</strong> implementation is a class that can perform validation
 * (correctness checks) on a {@link jakarta.faces.component.EditableValueHolder}. Zero or more <code>Validator</code>s
 * can be associated with each {@link jakarta.faces.component.EditableValueHolder} in the view, and are called during
 * the <em>Process Validations</em> phase of the request processing lifecycle.
 * </p>
 *
 * <p>
 * Individual {@link Validator}s should examine the value and component that they are passed, and throw a
 * {@link ValidatorException} containing a {@link jakarta.faces.application.FacesMessage}, documenting any failures to
 * conform to the required rules.
 *
 * <p>
 * For maximum generality, {@link Validator} instances may be configurable based on properties of the {@link Validator}
 * implementation class. For example, a range check {@link Validator} might support configuration of the minimum and
 * maximum values to be used.
 * </p>
 *
 * <p>
 * {@link Validator} implementations must have a zero-arguments public constructor. In addition, if the
 * {@link Validator} class wishes to have configuration property values saved and restored with the view, the
 * implementation must also implement {@link jakarta.faces.component.StateHolder}.
 * </p>
 *
 * <p class="changed_added_2_0">
 * If the class implementing <code>Validator</code> has a {@link jakarta.faces.application.ResourceDependency}
 * annotation, the action described in <code>ResourceDependency</code> must be taken when
 * {@link jakarta.faces.component.EditableValueHolder#addValidator} is called. If the class implementing
 * <code>Validator</code> has a {@link jakarta.faces.application.ResourceDependencies} annotation, the action described
 * in <code>ResourceDependencies</code> must be taken when
 * {@link jakarta.faces.component.EditableValueHolder#addValidator} is called.
 * </p>
 *
 * @param <T> The generic type of object value to validate.
 */

public interface Validator<T> extends EventListener {

    /**
     * <p>
     * <span class="changed_modified_2_0">Perform</span> the correctness checks implemented by this {@link Validator}
     * against the specified {@link UIComponent}. If any violations are found, a {@link ValidatorException} will be thrown
     * containing the {@link jakarta.faces.application.FacesMessage} describing the failure.
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * For a validator to be fully compliant with Version 2 and later of the specification, it must not fail validation on
     * <code>null</code> or empty values unless it is specifically intended to address <code>null</code> or empty values. An
     * application-wide <code>&lt;context-param&gt;</code> is provided to allow validators designed for Jakarta Faces
     * 1.2 to work with Jakarta Faces 2 and later. The <code>jakarta.faces.VALIDATE_EMPTY_FIELDS</code>
     * <code>&lt;context-param&gt;</code> must be set to <code>false</code> to enable this backwards compatibility behavior.
     * </p>
     *
     * </div>
     *
     * @param context FacesContext for the request we are processing
     * @param component UIComponent we are checking for correctness
     * @param value the value to validate
     * @throws ValidatorException if validation fails
     * @throws NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>
     */
    void validate(FacesContext context, UIComponent component, T value) throws ValidatorException;

}
