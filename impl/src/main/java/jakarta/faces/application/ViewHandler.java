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

package jakarta.faces.application;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.push.PushContext;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * <p>
 * <strong><span class="changed_modified_2_0 changed_modified_2_1 changed_modified_2_2 changed_modified_2_3">
 * ViewHandler</span></strong> is the pluggablity mechanism for allowing implementations of or applications using the
 * Jakarta Faces Specification to provide their own handling of the activities in the <em>Render Response</em>
 * and <em>Restore View</em> phases of the request processing lifecycle.
 *
 * This allows for implementations to support different response generation technologies, as well as alternative
 * strategies for saving and restoring the state of each view.
 *
 * <span class="changed_added_2_0">An implementation of this class must be thread-safe.</span>
 * </p>
 *
 * <p>
 * Please see {@link StateManager} for information on how the <code>ViewHandler</code> interacts the
 * {@link StateManager}.
 * </p>
 *
 * <p class="changed_added_2_0">
 * Version 2 of the specification formally introduced the concept of <em>View Declaration Language</em>. A View
 * Declaration Language (VDL) is a syntax used to declare user interfaces comprised of instances of Jakarta Faces
 * {@link UIComponent}s. Any of the responsibilities of the <code>ViewHandler</code> that specifically deal with the VDL
 * sub-system are now the domain of the VDL implementation. These responsibilities are defined on the
 * {@link ViewDeclarationLanguage} class. The <code>ViewHandler</code> provides {@link #getViewDeclarationLanguage} as a
 * convenience method to access the VDL implementation given a <code>viewId</code>.
 * </p>
 *
 */
public abstract class ViewHandler {

    private static final Logger log = Logger.getLogger("jakarta.faces.application");

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The key, in the session's attribute set, under which the response character encoding may be stored and retrieved.
     * </p>
     *
     */
    public static final String CHARACTER_ENCODING_KEY = "jakarta.faces.request.charset";

    /**
     * <p class="changed_added_2_2 changed_modified_4_1">
     * If this param is set, and calling toLowerCase().equals("true") on a String representation of its value returns true,
     * the runtime must ensure that any XML comments in the Facelets source page are not delivered to the client.
     * </p>
     *
     * @since 2.0
     */
    public static final String FACELETS_SKIP_COMMENTS_PARAM_NAME = "jakarta.faces.FACELETS_SKIP_COMMENTS";

    /**
     * <p class="changed_added_2_0">
     * Allow the web application to define <span class="changed_modified_4_0">a list of alternate suffixes</span> for Facelet based XHTML pages containing Jakarta Faces
     * content. <span class="changed_added_4_0">This list is a space separated list of values of the form
     * <i><code>.&lt;extension&gt;</code></i>. The first physical resource whose extension matches one of the configured
     * extensions will be the suffix used to create the view ID.</span> If this init parameter is not specified, the default value is taken from the value of the constant
     * {@link #DEFAULT_FACELETS_SUFFIX}
     * </p>
     *
     * @since 2.0
     */
    public static final String FACELETS_SUFFIX_PARAM_NAME = "jakarta.faces.FACELETS_SUFFIX";

    /**
     * <p class="changed_added_2_0">
     * The value to use for the default extension for Facelet based XHTML pages if the webapp is using url extension
     * mapping.
     * </p>
     *
     * @since 2.0
     */
    public static final String DEFAULT_FACELETS_SUFFIX = ".xhtml";

    /**
     * <p class="changed_added_2_0">
     * Allow the web application to define a semicolon (;) separated list of strings that is used to forcibly declare that
     * certain pages in the application must be interpreted as using Facelets, regardless of their extension. Each entry in
     * the semicolon (;) separated list of strings is either a file extension, as in <code>*.xhtml</code>, or a resource
     * prefix (starting with '/' and interpreted as relative to the web application root), as in <code>/user/*</code>. The
     * latter class of entry can also take the form of <code>/&lt;filename&gt;.&lt;extension&gt;*</code> such as
     * <code>/login.xhtml*</code>.
     * </p>
     *
     * @since 2.0
     */
    public static final String FACELETS_VIEW_MAPPINGS_PARAM_NAME = "jakarta.faces.FACELETS_VIEW_MAPPINGS";

    /**
     * <p class="changed_added_2_2 changed_modified_4_1">
     * The buffer size to set on the response when the ResponseWriter is generated. By default the value is 1024. A value of
     * -1 will not assign a buffer size on the response. This should be increased if you are using development mode in order
     * to guarantee that the response isn't partially rendered when an error is generated.
     * </p>
     *
     * @since 2.0
     */
    public static final String FACELETS_BUFFER_SIZE_PARAM_NAME = "jakarta.faces.FACELETS_BUFFER_SIZE";

    /**
     * <p class="changed_added_2_2 changed_modified_4_1">
     * <span class="changed_modified_2_3">When</span> a page is requested, what interval in seconds should the compiler
     * check for changes. If you don't want the compiler to check for changes once the page is compiled, then use a value of
     * -1. Setting a low refresh period helps during development to be able to edit pages in a running application. 
     * <span class="changed_added_2_3 changed_modified_4_1">If this value
     * is not specified, then the default depends on {@link jakarta.faces.application.ProjectStage}. If it is {@code Production},
     * then runtime must act as if it is set to -1</span><span class="changed_added_4_1">, else the runtime must act as if it is
     * set to 0.</span>
     * </p>
     *
     * @since 2.0
     */
    public static final String FACELETS_REFRESH_PERIOD_PARAM_NAME = "jakarta.faces.FACELETS_REFRESH_PERIOD";

    /**
     * <p class="changed_added_2_2 changed_modified_4_1">
     * If this param is set, the runtime must interpret it as a semicolon (;) separated list of paths, starting with "/"
     * (without the quotes). The runtime must interpret each entry in the list as a path relative to the web application
     * root and interpret the file found at that path as a facelet tag library, conforming to the facelet taglibrary schema
     * and expose the tags therein according to Section "Facelet Tag Library mechanism".
     * </p>
     *
     *
     * @since 2.0
     */
    public static final String FACELETS_LIBRARIES_PARAM_NAME = "jakarta.faces.FACELETS_LIBRARIES";

    /**
     * <p class="changed_added_2_2 changed_modified_4_1">
     * A semicolon (;) delimitted list of class names of type jakarta.faces.view.facelets.TagDecorator, with a no-argument
     * constructor. These decorators will be loaded when the first request for a Facelets VDL view hits the ViewHandler for
     * page compilation.
     * </p>
     *
     * @since 2.0
     */
    public static final String FACELETS_DECORATORS_PARAM_NAME = "jakarta.faces.FACELETS_DECORATORS";


    // ---------------------------------------------------------- Public Methods

    /**
     *
     * <p>
     * <span class="changed_modified_2_0">Initialize</span> the view for the request processing lifecycle.
     * </p>
     *
     * <p>
     * This method must be called at the beginning of the <em>Restore View Phase</em> of the Request Processing Lifecycle.
     * It is responsible for performing any per-request initialization necessary to the operation of the lifycecle.
     * </p>
     *
     * <p class="changed_modified_2_0">
     * The default implementation must perform the following actions. If {@link ExternalContext#getRequestCharacterEncoding}
     * returns <code>null</code>, call {@link #calculateCharacterEncoding} and pass the result, if non-<code>null</code>,
     * into the {@link ExternalContext#setRequestCharacterEncoding} method. If
     * {@link ExternalContext#getRequestCharacterEncoding} returns non-<code>null</code> take no action.
     * </p>
     *
     * @param context the Faces context.
     * @throws FacesException if a problem occurs setting the encoding, such as the
     * <code>UnsupportedEncodingException</code> thrown by the underlying Jakarta Servlet or Portlet technology when the
     * encoding is not supported.
     *
     */
    public void initView(FacesContext context) throws FacesException {
        String encoding = context.getExternalContext().getRequestCharacterEncoding();
        if (encoding != null) {
            return;
        }

        encoding = calculateCharacterEncoding(context);
        if (encoding != null) {
            try {
                context.getExternalContext().setRequestCharacterEncoding(encoding);
            } catch (UnsupportedEncodingException e) {
                String message = "Can't set encoding to: " + encoding + " Exception:" + e.getMessage();
                log.fine(message);

                throw new FacesException(message, e);
            }
        }
    }

    /**
     * <p>
     * <span class="changed_modified_2_0">Perform</span> whatever actions are required to restore the view associated with
     * the specified {@link FacesContext} and <code>viewId</code>. It may delegate to the <code>restoreView</code> of the
     * associated {@link StateManager} to do the actual work of restoring the view. If there is no available state for the
     * specified <code>viewId</code>, return <code>null</code>.
     * </p>
     *
     * <p class="changed_added_2_0">
     * Otherwise, the default implementation must obtain a reference to the {@link ViewDeclarationLanguage} for this
     * <code>viewId</code> and call its {@link ViewDeclarationLanguage#restoreView} method, returning the result and not
     * swallowing any exceptions thrown by that method.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param viewId the view identifier for the current request
     * @return the restored view root, or <b>null</b>.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     * @throws FacesException if a Jakarta Servlet error occurs
     */
    public abstract UIViewRoot restoreView(FacesContext context, String viewId);

    /**
     * <p>
     * <strong class="changed_modified_2_0">Create</strong> and return a new {@link UIViewRoot} instance initialized with
     * information from the argument <code>FacesContext</code> and <code>viewId</code>.
     * <span class="changed_modified_2_0">Locate the {@link ViewDeclarationLanguage} implementation for the VDL used in the
     * view. The argument <code>viewId</code> must be converted to a physical <code>viewId</code> that can refer to an
     * actual resource suitable for use by the <code>ViewDeclarationLanguage</code>
     * {@link ViewDeclarationLanguage#createView}, which must be called by this method.</span>
     *
     * @param context the Faces context.
     * @param viewId the view id.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     *
     * @return the viewroot.
     */
    public abstract UIViewRoot createView(FacesContext context, String viewId);

    /**
     * <p>
     * <span class="changed_modified_2_0">Perform</span> whatever actions are required to render the response view to the
     * response object associated with the current {@link FacesContext}.
     * </p>
     *
     * <p class="changed_added_2_0">
     * Otherwise, the default implementation must obtain a reference to the {@link ViewDeclarationLanguage} for the
     * <code>viewId</code> of the argument <code>viewToRender</code> and call its {@link ViewDeclarationLanguage#renderView}
     * method, returning the result and not swallowing any exceptions thrown by that method.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param viewToRender the view to render
     *
     * @throws IOException if an input/output error occurs
     * @throws NullPointerException if <code>context</code> or <code>viewToRender</code> is <code>null</code>
     * @throws FacesException if a Jakarta Servlet error occurs
     */
    public abstract void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException;

    /**
     * <p>
     * Returns an appropriate {@link Locale} to use for this and subsequent requests for the current client.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return the locale.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract Locale calculateLocale(FacesContext context);

    /**
     * <p>
     * Returns the correct character encoding to be used for this request.
     * </p>
     *
     * <p>
     * The following algorithm is employed.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * Examine the <code>Content-Type</code> request header. If it has a <code>charset</code> parameter, extract it and
     * return that as the encoding.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * If no <code>charset</code> parameter was found, check for the existence of a session by calling
     * {@link ExternalContext#getSession(boolean)} passing <code>false</code> as the argument. If that method returns
     * <code>true</code>, get the session Map by calling {@link ExternalContext#getSessionMap} and look for a value under
     * the key given by the value of the symbolic constant {@link ViewHandler#CHARACTER_ENCODING_KEY}. If present, return
     * the value, converted to String.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Otherwise, return <code>null</code>
     * </p>
     * </li>
     *
     * </ul>
     *
     * @param context the Faces context.
     * @return the character encoding, or <code>null</code>
     * @since 1.2
     */
    public String calculateCharacterEncoding(FacesContext context) {
        ExternalContext extContext = context.getExternalContext();
        Map<String, String> headerMap = extContext.getRequestHeaderMap();
        String contentType = headerMap.get("Content-Type");
        String charEnc = null;

        // Look for a charset in the Content-Type header first.
        if (contentType != null) {

            // See if this header had a charset
            String charsetStr = "charset=";
            int len = charsetStr.length();
            int idx = contentType.indexOf(charsetStr);

            // If we have a charset in this Content-Type header AND it
            // has a non-zero length.
            if (idx != -1 && idx + len < contentType.length()) {
                charEnc = contentType.substring(idx + len);
            }
        }

        // failing that, look in the session for a previously saved one
        if (charEnc == null) {
            if (extContext.getSession(false) != null) {
                charEnc = (String) extContext.getSessionMap().get(CHARACTER_ENCODING_KEY);
            }
        }

        return charEnc;
    }

    /**
     * <p>
     * Return an appropriate <code>renderKitId</code> for this and subsequent requests from the current client. It is an
     * error for this method to return <code>null</code>.
     * </p>
     *
     * <p>
     * The default return value is {@link jakarta.faces.render.RenderKitFactory#HTML_BASIC_RENDER_KIT}.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @return the render kit id.
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract String calculateRenderKitId(FacesContext context);

    /**
     * <p class="changed_added_2_0">
     * Derive and return the viewId from the current request, or the argument input by following the algorithm defined in
     * section 7.6.2 "Default ViewHandler Implementation" of the Jakarta Faces Specification Document.
     * </p>
     *
     * <p class="changed_added_2_3">
     * This method should work correctly when the FacesServlet is invoked via either a <code>path mapping</code>,
     * <code>extension mapping</code> or an <code>exact match</code> (mapping) as defined by Servlet.12.2. Note that
     * <code>path mapping</code> is also commonly known as prefix mapping (e.g. "/faces/*") and
     * <code>extension mapping</code> as suffix mapping (e.g. "*.xhtml"). An <code>exact match</code> is possible where
     * there's a servlet mapping with an exact URL pattern such as "/foo".
     * </p>
     *
     * <p>
     * The default implementation of this method simply returns requestViewId unchanged.
     * </p>
     *
     * @param context the <code>FacesContext</code> for this request
     *
     * @param requestViewId the <code>viewId</code> to derive,
     * @return the derived view id.
     * @since 2.0
     */
    public String deriveViewId(FacesContext context, String requestViewId) {
        return requestViewId;
    }

    /**
     * <p class="changed_added_2_1">
     * Derive and return the viewId from the current request, or the argument input by following the algorithm defined in
     * section 7.6.2 "Default ViewHandler Implementation" of the Jakarta Faces Specification Document.
     * Note that unlike <code>deriveViewId()</code>, this method does not require that a
     * physical view be present.
     * </p>
     *
     * <p class="changed_added_2_3">
     * This method should work correctly when the FacesServlet is invoked via either a <code>path mapping</code>,
     * <code>extension mapping</code> or an <code>exact match</code> (mapping) as defined by Servlet.12.2. Note that
     * <code>path mapping</code> is also commonly known as prefix mapping (e.g. "/faces/*") and
     * <code>extension mapping</code> as suffix mapping (e.g. "*.xhtml"). An <code>exact match</code> is possible where
     * there's a servlet mapping with an exact URL pattern such as "/foo".
     * </p>
     *
     * <p>
     * The default implementation of this method simply returns requestViewId unchanged.
     * </p>
     *
     * @param context the <code>FacesContext</code> for this request
     *
     * @param requestViewId the <code>viewId</code> to derive,
     * @return the derived logical view id.
     * @since 2.1
     */
    public String deriveLogicalViewId(FacesContext context, String requestViewId) {
        return requestViewId;
    }

    /**
     * <p class="changed_modified_2_0">
     * <span class="changed_modified_2_2">If</span> the value returned from this method is used as the <code>file</code>
     * argument to the four-argument constructor for <code>java.net.URL</code> (assuming appropriate values are used for the
     * first three arguments), then a client making a request to the <code>toExternalForm()</code> of that <code>URL</code>
     * will select the argument <code>viewId</code> for traversing the Jakarta Faces lifecycle. Please see
     * section 7.6.2 "Default ViewHandler Implementation" of the Jakarta Faces Specification Document
     * for the complete specification, <span class="changed_added_2_2">especially for details related to view
     * protection using the {@link jakarta.faces.render.ResponseStateManager#NON_POSTBACK_VIEW_TOKEN_PARAM}
     * </span><span class="changed_added_2_3"> and the behavior when the current request is to a URL for which the
     * FacesServlet has an exact mapping as defined by Servlet.12.2</span>.
     * </p>
     *
     *
     * @param context {@link FacesContext} for this request
     * @param viewId View identifier of the desired view
     *
     * @throws IllegalArgumentException if <code>viewId</code> is not valid for this <code>ViewHandler</code>, or does not
     * start with "/".
     * @throws NullPointerException if <code>context</code> or <code>viewId</code> is <code>null</code>.
     *
     * @return the action url.
     */
    public abstract String getActionURL(FacesContext context, String viewId);

    /**
     * <p class="changed_added_2_0">
     * Return a Jakarta Faces action URL derived from the <code>viewId</code> argument that is suitable to be used by
     * the {@link NavigationHandler} to issue a redirect request to the URL using a NonFaces request. Compliant
     * implementations must implement this method as specified in 
     * section 7.6.2 "Default ViewHandler Implementation" of the Jakarta Faces Specification Document.
     * The default implementation simply calls
     * through to {@link #getActionURL}, passing the arguments <code>context</code> and <code>viewId</code>.
     * </p>
     *
     * @param context The FacesContext processing this request
     * @param viewId The view identifier of the target page
     * @param parameters A mapping of parameter names to one or more values
     * @param includeViewParams A flag indicating whether view parameters should be encoded into this URL
     * @return the redirect URL.
     * @since 2.0
     */
    public String getRedirectURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
        return getActionURL(context, viewId);
    }

    /**
     * <p class="changed_added_2_0">
     * Return a Jakarta Faces action URL derived from the viewId argument that is suitable to be used as the target
     * of a link in a Jakarta Faces response. Compliant implementations must implement this method as specified in
     * section 7.6.2 "Default ViewHandler Implementation" of the Jakarta Faces Specification Document.
     * The default implementation simply calls through to {@link #getActionURL}, passing the arguments
     * <code>context</code> and <code>viewId</code>.
     * </p>
     *
     * @param context The FacesContext processing this request
     * @param viewId The view identifier of the target page
     * @param parameters A mapping of parameter names to one or more values
     * @param includeViewParams A flag indicating whether view parameters should be encoded into this URL
     *
     * @return the bookmarkable URL.
     *
     * @since 2.0
     */
    public String getBookmarkableURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
        return getActionURL(context, viewId);
    }

    /**
     * <p class="changed_modified_2_0">
     * If the value returned from this method is used as the <code>file</code> argument to the four-argument constructor for
     * <code>java.net.URL</code> (assuming appropriate values are used for the first three arguments), then a client making
     * a request to the <code>toExternalForm()</code> of that <code>URL</code> will select the argument <code>path</code>
     * for direct rendering. If the specified path starts with a slash, it must be treated as context relative; otherwise,
     * it must be treated as relative to the action URL of the current view.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     * @param path Resource path to convert to a URL
     *
     * @throws IllegalArgumentException if <code>viewId</code> is not valid for this <code>ViewHandler</code>.
     * @throws NullPointerException if <code>context</code> or <code>path</code> is <code>null</code>.
     *
     * @return the resource URL.
     */
    public abstract String getResourceURL(FacesContext context, String path);

    /**
     * <p class="changed_added_2_3">
     * If the value returned from this method is used as the <code>file</code> argument to the four-argument constructor for
     * <code>java.net.URL</code> (assuming appropriate values are used for the first three arguments), then a client making
     * a push handshake request to the <code>toExternalForm()</code> of that <code>URL</code> will select the argument
     * <code>channel</code> for connecting the websocket push channel in the current view. It must match the
     * {@link PushContext#URI_PREFIX} of the endpoint.
     * </p>
     *
     * @param context {@link FacesContext} for the current request.
     * @param channel The channel name of the websocket.
     *
     * @throws NullPointerException if <code>context</code> or <code>channel</code> is <code>null</code>.
     *
     * @return the websocket URL.
     * @see PushContext#URI_PREFIX
     */
    public abstract String getWebsocketURL(FacesContext context, String channel);

    /**
     * <p class="changed_added_2_2">
     * Return an unmodifiable <code>Set</code> of the protected views currently known to this <code>ViewHandler</code>
     * instance. Compliant implementations must return a <code>Set</code> that is the concatenation of the contents of all
     * the <code>&lt;url-pattern&gt;</code> elements within all the <code>&lt;protected-views&gt;</code> in all of the
     * application configuration resources in the current application. The runtime must support calling this method at any
     * time after application startup. The default implementation returns an unmodifiable empty <code>Set</code>.
     * </p>
     *
     * @return the unmodifiable set of protected views.
     * @since 2.2
     */
    public Set<String> getProtectedViewsUnmodifiable() {
        return unmodifiableSet(emptySet());
    }

    /**
     * <p class="changed_added_2_2">
     * Add the argument <code>urlPattern</code> to the thread safe <code>Set</code> of protected views for this application.
     * Compliant implementations make it so a subsequent call to {@link #getProtectedViewsUnmodifiable} contains the
     * argument. The runtime must support calling this method at any time after application startup. The default
     * implementation takes no action.
     * </p>
     *
     * @param urlPattern the url-pattern to add.
     *
     * @since 2.2
     */
    public void addProtectedView(String urlPattern) {

    }

    /**
     * <p class="changed_added_2_2">
     * Remove the argument <code>urlPattern</code> from the thread safe <code>Set</code> of protected views for this
     * application, if present in the <code>Set</code>. If the argument <code>urlPattern</code> is not present in the
     * <code>Set</code>, this method has no effect. Compliant implementations must make it so a subsequent call to
     * {@link #getProtectedViewsUnmodifiable} does not contain the argument. The runtime must support calling this method at
     * any time after application startup. Returns <code>true</code> if this <code>Set</code> contained the argument. The
     * default implementation takes no action and returns <code>false</code>.
     * </p>
     *
     * @param urlPattern the url-pattern to remove.
     * @return <code>true</code> if in the <code>Set</code>, <code>false</code> otherwise.
     * @since 2.2
     */
    public boolean removeProtectedView(String urlPattern) {
        return false;
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_1">Return</span> the {@link ViewDeclarationLanguage} instance used for this
     * <code>ViewHandler</code> instance.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * The default implementation must use
     * {@link jakarta.faces.view.ViewDeclarationLanguageFactory#getViewDeclarationLanguage} to obtain the appropriate
     * <code>ViewDeclarationLanguage</code> implementation for the argument <code>viewId</code>. Any exceptions thrown as a
     * result of invoking that method must not be swallowed.
     * </p>
     *
     * <p>
     * The default implementation of this method returns null.
     * </p>
     *
     * </div>
     *
     * @param context the <code>FacesContext</code> for this request.
     *
     * @param viewId <span class="changed_modified_2_1">the logical view id, as returned from {@link #deriveLogicalViewId}
     * for which the <code>ViewDeclarationLanguage</code> should be returned.</span>
     * @return the ViewDeclarationLanguage, or <b>null</b>.
     * @since 2.0
     */
    public ViewDeclarationLanguage getViewDeclarationLanguage(FacesContext context, String viewId) {
        return null;
    }

    /**
     * <p class="changed_added_2_3">
     * Return a {@code Stream} possibly lazily populated by walking the view trees of every active
     * {@link ViewDeclarationLanguage} rooted at a given initial path. The view tree of every
     * {@link ViewDeclarationLanguage} is individually traversed <em>breadth-first</em> as per the contract of
     * {@link ViewDeclarationLanguage#getViews(FacesContext, String, int, ViewVisitOption...)}. The elements in the stream
     * are <em>logical</em> view ids.
     * </p>
     *
     * <p>
     * The {@code maxDepth} parameter is the maximum depth of directory levels to visit for each
     * {@code ViewDeclarationLanguage} <em>beyond the initial path</em>, which is always visited. The value is relative to
     * the root ({@code /}), not to the given initial path. E.g. given {@code maxDepth} = {@code 3} and initial path
     * {@code /foo/}, visiting will proceed up to {@code /foo/bar/}, where {@code /} counts as depth {@code 1},
     * {@code /foo/} as depth {@code 2} and {@code /foo/bar/} as depth {@code 3}. A value lower or equal to the depth of the
     * initial path means that only the initial path is visited. A value of {@link Integer#MAX_VALUE MAX_VALUE} may be used
     * to indicate that all levels should be visited.
     *
     * <p>
     * In case more than one active {@code ViewDeclarationLanguage} is present, the order in which view ids from each
     * {@code ViewDeclarationLanguage} appear in the stream is undetermined, except for the guarantee that every individual
     * {@code ViewDeclarationLanguage} is traversed <em>breadth-first</em>.
     *
     * @param facesContext The {@link FacesContext} for this request.
     * @param path The initial path from which to start looking for view ids.
     * @param maxDepth The absolute maximum depth of nested directories to visit counted from the root ({@code /}).
     * @param options The options to influence the traversal. See {@link ViewVisitOption} for details on those.
     *
     * @return the {@link Stream} of view ids
     *
     * @since 2.3
     */
    public Stream<String> getViews(FacesContext facesContext, String path, int maxDepth, ViewVisitOption... options) {
        return Stream.empty();
    }

    /**
     * <p class="changed_added_2_3">
     * Return a {@code Stream} possibly lazily populated by walking the view trees of every active
     * {@link ViewDeclarationLanguage} rooted at a given initial path. The view tree of every
     * {@link ViewDeclarationLanguage} is individually traversed <em>breadth-first</em> as per the contract of
     * {@link ViewDeclarationLanguage#getViews(FacesContext, String, int, ViewVisitOption...)}. The elements in the stream
     * are <em>logical</em> view ids.
     * </p>
     *
     * <p>
     * This method works as if invoking it were equivalent to evaluating the expression: <blockquote>
     *
     * <pre>
     * getViews(facesContext, start, Integer.MAX_VALUE, options)
     * </pre>
     *
     * </blockquote> Put differently, it visits all levels of the view tree.
     *
     * <p>
     * In case more than one active {@code ViewDeclarationLanguage} is present, the order in which view ids from each
     * {@code ViewDeclarationLanguage} appear in the stream is undetermined, except for the guarantee that every individual
     * {@code ViewDeclarationLanguage} is traversed <em>breadth-first</em>.
     *
     * @param facesContext The {@link FacesContext} for this request.
     * @param path The initial path from which to start looking for view ids.
     * @param options The options to influence the traversal. See {@link ViewVisitOption} for details on those.
     *
     * @return the {@link Stream} of view ids
     *
     * @since 2.3
     */
    public Stream<String> getViews(FacesContext facesContext, String path, ViewVisitOption... options) {
        return Stream.empty();
    }

    /**
     * <p>
     * Take any appropriate action to either immediately write out the current state information (by calling
     * {@link StateManager#writeState}, or noting where state information should later be written.
     * </p>
     *
     * <p class="changed_added_2_0">
     * This method must do nothing if the current request is an <code>Ajax</code> request. When responding to
     * <code>Ajax</code> requests, the state is obtained by calling {@link StateManager#getViewState} and then written into
     * the <code>Ajax</code> response during final encoding <span class="changed_modified_2_3">
     * ({@link jakarta.faces.context.PartialViewContext#processPartial(jakarta.faces.event.PhaseId)}) </span>.
     * </p>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @throws IOException if an input/output error occurs
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public abstract void writeState(FacesContext context) throws IOException;

}
