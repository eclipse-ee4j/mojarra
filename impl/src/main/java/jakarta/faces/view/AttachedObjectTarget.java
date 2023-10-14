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

package jakarta.faces.view;

import java.util.List;

import jakarta.faces.component.UIComponent;

/**
 * <p class="changed_added_2_0">
 * Within the declaration of a <em>composite component</em>, an <code>AttachedObjectTarget</code> allows the
 * <em>composite component author</em> to expose the semantics of an inner component to the <em>page author</em> without
 * exposing the rendering or implementation details of the <em>inner component</em>. See
 * {@link ViewDeclarationLanguage#getComponentMetadata} for the context in which implementations of this interface are
 * used.
 * </p>
 *
 * <p class="changed_added_2_0">
 * The implementation must ensure that this instance is thread safe and may be shared among different component trees.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * Subinterfaces are provided for the common behavioral interfaces: {@link jakarta.faces.component.behavior.Behavior},
 * {@link jakarta.faces.component.ValueHolder}, {@link jakarta.faces.component.EditableValueHolder} and
 * {@link jakarta.faces.component.ActionSource}. The default VDL implementation must provide a corresponding Facelets
 * tag handler for each of the subinterfaces of this interface.
 * </p>
 *
 * </div>
 *
 * @since 2.0
 */
public interface AttachedObjectTarget {

    /**
     * <p class="changed_added_2_0">
     * The key in the value set of the <em>composite component <code>BeanDescriptor</code></em>, the value for which is a
     * <code>List&lt;AttachedObjectTarget&gt;</code>.
     * </p>
     */
    String ATTACHED_OBJECT_TARGETS_KEY = "jakarta.faces.view.AttachedObjectTargets";

    /**
     * <p class="changed_added_2_0">
     * Returns the <code>List&lt;UIComponent&gt;</code> that this <code>AttachedObjectTarget</code> exposes. Each
     * <em>attached object</em> exposed by the <em>composite component author</em> may point at multiple
     * <code>UIComponent</code> instances within the composite component. This method is used by the
     * {@link jakarta.faces.view.ViewDeclarationLanguage#retargetAttachedObjects} method to take the appropriate action on
     * the attached object.
     * </p>
     *
     * @param topLevelComponent the top level component for this composite component.
     *
     * @return the result as specified above
     *
     */
    List<UIComponent> getTargets(UIComponent topLevelComponent);

    /**
     * <p class="changed_added_2_0">
     * Returns the name by which this attached object target is exposed to the <em>page author</em>.
     * </p>
     *
     *
     * @return the name of the attached object target
     *
     */
    String getName();

}
