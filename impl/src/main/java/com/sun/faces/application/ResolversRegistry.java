/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
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
package com.sun.faces.application;

import com.sun.faces.context.flash.FlashELResolver;
import com.sun.faces.el.CompositeComponentAttributesELResolver;
import com.sun.faces.el.EmptyStringToNullELResolver;
import com.sun.faces.el.FacesResourceBundleELResolver;
import com.sun.faces.el.ResourceELResolver;
import com.sun.faces.el.ScopedAttributeELResolver;
import jakarta.el.ArrayELResolver;
import jakarta.el.BeanELResolver;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.ResourceBundleELResolver;

public class ResolversRegistry {
    public final BeanELResolver BEAN_RESOLVER = new BeanELResolver();
    public final ArrayELResolver ARRAY_RESOLVER = new ArrayELResolver();
    public final FacesResourceBundleELResolver FACES_BUNDLE_RESOLVER = new FacesResourceBundleELResolver();
    public final FlashELResolver FLASH_RESOLVER = new FlashELResolver();
    public final ListELResolver LIST_RESOLVER = new ListELResolver();
    public final MapELResolver MAP_RESOLVER = new MapELResolver();
    public final ResourceBundleELResolver BUNDLE_RESOLVER = new ResourceBundleELResolver();
    public final ScopedAttributeELResolver SCOPED_RESOLVER = new ScopedAttributeELResolver();
    public final ResourceELResolver RESOURCE_RESOLVER = new ResourceELResolver();
    public final CompositeComponentAttributesELResolver COMPOSITE_COMPONENT_ATTRIBUTES_EL_RESOLVER = new CompositeComponentAttributesELResolver();
    public final EmptyStringToNullELResolver EMPTY_STRING_TO_NULL_RESOLVER = new EmptyStringToNullELResolver();

}
