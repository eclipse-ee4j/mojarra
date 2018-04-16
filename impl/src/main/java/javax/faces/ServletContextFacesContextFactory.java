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

package javax.faces;


import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.servlet.ServletContext;

/*
 * Bug 20458755
 
 * This class provides a utility method to look up the current FacesContext
 * without performing the additional check introduced in FacesContext.getCurrentInstance()
 * by this bug fix.

 * This class also is a FacesContextFactory implementation that ignores 
 * all arguments and is able to look up the FacesContext corresponding
 * to the ServletContext corresponding to the current Thread context ClassLoader.

 * This FacesContextFactory implementation is used by FacesContext.getCurrentInstance()
 * so that the init FacesContext can be correctly looked up regardless of 
 * thread re-use.
 */
final class ServletContextFacesContextFactory extends FacesContextFactory {
    
    static final String SERVLET_CONTEXT_FINDER_NAME = "com.sun.faces.ServletContextFacesContextFactory";
    static final String SERVLET_CONTEXT_FINDER_REMOVAL_NAME = "com.sun.faces.ServletContextFacesContextFactory_Removal";

    private static final Logger LOGGER = Logger.getLogger("javax.faces", "javax.faces.LogStrings");

    private ThreadLocal<FacesContext> facesContextCurrentInstance;
    private ConcurrentHashMap<Thread, FacesContext> facesContextThreadInitContextMap;
    private ConcurrentHashMap<FacesContext, ServletContext> initContextServletContextMap;

    @SuppressWarnings("unchecked")
    ServletContextFacesContextFactory() {
        try {
            Field instanceField = FacesContext.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            facesContextCurrentInstance = (ThreadLocal<FacesContext>) instanceField.get(null);

            Field threadInitContextMapField = FacesContext.class.getDeclaredField("threadInitContext");
            threadInitContextMapField.setAccessible(true);
            facesContextThreadInitContextMap = (ConcurrentHashMap<Thread, FacesContext>) threadInitContextMapField.get(null);

            Field initContextServletContextMapField = FacesContext.class.getDeclaredField("initContextServletContext");
            initContextServletContextMapField.setAccessible(true);
            initContextServletContextMap = (ConcurrentHashMap<FacesContext, ServletContext>) initContextServletContextMapField.get(null);

        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Unable to access instance field of FacesContext", ex);
            }
        }
    }

    /**
     * This method does what FacesContext.getCurrentInstance() did *before* the fix for Bug
     * 20458755.
     */
    FacesContext getFacesContextWithoutServletContextLookup() {
        FacesContext result = facesContextCurrentInstance.get();
        
        if (result == null) {
            if (facesContextThreadInitContextMap != null) {
                result = facesContextThreadInitContextMap.get(Thread.currentThread());
            }
        }
        
        return result;
    }

    /**
     * Consult the initContextServletContextMap (reflectively obtained from the FacesContext in our
     * ctor). If it is non-empty, obtain the ServletContext corresponding to the current Thread's
     * context ClassLoader. If found, use the initContextServletContextMap to find the FacesContext
     * corresponding to that ServletContext.
     */
    @Override
    public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle) throws FacesException {
        FacesContext result = null;
        
        if (initContextServletContextMap != null && !initContextServletContextMap.isEmpty()) {
            
            // Obtain the ServletContext corresponding to the current Thread's context ClassLoader
            ServletContext servletContext = (ServletContext) FactoryFinder.FACTORIES_CACHE.getServletContextForCurrentClassLoader();
            
            if (servletContext != null) {
                
                // ServletContext found. Use the initContextServletContextMap to find the FacesContext corresponding 
                // to this ServletContext.
                
                for (Entry<FacesContext, ServletContext> entry : initContextServletContextMap.entrySet()) {
                    if (servletContext.equals(entry.getValue())) {
                        result = entry.getKey();
                        break;
                    }
                }
            }
        }

        return result;
    }

}
