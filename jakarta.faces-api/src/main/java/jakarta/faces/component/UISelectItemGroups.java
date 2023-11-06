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
import static jakarta.faces.component.PackageUtils.createSelectItems;

import java.util.ArrayList;
import java.util.List;

import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

/**
 * <p class="changed_added_4_0">
 * <strong>UISelectItemGroups</strong> is a component that may be nested inside a {@link UISelectMany} or {@link UISelectOne} component, and causes the addition
 * of one or more {@link SelectItemGroup} of one or more {@link SelectItem} instances to the list of available options in the parent component. This component
 * accepts only children of type {@link UISelectItems} or {@link UISelectItem}. The <code>value</code> attribute of this component, set either directly, or
 * acquired indirectly via a {@link jakarta.el.ValueExpression}, can be an array or {@link Iterable} of items of any type which is acceptable by the
 * <code>value</code> attribute of any nested {@link UISelectItems} or {@link UISelectItem} component.
 * </p>
 *
 * @since 4.0
 */

public class UISelectItemGroups extends UISelectItems {

    // -------------------------------------------------------------------------------------------------------------------------------------- Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.SelectItemGroups";

    // ---------------------------------------------------------------------------------------------------------------------------------- ValueHolder Properties

    /**
     * <p>
     * Iterate over the <code>value</code> attribute and wrap each item in a new {@link SelectItemGroup} instance whereby the item is exposed as a request
     * attribute under the key specified by the <code>var</code> property. This must allow any nested {@link UISelectItems} or {@link UISelectItem} component
     * to access the item via their attributes. Finally return these {@link SelectItemGroup} instances as an ordered collection.
     * </p>
     */
    @Override
    public Object getValue() {
        FacesContext context = getFacesContext();
        List<SelectItemGroup> groups = new ArrayList<>();

        createSelectItems(context, this, super.getValue(), SelectItemGroup::new, selectItemGroup -> {
            selectItemGroup.setSelectItems(collectSelectItems(context, this));
            groups.add(selectItemGroup);
        });

        return groups;
    }

}
