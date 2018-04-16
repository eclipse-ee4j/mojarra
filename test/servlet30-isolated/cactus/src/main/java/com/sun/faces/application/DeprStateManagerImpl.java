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

// DeprStateManagerImpl.java 

package com.sun.faces.application;

import com.sun.faces.RIConstants;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.TreeStructure;
import com.sun.faces.util.LRUMap;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.RequestStateManager;

import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.faces.component.NamingContainer;
import javax.faces.component.UINamingContainer;

/**
 * <B>DeprStateManagerImpl</B> is a test class which implements
 * deprecated methods only. 
 */
public class DeprStateManagerImpl extends StateManager {

    private static final String NUMBER_OF_VIEWS_IN_SESSION =
        RIConstants.FACES_PREFIX + "NUMBER_OF_VIEWS_IN_SESSION";
    private static final int DEFAULT_NUMBER_OF_VIEWS_IN_SESSION = 15;

    private static final String NUMBER_OF_VIEWS_IN_LOGICAL_VIEW_IN_SESSION =
        RIConstants.FACES_PREFIX + "NUMBER_OF_VIEWS_IN_LOGICAL_VIEW_IN_SESSION";
    private static final int DEFAULT_NUMBER_OF_VIEWS_IN_LOGICAL_VIEW_IN_SESSION = 15;

    private static final String LOGICAL_VIEW_MAP =
          RIConstants.FACES_PREFIX + "logicalViewMap";
    /**
     * Number of views in logical view to be saved in session.
     */
    int noOfViews = 0;
    int noOfViewsInLogicalView = 0;
    
    public SerializedView saveSerializedView(FacesContext context) 
        throws IllegalStateException{
        SerializedView result = null;
	Object treeStructure = null;
	Object componentState = null;
	// irrespective of method to save the tree, if the root is transient
	// no state information needs to  be persisted.
	UIViewRoot viewRoot = context.getViewRoot();
	if (viewRoot.isTransient()) {
            return result;
	}
	
	// honor the requirement to check for id uniqueness
	checkIdUniqueness(context, viewRoot, new HashSet());

	
	result = new SerializedView(treeStructure = 
				    getTreeStructureToSave(context),
				    componentState =
				    getComponentStateToSave(context));
        if (!isSavingStateInClient(context)) {
            //
            // Server Side state saving is handled stored in two nested LRU maps
            // in the session.
            //
            // The first map is called the LOGICAL_VIEW_MAP.  A logical view
            // is a top level view that may have one or more actual views inside
            // of it.  This will be the case when you have a frameset, or an
            // application that has multiple windows operating at the same time.
            // The LOGICAL_VIEW_MAP map contains 
            // an entry for each logical view, up to the limit specified by the
            // numberOfViewsParameter.  Each entry in the LOGICAL_VIEW_MAP
            // is an LRU Map, configured with the numberOfViewsInLogicalView
            // parameter.  
            //
            // The motivation for this is to allow better memory tuning for 
            // apps that need this multi-window behavior.
            
            String id = null,
                   idInActualMap = null,
                   idInLogicalMap = (String)
                RequestStateManager.get(context, RequestStateManager.LOGICAL_VIEW_MAP);
            LRUMap logicalMap = null, actualMap = null;
            int 
                logicalMapSize = getNumberOfViewsParameter(context),
                actualMapSize = getNumberOfViewsInLogicalViewParameter(context);
            
            Object stateArray[] = { treeStructure, componentState };
            Map sessionMap = context.getExternalContext().getSessionMap();
            
 	    synchronized (this) {
                if (null == (logicalMap = (LRUMap) sessionMap.get(LOGICAL_VIEW_MAP))) {
                    logicalMap = new LRUMap(logicalMapSize);
 		    sessionMap.put(LOGICAL_VIEW_MAP, logicalMap);
                }
                assert(null != logicalMap); 

                if (null == idInLogicalMap) {
                    idInLogicalMap = createUniqueRequestId();
                }
                assert(null != idInLogicalMap);
 
                idInActualMap = createUniqueRequestId();
 		if (null == (actualMap = (LRUMap) 
                        logicalMap.get(idInLogicalMap))) {
		    actualMap = new LRUMap(actualMapSize);
                    logicalMap.put(idInLogicalMap, actualMap);
 		}
                id = idInLogicalMap + ':' +
                        idInActualMap;
		result = new SerializedView(id, null);
                actualMap.put(idInActualMap, stateArray);
 	    }
 	}
	
        return result;
    }


