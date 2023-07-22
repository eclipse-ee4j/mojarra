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

package com.sun.faces.config.manager;

import static com.sun.faces.util.Util.isEmpty;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.config.manager.tasks.ParseConfigResourceToDOMTask;
import com.sun.faces.util.FacesLogger;

/**
 * <p>
 * Wrapper around the <code>/WEB-INF/faces-config.xml</code>, if present, to expose information relevant to the
 * intialization of the runtime.
 * </p>
 */
public class FacesConfigInfo {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    private static final String ABSOLUTE_ORDERING = "absolute-ordering";
    private static final String ORDERING = "ordering";
    private static final String NAME = "name";
    private static final String OTHERS = "others";

    private double version = 2.0;
    private final boolean isWebInfFacesConfig;
    private final boolean metadataComplete;
    private List<String> absoluteOrdering;

    // -------------------------------------------------------- Constructors

    /**
     * Creates a new <code>WebInfFacesConfig</code> document based on the provided <code>Document</code>. If the
     * <code>Document</code> does not represent the <code>WEB-INF/faces-config.xml</code> the {@link #isWebInfFacesConfig()}
     * method will return <code>false</code>
     *
     * @param documentInfo DocumentInfo representing the <code>/WEB-INF/faces-config.xml</code>
     */
    public FacesConfigInfo(DocumentInfo documentInfo) {

        Document document = documentInfo.getDocument();
        isWebInfFacesConfig = isWebinfFacesConfig(document);
        version = getVersion(document);

        if (isWebInfFacesConfig && isVersionGreaterOrEqual(2.0)) {
            extractOrdering(document);
        }

        metadataComplete = isMetadataComplete(document);

    }

    // ------------------------------------------------------ Public Methods

    /**
     * @param version version to check
     * @return <code>true</code> if <code>version</code> is greater or equal to the version of the
     * <code>/WEB-INF/faces-config.xml</code>
     */
    public boolean isVersionGreaterOrEqual(double version) {
        return this.version >= version;
    }

    /**
     * @return <code>true</code> if the <code>Document</code> provided at construction time represents the
     * <code>/WEB-INF/faces-config.xml</code>.
     */
    public boolean isWebInfFacesConfig() {
        return isWebInfFacesConfig;
    }

    /**
     * @return <code>true</code> if the <code>Document</code> provided at construction time represents the
     * <code>/WEB-INF/faces-config.xml</code> and is metadata complete.
     */
    public boolean isMetadataComplete() {
        return metadataComplete;
    }

    /**
     * @return a <code>List</code> of document names that in the order that they should be processed. The presense of the
     * keyword "others" indicates all documents not explicitly referenced by name in the list should be places in the final
     * parsing order at same location. If there are multiple documents that aren't named and the others element is present,
     * the order that these documents are inserted into the final list is unspecified at this time.
     */
    public List<String> getAbsoluteOrdering() {
        return absoluteOrdering != null ? unmodifiableList(absoluteOrdering) : null;
    }

    // ----------------------------------------------------- Private Methods

    /**
     * @param document document representing <code>WEB-INF/faces-config.xml</code>
     * @return return the value of the version attribute of the provided document. If no version attribute is specified,
     * assume 1.1.
     */
    private double getVersion(Document document) {

        String version = document.getDocumentElement().getAttributeNS(document.getNamespaceURI(), "version");
        if (version != null && version.length() > 0) {
            return Double.parseDouble(version);
        }

        return 1.1d;
    }

    /**
     * @param document the <code>Document</code> to inspect
     * @return <code>true</code> if the document represents the <code>/WEB-INF/faces-config.xml</code>
     */
    private boolean isWebinfFacesConfig(Document document) {
        return !isEmpty(document.getDocumentElement().getAttribute(ParseConfigResourceToDOMTask.WEB_INF_MARKER));
    }

    private boolean isMetadataComplete(Document document) {

        if (isVersionGreaterOrEqual(2.0)) {
            String metadataComplete = document.getDocumentElement().getAttributeNS(document.getNamespaceURI(), "metadata-complete");
            return Boolean.parseBoolean(metadataComplete);
        }

        // not a 2.0 application, so annotation processing will not occur
        return true;
    }

    private void extractOrdering(Document document) {

        Element documentElement = document.getDocumentElement();
        String namespace = documentElement.getNamespaceURI();

        NodeList orderingElements = documentElement.getElementsByTagNameNS(namespace, ORDERING);
        if (orderingElements.getLength() > 0) {
            LOGGER.warning("faces.configuration.web.faces.config.contains.ordering");
        }

        NodeList absoluteOrderingElements = documentElement.getElementsByTagNameNS(namespace, ABSOLUTE_ORDERING);

        if (absoluteOrderingElements.getLength() > 0) {
            // according to the schema there, should be only one
            if (absoluteOrderingElements.getLength() > 1) {
                throw new IllegalStateException("Multiple 'absolute-ordering' elements found within WEB-INF/faces-config.xml");
            }
            Node absoluteOrderingNode = absoluteOrderingElements.item(0);
            NodeList children = absoluteOrderingNode.getChildNodes();
            absoluteOrdering = new ArrayList<>(children.getLength());
            for (int i = 0, len = children.getLength(); i < len; i++) {
                Node n = children.item(i);
                if (null != n.getLocalName()) {
                    switch (n.getLocalName()) {
                    case NAME:
                        absoluteOrdering.add(getNodeText(n));
                        break;
                    case OTHERS:
                        if (absoluteOrdering.contains("others")) {
                            throw new IllegalStateException(
                                    "'absolute-ordering' element defined with multiple 'others' child elements found within WEB-INF/faces-config.xml");
                        }
                        absoluteOrdering.add("others");
                        break;
                    }
                }
            }
        }
    }

    /**
     * Return the textual content, if any, of the provided <code>Node</code>.
     */
    private String getNodeText(Node node) {

        String nodeText = null;
        if (node != null) {
            nodeText = node.getTextContent();
            if (nodeText != null) {
                nodeText = nodeText.trim();
            }
        }

        return !isEmpty(nodeText) ? nodeText : null;

    }

} // END FacesConfigInfo
