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

// RestoreViewPhase.java

package com.sun.faces.lifecycle;

import static com.sun.faces.util.MessageUtils.NULL_CONTEXT_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.NULL_REQUEST_VIEW_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.RESTORE_VIEW_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.Util.getViewHandler;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.MethodExpression;
import javax.faces.FacesException;
import javax.faces.application.ProtectedViewException;
import javax.faces.application.ViewExpiredException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.event.PostRestoreStateEvent;
import javax.faces.flow.FlowHandler;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;
import static com.sun.faces.util.Util.isOneOf;

/**
 * <B>Lifetime And Scope</B> <P> Same lifetime and scope as
 * DefaultLifecycleImpl.
 *
 */

public class RestoreViewPhase extends Phase {

    private static final String WEBAPP_ERROR_PAGE_MARKER =
            "javax.servlet.error.message";

    private static final Logger LOGGER = FacesLogger.LIFECYCLE.getLogger();

    private WebConfiguration webConfig;

    private static String SKIP_ITERATION_HINT =
        "javax.faces.visit.SKIP_ITERATION";


    // ---------------------------------------------------------- Public Methods


    @Override
    public PhaseId getId() {

        return PhaseId.RESTORE_VIEW;

    }


    @Override
    public void doPhase(FacesContext context,
                        Lifecycle lifecycle,
                        ListIterator<PhaseListener> listeners) {

        Util.getViewHandler(context).initView(context);
        super.doPhase(context, lifecycle, listeners);

        // Notify View Root after phase listener (if registered)
        notifyAfter(context, lifecycle);
    }

    /**
     * PRECONDITION: the necessary factories have been installed in the
     * ServletContext attr set. <P>
     * <p/>
     * POSTCONDITION: The facesContext has been initialized with a tree.
     */

    @Override
    public void execute(FacesContext facesContext) throws FacesException {

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine("Entering RestoreViewPhase");
        }
        if (facesContext == null) {
            throw new FacesException(MessageUtils.getExceptionMessageString(NULL_CONTEXT_ERROR_MESSAGE_ID));
        }
        
