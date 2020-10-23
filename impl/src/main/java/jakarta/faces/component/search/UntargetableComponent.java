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

package jakarta.faces.component.search;

/**
 * <div class="changed_added_2_3">Components implementing this interface are ignored by the algorithm - especially in
 * the implementation of <code>@child(n)</code>, <code>@next</code> and <code>@previous</code>.
 *
 * If you suppose this case, markup/literal components must implement {@link UntargetableComponent}:
 *
 * <pre>
 * &lt;table&gt;
 *      &lt;tr&gt;
 *          &lt;td&gt;
 *              &lt;h:outputLabel for="@next" value="Name:" /&gt;
 *          &lt;/td&gt;
 *          &lt;td&gt;
 *              &lt;h:inputText id="input" value="#{bean.name} /&gt;
 *          &lt;/td&gt;
 *      &lt;/tr&gt;
 * &lt;/table&gt;
 * </pre>
 *
 * <code>@next</code> would otherwise actually target the markup:
 *
 * <pre>
 *  &lt;/td&gt;
 *  &lt;td&gt;
 * </pre>
 *
 * and not desired component:
 *
 * <pre>
 *  &lt;h:inputText id="input" value="#{bean.name} /&gt;
 * </pre>
 *
 * </div>
 *
 * @since 2.3
 */
public interface UntargetableComponent {

}
