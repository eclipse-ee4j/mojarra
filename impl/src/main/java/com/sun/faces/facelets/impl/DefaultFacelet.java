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

import javax.faces.view.facelets.Facelet;
import com.sun.faces.facelets.tag.jsf.ComponentSupport;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import javax.el.ExpressionFactory;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default Facelet implementation.
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
final class DefaultFacelet extends Facelet implements XMLFrontMatterSaver {

    private static final Logger log = FacesLogger.FACELETS_FACELET.getLogger();

    private final static String APPLIED_KEY = "com.sun.faces.facelets.APPLIED";
    private static final String JAVAX_FACES_ERROR_XHTML = "javax.faces.error.xhtml";

    private final String alias;

    private final ExpressionFactory elFactory;

    private final DefaultFaceletFactory factory;

    private final long createTime;

    private final long refreshPeriod;

    private final FaceletHandler root;

    private final URL src;

    private IdMapper mapper;
    
    private String savedDoctype;
    
    private String savedXMLDecl;

    public DefaultFacelet(DefaultFaceletFactory factory,
                          ExpressionFactory el,
                          URL src,
                          String alias,
                          FaceletHandler root) {

        this.factory = factory;
        this.elFactory = el;
        this.src = src;
        this.root = root;
        this.alias = alias;
        this.mapper = factory.idMappers != null? factory.idMappers.get(alias) : null;
        this.createTime = System.currentTimeMillis();
        this.refreshPeriod = this.factory.getRefreshPeriod();

        String DOCTYPE = Util.getDOCTYPEFromFacesContextAttributes(FacesContext.getCurrentInstance());
        if (null != DOCTYPE) {
            // This will happen on the request that causes the facelets to be compiled
            this.setSavedDoctype(DOCTYPE);
        }        

        String XMLDECL = Util.getXMLDECLFromFacesContextAttributes(FacesContext.getCurrentInstance());
        if (null != XMLDECL) {
            // This will happen on the request that causes the facelets to be compiled
            this.setSavedXMLDecl(XMLDECL);
        }        
        
    }

    /**
     * @see com.sun.faces.facelets.Facelet#apply(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
     */
    @Override
    public void apply(FacesContext facesContext, UIComponent parent)
        throws IOException {

        IdMapper idMapper = IdMapper.getMapper(facesContext);
        boolean mapperSet = false;
        if (idMapper == null && this.mapper != null) {
            IdMapper.setMapper(facesContext, this.mapper);
            mapperSet = true;
        }
        
        DefaultFaceletContext ctx = new DefaultFaceletContext(facesContext, this);
        this.refresh(parent);
        ComponentSupport.markForDeletion(parent);
        this.root.apply(ctx, parent);
        ComponentSupport.finalizeForDeletion(parent);
        this.markApplied(parent);

        if (mapperSet) {
            IdMapper.setMapper(facesContext, null);
        }


    }

    private void refresh(UIComponent c) {
        if (this.refreshPeriod > 0) {

            // finally remove any children marked as deleted
            int sz = c.getChildCount();
            if (sz > 0) {
                List cl = c.getChildren();
                ApplyToken token;
                while (--sz >= 0) {
                    UIComponent cc = (UIComponent) cl.get(sz);
                    if (!cc.isTransient()) {
                        token = (ApplyToken) cc.getAttributes().get(APPLIED_KEY);
                        if (token != null && token.time < this.createTime
                                && token.alias.equals(this.alias)) {
                            if (log.isLoggable(Level.INFO)) {
                                DateFormat df = SimpleDateFormat.getTimeInstance();
                                log.info("Facelet[" + this.alias
                                        + "] was modified @ "
                                        + df.format(new Date(this.createTime))
                                        + ", flushing component applied @ "
                                        + df.format(new Date(token.time)));
                            }
                            cl.remove(sz);
                        }
                    }
                }
            }

            // remove any facets marked as deleted
            if (c.getFacets().size() > 0) {
                Collection col = c.getFacets().values();
                UIComponent fc;
                ApplyToken token;
                for (Iterator itr = col.iterator(); itr.hasNext();) {
                    fc = (UIComponent) itr.next();
                    if (!fc.isTransient()) {
                        token = (ApplyToken) fc.getAttributes().get(APPLIED_KEY);
                        if (token != null && token.time < this.createTime
                                && token.alias.equals(this.alias)) {
                            if (log.isLoggable(Level.INFO)) {
                                DateFormat df = SimpleDateFormat.getTimeInstance();
                                log.info("Facelet[" + this.alias
                                        + "] was modified @ "
                                        + df.format(new Date(this.createTime))
                                        + ", flushing component applied @ "
                                        + df.format(new Date(token.time)));
                            }
                            itr.remove();
                        }
                    }
                }
            }
        }
    }

