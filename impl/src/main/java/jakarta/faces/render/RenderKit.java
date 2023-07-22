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

package jakarta.faces.render;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;

/**
 * <p>
 * <span class="changed_modified_2_0"><strong>RenderKit</strong></span> represents a collection of {@link Renderer}
 * instances that, together, know how to render Jakarta Faces {@link UIComponent} instances for a specific
 * client. Typically, {@link RenderKit}s are specialized for some combination of client device type, markup language,
 * and/or user <code>Locale</code>. A {@link RenderKit} also acts as a Factory for associated {@link Renderer}
 * instances, which perform the actual rendering process for each component.
 * </p>
 *
 * <p>
 * A typical Jakarta Faces implementation will configure one or more {@link RenderKit} instances at web
 * application startup. They are made available through calls to the <code>getRenderKit()</code> methods of
 * {@link RenderKitFactory}. Because {@link RenderKit} instances are shared, they must be implemented in a thread-safe
 * manner. Due to limitations in the current specification having multiple <code>RenderKit</code> instances at play in
 * the same application requires a custom {@link jakarta.faces.application.ViewHandler} instance that is aware of how to
 * deal with this case. This limitation will be lifted in a future version of the spec.
 * </p>
 *
 * <p>
 * The <code>RenderKit</code> instance must also vend a {@link ResponseStateManager} instance, which is used in the
 * process of saving and restoring tree structure and state.
 * </p>
 */
public abstract class RenderKit {

    /**
     * <p>
     * Register the specified {@link Renderer} instance, associated with the specified component <code>family</code> and
     * <code>rendererType</code>, to the set of {@link Renderer}s registered with this {@link RenderKit}, replacing any
     * previously registered {@link Renderer} for this combination of identifiers.
     * </p>
     *
     * @param family Component family of the {@link Renderer} to register
     * @param rendererType Renderer type of the {@link Renderer} to register
     * @param renderer {@link Renderer} instance we are registering
     *
     * @throws NullPointerException if <code>family</code> or <code>rendererType</code> or <code>renderer</code> is null
     */
    public abstract void addRenderer(String family, String rendererType, Renderer renderer);

    /**
     * <p>
     * Return the {@link Renderer} instance most recently registered for the specified component <code>family</code> and
     * <code>rendererType</code>, if any; otherwise, return <code>null</code>.
     * </p>
     *
     * @param family Component family of the requested {@link Renderer} instance
     * @param rendererType Renderer type of the requested {@link Renderer} instance
     *
     * @throws NullPointerException if <code>family</code> or <code>rendererType</code> is <code>null</code>
     *
     * @return the {@link Renderer} instance
     */
    public abstract Renderer getRenderer(String family, String rendererType);

    /**
     * <p>
     * Return an instance of {@link ResponseStateManager} to handle rendering technology specific state management
     * decisions.
     * </p>
     *
     * @return the {@link ResponseStateManager}
     */
    public abstract ResponseStateManager getResponseStateManager();

    /**
     * <p>
     * Use the provided <code>Writer</code> to create a new {@link ResponseWriter} instance for the specified (optional)
     * content type, and character encoding.
     * </p>
     *
     * <p>
     * Implementors are advised to consult the <code>getCharacterEncoding()</code> method of class
     * {@link jakarta.servlet.ServletResponse} to get the required value for the characterEncoding for this method. Since
     * the <code>Writer</code> for this response will already have been obtained (due to it ultimately being passed to this
     * method), we know that the character encoding cannot change during the rendering of the response.
     * </p>
     *
     * @param writer the Writer around which this {@link ResponseWriter} must be built.
     *
     * @param contentTypeList an "Accept header style" list of content types for this response, or <code>null</code> if the
     * RenderKit should choose the best fit. As of the current version, the values accepted by the Standard render-kit for
     * this parameter include any valid "Accept header style" String that includes the String <code>text/html</code>,
     * <code>application/xhtml+xml</code>, <code>application/xml</code> or <code>text/xml</code>. This may change in a
     * future version. The RenderKit must support a value for this argument that comes straight from the <code>Accept</code>
     * HTTP header, and therefore requires parsing according to the specification of the <code>Accept</code> header. Please
     * see <a href="http://www.ietf.org/rfc/rfc2616.txt?number=2616">Section 14.1 of RFC 2616</a> for the specification of
     * the <code>Accept</code> header.
     *
     * @param characterEncoding such as "ISO-8859-1" for this ResponseWriter, or <code>null</code> if the
     * <code>RenderKit</code> should choose the best fit. Please see
     * <a href="http://www.iana.org/assignments/character-sets">the IANA</a> for a list of character encodings.
     *
     * @return a new {@link ResponseWriter}.
     *
     * @throws IllegalArgumentException if no matching content type can be found in <code>contentTypeList</code>, no
     * appropriate content type can be found with the implementation dependent best fit algorithm, or no matching character
     * encoding can be found for the argument <code>characterEncoding</code>.
     *
     */
    public abstract ResponseWriter createResponseWriter(Writer writer, String contentTypeList, String characterEncoding);

