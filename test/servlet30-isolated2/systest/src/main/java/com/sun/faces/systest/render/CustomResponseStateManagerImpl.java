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

package com.sun.faces.systest.render;

import javax.faces.application.StateManager;
import javax.faces.application.StateManager.SerializedView;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.sun.faces.util.Util;

/**
 * <B>RenderKitImpl</B> is a class ...
 */

public class CustomResponseStateManagerImpl extends ResponseStateManager {

    //
    // Protected Constants
    //
    private static final String FACES_VIEW_STATE = "com.sun.faces.FACES_VIEW_STATE";

    private static final String COMPRESS_STATE_PARAM = "com.sun.faces.COMPRESS_STATE";
    //
    // Class Variables
    //

    //
    // Instance Variables
    //
    private Boolean compressStateSet = null;

    //
    // Ivars used during actual client lifetime
    //

    // Relationship Instance Variables

    //
    // Constructors and Initializers
    //

    public CustomResponseStateManagerImpl() {
        super();
    }

    //
    // Class methods
    //

    //
    // General Methods
    //

    //
    // Methods From ResponseStateManager
    //

    @Override
    public Object getState(FacesContext context, String viewId) {
        Object stateArray[] = { getTreeStructure(context, viewId), getComponentState(context) };
        return stateArray;
    }

    @Override
    public boolean isPostback(FacesContext context) {
        boolean result = context.getExternalContext().getRequestParameterMap()
                .containsKey(javax.faces.render.ResponseStateManager.VIEW_STATE_PARAM);
        return result;
    }

    private Object getComponentState(FacesContext context) {

        // requestMap is a local variable so we don't need to synchronize
        Map requestMap = context.getExternalContext().getRequestMap();
        Object state = requestMap.get(FACES_VIEW_STATE);
        // null out the temporary attribute, since we don't need it anymore.
        requestMap.remove(FACES_VIEW_STATE);
        return state;
    }

    private Object getTreeStructure(FacesContext context, String treeId) {
        StateManager stateManager = Util.getStateManager(context);

        Map requestParamMap = context.getExternalContext().getRequestParameterMap();

        String viewString = (String) requestParamMap.get(javax.faces.render.ResponseStateManager.VIEW_STATE_PARAM);
        Object structure = null;
        if (viewString == null) {
            return null;
        }

        if (stateManager.isSavingStateInClient(context)) {
            Object state = null;
            ByteArrayInputStream bis = null;
            GZIPInputStream gis = null;
            ObjectInputStream ois = null;
            boolean compress = isCompressStateSet(context);

            try {
                byte[] bytes = Base64.getDecoder().decode(viewString.getBytes());
                bis = new ByteArrayInputStream(bytes);
                if (isCompressStateSet(context)) {
                    gis = new GZIPInputStream(bis);
                    ois = new ObjectInputStream(gis);
                } else {
                    ois = new ObjectInputStream(bis);
                }
                structure = ois.readObject();
                state = ois.readObject();
                Map requestMap = context.getExternalContext().getRequestMap();
                // store the state object temporarily in request scope
                // until it is processed by getComponentStateToRestore
                // which resets it.
                requestMap.put(FACES_VIEW_STATE, state);
                bis.close();
                if (compress) {
                    gis.close();
                }
                ois.close();
            } catch (java.io.OptionalDataException ode) {
            } catch (java.lang.ClassNotFoundException cnfe) {
            } catch (java.io.IOException iox) {
            }
        } else {
            structure = viewString;
        }
        return structure;
    }

    @Override
    public void writeState(FacesContext context, Object state) throws IOException {
        SerializedView view = null;
        if (state instanceof SerializedView) {
            view = (SerializedView) state;
        } else {
            Object[] stateArray = (Object[]) state;
            StateManager stateManager = context.getApplication().getStateManager();
            view = stateManager.new SerializedView(stateArray[0], null);
        }
        writeSerializedState(context, view);
    }

    private void writeSerializedState(FacesContext context, SerializedView view) throws IOException {
        String hiddenField = null;
        StateManager stateManager = Util.getStateManager(context);

        if (stateManager.isSavingStateInClient(context)) {
            GZIPOutputStream zos = null;
            ObjectOutputStream oos = null;
            boolean compress = isCompressStateSet(context);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (compress) {
                zos = new GZIPOutputStream(bos);
                oos = new ObjectOutputStream(zos);
            } else {
                oos = new ObjectOutputStream(bos);
            }
            oos.writeObject(view.getStructure());
            oos.writeObject(view.getState());
            oos.close();
            if (compress) {
                zos.close();
            }
            byte[] securedata = bos.toByteArray();
            bos.close();

            hiddenField = " <input type=\"hidden\" name=\"" + javax.faces.render.ResponseStateManager.VIEW_STATE_PARAM + "\"" + " value=\""
                    + (new String(Base64.getEncoder().encode(securedata), "ISO-8859-1")) + "\" />\n ";
        } else {
            hiddenField = " <input type=\"hidden\" name=\"" + javax.faces.render.ResponseStateManager.VIEW_STATE_PARAM + "\"" + " value=\""
                    + view.getStructure() + "\" />\n ";

        }
        context.getResponseWriter().write(hiddenField);

        // write this out regardless of state saving mode
        // Only write it out if there is a default specified, and
        // this render kit identifier is not the default.
        String result = context.getApplication().getDefaultRenderKitId();
        if ((null != result && !result.equals("CUSTOM")) || result == null) {
            hiddenField = " <input type=\"hidden\" name=\"" + ResponseStateManager.RENDER_KIT_ID_PARAM + "\"" + " value=\"CUSTOM\""
                    + "\" />\n ";
            context.getResponseWriter().write(hiddenField);
        }
    }

    private boolean isCompressStateSet(FacesContext context) {
        if (null != compressStateSet) {
            return compressStateSet.booleanValue();
        }
        compressStateSet = Boolean.TRUE;

        String compressStateParam = context.getExternalContext().getInitParameter(COMPRESS_STATE_PARAM);
        if (compressStateParam != null) {
            compressStateSet = Boolean.valueOf(compressStateParam);
        }
        return compressStateSet.booleanValue();
    }

} // end of class CustomResponseStateManagerImpl
