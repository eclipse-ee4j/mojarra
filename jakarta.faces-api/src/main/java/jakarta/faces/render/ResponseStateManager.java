/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;

import jakarta.faces.application.StateManager.StateSavingMethod;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_2"> ResponseStateManager</strong> is the helper class to
 * {@link jakarta.faces.application.StateManager} that knows the specific rendering technology being used to generate
 * the response. It is a singleton abstract class, vended by the {@link RenderKit}. This class knows the mechanics of
 * saving state, whether it be in hidden fields, session, or some combination of the two.
 * </p>
 */
public abstract class ResponseStateManager {

    /**
     * <p>
     * The name of the request parameter used by the default implementation of
     * {@link jakarta.faces.application.ViewHandler#calculateRenderKitId} to derive a RenderKit ID.
     * </p>
     */
    public static final String RENDER_KIT_ID_PARAM = "jakarta.faces.RenderKitId";

    /**
     * <p>
     * <span class="changed_modified_2_0 changed_modified_2_2">Implementations</span> must use this constant field value as
     * the name of the client parameter in which to save the state between requests. <span class="changed_added_2_2">The
     * <code>id</code> attribute must be a concatenation of the return from
     * {@link jakarta.faces.component.UIViewRoot#getContainerClientId}, the return from
     * {@link jakarta.faces.component.UINamingContainer#getSeparatorChar}, this constant field value, the separator char,
     * and a number that is guaranteed to be unique with respect to all the other instances of this kind of client parameter
     * in the view.</span>
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is strongly recommend that implementations guard against cross site scripting attacks by at least making the value
     * of this parameter difficult to predict.
     * </p>
     *
     * @since 1.2
     */
    public static final String VIEW_STATE_PARAM = "jakarta.faces.ViewState";

    /**
     * <p class="changed_added_2_2">
     * The name of the hidden field that refers to the encoded ClientWindow. This field is only used if
     * {@link jakarta.faces.lifecycle.ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME} is not "none". The <code>id</code>
     * attribute must be a concatenation of the return from {@link jakarta.faces.component.UIViewRoot#getContainerClientId},
     * the return from {@link jakarta.faces.component.UINamingContainer#getSeparatorChar}, this constant field value, the
     * separator char, and a number that is guaranteed to be unique with respect to all the other instances of this kind of
     * client parameter in the view. The value of this parameter is the return from
     * {@link jakarta.faces.lifecycle.ClientWindow#getId}.
     * </p>
     *
     * @since 2.2
     *
     */
    public static final String CLIENT_WINDOW_PARAM = "jakarta.faces.ClientWindow";

    /**
     * <p class="changed_added_2_2">
     * The name of the URL query parameter for transmitting the client window id. This parameter is only used if
     * {@link jakarta.faces.lifecycle.ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME} is not "none". The name of the parameter
     * is given by the constant value of this field. The value of this parameter is the return from
     * {@link jakarta.faces.lifecycle.ClientWindow#getId}.
     * </p>
     *
     * @since 2.2
     */
    public static final String CLIENT_WINDOW_URL_PARAM = "jfwid";

    /**
     * <p class="changed_added_2_2">
     * The value of this constant is taken to be the name of a request parameter whose value is inspected to verify the
     * safety of an incoming non-postback request with respect to the currently configured <code>Set</code> of protected
     * views for this application.
     * </p>
     *
     * @since 2.2
     */
    public static final String NON_POSTBACK_VIEW_TOKEN_PARAM = "jakarta.faces.Token";

    /**
     * <p>
     * <span class="changed_modified_2_2">Take</span> the argument <code>state</code> and write it into the output using the
     * current {@link ResponseWriter}, which must be correctly positioned already.
     * </p>
     *
     * <p class="changed_added_2_2">
     * Call {@link FacesContext#getViewRoot()}. If {@link jakarta.faces.component.UIComponent#isTransient()} returns
     * {@code true}, take implementation specific action so that the following call to {@link #isStateless} returns
     * {@code true} and return. Otherwise, proceed as follows.
     * </p>
     *
     * <p>
     * If the state is to be written out to hidden fields, the implementation must take care to make all necessary character
     * replacements to make the Strings suitable for inclusion as an HTTP request paramater.
     * </p>
     *
     * <p>
     * If the state saving method for this application is
     * <span class="changed_modified_5_0">{@link StateSavingMethod#CLIENT}</span>, the implementation
     * <span class="changed_modified_2_2">must</span> encrypt the state to be saved to the client
     * <span class="changed_modified_2_2">in a tamper evident manner</span>.
     * </p>
     *
     * <p>
     * If the state saving method for this application is
     * <span class="changed_modified_5_0">{@link StateSavingMethod#SERVER}</span>, and the current request is an
     * <code>Ajax</code> request ({@link jakarta.faces.context.PartialViewContext#isAjaxRequest} returns <code>true</code>),
     * use the current view state identifier if it is available (do not generate a new identifier).
     * </p>
     *
     * <p>
     * Write out the render kit identifier associated with this <code>ResponseStateManager</code> implementation with the
     * name as the value of the <code>String</code> constant <code>ResponseStateManager.RENDER_KIT_ID_PARAM</code>. The
     * render kit identifier must not be written if:
     * </p>
     * <ul>
     * <li>it is the default render kit identifier as returned by
     * {@link jakarta.faces.application.Application#getDefaultRenderKitId()} or</li>
     * <li>the render kit identfier is the value of <code>jakarta.faces.render.RenderKitFactory.HTML_BASIC_RENDER_KIT</code>
     * and {@link jakarta.faces.application.Application#getDefaultRenderKitId()} returns <code>null</code>.</li>
     * </ul>
     *
     * <p class="changed_added_2_2">
     * The {@link jakarta.faces.lifecycle.ClientWindow} must be written using these steps. Call
     * {@link jakarta.faces.context.ExternalContext#getClientWindow}. If the result is <code>null</code>, take no further
     * action regarding the <code>ClientWindow</code>. If the result is non-<code>null</code>, write a hidden field whose
     * name is {@link #CLIENT_WINDOW_PARAM} and whose id is
     * <code>&lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt;&lt;SEP&gt;jakarta.faces.ClientWindow&lt;SEP&gt;&lt;UNIQUE_PER_VIEW_NUMBER&gt;</code>
     * where &lt;SEP&gt; is the currently configured <code>UINamingContainer.getSeparatorChar()</code>.
     * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; is the return from <code>UIViewRoot.getContainerClientId()</code> on the view
     * from whence this state originated. &lt;UNIQUE_PER_VIEW_NUMBER&gt; is a number that must be unique within this view,
     * but must not be included in the view state. The value of the field is implementation dependent but must uniquely
     * identify this window within the user's session.
     * </p>
     *
     *
     * @since 1.2
     *
     * @param context The {@link FacesContext} instance for the current request
     * @param state The serialized state information previously saved
     * @throws IOException if the state argument is not an array of length 2.
     *
     */
    public void writeState(FacesContext context, Object state) throws IOException {
    }

