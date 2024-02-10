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

// RestoreViewPhase.java

package com.sun.faces.lifecycle;

import static com.sun.faces.renderkit.RenderKitUtils.getResponseStateManager;
import static com.sun.faces.util.MessageUtils.NULL_CONTEXT_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.NULL_REQUEST_VIEW_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.RESTORE_VIEW_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.Util.getViewHandler;
import static com.sun.faces.util.Util.isOneOf;
import static jakarta.faces.component.visit.VisitHint.SKIP_ITERATION;
import static jakarta.faces.event.PhaseId.RESTORE_VIEW;
import static jakarta.faces.render.ResponseStateManager.NON_POSTBACK_VIEW_TOKEN_PARAM;
import static jakarta.faces.view.ViewMetadata.hasMetadata;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.el.MethodExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.ProtectedViewException;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.event.PostRestoreStateEvent;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewMetadata;

/**
 * <B>Lifetime And Scope</B>
 * <P>
 * Same lifetime and scope as DefaultLifecycleImpl.
 *
 */
public class RestoreViewPhase extends Phase {

    private static final String WEBAPP_ERROR_PAGE_MARKER = "jakarta.servlet.error.message";
    private static final Logger LOGGER = FacesLogger.LIFECYCLE.getLogger();

    private static final Set<VisitHint> SKIP_ITERATION_HINT = EnumSet.of(SKIP_ITERATION);

    // ---------------------------------------------------------- Public Methods

    @Override
    public PhaseId getId() {
        return PhaseId.RESTORE_VIEW;
    }

    @Override
    public void doPhase(FacesContext context, Lifecycle lifecycle, ListIterator<PhaseListener> listeners) {
        Util.getViewHandler(context).initView(context);
        super.doPhase(context, lifecycle, listeners);

        // Notify View Root after phase listener (if registered)
        notifyAfter(context, lifecycle);
    }

    /**
     * PRECONDITION: the necessary factories have been installed in the ServletContext attr set.
     * <P>
     *
     * POSTCONDITION: The facesContext has been initialized with a tree.
     */

