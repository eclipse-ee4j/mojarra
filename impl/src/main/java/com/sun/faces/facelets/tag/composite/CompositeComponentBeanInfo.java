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

package com.sun.faces.facelets.tag.composite;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.FacesException;

public class CompositeComponentBeanInfo extends SimpleBeanInfo implements BeanInfo, Externalizable {

    private BeanDescriptor descriptor = null;

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return descriptor;
    }

    public void setBeanDescriptor(BeanDescriptor newDescriptor) {
        descriptor = newDescriptor;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        List<PropertyDescriptor> list = getPropertyDescriptorsList();
        PropertyDescriptor[] result = new PropertyDescriptor[list.size()];
        list.toArray(result);
        return result;
    }

    private List<PropertyDescriptor> propertyDescriptors;

    public List<PropertyDescriptor> getPropertyDescriptorsList() {

        if (null == propertyDescriptors) {
            propertyDescriptors = new ArrayList<>();
        }
        return propertyDescriptors;
    }

    // ----------------------------------------------Methods From Externalizable

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeObject(descriptor.getBeanClass());
        if (propertyDescriptors != null && propertyDescriptors.size() > 0) {
            out.writeObject(propertyDescriptors.size());
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                out.writeObject(propertyDescriptor.getName());
                out.writeObject(propertyDescriptor.getValue("type"));
            }
        } else {
            out.writeObject(0);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        descriptor = new BeanDescriptor((Class<?>) in.readObject());
        int size = (int) in.readObject();
        for (int i = 0; i < size; i++) {

            try {
                String name = (String) in.readObject();
                CompositeAttributePropertyDescriptor pd = new CompositeAttributePropertyDescriptor(name, null, null); // NOPMD

                Object type = in.readObject();
                if (type != null) {
                    pd.setValue("type", type);
                }

                getPropertyDescriptorsList().add(pd);
            } catch (IntrospectionException ex) {
                throw new FacesException(ex);
            }
        }
    }
}