    /**
     * <p class="changed_added_2_2">
     * If the preceding call to {@link #writeState(jakarta.faces.context.FacesContext, java.lang.Object)} was stateless,
     * return {@code true}. If the preceding call to {@code writeState()} was stateful, return {@code false}. Otherwise
     * throw {@code IllegalStateException}.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * To preserve backward compatibility with custom implementations that may have extended from an earlier version of this
     * class, an implementation is provided that returns <code>false</code>. A compliant implementation must override this
     * method to take the specified action.
     * </p>
     *
     * </div>
     *
     * @param context The {@link FacesContext} instance for the current request
     * @param viewId View identifier of the view to be restored
     * @throws NullPointerException if the argument {@code context} is {@code null}.
     * @throws IllegalStateException if this method is invoked and the statefulness of the preceding call to
     * {@link #writeState(jakarta.faces.context.FacesContext, java.lang.Object)} cannot be determined.
     *
     * @since 2.2
     *
     *
     * @return the value of the statelessness of this run through the lifecycle.
     *
     */
    public boolean isStateless(FacesContext context, String viewId) {
        return false;
    }

    /**
     * <p>
     * <span class="changed_modified_2_2">The</span> implementation must inspect the current request and return an Object
     * representing the tree structure and component state passed in to a previous invocation of
     * {@link #writeState(jakarta.faces.context.FacesContext,java.lang.Object)}.
     * </p>
     *
     * <p class="changed_added_2_2">
     * If the state saving method for this application is
     * <span class="changed_modified_5_0">{@link StateSavingMethod#CLIENT}</span>, <code>writeState()</code> will have
     * encrypted the state in a tamper evident manner. If the state fails to decrypt, or decrypts but indicates evidence of
     * tampering, a {@link jakarta.faces.application.ProtectedViewException} must be thrown.
     * </p>
     *
     * @since 1.2
     *
     * @param context The {@link FacesContext} instance for the current request
     * @param viewId View identifier of the view to be restored
     *
     * @return the tree structure and component state Object passed in to <code>writeState</code>. If this is an initial
     * request, this method returns <code>null</code>.
     */
    public Object getState(FacesContext context, String viewId) {
        return null;
    }

    /**
     * <p>
     * Return true if the current request is a postback. This method is leveraged from the <i>Restore View Phase</i> to
     * determine if {@link jakarta.faces.application.ViewHandler#restoreView} or
     * {@link jakarta.faces.application.ViewHandler#createView} should be called. The default implementation must return
     * <code>true</code> if this <code>ResponseStateManager</code> instance wrote out state on a previous request to which
     * this request is a postback, <code>false</code> otherwise.
     * </p>
     *
     * <p>
     * The implementation of this method for the Standard HTML RenderKit must consult the
     * {@link jakarta.faces.context.ExternalContext}'s <code>requestParameterMap</code> and return <code>true</code> if and
     * only if there is a key equal to the value of the symbolic constant {@link #VIEW_STATE_PARAM}.
     * </p>
     *
     * <p>
     * For backwards compatibility with implementations of <code>ResponseStateManager</code> prior to Jakarta Faces
     * 1.2, a default implementation is provided that consults the {@link jakarta.faces.context.ExternalContext}'s
     * <code>requestParameterMap</code> and return <code>true</code> if its size is greater than 0.
     * </p>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @return the value as specified above
     *
     * @since 1.2
     */
    public boolean isPostback(FacesContext context) {
        return !context.getExternalContext().getRequestParameterMap().isEmpty();
    }

    /**
     * <p>
     * Return the specified state as a <code>String</code> without any markup related to the rendering technology supported
     * by this ResponseStateManager.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @param state the state from which the String version will be generated from
     * @return the view state for this request without any markup specifics
     *
     * @since 2.0
     */
    public String getViewState(FacesContext context, Object state) {
        return null;
    }

    /**
     * <p class="changed_added_2_2">
     * Compliant implementations must return a cryptographically strong token for use to protect views in this application.
     * For backwards compatibility with earlier revisions, a default implementation is provided that simply returns
     * <code>null</code>.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     *
     * @return a cryptographically strong value
     *
     * @since 2.2
     */
    public String getCryptographicallyStrongTokenFromSession(FacesContext context) {
        return null;
    }
}
