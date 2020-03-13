/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.UseFaceletsID;
import com.sun.faces.context.FacesFileNotFoundException;
import java.net.MalformedURLException;
import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.Facelet;
import javax.faces.view.facelets.FaceletCache;
import com.sun.faces.facelets.compiler.Compiler;
import com.sun.faces.util.Cache;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import javax.faces.view.facelets.FaceletCacheFactory;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.ResourceResolver;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletException;


/**
 * Default FaceletFactory implementation.
 *
 * @author Jacob Hookom
 * @version $Id: DefaultFaceletFactory.java,v 1.10 2007/04/09 01:13:17 youngm
 *          Exp $
 */
public class DefaultFaceletFactory {

    protected final static Logger log = FacesLogger.FACELETS_FACTORY.getLogger();

    private Compiler compiler;

    // We continue to use a ResourceResolver just in case someone
    // provides a custom one.  The DefaultResourceResolver simply uses
    // the ResourceHandler to do its work.
    private ResourceResolver resolver;

    private  URL baseUrl;
    
    private long refreshPeriod;

    private FaceletCache<DefaultFacelet> cache;

    private ConcurrentMap<String, FaceletCache<DefaultFacelet>> cachePerContract;

    Cache<String,IdMapper> idMappers;
    


    // ------------------------------------------------------------ Constructors

    public DefaultFaceletFactory() {
        this.compiler = null;
        this.resolver = null;
        this.refreshPeriod = -1;
        this.cache = null;
        this.baseUrl = null;
    }
    
    
    public DefaultFaceletFactory(Compiler compiler, ResourceResolver resolver)
    throws IOException {

        this(compiler, resolver, -1, null);

    }


    public DefaultFaceletFactory(Compiler compiler,
                                 ResourceResolver resolver,
                                 long refreshPeriod) {
        this(compiler, resolver, refreshPeriod, null);
    }

    public DefaultFaceletFactory(Compiler compiler,
                                 ResourceResolver resolver,
                                 long refreshPeriod,
                                 FaceletCache cache) {
        this.init(compiler, resolver, refreshPeriod, cache);
    }
    
    public final void init(Compiler compiler,
            ResourceResolver resolver,
            long refreshPeriod,
            FaceletCache cache) {
        Util.notNull("compiler", compiler);
        Util.notNull("resolver", resolver);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            throw new IllegalStateException("DefaultFaceletFactory cannot locate the faces context");
        }
        ExternalContext externalContext = facesContext.getExternalContext();
        WebConfiguration config = WebConfiguration.getInstance(externalContext);

