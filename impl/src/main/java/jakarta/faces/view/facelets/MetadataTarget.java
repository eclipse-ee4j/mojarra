/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2005-2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jakarta.faces.view.facelets;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * <p class="changed_added_2_0">
 * Information used with {@link MetaRule} for determining how and what {@link Metadata} should be wired.
 * </p>
 *
 * @since 2.0
 */
public abstract class MetadataTarget {

    /**
     * <p class="changed_added_2_0">
     * Return a beans <code>PropertyDescriptor</code> for the property with name given by argument <code>name</code>.
     * </p>
     *
     * @param name the name of the property for which the <code>PropertyDescriptor</code> must be returned.
     * @return the {@link PropertyDescriptor}.
     */
    public abstract PropertyDescriptor getProperty(String name);

    /**
     * <p class="changed_added_2_0">
     * Return true if the target for this metadata element is an instance of the argument <code>type</code>.
     * </p>
     *
     * @param type the <code>Class</code> to test for the instance of.
     * @return true if the type is a target instance, false otherwise.
     */
    public abstract boolean isTargetInstanceOf(Class type);

    /**
     * <p class="changed_added_2_0">
     * Return the <code>Class</code> of the metadata target.
     * </p>
     *
     * @return the target Class.
     */
    public abstract Class getTargetClass();

    /**
     * <p class="changed_added_2_0">
     * Return a <code>Class</code> for the property with name given by argument <code>name</code>.
     * </p>
     *
     * @param name the name of the property for which the <code>Class</code> must be returned.
     * @return the Class of the property.
     */
    public abstract Class getPropertyType(String name);

    /**
     * <p class="changed_added_2_0">
     * Return a <code>Method</code> for the setter of the property with name given by argument <code>name</code>.
     * </p>
     *
     * @param name the name of the property for which the <code>Method</code> must be returned.
     * @return the write {@link Method}.
     */
    public abstract Method getWriteMethod(String name);

    /**
     * <p class="changed_added_2_0">
     * Return a <code>Method</code> for the getter of the property with name given by argument <code>name</code>.
     * </p>
     *
     * @param name the name of the property for which the <code>Method</code> must be returned.
     * @return the read {@link Method}.
     */
    public abstract Method getReadMethod(String name);
}