        // If an app had explicitely set the tree in the context, use that;
        
        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot != null) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.fine("Found a pre created view in FacesContext");
            }
            facesContext.getViewRoot().setLocale(facesContext.getExternalContext().getRequestLocale());

            // do per-component actions
            deliverPostRestoreStateEvent(facesContext);

            if (!facesContext.isPostback()) {
                facesContext.renderResponse();
            }
            return;
        }
        FacesException thrownException = null;

        try {

            // Reconstitute or create the request tree
            Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();
            String viewId = (String) requestMap.get("javax.servlet.include.path_info");
            
            if (viewId == null) {
                viewId = facesContext.getExternalContext().getRequestPathInfo();
            }

            // It could be that this request was mapped using a prefix mapping in which case there would be no
            // path_info. Query the servlet path.
            if (viewId == null) {
                viewId = (String) requestMap.get("javax.servlet.include.servlet_path");
            }

            if (viewId == null) {
                viewId = facesContext.getExternalContext().getRequestServletPath();
            }

            if (viewId == null) {
                throw new FacesException(MessageUtils.getExceptionMessageString(NULL_REQUEST_VIEW_ERROR_MESSAGE_ID));
            }

            ViewHandler viewHandler = getViewHandler(facesContext);

            if (facesContext.isPostback() && !isErrorPage(facesContext)) {
                facesContext.setProcessingEvents(false);
                // try to restore the view
                viewRoot = viewHandler.restoreView(facesContext, viewId);
                if (viewRoot == null) {
                    if (is11CompatEnabled(facesContext)) {
                        // 1.1 -> create a new view and flag that the response should
                        //        be immediately rendered
                        if (LOGGER.isLoggable(FINE)) {
                            LOGGER.fine("Postback: recreating a view for " + viewId);
                        }
                        viewRoot = viewHandler.createView(facesContext, viewId);
                        facesContext.renderResponse();

                    } else {
                        Object[] params = {viewId};
                        throw new ViewExpiredException(
                            getExceptionMessageString(RESTORE_VIEW_ERROR_MESSAGE_ID, params),
                            viewId);
                    }
                }

                facesContext.setViewRoot(viewRoot);
                facesContext.setProcessingEvents(true);
                
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.fine("Postback: restored view for " + viewId);
                }
            } else {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.fine("New request: creating a view for " + viewId);
                }

                String logicalViewId = viewHandler.deriveLogicalViewId(facesContext, viewId);
                ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(facesContext, logicalViewId);
                
                maybeTakeProtectedViewAction(facesContext, viewHandler, vdl, logicalViewId);
                    
                ViewMetadata metadata  = null;
                if (vdl != null) {
                    // If we have one, get the ViewMetadata...
                    metadata = vdl.getViewMetadata(facesContext, logicalViewId);
                    
                    if (metadata != null) { // perhaps it's not supported
                        // and use it to create the ViewRoot.  This will have, at most
                        // the UIViewRoot and its metadata facet.
                        viewRoot = metadata.createMetadataView(facesContext);
                        
                        // Only skip to render response if there is no metadata
                        if (!ViewMetadata.hasMetadata(viewRoot)) {
                            facesContext.renderResponse();
                        }
                    }
                }
                
                if (vdl == null || metadata == null) {
                    facesContext.renderResponse();
                }

                if (viewRoot == null) {
                    viewRoot = getViewHandler(facesContext).createView(facesContext, logicalViewId);
                }
                facesContext.setViewRoot(viewRoot);
                
                assert (viewRoot != null);
            }
        } catch (Throwable fe) {
            if (fe instanceof FacesException) {
              thrownException = (FacesException) fe;
            } else {
              thrownException = new FacesException(fe);
            }
        } finally {
            if (thrownException == null) {
                FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
                if (flowHandler != null) {
                    flowHandler.clientWindowTransition(facesContext);
                }
                
                deliverPostRestoreStateEvent(facesContext);
            } else {
                throw thrownException;
            }
        }

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine("Exiting RestoreViewPhase");
        }
    }
    
    private void maybeTakeProtectedViewAction(FacesContext context, ViewHandler viewHandler, ViewDeclarationLanguage vdl, String viewId) {
        // http://java.net/jira/browse/JAVASERVERFACES-2204
        // PENDING: this code is optimized to be fast to write.
        // It must be optimized to be fast to run.
        
        // See git clone ssh://edburns@git.java.net/grizzly~git 1_9_36 for
        // how grizzly does this.
        
        Set<String> urlPatterns = viewHandler.getProtectedViewsUnmodifiable();
        // Implement section 12.1 of the Servlet spec.
        boolean currentViewIsProtected = isProtectedView(viewId, urlPatterns);       
        if (currentViewIsProtected) {
            ExternalContext extContext = context.getExternalContext();
            Map<String, String> headers = extContext.getRequestHeaderMap();
            
            //Check the token
            String rkId = viewHandler.calculateRenderKitId(context);
            ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(context, rkId);
            String incomingSecretKeyValue = extContext.getRequestParameterMap().
                get(ResponseStateManager.NON_POSTBACK_VIEW_TOKEN_PARAM);
            if (null != incomingSecretKeyValue) {
                try {
                    incomingSecretKeyValue = URLEncoder.encode(incomingSecretKeyValue, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, "Unable to re-encode value of request parameter " + 
                               ResponseStateManager.NON_POSTBACK_VIEW_TOKEN_PARAM + ":" + 
                                incomingSecretKeyValue, e);
                    }
                  incomingSecretKeyValue = null;
                }
           }
            
            String correctSecretKeyValue = rsm.getCryptographicallyStrongTokenFromSession(context);
            if (null == incomingSecretKeyValue || 
                !correctSecretKeyValue.equals(incomingSecretKeyValue)) {
                LOGGER.log(Level.SEVERE, "correctSecretKeyValue = {0} incomingSecretKeyValue = {1}", new Object[]{correctSecretKeyValue, incomingSecretKeyValue});
                throw new ProtectedViewException();
            }

            // Check the referer header
            if (headers.containsKey("Referer")) {
                String referer = headers.get("Referer");
                boolean refererIsInProtectedSet = isProtectedView(referer, urlPatterns);
                if (!refererIsInProtectedSet) {
                    boolean refererOriginatesInThisWebapp = false;
                    try {
                        refererOriginatesInThisWebapp = originatesInWebapp(context, referer, vdl);
                    } catch(URISyntaxException ue) {
                        throw new ProtectedViewException(ue);    
                    }
                    if (!refererOriginatesInThisWebapp) {
                        String message = FacesLogger.LIFECYCLE.interpolateMessage(context, 
                            "jsf.lifecycle.invalid.referer", new String [] { referer, viewId });
                        if (LOGGER.isLoggable(SEVERE)) {
                            LOGGER.log(SEVERE, message);
                        }
                        throw new ProtectedViewException(message);                
                    }
                }
            }
            // Check the origin header
            if (headers.containsKey("Origin")) {
                String origin = headers.get("Origin");
                boolean originIsInProtectedSet = isProtectedView(origin, urlPatterns);
                if (!originIsInProtectedSet) {
                    boolean originOriginatesInThisWebapp = false;
                    try {
                        originOriginatesInThisWebapp = originatesInWebapp(context, origin, vdl);
                    } catch(URISyntaxException ue) {
                        throw new ProtectedViewException(ue);    
                    }
                    if (!originOriginatesInThisWebapp) {
                        String message = FacesLogger.LIFECYCLE.interpolateMessage(context, 
                            "jsf.lifecycle.invalid.origin", new String [] { origin, viewId });
                        if (LOGGER.isLoggable(SEVERE)) {
                            LOGGER.log(Level.SEVERE, message);
                        }
                        throw new ProtectedViewException(message);                
                    }
                }
            }
        }
    }
    
    private boolean isProtectedView(String viewToCheck, Set<String> urlPatterns) {
        boolean isProtected = false;
        for (String urlPattern : urlPatterns) {
            if (urlPattern.equals(viewToCheck)) {
                isProtected = true;
                break;
            }
        }   
        
        return isProtected;
    }
    
    private boolean originatesInWebapp(FacesContext context, String view, ViewDeclarationLanguage vdl) throws URISyntaxException {
        boolean doesOriginate = false;
        ExternalContext extContext = context.getExternalContext();
        String sep = "/";
        URI uri = null;
        String path = null;
        boolean isAbsoluteURI = view.matches("^[a-z]+://.*");
        if (!isAbsoluteURI) {
            URI absoluteURI = null;
            URI relativeURI = null;
            String base = extContext.getRequestScheme() + ":" +
                sep + sep + extContext.getRequestServerName() +
                ":" + extContext.getRequestServerPort();
                absoluteURI = new URI(base);
                relativeURI = new URI(view);
                uri = absoluteURI.resolve(relativeURI);  
        }
        boolean hostsMatch = false,
            portsMatch = false,
            contextPathsMatch = false;
            
        if (null == uri) {
            uri = new URI(view);
        }
        if (null == uri.getHost()) {
            hostsMatch = false;
        } else {
            hostsMatch = uri.getHost().equals(extContext.getRequestServerName());    
        }
        if (-1 == uri.getPort()) {
            //When running on default http/https ports the uri will not contain the port number
            // to verify run test-javaee7-protectedView.war on port 80
        	portsMatch = isOneOf(extContext.getRequestServerPort(), 80, 443);
        } else {
            portsMatch = uri.getPort() == extContext.getRequestServerPort();
        }
        path = uri.getPath();
        contextPathsMatch = path.contains(extContext.getApplicationContextPath());
            
        doesOriginate = hostsMatch && portsMatch && contextPathsMatch;
        
        if (!doesOriginate) {
            // Last chance view originates in this web app.
            int idx = path.lastIndexOf(sep);
            if (-1 != idx) {
                path = path.substring(idx);
            } 
            if (null == path || !vdl.viewExists(context, path)) {
                doesOriginate = false;
            }    else {
                doesOriginate = true;
            }
        }
        return doesOriginate;      
    }
    
    private void deliverPostRestoreStateEvent(FacesContext facesContext) throws FacesException {
        UIViewRoot root = facesContext.getViewRoot();
        final PostRestoreStateEvent postRestoreStateEvent = new PostRestoreStateEvent(root);
        try {
            // PENDING: This is included for those component frameworks that don't utilize the
            // new VisitHint(s) yet - but still wish to know that they should be non-iterating
            // during state saving.  It should be removed at some point.
            facesContext.getAttributes().put(SKIP_ITERATION_HINT, true);
            facesContext.getApplication().publishEvent(facesContext, PostRestoreStateEvent.class, root);

            Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
            VisitContext visitContext = VisitContext.createVisitContext(facesContext, null, hints);
            root.visitTree(visitContext, new VisitCallback() {

                        @Override
                        public VisitResult visit(VisitContext context, UIComponent target) {
                            postRestoreStateEvent.setComponent(target);
                            target.processEvent(postRestoreStateEvent);
                            //noinspection ReturnInsideFinallyBlock
                            return VisitResult.ACCEPT;
                        }
                    });
        } catch (AbortProcessingException e) {
            facesContext.getApplication().publishEvent(facesContext,
                    ExceptionQueuedEvent.class,
                    new ExceptionQueuedEventContext(facesContext,
                    e,
                    null,
                    PhaseId.RESTORE_VIEW));
        } finally {
            // PENDING: This is included for those component frameworks that don't utilize the
            // new VisitHint(s) yet - but still wish to know that they should be non-iterating
            // during state saving.  It should be removed at some point.
            facesContext.getAttributes().remove(SKIP_ITERATION_HINT);
        }
    }

    // --------------------------------------------------------- Private Methods

    /**
     * Notify afterPhase listener that is registered on the View Root.
     * @param context the FacesContext for the current request
     * @param lifecycle lifecycle instance
     */
    private void notifyAfter(FacesContext context, Lifecycle lifecycle) {
        UIViewRoot viewRoot = context.getViewRoot();
        if (null == viewRoot) {
            return;
        }
        MethodExpression afterPhase = viewRoot.getAfterPhaseListener();
        if (null != afterPhase) {
            try {
                PhaseEvent event = new PhaseEvent(context, PhaseId.RESTORE_VIEW, lifecycle);
                afterPhase.invoke(context.getELContext(), new Object[]{event});
            }
            catch (Exception e) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE,
                               "severe.component.unable_to_process_expression",
                               new Object[] { afterPhase.getExpressionString(),
                               ("afterPhase")});
                }
                return;
            }
        }
    }


    /**
     * The Servlet specification states that if an error occurs
     * in the application and there is a matching error-page declaration,
     * the that original request the cause the error is forwarded
     * to the error page.
     *
     * If the error occurred during a post-back and a matching
     * error-page definition was found, then an attempt to restore
     * the error view would be made as the javax.faces.ViewState
     * marker would still be in the request parameters.
     *
     * Use this method to determine if the current request is
     * an error page to avoid the above condition.
     *
     * @param context the FacesContext for the current request
     * @return <code>true</code> if <code>WEBAPP_ERROR_PAGE_MARKER</code>
     *  is found in the request, otherwise return <code>false</code>
     */
    private static boolean isErrorPage(FacesContext context) {

        return (context.getExternalContext().
                    getRequestMap().get(WEBAPP_ERROR_PAGE_MARKER) != null);

    }


    private WebConfiguration getWebConfig(FacesContext context) {

        if (webConfig == null) {
            webConfig = WebConfiguration.getInstance(context.getExternalContext());
        }
        return webConfig;

    }

    private boolean is11CompatEnabled(FacesContext context) {

        return (getWebConfig(context).isOptionEnabled(
              BooleanWebContextInitParameter.EnableRestoreView11Compatibility));
        
    }

    // The testcase for this class is TestRestoreViewPhase.java

} // end of class RestoreViewPhase