        this.compiler = compiler;
        this.cachePerContract = new ConcurrentHashMap<>();
        this.resolver = resolver;
        this.baseUrl = resolver.resolveUrl("/");
        this.idMappers = config.isOptionEnabled(UseFaceletsID)? null : new Cache<>(new IdMapperFactory());
        // this.location = url;
        refreshPeriod = (refreshPeriod >= 0) ? refreshPeriod * 1000 : -1;
        this.refreshPeriod = refreshPeriod;
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Using ResourceResolver: {0}", resolver);
            log.log(Level.FINE, "Using Refresh Period: {0}", refreshPeriod);
        }
        
        // We can cast to the FaceletCache<DefaultFacelet> here because we know
        // that the Generics information is only used at compile time, and all cache
        // implementations will be using instance factories provided by us and returning DefaultFacelet
        this.cache = initCache((FaceletCache<DefaultFacelet>)cache);
    }

    private FaceletCache<DefaultFacelet> initCache(FaceletCache<DefaultFacelet> cache) {

        if(cache == null) {
            FaceletCacheFactory cacheFactory = (FaceletCacheFactory)
                    FactoryFinder.getFactory(FactoryFinder.FACELET_CACHE_FACTORY);
            cache = cacheFactory.getFaceletCache();
        }

        // Create instance factories for the  cache, so that the cache can
        // create Facelets and Metadata Facelets
        FaceletCache.MemberFactory<DefaultFacelet> faceletFactory =
            new FaceletCache.MemberFactory<DefaultFacelet>() {
                @Override
                public DefaultFacelet newInstance(final URL key) throws IOException {
                    return createFacelet(key);
                }
            };
        FaceletCache.MemberFactory<DefaultFacelet> metadataFaceletFactory =
            new FaceletCache.MemberFactory<DefaultFacelet>() {
                @Override
                public DefaultFacelet newInstance(final URL key) throws IOException {
                    return createMetadataFacelet(key);
                }
            };
        
        cache.setCacheFactories(faceletFactory, metadataFaceletFactory);
        return cache;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.sun.facelets.FaceletFactory#getResourceResolver
      */
    public ResourceResolver getResourceResolver() {
        return resolver;
    }

    
    /*
      * (non-Javadoc)
      *
      * @see com.sun.facelets.FaceletFactory#getFacelet(java.lang.String)
      */
    public Facelet getFacelet(FacesContext context, String uri) throws IOException {

        return this.getFacelet(context, resolveURL(uri));

    }


    public Facelet getMetadataFacelet(FacesContext context, String uri) throws IOException {

        return this.getMetadataFacelet(context, resolveURL(uri));

    }


    /**
     * Resolves a path based on the passed URL. If the path starts with '/', then
     * resolve the path against {@link javax.faces.context.ExternalContext#getResource(java.lang.String)
     * javax.faces.context.ExternalContext#getResource(java.lang.String)}.
     * Otherwise create a new URL via {@link URL#URL(java.net.URL,
     * java.lang.String) URL(URL, String)}.
     *
     * @param source base to resolve from
     * @param path   relative path to the source
     *
     * @return resolved URL
     *
     * @throws IOException
     */
    public URL resolveURL(URL source, String path) throws IOException {
        // PENDING(FCAPUTO): always go to the resolver to make resource libary contracts work with relative urls
        if (path.startsWith("/")) {
            URL url = this.resolver.resolveUrl(path);
            if (url == null) {
                throw new FacesFileNotFoundException(path
                                                + " Not Found in ExternalContext as a Resource");
            }
            return url;
        } else {
            return new URL(source, path);
        }
    }

    /**
     * Create a Facelet from the passed URL. This method checks if the cached
     * Facelet needs to be refreshed before returning. If so, uses the passed URL
     * to build a new instance;
     *
     * @param url source url
     *
     * @return Facelet instance
     *
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    public Facelet getFacelet(FacesContext context, URL url) throws IOException {
        
        Facelet result = getCache(context).getFacelet(url);

        DefaultFacelet _facelet = null;
        if (result instanceof DefaultFacelet) {
            _facelet = (DefaultFacelet) result;
            String docType = _facelet.getSavedDoctype();
            if (null != docType) {
                Util.saveDOCTYPEToFacesContextAttributes(docType);
            }
            
            String xmlDecl = _facelet.getSavedXMLDecl();
            if (null != xmlDecl) {
                Util.saveXMLDECLToFacesContextAttributes(xmlDecl);
            }
        }

        return result;
        
    }

    public Facelet getMetadataFacelet(FacesContext context, URL url) throws IOException {
        return getCache(context).getViewMetadataFacelet(url);
    }

    public boolean needsToBeRefreshed(URL url) {
        if(!cache.isFaceletCached(url)) {
            return true;
        }
        if (cachePerContract == null) {
            return false;
        }
        // PENDING(FCAPUTO) not sure, if this is what we want.
        for (FaceletCache<DefaultFacelet> faceletCache : cachePerContract.values()) {
            if(!faceletCache.isFaceletCached(url)) {
                return true;
            }
        }
        return false;
    }

    private FaceletCache<DefaultFacelet> getCache(FacesContext context) {
        List<String> contracts = context.getResourceLibraryContracts();
        if(!contracts.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i=0; i<contracts.size(); i++) {
                builder.append(contracts.get(i));
                if (i + 1 != contracts.size()) {
                    builder.append(",");
                }
            }
            String contractsKey = builder.toString();
            FaceletCache<DefaultFacelet> faceletCache = cachePerContract.get(contractsKey);
            if(faceletCache == null) {
                // PENDING(FCAPUTO) we don't support com.sun.faces.config.WebConfiguration.WebContextInitParameter#FaceletCache for contracts
                faceletCache = initCache(null);
                cachePerContract.putIfAbsent(contractsKey, faceletCache);
                faceletCache = cachePerContract.get(contractsKey);
            }
            return faceletCache;
        }
        return this.cache;
    }

    private URL resolveURL(String uri) throws IOException {
        // PENDING(FCAPUTO) Deactivated caching for resource library contracts. If we still want to cache it, we need a cache per contract libraries list.
        //         But the ResourceHandler caches on his own (using ResourceManager).
        URL url = this.resolveURL(this.baseUrl, uri);
        if (url == null) {
            throw new IOException("'" + uri + "' not found.");
        }
        return url;
    }

    public UIComponent _createComponent(FacesContext context, String taglibURI, String tagName, 
    Map<String, Object> attributes) {
        // PENDING(FCAPUTO) does this work for resource library contracts? I think so.
        UIComponent result = null;
        Application app = context.getApplication();
        ExternalContext extContext = context.getExternalContext();
        File tmpDir = (File) extContext.getApplicationMap().get("javax.servlet.context.tempdir");
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
            if (null != attributes && !attributes.isEmpty()) {
                for (Map.Entry<String,Object> attr : attributes.entrySet()) {
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
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "Flushing and closing stream", ex);
                }
            }
                  
            URL fabricatedFaceletPage = tempFile.toURI().toURL();
            Facelet f = createFacelet(fabricatedFaceletPage);
            UIComponent tmp = (UIComponent)
                    app.createComponent("javax.faces.NamingContainer");
            tmp.setId(context.getViewRoot().createUniqueId());
            f.apply(context, tmp);
                result = tmp.findComponent(tempId);
            tmp.getChildren().clear();
            osw = null;
            
        } catch (MalformedURLException mue) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "Invalid URL", mue);
            }
        } catch (IOException ioe) {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "I/O error", ioe);
            }
        } finally {
            if (null != osw) {
                try {
                    osw.close();
                } catch (IOException ioe) {
                    if (log.isLoggable(Level.FINEST)) {
                        log.log(Level.FINEST, "Closing stream", ioe);
                    }
                }
            }
            if (null != tempFile) {
                boolean successful = tempFile.delete();
                if (!successful && log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "Unable to delete temporary file.");
                }
            }
        }
        
        try {
            byte [] faceletPage = "facelet".getBytes(RIConstants.CHAR_ENCODING);
            ByteArrayInputStream bais = new ByteArrayInputStream(faceletPage);
        } catch (UnsupportedEncodingException uee) {
            if (log.isLoggable(Level.SEVERE)) {
                log.log(Level.SEVERE, "Unsupported encoding when creating component for " + tagName + " in " + taglibURI,
                        uee);
            }
        }
              
        if (null != result) {
            result.setId(null);        
        }
        return result;
    }
    
    


    /**
     * Uses the internal Compiler reference to build a Facelet given the passed
     * URL.
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
        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating Facelet for: " + url);
        }
        String escapedBaseURL = Pattern.quote(this.baseUrl.getFile());
        String alias = '/' + url.getFile().replaceFirst(escapedBaseURL, "");
        try {
            FaceletHandler h = this.compiler.compile(url, alias);
            return new DefaultFacelet(this,
                                      this.compiler.createExpressionFactory(),
                                      url,
                                      alias,
                                      h);
        } catch (FileNotFoundException fnfe) {
            throw new FileNotFoundException("Facelet "
                                            + alias
                                            + " not found at: "
                                            + url.toExternalForm());
        }
    }

    private DefaultFacelet createMetadataFacelet(URL url) throws IOException {

        if (log.isLoggable(Level.FINE)) {
            log.fine("Creating Metadata Facelet for: " + url);
        }
        String escapedBaseURL = Pattern.quote(this.baseUrl.getFile());
        String alias = '/' + url.getFile().replaceFirst(escapedBaseURL, "");
        try {
            FaceletHandler h = this.compiler.metadataCompile(url, alias);
            return new DefaultFacelet(this,
                                      this.compiler.createExpressionFactory(),
                                      url,
                                      alias,
                                      h);
        } catch (FileNotFoundException fnfe) {
            throw new FileNotFoundException("Facelet "
                                            + alias
                                            + " not found at: "
                                            + url.toExternalForm());
        }

    }


    public long getRefreshPeriod() {
        return this.refreshPeriod;
    }


    // ---------------------------------------------------------- Nested Classes


    private static final class IdMapperFactory implements Cache.Factory<String,IdMapper> {


        // ------------------------------------------ Methods from Cache.Factory


        @Override
        public IdMapper newInstance(String arg) throws InterruptedException {

            return new IdMapper();

        }

    }    
    
}
