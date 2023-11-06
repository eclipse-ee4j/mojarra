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

import java.io.IOException;

import jakarta.faces.component.UIComponent;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2">Abstract</span> class that defines methods relating to helping tag handler
 * instances. This abstraction enables implementation details to be hidden by the Jakarta Faces implementation
 * while still allowing concrete classes to be defined for extension by users.
 * </p>
 *
 * @since 2.0
 */
public abstract class TagHandlerDelegate {

    /**
     * <p class="changed_added_2_0">
     * Return a {@link MetaRuleset} particular to this kind of tag handler. Called from classes that implement
     * {@link MetaTagHandler}.
     * </p>
     *
     * @param type the <code>Class</code> for which the <code>MetaRuleset</code> must be created.
     *
     * @return a {@link MetaRuleset} particular to this kind of tag handler.
     *
     * @since 2.0
     */
    public abstract MetaRuleset createMetaRuleset(Class type);

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Called</span> by classes that implement
     * {@link jakarta.faces.view.facelets.FaceletHandler} in their implementation of <code>apply()</code>.
     * </p>
     *
     * <p class="changed_added_2_2">
     * If the argument {@code comp} is new to the view, for each tag attribute declared to be in the pass through attribute
     * namespace, set the name and value of the attribute into the pass through attributes map of the component. See
     * {@link UIComponent#getPassThroughAttributes(boolean) }. See the VDLDocs for the namespace URI of the pass through
     * attribute namespace. Attributes whose value is a {@code ValueExpression} must remain un-evaluated and stored in the
     * map as {@code ValueExpression} instances.
     * </p>
     *
     * @param ctx the <code>FaceletContext</code> for this request
     *
     * @param comp the <code>UIComponent</code> that corresponds to this element.
     *
     * @throws IOException if any files necessary to apply this tag handler have any difficulty while loading
     *
     */
    public abstract void apply(FaceletContext ctx, UIComponent comp) throws IOException;

}
