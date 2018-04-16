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

package javax.faces.el;


/**
 * <p><strong>PropertyResolver</strong> represents a pluggable mechanism
 * for accessing a "property" of an underlying Java object instance.
 * Different {@link PropertyResolver} implementations can support
 * property resolution on instances of different Java classes (such as
 * introspection-based access to properties of a JavaBeans component, or
 * <code>get()</code> and <code>put()</code> calls on a
 * <code>java.util.Map</code> instance).</p>
 * <p>All implementations must respect the rules for JavaBeans component,
 * <code>java.util.Map</code>, <code>java.util.List</code> and array
 * instances defined for each method but are allowed to add custom
 * semantics for other types.</p>
 *
 * @deprecated This has been replaced by {@link javax.el.ELResolver}.  
 */

public abstract class PropertyResolver {


    /**
     * <p>Return the value of the specified property from the specified
     * base object.</p>
     * <p>For a bean base object, the property is coerced to a
     * <code>String</code> and used as the property name.
     * For all other base object types (e.g., a Map), the property is used
     * without any coercing.</p>
     *
     * @param base The base object whose property value is to be returned
     * @param property The property to be returned
     * @return The property value, or <code>null</code> if <code>base</code>
     *  or <code>property</code> is <code>null</code>, or if the property
     *  doesn't exist and the base object is a <code>Map</code> instance
     *
     * @throws EvaluationException if an exception is thrown while getting
     *  the property value (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws PropertyNotFoundException if the specified property
     *  for a bean base object does not exist or is not readable
     */
    public abstract Object getValue(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Return the value at the specified index of the specified
     * base object.</p>
     *
     * @param base The base object whose property value is to be returned
     * @param index The index of the value to return
     * @return The property value, or <code>null</code> if <code>base</code>
     *  is <code>null</code>, or if the index is out of bounds for the base
     *  object
     *
     * @throws EvaluationException if an exception is thrown while getting
     *  the property value (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws PropertyNotFoundException if the index is out of
     *  bounds or if <code>base</code> is <code>null</code>
     */
    public abstract Object getValue(Object base, int index)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Set the specified value of the specified property on
     * the specified base object.</p>
     * <p>For a bean base object, the property is coerced to a
     * <code>String</code> and used as the property name.
     * For all other base object types (e.g., a Map), the property is used
     * without any coercing.</p>
     *
     * @param base The base object whose property value is to be set
     * @param property The property to be set
     * @param value The value of the property to be set
     *
     * @throws EvaluationException if an exception is thrown while setting
     *  the property value (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws PropertyNotFoundException if the specified bean base
     *  object property does not exist or is not writeable, or if
     *  <code>base</code> or <code>name</code> is <code>null</code>
     */
    public abstract void setValue(Object base, Object property, Object value)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Set the value at the specified index of the specified
     * base object.</p>
     *
     * @param base The base object whose property value is to be set
     * @param index The index of the value to set
     * @param value The value to be set
     *
     * @throws EvaluationException if an exception is thrown while setting
     *  the property value (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws PropertyNotFoundException if the index is out of
     *  bounds or if <code>base</code> is <code>null</code>
     */
    public abstract void setValue(Object base, int index, Object value)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Checks if the specified property is read-only.</p>
     * <p>For a bean base object, the property is coerced to a
     * <code>String</code> and used as the property name.
     * For all other base object types (e.g., a Map), the property is used
     * without any coercing.</p>
     *
     * @param base The base object whose property is to be analyzed
     * @param property The property to be analyzed
     * @return <code>true</code> if the specified property of the specified
     *  base object is known to be immutable; otherwise <code>false</code>
     *
     * @throws EvaluationException if an exception is thrown while testing
     *  the property (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws PropertyNotFoundException if the specified bean base
     *  object property does not exist or if <code>base</code> or 
     *  <code>property</code> is <code>null</code>
     */
    public abstract boolean isReadOnly(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Checks if the specified index is read-only.</p>
     *
     * @param base The base object whose property is to be analyzed
     * @param index The index of the value whose type is to be returned
     * @return <code>true</code> if the value at the specified index of
     *  the specified base object is known to be immutable; otherwise,
     *  <code>false</code>
     *
     * @throws EvaluationException if an exception is thrown while testing
     *  the property (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws PropertyNotFoundException if the index is out of
     *  bounds or if <code>base</code> is <code>null</code>
     */
    public abstract boolean isReadOnly(Object base, int index)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Return the <code>java.lang.Class</code> representing the type
     * of the specified property.  An instance of any Object of that
     * type my be set as the value of that property, as long as the
     * property is not read-only.  The actual value of the property, if
     * non-null, is guaranteed to be an instance of this type, or an
     * instance of a subclass of this type.  This method is also useful
     * for discovering the type of Objects that may be set as the value
     * of the property.</p>
     *
     * <p>For a bean base object, the property is coerced to a
     * <code>String</code> and used as the property name.  For all other
     * base object types (e.g., a Map), the property is used without any
     * coercing.</p>
     *
     * @param base The base object whose property is to be analyzed
     * @param property The property to be analyzed
     * @return the <code>java.lang.Class</code> representing the type of
     *  the specified property of the specified base object, if it can be
     *  determined; otherwise <code>null</code>
     *
     * @throws EvaluationException if an exception is thrown while testing
     *  the property (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws PropertyNotFoundException if the specified bean base
     *  object property does not exist or if <code>base</code> or
     *  <code>property</code> is <code>null</code>
     */
    public abstract Class getType(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException;


    /**
     * <p>Return the <code>java.lang.Class</code> representing the type of
     * the specified index.</p>
     *
     * @param base The base object whose property is to be analyzed
     * @param index The index of the value whose type is to be returned
     * @return The <code>java.lang.Class</code> representing the type of
     *  value at the specified index of the specified base object, if it
     *  can be determined; otherwise <code>null</code>
     *
     * @throws EvaluationException if an exception is thrown while testing
     *  the property (the thrown exception must be included as the
     *  <code>cause</code> property of this exception)
     * @throws PropertyNotFoundException if the index is out of
     *  bounds or if <code>base</code> is <code>null</code>
     */
    public abstract Class getType(Object base, int index)
        throws EvaluationException, PropertyNotFoundException;


}
