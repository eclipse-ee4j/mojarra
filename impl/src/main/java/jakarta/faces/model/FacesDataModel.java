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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.faces.component.UIData;
import jakarta.inject.Qualifier;

/**
 * <div class="changed_added_2_3">
 * <p>
 * The presence of this annotation on a class automatically registers the class with the runtime as a {@link DataModel}
 * that is capable of wrapping a type indicated by the {@link FacesDataModel#forClass()} attribute.
 *
 * <p>
 * The runtime must maintain a collection of these {@link DataModel}s such that {@link UIData} and other components
 * defined by the Jakarta Faces Specification can query the runtime for a suitable {@link DataModel} wrapper
 * (adapter) for the type of their <code>value</code>. This has to be done after all wrappers for specific types such as
 * {@link Set} are tried, but before the {@link ScalarDataModel} is selected as the wrapper. See
 * {@link UIData#getValue()}.
 *
 * <p>
 * This query must work as follows:
 *
 * <p>
 * For an instance of type <code>Z</code> that is being bound to a <code>UIData</code> component or other component
 * defined by the Jakarta Faces Specification that utilizes <code>DataModel</code>, the query for that type must
 * return the <em>most specific</em> DataModel that can wrap <code>Z</code>.
 *
 * <p>
 * This <em>most specific</em> DataModel is defined as the DataModel that is obtained by first sorting the collection in
 * which the registered <code>DataModels</code> are stored <i>(for details on this sorting see below)</i> and then
 * iterating through the sorted collection from beginning to end and stopping this iteration at the first match where
 * for the class <code>ZZ</code> wrapped by the DataModel (as indicated by the {@link FacesDataModel#forClass()}
 * attribute) it holds that <code>ZZ.isAssignableFrom(Z)</code>. This match is then taken as the <em>most specific</em>
 * DataModel.
 *
 * <p>
 * The sorting must be done as follows:
 *
 * <p>
 * Sort on the class wrapped by a DataModel that is stored in the above mentioned collection such that for any 2 classes
 * <code>X</code> and <code>Y</code> from this collection, if an object of <code>X</code> is an <code>instanceof</code>
 * an object of <code>Y</code>, <code>X</code> appears in the collection <em>before</em> <code>Y</code>. The
 * collection's sorting is otherwise arbitrary. In other words, subclasses come before their superclasses.
 *
 * <p>
 * For example:
 *
 * <p>
 * Given <code>class B</code>, <code>class A extends B</code> and <code>class Q</code>, two possible orders are;
 * <ol>
 * <li><code>{A, B, Q}</code>
 * <li><code>{Q, A, B}</code>
 * </ol>
 *
 * <p>
 * The only requirement here is that <code>A</code> appears before <code>B</code>, since <code>A</code> is a subclass of
 * <code>B</code>.
 *
 * <p>
 * The specification does not define a public method to obtain an instance of the "most specific DataModel for a given
 * type". Such an instance can be obtained using code similar to the following.
 * </p>
 *
 * <pre>
 * <code>
 *   &#64;SuppressWarnings("unchecked")
 *   public &lt;T&gt; DataModel&lt;T&gt; createDataModel(Class&lt;T&gt; forClass, Object value) {
 *       class LocalUIData extends UIData {
 *           &#64;Override
 *           public DataModel&lt;?&gt; getDataModel() {
 *               return super.getDataModel();
 *           }
 *       }
 *       LocalUIData localUIData = new LocalUIData();
 *       localUIData.setValue(value);
 *
 *       return (DataModel&lt;T&gt;) localUIData.getDataModel();
 *   }
 * </code>
 * </pre>
 *
 * <p>
 * For example:
 * </p>
 *
 * <pre>
 * <code>
 * public class Child1 {
 *
 * }
 * </code>
 * </pre>
 *
 * and
 *
 * <pre>
 * <code>
 * package test.faces23;
 *
 * &#64;FacesDataModel(forClass = Child1.class)
 * public class Child1Model&lt;E&gt; extends DataModel&lt;E&gt; {
 *
 *    &#64;Override
 *    public int getRowCount() {
 *        return 0;
 *    }
 *
 *    &#64;Override
 *    public E getRowData() {
 *        return null;
 *    }
 *
 *    &#64;Override
 *    public int getRowIndex() {
 *        return 0;
 *    }
 *
 *    &#64;Override
 *    public Object getWrappedData() {
 *        return null;
 *    }
 *
 *    &#64;Override
 *    public boolean isRowAvailable() {
 *        return false;
 *    }
 *
 *    &#64;Override
 *    public void setRowIndex(int arg0) {
 *
 *    }
 *
 *    &#64;Override
 *    public void setWrappedData(Object arg0) {
 *
 *    }
 * }
 * </code>
 * </pre>
 *
 * <p>
 * Then the following must work:
 * </p>
 *
 * <pre>
 * <code>
 * DataModel&lt;Child1&gt; myModel = createDataModel(Child1.class, new Child1());
 * assert myModel instanceof Child1Model;
 * System.out.println(myModel.getClass());
 * </code>
 * </pre>
 *
 * <p>
 * The result printed should be e.g.: <code>"class
 * test.faces23.Child1Model"</code>
 * </p>
 *
 * </div>
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Qualifier
public @interface FacesDataModel {

    /**
     * <p class="changed_added_2_3">
     * The value of this annotation attribute is taken to be the type that the DataModel that is annotated with this
     * annotation is able to wrap.
     * </p>
     *
     * @return the type that the DataModel that is annotated with this annotation is able to wrap
     */
    Class<?> forClass() default Object.class;

    /**
     * <p class="changed_added_4_0">
     * Supports inline instantiation of the {@link FacesDataModel} qualifier.
     * </p>
     *
     * @since 4.0
     */
    public static final class Literal extends AnnotationLiteral<FacesDataModel> implements FacesDataModel {

        private static final long serialVersionUID = 1L;

        /**
         * Instance of the {@link FacesDataModel} qualifier.
         */
        public static final Literal INSTANCE = of(Object.class);

        private final Class<?> forClass;

        public static Literal of(Class<?> forClass) {
            return new Literal(forClass);
        }

        private Literal(Class<?> forClass) {
            this.forClass = forClass;
        }

        @Override
        public Class<?> forClass() {
            return forClass;
        }
    }
}
