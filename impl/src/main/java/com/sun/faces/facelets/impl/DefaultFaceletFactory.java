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

package com.sun.faces.facelets.impl;

import static com.sun.faces.RIConstants.CHAR_ENCODING;
import static com.sun.faces.cdi.CdiUtils.getBeanReference;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.UseFaceletsID;
import static com.sun.faces.util.Util.isEmpty;
import static com.sun.faces.util.Util.notNull;
import static com.sun.faces.util.Util.saveDOCTYPEToFacesContextAttributes;
import static com.sun.faces.util.Util.saveXMLDECLToFacesContextAttributes;
import static java.util.logging.Level.FINEST;
import static java.util.regex.Pattern.quote;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.resource.ResourceManager;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.context.FacesFileNotFoundException;
import com.sun.faces.facelets.compiler.Compiler;
import com.sun.faces.util.Cache;
import com.sun.faces.util.FacesLogger;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.annotation.View;
import jakarta.faces.application.Application;
import jakarta.faces.component.Doctype;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;
import jakarta.faces.view.facelets.FaceletCache;
import jakarta.faces.view.facelets.FaceletCacheFactory;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.FaceletHandler;

/**
 * Default FaceletFactory implementation.
 *
 * @author Jacob Hookom
 * @version $Id: DefaultFaceletFactory.java,v 1.10 2007/04/09 01:13:17 youngm Exp $
 */
public class DefaultFaceletFactory {

    protected final static Logger log = FacesLogger.FACELETS_FACTORY.getLogger();

    private Compiler compiler;

    // We continue to use a ResourceResolver just in case someone
    // provides a custom one. The DefaultResourceResolver simply uses
    // the ResourceHandler to do its work.
    private DefaultResourceResolver resolver;
    private ResourceManager manager;
    private URL baseUrl;
    private String baseUrlAsString;
    private long refreshPeriod;
    private FaceletCache<DefaultFacelet> cache;
    private ConcurrentMap<String, FaceletCache<DefaultFacelet>> cachePerContract;

    Cache<String, IdMapper> idMappers;

    // ------------------------------------------------------------ Constructors

    public DefaultFaceletFactory() {
        refreshPeriod = -1;
    }

    public final void init(FacesContext facesContext, Compiler compiler, DefaultResourceResolver resolver, long refreshPeriod, FaceletCache cache) {
        notNull("compiler", compiler);
        notNull("resolver", resolver);

        ExternalContext externalContext = facesContext.getExternalContext();
        WebConfiguration config = WebConfiguration.getInstance(externalContext);

        this.compiler = compiler;
        cachePerContract = new ConcurrentHashMap<>();
        this.resolver = resolver;
        this.manager = ApplicationAssociate.getInstance(externalContext).getResourceManager();
        baseUrl = resolver.resolveUrl("/");
        baseUrlAsString = baseUrl.toExternalForm();
        this.idMappers = config.isOptionEnabled(UseFaceletsID) ? null : new Cache<>(new IdMapperFactory());
        refreshPeriod = refreshPeriod >= 0 ? refreshPeriod * 1000 : -1;
        this.refreshPeriod = refreshPeriod;
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Using ResourceResolver: {0}", resolver);
            log.log(Level.FINE, "Using Refresh Period: {0}", refreshPeriod);
        }