    @Override
    public void execute(FacesContext facesContext) throws FacesException {
        LOGGER.fine("Entering RestoreViewPhase");
        if (facesContext == null) {
            throw new FacesException(getExceptionMessageString(NULL_CONTEXT_ERROR_MESSAGE_ID));
        }

        // If an app had explicitely set the tree in the context, use that;

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot != null) {
            LOGGER.fine("Found a pre created view in FacesContext");
            facesContext.getViewRoot().setLocale(facesContext.getExternalContext().getRequestLocale());

            // Do per-component actions
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
            String viewId = (String) requestMap.get("jakarta.servlet.include.path_info");

            if (viewId == null) {
                viewId = facesContext.getExternalContext().getRequestPathInfo();
            }

            // It could be that this request was mapped using a prefix mapping in which case there would be no
            // path_info. Query the servlet path.
            if (viewId == null) {
                viewId = (String) requestMap.get("jakarta.servlet.include.servlet_path");
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
                    Object[] params = { viewId };
                    throw new ViewExpiredException(getExceptionMessageString(RESTORE_VIEW_ERROR_MESSAGE_ID, params), viewId);
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

                ViewMetadata metadata = null;
                if (vdl != null) {
                    // If we have one, get the ViewMetadata...
                    metadata = vdl.getViewMetadata(facesContext, logicalViewId);

                    if (metadata != null) { // perhaps it's not supported
                        // and use it to create the ViewRoot. This will have, at most
                        // the UIViewRoot and its metadata facet.
                        viewRoot = metadata.createMetadataView(facesContext);

                        // Only skip to render response if there is no metadata
                        if (!hasMetadata(viewRoot)) {
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

        LOGGER.fine("Exiting RestoreViewPhase");
    }

    private void maybeTakeProtectedViewAction(FacesContext context, ViewHandler viewHandler, ViewDeclarationLanguage vdl, String viewId) {
        // http://java.net/jira/browse/JAVASERVERFACES-2204
        // PENDING: this code is optimized to be fast to write.
        // It must be optimized to be fast to run.

        // See git clone ssh://edburns@git.java.net/grizzly~git 1_9_36 for
        // how grizzly does this.

        Set<String> urlPatterns = viewHandler.getProtectedViewsUnmodifiable();

        // Implement section 12.1 of the Servlet spec (consider using Jakarta Authorization, perhaps via SPI)
        if (isProtectedView(viewId, urlPatterns)) {
            ExternalContext extContext = context.getExternalContext();
            Map<String, String> headers = extContext.getRequestHeaderMap();

            // Check the token
            ResponseStateManager rsm = getResponseStateManager(context, viewHandler.calculateRenderKitId(context));

            String incomingSecretKeyValue = extContext.getRequestParameterMap().get(NON_POSTBACK_VIEW_TOKEN_PARAM);
            if (incomingSecretKeyValue != null) {
                incomingSecretKeyValue = URLEncoder.encode(incomingSecretKeyValue, UTF_8);
            }

            String correctSecretKeyValue = rsm.getCryptographicallyStrongTokenFromSession(context);
            if (incomingSecretKeyValue == null || !correctSecretKeyValue.equals(incomingSecretKeyValue)) {
                LOGGER.log(SEVERE, "correctSecretKeyValue = {0} incomingSecretKeyValue = {1}",
                        new Object[] { correctSecretKeyValue, incomingSecretKeyValue });
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
                    } catch (URISyntaxException ue) {
                        throw new ProtectedViewException(ue);
                    }

                    if (!refererOriginatesInThisWebapp) {
                        String message = FacesLogger.LIFECYCLE.interpolateMessage(context, "faces.lifecycle.invalid.referer", new String[] { referer, viewId });
                        LOGGER.log(SEVERE, message);
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
                    } catch (URISyntaxException ue) {
                        throw new ProtectedViewException(ue);
                    }

                    if (!originOriginatesInThisWebapp) {
                        String message = FacesLogger.LIFECYCLE.interpolateMessage(context, "faces.lifecycle.invalid.origin", new String[] { origin, viewId });
                        LOGGER.log(SEVERE, message);
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
            String base = extContext.getRequestScheme() + ":" + sep + sep + extContext.getRequestServerName() + ":" + extContext.getRequestServerPort();
            absoluteURI = new URI(base);
            relativeURI = new URI(view);
            uri = absoluteURI.resolve(relativeURI);
        }
        boolean hostsMatch = false, portsMatch = false, contextPathsMatch = false;

        if (uri == null) {
            uri = new URI(view);
        }

        if (uri.getHost() == null) {
            hostsMatch = false;
        } else {
            hostsMatch = uri.getHost().equals(extContext.getRequestServerName());
        }

        if (uri.getPort() == -1) {
            // When running on default http/https ports the uri will not contain the port number
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
            if (path == null || !vdl.viewExists(context, path)) {
                doesOriginate = false;
            } else {
                doesOriginate = true;
            }
        }

        return doesOriginate;
    }

    private void deliverPostRestoreStateEvent(FacesContext facesContext) throws FacesException {
        UIViewRoot root = facesContext.getViewRoot();
        PostRestoreStateEvent postRestoreStateEvent = new PostRestoreStateEvent(root);
        try {
            facesContext.getApplication().publishEvent(facesContext, PostRestoreStateEvent.class, root);
            VisitContext visitContext = VisitContext.createVisitContext(facesContext, null, SKIP_ITERATION_HINT);
            root.visitTree(visitContext, (context, target) -> {
                postRestoreStateEvent.setComponent(target);
                target.processEvent(postRestoreStateEvent);
                // noinspection ReturnInsideFinallyBlock
                return VisitResult.ACCEPT;
            });
        } catch (AbortProcessingException e) {
            facesContext.getApplication()
                        .publishEvent(
                            facesContext, ExceptionQueuedEvent.class,
                            new ExceptionQueuedEventContext(facesContext, e, null, PhaseId.RESTORE_VIEW));
        }
    }


    // --------------------------------------------------------- Private Methods

    /**
     * Notify afterPhase listener that is registered on the View Root.
     *
     * @param context the FacesContext for the current request
     * @param lifecycle lifecycle instance
     */
    private void notifyAfter(FacesContext context, Lifecycle lifecycle) {
        UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot == null) {
            return;
        }

        MethodExpression afterPhase = viewRoot.getAfterPhaseListener();
        if (afterPhase != null) {
            try {
                PhaseEvent event = new PhaseEvent(context, RESTORE_VIEW, lifecycle);
                afterPhase.invoke(context.getELContext(), new Object[] { event });
            } catch (Exception e) {
                if (LOGGER.isLoggable(SEVERE)) {
                    LOGGER.log(SEVERE, "severe.component.unable_to_process_expression",
                            new Object[] { afterPhase.getExpressionString(), "afterPhase" });
                }
                return;
            }
        }
    }

    /**
     * The Servlet specification states that if an error occurs in the application and there is a matching error-page
     * declaration, the that original request the cause the error is forwarded to the error page.
     *
     * If the error occurred during a post-back and a matching error-page definition was found, then an attempt to restore
     * the error view would be made as the jakarta.faces.ViewState marker would still be in the request parameters.
     *
     * Use this method to determine if the current request is an error page to avoid the above condition.
     *
     * @param context the FacesContext for the current request
     * @return <code>true</code> if <code>WEBAPP_ERROR_PAGE_MARKER</code> is found in the request, otherwise return
     * <code>false</code>
     */
    private static boolean isErrorPage(FacesContext context) {
        return context.getExternalContext().getRequestMap().get(WEBAPP_ERROR_PAGE_MARKER) != null;
    }

}