    char requestIdSerial = 0;

    private String createUniqueRequestId() {
	if (requestIdSerial++ == Character.MAX_VALUE) {
	    requestIdSerial = 0;
	}
	return UIViewRoot.UNIQUE_ID_PREFIX + ((int) requestIdSerial);
    }


    protected void checkIdUniqueness(FacesContext context,
        UIComponent component, Set componentIds) throws IllegalStateException{
        UIComponent kid;
        // deal with children that are marked transient.
        Iterator kids = component.getChildren().iterator();
        String id;
        while (kids.hasNext()) {
            kid = (UIComponent) kids.next();
	    // check for id uniqueness
	    id = kid.getClientId(context);
	    if (id != null && !componentIds.add(id)) {
		throw new IllegalStateException(MessageUtils.getExceptionMessageString(
                        MessageUtils.DUPLICATE_COMPONENT_ID_ERROR_ID,
                        new Object[]{id}));
	    }

	    checkIdUniqueness(context, kid, componentIds);
        }
        // deal with facets that are marked transient.
        kids = component.getFacets().values().iterator();
        while (kids.hasNext()) {
            kid = (UIComponent) kids.next();
	    // check for id uniqueness
	    id = kid.getClientId(context);
	    if (id != null && !componentIds.add(id)) {
		throw new IllegalStateException(MessageUtils.getExceptionMessageString(
                        MessageUtils.DUPLICATE_COMPONENT_ID_ERROR_ID,
                        new Object[]{id}));
	    }

	    checkIdUniqueness(context, kid, componentIds);

        }
    }


    protected Object getComponentStateToSave(FacesContext context) {
	return context.getViewRoot().processSaveState(context);
    }


    protected Object getTreeStructureToSave(FacesContext context) {
        TreeStructure structRoot = null;
        UIComponent viewRoot = context.getViewRoot();
        if (!(viewRoot.isTransient())) {
            structRoot = new TreeStructure(viewRoot);
            buildTreeStructureToSave(context, viewRoot, structRoot, null);
        }
        return structRoot;
    }