        // We can cast to the FaceletCache<DefaultFacelet> here because we know
        // that the Generics information is only used at compile time, and all cache
        // implementations will be using instance factories provided by us and returning DefaultFacelet
        this.cache = initCache(cache);
    }

    public DefaultResourceResolver getResourceResolver() {
        return resolver;
    }

    public Facelet getMetadataFacelet(FacesContext context, String viewId) throws IOException {
        Facelet facelet = getBeanReference(context, Facelet.class, View.Literal.of(viewId));
        if (facelet == null) {
            facelet = getMetadataFacelet(context, resolveURL(viewId));
        }

        return facelet;
    }
   
    public Facelet getFacelet(FacesContext context, String viewId) throws IOException {
        Facelet facelet = getBeanReference(context, Facelet.class, View.Literal.of(viewId));
        if (facelet == null) {
            facelet = getFacelet(context, resolveURL(viewId));
        }

        return facelet;
    }

    /**
     * Resolves a path based on the passed URL. If the path starts with '/', then resolve the path against
     * {@link jakarta.faces.context.ExternalContext#getResource(java.lang.String)
     * jakarta.faces.context.ExternalContext#getResource(java.lang.String)}. Otherwise create a new URL via
     * {@link URL#URL(java.net.URL, java.lang.String) URL(URL, String)}.
     *
     * @param source base to resolve from
     * @param path relative path to the source
     *
     * @return resolved URL
     *
     * @throws IOException when an I/O exception occurs
     */
    public URL resolveURL(URL source, String path) throws IOException {
        // PENDING(FCAPUTO): always go to the resolver to make resource library contracts work with relative urls
        if (path.startsWith("/")) {
            URL url = resolver.resolveUrl(path);
            if (url == null) {
                throw new FacesFileNotFoundException(path + " Not Found in ExternalContext as a Resource");
            }
            return url;
        }

        return new URL(source, path);
    }

    /**
     * Returns true if given url is a contracts resource.
     * @param url source url
     * @return true if given url is a contracts resource.
     */
    public boolean isContractsResource(URL url) {
        String urlAsString = url.toExternalForm();
        
        if (!urlAsString.startsWith(baseUrlAsString)) {
            return false;
        }

        String path = urlAsString.substring(baseUrlAsString.length());
        return manager.isContractsResource(path);
    }

    /**
     * Create a Facelet from the passed URL. This method checks if the cached Facelet needs to be refreshed before
     * returning. If so, uses the passed URL to build a new instance;
     *
     * @param context the involved faces context
     * @param url source url
     *
     * @return Facelet instance
     *
     * @throws IOException when an I/O exception occurs
     * @throws FaceletException when a Facelet exception occurs
     * @throws FacesException when a Faces exception occurs
     * @throws ELException when an EL exception occurs
     */
    public Facelet getFacelet(FacesContext context, URL url) throws IOException {
        Facelet result = getCache(context).getFacelet(url);

        DefaultFacelet _facelet = null;
        if (result instanceof DefaultFacelet) {
            _facelet = (DefaultFacelet) result;

            Doctype doctype = _facelet.getSavedDoctype();
            if (doctype != null) {
                saveDOCTYPEToFacesContextAttributes(doctype);
            }

            String xmlDecl = _facelet.getSavedXMLDecl();
            if (xmlDecl != null) {
                saveXMLDECLToFacesContextAttributes(xmlDecl);
            }
        }

        return result;
    }

    public Facelet getMetadataFacelet(FacesContext context, URL url) throws IOException {
        return getCache(context).getViewMetadataFacelet(url);
    }

    public boolean needsToBeRefreshed(URL url) {
        if (!cache.isFaceletCached(url)) {
            return true;
        }
        if (cachePerContract == null) {
            return false;
        }

        // PENDING(FCAPUTO) not sure, if this is what we want.
        for (FaceletCache<DefaultFacelet> faceletCache : cachePerContract.values()) {
            if (!faceletCache.isFaceletCached(url)) {
                return true;
            }
        }

        return false;
    }

    public UIComponent _createComponent(FacesContext context, String taglibURI, String tagName, Map<String, Object> attributes) {
        // PENDING(FCAPUTO) does this work for resource library contracts? I think so.
        UIComponent result = null;
        Application app = context.getApplication();
        ExternalContext extContext = context.getExternalContext();
        File tmpDir = (File) extContext.getApplicationMap().get("jakarta.servlet.context.tempdir");
        File tempFile = null;
        OutputStreamWriter osw = null;
        try {

            // create a temporary file in that directory
            tempFile = File.createTempFile("mojarra", ".tmp", tmpDir);
            osw = new OutputStreamWriter(new FileOutputStream(tempFile), RIConstants.CHAR_ENCODING);
            osw.append("<?xml version='1.0' encoding='");
            osw.append(RIConstants.CHAR_ENCODING);
            osw.append("' ?>");
            osw.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
            osw.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"\n");
            osw.append("      xmlns:j=\"").append(taglibURI).append("\">");
            osw.append("  <j:").append(tagName).append(" ");

            if (!isEmpty(attributes)) {
                for (Map.Entry<String, Object> attr : attributes.entrySet()) {
                    osw.append(attr.getKey()).append("=\"").append(attr.getValue().toString()).append("\"").append(" ");
                }
            }

            String tempId = context.getViewRoot().createUniqueId(context, tagName);
            osw.append(" id=\"").append(tempId).append("\" />");
            osw.append("</html>");
            try {
                osw.flush();
                osw.close();
            } catch (IOException ex) {
                if (log.isLoggable(FINEST)) {
                    log.log(FINEST, "Flushing and closing stream", ex);
                }
            }

            URL fabricatedFaceletPage = tempFile.toURI().toURL();
            Facelet fabricatedFacelet = createFacelet(fabricatedFaceletPage);
            UIComponent tmp = app.createComponent("jakarta.faces.NamingContainer");
            tmp.setId(context.getViewRoot().createUniqueId());
            fabricatedFacelet.apply(context, tmp);
            result = tmp.findComponent(tempId);
            tmp.getChildren().clear();
            osw = null;

        } catch (MalformedURLException mue) {
                log.log(FINEST, "Invalid URL", mue);
        } catch (IOException ioe) {
                log.log(FINEST, "I/O error", ioe);
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException ioe) {
                    log.log(FINEST, "Closing stream", ioe);
                }
            }

            if (tempFile != null) {
                boolean successful = tempFile.delete();
                if (!successful && log.isLoggable(FINEST)) {
                    log.log(FINEST, "Unable to delete temporary file.");
                }
            }
        }

        try {
            byte[] faceletPage = "facelet".getBytes(CHAR_ENCODING);
            ByteArrayInputStream bais = new ByteArrayInputStream(faceletPage);
        } catch (UnsupportedEncodingException uee) {
            if (log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "Unsupported encoding when creating component for " + tagName + " in " + taglibURI, uee);
            }
        }

        if (result != null) {
            result.setId(null);
        }

        return result;
    }


    // ---------------------------------------------------------- Private Methods


    private FaceletCache<DefaultFacelet> initCache(FaceletCache<DefaultFacelet> cache) {
        if (cache == null) {
            FaceletCacheFactory cacheFactory = (FaceletCacheFactory) FactoryFinder.getFactory(FactoryFinder.FACELET_CACHE_FACTORY);
            cache = cacheFactory.getFaceletCache();
        }

        // Create instance factories for the cache, so that the cache can
        // create Facelets and Metadata Facelets
        FaceletCache.MemberFactory<DefaultFacelet> faceletFactory = key -> createFacelet(key);
        FaceletCache.MemberFactory<DefaultFacelet> metadataFaceletFactory = key -> createMetadataFacelet(key);

        cache.setCacheFactories(faceletFactory, metadataFaceletFactory);
        return cache;
    }

    private FaceletCache<DefaultFacelet> getCache(FacesContext context) {
        List<String> contracts = context.getResourceLibraryContracts();
        if (!contracts.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < contracts.size(); i++) {
                builder.append(contracts.get(i));
                if (i + 1 != contracts.size()) {
                    builder.append(",");
                }
            }

            String contractsKey = builder.toString();
            FaceletCache<DefaultFacelet> faceletCache = cachePerContract.get(contractsKey);
            if (faceletCache == null) {
                // PENDING(FCAPUTO) we don't support com.sun.faces.config.WebConfiguration.WebContextInitParameter#FaceletCache for
                // contracts
                faceletCache = initCache(null);
                cachePerContract.putIfAbsent(contractsKey, faceletCache);
                faceletCache = cachePerContract.get(contractsKey);
            }

            return faceletCache;
        }

        return cache;
    }

    private URL resolveURL(String uri) throws IOException {
        // PENDING(FCAPUTO) Deactivated caching for resource library contracts. If we still want to cache it, we need a cache
        // per contract libraries list.
        // But the ResourceHandler caches on his own (using ResourceManager).
        URL url = resolveURL(baseUrl, uri);
        if (url == null) {
            throw new IOException("'" + uri + "' not found.");
        }

        return url;
    }

    /**
     * Uses the internal Compiler reference to build a Facelet given the passed URL.
     *
     * @param url source
     *
     * @return a Facelet instance
     *
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    private DefaultFacelet createFacelet(URL url) throws IOException {
        String escapedBaseURL = Pattern.quote(this.baseUrl.getFile());
        String alias = '/' + url.getFile().replaceFirst(escapedBaseURL, "");
        return createFacelet(url, alias);
    }

    private DefaultFacelet createFacelet(URL url, String alias) throws IOException {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating Facelet for: " + url);
        }
        try {
            FaceletHandler h = compiler.compile(url, alias);
            return new DefaultFacelet(this, compiler.createExpressionFactory(), url, alias, h);
        } catch (FileNotFoundException fnfe) {
            throw new FileNotFoundException("Facelet " + alias + " not found at: " + url.toExternalForm());
        }
    }

    private DefaultFacelet createMetadataFacelet(URL url) throws IOException {
        log.fine(() -> "Creating Metadata Facelet for: " + url);

        String alias = '/' + url.getFile().replaceFirst(quote(baseUrl.getFile()), "");
        try {
            return new DefaultFacelet(this,
                compiler.createExpressionFactory(),
                url, alias,
                compiler.metadataCompile(url, alias));
        } catch (FileNotFoundException fnfe) {
            throw new FileNotFoundException("Facelet " + alias + " not found at: " + url.toExternalForm());
        }

    }

    public long getRefreshPeriod() {
        return refreshPeriod;
    }


    // ---------------------------------------------------------- Nested Classes

    private static final class IdMapperFactory implements Cache.Factory<String, IdMapper> {

        // ------------------------------------------ Methods from Cache.Factory

        @Override
        public IdMapper newInstance(String arg) throws InterruptedException {
            return new IdMapper();
        }

    }

}
