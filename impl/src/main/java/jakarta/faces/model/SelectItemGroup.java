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

package jakarta.faces.model;

import java.util.Collection;

import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.UISelectOne;

/**
 * <p>
 * <strong>SelectItemGroup</strong> is a subclass of {@link SelectItem} that identifies a set of options that will be
 * made available as a subordinate "submenu" or "options list", depending upon the requirements of the
 * {@link UISelectMany} or {@link UISelectOne} renderer that is actually used. In general, the <code>value</code>
 * property of this instance will be ignored, and the <code>label</code> property of this instance will be used to label
 * the submenu.
 * </p>
 *
 * <p>
 * Although it is feasible to incorporate {@link SelectItemGroup} instances in he <code>selectItems</code> property of
 * this instance (thereby creating a data structure suitable for cascading submenus), some renderers may place
 * restrictions on the level of nesting they support. For example, HTML based renderers that create an
 * <code>&lt;select&gt;</code> element will typically render this instance as an <code>&lt;optgroup&gt;</code> element,
 * but the HTML 4.01 Specification disallows nested option groups.
 * </p>
 */

public class SelectItemGroup extends SelectItem {

    // ------------------------------------------------------------ Constructors

    private static final long serialVersionUID = 8355957402275580167L;

    /**
     * <p>
     * Construct a <code>SelectItemGroup</code> with no initialized property values.
     * </p>
     */
    public SelectItemGroup() {

        super();

    }

    /**
     * <p>
     * Construct a <code>SelectItemGroup</code> with the specified label and no associated <code>selectItem</code>s. The
     * <code>value</code> property will be set to a zero-length String, the <code>description</code> property will be set to
     * <code>null</code>, and the <code>disabled</code> property will be set to false.
     * </p>
     *
     * @param label Label to be rendered for this group in the response
     *
     * @throws NullPointerException if <code>label</code> is <code>false</code>
     */
    public SelectItemGroup(String label) {

        super("", label);

    }

    /**
     * <p>
     * Construct a <code>SelectItemGroup</code> with the specified properties. The <code>value</code> property will be set
     * to a zero-length String.
     * </p>
     *
     * @param label Label to be rendered for this group in the response
     * @param description Description of this group, for use in tools
     * @param disabled Flag indicating that this group is disabled
     * @param selectItems <span class="changed_modified_4_0">Variable array</span> of {@link SelectItem} describing the items available in this group
     *
     * @throws NullPointerException if <code>label</code> or <code>selectItems</code> is <code>false</code>
     */
    public SelectItemGroup(String label, String description, boolean disabled, SelectItem... selectItems) {

        super("", label, description, disabled);
        setSelectItems(selectItems);

    }

    /**
     * <p class="changed_added_4_0">
     * Construct a <code>SelectItemGroup</code> with the specified properties. The <code>value</code> property will be set
     * to a zero-length String.
     * </p>
     *
     * @param label Label to be rendered for this group in the response
     * @param description Description of this group, for use in tools
     * @param disabled Flag indicating that this group is disabled
     * @param selectItems Collection of {@link SelectItem} describing the items available in this group
     *
     * @throws NullPointerException if <code>label</code> or <code>selectItems</code> is <code>false</code>
     * 
     * @since 4.0
     */
    public SelectItemGroup(String label, String description, boolean disabled, Collection<? extends SelectItem> selectItems) {

        super("", label, description, disabled);
        setSelectItems(selectItems);

    }

    // ------------------------------------------------------ Instance Variables

    private SelectItem[] selectItems = null;

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return the set of subordinate {@link SelectItem}s for this group.
     * </p>
     *
     * @return the set of subordinate {@link SelectItem}s for this group
     */
    public SelectItem[] getSelectItems() {

        return selectItems;

    }

    /**
     * <p>
     * Set the set of subordinate {@link SelectItem}s for this group <span class="changed_modified_4_0">as a variable array</span>.
     * </p>
     *
     * @param selectItems The new set of subordinate items <span class="changed_modified_4_0">as a variable array</span>
     *
     * @throws NullPointerException if <code>selectItems</code> is <code>null</code>
     */
    public void setSelectItems(SelectItem... selectItems) {

        if (selectItems == null) {
            throw new NullPointerException();
        }
        this.selectItems = selectItems;

    }

    /**
     * <p class="changed_added_4_0">
     * Set the set of subordinate {@link SelectItem}s for this group as a collection.
     * </p>
     *
     * @param selectItems The new set of subordinate items as a collection.
     *
     * @throws NullPointerException if <code>selectItems</code> is <code>null</code>
     * 
     * @since 4.0
     */
    public void setSelectItems(Collection<? extends SelectItem> selectItems) {

        if (selectItems == null) {
            throw new NullPointerException();
        }
        setSelectItems(selectItems.toArray(new SelectItem[0]));

    }

}
