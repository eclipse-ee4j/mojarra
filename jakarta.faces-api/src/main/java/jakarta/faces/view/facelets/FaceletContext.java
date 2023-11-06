/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.net.URL;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.FunctionMapper;
import jakarta.el.VariableMapper;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * Context representative of a single request from a Facelet. This instance is passed to nearly every method call in
 * this API.
 * </p>
 *
 * @since 2.0
 */
public abstract class FaceletContext extends ELContext {

    /**
     * The key in the FacesContext attribute map for the FaceletContext instance.
     */
    public static final String FACELET_CONTEXT_KEY = "jakarta.faces.FACELET_CONTEXT".intern();

    /**
     * <p class="changed_added_2_0">
     * The current FacesContext bound to this "request". Must not be <code>null</code>.
     * </p>
     *
     * @return The current FacesContext bound to this "request".
     *
     * @since 2.0
     */
    public abstract FacesContext getFacesContext();

    /**
     * <p class="changed_added_2_0">
     * Generate a unique ID for the passed String
     * </p>
     *
     * @param base the string from which to generate the ID.
     *
     * @return the generated id
     *
     * @since 2.0
     */
    public abstract String generateUniqueId(String base);

    /**
     * <p class="changed_added_2_0">
     * The ExpressionFactory to use within the Facelet this context is executing upon. Must not be <code>null</code>.
     * </p>
     *
     * @return the {@code ExpressionFactory} for this Facelet.
     *
     * @since 2.0
     */
    public abstract ExpressionFactory getExpressionFactory();

    /**
     * <p class="changed_added_2_0">
     * Set the VariableMapper to use in EL evaluation/creation.
     * </p>
     *
     * @param varMapper the new <code>VariableMapper</code>
     *
     * @since 2.0
     */
    public abstract void setVariableMapper(VariableMapper varMapper);

    /**
     * <p class="changed_added_2_0">
     * Set the FunctionMapper to use in EL evaluation/creation.
     * </p>
     *
     * @param fnMapper the new <code>FunctionMapper</code>
     *
     * @since 2.0
     */
    public abstract void setFunctionMapper(FunctionMapper fnMapper);

    /**
     * <p class="changed_added_2_0">
     * Support method which is backed by the current VariableMapper.
     * </p>
     *
     * @param name the name of the attribute
     * @param value the value of the attribute
     *
     * @since 2.0
     */
    public abstract void setAttribute(String name, Object value);

    /**
     * <p class="changed_added_2_0">
     * Return an attribute set by a previous call to {@link #setAttribute}. Support method which is backed by the current
     * VariableMapper
     * </p>
     *
     * @param name the name of the attribute to return.
     *
     * @return the value of the named attribute
     *
     * @since 2.0
     */
    public abstract Object getAttribute(String name);

    /**
     * <p class="changed_added_2_0">
     * Include another Facelet defined at some path, relative to the executing context, not the current Facelet (same as
     * include directive in Jakarta Server Pages)
     * </p>
     *
     * @param parent the <code>UIComponent</code> that will be the parent of any components in the included facelet.
     * @param relativePath the path of the resource containing the facelet markup, relative to the current markup
     *
     * @throws IOException if unable to load <code>relativePath</code>
     * @throws FaceletException if unable to parse the markup loaded from <code>relativePath</code>
     * @throws FacesException if unable to create child <code>UIComponent</code> instances
     * @throws ELException if any of the expressions in the markup loaded from <code>relativePath</code> fail
     *
     * @since 2.0
     */
    public abstract void includeFacelet(UIComponent parent, String relativePath) throws IOException;

    /**
     * <p class="changed_added_2_0">
     * Include another Facelet defined at some path, absolute to this ClassLoader/OS
     * </p>
     *
     * @param parent the <code>UIComponent</code> that will be the parent of any components in the included facelet.
     * @param absolutePath the absolute path to the resource containing the facelet markup
     *
     * @throws IOException if unable to load <code>relativePath</code>
     * @throws FaceletException if unable to parse the markup loaded from <code>relativePath</code>
     * @throws FacesException if unable to create child <code>UIComponent</code> instances
     * @throws ELException if any of the expressions in the markup loaded from <code>relativePath</code> fail
     *
     */
    public abstract void includeFacelet(UIComponent parent, URL absolutePath) throws IOException;

}
