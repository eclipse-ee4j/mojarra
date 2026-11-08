/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.facelets.tag.composite;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import jakarta.el.ValueExpression;

import org.junit.jupiter.api.Test;

/**
 * Only an attribute which may hold a method expression needs the composite component to retarget method expressions.
 */
class CompositeComponentBeanInfoTest {

    @Test
    void anAttributeWithoutTypeAndMethodSignatureHoldsNone() throws IntrospectionException {
        assertFalse(beanInfo(attribute("value")).mayHoldMethodExpressions());
    }

    @Test
    void anAttributeWithAMethodSignatureMayHoldOne() throws IntrospectionException {
        PropertyDescriptor attribute = attribute("listener");
        attribute.setValue("method-signature", mock(ValueExpression.class));

        assertTrue(beanInfo(attribute).mayHoldMethodExpressions());
    }

    /**
     * An attribute with a type holds a value expression, also when it declares a method signature as well.
     */
    @Test
    void anAttributeWithATypeHoldsNone() throws IntrospectionException {
        PropertyDescriptor attribute = attribute("listener");
        attribute.setValue("type", String.class);
        attribute.setValue("method-signature", mock(ValueExpression.class));

        assertFalse(beanInfo(attribute).mayHoldMethodExpressions());
    }

    @Test
    void anAttributeWithASpecialNameMayHoldOne() throws IntrospectionException {
        assertTrue(beanInfo(attribute("action")).mayHoldMethodExpressions());
    }

    /**
     * The target attribute name is an expression, which may evaluate to a special name.
     */
    @Test
    void anAttributeWithATargetAttributeNameMayHoldOne() throws IntrospectionException {
        PropertyDescriptor attribute = attribute("submit");
        attribute.setValue("targetAttributeName", mock(ValueExpression.class));

        assertTrue(beanInfo(attribute).mayHoldMethodExpressions());
    }

    @Test
    void aCompositeComponentWithoutAttributesHoldsNone() {
        assertFalse(new CompositeComponentBeanInfo().mayHoldMethodExpressions());
    }

    private static CompositeComponentBeanInfo beanInfo(PropertyDescriptor... attributes) {
        CompositeComponentBeanInfo beanInfo = new CompositeComponentBeanInfo();
        for (PropertyDescriptor attribute : attributes) {
            beanInfo.getPropertyDescriptorsList().add(attribute);
        }
        return beanInfo;
    }

    private static PropertyDescriptor attribute(String name) throws IntrospectionException {
        return new CompositeAttributePropertyDescriptor(name, null, null);
    }

}