    /**
     * <p>
     * Use the provided <code>OutputStream</code> to create a new {@link ResponseStream} instance.
     * </p>
     *
     * @param out the {@code OutputStream} around which to create the {@link ResponseStream}
     *
     * @return the new {@link ResponseStream}
     *
     */
    public abstract ResponseStream createResponseStream(OutputStream out);

    /**
     * <p class="changed_added_2_0">
     * Return an <code>Iterator</code> over the component-family entries supported by this <code>RenderKit</code> instance.
     * </p>
     *
     * <p class="changed_added_2_0">
     * The default implementation of this method returns an empty <code>Iterator</code>
     * </p>
     *
     * @since 2.0
     *
     * @return Return an <code>Iterator</code> over the component-family entries
     *
     */
    public Iterator<String> getComponentFamilies() {

        Set<String> empty = Collections.emptySet();
        return empty.iterator();

    }

    /**
     * <p class="changed_added_2_0">
     * Return an <code>Iterator</code> over the renderer-type entries for the given component-family.
     * </p>
     *
     * <p class="changed_added_2_0">
     * If the specified <code>componentFamily</code> is not known to this <code>RenderKit</code> implementation, return an
     * empty <code>Iterator</code>
     * </p>
     *
     * <p class="changed_added_2_0">
     * The default implementation of this method returns an empty <code>Iterator</code>
     * </p>
     *
     * @param componentFamily one of the members of the <code>Iterator</code> returned by {@link #getComponentFamilies}.
     *
     * @since 2.0
     *
     * @return an <code>Iterator</code> over the renderer-type
     *
     */
    public Iterator<String> getRendererTypes(String componentFamily) {

        Set<String> empty = Collections.emptySet();
        return empty.iterator();

    }

    /**
     * <p>
     * Register the specified {@link ClientBehaviorRenderer} instance, associated with the specified component
     * <code>type</code>, to the set of {@link ClientBehaviorRenderer}s registered with this {@link RenderKit}, replacing
     * any previously registered {@link ClientBehaviorRenderer} for this type.
     * </p>
     *
     * @param type type of the {@link ClientBehaviorRenderer} to register
     * @param renderer {@link ClientBehaviorRenderer} instance we are registering
     *
     * @throws NullPointerException if <code>type</code> or <code>renderer</code> is null
     *
     * @since 2.0
     */
    public void addClientBehaviorRenderer(String type, ClientBehaviorRenderer renderer) {
        throw new UnsupportedOperationException("The default implementation must override this method");
    }

    /**
     * <p>
     * Return the {@link ClientBehaviorRenderer} instance most recently registered for the specified <code>type</code>, if
     * any; otherwise, return <code>null</code>.
     * </p>
     *
     * @param type type of the requested {@link ClientBehaviorRenderer} instance
     *
     * @throws NullPointerException if <code>type</code> is <code>null</code>
     *
     * @since 2.0
     *
     * @return the {@link ClientBehaviorRenderer} instance
     */
    public ClientBehaviorRenderer getClientBehaviorRenderer(String type) {
        throw new UnsupportedOperationException("The default implementation must override this method");
    }

    /**
     * <p class="changed_added_2_0">
     * Return an <code>Iterator</code> over the {@link ClientBehaviorRenderer} types.
     * </p>
     *
     * @since 2.0
     *
     * @return an <code>Iterator</code> over the {@link ClientBehaviorRenderer}
     */
    public Iterator<String> getClientBehaviorRendererTypes() {
        throw new UnsupportedOperationException("The default implementation must override this method");
    }

}
