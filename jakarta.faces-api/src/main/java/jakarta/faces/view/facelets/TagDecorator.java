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
 * <p>
 * <span class="changed_modified_2_2 changed_modified_2_3">Provides</span> the ability to completely change the Tag
 * before it's processed for compiling with the associated {@link TagHandler}.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * The runtime must provide a default implementation of this interface that performs the following actions in its
 * {@link #decorate} method.
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * Inspect the attributes of the {@code tag} argument. If none of the attributes are declared to be in the {@code
 * jakarta.faces} namespace, iterate through the list of {@code TagDecorator} instances created from the
 * values in the {@link jakarta.faces.application.ViewHandler#FACELETS_DECORATORS_PARAM_NAME} {@code context-param}, if
 * any. For each entry, call its {@link #decorate} method, passing the argument {@code tag}. The first such entry that
 * returns non-{@code null} from its {@link #decorate} method must cause the iteration to stop.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * If one or more of the attributes of the {@code tag} argument are in the {@code jakarta.faces} namespace,
 * obtain a reference to <strong>decoratedTag</strong> as described in the following steps and iterate through the list
 * of {@link TagDecorator} instances as described in the preceding step, but pass <strong>decoratedTag</strong> to each
 * call to {@link #decorate}.
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * If the namespace of the tag is any namespace other than the empty string or {@code http://www.w3.org/1999/xhtml},
 * throw a {@link FaceletException}.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Let <strong>localName</strong> be the return from {@link Tag#getLocalName}. Use <strong>localName</strong> to
 * identify an entry in a data structure based on the following table. Once an entry has been identified, let
 * <strong>targetTag</strong> be the value of the "target tag" column for that entry.
 * </p>
 *
 * <table border="1">
 * <caption>localName and selector attribute to tag mapping</caption>
 * <tr>
 *
 * <th>localName</th>
 *
 * <th>selector attribute</th>
 *
 * <th>target tag</th>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>a</td>
 *
 * <td>faces:action</td>
 *
 * <td>h:commandLink</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>a</td>
 *
 * <td>faces:actionListener</td>
 *
 * <td>h:commandLink</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>a</td>
 *
 * <td>faces:value</td>
 *
 * <td>h:outputLink</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>a</td>
 *
 * <td>faces:outcome</td>
 *
 * <td>h:link</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>body</td>
 *
 * <td></td>
 *
 * <td>h:body</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>button</td>
 *
 * <td></td>
 *
 * <td>h:commandButton</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>button</td>
 *
 * <td>faces:outcome</td>
 *
 * <td>h:button</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>form</td>
 *
 * <td></td>
 *
 * <td>h:form</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>head</td>
 *
 * <td></td>
 *
 * <td>h:head</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>img</td>
 *
 * <td></td>
 *
 * <td>h:graphicImage</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="button"</td>
 *
 * <td>h:commandButton</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="checkbox"</td>
 *
 * <td>h:selectBooleanCheckbox</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="color"</td>
 *
 * <td rowspan="12">h:inputText</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="date"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="datetime"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="datetime-local"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="email"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="month"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="number"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="range"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="search"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="time"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="url"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="week"</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="file"</td>
 *
 * <td>h:inputFile</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="hidden"</td>
 *
 * <td>h:inputHidden</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="password"</td>
 *
 * <td>h:inputSecret</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="reset"</td>
 *
 * <td>h:commandButton</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="submit"</td>
 *
 * <td>h:commandButton</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>input</td>
 *
 * <td>type="*"</td>
 *
 * <td>h:inputText</td>
 *
 * </tr>
 * <tr>
 *
 * <td>label</td>
 *
 * <td></td>
 *
 * <td>h:outputLabel</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>link</td>
 *
 * <td></td>
 *
 * <td>h:outputStylesheet</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>script</td>
 *
 * <td></td>
 *
 * <td>h:outputScript</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>select</td>
 *
 * <td>multiple="*"</td>
 *
 * <td>h:selectManyListbox</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>select</td>
 *
 * <td></td>
 *
 * <td>h:selectOneListbox</td>
 *
 * </tr>
 *
 * <tr>
 *
 * <td>textarea</td>
 *
 * <td></td>
 *
 * <td>h:inputTextArea</td>
 *
 * </tr>
 *
 * </table>
 *
 * <p>
 * In the case where there are multiple rows with the same <strong>localName</strong>, find a matching entry by using
 * the argument {@code tag}'s attributes and the value from the "selector attribute" column in the table in the given
 * order. A selector attribute value of <strong>*</strong> indicates any value. In the table, a selector attribute name
 * prefixed with <strong>faces:</strong> means the tag is treated as if it were in the {@code
 * jakarta.faces} namespace. In actual Facelet pages, the namespace is what matters, not the prefix.
 * </p>
 *
 *
 * <p>
 * If no matching entry is found, let {@code faces:element} be the value of <strong>targetTag</strong>
 * </p>
 *
 * </li>
 *
 * <li>
 * <p>
 * Convert all the attributes of the argument {@code tag} as follows. First, create a new instance of
 * {@link TagAttribute} with the following characteristics: location: from the argument {@code
 * tag}'s location, namespace: {@code
 * jakarta.faces.passthrough}, local name: value of
 * {@link jakarta.faces.render.Renderer#PASSTHROUGH_RENDERER_LOCALNAME_KEY}, qualified name: same as local name with the
 * "p:" prefix, value: from the argument {@code tag}'s local name. Let this {@code TagAttribute} be
 * <strong>elementNameTagAttribute</strong>.
 * </p>
 *
 * <p>
 * For each of argument {@code tag}'s attributes obtain a reference to a {@link TagAttribute} with the following
 * characteristics. For discussion let such an attribute be <strong>convertedTagAttribute</strong>.
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * <strong>convertedTagAttribute</strong>'s location: from the argument {@code tag}'s location.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * If the current attribute's namespace is {@code
 * jakarta.faces}, <strong>convertedTagAttribute</strong>'s qualified name must be the current attribute's
 * local name and <strong>convertedTagAttribute</strong>'s namespace must be the empty string. This will have the effect
 * of setting the current attribute as a proper property on the {@code UIComponent} instance represented by this
 * markup.</li>
 *
 * <li>
 * <p class="changed_modified_2_3">
 * If the current attribute's namespace is non-empty and different from the argument {@code tag}'s namespace, let the
 * current attribute be <strong>convertedTagAttribute</strong>.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Otherwise, assume the current attribute's namespace is {@code
 * jakarta.faces.passthrough}. <strong>ConvertedTagAttribute</strong>'s qualified name is the current
 * attribute's local name prefixed by "p:". <strong>convertedTagAttribute</strong>'s namespace must be {@code
 * jakarta.faces.passthrough}.
 * </p>
 * </li>
 *
 * </ul>
 *
 * <p>
 * Create a {@link TagAttributes} instance containing <strong>elementNameTagAttribute</strong> and all the
 * <strong>convertedTagAttribute</strong>s.
 * </p>
 *
 * </li>
 *
 * <li>
 * <p>
 * Create a new {@link Tag} instance with the following characteristics.
 * </p>
 *
 * <p>
 * location: from the argument {@code tag}'s location.
 * </p>
 *
 * <p>
 * namespace: if <strong>targetTag</strong>'s prefix is "h", {@code
 * jakarta.faces.html}; if <strong>targetTag</strong>'s prefix is "faces", {@code jakarta.faces}.
 * </p>
 *
 * <p>
 * local name: the local name from the target tag column.
 * </p>
 *
 * <p>
 * attributes: the {@link TagAttributes} from the preceding step.
 *
 * <p>
 * Let this new {@link Tag} instance be <strong>convertedTag</strong>.
 *
 * </li>
 *
 * </ul>
 *
 * </li>
 *
 * </ul>
 *
 * <p>
 * The {@link Tag} instance returned from this decoration process must ultimately be passed to a {@link FaceletHandler}
 * instance as described in 
 * the Jakarta Faces Specification Document section 10.2.1 "Specification of the ViewDeclarationLanguage Implementation for Facelets for Jakarta Faces".
 * </p>
 *
 * </div>
 *
 */
public interface TagDecorator {

    /**
     * If handled, return a new Tag instance, otherwise return null
     *
     * @param tag tag to be decorated
     * @return a decorated tag, otherwise null
     */
    Tag decorate(Tag tag);
}
