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
 * Every kind of markup element in Facelets VDL that has attributes that need to take action on a Jakarta Faces
 * Java API artifact is associated with an instance of this class. This class is an abstraction to enable a rule based
 * method for directing how different kinds of elements take different kinds of actions in the Jakarta Faces Java
 * API. For example, consider this markup:
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <pre>
 * <code>&lt;h:inputText value="#{user.userid}"
 * valueChangeListener="#{user.newUserId}" /&gt;</code>
 * </pre>
 *
 * <p>
 * This markup element corresponds to an instance of {@link jakarta.faces.component.html.HtmlInputText} in the view.
 * <code>HtmlImputText</code> has a number of attributes that are to be exposed to the page author.
 * <code>HtmlInputText</code> also implements {@link jakarta.faces.component.EditableValueHolder}, which extends
 * {@link jakarta.faces.component.ValueHolder}. Each of these interfaces also expose a number of attributes to the page
 * author.
 * </p>
 *
 * <p>
 * Facelets employes the strategy pattern to allow the manner in which all possible attributes are handled based on the
 * nature of the Jakarta Faces Java API artifact associated with the markup element.
 * </p>
 *
 * <p>
 * Subclasses override the {@link #createMetaRuleset} method to return a {@link MetaRuleset} instance encapsulating all
 * the strategies for all the attributes that make sense for this particular markup element. The runtime calls the
 * {@link #setAttributes(FaceletContext, Object)} method to cause those rules to be executed and applied.
 * </p>
 *
 * </div>
 *
 * @since 2.0
 */
public abstract class MetaTagHandler extends TagHandler {

    /**
     * Stores the last type.
     */
    private Class<?> lastType = Object.class;

    /**
     * Stores the mapper.
     */
    private Metadata mapper;

    /**
     * Constructor.
     *
     * @param config the tag configuration.
     */
    public MetaTagHandler(TagConfig config) {
        super(config);
    }

    /**
     * Extend this method in order to add your own rules.
     *
     * @param type the type.
     * @return the {@link MetaRuleset}.
     */
    protected abstract MetaRuleset createMetaRuleset(Class type);

    /**
     * Invoking/extending this method will cause the results of the created MetaRuleset to auto-wire state to the passed
     * instance.
     *
     * @param ctx the Facelet context.
     * @param instance the instance.
     */
    protected void setAttributes(FaceletContext ctx, Object instance) {
        if (instance != null) {
            Class<?> type = instance.getClass();
            if (mapper == null || !lastType.equals(type)) {
                lastType = type;
                mapper = createMetaRuleset(type).finish();
            }
            mapper.applyMetadata(ctx, instance);
        }
    }
}
