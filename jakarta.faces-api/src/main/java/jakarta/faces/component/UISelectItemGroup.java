/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package jakarta.faces.component;

import static jakarta.faces.component.PackageUtils.collectSelectItems;
import static jakarta.faces.component.PackageUtils.createSelectItem;

import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

/**
 * <p class="changed_added_4_0">
 * <strong>UISelectItemGroup</strong> is a component that may be nested inside a {@link UISelectMany} or {@link UISelectOne} component, and causes the addition
 * of one {@link SelectItemGroup} of one or more {@link SelectItem} instances to the list of available options in the parent component. This component
 * accepts only children of type {@link UISelectItems} or {@link UISelectItem}.
 * </p>
 *
 * @since 4.0
 */

public class UISelectItemGroup extends UISelectItem {

    // -------------------------------------------------------------------------------------------------------------------------------------- Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.SelectItemGroup";

    // ---------------------------------------------------------------------------------------------------------------------------------- ValueHolder Properties

    /**
     * <p>
     * Return a new {@link SelectItemGroup} instance containing one or more {@link SelectItem} instances represented by any nested {@link UISelectItems} or
     * {@link UISelectItem} components.
     * </p>
     */
    @Override
    public Object getValue() {
        SelectItemGroup group = createSelectItem(this, null, SelectItemGroup::new);
        group.setSelectItems(collectSelectItems(getFacesContext(), this));
        return group;
    }

}
