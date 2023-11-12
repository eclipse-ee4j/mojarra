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

import static jakarta.faces.validator.BeanValidator.ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME;
import static jakarta.faces.validator.BeanValidator.VALIDATOR_ID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.context.FacesContext;
import jakarta.validation.groups.Default;

class MultiFieldValidationUtils {

    static final String MULTI_FIELD_VALIDATION_CANDIDATES = VALIDATOR_ID + ".MULTI_FIELD_VALIDATION_CANDIDATES";

    /**
     * <p class="changed_added_2_3">
     * Special value to indicate the proposed value for a property failed field-level validation. This prevents any attempt
     * to perform class level validation.
     * </p>
     */
    static final String FAILED_FIELD_LEVEL_VALIDATION = VALIDATOR_ID + ".FAILED_FIELD_LEVEL_VALIDATION";

    /*
     * <p class="changed_added_2_3">Returns a data structure that stores the information necessary to perform class-level
     * validation by <code>&lt;f:validateWholeBean &gt;</code> components elsewhere in the tree. The lifetime of this data
     * structure does not extend beyond the current {@code FacesContext}. The data structure must conform to the following
     * specification.</p>
     *
     * <div class="changed_added_2_3">
     *
     * <ul>
     *
     * <li><p>It is a non-thread-safe {@code Map}.</p></li>
     *
     * <li><p>Keys are CDI bean instances that are referenced by the {@code value} attribute of
     * <code>&lt;f:validateWholeBean &gt;</code> components.</p></li>
     *
     * <li>
     *
     * <p>Values are {@code Map}s that represent the properties to be stored on the CDI bean instance that is the current
     * key. The inner {@code Map} must conform to the following specification.</p>
     *
     * <ul>
     *
     * <li><p>It is a non-thread-safe {@code Map}.</p></li>
     *
     * <li><p>Keys are property names.</p></li>
     *
     * <li><p>Values are {@code Map} instances. In this innermost map, the following keys are supported.</p>
     *
     * <p>component: Object that is the EditableValueHolder</p> <p>value: Object that is the value of the property</p>
     *
     * </li>
     *
     * </ul>
     *
     * </li>
     *
     *
     *
     * </ul>
     *
     * </div>
     *
     * @param context the {@link FacesContext} for this request
     *
     * @param create if {@code true}, the data structure must be created if not present. If {@code false} the data structure
     * must not be created and {@code Collections.emptyMap()} must be returned.
     *
     * @return the data structure representing the multi-field validation candidates
     *
     * @since 2.3
     */
    static Map<Object, Map<String, Map<String, Object>>> getMultiFieldValidationCandidates(FacesContext context, boolean create) {
        Map<Object, Object> attrs = context.getAttributes();
        Map<Object, Map<String, Map<String, Object>>> result;
        result = (Map<Object, Map<String, Map<String, Object>>>) attrs.get(MULTI_FIELD_VALIDATION_CANDIDATES);
        if (null == result) {
            if (create) {
                result = new HashMap<>();
                attrs.put(MULTI_FIELD_VALIDATION_CANDIDATES, result);
            } else {
                result = Collections.emptyMap();
            }
        }

        return result;
    }

    static boolean wholeBeanValidationEnabled(FacesContext context, Class[] validationGroupsArray) {
        boolean result;

        Map<Object, Object> attrs = context.getAttributes();
        if (!(attrs.containsKey(ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME) && (Boolean) attrs.get(ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME))) { // NOPMD
            return false;
        }

        result = !(1 == validationGroupsArray.length && Default.class == validationGroupsArray[0]);

        return result;
    }

}
