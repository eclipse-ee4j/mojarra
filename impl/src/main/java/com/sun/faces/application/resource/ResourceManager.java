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

package com.sun.faces.application.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import javax.faces.application.ProjectStage;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceVisitOption;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

/**
 * This class is used to lookup {@link ResourceInfo} instances
 * and cache any that are successfully looked up to reduce the
 * computational overhead with the scanning/version checking.
 *
 * @since 2.0
 */
public class ResourceManager {

    private static final Logger LOGGER = FacesLogger.RESOURCE.getLogger();

    /**
     * {@link Pattern} for valid mime types to configure compression.
     */
    private static final Pattern CONFIG_MIMETYPE_PATTERN = Pattern.compile("[a-z-]*/[a-z0-9.\\*-]*");
    
    private FaceletWebappResourceHelper faceletWebappResourceHelper = new FaceletWebappResourceHelper();

    /**
     * {@link ResourceHelper} used for looking up webapp-based resources.
     */
    private ResourceHelper webappResourceHelper = new WebappResourceHelper();

    /**
     * {@link ResourceHelper} used for looking up classpath-based resources.
     */
    private ClasspathResourceHelper classpathResourceHelper = new ClasspathResourceHelper();

    /**
     * Cache for storing {@link ResourceInfo} instances to reduce the cost
     * of the resource lookups.
     */
    private ResourceCache cache;

    /**
     * Patterns used to find {@link ResourceInfo} instances that may have their
     * content compressed.
     */
    private List<Pattern> compressableTypes;

    /**
     * This lock is used to ensure the lookup of compressable {@link ResourceInfo}
     * instances are atomic to prevent theading issues when writing the compressed
     * content during a lookup.
     */
    private ReentrantLock lock = new ReentrantLock();


    // ------------------------------------------------------------ Constructors

    /*
     * This ctor is only ever called by test code.
     */

    public ResourceManager(ResourceCache cache) {
        this.cache = cache;
        Map<String, Object> throwAwayMap = new HashMap<>();
        initCompressableTypes(throwAwayMap);
    }

    /**
     * Constructs a new <code>ResourceManager</code>.  Note:  if the current
     * {@link ProjectStage} is {@link ProjectStage#Development} caching or
     * {@link ResourceInfo} instances will not occur.
     */
    public ResourceManager(Map<String, Object> appMap, ResourceCache cache) {
        this.cache = cache;
        initCompressableTypes(appMap);
    }


    // ------------------------------------------------------ Public Methods


    /**
     * <p>
     * Attempt to lookup a {@link ResourceInfo} based on the specified
     * <code>libraryName<code> and <code>resourceName</code>
     * </p>
     *
     * <p>
     * Implementation Note:  Synchronization is necessary when looking up
     * compressed resources.  This ensures the atomicity of the content
     * being compressed.  As such, the cost of doing this is low as once
     * the resource is in the cache, the lookup won't be performed again
     * until the cache is cleared.  That said, it's not a good idea
     * to have caching disabled in a production environment if leveraging
     * compression.
     *
     * If the resource isn't compressable, then we don't worry about creating
     * a few extra copies of ResourceInfo until the cache is populated.
     * </p>
     *
     * @param libraryName the name of the library (if any)
     * @param resourceName the name of the resource
     * @param contentType the content type of the resource.  This will be
     *  used to determine if the resource is compressable
     * @param ctx the {@link javax.faces.context.FacesContext} for the current
     *  request
     *  
     * @return a {@link ResourceInfo} if a resource if found matching the
     *  provided arguments, otherwise, return <code>null</code>
     */
    public ResourceInfo findResource(String libraryName, String resourceName, String contentType, FacesContext ctx) {
        return findResource(libraryName, resourceName, contentType, false, ctx);
    }
    
    public ResourceInfo findViewResource(String resourceName, String contentType, FacesContext facesContext) {
        String localePrefix = getLocalePrefix(facesContext);
        List<String> contracts = getResourceLibraryContracts(facesContext);
        
        ResourceInfo info = getFromCache(resourceName, null, localePrefix, contracts);
        
        if (info == null) {
            if (isCompressable(contentType, facesContext)) {
                info = findResourceCompressed(null, resourceName, true, localePrefix, contracts, facesContext);
            } else {
               info = findResourceNonCompressed(null, resourceName, true, localePrefix, contracts, facesContext);
            }
        }

        return info;
    }
    
