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

package jakarta.faces.el;

import jakarta.el.ValueExpression;

/**
 * This interface <em>must</em> be implemented by the <code>Map</code> returned by the composite component
 * <code>ELResolver</code>, described in 
 * section 5.4.3.1 "faces.CompositeComponentAttributesELResolver" of the Jakarta Faces Specification Document,
 * when evaluating <code>#{cc.attrs}</code> expressions.
 *
 * @since 2.0
 */
public interface CompositeComponentExpressionHolder {

    /**
     * @param name that attribute name which may be associated with a <code>ValueExpression</code>
     * @return the <code>ValueExpression</code> associated with <code>name</code> otherwise return <code>null</code>
     */
    ValueExpression getExpression(String name);

}
