/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.composite;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.view.AttachedObjectTarget;

public class CompositeComponentMetadataUtils {

    /**
     * <p>Use the composite component metadata specification
     * in section JSF.3.6.2.1 to print out the metadata to
     * the argument writer.</p>
     * @throws IOException
     */

    public static void writeMetadata(BeanInfo metadata, 
            ResponseWriter writer) throws IOException{

        // Print out the top level BeanDescriptor stuff.
        BeanDescriptor descriptor = metadata.getBeanDescriptor();
        writeFeatureDescriptor("composite-component-BeanDescriptor", descriptor,
                writer);
        writeFeatureDescriptorValues(
                "composite-component-BeanDescriptor", descriptor,
                writer);
        PropertyDescriptor attributes[] = metadata.getPropertyDescriptors();
        for (PropertyDescriptor cur : attributes) {
            writeFeatureDescriptor("composite-component-attribute", cur,
                    writer);
            writeFeatureDescriptorValues("composite-component-attribute", cur,
                    writer);
        }
    }

    public static void writeFeatureDescriptor(String prefix,
            FeatureDescriptor fd, ResponseWriter writer) throws IOException {

        writer.write(prefix + "-name:" +
                fd.getName() + "\n");
        writer.write(prefix + "-displayName:" +
                fd.getDisplayName() + "\n");
        writer.write(prefix + "-shortDescription:" +
                fd.getShortDescription() + "\n");
        writer.write(prefix + "-expert:" +
                fd.isExpert() + "\n");
        writer.write(prefix + "-hidden:" +
                fd.isHidden() + "\n");
        writer.write(prefix + "-preferred:" +
                fd.isPreferred() + "\n");

    }

    public static void writeFeatureDescriptorValues(String prefix,
            FeatureDescriptor fd, ResponseWriter writer) throws IOException {

        Enumeration<String> extraValues = fd.attributeNames();
        String curName;
        while (extraValues.hasMoreElements()) {
            curName = extraValues.nextElement();
            if (curName.equals(AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY)) {
                List<AttachedObjectTarget> attachedObjects =
                        (List<AttachedObjectTarget>) fd.getValue(curName);
                for (AttachedObjectTarget curTarget : attachedObjects) {
                    writer.write(prefix + "-attached-object-" + curTarget.getName() + "\n");
                }
            } else if (curName.equals(UIComponent.FACETS_KEY)) {
                Map<String, PropertyDescriptor> facets =
                        (Map<String, PropertyDescriptor>) fd.getValue(curName);
                for (String cur : facets.keySet()) {
                    String facetPrefix = prefix + "-facet-" + cur;
                    writeFeatureDescriptor(facetPrefix, facets.get(cur),
                            writer);
                    writeFeatureDescriptorValues(facetPrefix,
                            facets.get(cur), writer);
                }
            } else {
                ValueExpression ve = (ValueExpression) fd.getValue(curName);
                writer.write(prefix + "-extra-attribute-" + curName + ": " +
                        ve.getValue(FacesContext.getCurrentInstance().getELContext())
                        + "\n");
            }
        }
    }


}
