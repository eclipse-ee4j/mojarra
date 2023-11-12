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

package jakarta.faces.view.facelets;

import jakarta.faces.FacesWrapper;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2 changed_modified_2_3">Abstract</span> factory for creating instances of
 * {@link TagHandlerDelegate}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.0
 */
public abstract class TagHandlerDelegateFactory implements FacesWrapper<TagHandlerDelegateFactory> {

    private TagHandlerDelegateFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public TagHandlerDelegateFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public TagHandlerDelegateFactory(TagHandlerDelegateFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_modified_2_3">
     * If this factory has been decorated, the implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public TagHandlerDelegateFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p class="changed_added_2_0">
     * Create and return a {@link TagHandlerDelegate} instance designed for use with {@link ComponentHandler}.
     * </p>
     *
     * @param owner the <code>ComponentHandler</code> instance being helped by this helper instance.
     *
     * @return the newly created instance
     *
     * @since 2.0
     */
    public abstract TagHandlerDelegate createComponentHandlerDelegate(ComponentHandler owner);

    /**
     * <p class="changed_added_2_0">
     * Create and return a {@link TagHandlerDelegate} instance designed for use with {@link ValidatorHandler}.
     * </p>
     *
     * @param owner the <code>ValidatorHandler</code> instance being helped by this helper instance.
     *
     * @return the newly created instance
     *
     * @since 2.0
     */
    public abstract TagHandlerDelegate createValidatorHandlerDelegate(ValidatorHandler owner);

    /**
     * <p class="changed_added_2_0">
     * Create and return a {@link TagHandlerDelegate} instance designed for use with {@link ConverterHandler}.
     * </p>
     *
     * @param owner the <code>ValidatorHandler</code> instance being helped by this helper instance.
     *
     * @return the newly created instance
     *
     * @since 2.0
     */
    public abstract TagHandlerDelegate createConverterHandlerDelegate(ConverterHandler owner);

    /**
     * <p class="changed_added_2_0">
     * Create and return a {@link TagHandlerDelegate} instance designed for use with {@link BehaviorHandler}.
     * </p>
     *
     * @param owner the <code>ValidatorHandler</code> instance being helped by this helper instance.
     *
     * @return the newly created instance
     *
     * @since 2.0
     */
    public abstract TagHandlerDelegate createBehaviorHandlerDelegate(BehaviorHandler owner);

}
