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

import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.DefaultResourceMaxAge;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.ResourceBufferSize;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.ResourceExcludes;
import static com.sun.faces.util.RequestStateManager.RESOURCE_REQUEST;
import static com.sun.faces.util.Util.getFacesMapping;
import static com.sun.faces.util.Util.notNegative;
import static com.sun.faces.util.Util.notNull;
import static jakarta.faces.application.ProjectStage.Development;
import static jakarta.faces.application.ProjectStage.Production;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static jakarta.servlet.http.MappingMatch.EXTENSION;
import static java.lang.Boolean.FALSE;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.Util;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.ResourceVisitOption;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * This is the default implementation of {@link ResourceHandler}.
 */
public class ResourceHandlerImpl extends ResourceHandler {

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    ResourceManager manager;
    List<Pattern> excludePatterns;
    private long creationTime;
    private long maxAge;
    private final WebConfiguration webconfig;

    // ------------------------------------------------------------ Constructors

    /**
     * Creates a new instance of ResourceHandlerImpl
     */
    public ResourceHandlerImpl() {
        creationTime = System.currentTimeMillis();
        webconfig = WebConfiguration.getInstance();
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        manager = ApplicationAssociate.getInstance(extContext).getResourceManager();
        initExclusions(extContext.getApplicationMap());
        initMaxAge();
    }

    // ------------------------------------------- Methods from Resource Handler

    /**
     * @see ResourceHandler#createResource(String)
     */
    @Override
    public Resource createResource(String resourceName) {
        return createResource(resourceName, null, null);
    }

    /**
     * @see ResourceHandler#createResource(String, String)
     */
    @Override
    public Resource createResource(String resourceName, String libraryName) {
        return createResource(resourceName, libraryName, null);
    }

    /**
     * @see ResourceHandler#createResource(String, String, String)
     */
    @Override
    public Resource createResource(String resourceName, String libraryName, String contentType) {
        notNull("resourceName", resourceName);

        FacesContext ctx = FacesContext.getCurrentInstance();

        String ctype = contentType != null ? contentType : getContentType(ctx, resourceName);
        ResourceInfo info = manager.findResource(libraryName, resourceName, ctype, ctx);

        if (info == null) {
            return null;
        }

        return new ResourceImpl(info, ctype, creationTime, maxAge);
    }

    @Override
    public Resource createViewResource(FacesContext facesContext, String resourceName) {
        notNull("resourceName", resourceName);

        String contentType = getContentType(facesContext, resourceName);
        ResourceInfo resourceInfo = manager.findViewResource(resourceName, contentType, facesContext);

        if (resourceInfo == null) {
            return null;
        }

        return new ResourceImpl(resourceInfo, contentType, creationTime, maxAge);
    }

    /**
     * @see ResourceHandler#getViewResources(FacesContext, String, ResourceVisitOption...)
     */
    @Override
    public Stream<String> getViewResources(FacesContext facesContext, String path, ResourceVisitOption... options) {
        notNull("path", path);

        return manager.getViewResources(facesContext, path, Integer.MAX_VALUE, options);
    }

    /**
     * @see ResourceHandler#getViewResources(FacesContext, String, int, ResourceVisitOption...)
     */
    @Override
    public Stream<String> getViewResources(FacesContext facesContext, String path, int maxDepth, ResourceVisitOption... options) {
        notNull("path", path);
        notNegative("maxDepth", maxDepth);

        return manager.getViewResources(facesContext, path, maxDepth, options);
    }

    /**
     * @see ResourceHandler#createResourceFromId(String)
     */
    @Override
    public Resource createResourceFromId(String resourceId) {
        notNull("resourceId", resourceId);
        FacesContext ctx = FacesContext.getCurrentInstance();

        boolean development = ctx.isProjectStage(Development);

        ResourceInfo info = manager.findResource(resourceId);
        String ctype = getContentType(ctx, resourceId);
        if (info == null) {
            logMissingResource(ctx, resourceId, null);
            return null;
        } else {
            return new ResourceImpl(info, ctype, creationTime, maxAge);
        }

    }