    private void markApplied(UIComponent parent) {
        if (this.refreshPeriod > 0) {
            Iterator itr = parent.getFacetsAndChildren();
            ApplyToken token =
                  new ApplyToken(this.alias,
                                 System.currentTimeMillis() + this.refreshPeriod);
            while (itr.hasNext()) {
                UIComponent c = (UIComponent) itr.next();
                if (!c.isTransient()) {
                    Map<String,Object> attr = c.getAttributes();
                    if (!attr.containsKey(APPLIED_KEY)) {
                        attr.put(APPLIED_KEY, token);
                    }
                }
            }
        }
    }

    /**
     * Return the alias name for error messages and logging
     * 
     * @return alias name
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Return this Facelet's ExpressionFactory instance
     * 
     * @return internal ExpressionFactory instance
     */
    public ExpressionFactory getExpressionFactory() {
        return this.elFactory;
    }

    /**
     * The time when this Facelet was created, NOT the URL source code
     * 
     * @return final timestamp of when this Facelet was created
     */
    public long getCreateTime() {
        return this.createTime;
    }

    /**
     * Delegates resolution to DefaultFaceletFactory reference. Also, caches
     * URLs for relative paths.
     * 
     * @param path
     *            a relative url path
     * @return URL pointing to destination
     * @throws IOException
     *             if there is a problem creating the URL for the path specified
     */
    private URL getRelativePath(String path) throws IOException {
        return this.factory.resolveURL(this.src, path);
    }

    /**
     * The URL this Facelet was created from.
     * 
     * @return the URL this Facelet was created from
     */
    public URL getSource() {
        return this.src;
    }

    /**
     * Given the passed FaceletContext, apply our child FaceletHandlers to the
     * passed parent
     * 
     * @see FaceletHandler#apply(FaceletContext, UIComponent)
     * @param ctx
     *            the FaceletContext to use for applying our FaceletHandlers
     * @param parent
     *            the parent component to apply changes to
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    private void include(DefaultFaceletContext ctx, UIComponent parent)
    throws IOException  {
        this.refresh(parent);
        this.root.apply(new DefaultFaceletContext(ctx, this), parent);
        this.markApplied(parent);
    }

    /**
     * Used for delegation by the DefaultFaceletContext. First pulls the URL
     * from {@link #getRelativePath(String) getRelativePath(String)}, then
     * calls
     * {@link #include(DefaultFaceletContext, javax.faces.component.UIComponent, String)}.
     * 
     * @see FaceletContext#includeFacelet(UIComponent, String)
     * @param ctx
     *            FaceletContext to pass to the included Facelet
     * @param parent
     *            UIComponent to apply changes to
     * @param path
     *            relative path to the desired Facelet from the FaceletContext
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    public void include(DefaultFaceletContext ctx, UIComponent parent, String path)
    throws IOException {
        URL url;
        if (path.equals(JAVAX_FACES_ERROR_XHTML)) {
            if (isDevelopment(ctx)) {
                // try using this class' ClassLoader
                url = getErrorFacelet(DefaultFacelet.class.getClassLoader());
                if (url == null) {
                    url = getErrorFacelet(Util.getCurrentLoader(this));
                }
            } else {
                return;
            }
        } else {
            url = this.getRelativePath(path);
        }
        this.include(ctx, parent, url);
    }

    /**
     * Grabs a DefaultFacelet from referenced DefaultFaceletFacotry
     * 
     * @see DefaultFaceletFactory#getFacelet(URL)
     * @param ctx
     *            FaceletContext to pass to the included Facelet
     * @param parent
     *            UIComponent to apply changes to
     * @param url
     *            URL source to include Facelet from
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    public void include(DefaultFaceletContext ctx, UIComponent parent, URL url)
    throws IOException {
        DefaultFacelet f = (DefaultFacelet) this.factory.getFacelet(ctx.getFacesContext(), url);
        f.include(ctx, parent);
    }

    private static class ApplyToken implements Externalizable {
        public String alias;

        public long time;

        @SuppressWarnings({"UnusedDeclaration"})
        public ApplyToken() { } // For Serialization

        public ApplyToken(String alias, long time) {
            this.alias = alias;
            this.time = time;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException,
                ClassNotFoundException {
            this.alias = in.readUTF();
            this.time = in.readLong();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(this.alias);
            out.writeLong(this.time);
        }
    }

    @Override
    public String toString() {
        return this.alias;
    }
    
    // ---------------------------------------------------------- Helper Methods

    @Override
    public String getSavedDoctype() {
        return savedDoctype;
    }

    @Override
    public void setSavedDoctype(String savedDoctype) {
        this.savedDoctype = savedDoctype;
    }

    @Override
    public String getSavedXMLDecl() {
        return savedXMLDecl;
    }

    @Override
    public void setSavedXMLDecl(String savedXMLDecl) {
        this.savedXMLDecl = savedXMLDecl;
    }
    
    
    
    // --------------------------------------------------------- Private Methods


    private boolean isDevelopment(FaceletContext ctx) {

        return ctx.getFacesContext().isProjectStage(ProjectStage.Development);

    }


    private URL getErrorFacelet(ClassLoader loader) {

        return loader.getResource("META-INF/error-include.xhtml");

    }
}
