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

package com.sun.faces.cdi;

import static com.sun.faces.component.CompositeComponentStackManager.getManager;
import static jakarta.faces.component.UIComponent.getCurrentCompositeComponent;
import static jakarta.faces.context.FacesContext.getCurrentInstance;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_3">
 * The Composite Component producer is the CDI producer that allows EL resolving of <code>#{cc}</code>
 * </p>
 *
 * @since 2.3
 */
public class CompositeComponentProducer extends CdiProducer<Object> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    public CompositeComponentProducer(BeanManager beanManager) {
        super.name("cc")
            .beanClass(beanManager, UIComponent.class)
            .types(UIComponent.class)
            .create(e -> {

            FacesContext context = getCurrentInstance();

            // The following five lines violate the specification.
            // The specification states that the 'cc' implicit object
            // always evaluates to the current composite component,
            // however, this isn't desirable behavior when passing
            // attributes between nested composite components, so we
            // need to alter the behavior so that the components behave
            // as the user would expect.
            /* BEGIN DEVIATION */
            UIComponent component = getManager(context).peek();

            /* END DEVIATION */
            if (component == null) {
                component = getCurrentCompositeComponent(context);
            }

            return component;

        });

    }

}
