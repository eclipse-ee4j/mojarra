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

package com.sun.faces.application.resource;

import static com.sun.faces.util.Util.getFacesMapping;
import static com.sun.faces.util.Util.getFirstWildCardMappingToFacesServlet;
import static com.sun.faces.util.Util.getLastModified;
import static com.sun.faces.util.Util.isResourceExactMappedToFacesServlet;
import static jakarta.faces.application.ProjectStage.Development;
import static jakarta.faces.application.ProjectStage.Production;
import static jakarta.faces.application.ResourceHandler.FACES_SCRIPT_LIBRARY_NAME;
import static jakarta.faces.application.ResourceHandler.FACES_SCRIPT_RESOURCE_NAME;
import static jakarta.faces.application.ResourceHandler.RESOURCE_IDENTIFIER;
import static jakarta.servlet.http.MappingMatch.EXACT;
import static jakarta.servlet.http.MappingMatch.PATH;
import static java.util.Collections.emptyMap;
import static java.util.Locale.US;
import static java.util.logging.Level.FINEST;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Default implementation of {@link jakarta.faces.application.Resource}. The ResourceImpl instance itself has the same
 * lifespan as the request, however, the ResourceInfo instances that back this object are cached by the ResourceManager
 * to reduce the time spent scanning for resources.
 */
public class ResourceImpl extends Resource implements Externalizable {

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    /* HTTP Date format required by the HTTP/1.1 RFC */
    private static final String RFC1123_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    /* The meta data on the resource */
    private transient ResourceInfo resourceInfo;

    /*
     * Response headers that need to be added by the ResourceManager implementation.
     */
    private transient Map<String, String> responseHeaders;

    /**
     * Time when this application was started. This is used to generate expiration headers.
     */
    private long initialTime;

    /**
     * Lifespan of this resource for caching purposes.
     */
    private long maxAge;

    /**
     * The URL of this {@link ResourceImpl} object is valid exactly as long as this {@link ResourceImpl} object itself is valid.
     * Therefore, resolve the URL only once and re-use the already resolved URL value for subsequent calls to {@link ResourceImpl#getURL()}.
     */
    private URL resolvedUrl = null;

    // ------------------------------------------------------------ Constructors

    /**
     * Necessary for serialization.
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    public ResourceImpl() {
    }

    /**
     * Creates a new instance of ResourceBase
     * @param resourceInfo the resource info
     * @param contentType the resource content type
     * @param initialTime the resource initial time
     * @param maxAge the resource max age
     */
    public ResourceImpl(ResourceInfo resourceInfo, String contentType, long initialTime, long maxAge) {

        this.resourceInfo = resourceInfo;
        super.setResourceName(resourceInfo.getName());
        super.setLibraryName(resourceInfo.getLibraryInfo() != null ? resourceInfo.getLibraryInfo().getName() : null);
        super.setContentType(contentType);
        this.initialTime = initialTime;
        this.maxAge = maxAge;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceImpl resource = (ResourceImpl) o;

        return resourceInfo.equals(resource.resourceInfo);
    }

    @Override
    public int hashCode() {
        return resourceInfo.hashCode();
    }

    // --------------------------------------------------- Methods from Resource

    /**
     * @see jakarta.faces.application.Resource#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        initResourceInfo();
        return resourceInfo.getHelper().getInputStream(resourceInfo, FacesContext.getCurrentInstance());
    }

    /**
     * @see jakarta.faces.application.Resource#getURL()
     */
    @Override
    public URL getURL() {
        if (resolvedUrl != null) {
            // fast path - re-use the already resolved url
            return resolvedUrl;
        }

        URL url = resourceInfo.getHelper().getURL(resourceInfo, FacesContext.getCurrentInstance());

        // remember this url for subsequent calls to this method
        resolvedUrl = url;

        return url;
    }

