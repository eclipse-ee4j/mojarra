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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletRegistration;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_1 changed_modified_2_2 changed_modified_2_3">An</span> instance of
 * <code>Resource</code> is a Java object representation of the artifact that is served up in response to a <i>resource
 * request</i> from the client. Instances of <code>Resource</code> are normally created and initialized via calls to
 * {@link ResourceHandler#createResource}. See the documentation for {@link ResourceHandler} for more information.
 * </p>
 *
 * <div class="changed_added_2_0"> </div>
 *
 * @since 2.0
 */
public abstract class Resource extends ViewResource {

    /**
     * <p class="changed_added_2_0">
     * This constant is used as the key in the component attribute map of a composite component to associate the component
     * with its <code>Resource</code> instance. The value for this key is the actual <code>Resource</code> instance.
     * </p>
     *
     */
    public static final String COMPONENT_RESOURCE_KEY = "jakarta.faces.application.Resource.ComponentResource";

    private String contentType;
    private String libraryName;
    private String resourceName;

    // ---------------------------------------------------------- Public Methods

    /**
     * <p class="changed_added_2_0">
     * Return the MIME content-type for this resource.
     * </p>
     *
     * @return the MIME content-type for this resource.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     *
     * <p class="changed_added_2_0">
     * Set the MIME content-type for this resource. The default implementation performs no validation on the argument.
     * </p>
     *
     * @param contentType the MIME content-type for this resource. The default implementation must accept <code>null</code>
     * as a parameter.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the libraryName for this resource. May be <code>null</code>. The libraryName for a resource is an optional
     * String that indicates membership in a "resource library". All resources with the same libraryName belong to the same
     * "resource library". The "resource library" concept allows disambiguating resources that have the same resourceName.
     * See {@link ResourceHandler} for more information.
     * </p>
     *
     * @return Return the libraryName for this resource. May be <code>null</code>.
     */
    public String getLibraryName() {
        return libraryName;
    }

    /**
     * <p class="changed_added_2_0">
     * Set the libraryName for this resource.
     * </p>
     *
     * @param libraryName the libraryName for this resource. The default implementation must accept <code>null</code> for
     * the <em>libraryName</em>.
     */
    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the resourceName for this resource. Will never be null. All <code>Resource</code> instances must have a
     * resourceName.
     * </p>
     *
     * @return Return the resourceName for this resource. Will never be null.
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * <p class="changed_added_2_0">
     * Set the resourceName for this resource.
     * </p>
     *
     * @param resourceName a non-null String.
     *
     * @throws NullPointerException if argument <code>resourceName</code> is null.
     */
    public void setResourceName(String resourceName) {

        if (resourceName == null) {
            throw new NullPointerException("All resources must have a non-null resourceName.");
        }

        this.resourceName = resourceName;
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_1">If</span> the current request is a resource request, (that is,
     * {@link ResourceHandler#isResourceRequest} returns <code>true</code>), return an <code>InputStream</code> containing
     * the bytes of the resource. Otherwise, throw an <code>IOException</code>.
     * </p>
     *
     * @return an <code>InputStream</code> containing the bytes of the resource.
     *
     * <p class="changed_modified_2_1">
     * Any Jakarta Expression Language expressions present in the resource must be evaluated before serving the bytes of the
     * resource. Note that due to browser and server caching, Jakarta Expression Language expressions in a resource file
     * will generally only be evaluated once, when the resource is first served up. Therefore, using Jakarta Expression
     * Language expressions that refer to per-request data is not advisable since this data can become stale.
     * </p>
     *
     * @throws IOException if the current request is not a resource request.
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * <p class="changed_added_2_0">
     * Returns a mutable <code>Map&lt;String, String&gt;</code> whose entries will be sent as response headers during
     * {@link ResourceHandler#handleResourceRequest}. The entries in this map must not persist beyond the scope of a single
     * request. Any modifications made to the map after the resource has been served will be ignored by the run-time.
     * </p>
     *
     * @return a mutable <code>Map&lt;String, String&gt;</code> of headers that will be included with the response.
     */
    public abstract Map<String, String> getResponseHeaders();

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2 changed_modified_2_3">Return</span> a path to this resource such that, when the
     * browser resolves it against the base URI for the view that includes the resource, and issues a GET request to the
     * resultant fully qualified URL, the bytes of the resource are returned in response.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * The default implementation must implement the following algorithm. For discussion, the return result from this method
     * will be called <em>result</em>.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * Get the context-root for this web application, not ending in slash. For discussion this will be called
     * <em>contextRoot</em>.
     * </p>
     * </li>
     *
     * <li>
     * <p class="changed_modified_2_3">
     * Discover if the <code>FacesServlet</code> is prefix (path) mapped, extension mapped, or exact mapped (as defined by
     * Servlet.12.2.) and the value of the mapping (including the leading '.' in the case of extension mapping). For
     * discussion, this will be <em>facesServletMapping</em>.
     * </p>
     *
     * <div class="changed_added_2_3">
     * <p>
     * If exact mapped, <em>result</em> must be the following if and only if the FacesServlet is mapped to the exact URL
     * pattern {@link ResourceHandler#RESOURCE_IDENTIFIER} + {@link #getResourceName}
     * </p>
     *
     * <blockquote>
     * <p>
     * <code>result = <em>contextRoot</em> + {@link
     * ResourceHandler#RESOURCE_IDENTIFIER} + {@link #getResourceName}</code>
     * </p>
     * </blockquote>
     *
     * <p>
     * If exact mapped, and the FacesServlet is <em>not</em> mapped to the exact URL pattern
     * {@link ResourceHandler#RESOURCE_IDENTIFIER} + {@link #getResourceName} do the following:
     * </p>
     *
     * <p>
     * Retrieve the existing mappings of the FacesServlet, e.g. using {@link ServletRegistration#getMappings()}, and from
     * those pick any prefix mapping or extension mapping. If no such mapping is found, throw an
     * {@link IllegalStateException}. If such mapping is found remove the <code>*</code> character from that mapping, take
     * that as the new <em>facesServletMapping</em> and continue with evaluating this mapping as specified below for <em>if
     * prefix mapped</em> and for <em>if extension mapped</em> </div>
     *
     * <p>
     * If prefix mapped, <em>result</em> must be
     * </p>
     *
     * <blockquote>
     * <p>
     * <code>result = <em>contextRoot</em> + '/' +
     * <em>facesServletMapping</em> + {@link
     * ResourceHandler#RESOURCE_IDENTIFIER} + '/' + {@link
     * #getResourceName}</code>
     * </p>
     * </blockquote>
     *
     * <p>
     * If extension mapped, <em>result</em> must be
     * </p>
     *
     * <blockquote>
     * <p>
     * <code>result = <em>contextRoot</em> + {@link
     * ResourceHandler#RESOURCE_IDENTIFIER} + {@link #getResourceName} +
     * <em>facesServletMapping</em></code>
     * </p>
     * </blockquote>
     *
     * </li>
     *
     * <li class="changed_modified_2_2">
     * <p>
     * Build up a string, called <em>resourceMetaData</em> which is an &amp; separated string of name=value pairs suitable
     * for inclusion in a URL query string.
     * </p>
     *
     * <blockquote>
     *
     * <p>
     * If {@link #getLibraryName} returns non-<code>null</code>, <code>resourceMetaData</code> must include "ln=" + the
     * return from {@link #getLibraryName}
     * </p>
     *
     * <p class="changed_added_2_2">
     * If there is a <code>localePrefix</code> for this application, as defined in {@link ResourceHandler#LOCALE_PREFIX},
     * <code>resourceMetaData</code> must include "loc=" + the <code>localePrefix</code>.
     * </p>
     *
     *
     * <p class="changed_added_2_2">
     * If this resource is contained in a resource library contract, <code>resourceMetaData</code> must include "con=" + the
     * name of the resource library contract.
     * </p>
     *
     * </blockquote>
     *
     * <p>
     * Append "?" + <em>resourceMetaData</em> to <em>result</em>.
     * </p>
     *
     * </li>
     *
     * <li>
     * <p>
     * Make it portlet safe by passing the result through {@link ViewHandler#getResourceURL}.
     * </p>
     * </li>
     *
     * </ul>
     *
     * </div>
     *
     * @return the path to this resource, intended to be included in the encoded view that is sent to the browser when
     * sending a faces response.
     */
    public abstract String getRequestPath();

    /**
     * <p class="changed_added_2_0">
     * Return an actual <code>URL</code> instance that refers to this resource instance.
     * </p>
     *
     * @return Return an actual <code>URL</code> instance that refers to this resource instance.
     */
    @Override
    public abstract URL getURL();

    /**
     * <p class="changed_added_2_0">
     * Call through to {@link #getRequestPath} and return the result.
     * </p>
     *
     * @return Call through to {@link #getRequestPath} and return the result.
     */
    @Override
    public String toString() {
        return getRequestPath();
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Return</span> <code>true</code> if the user-agent requesting this resource needs
     * an update. <span class="changed_added_2_2">If the {@code If-Modified-Since} HTTP header is available for this
     * request, its value must be consulted, as specified in Section 14.25 of IETF RFC 2616, to determine the result.</span>
     * Returns <code>false</code> if the user-agent does not need an update for this resource.
     * </p>
     *
     * @param context the Faces context.
     * @return <code>true</code> or <code>false</code> depending on whether or not the user-agent needs an update of this
     * resource.
     */
    public abstract boolean userAgentNeedsUpdate(FacesContext context);

}