    @Override
    public boolean libraryExists(String libraryName) {

        if (libraryName.contains("../")) {
            return false;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        // PENDING(fcaputo) do we need to iterate over the contracts here? I don't think so.
        LibraryInfo info = manager.findLibrary(libraryName, null, null, context);
        if (info == null) {
            info = manager.findLibraryOnClasspathWithZipDirectoryEntryScan(libraryName, null, null, context, true);

        }

        return info != null;
    }

    /**
     * @see ResourceHandler#isResourceRequest(jakarta.faces.context.FacesContext)
     */
    @Override
    public boolean isResourceRequest(FacesContext context) {

        Boolean isResourceRequest = RequestStateManager.get(context, RESOURCE_REQUEST);
        if (isResourceRequest == null) {
            String resourceId = normalizeResourceRequest(context);
            isResourceRequest = resourceId != null ? resourceId.startsWith(RESOURCE_IDENTIFIER) : FALSE;
            RequestStateManager.set(context, RESOURCE_REQUEST, isResourceRequest);
        }

        return isResourceRequest;
    }

    @Override
    public String getRendererTypeForResourceName(String resourceName) {
        String rendererType = null;

        String contentType = getContentType(FacesContext.getCurrentInstance(), resourceName);
        if (null != contentType) {
            contentType = contentType.toLowerCase();
            if (contentType.contains("javascript")) {
                rendererType = "jakarta.faces.resource.Script";
            } else if (contentType.contains("css")) {
                rendererType = "jakarta.faces.resource.Stylesheet";
            }
        }

        return rendererType;
    }

    /**
     * @see jakarta.faces.application.ResourceHandler#handleResourceRequest(jakarta.faces.context.FacesContext)
     */
    @Override
    public void handleResourceRequest(FacesContext context) throws IOException {
        String resourceId = normalizeResourceRequest(context);
        if (resourceId == null) {
            // handleResourceRequest called for a non-resource request, bail out.
            return;
        }

        ExternalContext extContext = context.getExternalContext();

        if (isExcluded(resourceId)) {
            extContext.setResponseStatus(SC_NOT_FOUND);
            return;
        }

        assert null != resourceId;
        assert resourceId.startsWith(RESOURCE_IDENTIFIER);

        Resource resource = null;
        String resourceName = null;
        String libraryName = null;
        if (RESOURCE_IDENTIFIER.length() < resourceId.length()) {
            resourceName = resourceId.substring(RESOURCE_IDENTIFIER.length() + 1);
            assert resourceName != null;
            libraryName = context.getExternalContext().getRequestParameterMap().get("ln");

            boolean createResource;

            if (libraryName != null) {
                createResource = libraryNameIsSafe(libraryName);
                if (!createResource) {
                    send404(context, resourceName, libraryName, true);
                    return;
                }
            } else {
                createResource = true;
            }
            if (createResource) {
                resource = context.getApplication().getResourceHandler().createResource(resourceName, libraryName);
            }
        }

        if (resource != null) {
            if (resource.userAgentNeedsUpdate(context)) {
                ReadableByteChannel resourceChannel = null;
                WritableByteChannel out = null;
                ByteBuffer buf = allocateByteBuffer();
                try {
                    InputStream in = resource.getInputStream();
                    if (in == null) {
                        send404(context, resourceName, libraryName, true);
                        return;
                    }
                    resourceChannel = Channels.newChannel(in);
                    out = Channels.newChannel(extContext.getResponseOutputStream());
                    extContext.setResponseBufferSize(buf.capacity());
                    String contentType = resource.getContentType();
                    if (contentType != null) {
                        extContext.setResponseContentType(resource.getContentType());
                    }
                    handleHeaders(context, resource);

                    int size = 0;
                    for (int thisRead = resourceChannel.read(buf), totalWritten = 0; thisRead != -1; thisRead = resourceChannel.read(buf)) {

                        buf.rewind();
                        buf.limit(thisRead);
                        size += thisRead;
                        do {
                            totalWritten += out.write(buf);
                        } while (totalWritten < size);
                        buf.clear();
                    }

                    if (!extContext.isResponseCommitted()) {
                        extContext.setResponseContentLength(size);
                    }

                } catch (IOException ioe) {
                    if (isConnectionAbort(ioe)) { // to be removed, when the exception is standardised in servlet.
                        send404(context, resourceName, libraryName, false);
                    } else {
                        send404(context, resourceName, libraryName, ioe, true);
                    }
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ignored) {
                            // Maybe log a warning here?
                        }
                    }
                    if (resourceChannel != null) {
                        resourceChannel.close();
                    }
                }
            } else {
                send304(context);
            }

        } else {
            // already logged elsewhere
            send404(context, resourceName, libraryName, true);
        }

    }

    private static boolean isConnectionAbort(IOException ioe) {
        String exceptionClassName = ioe.getClass().getCanonicalName();
        return exceptionClassName.equals("org.apache.catalina.connector.ClientAbortException") ||
               exceptionClassName.equals("org.eclipse.jetty.io.EofException");
    }

    private boolean libraryNameIsSafe(String libraryName) {
        assert null != libraryName;
        boolean result;

        result = !(libraryName.startsWith(".") ||

                libraryName.startsWith("/") || libraryName.contains("/") ||

                libraryName.startsWith("\\") || libraryName.contains("\\") ||

                libraryName.startsWith("%2e") ||

                libraryName.startsWith("%2f") || libraryName.contains("%2f") ||

                libraryName.startsWith("%5c") || libraryName.contains("%5c") ||

                libraryName.startsWith("\\u002e") ||

                libraryName.startsWith("\\u002f") || libraryName.contains("\\u002f") ||

                libraryName.startsWith("\\u005c") || libraryName.contains("\\u005c"));

        return result;
    }

    private void send404(FacesContext ctx, String resourceName, String libraryName, boolean logMessage) {
        send404(ctx, resourceName, libraryName, null, logMessage);
    }

    private void send404(FacesContext ctx, String resourceName, String libraryName, Throwable t, boolean logMessage) {
        ctx.getExternalContext().setResponseStatus(SC_NOT_FOUND);
        if (logMessage) {
            logMissingResource(ctx, resourceName, libraryName, t);
        }
    }

    private void send304(FacesContext ctx) {
        ctx.getExternalContext().setResponseStatus(SC_NOT_MODIFIED);
    }

    // ------------------------------------------------- Package Private Methods

    /**
     * This method is leveraged by {@link ResourceImpl} to detemine if a resource has been upated. In short, a resource has
     * been updated if the timestamp is newer than the timestamp of the ResourceHandler creation time.
     *
     * @return the time when the ResourceHandler was instantiated (in milliseconds)
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    long getCreationTime() {
        return creationTime;
    }

    /**
     * This method is here soley for the purpose of unit testing and will not be invoked during normal runtime.
     *
     * @param creationTime the time in milliseconds
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Utility method leveraged by ResourceImpl to reduce the cost of looking up the WebConfiguration per-instance.
     *
     * @return the {@link WebConfiguration} for this application
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    WebConfiguration getWebConfig() {
        return webconfig;
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Log a message indicating a particular resource (reference by name and/or library) could not be found. If this was due
     * to an exception, the exception provided will be logged as well.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param resourceName the resource name
     * @param libraryName the resource library
     * @param t the exception caught when attempting to find the resource
     */
    private void logMissingResource(FacesContext ctx, String resourceName, String libraryName, Throwable t) {

        Level level;
        if (!ctx.isProjectStage(Production)) {
            level = WARNING;
        } else {
            level = t != null ? WARNING : FINE;
        }

        if (libraryName != null) {
            if (LOGGER.isLoggable(level)) {
                LOGGER.log(level, "faces.application.resource.unable_to_serve_from_library", new Object[] { resourceName, libraryName });
                if (t != null) {
                    LOGGER.log(level, "", t);
                }
            }
        } else {
            if (LOGGER.isLoggable(level)) {
                LOGGER.log(level, "faces.application.resource.unable_to_serve", new Object[] { resourceName });
                if (t != null) {
                    LOGGER.log(level, "", t);
                }
            }
        }

    }

    /**
     * Log a message indicating a particular resource (reference by name and/or library) could not be found. If this was due
     * to an exception, the exception provided will be logged as well.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param resourceName the resource name
     * @param libraryName the resource library
     * @param t the exception caught when attempting to find the resource
     */
    private void logMissingResource(FacesContext ctx, String resourceId, Throwable t) {
        Level level;
        if (!ctx.isProjectStage(Production)) {
            level = WARNING;
        } else {
            level = t != null ? WARNING : FINE;
        }

        if (LOGGER.isLoggable(level)) {
            LOGGER.log(level, "faces.application.resource.unable_to_serve", new Object[] { resourceId });
            if (t != null) {
                LOGGER.log(level, "", t);
            }
        }

    }

    /**
     * @param resourceName the resource of interest. The resourceName in question may consist of zero or more path elements
     * such that resourceName could be something like path1/path2/resource.jpg or resource.jpg
     * @return the content type for this resource
     */
    private String getContentType(FacesContext ctx, String resourceName) {
        return ctx.getExternalContext().getMimeType(resourceName);
    }

    /**
     * Normalize the request path to exclude Faces invocation information.
     *
     * <P>
     * If the FacesServlet servicing this request was
     * extension mapped, then the extension will be trimmed off.
     *
     * <p>
     * If the FacesServlet servicing this request was
     * prefix mapped, then the path to the FacesServlet will be removed.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @return the request path without Faces invocation information
     */
    private String normalizeResourceRequest(FacesContext context) {

        // If it is extension mapped
        if (getFacesMapping(context).getMappingMatch() == EXTENSION) {
            String path = context.getExternalContext().getRequestServletPath();
            // strip off the extension
            return path.substring(0, path.lastIndexOf("."));
        }

        return context.getExternalContext().getRequestPathInfo();
    }

    /**
     * @param resourceId the normalized request path as returned by
     * {@link #normalizeResourceRequest(jakarta.faces.context.FacesContext)}
     * @return <code>true</code> if the request matces an excluded resource, otherwise <code>false</code>
     */
    private boolean isExcluded(String resourceId) {
        for (Pattern pattern : excludePatterns) {
            if (pattern.matcher(resourceId).matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Initialize the exclusions for this application. If no explicit exclusions are configured, the defaults of
     * <ul>
     * <li>.class</li>
     * <li>.properties</li>
     * <li>.xhtml</li>
     * <ul>
     * will be used.
     */
    private void initExclusions(Map<String, Object> appMap) {
        String excludesParam = webconfig.getOptionValue(ResourceExcludes);
        String[] patterns = Util.split(appMap, excludesParam, " ");

        excludePatterns = new ArrayList<>(patterns.length);
        for (String pattern : patterns) {
            excludePatterns.add(Pattern.compile(".*\\" + pattern));
        }
    }

    private void initMaxAge() {
        maxAge = Long.parseLong(webconfig.getOptionValue(DefaultResourceMaxAge));
    }

    private void handleHeaders(FacesContext ctx, Resource resource) {
        ExternalContext extContext = ctx.getExternalContext();
        for (Map.Entry<String, String> cur : resource.getResponseHeaders().entrySet()) {
            extContext.setResponseHeader(cur.getKey(), cur.getValue());
        }
    }

    private ByteBuffer allocateByteBuffer() {
        int size;
        try {
            size = Integer.parseInt(webconfig.getOptionValue(ResourceBufferSize));
        } catch (NumberFormatException nfe) {
            if (LOGGER.isLoggable(WARNING)) {
                LOGGER.log(WARNING, "faces.application.resource.invalid_resource_buffer_size", new Object[] { webconfig.getOptionValue(ResourceBufferSize),
                        ResourceBufferSize.getQualifiedName(), ResourceBufferSize.getDefaultValue() });
            }
            size = Integer.parseInt(ResourceBufferSize.getDefaultValue());
        }

        return ByteBuffer.allocate(size);
    }

}
