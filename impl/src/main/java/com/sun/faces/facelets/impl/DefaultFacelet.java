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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.Doctype;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.FaceletHandler;

/**
 * Default Facelet implementation.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
final class DefaultFacelet extends Facelet implements XMLFrontMatterSaver {

    private static final Logger log = FacesLogger.FACELETS_FACELET.getLogger();

    private final static String APPLIED_KEY = "com.sun.faces.facelets.APPLIED";
    private static final String JAKARTA_FACES_ERROR_XHTML = "jakarta.faces.error.xhtml";

    private final String alias;

    private final ExpressionFactory elFactory;

    private final DefaultFaceletFactory factory;

    private final long createTime;

    private final long refreshPeriod;

    private final FaceletHandler root;

    private final URL src;

    private IdMapper mapper;

    private Doctype savedDoctype;

    private String savedXMLDecl;

    public DefaultFacelet(DefaultFaceletFactory factory, ExpressionFactory el, URL src, String alias, FaceletHandler root) {

        this.factory = factory;
        elFactory = el;
        this.src = src;
        this.root = root;
        this.alias = alias;
        this.mapper = factory.idMappers != null ? factory.idMappers.get(alias) : null;
        createTime = System.currentTimeMillis();
        refreshPeriod = this.factory.getRefreshPeriod();

        Doctype doctype = Util.getDOCTYPEFromFacesContextAttributes(FacesContext.getCurrentInstance());
        if (null != doctype) {
            // This will happen on the request that causes the facelets to be compiled
            setSavedDoctype(doctype);
        }

        String XMLDECL = Util.getXMLDECLFromFacesContextAttributes(FacesContext.getCurrentInstance());
        if (null != XMLDECL) {
            // This will happen on the request that causes the facelets to be compiled
            setSavedXMLDecl(XMLDECL);
        }

    }

    @Override
    public void applyMetadata(FacesContext facesContext, UIComponent parent) throws IOException {
        // Call apply, since a DefaultFacelet instance will be specifically created to only
        // hold the Metadata in advance.
        apply(facesContext, parent);
    }

    /**
     * @see jakarta.faces.view.facelets.Facelet#apply(jakarta.faces.context.FacesContext,
     *      jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FacesContext facesContext, UIComponent parent) throws IOException {

        IdMapper idMapper = IdMapper.getMapper(facesContext);
        boolean mapperSet = false;
        if (idMapper == null && this.mapper != null) {
            IdMapper.setMapper(facesContext, mapper);
            mapperSet = true;
        }

        DefaultFaceletContext ctx = new DefaultFaceletContext(facesContext, this);
        refresh(parent);
        ComponentSupport.markForDeletion(parent);
        root.apply(ctx, parent);
        ComponentSupport.finalizeForDeletion(parent);
        markApplied(parent);

        if (mapperSet) {
            IdMapper.setMapper(facesContext, null);
        }

    }

    private void refresh(UIComponent c) {
        if (refreshPeriod > 0) {

            // finally remove any children marked as deleted
            int sz = c.getChildCount();
            if (sz > 0) {
                List cl = c.getChildren();
                ApplyToken token;
                while (--sz >= 0) {
                    UIComponent cc = (UIComponent) cl.get(sz);
                    if (!cc.isTransient()) {
                        token = (ApplyToken) cc.getAttributes().get(APPLIED_KEY);
                        if (token != null && token.time < createTime && token.alias.equals(alias)) {
                            if (log.isLoggable(Level.INFO)) {
                                DateFormat df = SimpleDateFormat.getTimeInstance();
                                log.info("Facelet[" + alias + "] was modified @ " + df.format(new Date(createTime))
                                        + ", flushing component applied @ " + df.format(new Date(token.time)));
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
                        if (token != null && token.time < createTime && token.alias.equals(alias)) {
                            if (log.isLoggable(Level.INFO)) {
                                DateFormat df = SimpleDateFormat.getTimeInstance();
                                log.info("Facelet[" + alias + "] was modified @ " + df.format(new Date(createTime))
                                        + ", flushing component applied @ " + df.format(new Date(token.time)));
                            }
                            itr.remove();
                        }
                    }
                }
            }
        }
    }

    private void markApplied(UIComponent parent) {
        if (refreshPeriod > 0) {
            Iterator itr = parent.getFacetsAndChildren();
            ApplyToken token = new ApplyToken(alias, System.currentTimeMillis() + refreshPeriod);
            while (itr.hasNext()) {
                UIComponent c = (UIComponent) itr.next();
                if (!c.isTransient()) {
                    Map<String, Object> attr = c.getAttributes();
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
        return alias;
    }

    /**
     * Return this Facelet's ExpressionFactory instance
     *
     * @return internal ExpressionFactory instance
     */
    public ExpressionFactory getExpressionFactory() {
        return elFactory;
    }

    /**
     * The time when this Facelet was created, NOT the URL source code
     *
     * @return final timestamp of when this Facelet was created
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Delegates resolution to DefaultFaceletFactory reference. Also, caches URLs for absolute paths.
     *
     * @param relativePath a relative url path
     * @return URL pointing to destination
     * @throws IOException if there is a problem creating the URL for the path specified
     */
    private URL resolveURL(String relativePath) throws IOException {
        return factory.resolveURL(src, relativePath);
    }

    /**
     * The URL this Facelet was created from.
     *
     * @return the URL this Facelet was created from
     */
    public URL getSource() {
        return src;
    }

    /**
     * Given the passed FaceletContext, apply our child FaceletHandlers to the passed parent
     *
     * @see FaceletHandler#apply(FaceletContext, UIComponent)
     * @param ctx the FaceletContext to use for applying our FaceletHandlers
     * @param parent the parent component to apply changes to
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    private void include(DefaultFaceletContext ctx, UIComponent parent) throws IOException {
        refresh(parent);
        root.apply(new DefaultFaceletContext(ctx, this), parent);
        markApplied(parent);
    }

    /**
     * Used for delegation by the DefaultFaceletContext. First validates that the path does not represent
     * a contracts resource, then pulls the URL from {@link #resolveURL(String)}, then calls
     * {@link #include(DefaultFaceletContext, jakarta.faces.component.UIComponent, String)}.
     *
     * @see FaceletContext#includeFacelet(UIComponent, String)
     * @param ctx FaceletContext to pass to the included Facelet
     * @param parent UIComponent to apply changes to
     * @param relativePath relative path to the desired Facelet from the FaceletContext
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    public void include(DefaultFaceletContext ctx, UIComponent parent, String relativePath) throws IOException {
        URL url;
        if (relativePath.equals(JAKARTA_FACES_ERROR_XHTML)) {
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
            if (factory.isContractsResource(new URL(src, relativePath))) {
                throw new IOException("Contract resources cannot be accessed this way");
            }
            url = resolveURL(relativePath);
        }
        this.include(ctx, parent, url);
    }

    /**
     * Grabs a DefaultFacelet from referenced DefaultFaceletFacotry
     *
     * @see DefaultFaceletFactory#getFacelet(FacesContext,URL)
     * @param ctx FaceletContext to pass to the included Facelet
     * @param parent UIComponent to apply changes to
     * @param url URL source to include Facelet from
     * @throws IOException
     * @throws FacesException
     * @throws FaceletException
     * @throws ELException
     */
    public void include(DefaultFaceletContext ctx, UIComponent parent, URL url) throws IOException {
        DefaultFacelet f = (DefaultFacelet) factory.getFacelet(ctx.getFacesContext(), url);
        f.include(ctx, parent);
    }

    private static class ApplyToken implements Externalizable {
        public String alias;

        public long time;

        @SuppressWarnings({ "UnusedDeclaration" })
        public ApplyToken() {
        } // For Serialization

        public ApplyToken(String alias, long time) {
            this.alias = alias;
            this.time = time;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            alias = in.readUTF();
            time = in.readLong();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(alias);
            out.writeLong(time);
        }
    }

    @Override
    public String toString() {
        return alias;
    }

    // ---------------------------------------------------------- Helper Methods

    @Override
    public Doctype getSavedDoctype() {
        return savedDoctype;
    }

    @Override
    public void setSavedDoctype(Doctype savedDoctype) {
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
