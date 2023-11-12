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

/**
 * <p class="changed_added_2_0">
 * The root class of the abstraction that dictates how attributes on a markup element in a Facelets VDL page are wired
 * to the Jakarta Faces API object instance associated with that element. The single method on this interface,
 * {@link #applyRule}, returns an encapsulation of the behavior that actually does the work of handling the attribute
 * and its value. There are implementations of specific concrete subclasses of this class for all the basic kinds of
 * elements that appear in Facelets VDL pages: components, non-components, and attached objects.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * For example, consider this markup:
 * </p>
 *
 * <pre>
 * <code>&lt;h:inputText value="#{user.userid}"
 * valueChangeListener="#{user.newUserId}" /&gt;</code>
 * </pre>
 *
 * <p>
 * Two <code>MetaRule</code> instances are involved in this example.
 * </p>
 *
 * <ol>
 *
 * <li>
 * <p>
 * The first has an <code>applyRule()</code> method that returns a {@link Metadata} instance that, when its
 * <code>applyMetada()</code> method is called, dictates how the "value" attribute is processed: calling
 * {@link jakarta.faces.component.UIComponent#setValueExpression} on the <code>UIComponent</code> instance associated
 * with the <code>&lt;h:inputText&gt;</code> element.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * The second has an <code>applyRule()</code> method that returns a {@link Metadata} instance that, when its
 * <code>applyMetadata()</code> method is called, dictates how the "valueChangeListener" attribute is processed: calling
 * {@link jakarta.faces.component.EditableValueHolder#addValueChangeListener}.
 * </p>
 * </li>
 *
 * </ol>
 *
 * </div>
 *
 * @since 2.0
 */
public abstract class MetaRule {

    /**
     * <p class="changed_added_2_0">
     * Return an abstraction that takes appropriate action given the kind of rule represented by the argument
     * <code>name</code>, in the context of this particular concrete subclass of <code>MetaRule</code>. The abstraction must
     * encapsulate the value from the argument <code>attribute</code>.
     * </p>
     *
     * @param name the name for this rule. This will generally be the name of a tag attribute in the VDL.
     * @param attribute the name/value pair for this attribute on this particular instance of an element in the page.
     * @param meta the <code>MetadataTarged</code> that can be used to discern what kind of action to encapsulate within the
     * abstraction to be returned.
     * @return the {@link Metadata}.
     * @since 2.0
     */
    public abstract Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta);
}