    public ResourceInfo findResource(String libraryName, String resourceName, String contentType, boolean isViewResource, FacesContext ctx) {
        
        String localePrefix = getLocalePrefix(ctx);
        List<String> contracts = getResourceLibraryContracts(ctx);
        
        ResourceInfo info = getFromCache(resourceName, libraryName, localePrefix, contracts);
        
        if (info == null) {
            if (isCompressable(contentType, ctx)) {
                info = findResourceCompressed(libraryName, resourceName, isViewResource, localePrefix, contracts, ctx);
            } else {
               info = findResourceNonCompressed(libraryName, resourceName, isViewResource, localePrefix, contracts, ctx);
            }
        }

        return info;
    }
    
    public Stream<String> getViewResources(FacesContext facesContext, String path, int maxDepth, ResourceVisitOption... options) {
        return faceletWebappResourceHelper.getViewResources(facesContext, path, maxDepth, options);
    }


    // ----------------------------------------------------- Private Methods

    private ResourceInfo findResourceCompressed(String libraryName, String resourceName, boolean isViewResource, String localePrefix, List<String> contracts, FacesContext ctx) {
        
        ResourceInfo info = null;
        
        lock.lock();
        try {
            info = getFromCache(resourceName, libraryName, localePrefix, contracts);
            if (info == null) {
                info = doLookup(libraryName, resourceName, localePrefix, true, isViewResource, contracts, ctx);
                if (info != null) {
                    addToCache(info, contracts);
                }
            }
        } finally {
            lock.unlock();
        }
        
        return info;
    }
    
    private ResourceInfo findResourceNonCompressed(String libraryName, String resourceName, boolean isViewResource, String localePrefix, List<String> contracts, FacesContext ctx) {
        ResourceInfo info = doLookup(libraryName, resourceName, localePrefix, false, isViewResource, contracts, ctx);

        if (info == null && contracts != null) {
            info = doLookup(libraryNameFromContracts(libraryName, contracts), resourceName, localePrefix, false, isViewResource, contracts, ctx);
        }
        
        if (info != null && !info.isDoNotCache()) {
            addToCache(info, contracts);
        }
        
        return info;
    }
    
    private String libraryNameFromContracts(String libraryName, List<String> contracts) {
        // If the library name is equal to one of the contracts,
        // assume the resource to be found is within that contract
        for (String contract : contracts) {
            if (contract.equals(libraryName)) {
                return null;
            }
        }
        
        return libraryName;
    }
    

    /**
     * Attempt to look up the Resource based on the provided details.
     *
     * @param libraryName the name of the library (if any)
     * @param resourceName the name of the resource
     * @param localePrefix the locale prefix for this resource (if any)
     * @param compressable if this resource can be compressed
     * @param isViewResource 
     * @param contracts the contracts to consider
     * @param ctx the {@link javax.faces.context.FacesContext} for the current
     *  request
     *
     * @return a {@link ResourceInfo} if a resource if found matching the
     *  provided arguments, otherwise, return <code>null</code>
     */
    private ResourceInfo doLookup(String libraryName, String resourceName, String localePrefix, boolean compressable, boolean isViewResource, List<String> contracts, FacesContext ctx) {
        
        // Loop over the contracts as described in deriveResourceIdConsideringLocalePrefixAndContracts in the spec
        for (String contract : contracts) {
            ResourceInfo info = getResourceInfo(libraryName, resourceName, localePrefix, contract, compressable, isViewResource, ctx, null);
            if (info != null) {
                return info;
            }
        }
        
        return getResourceInfo(libraryName, resourceName, localePrefix, null, compressable, isViewResource, ctx, null);
    }