    /**
     * <p>
     * Implementation note. Any values added to getResponseHeaders() will only be visible across multiple calls to this
     * method when servicing a resource request (i.e.
     * {@link ResourceHandler#isResourceRequest(jakarta.faces.context.FacesContext)} returns <code>true</code>). If we're
     * not servicing a resource request, an empty Map will be returned and the values added are effectively thrown away.
     * </p>
     *
     * @see jakarta.faces.application.Resource#getResponseHeaders()
     */
    @Override
    public Map<String, String> getResponseHeaders() {

        if (isResourceRequest()) {
            if (responseHeaders == null) {
                responseHeaders = new HashMap<>(6, 1.0f);
            }

            if (FacesContext.getCurrentInstance().isProjectStage(Development)) {
                responseHeaders.put("Cache-Control", "no-store, must-revalidate");
            } else {
                responseHeaders.put("Cache-Control", "max-age=" + (maxAge/1000));
            }

            URL url = getURL();
            InputStream in = null;
            try {
                URLConnection conn = url.openConnection();
                conn.setUseCaches(false);
                conn.connect();
                in = conn.getInputStream();
                long lastModified = getLastModified(url);
                long contentLength = conn.getContentLength();
                if (lastModified == 0) {
                    lastModified = initialTime;
                }
                SimpleDateFormat format = new SimpleDateFormat(RFC1123_DATE_PATTERN, US);
                format.setTimeZone(GMT);
                responseHeaders.put("Last-Modified", format.format(new Date(lastModified)));
                if (lastModified != 0 && contentLength != -1) {
                    responseHeaders.put("ETag", "W/\"" + contentLength + '-' + lastModified + '"');
                }
            } catch (IOException ioe) {
                if (LOGGER.isLoggable(FINEST)) {
                    LOGGER.log(FINEST, "Closing stream", ioe);
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                        if (LOGGER.isLoggable(FINEST)) {
                            LOGGER.log(FINEST, "Closing stream", ioe);
                        }
                    }
                }
            }
            return responseHeaders;
        } else {
            return emptyMap();
        }
    }

    /**
     * @see jakarta.faces.application.Resource#getRequestPath()
     */
    @Override
    public String getRequestPath() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletMapping mapping = getFacesMapping(context);

        String uri = null;

        // Check for exact mapping first
        if (mapping.getMappingMatch() == EXACT) {
            String resource = RESOURCE_IDENTIFIER + '/' + getResourceName();
            // Check if the FacesServlet is exact mapped to the resource
            if (isResourceExactMappedToFacesServlet(context.getExternalContext(), resource)) {
                uri = resource;
            } else {
                // No exact mapping for the requested resource, see if Facelets servlet is mapped to
                // e.g. /faces/* or *.xhtml and take that mapping
                mapping = getFirstWildCardMappingToFacesServlet(context.getExternalContext());

                if (mapping == null) {

                    // If there are only exact mappings and the resource is not exact mapped,
                    // we can't serve this resource

                    throw new IllegalStateException("No suitable mapping for FacesServlet found. To serve resources "
                            + "FacesServlet should have at least one prefix or suffix mapping.");
                }
            }
        }

        if (uri == null) {
            if (mapping.getMappingMatch() == PATH) {
                // If it is prefix/path mapped, e.g /faces/* -> /faces/jakarta.faces.resource/name
                uri = mapping.getPattern().replace("/*", RESOURCE_IDENTIFIER) + '/' + getResourceName();
            } else {
                // If it is prefix/path mapped, e.g *.xhtml -> /jakarta.faces.resource/name.xhtml
                uri = RESOURCE_IDENTIFIER + '/' + mapping.getPattern().replace("*", getResourceName());
            }
        }

        boolean queryStarted = false;
        if (getLibraryName() != null) {
            queryStarted = true;
            uri += "?ln=" + getLibraryName();
        }

        String version = "";
        initResourceInfo();
        if (resourceInfo.getLibraryInfo() != null && resourceInfo.getLibraryInfo().getVersion() != null) {
            version += resourceInfo.getLibraryInfo().getVersion().toString();
        }
        if (resourceInfo.getVersion() != null) {
            version += resourceInfo.getVersion().toString();
        }

        if (version.length() > 0) {
            uri += (queryStarted ? "&v=" : "?v=") + version;
            queryStarted = true;
        }

        String localePrefix = resourceInfo.getLocalePrefix();
        if (localePrefix != null) {
            uri += (queryStarted ? "&loc=" : "?loc=") + localePrefix;
            queryStarted = true;
        }

        String contract = resourceInfo.getContract();
        if (contract != null) {
            uri += (queryStarted ? "&con=" : "?con=") + contract;
            queryStarted = true;
        }

        if (FACES_SCRIPT_RESOURCE_NAME.equals(getResourceName()) && FACES_SCRIPT_LIBRARY_NAME.equals(getLibraryName())) {
            ProjectStage stage = context.getApplication().getProjectStage();
            switch (stage) {
            case Development:
                uri += queryStarted ? "&stage=Development" : "?stage=Development";
                break;
            case SystemTest:
                uri += queryStarted ? "&stage=SystemTest" : "?stage=SystemTest";
                break;
            case UnitTest:
                uri += queryStarted ? "&stage=UnitTest" : "?stage=UnitTest";
                break;
            default:
                assert stage.equals(Production);
            }
        }

        return context.getApplication().getViewHandler().getResourceURL(context, uri);
    }

    /**
     * @see jakarta.faces.application.Resource#userAgentNeedsUpdate(jakarta.faces.context.FacesContext)
     */
    @Override
    public boolean userAgentNeedsUpdate(FacesContext context) {

        // PENDING(edburns): this is a sub-optimal implementation choice
        // done in the interest of prototyping. It's never a good idea
        // to do a switch statement based on the type of an object.

        if (resourceInfo instanceof FaceletResourceInfo) {
            return true;
        }

        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
        // 14.25 If-Modified-Since

        // if the requested variant has not been modified since the time
        // specified in this field, an entity will not be returned from the
        // server; instead, a 304 (not modified) response will be returned
        // without any message-body.

        // A date which is later than the server's current time is
        // invalid.

        Map<String, String> requestHeaders = context.getExternalContext().getRequestHeaderMap();

        if (requestHeaders.containsKey(IF_MODIFIED_SINCE)) {
            initResourceInfo();
            /*
             * Make sure that we strip the milliseconds out of what comes back from the getLastModified call for a resource as the
             * 'If-Modified-Since' header does not use milliseconds.
             */
            long lastModifiedOfResource = ((ClientResourceInfo) resourceInfo).getLastModified(context) / 1000 * 1000;
            long lastModifiedHeader = getIfModifiedHeader(context.getExternalContext());
            if (0 == lastModifiedOfResource) {
                long startupTime = ApplicationAssociate.getInstance(context.getExternalContext()).getTimeOfInstantiation();
                return startupTime > lastModifiedHeader;
            } else {
                return lastModifiedOfResource > lastModifiedHeader;
            }
        }
        return true;

    }

    // --------------------------------------------------------- Private Methods

    /*
     * This method should only be called if the 'If-Modified-Since' header is present in the request header map.
     */
    private long getIfModifiedHeader(ExternalContext extcontext) {

        Object request = extcontext.getRequest();
        if (request instanceof HttpServletRequest) {
            // try to use the container where we can. V3 for instance
            // has a FastHttpDateFormat format/parse implementation
            // which is more than likely more performant than SimpleDateFormat
            // (otherwise, why would it be there?).
            return ((HttpServletRequest) request).getDateHeader(IF_MODIFIED_SINCE);
        } else {
            SimpleDateFormat format = new SimpleDateFormat(RFC1123_DATE_PATTERN, Locale.US);
            try {
                Date ifModifiedSinceDate = format.parse(extcontext.getRequestHeaderMap().get(IF_MODIFIED_SINCE));
                return ifModifiedSinceDate.getTime();
            } catch (ParseException ex) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "faces.application.resource.invalid_if_modified_since_header",
                            new Object[] { extcontext.getRequestHeaderMap().get(IF_MODIFIED_SINCE) });
                    LOGGER.log(Level.WARNING, "", ex);
                }
                return -1;
            }
        }

    }

    // --------------------------------------------- Methods from Externalizable

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeObject(getResourceName());
        out.writeObject(getLibraryName());
        out.writeObject(getContentType());
        out.writeLong(initialTime);
        out.writeLong(maxAge);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        setResourceName((String) in.readObject());
        setLibraryName((String) in.readObject());
        setContentType((String) in.readObject());
        initialTime = in.readLong();
        maxAge = in.readLong();
    }

    private void initResourceInfo() {
        if (resourceInfo != null) {
            return;
        }
        ResourceManager manager = ApplicationAssociate.getInstance(FacesContext.getCurrentInstance().getExternalContext()).getResourceManager();
        resourceInfo = manager.findResource(getLibraryName(), getResourceName(), getContentType(), FacesContext.getCurrentInstance());
    }

    // --------------------------------------------------------- Private Methods

    private boolean isResourceRequest() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getApplication().getResourceHandler().isResourceRequest(ctx);
    }

}
