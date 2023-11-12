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

/**
 * <p class="changed_added_2_0">
 * A VDL handler that exposes {@link jakarta.faces.convert.Converter} to a <em>page author</em>. The default
 * implementation of Facelets must provide an implemention of this in the handler for the
 * <code>&lt;f:converter&gt;</code> (and any tags for any of the standard converters) tags.
 * </p>
 *
 * @since 2.0
 */
public interface ValueHolderAttachedObjectHandler extends AttachedObjectHandler {

}