    private ResourceInfo getResourceInfo(String libraryName, String resourceName, String localePrefix, String contract, boolean compressable, boolean isViewResource, FacesContext ctx, LibraryInfo library) {
        if (libraryName != null && !nameContainsForbiddenSequence(libraryName)) {
            library = findLibrary(libraryName, localePrefix, contract, ctx);
            
            if (library == null && localePrefix != null) {
                // no localized library found.  Try to find a library that isn't localized.
                library = findLibrary(libraryName, null, contract, ctx);
            }
            
            if (library == null) {
                // If we don't have one by now, perhaps it's time to consider scanning directories.
                library = findLibraryOnClasspathWithZipDirectoryEntryScan(libraryName, localePrefix, contract, ctx, false);
                
                if (library == null && localePrefix != null) {
                    // no localized library found.  Try to find
                    // a library that isn't localized.
                    library = findLibraryOnClasspathWithZipDirectoryEntryScan(libraryName, null, contract, ctx, false);
                }
                
                if (library == null) {
                    return null;
                }
            }
        } else if (nameContainsForbiddenSequence(libraryName)) {
            return null;
        }

        String resName = trimLeadingSlash(resourceName);
        if (nameContainsForbiddenSequence(resName) || (!isViewResource && resName.startsWith("WEB-INF"))) {
            return null;
        }

        ResourceInfo info = findResource(library, resourceName, localePrefix, compressable, isViewResource, ctx);
        if (info == null && localePrefix != null) {
            // no localized resource found, try to find a
            // resource that isn't localized
            info = findResource(library, resourceName, null, compressable, isViewResource, ctx);
        }

        // If no resource has been found so far, and we have a library that
        // was found in the webapp filesystem, see if there is a matching
        // library on the classpath.  If one is found, try to find a matching
        // resource in that library.
        if (info == null && library != null && library.getHelper() instanceof WebappResourceHelper) {
            LibraryInfo altLibrary = classpathResourceHelper.findLibrary(libraryName, localePrefix, contract, ctx);
            if (altLibrary != null) {
                VersionInfo originalVersion = library.getVersion();
                VersionInfo altVersion = altLibrary.getVersion();
                if (originalVersion == null && altVersion == null) {
                    library = altLibrary;
                } else if (originalVersion == null && altVersion != null) {
                    library = null;
                } else if (originalVersion != null && altVersion == null) {
                    library = null;
                } else if (originalVersion.compareTo(altVersion) == 0) {
                    library = altLibrary;
                }

            }

            if (library != null) {
                info = findResource(library, resourceName, localePrefix, compressable, isViewResource, ctx);
                if (info == null && localePrefix != null) {
                    // no localized resource found, try to find a
                    // resource that isn't localized
                    info = findResource(library, resourceName, null, compressable, isViewResource, ctx);
                }
            }
        }
        
        return info;
    }

    /**
     * @param s input String
     * @return the String without a leading slash if it has one.
     */
    private String trimLeadingSlash(String s) {
        if (s.charAt(0) == '/') {
            return s.substring(1);
        } else {
            return s;
        }
    }
    
    private static boolean nameContainsForbiddenSequence(String name) {
        boolean result = false;
        if (name != null) {
            name = name.toLowerCase();
    
            result = name.startsWith(".") ||
                     name.contains("../") ||
                     name.contains("..\\") ||
                     name.startsWith("/") ||
                     name.startsWith("\\") ||
                     name.endsWith("/") ||
    
                     name.contains("..%2f") ||
                     name.contains("..%5c") ||
                     name.startsWith("%2f") ||
                     name.startsWith("%5c") ||
                     name.endsWith("%2f") ||
    
                     name.contains("..\\u002f") ||
                     name.contains("..\\u005c") ||
                     name.startsWith("\\u002f") ||
                     name.startsWith("\\u005c") ||
                     name.endsWith("\\u002f")
    
                    ;
        }
        
        return result;
    }


    /**
     *
     * @param name the resource name
     * @param library the library name
     * @param localePrefix the Locale prefix
     * @param contracts
     * @return the {@link ResourceInfo} from the cache or <code>null</code>
     *  if no cached entry is found
     */
    private ResourceInfo getFromCache(String name, String library, String localePrefix, List<String> contracts) {
        if (cache == null) {
            return null;
        }
        
        return cache.get(name, library, localePrefix, contracts);
    }


    /**
     * Adds the the specified {@link ResourceInfo} to the cache.
     * @param info the @{link ResourceInfo} to add.
     * @param contracts the contracts
     */
    private void addToCache(ResourceInfo info, List<String> contracts) {
        if (cache == null) {
            return;
        }
        
        cache.add(info, contracts);
    }

