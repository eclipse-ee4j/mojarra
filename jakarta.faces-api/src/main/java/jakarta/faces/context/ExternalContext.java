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

package jakarta.faces.context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.faces.lifecycle.ClientWindow;

/**
 * <p>
 * <span class="changed_modified_2_0 changed_modified_2_1 changed_modified_2_2 changed_modified_2_3"> This</span> class
 * allows the Faces API to be unaware of the nature of its containing application environment. In particular, this class
 * allows Jakarta Faces based applications to run in either a Jakarta Servlet or a Portlet environment.
 * </p>
 *
 * <p class="changed_modified_2_0">
 * The documentation for this class only specifies the behavior for the <em>Jakarta Servlet</em> implementation of
 * <code>ExternalContext</code>. The <em>Portlet</em> implementation of <code>ExternalContext</code> is specified under
 * the revision of the <span style="text-decoration: underline;">Portlet Bridge Specification for JavaServer
 * Faces</span> JSR that corresponds to this version of the Jakarta Faces Specification. 
 * See the Preface of the Jakarta Faces Specification Document, for a reference.
 * </p>
 *
 * <p class="changed_added_2_0">
 * If a reference to an <code>ExternalContext</code> is obtained during application startup or shutdown time, any method
 * documented as "valid to call this method during application startup or shutdown" must be supported during application
 * startup or shutdown time. The result of calling a method during application startup or shutdown time that does not
 * have this designation is undefined.
 * </p>
 *
 * <p class="changed_added_2_3">
 * An ExternalContext can be injected into a CDI managed bean using
 * <code>@Inject ExternalContext externalContext;</code>
 * </p>
 */

public abstract class ExternalContext {

    @SuppressWarnings({ "UnusedDeclaration" })
    private ExternalContext defaultExternalContext;

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * String identifier for BASIC authentication.
     * </p>
     */
    public static final String BASIC_AUTH = "BASIC";

    /**
     * <p>
     * String identifier for CLIENT_CERT authentication.
     * </p>
     */
    public static final String CLIENT_CERT_AUTH = "CLIENT_CERT";

    /**
     * <p>
     * String identifier for DIGEST authentication.
     * </p>
     */
    public static final String DIGEST_AUTH = "DIGEST";

    /**
     * <p>
     * String identifier for FORM authentication.
     * </p>
     */
    public static final String FORM_AUTH = "FORM";

    // ---------------------------------------------------------- Public Methods

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Adds</span> the cookie represented by the arguments to the response.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be accomplished by calling the
     * <code>jakarta.servlet.http.HttpServletResponse</code> method <code>addCookie()</code>. The <code>Cookie</code>
     * argument must be constructed by passing the <code>name</code> and <code>value</code> parameters. If the
     * <code>properties</code> arugument is non-<code>null</code> and not empty, the <code>Cookie</code> instance must be
     * initialized as described below.
     * </p>
     *
     * <table border="1">
     * <caption>Cookie handling table</caption>
     *
     * <tr>
     *
     * <th>Key in "values" <code>Map</code> <span class="changed_added_4_0">(case sensitive)</span></th>
     *
     * <th>Expected type of value.</th>
     *
     * <th>Name of setter method on <code>Cookie</code> instance to be set with the value from the <code>Map</code>.</th>
     *
     * </tr>
     *
     * <tr>
     *
     * <td>comment</td>
     *
     * <td>String</td>
     *
     * <td>setComment</td>
     *
     * </tr>
     *
     * <tr>
     *
     * <td>domain</td>
     *
     * <td>String</td>
     *
     * <td>setDomain</td>
     *
     * </tr>
     *
     * <tr>
     *
     * <td>maxAge</td>
     *
     * <td>Integer</td>
     *
     * <td>setMaxAge</td>
     *
     * </tr>
     *
     * <tr>
     *
     * <td>secure</td>
     *
     * <td>Boolean</td>
     *
     * <td>setSecure</td>
     *
     * </tr>
     *
     * <tr>
     *
     * <td>path</td>
     *
     * <td>String</td>
     *
     * <td>setPath</td>
     *
     * </tr>
     *
     * <tr class="changed_added_2_2">
     *
     * <td>httpOnly</td>
     *
     * <td>Boolean</td>
     *
     * <td>setHttpOnly</td>
     *
     * </tr>
     *
     * <tr class="changed_added_4_0">
     *
     * <td><i>any other attribute</i> (e.g. SameSite)</td>
     *
     * <td>String</td>
     *
     * <td>setAttribute</td>
     *
     * </tr>
     *
     * </table>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @param name To be passed as the first argument to the <code>Cookie</code> constructor.
     *
     * @param value To be passed as the second argument to the <code>Cookie</code> constructor.
     *
     * @param properties A <code>Map</code> containg key/value pairs to be passed as arguments to the setter methods as
     * described above.
     *
     * @since 2.0
     */