    public UIViewRoot restoreView(FacesContext context, String viewId,
                                  String renderKitId) {
        if (null == renderKitId) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "renderKitId");
            throw new IllegalArgumentException(message);
        }

        UIViewRoot viewRoot = null;
        if (isSavingStateInClient(context)) {
            // restore view from response.
            viewRoot = restoreTreeStructure(context, viewId, renderKitId);
            if (viewRoot != null) {
                restoreComponentState(context, viewRoot, renderKitId);
            } else {
            }
        } else {
            // restore tree from session.
            // The ResponseStateManager implementation may be using the new methods or
            // deprecated methods.  We need to know which one to call.
            Object id = null;
            ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(context, renderKitId);
	    id = rsm.getTreeStructureToRestore(context, viewId);

	    if (null != id) {
                String idString = (String) id,
                       idInLogicalMap = null,
                       idInActualMap = null;
                
                int sep = idString.indexOf(':');
                assert(-1 != sep);
                assert(sep < idString.length());
                
                idInLogicalMap = idString.substring(0, sep);
                idInActualMap = idString.substring(sep + 1);
                		
                ExternalContext externalCtx = context.getExternalContext();
                Object sessionObj = externalCtx.getSession(false);
                
                // stop evaluating if the session is not available
                if (sessionObj == null) {
                    return null;
                }
                
                Map logicalMap = null,
                actualMap = null,
                sessionMap = externalCtx.getSessionMap();
                
		TreeStructure structRoot = null;
		Object [] stateArray = null;
		synchronized (sessionObj) {
		    logicalMap = (Map) sessionMap.get(LOGICAL_VIEW_MAP);
                    if (logicalMap != null) {
                        actualMap = (Map) logicalMap.get(idInLogicalMap);
                        if (actualMap != null) {
                            RequestStateManager.set(context, RequestStateManager.LOGICAL_VIEW_MAP,
                                idInLogicalMap);
                            stateArray = (Object []) actualMap.get(idInActualMap);
                        }
                    }
		}
                if (stateArray == null) {
                    return null;
                }
		structRoot = (TreeStructure)stateArray[0];
		viewRoot = (UIViewRoot) structRoot.createComponent();
		restoreComponentTreeStructure(structRoot, viewRoot);
		
		viewRoot.processRestoreState(context, stateArray[1]);
		
	    }
        }
        return viewRoot;
    }


    protected void restoreComponentState(FacesContext context,
                                         UIViewRoot root, String renderKitId) {
        if (null == renderKitId) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "renderKitId");
            throw new IllegalArgumentException(message);
        }
        Object state = null;
        ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(context, renderKitId);
	state = rsm.getComponentStateToRestore(context);
        root.processRestoreState(context, state);
    }


    protected UIViewRoot restoreTreeStructure(FacesContext context,
                                              String viewId, String renderKitId) {
        if (null == renderKitId) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "renderKitId");
            throw new IllegalArgumentException(message);
        }
        UIComponent viewRoot = null;
        TreeStructure structRoot = null;
        ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(context, renderKitId);
        structRoot = (TreeStructure)rsm.getTreeStructureToRestore(context, viewId);
        if (structRoot == null) {
            return null;
        }
        viewRoot = structRoot.createComponent();
        restoreComponentTreeStructure(structRoot, viewRoot);
        return ((UIViewRoot) viewRoot);
    }

    public void writeState(FacesContext context, SerializedView state)
        throws IOException {
        String renderKitId = context.getViewRoot().getRenderKitId();
        ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(context, renderKitId);
        rsm.writeState(context, state);
    }

    /**
     * Builds a hierarchy of TreeStrucure objects simulating the component
     * tree hierarchy.
     */
    public void buildTreeStructureToSave(FacesContext context,
                                         UIComponent component,
                                         TreeStructure treeStructure,
                                         Set componentIds) {
        // traverse the component hierarchy and save the tree structure 
        // information for every component.
        
        // Set for catching duplicate IDs
        if (null == componentIds) {
            componentIds = new HashSet();
        }
        
        // save the structure info of the children of the component 
        // being processed.
        Iterator kids = component.getChildren().iterator();
        String id;
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();

	    // check for id uniqueness
	    id = kid.getClientId(context);
	    if (id != null && !componentIds.add(id)) {
		throw new IllegalStateException(MessageUtils.getExceptionMessageString(
                        MessageUtils.DUPLICATE_COMPONENT_ID_ERROR_ID,
                        new Object[]{id}));
	    }
            
            // if a component is marked transient do not persist its state as
            // well as its children.
            if (!kid.isTransient()) {
                TreeStructure treeStructureChild = new TreeStructure(kid);
                treeStructure.addChild(treeStructureChild);
                buildTreeStructureToSave(context, kid, treeStructureChild,
                                         componentIds);
            }
        }

        // save structure info of the facets of the component currenly being 
        // processed.
        Iterator facets = component.getFacets().keySet().iterator();
        while (facets.hasNext()) {
            String facetName = (String) facets.next();
            UIComponent facetComponent = (UIComponent) component.getFacets().
                get(facetName);

	    // check for id uniqueness
	    id = facetComponent.getClientId(context);
	    if (id != null && !componentIds.add(id)) {
		throw new IllegalStateException(MessageUtils.getExceptionMessageString(
                        MessageUtils.DUPLICATE_COMPONENT_ID_ERROR_ID,
                        new Object[]{id}));
	    }
            
            // if a facet is marked transient do not persist its state as well as
            // its children.
            if (!(facetComponent.isTransient())) {
                TreeStructure treeStructureFacet =
                    new TreeStructure(facetComponent);
                treeStructure.addFacet(facetName, treeStructureFacet);
                // process children of facet.
                buildTreeStructureToSave(context,
                                         facetComponent, treeStructureFacet,
                                         componentIds);
            }
        }
    }


    /**
     * Reconstitutes the component tree from TreeStructure hierarchy
     */
    public void restoreComponentTreeStructure(TreeStructure treeStructure,
                                              UIComponent component) {
        // traverse the tree strucure hierarchy and restore component
        // structure.
      
        // restore the structure of the children of the component being processed.
        Iterator kids = treeStructure.getChildren();
        while (kids.hasNext()) {
            TreeStructure kid = (TreeStructure) kids.next();
            UIComponent child = kid.createComponent();
            component.getChildren().add(child);
            restoreComponentTreeStructure(kid, child);
        }
        
        // process facets
        Iterator facets = treeStructure.getFacetNames();
        while (facets.hasNext()) {
            String facetName = (String) facets.next();
            TreeStructure facetTreeStructure =
                treeStructure.getTreeStructureForFacet(facetName);
            UIComponent facetComponent = facetTreeStructure.createComponent();
            component.getFacets().put(facetName, facetComponent);
            restoreComponentTreeStructure(facetTreeStructure, facetComponent);
        }
    }
    
    /**
     * Returns the <code> UIViewRoot</code> corresponding the 
     * <code> viewId </code> by restoring the view structure and state.
     */
    protected UIViewRoot restoreSerializedView(FacesContext context, 
        SerializedView sv, String viewId) {
        if ( sv == null) {
            return null;
        }
        TreeStructure structRoot = (TreeStructure) sv.getStructure();
        if (structRoot == null) {
            return null;
        }
        UIComponent viewRoot = structRoot.createComponent();
        if (viewRoot != null) {
             restoreComponentTreeStructure(structRoot, viewRoot);
             Object state = sv.getState();
             viewRoot.processRestoreState(context, state);
        }
        return ((UIViewRoot)viewRoot);
    }
    
    /**
     * Returns the value of ServletContextInitParameter that specifies the
     * maximum number of logical views to be saved in session. If none is specified
     * returns <code>DEFAULT_NUMBER_OF_VIEWS_IN_SESSION</code>.
     */
    protected int getNumberOfViewsParameter(FacesContext context) {
        if (noOfViews != 0) { 
            return noOfViews;
        }
        noOfViews = DEFAULT_NUMBER_OF_VIEWS_IN_SESSION;
        String noOfViewsStr = context.getExternalContext().
                getInitParameter(NUMBER_OF_VIEWS_IN_SESSION);
        if (noOfViewsStr != null) {
            try {
                noOfViews = Integer.valueOf(noOfViewsStr).intValue();
            } catch (NumberFormatException nfe) {
            }
        } 
        return noOfViews;
    }

    /**
     * Returns the value of ServletContextInitParameter that specifies the
     * maximum number of views to be saved in this logical view. If none is specified
     * returns <code>DEFAULT_NUMBER_OF_VIEWS_IN_LOGICAL_VIEW_IN_SESSION</code>.
     */
    protected int getNumberOfViewsInLogicalViewParameter(FacesContext context) {
        if (noOfViewsInLogicalView != 0) { 
            return noOfViewsInLogicalView;
        }
        noOfViewsInLogicalView = DEFAULT_NUMBER_OF_VIEWS_IN_LOGICAL_VIEW_IN_SESSION;
        String noOfViewsStr = context.getExternalContext().
                getInitParameter(NUMBER_OF_VIEWS_IN_LOGICAL_VIEW_IN_SESSION);
        if (noOfViewsStr != null) {
            try {
                noOfViewsInLogicalView = Integer.valueOf(noOfViewsStr).intValue();
            } catch (NumberFormatException nfe) {
            }
        } 
        return noOfViewsInLogicalView;
    }
}