    /**
     * <p> Attempt to lookup and return a {@link LibraryInfo} based on the
     * specified <code>arguments</code>.
     * <p/>
     * <p> The lookup process will first search the file system of the web
     * application *within the resources directory*.  
     * If the library is not found, then it processed to
     * searching the classpath, if not found there, search from the webapp root
     * *excluding* the resources directory.</p>
     * <p/>
     * <p> If a library is found, this method will return a {@link
     * LibraryInfo} instance that contains the name, version, and {@link
     * ResourceHelper}.</p>
     *
     *
     * @param libraryName the library to find
     * @param localePrefix the prefix for the desired locale
     * @param contract the contract to use
     *@param ctx         the {@link javax.faces.context.FacesContext} for the current request
     *  @return the Library instance for the specified library
     */
     LibraryInfo findLibrary(String libraryName, String localePrefix, String contract, FacesContext ctx) {

        LibraryInfo library = webappResourceHelper.findLibrary(libraryName, localePrefix, contract, ctx);
        
        if (library == null) {
            library = classpathResourceHelper.findLibrary(libraryName, localePrefix, contract, ctx);
        }
        
        if (library == null && contract == null) {
            // FCAPUTO facelets in contracts should have been found by the webapphelper already
            library = faceletWebappResourceHelper.findLibrary(libraryName, localePrefix, contract, ctx);
        }

        // if not library is found at this point, let the caller deal with it
        return library;
    }

     LibraryInfo findLibraryOnClasspathWithZipDirectoryEntryScan(String libraryName,
                                                                 String localePrefix,
                                                                 String contract, FacesContext ctx, boolean forceScan) {
         return classpathResourceHelper.findLibraryWithZipDirectoryEntryScan(libraryName, localePrefix, contract, ctx, forceScan);
     }

   /**
     * <p> Attempt to lookup and return a {@link ResourceInfo} based on the
     * specified <code>arguments</code>.
     * <p/>
     * <p> The lookup process will first search the file system of the web
     * application.  If the library is not found, then it processed to
     * searching the classpath.</p>
     * <p/>
     * <p> If a library is found, this method will return a {@link
     * LibraryInfo} instance that contains the name, version, and {@link
     * ResourceHelper}.</p>
     *
     * @param library the library the resource should be found in
     * @param resourceName the name of the resource
     * @param localePrefix the prefix for the desired locale
     * @param compressable <code>true</code> if the resource can be compressed
     * @param ctx the {@link javax.faces.context.FacesContext} for the current request
     *
     * @return the Library instance for the specified library
     */
    private ResourceInfo findResource(LibraryInfo library,
                                      String resourceName,
                                      String localePrefix,
                                      boolean compressable,
                                      boolean skipToFaceletResourceHelper,
                                      FacesContext ctx) {

        if (library != null) {
            return library.getHelper().findResource(library,
                                                    resourceName,
                                                    localePrefix,
                                                    compressable,
                                                    ctx);
        } else {
            ResourceInfo resource = null;
            
            if (!skipToFaceletResourceHelper) {
                resource = webappResourceHelper.findResource(null,
                        resourceName,
                        localePrefix,
                        compressable,
                        ctx);
            }
            if (resource == null && !skipToFaceletResourceHelper) {
                resource = classpathResourceHelper.findResource(null,
                                                        resourceName,
                                                        localePrefix,
                                                        compressable, 
                                                        ctx);
            }
            if (resource == null) {
                resource = faceletWebappResourceHelper.findResource(library, 
                    resourceName, 
                    localePrefix, 
                    compressable, 
                    ctx);
            }
            return resource;
        }

    }
    
    ResourceInfo findResource(String resourceId) {
        // PENDING(fcaputo) do we need to handle contracts here?
        String libraryName = null;
        String resourceName = null;
        int end = 0, start = 0;
        if (-1 != (end = resourceId.lastIndexOf("/"))) {
            resourceName = resourceId.substring(end+1);
            if (-1 != (start = resourceId.lastIndexOf("/", end - 1))) {
                libraryName = resourceId.substring(start+1, end);
            } else {
                libraryName = resourceId.substring(0, end);
            }
        }
        FacesContext context = FacesContext.getCurrentInstance();
        LibraryInfo info = this.findLibrary(libraryName, null, null, context);
        ResourceInfo resourceInfo = this.findResource(info, resourceName, libraryName, true, false, context);
        
        return resourceInfo;
    }


