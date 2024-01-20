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

package jakarta.faces.context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.faces.RIConstants;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.render.ResponseStateManager;

/**
 * <p class="changed_added_2_0">
 * <strong>PartialResponseWriter</strong> decorates an existing <code>ResponseWriter</code> to support the generation of
 * a partial response suitable for Ajax operations. In addition to the markup generation methods inherited from
 * <code>jakarta.faces.context.ResponseWriter</code>, this class provides methods for constructing the standard partial
 * response elements.
 * </p>
 *
 * @since 2.0
 */
public class PartialResponseWriter extends ResponseWriterWrapper {
    // True when we need to close a changes tag
    //
    private boolean inChanges = false;

    // True when we need to close a before insert tag
    //
    private boolean inInsertBefore = false;

    // True when we need to close afer insert tag
    //
    private boolean inInsertAfter = false;

    // True when we need to close an update tag
    //
    private boolean inUpdate = false;

    /**
     * <p class="changed_added_2_0">
     * Reserved ID value to indicate entire ViewRoot.
     * </p>
     *
     * @since 2.0
     */
    public static final String RENDER_ALL_MARKER = "jakarta.faces.ViewRoot";

    /**
     * <p class="changed_added_2_0">
     * Reserved ID value to indicate serialized ViewState.
     * </p>
     *
     * @since 2.0
     */
    public static final String VIEW_STATE_MARKER = ResponseStateManager.VIEW_STATE_PARAM;

    /**
     * <p class="changed_added_2_0">
     * Create a <code>PartialResponseWriter</code>.
     * </p>
     *
     * @param writer The writer to wrap.
     * @since 2.0
     */
    public PartialResponseWriter(ResponseWriter writer) {
        super(writer);
    }