    public void addResponseCookie(String name, String value, Map<String, Object> properties) {

        if (defaultExternalContext != null) {
            defaultExternalContext.addResponseCookie(name, value, properties);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p>
     * <span class="changed_modified_2_2">Dispatch</span> a request to the specified resource to create output for this
     * response.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be accomplished by calling the <code>jakarta.servlet.ServletContext</code> method
     * <code>getRequestDispatcher(path)</code>, and calling the <code>forward()</code> method on the resulting object.
     * </p>
     * <p class="changed_added_2_2">
     * If the call to <code>getRequestDisatcher(path)</code> returns <code>null</code>, send
     * a<code>ServletResponse SC_NOT_FOUND</code> error code.
     * </p>
     *
     * @param path Context relative path to the specified resource, which must start with a slash ("/") character
     *
     * @throws jakarta.faces.FacesException thrown if a <code>ServletException</code> occurs
     * @throws IOException if an input/output error occurs
     */
    public abstract void dispatch(String path) throws IOException;

    /**
     * <p>
     * <span class="changed_modified_2_2">Return</span> the input URL, after performing any rewriting needed to ensure that
     * it will correctly identify an addressable action in the current application.
     * </p>
     *
     * <p class="changed_added_2_2">
     * Encoding the {@link jakarta.faces.lifecycle.ClientWindow}
     * </p>
     *
     * <blockquote>
     *
     * <p class="changed_added_2_2">
     * Call
     * {@link jakarta.faces.lifecycle.ClientWindow#isClientWindowRenderModeEnabled(jakarta.faces.context.FacesContext) }. If
     * the result is <code>false</code> take no further action and return the rewritten URL. If the result is
     * <code>true</code>, call {@link #getClientWindow()}. If the result is non-<code>null</code>, call
     * {@link jakarta.faces.lifecycle.ClientWindow#getId()} and append the id to the query string of the URL, making the
     * necessary allowances for a pre-existing query string or no query-string.
     * </p>
     *
     * <p>
     * Call {@link jakarta.faces.lifecycle.ClientWindow#getQueryURLParameters}. If the result is non-{@code null}, for each
     * parameter in the map, unconditionally add that parameter to the URL.
     * </p>
     *
     * <p>
     * The name of the query string parameter is given by the value of the constant
     * {@link jakarta.faces.render.ResponseStateManager#CLIENT_WINDOW_URL_PARAM}.
     * </p>
     *
     * </blockquote>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the
     * <code>jakarta.servlet.http.HttpServletResponse</code> method <code>encodeURL(url)</code>.
     * </p>
     *
     * @param url The input URL to be encoded
     *
     * @return the encoded URL.
     *
     * @throws NullPointerException if <code>url</code> is <code>null</code>
     */
    public abstract String encodeActionURL(String url);

    /**
     * <p>
     * <span class="changed_modified_2_2">Return</span> the specified name, after prefixing it with a namespace that ensures
     * that it will be unique within the context of a particular page.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> The input value must be returned unchanged.
     * </p>
     *
     * @param name Name to be encoded
     *
     * @return the unique name prefixed with namespace.
     *
     * <!-- Removed the throws clause in 2.2 -->
     *
     */
    public abstract String encodeNamespace(String name);

    /**
     * <p>
     * Return the input URL, after performing any rewriting needed to ensure that it will correctly identify an addressable
     * resource in the current application.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the
     * <code>jakarta.servlet.http.HttpServletResponse</code> method <code>encodeURL(url)</code>.
     * </p>
     *
     * @param url The input URL to be encoded
     *
     * @return the encoded resource URL.
     *
     * @throws NullPointerException if <code>url</code> is <code>null</code>
     */
    // PENDING(craigmcc) - Currently identical to encodeActionURL()
    public abstract String encodeResourceURL(String url);

    /**
     * <p>
     * Return the websocket URL, after performing any rewriting needed to ensure that it will correctly identify an
     * addressable websocket in the current application.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must ensure that the input URL is prefixed with the correct websocket scheme, domain
     * and port and then encoded by {@link #encodeResourceURL(String)}.
     * </p>
     *
     * @param url The input URL to be encoded.
     *
     * @return the encoded websocket URL.
     *
     * @throws NullPointerException if <code>url</code> is <code>null</code>.
     *
     * @since 2.3
     */
    public abstract String encodeWebsocketURL(String url);

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> a mutable <code>Map</code> representing the application scope
     * attributes for the current application. The returned <code>Map</code> must implement the entire contract for a
     * modifiable map as described in the JavaDocs for <code>java.util.Map</code>. Modifications made in the
     * <code>Map</code> must cause the corresponding changes in the set of application scope attributes. Particularly the
     * <code>clear()</code>, <code>remove()</code>, <code>put()</code>, <code>putAll()</code>, and <code>get()</code>
     * operations must take the appropriate action on the underlying data structure.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called at startup or shutdown time, this
     * method returns a <code>Map</code> that is backed by the same container context instance (<code>ServletContext</code>
     * or <code>PortletContext</code>) as the one returned by calling <code>getApplicationMap()</code> on the
     * <code>ExternalContext</code> returned by the <code>FacesContext</code> during an actual request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the set of attributes available via the
     * <code>jakarta.servlet.ServletContext</code> methods <code>getAttribute()</code>, <code>getAttributeNames()</code>,
     * <code>removeAttribute()</code>, and <code>setAttribute()</code>.
     * </p>
     *
     * @return the map associated with the backed <code>ServletContext</code>.
     *
     */
    public abstract Map<String, Object> getApplicationMap();

    /**
     * <p>
     * Return the name of the authentication scheme used to authenticate the current user, if any; otherwise, return
     * <code>null</code>. For standard authentication schemes, the returned value will match one of the following constants:
     * <code>BASIC_AUTH</code>, <code>CLIENT_CERT_AUTH</code>, <code>DIGEST_AUTH</code>, or <code>FORM_AUTH</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.http.HttpServletRequest</code>
     * method <code>getAuthType()</code>.
     * </p>
     *
     * @return the authentication type.
     */
    public abstract String getAuthType();

    /**
     * <p class="changed_added_2_0">
     * Return the threadsafe {@link Flash} for this application. The default implementation will throw
     * <code>UnsupportedOperationException</code>. Compliant Jakarta Faces runtimes must provide an implementation of
     * this method.
     * </p>
     *
     * @return the <code>Flash</code> for this application.
     *
     * @since 2.0
     */

    public Flash getFlash() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getFlash();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * Returns the MIME type of the specified file or <code>null</code> if the MIME type is not known. The MIME type is
     * determined by the container.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this method calls through to the <code>getMimeType()</code> method on the same container context instance
     * (<code>ServletContext</code> or <code>PortletContext</code>) as the one used when calling <code>getMimeType()</code>
     * on the <code>ExternalContext</code> returned by the <code>FacesContext</code> during an actual request.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.ServletContext</code> method
     * <code>getMimeType()</code>.
     * </p>
     *
     * </div>
     *
     * @param file The file for which the mime type should be obtained.
     *
     * @return the MIME type of the file.
     *
     * @since 2.0
     */
    public String getMimeType(String file) {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getMimeType(file);
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> the application environment object instance for the current
     * appication.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this returns the same container context instance (<code>ServletContext</code> or
     * <code>PortletContext</code>) as the one returned when calling <code>getContext()</code> on the
     * <code>ExternalContext</code> returned by the <code>FacesContext</code> during an actual request.
     * </p>
     *
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the current application's <code>jakarta.servlet.ServletContext</code>
     * instance.
     * </p>
     *
     * @return the object of the <code>ServletContext</code>.
     *
     */
    public abstract Object getContext();

    /**
     *
     * <p class="changed_added_2_0">
     * Return the name of the container context for this application.
     * </p>
     *
     * <p class="changed_added_2_0">
     * Return the result of calling <code>getServletContextName()</code> on the <code>ServletContext</code> instance for
     * this application. It is valid to call this method during application startup or shutdown.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the name the <code>ServletContext</code>.
     *
     */
    public String getContextName() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getContextName();
        }

        throw new UnsupportedOperationException();

    }

    /**
     *
     * <p class="changed_added_2_2">
     * Return the name of the container context for this application.
     * </p>
     *
     * <p class="changed_added_2_2">
     * <em>Jakarta Servlet:</em> Return the result of calling <code>getContextPath()</code> on the
     * <code>ServletContext</code> instance for this application.
     * </p>
     *
     * <p>
     * It is valid to call this method during application startup or shutdown.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the context path of this application.
     *
     * @since 2.2
     */

    public String getApplicationContextPath() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getApplicationContextPath();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> the value of the specified application initialization parameter (if
     * any).
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the result of the <code>jakarta.servlet.ServletContext</code> method
     * <code>getInitParameter(name)</code>.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this method calls through to the actual container context to return the init parameter value.
     * </p>
     *
     * @param name Name of the requested initialization parameter
     *
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     *
     * @return the value of the specified parameter.
     *
     */
    public abstract String getInitParameter(String name);

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> an immutable <code>Map</code> whose keys are the set of application
     * initialization parameter names configured for this application, and whose values are the corresponding parameter
     * values. The returned <code>Map</code> must implement the entire contract for an unmodifiable map as described in the
     * JavaDocs for <code>java.util.Map</code>.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this method returns a <code>Map</code> that is backed by the same container context instance
     * (<code>ServletContext</code> or <code>PortletContext</code>) as the one returned by calling
     * <code>getInitParameterMap()</code> on the <code>ExternalContext</code> returned by the <code>FacesContext</code>
     * during an actual request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This result must be as if it were synthesized by calling the
     * <code>jakarta.servlet.ServletContext</code> method <code>getInitParameterNames</code>, and putting each configured
     * parameter name/value pair into the result.
     * </p>
     *
     * @return the init parameter map for this application.
     *
     */
    public abstract Map<String, String> getInitParameterMap();

    /**
     * <p>
     * Return the login name of the user making the current request if any; otherwise, return <code>null</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.http.HttpServletRequest</code>
     * method <code>getRemoteUser()</code>.
     * </p>
     *
     * @return the user name of the current request.
     *
     */
    public abstract String getRemoteUser();

    /**
     * <p>
     * Return the environment-specific object instance for the current request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the current request's <code>jakarta.servlet.http.HttpServletRequest</code>
     * instance.
     * </p>
     *
     * @return the instance of the current request.
     *
     */
    public abstract Object getRequest();

    /**
     * <p>
     * Set the environment-specific request to be returned by subsequent calls to {@link #getRequest}. This may be used to
     * install a wrapper for the request.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param request the request object to be set.
     *
     * @since 1.2
     */
    public void setRequest(Object request) {

        if (defaultExternalContext != null) {
            defaultExternalContext.setRequest(request);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Returns the name of the scheme used to make this request, for example, http, https, or ftp.
     * </p>
     *
     * <div class="changed_added_2_0">
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.ServletRequest</code> method
     * <code>getScheme()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @return the name of the scheme.
     *
     * @since 2.0
     */
    public String getRequestScheme() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getRequestScheme();
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_0">
     * Returns the host name of the server to which the request was sent.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.ServletRequest</code> method
     * <code>getServerName()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @return the host name of the server.
     *
     * @since 2.0
     */
    public String getRequestServerName() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getRequestServerName();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * Returns the port number to which the request was sent.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.ServletRequest</code> method
     * <code>getServerPort()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @return the port number to which the request was sent.
     *
     * @since 2.0
     */
    public int getRequestServerPort() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getRequestServerPort();
        }

        throw new UnsupportedOperationException();
    }

    /**
     *
     * <p>
     * Overrides the name of the character encoding used in the body of this request.
     * </p>
     *
     * <p>
     * Calling this method after the request has been accessed will have no no effect, unless a <code>Reader</code> or
     * <code>Stream</code> has been obtained from the request, in which case an <code>IllegalStateException</code> is
     * thrown.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must call through to the <code>jakarta.servlet.ServletRequest</code> method
     * <code>setCharacterEncoding()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param encoding the encoding name to be set.
     *
     * @throws java.io.UnsupportedEncodingException if this is not a valid encoding
     *
     * @since 1.2
     *
     */
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {

        if (defaultExternalContext != null) {
            defaultExternalContext.setRequestCharacterEncoding(encoding);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Returns a String containing the real path for a given virtual path.
     * </p>
     *
     * <p class="changed_added_2_3">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this method calls through to the <code>getRealPath()</code> method on the same container context instance
     * (<code>ServletContext</code> or <code>PortletContext</code>) as the one used when calling <code>getRealPath()</code>
     * on the <code>ExternalContext</code> returned by the <code>FacesContext</code> during an actual request.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.ServletContext</code> method
     * <code>getRealPath()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @param path The context of the requested initialization parameter
     *
     * @return the real path for the specified virtual path.
     *
     * @since 2.0
     */
    public String getRealPath(String path) {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getRealPath(path);
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p>
     * Return the portion of the request URI that identifies the web application context for this request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.http.HttpServletRequest</code>
     * method <code>getContextPath()</code>.
     * </p>
     *
     * @return the context path for this request.
     */
    public abstract String getRequestContextPath();

    /**
     * <p>
     * Return an immutable <code>Map</code> whose keys are the set of cookie names included in the current request, and
     * whose values (of type <code>jakarta.servlet.http.Cookie</code>) are the first (or only) cookie for each cookie name
     * returned by the underlying request. The returned <code>Map</code> must implement the entire contract for an
     * unmodifiable map as described in the JavaDocs for <code>java.util.Map</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.http.HttpServletRequest</code>
     * method <code>getCookies()</code>, unless <code>null</code> was returned, in which case this must be a zero-length
     * array.
     * </p>
     *
     * @return the cookie map in the current request.
     *
     */
    public abstract Map<String, Object> getRequestCookieMap();

    /**
     * <p>
     * Return an immutable <code>Map</code> whose keys are the set of request header names included in the current request,
     * and whose values (of type String) are the first (or only) value for each header name returned by the underlying
     * request. The returned <code>Map</code> must implement the entire contract for an unmodifiable map as described in the
     * JavaDocs for <code>java.util.Map</code>. In addition, key comparisons must be performed in a case insensitive manner.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the set of headers available via the
     * <code>jakarta.servlet.http.HttpServletRequest</code> methods <code>getHeader()</code> and
     * <code>getHeaderNames()</code>.
     * </p>
     *
     * @return the header map in the current request.
     *
     */
    public abstract Map<String, String> getRequestHeaderMap();

    /**
     * <p>
     * Return an immutable <code>Map</code> whose keys are the set of request header names included in the current request,
     * and whose values (of type String[]) are all of the value for each header name returned by the underlying request. The
     * returned <code>Map</code> must implement the entire contract for an unmodifiable map as described in the JavaDocs for
     * <code>java.util.Map</code>. In addition, key comparisons must be performed in a case insensitive manner.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the set of headers available via the
     * <code>jakarta.servlet.http.HttpServletRequest</code> methods <code>getHeaders()</code> and
     * <code>getHeaderNames()</code>.
     * </p>
     *
     * @return the header values map in the current request.
     */
    public abstract Map<String, String[]> getRequestHeaderValuesMap();

    /**
     * <p>
     * Return the preferred <code>Locale</code> in which the client will accept content.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.ServletRequest</code> method
     * <code>getLocale()</code>.
     * </p>
     *
     * @return the <code>Locale</code> of the current request.
     */
    public abstract Locale getRequestLocale();

    /**
     * <p>
     * Return an <code>Iterator</code> over the preferred <code>Locale</code>s specified in the request, in decreasing order
     * of preference.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be an <code>Iterator</code> over the values returned by the
     * <code>jakarta.servlet.ServletRequest</code> method <code>getLocales()</code>.
     * </p>
     *
     * @return the <code>Iterator</code> of <code>Locale</code>s of the current request.
     *
     */
    public abstract Iterator<Locale> getRequestLocales();

    /**
     * <p>
     * Return a mutable <code>Map</code> representing the request scope attributes for the current application. The returned
     * <code>Map</code> must implement the entire contract for a modifiable map as described in the JavaDocs for
     * <code>java.util.Map</code>. Modifications made in the <code>Map</code> must cause the corresponding changes in the
     * set of request scope attributes. Particularly the <code>clear()</code>, <code>remove()</code>, <code>put()</code>,
     * <code>putAll()</code>, and <code>get()</code> operations must take the appropriate action on the underlying data
     * structure.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the set of attributes available via the
     * <code>jakarta.servlet.ServletRequest</code> methods <code>getAttribute()</code>, <code>getAttributeNames()</code>,
     * <code>removeAttribute()</code>, and <code>setAttribute()</code>.
     * </p>
     *
     * @return the map including the attributes of the current request.
     *
     */
    public abstract Map<String, Object> getRequestMap();

    /**
     * <p>
     * Return an immutable <code>Map</code> whose keys are the set of request parameters names included in the current
     * request, and whose values (of type String) are the first (or only) value for each parameter name returned by the
     * underlying request. The returned <code>Map</code> must implement the entire contract for an unmodifiable map as
     * described in the JavaDocs for <code>java.util.Map</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the set of parameters available via the
     * <code>jakarta.servlet.ServletRequest</code> methods <code>getParameter()</code> and <code>getParameterNames()</code>.
     * </p>
     *
     * @return the map for the current request parameters.
     *
     */
    public abstract Map<String, String> getRequestParameterMap();

    /**
     * <p>
     * Return an <code>Iterator</code> over the names of all request parameters included in the current request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be an <code>Iterator</code> over the values returned by the
     * <code>jakarta.servlet.ServletRequest</code> method <code>getParameterNames()</code>.
     * </p>
     *
     * @return the <code>Iterator</code> for the names of the current request parameters.
     *
     */
    public abstract Iterator<String> getRequestParameterNames();

    /**
     * <p>
     * Return an immutable <code>Map</code> whose keys are the set of request parameters names included in the current
     * request, and whose values (of type String[]) are all of the values for each parameter name returned by the underlying
     * request. The returned <code>Map</code> must implement the entire contract for an unmodifiable map as described in the
     * JavaDocs for <code>java.util.Map</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the set of parameters available via the
     * <code>jakarta.servlet.ServletRequest</code> methods <code>getParameterValues()</code> and
     * <code>getParameterNames()</code>.
     * </p>
     *
     * @return the map for the parameter values of the current request.
     *
     */
    public abstract Map<String, String[]> getRequestParameterValuesMap();

    /**
     * <p>
     * Return the extra path information (if any) included in the request URI; otherwise, return <code>null</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.http.HttpServletRequest</code>
     * method <code>getPathInfo()</code>.
     * </p>
     *
     * @return the path information of the current request.
     *
     */
    public abstract String getRequestPathInfo();

    /**
     * <p>
     * Return the Jakarta Servlet path information (if any) included in the request URI; otherwise, return
     * <code>null</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.http.HttpServletRequest</code>
     * method <code>getServletPath()</code>.
     * </p>
     *
     * @return the Jakarta Servlet path information of the current request.
     */
    public abstract String getRequestServletPath();

    /**
     *
     * <p>
     * Return the character encoding currently being used to interpret this request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must return the value returned by the <code>jakarta.servlet.ServletRequest</code>
     * method <code>getCharacterEncoding()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the character encoding currently being used.
     *
     * @since 1.2
     *
     */
    public String getRequestCharacterEncoding() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getRequestCharacterEncoding();
        }
        throw new UnsupportedOperationException();

    }

    /**
     *
     * <p>
     * Return the MIME Content-Type for this request. If not available, return <code>null</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must return the value returned by the <code>jakarta.servlet.ServletRequest</code>
     * method <code>getContentType()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the Content-Type for this request.
     *
     * @since 1.2
     */
    public String getRequestContentType() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getRequestContentType();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * Return the result of calling <code>getContentLenth()</code> on the <code>ServletRequest</code> instance for this
     * request.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the content length of the current request.
     *
     * @since 2.0
     */

    public int getRequestContentLength() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getRequestContentLength();
        }

        throw new UnsupportedOperationException();

    }

