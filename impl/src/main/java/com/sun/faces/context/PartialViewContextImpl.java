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

package com.sun.faces.context;

import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.PARTIAL_EXECUTE_PARAM;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.PARTIAL_RENDER_PARAM;
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.PARTIAL_RESET_VALUES_PARAM;
import static javax.faces.FactoryFinder.VISIT_CONTEXT_FACTORY;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.ResourceHandler;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitContextFactory;
import javax.faces.component.visit.VisitContextWrapper;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.PartialViewContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import javax.faces.lifecycle.ClientWindow;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;

import com.sun.faces.RIConstants;
import com.sun.faces.component.visit.PartialVisitContext;
import com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.HtmlUtils;
import com.sun.faces.util.Util;

 public class PartialViewContextImpl extends PartialViewContext {

    // Log instance for this class
    private static Logger LOGGER = FacesLogger.CONTEXT.getLogger();

    private boolean released;

    // BE SURE TO ADD NEW IVARS TO THE RELEASE METHOD
    private PartialResponseWriter partialResponseWriter;
    private List<String> executeIds;
    private Collection<String> renderIds;
    private List<String> evalScripts;
    private Boolean ajaxRequest;
    private Boolean partialRequest;
    private Boolean renderAll;
    private FacesContext ctx;

    private static final String ORIGINAL_WRITER = "com.sun.faces.ORIGINAL_WRITER";


    // ----------------------------------------------------------- Constructors


    public PartialViewContextImpl(FacesContext ctx) {
        this.ctx = ctx;
    }


    // ---------------------------------------------- Methods from PartialViewContext

    /**
     * @see javax.faces.context.PartialViewContext#isAjaxRequest()
     */
    @Override
    public boolean isAjaxRequest() {

        assertNotReleased();
        if (ajaxRequest == null) {
            ajaxRequest = "partial/ajax".equals(ctx.
                getExternalContext().getRequestHeaderMap().get("Faces-Request"));
            if (!ajaxRequest) {
                ajaxRequest = "partial/ajax".equals(ctx.getExternalContext().getRequestParameterMap().
                    get("Faces-Request"));
            }
        }
        return ajaxRequest;

    }

    /**
     * @see javax.faces.context.PartialViewContext#isPartialRequest()
     */
    @Override
    public boolean isPartialRequest() {

        assertNotReleased();
        if (partialRequest == null) {
            partialRequest = isAjaxRequest() ||
                    "partial/process".equals(ctx.
                    getExternalContext().getRequestHeaderMap().get("Faces-Request"));
        }
        return partialRequest;

    }


    /**
     * @see javax.faces.context.PartialViewContext#isExecuteAll()
     */
    @Override
    public boolean isExecuteAll() {

        assertNotReleased();
        String execute = PARTIAL_EXECUTE_PARAM.getValue(ctx);
        return (ALL_PARTIAL_PHASE_CLIENT_IDS.equals(execute));

    }

    /**
     * @see javax.faces.context.PartialViewContext#isRenderAll()
     */
    @Override
    public boolean isRenderAll() {

        assertNotReleased();
        if (renderAll == null) {
            String render = PARTIAL_RENDER_PARAM.getValue(ctx);
            renderAll = (ALL_PARTIAL_PHASE_CLIENT_IDS.equals(render));
        }

        return renderAll;

    }
    
    /**
     * @see javax.faces.context.PartialViewContext#setRenderAll(boolean) 
     */
    @Override
    public void setRenderAll(boolean renderAll) {

        this.renderAll = renderAll;

    }

    @Override
    public boolean isResetValues() {
        Object value = PARTIAL_RESET_VALUES_PARAM.getValue(ctx);
        return (null != value && "true".equals(value)) ? true : false;
    }

    @Override
    public void setPartialRequest(boolean isPartialRequest) {
        this.partialRequest = isPartialRequest;
    }


    /**
     * @see javax.faces.context.PartialViewContext#getExecuteIds()
     */
    @Override
    public Collection<String> getExecuteIds() {

        assertNotReleased();
        if (executeIds != null) {
            return executeIds;
        }
        executeIds = populatePhaseClientIds(PARTIAL_EXECUTE_PARAM);

        // include the view parameter facet ID if there are other execute IDs
        // to process
        if (!executeIds.isEmpty()) {
            UIViewRoot root = ctx.getViewRoot();
            if (root.getFacetCount() > 0) {
                if (root.getFacet(UIViewRoot.METADATA_FACET_NAME) != null) {
                    executeIds.add(0, UIViewRoot.METADATA_FACET_NAME);   
                }
            }
        }
        return executeIds;

    }

    /**
     * @see javax.faces.context.PartialViewContext#getRenderIds()
     */
    @Override
    public Collection<String> getRenderIds() {

        assertNotReleased();
        if (renderIds != null) {
            return renderIds;
        }
        renderIds = populatePhaseClientIds(PARTIAL_RENDER_PARAM);
        return renderIds;

    }

	/**
	 * @see javax.faces.context.PartialViewContext#getEvalScripts()
	 */
	@Override
	public List<String> getEvalScripts() {
		assertNotReleased();

		if (evalScripts == null) {
			evalScripts = new ArrayList<>(1);
		}

		return evalScripts;
	}

    /**
     * @see PartialViewContext#processPartial(javax.faces.event.PhaseId) 
     */
    @Override
    public void processPartial(PhaseId phaseId) {
        PartialViewContext pvc = ctx.getPartialViewContext();
        Collection <String> myExecuteIds = pvc.getExecuteIds();
        Collection <String> myRenderIds = pvc.getRenderIds();
        UIViewRoot viewRoot = ctx.getViewRoot();

        if (phaseId == PhaseId.APPLY_REQUEST_VALUES ||
            phaseId == PhaseId.PROCESS_VALIDATIONS ||
            phaseId == PhaseId.UPDATE_MODEL_VALUES) {

            // Skip this processing if "none" is specified in the render list,
            // or there were no execute phase client ids.

            if (myExecuteIds == null || myExecuteIds.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                        "No execute and render identifiers specified.  Skipping component processing.");
                }
                return;
            }

            try {
                processComponents(viewRoot, phaseId, myExecuteIds, ctx);
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO,
                           e.toString(),
                           e);
                }
                throw new FacesException(e);
            }

            // If we have just finished APPLY_REQUEST_VALUES phase, install the
            // partial response writer.  We want to make sure that any content
            // or errors generated in the other phases are written using the
            // partial response writer.
            //
            if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                PartialResponseWriter writer = pvc.getPartialResponseWriter();
                ctx.setResponseWriter(writer);
            }

        } else if (phaseId == PhaseId.RENDER_RESPONSE) {

            try {
                //
                // We re-enable response writing.
                //
                PartialResponseWriter writer = pvc.getPartialResponseWriter();
                ResponseWriter orig = ctx.getResponseWriter();
                ctx.getAttributes().put(ORIGINAL_WRITER, orig);
                ctx.setResponseWriter(writer);

                ExternalContext exContext = ctx.getExternalContext();
                exContext.setResponseContentType(RIConstants.TEXT_XML_CONTENT_TYPE);
                exContext.addResponseHeader("Cache-Control", "no-cache");
                
//                String encoding = writer.getCharacterEncoding( );
//                if( encoding == null ) {
//                    encoding = "UTF-8";
//                }
//                writer.writePreamble("<?xml version='1.0' encoding='" + encoding + "'?>\n");
                writer.startDocument();
                
                if (isResetValues()) {
                    viewRoot.resetValues(ctx, myRenderIds);
                }
                
                if (isRenderAll()) {
                    renderAll(ctx, viewRoot);
                    renderState(ctx);
                    writer.endDocument();
                    return;
                }

                renderComponentResources(ctx, viewRoot);

                // Skip this processing if "none" is specified in the render list,
                // or there were no render phase client ids.
                if (myRenderIds != null && !myRenderIds.isEmpty()) {
                    processComponents(viewRoot, phaseId, myRenderIds, ctx);
                }

                renderState(ctx);
                renderEvalScripts(ctx);

                writer.endDocument();
            } catch (IOException ex) {
                this.cleanupAfterView();
            } catch (RuntimeException ex) {
                this.cleanupAfterView();
                // Throw the exception
                throw ex;
            }
        }
    }

    /**
     * @see javax.faces.context.PartialViewContext#getPartialResponseWriter()
     */
    @Override
    public PartialResponseWriter getPartialResponseWriter() {
        assertNotReleased();
        if (partialResponseWriter == null) {
            partialResponseWriter = new DelayedInitPartialResponseWriter(this);
        }
        return partialResponseWriter;
    }

    /**
     * @see javax.faces.context.PartialViewContext#release()
     */
    @Override
    public void release() {
        
        released = true;
        ajaxRequest = null;
        renderAll = null;
        partialResponseWriter = null;
        executeIds = null;
        renderIds = null;
        evalScripts = null;
        ctx = null;
        partialRequest = null;

    }

    // -------------------------------------------------------- Private Methods



    private List<String> populatePhaseClientIds(PredefinedPostbackParameter parameterName) {

        String param = parameterName.getValue(ctx);
        if (param == null) {
            return new ArrayList<>();
        } else {
            Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
            String[] pcs = Util.split(appMap, param, "[ \t]+");
            return ((pcs != null && pcs.length != 0)
                    ? new ArrayList<>(Arrays.asList(pcs))
                    : new ArrayList<>());
        }
        
    }

    // Process the components specified in the phaseClientIds list
    private void processComponents(UIComponent component, PhaseId phaseId,
        Collection<String> phaseClientIds, FacesContext context) throws IOException {

        // We use the tree visitor mechanism to locate the components to
        // process.  Create our (partial) VisitContext and the
        // VisitCallback that will be invoked for each component that
        // is visited.  Note that we use the SKIP_UNRENDERED hint as we
        // only want to visit the rendered subtree.
        EnumSet<VisitHint> hints = EnumSet.of(VisitHint.SKIP_UNRENDERED, VisitHint.EXECUTE_LIFECYCLE);
        VisitContextFactory visitContextFactory = (VisitContextFactory) 
                FactoryFinder.getFactory(VISIT_CONTEXT_FACTORY);
        VisitContext visitContext = visitContextFactory.getVisitContext(context, phaseClientIds, hints);
        PhaseAwareVisitCallback visitCallback =
            new PhaseAwareVisitCallback(ctx, phaseId);
        component.visitTree(visitContext, visitCallback);

        PartialVisitContext partialVisitContext = unwrapPartialVisitContext(visitContext);
        if (partialVisitContext != null) {
            if (LOGGER.isLoggable(Level.FINER) && !partialVisitContext.getUnvisitedClientIds().isEmpty()) {
                Collection<String> unvisitedClientIds = partialVisitContext.getUnvisitedClientIds();
                StringBuilder builder = new StringBuilder();
                for (String cur : unvisitedClientIds) {
                    builder.append(cur).append(" ");
                }
                LOGGER.log(Level.FINER,
                        "jsf.context.partial_visit_context_unvisited_children",
                        new Object[]{builder.toString()});
            }
        }    
    }

    /**
     * Unwraps {@link PartialVisitContext} from a chain of {@link VisitContextWrapper}s.
     *
     * If no {@link PartialVisitContext} is found in the chain, null is returned instead.
     * 
     * @param visitContext the visit context.
     * @return the (unwrapped) partial visit context.
     */
    private static PartialVisitContext unwrapPartialVisitContext(VisitContext visitContext) {
        if (visitContext == null) {
            return null;
        }
        if (visitContext instanceof PartialVisitContext) {
            return (PartialVisitContext) visitContext;
        }
        if (visitContext instanceof VisitContextWrapper) {
            return unwrapPartialVisitContext(((VisitContextWrapper) visitContext).getWrapped());
        }
        return null;
    }
    
    private void renderAll(FacesContext context, UIViewRoot viewRoot) throws IOException {
        // If this is a "render all via ajax" request,
        // make sure to wrap the entire page in a <render> elemnt
        // with the special viewStateId of VIEW_ROOT_ID.  This is how the client
        // JavaScript knows how to replace the entire document with
        // this response.
        PartialViewContext pvc = context.getPartialViewContext();
        PartialResponseWriter writer = pvc.getPartialResponseWriter();
        
        if (!(viewRoot instanceof NamingContainer)) {
            writer.startUpdate(PartialResponseWriter.RENDER_ALL_MARKER);
            if (viewRoot.getChildCount() > 0) {
                for (UIComponent uiComponent : viewRoot.getChildren()) {
                    uiComponent.encodeAll(context);
                }
            }
            writer.endUpdate();
        }
        else {
            /*
             * If we have a portlet request, start rendering at the view root.
             */
            writer.startUpdate(viewRoot.getClientId(context));
            viewRoot.encodeBegin(context);
            if (viewRoot.getChildCount() > 0) {
                for (UIComponent uiComponent : viewRoot.getChildren()) {
                    uiComponent.encodeAll(context);
                }
            }
            viewRoot.encodeEnd(context);
            writer.endUpdate();
        }
    }

    private void renderComponentResources(FacesContext context, UIViewRoot viewRoot) throws IOException {
        ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
        PartialResponseWriter writer = context.getPartialViewContext().getPartialResponseWriter();
        boolean updateStarted = false;

        for (UIComponent resource : viewRoot.getComponentResources(context)) {
            String name = (String) resource.getAttributes().get("name");
            String library = (String) resource.getAttributes().get("library");

            if (resource.getChildCount() == 0 
              && resourceHandler.getRendererTypeForResourceName(name) != null 
              && !resourceHandler.isResourceRendered(context, name, library)) 
            {
                if (!updateStarted) {
                    writer.startUpdate("javax.faces.Resource");
                    updateStarted = true;
                }

                resource.encodeAll(context);
            }
        }

        if (updateStarted) {
            writer.endUpdate();
        }
    }
    
    private void renderState(FacesContext context) throws IOException {
        // Get the view state and write it to the response..
        PartialViewContext pvc = context.getPartialViewContext();
        PartialResponseWriter writer = pvc.getPartialResponseWriter();
        String viewStateId = Util.getViewStateId(context);

        writer.startUpdate(viewStateId);
        String state = context.getApplication().getStateManager().getViewState(context);
        writer.write(state);
        writer.endUpdate();

        ClientWindow window = context.getExternalContext().getClientWindow();
        if (null != window) {
            String clientWindowId = Util.getClientWindowId(context);
            writer.startUpdate(clientWindowId);
            writer.write(window.getId());
            writer.endUpdate();
        }
    }

	private void renderEvalScripts(FacesContext context) throws IOException {
		PartialViewContext pvc = context.getPartialViewContext();
		PartialResponseWriter writer = pvc.getPartialResponseWriter();

		for (String evalScript : pvc.getEvalScripts()) {
			writer.startEval();
			writer.write(evalScript);
			writer.endEval();
		}
	}

    private PartialResponseWriter createPartialResponseWriter() {

        ExternalContext extContext = ctx.getExternalContext();
        String encoding = extContext.getRequestCharacterEncoding();
        extContext.setResponseCharacterEncoding(encoding);
        ResponseWriter responseWriter = null;
        Writer out = null;
        try {
            out = extContext.getResponseOutputWriter();
        } catch (IOException ioe) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE,
                           ioe.toString(),
                           ioe);
            }
        }

        if (out != null) {
            UIViewRoot viewRoot = ctx.getViewRoot();
            if (viewRoot != null) {
                responseWriter =
                    ctx.getRenderKit().createResponseWriter(out,
                    RIConstants.TEXT_XML_CONTENT_TYPE, encoding);
            } else {
                RenderKitFactory factory = (RenderKitFactory)
                    FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
                RenderKit renderKit = factory.getRenderKit(ctx, RenderKitFactory.HTML_BASIC_RENDER_KIT);
                responseWriter = renderKit.createResponseWriter(out, RIConstants.TEXT_XML_CONTENT_TYPE, encoding);
            }
        }
        if (responseWriter instanceof PartialResponseWriter)  {
            return (PartialResponseWriter) responseWriter;
        } else {
            return new PartialResponseWriter(responseWriter);
        }

    }

    private void cleanupAfterView() {
        ResponseWriter orig = (ResponseWriter) ctx.getAttributes().
            get(ORIGINAL_WRITER);
        assert(null != orig);
        // move aside the PartialResponseWriter
        ctx.setResponseWriter(orig);
    }

    private void assertNotReleased() {
        if (released) {
            throw new IllegalStateException();
        }
    }

    // ----------------------------------------------------------- Inner Classes


    private static class PhaseAwareVisitCallback implements VisitCallback {

        private PhaseId curPhase;
        private FacesContext ctx;

        private PhaseAwareVisitCallback(FacesContext ctx, PhaseId curPhase) {
            this.ctx = ctx;
            this.curPhase = curPhase;
        }  


        @Override
        public VisitResult visit(VisitContext context, UIComponent comp) {
            try {

                if (curPhase == PhaseId.APPLY_REQUEST_VALUES) {

                    // RELEASE_PENDING handle immediate request(s)
                    // If the user requested an immediate request
                    // Make sure to set the immediate flag here.

                    comp.processDecodes(ctx);
                } else if (curPhase == PhaseId.PROCESS_VALIDATIONS) {
                    comp.processValidators(ctx);
                } else if (curPhase == PhaseId.UPDATE_MODEL_VALUES) {
                    comp.processUpdates(ctx);
                } else if (curPhase == PhaseId.RENDER_RESPONSE) {
                    PartialResponseWriter writer = ctx.getPartialViewContext().getPartialResponseWriter();
                    writer.startUpdate(comp.getClientId(ctx));
                    // do the default behavior...
                    comp.encodeAll(ctx);
                    writer.endUpdate();
                } else {
                    throw new IllegalStateException("I18N: Unexpected " +
                                                    "PhaseId passed to " +
                                              " PhaseAwareContextCallback: " +
                                                    curPhase.toString());
                }
            }
            catch (IOException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.severe(ex.toString());
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                    ex.toString(),
                    ex);
                }
                throw new FacesException(ex);
            }

            // Once we visit a component, there is no need to visit
            // its children, since processDecodes/Validators/Updates and
            // encodeAll() already traverse the subtree.  We return
            // VisitResult.REJECT to supress the subtree visit.
            return VisitResult.REJECT;
        }
    }


     /**
      * Delays the actual construction of the PartialResponseWriter <em>until</em>
      * content is going to actually be written.
      */
    private static final class DelayedInitPartialResponseWriter extends PartialResponseWriter {

        private ResponseWriter writer;
        private PartialViewContextImpl ctx;

        // -------------------------------------------------------- Constructors


        public DelayedInitPartialResponseWriter(PartialViewContextImpl ctx) {

            super(null);
            this.ctx = ctx;
            ExternalContext extCtx = ctx.ctx.getExternalContext();
            extCtx.setResponseContentType(RIConstants.TEXT_XML_CONTENT_TYPE);
            extCtx.setResponseCharacterEncoding(extCtx.getRequestCharacterEncoding());
            extCtx.setResponseBufferSize(ctx.ctx.getExternalContext().getResponseBufferSize());
        }


        // ---------------------------------- Methods from PartialResponseWriter

        @Override
        public void write(String text) throws IOException {
            HtmlUtils.writeUnescapedTextForXML(getWrapped(), text);
        }

        @Override
        public ResponseWriter getWrapped() {

            if (writer == null) {
                writer = ctx.createPartialResponseWriter();
            }
            return writer;

        }
         
    } // END DelayedInitPartialResponseWriter

} 