    /**
     * <p class="changed_added_2_0">
     * Write the start of a partial response.
     * </p>
     * <p class="changed_added_2_3">
     * If {@link UIViewRoot} is an instance of {@link NamingContainer}, then write
     * {@link UIViewRoot#getContainerClientId(FacesContext)} as value of the <code>id</code> attribute of the root element.
     * </p>
     *
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    @Override
    public void startDocument() throws IOException {
        ResponseWriter writer = getWrapped();
        String encoding = writer.getCharacterEncoding();
        if (encoding == null) {
            encoding = RIConstants.CHAR_ENCODING;
        }
        writer.writePreamble("<?xml version='1.0' encoding='" + encoding + "'?>\n");
        writer.startElement("partial-response", null);
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (null != ctx && ctx.getViewRoot() instanceof NamingContainer) {
            String id = ctx.getViewRoot().getContainerClientId(ctx);
            writer.writeAttribute("id", id, "id");
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Write the end of a partial response.
     * </p>
     *
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    @Override
    public void endDocument() throws IOException {
        endChangesIfNecessary();
        ResponseWriter writer = getWrapped();
        /*
         * Because during a <script> writing an exception can occur we need to make sure the wrapped response only writes one
         * partial-response, but also calls to end the document (so we can properly cleanup in the wrapped HtmlResponseWriter).
         * See issue #3473.
         */
        if (!(writer instanceof PartialResponseWriter)) {
            writer.endElement("partial-response");
        }
        writer.endDocument();
    }

    /**
     * <p class="changed_added_2_0">
     * Write the start of an insert operation where the contents will be inserted before the specified target node.
     * </p>
     *
     * @param targetId ID of the node insertion should occur before
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void startInsertBefore(String targetId) throws IOException {
        startChangesIfNecessary();
        inInsertBefore = true;
        ResponseWriter writer = getWrapped();
        writer.startElement("insert", null);
        writer.startElement("before", null);
        writer.writeAttribute("id", targetId, null);
        writer.startCDATA();
    }

    /**
     * <p class="changed_added_2_0">
     * Write the start of an insert operation where the contents will be inserted after the specified target node.
     * </p>
     *
     * @param targetId ID of the node insertion should occur after
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void startInsertAfter(String targetId) throws IOException {
        startChangesIfNecessary();
        inInsertAfter = true;
        ResponseWriter writer = getWrapped();
        writer.startElement("insert", null);
        writer.startElement("after", null);
        writer.writeAttribute("id", targetId, null);
        writer.startCDATA();
    }

    /**
     * <p class="changed_added_2_0">
     * Write the end of an insert operation.
     * </p>
     *
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void endInsert() throws IOException {
        ResponseWriter writer = getWrapped();
        writer.endCDATA();
        if (inInsertBefore) {
            writer.endElement("before");
            inInsertBefore = false;
        } else if (inInsertAfter) {
            writer.endElement("after");
            inInsertAfter = false;
        }
        writer.endElement("insert");
    }

    /**
     * <p class="changed_added_2_0">
     * Write the start of an update operation.
     * </p>
     *
     * @param targetId ID of the node to be updated
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void startUpdate(String targetId) throws IOException {
        startChangesIfNecessary();
        inUpdate = true;
        ResponseWriter writer = getWrapped();
        writer.startElement("update", null);
        writer.writeAttribute("id", targetId, null);
        writer.startCDATA();
    }

    /**
     * <p class="changed_added_2_0">
     * Write the end of an update operation.
     * </p>
     *
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void endUpdate() throws IOException {
        ResponseWriter writer = getWrapped();
        writer.endCDATA();
        writer.endElement("update");
        inUpdate = false;
    }

    /**
     * <p class="changed_added_2_0">
     * Write an attribute update operation.
     * </p>
     *
     * @param targetId ID of the node to be updated
     * @param attributes Map of attribute name/value pairs to be updated
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void updateAttributes(String targetId, Map<String, String> attributes) throws IOException {
        startChangesIfNecessary();
        ResponseWriter writer = getWrapped();
        writer.startElement("attributes", null);
        writer.writeAttribute("id", targetId, null);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            writer.startElement("attribute", null);
            writer.writeAttribute("name", entry.getKey(), null);
            writer.writeAttribute("value", entry.getValue(), null);
            writer.endElement("attribute");
        }
        writer.endElement("attributes");
    }

    /**
     * <p class="changed_added_2_0">
     * Write a delete operation.
     * </p>
     *
     * @param targetId ID of the node to be deleted
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void delete(String targetId) throws IOException {
        startChangesIfNecessary();
        ResponseWriter writer = getWrapped();
        writer.startElement("delete", null);
        writer.writeAttribute("id", targetId, null);
        writer.endElement("delete");
    }

    /**
     * <p class="changed_added_2_0">
     * Write a redirect operation.
     * </p>
     *
     * @param url URL to redirect to
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void redirect(String url) throws IOException {
        endChangesIfNecessary();
        ResponseWriter writer = getWrapped();
        writer.startElement("redirect", null);
        writer.writeAttribute("url", url, null);
        writer.endElement("redirect");
    }

    /**
     * <p class="changed_added_2_0">
     * Write the start of an eval operation.
     * </p>
     *
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void startEval() throws IOException {
        startChangesIfNecessary();
        ResponseWriter writer = getWrapped();
        writer.startElement("eval", null);
        writer.startCDATA();
    }

    /**
     * <p class="changed_added_2_0">
     * Write the end of an eval operation.
     * </p>
     *
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void endEval() throws IOException {
        ResponseWriter writer = getWrapped();
        writer.endCDATA();
        writer.endElement("eval");
    }

    /**
     * <p class="changed_added_2_0">
     * Write the start of an extension operation.
     * </p>
     *
     * @param attributes String name/value pairs for extension element attributes
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void startExtension(Map<String, String> attributes) throws IOException {
        startChangesIfNecessary();
        ResponseWriter writer = getWrapped();
        writer.startElement("extension", null);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                writer.writeAttribute(entry.getKey(), entry.getValue(), null);
            }
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Write the end of an extension operation.
     * </p>
     *
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void endExtension() throws IOException {
        ResponseWriter writer = getWrapped();
        writer.endElement("extension");
    }

    /**
     * <p class="changed_added_2_0">
     * Write the start of an error.
     * </p>
     *
     * @param errorName Descriptive string for the error
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void startError(String errorName) throws IOException {
        endUpdateIfNecessary();
        endChangesIfNecessary();
        ResponseWriter writer = getWrapped();
        writer.startElement("error", null);
        writer.startElement("error-name", null);
        writer.write(errorName);
        writer.endElement("error-name");
        writer.startElement("error-message", null);
        writer.startCDATA();
    }

    /**
     * <p class="changed_added_2_0">
     * Write the end of an error.
     * </p>
     *
     * @throws IOException if an input/output error occurs
     * @since 2.0
     */
    public void endError() throws IOException {
        ResponseWriter writer = getWrapped();
        writer.endCDATA();
        writer.endElement("error-message");
        writer.endElement("error");
    }

    private void startChangesIfNecessary() throws IOException {
        if (!inChanges) {
            ResponseWriter writer = getWrapped();
            writer.startElement("changes", null);
            inChanges = true;
        }
    }

    private void endUpdateIfNecessary() throws IOException {
        if (inUpdate) {
            endUpdate();
        }
    }

    private void endChangesIfNecessary() throws IOException {
        if (inChanges) {
            ResponseWriter writer = getWrapped();
            writer.endElement("changes");
            inChanges = false;
        }
    }

}