    /**
     *
     * <p>
     * <span class="changed_modified_2_3">Returns</span> the name of the character encoding (MIME charset) used for the body
     * sent in this response.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must return the value returned by the <code>jakarta.servlet.ServletResponse</code>
     * method <code>getCharacterEncoding()</code>.
     * </p>
     *
     * <p class="changed_added_2_3">
     * Portlet: if this method is called during a lifecycle phase other than RENDER_RESPONSE, this must return
     * <code>null</code>. If called during RENDER_RESPONSE, return the response encoding of the portlet response.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the name of the character encoding.
     *
     * @since 1.2
     */
    public String getResponseCharacterEncoding() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getResponseCharacterEncoding();
        }

        throw new UnsupportedOperationException();

    }

    /**
     *
     * <p>
     * Return the MIME Content-Type for this response. If not available, return <code>null</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must return the value returned by the <code>jakarta.servlet.ServletResponse</code>
     * method <code>getContentType()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the MIME Content-Type for this response.
     *
     * @since 1.2
     */
    public String getResponseContentType() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getResponseContentType();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> a <code>URL</code> for the application resource mapped to the
     * specified path, if it exists; otherwise, return <code>null</code>.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this method calls through to the <code>getResource()</code> method on the same container context instance
     * (<code>ServletContext</code> or <code>PortletContext</code>) as the one used when calling <code>getResource()</code>
     * on the <code>ExternalContext</code> returned by the <code>FacesContext</code> during an actual request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.ServletContext</code> method
     * <code>getResource(path)</code>.
     * </p>
     *
     * @param path The path to the requested resource, which must start with a slash ("/" character
     *
     * @return the URL of the resource.
     *
     * @throws MalformedURLException if the specified path is not in the correct form
     * @throws NullPointerException if <code>path</code> is <code>null</code>
     */
    public abstract URL getResource(String path) throws MalformedURLException;

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> an <code>InputStream</code> for an application resource mapped to
     * the specified path, if it exists; otherwise, return <code>null</code>.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this method calls through to the <code>getResourceAsStream()</code> method on the same container context
     * instance (<code>ServletContext</code> or <code>PortletContext</code>) as the one used when calling
     * <code>getResourceAsStream()</code> on the <code>ExternalContext</code> returned by the <code>FacesContext</code>
     * during an actual request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.ServletContext</code> method
     * <code>getResourceAsStream(path)</code>.
     * </p>
     *
     * @param path The path to the requested resource, which must start with a slash ("/" character
     *
     * @return the <code>InputStream</code> for the application resource.
     *
     * @throws NullPointerException if <code>path</code> is <code>null</code>
     */
    public abstract InputStream getResourceAsStream(String path);

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> the <code>Set</code> of resource paths for all application resources
     * whose resource path starts with the specified argument.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this method calls through to the <code>getResourcePaths()</code> method on the same container context
     * instance (<code>ServletContext</code> or <code>PortletContext</code>) as the one used when calling
     * <code>getResourcePaths()</code> on the <code>ExternalContext</code> returned by the <code>FacesContext</code> during
     * an actual request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.ServletContext</code> method
     * <code>getResourcePaths(path).</code>
     * </p>
     *
     * @param path Partial path used to match resources, which must start with a slash ("/") character
     *
     * @return the <code>Set</code> of resource paths for the application resources.
     *
     * @throws NullPointerException if <code>path</code> is <code>null</code>
     */
    public abstract Set<String> getResourcePaths(String path);

    /**
     * <p>
     * Return the environment-specific object instance for the current response.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This is the current request's <code>jakarta.servlet.http.HttpServletResponse</code>
     * instance.
     * </p>
     *
     * @return the instance of the current <code>jakarta.servlet.http.HttpServletResponse</code>.
     */
    public abstract Object getResponse();

    /**
     * <p>
     * Set the environment-specific response to be returned by subsequent calls to {@link #getResponse}. This may be used to
     * install a wrapper for the response.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param response the response instance to be set.
     *
     * @since 1.2
     */
    public void setResponse(Object response) {

        if (defaultExternalContext != null) {
            defaultExternalContext.setResponse(response);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Returns an <code>OutputStream</code> suitable for writing binary data to the user-agent.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must return the value returned by the <code>jakarta.servlet.ServletResponse</code>
     * method <code>getOutputStream()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @return the <code>OutputStream</code> for the current response.
     *
     * @throws IOException any IO related exception.
     *
     * @since 2.0
     */
    public OutputStream getResponseOutputStream() throws IOException {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getResponseOutputStream();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * Returns a <code>Writer</code> suitable for writing character data to the user-agent.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must return the value returned by the
     * {@link jakarta.servlet.ServletResponse#getWriter}.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @return the <code>Writer</code> for the current response.
     *
     * @throws IOException any IO related exception.
     *
     * @since 2.0
     */
    public Writer getResponseOutputWriter() throws IOException {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getResponseOutputWriter();
        }

        throw new UnsupportedOperationException();

    }

    /**
     *
     * <p>
     * Sets the character encoding (MIME charset) of the response being sent to the client, for example, to UTF-8.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must call through to the <code>jakarta.servlet.ServletResponse</code> method
     * <code>setCharacterEncoding()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param encoding the character encoding to be sent by the current response.
     *
     * @since 1.2
     *
     */
    public void setResponseCharacterEncoding(String encoding) {

        if (defaultExternalContext != null) {
            defaultExternalContext.setResponseCharacterEncoding(encoding);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Sets the content type of the response being sent to the client, if the response has not been committed yet.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must call <code>setContentType()</code> on the underlying
     * <code>jakarta.servlet.ServletResponse</code> instance.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @param contentType The content type to be set as the contentType of the response.
     *
     * @since 2.0
     */
    public void setResponseContentType(String contentType) {

        if (defaultExternalContext != null) {
            defaultExternalContext.setResponseContentType(contentType);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p>
     * If the <code>create</code> parameter is <code>true</code>, create (if necessary) and return a session instance
     * associated with the current request. If the <code>create</code> parameter is <code>false</code> return any existing
     * session instance associated with the current request, or return <code>null</code> if there is no such session.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must return the result of calling <code>getSession(create)</code> on the underlying
     * <code>jakarta.servlet.http.HttpServletRequest</code> instance.
     * </p>
     *
     * @param create Flag indicating whether or not a new session should be created if there is no session associated with
     * the current request
     *
     * @return the session object of the current request.
     */
    public abstract Object getSession(boolean create);

    /**
     * <p class="changed_added_2_2">
     * Return the id of the current session or the empty string if no session has been created and the <code>create</code>
     * parameter is <code>false</code>.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * <em>Jakarta Servlet:</em> If <code>create</code> is true, obtain a reference to the <code>HttpSession</code> for the
     * current request (creating the session if necessary) and return its id. If <code>create</code> is <code>false</code>,
     * obtain a reference to the current session, if one exists, and return its id. If no session exists, return the empty
     * string.
     * </p>
     *
     * </div>
     *
     * @since 2.2
     *
     * @param create Flag indicating whether or not a new session should be created if there is no session associated with
     * the current request
     *
     * @return the session id for the current request.
     */
    public String getSessionId(boolean create) {
        String result = "";
        if (defaultExternalContext != null) {
            result = defaultExternalContext.getSessionId(create);
        } else {
            throw new UnsupportedOperationException();
        }
        return result;
    }

    /**
     * <p class="changed_added_2_1">
     * Returns the maximum time interval, in seconds, that the Jakarta Servlet container will keep this session open between
     * client accesses. After this interval, the Jakarta Servlet container will invalidate the session. The maximum time
     * interval can be set with the {@link #setSessionMaxInactiveInterval} method.
     * </p>
     *
     * <p class="changed_added_2_1">
     * A return value of zero or less indicates that the session will never timeout.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must return the result of calling <code>getMaxInactiveInterval</code> on the
     * underlying <code class="changed_modified_2_3">jakarta.servlet.http.HttpSession</code> instance.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the session maximum inactive interval.
     * @since 2.1
     */
    public int getSessionMaxInactiveInterval() {
        int result = 0;
        if (defaultExternalContext != null) {
            result = defaultExternalContext.getSessionMaxInactiveInterval();
        } else {
            throw new UnsupportedOperationException();
        }

        return result;
    }

    /**
     * <p>
     * Return a mutable <code>Map</code> representing the session scope attributes for the current application. The returned
     * <code>Map</code> must implement the entire contract for a modifiable map as described in the JavaDocs for
     * <code>java.util.Map</code>. Modifications made in the <code>Map</code> must cause the corresponding changes in the
     * set of session scope attributes. Particularly the <code>clear()</code>, <code>remove()</code>, <code>put()</code>,
     * and <code>get()</code> operations must take the appropriate action on the underlying data structure. Accessing
     * attributes via this <code>Map</code> must cause the creation of a session associated with the current request, if
     * such a session does not already exist.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the set of attributes available via the
     * <code>jakarta.servlet.http.HttpSession</code> methods <code>getAttribute()</code>, <code>getAttributeNames()</code>,
     * <code>removeAttribute()</code>, and <code>setAttribute()</code>.
     * </p>
     *
     * @return the session map for the current application.
     *
     */
    public abstract Map<String, Object> getSessionMap();

    /**
     * <p>
     * Return the <code>Principal</code> object containing the name of the current authenticated user, if any; otherwise,
     * return <code>null</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.http.HttpServletRequest</code>
     * method <code>getUserPrincipal()</code>.
     * </p>
     *
     * @return the <code>Principal</code> object.
     */
    public abstract Principal getUserPrincipal();

    /**
     * <p class="changed_added_2_2">
     * Return the {@link ClientWindow} set in a preceding call to {@link #setClientWindow}, or <code>null</code> if no such
     * call has been made.
     * </p>
     *
     * @since 2.2
     *
     * @return the instance of the <code>ClientWindow</code>.
     */
    public ClientWindow getClientWindow() {
        if (defaultExternalContext != null) {
            return defaultExternalContext.getClientWindow();
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Invalidates this session then unbinds any objects bound to it.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.http.HttpSession</code> method
     * <code>invalidate()</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @since 2.0
     */
    public void invalidateSession() {

        if (defaultExternalContext != null) {
            defaultExternalContext.invalidateSession();
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p>
     * Return <code>true</code> if the currently authenticated user is included in the specified role. Otherwise, return
     * <code>false</code>.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be the value returned by the <code>jakarta.servlet.http.HttpServletRequest</code>
     * method <code>isUserInRole(role)</code>.
     * </p>
     *
     * @param role Logical role name to be checked
     *
     * @return the flag indicating whether the current user is in the specified role.
     *
     * @throws NullPointerException if <code>role</code> is <code>null</code>
     */
    public abstract boolean isUserInRole(String role);

    /**
     * <p>
     * <span class="changed_modified_2_0">Log</span> the specified message to the application object.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this calls the <code>log()</code> method on the same container context instance
     * (<code>ServletContext</code> or <code>PortletContext</code>) as the one used during a call to <code>log()</code> on
     * the <code>ExternalContext</code> returned by the <code>FacesContext</code> during an actual request.
     * </p>
     *
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.ServletContext</code> method
     * <code>log(String)</code>.
     * </p>
     *
     * @param message Message to be logged
     *
     * @throws NullPointerException if <code>message</code> is <code>null</code>
     */
    public abstract void log(String message);

    /**
     * <p>
     * <span class="changed_modified_2_0">Log</span> the specified message and exception to the application object.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this calls the <code>log()</code> method on the same container context instance
     * (<code>ServletContext</code> or <code>PortletContext</code>) as the one used when calling <code>log()</code> on the
     * <code>ExternalContext</code> returned by the <code>FacesContext</code> during an actual request.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.ServletContext</code> method
     * <code>log(String,Throwable)</code>.
     * </p>
     *
     * @param message Message to be logged
     * @param exception Exception to be logged
     *
     * @throws NullPointerException if <code>message</code> or <code>exception</code> is <code>null</code>
     */
    public abstract void log(String message, Throwable exception);

    /**
     * <p>
     * <span class="changed_modified_2_0">Redirect</span> a request to the specified URL, and cause the
     * <code>responseComplete()</code> method to be called on the {@link FacesContext} instance for the current request.
     * </p>
     *
     * <p class="changed_added_2_0">
     * The implementation must determine if the request is an <code>Ajax</code> request by obtaining a
     * {@link PartialViewContext} instance from the {@link FacesContext} and calling
     * {@link PartialViewContext#isAjaxRequest()}.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> <span class="changed_modified_2_0">For non <code>Ajax</code> requests, this must be
     * accomplished by calling the <code>jakarta.servlet.http.HttpServletResponse</code> method
     * <code>sendRedirect()</code>.</span> <div class="changed_added_2_0"> For Ajax requests, the implementation must:
     * </div>
     * <ul>
     * <li>Get a {@link PartialResponseWriter} instance from the {@link FacesContext}.</li>
     * <li>Call {@link #setResponseContentType} with <code>text/xml</code></li>
     * <li>Call {@link #setResponseCharacterEncoding} with <code>UTF-8</code></li>
     * <li>Call {@link #addResponseHeader} with <code>Cache-Control</code>, <code>no-cache</code></li>
     * <li>Call {@link PartialResponseWriter#startDocument}</li>
     * <li>Call {@link PartialResponseWriter#redirect} with the <code>url</code> argument.</li>
     * <li>Call {@link PartialResponseWriter#endDocument}</li>
     * </ul>
     *
     * @param url Absolute URL to which the client should be redirected
     *
     * @throws IllegalArgumentException if the specified url is relative
     * @throws IllegalStateException if, in a portlet environment, the current response object is a
     * <code>RenderResponse</code> instead of an <code>ActionResponse</code>
     * @throws IllegalStateException if, in a Jakarta Servlet environment, the current response has already been committed
     * @throws IOException if an input/output error occurs
     */
    public abstract void redirect(String url) throws IOException;

    /**
     * <p class="changed_added_2_0">
     * Set the response header with the given name and value.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em>This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>setHeader</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param name The name of the response header.
     * @param value The value of the response header.
     *
     * @since 2.0
     */
    public void setResponseHeader(String name, String value) {

        if (defaultExternalContext != null) {
            defaultExternalContext.setResponseHeader(name, value);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Add the given name and value to the response header.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em>This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>addHeader</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param name The name of the response header.
     * @param value The value of the response header.
     *
     * @since 2.0
     */
    public void addResponseHeader(String name, String value) {

        if (defaultExternalContext != null) {
            defaultExternalContext.addResponseHeader(name, value);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Set the buffer size for the current response.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>setBufferSize</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param size the new buffer size
     *
     * @since 2.0
     */
    public void setResponseBufferSize(int size) {

        if (defaultExternalContext != null) {
            defaultExternalContext.setResponseBufferSize(size);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Return the buffer size for the current response.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>getBufferSize</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @since 2.0
     *
     * @return the buffer size of the response.
     */
    public int getResponseBufferSize() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.getResponseBufferSize();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * Check if the current response has been committed.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>isCommitted</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @since 2.0
     *
     * @return the flag indicating whether the current response has been committed.
     */
    public boolean isResponseCommitted() {

        if (defaultExternalContext != null) {
            return defaultExternalContext.isResponseCommitted();
        }
        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * Resets the current response.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>reset</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @since 2.0
     */
    public void responseReset() {

        if (defaultExternalContext != null) {
            defaultExternalContext.responseReset();
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Sends an HTTP status code with message.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>sendError</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param statusCode an HTTP status code
     * @param message an option message to detail the cause of the code
     *
     * @since 2.0
     *
     * @throws IOException any IO related exceptions.
     */
    public void responseSendError(int statusCode, String message) throws IOException {

        if (defaultExternalContext != null) {
            defaultExternalContext.responseSendError(statusCode, message);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Sets the HTTP status code for the response.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>setStatus</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param statusCode an HTTP status code
     *
     * @since 2.0
     */
    public void setResponseStatus(int statusCode) {

        if (defaultExternalContext != null) {
            defaultExternalContext.setResponseStatus(statusCode);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_1">
     * Specifies the time, in seconds, between client requests before the Jakarta Servlet container will invalidate this
     * session.
     * </p>
     *
     * <p class="changed_added_2_1">
     * An interval value of zero or less indicates that the session should never timeout.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must call <code>setMaxInactiveInterval</code> on the underlying
     * <code>jakarta.servlet.http.HttpServletRequest</code> instance.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param interval the value to be set.
     *
     * @since 2.1
     */
    public void setSessionMaxInactiveInterval(int interval) {
        if (defaultExternalContext != null) {
            defaultExternalContext.setSessionMaxInactiveInterval(interval);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_2">
     * Associate this instance with a {@link ClientWindow}.
     * </p>
     *
     * @param window the window with which this instance is associated.
     *
     * @since 2.2
     */

    public void setClientWindow(ClientWindow window) {
        if (defaultExternalContext != null) {
            defaultExternalContext.setClientWindow(window);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Flushes the buffered response content to the client.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>flushBuffer</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @since 2.0
     *
     * @throws IOException any IO related exception.
     */
    public void responseFlushBuffer() throws IOException {

        if (defaultExternalContext != null) {
            defaultExternalContext.responseFlushBuffer();
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Set the content length of the response <span class="changed_modified_4_1">as an int</span>
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>setContentLength</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param length the value to be set.
     *
     * @since 2.0
     */
    public void setResponseContentLength(int length) {

        if (defaultExternalContext != null) {
            defaultExternalContext.setResponseContentLength(length);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_4_1">
     * Set the content length of the response as a long.
     * </p>
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must be performed by calling the <code>jakarta.servlet.http.HttpServletResponse</code>
     * <code>setContentLengthLong</code> method.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param length the value to be set.
     *
     * @since 4.1
     */
    public void setResponseContentLengthLong(long length) {

        if (defaultExternalContext != null) {
            defaultExternalContext.setResponseContentLengthLong(length);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">The</span> purpose of this method is to generate a query string from the
     * collection of Parameter objects provided by the parameters argument and append that query string to the baseUrl. This
     * method must be able to encode the parameters to a baseUrl that may or may not have existing query parameters. The
     * parameter values should be encoded appropriately for the environment so that the resulting URL can be used as the
     * target of a link (e.g., in an href attribute) in a Jakarta Faces response. It's possible for an
     * ExternalContext implementation to override this method in any way that would make the URL bookmarkable in that
     * environment.
     * </p>
     *
     * <p class="changed_added_2_2">
     * See {@link #encodeActionURL(java.lang.String)} for the required specification of how to encode the
     * {@link jakarta.faces.lifecycle.ClientWindow}.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param baseUrl The base URL onto which the query string generated by this method will be appended. The URL may
     * contain query parameters.
     * @param parameters The collection of Parameter objects, representing name=value pairs that are used to produce a query
     * string
     *
     * @return the result of encoding.
     *
     * @since 2.0
     */
    public String encodeBookmarkableURL(String baseUrl, Map<String, List<String>> parameters) {

        if (defaultExternalContext != null) {
            return defaultExternalContext.encodeBookmarkableURL(baseUrl, parameters);
        }
        throw new UnsupportedOperationException();

    }

    /**
     * <span class="changed_modified_2_2">The</span> purpose of this method is to generate a query string from the
     * collection of Parameter objects provided by the parameters argument and append that query string to the baseUrl. This
     * method must be able to encode the parameters to a baseUrl that may or may not have existing query parameters. The
     * parameter values should be encoded appropriately for the environment so that the resulting URL can be used as the
     * target of a redirect. It's possible for an ExternalContext implementation to override this method to accomodate the
     * definition of redirect for that environment.
     *
     * <p class="changed_added_2_2">
     * See {@link #encodeActionURL(java.lang.String)} for the required specification of how to encode the
     * {@link jakarta.faces.lifecycle.ClientWindow}.
     * </p>
     *
     * @param baseUrl The base URL onto which the query string generated by this method will be appended. The URL may
     * contain query parameters.
     * @param parameters The collection of Parameter objects, representing name=value pairs that are used to produce a query
     * string
     *
     * @return the result of encoding.
     * @since 2.0
     */
    public String encodeRedirectURL(String baseUrl, Map<String, List<String>> parameters) {

        if (defaultExternalContext != null) {
            return defaultExternalContext.encodeRedirectURL(baseUrl, parameters);
        }
        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Return</span> the input URL, after performing any rewriting needed to ensure that
     * it can be used in a partial page submission (ajax request) to correctly identify an addressable action in the current
     * application.
     * </p>
     *
     * <p class="changed_added_2_2">
     * See {@link #encodeActionURL(java.lang.String)} for the required specification of how to encode the
     * {@link jakarta.faces.lifecycle.ClientWindow}.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * <em>Jakarta Servlet:</em>Returns the same encoded URL as the {@link #encodeActionURL(String url)} method.
     * </p>
     *
     * <p>
     * <em>Portlet:</em>Returns an encoded URL that, upon HTTP POST, will invoke the RESOURCE_PHASE of the portlet
     * lifecycle.
     * </p>
     *
     * </div>
     *
     * @param url The input URL to be encoded
     *
     * @return the encoded URL.
     *
     * @throws NullPointerException if <code>url</code> is <code>null</code>
     *
     * @since 2.0
     */
    public String encodePartialActionURL(String url) {
        if (defaultExternalContext != null) {
            return defaultExternalContext.encodePartialActionURL(url);
        }
        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_1">
     * Returns a boolean indicating whether this request was made using a secure channel, such as HTTPS.
     *
     *
     * <p>
     * <em>Jakarta Servlet:</em> This must return the result of calling <code>isSecure</code> on the underlying
     * <code>jakarta.servlet.http.HttpServletRequest</code> instance.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the boolean indicating whether this request is secured.
     *
     * @since 2.1
     */
    public boolean isSecure() {
        if (defaultExternalContext != null) {
            return defaultExternalContext.isSecure();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p>
     * <span class="changed_added_4_0">Release</span> any resources associated with this <code>ExternalContext</code>
     * instance. This method is called during during destruction of the associated <code>FacesContext</code>.
     * </p>
     *
     * @since 4.0
     */
    public abstract void release(); 
}