    /**
     * <p>
     * Obtains the application configured message resources for the current
     * request locale.  If a ResourceBundle is found and contains the key
     * <code>javax.faces.resource.localePrefix</code>, use the value associated
     * with that key as the prefix for locale specific resources.
     * </p>
     *
     * <p>
     * For example, say the request locale is en_US, and
     * <code>javax.faces.resourceLocalePrefix</code> is found with a value of
     * <code>en</code>, a resource path within a web application might look like
     * <code>/resources/en/corp/images/greetings.jpg</code>
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @return the localePrefix based on the current request, or <code>null</code>
     *  if no prefix can be determined
     */
    private String getLocalePrefix(FacesContext context) {

        String localePrefix = null;
        
        localePrefix = context.getExternalContext().getRequestParameterMap().get("loc");
        
        if(localePrefix != null){
            return localePrefix;
        }
        
        String appBundleName = context.getApplication().getMessageBundle();
        if (null != appBundleName) {
        	
            Locale locale = null;
            if (context.getViewRoot() != null) {
                locale = context.getViewRoot().getLocale();
            } else {
                locale = context.getApplication().getViewHandler().calculateLocale(context);
            }
            
                try {
                    ResourceBundle appBundle =
                          ResourceBundle.getBundle(appBundleName,
                                                   locale,
                                                   Util.getCurrentLoader(ResourceManager.class));
                    localePrefix =
                          appBundle
                                .getString(ResourceHandler.LOCALE_PREFIX);
                } catch (MissingResourceException mre) { 
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Ignoring missing resource", mre);
                    }
                }
        }
        return localePrefix;

    }

    private List<String> getResourceLibraryContracts(FacesContext context) {
        UIViewRoot viewRoot = context.getViewRoot();
        if(viewRoot == null) {

            if(context.getApplication().getResourceHandler().isResourceRequest(context)) {
                // it is a resource request. look at the parameter con=.

                String param = context.getExternalContext().getRequestParameterMap().get("con");
                if(!nameContainsForbiddenSequence(param) && param != null && param.trim().length() > 0) {
                    return Arrays.asList(param);
                }
            }
            // PENDING(edburns): calculate the contracts!
            return Collections.emptyList();
        }
        return context.getResourceLibraryContracts();
    }


    /**
     * @param contentType content-type in question
     * @param ctx the @{link FacesContext} for the current request
     * @return <code>true</code> if this resource can be compressed, otherwise
     *  <code>false</code>
     */
    private boolean isCompressable(String contentType, FacesContext ctx) {

        // No compression when developing.
        if (contentType == null || ctx.isProjectStage(ProjectStage.Development)) {
            return false;
        } else {
            if (compressableTypes != null && !compressableTypes.isEmpty()) {
                for (Pattern p : compressableTypes) {
                    boolean matches = p.matcher(contentType).matches();
                    if (matches) {
                        return true;
                    }
                }
            }
        }

        return false;

    }


    /**
     * Init <code>compressableTypes</code> from the configuration.
     */
    private void initCompressableTypes(Map<String, Object> appMap) {

        WebConfiguration config = WebConfiguration.getInstance();
        String value = config.getOptionValue(WebConfiguration.WebContextInitParameter.CompressableMimeTypes);
        if (value != null && value.length() > 0) {
            String[] values = Util.split(appMap, value, ",");
            if (values != null) {
                for (String s : values) {
                    String pattern = s.trim();
                    if (!isPatternValid(pattern)) {
                        continue;
                    }
                    if (pattern.endsWith("/*")) {
                        pattern = pattern.substring(0, pattern.indexOf("/*"));
                        pattern += "/[a-z0-9.-]*";
                    }
                    if (compressableTypes == null) {
                        compressableTypes = new ArrayList<>(values.length);
                    }
                    try {
                        compressableTypes.add(Pattern.compile(pattern));
                    } catch (PatternSyntaxException pse) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            // PENDING i18n
                            LOGGER.log(Level.WARNING,
                                       "jsf.resource.mime.type.configration.invalid",
                                       new Object[] { pattern, pse.getPattern()});
                        }
                    }
                }
            }
        }

    }


    /**
     * @param input input mime-type pattern from the configuration
     * @return <code>true</code> if the input matches the expected pattern,
     *  otherwise <code>false</code>
     */
    private boolean isPatternValid(String input) {

        return (CONFIG_MIMETYPE_PATTERN.matcher(input).matches());

    }


}
