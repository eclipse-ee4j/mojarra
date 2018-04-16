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

package com.sun.faces.config.rules;


import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

import com.sun.faces.config.beans.DescriptionBean;
import org.apache.commons.digester.NodeCreateRule;


/**
 * <p>Digester rule for the <code>&lt;description&gt;</code> element,
 * used to absorb the body content of the matching element even if it
 * contains mixed markup and body content, and then save it as the
 * value of the <code>description</code> property of the corresponding
 * <code>DescriptionBean</code> instance..</p>
 */

public class DescriptionTextRule extends NodeCreateRule {


    // ------------------------------------------------------------- Constructor


    /**
     * <p>Construct a variant of <code>NodeCreateRule</code> that will
     * create a <code>DocumentFragment</code> object.
     */
    public DescriptionTextRule() throws Exception {

        super(Node.DOCUMENT_FRAGMENT_NODE);

    }


    // ------------------------------------------------------------ Rule Methods


    /**
     * <p>Ensure that the object at the top of the stack is a
     * <code>DescriptionBean</code>, then perform the standard
     * superclass processing.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param attributes The attribute list of this element
     *
     * @exception IllegalStateException if the parent stack element is not
     *  of type FeatureBean
     */
    public void begin(String namespace, String name,
                      Attributes attributes) throws Exception {
        
        assert digester.peek() instanceof DescriptionBean
              : "Assertion Error: Expected DescriptionBean to be at the top of the stack";

        // Perform our standard superclass processing
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[DescriptionTextRule]{" +
                                       digester.getMatch() +
                                       "} Begin");
        }
        super.begin(namespace, name, attributes);

    }


    /**
     * <p>No body processing is required.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     * @param text The text of the body of this element
     */
    public void body(String namespace, String name,
                     String text) throws Exception {
    }


    /**
     * <p>Serialize the resulting DOM into text, and use it to set the
     * <code>description</code> property of the parent
     * <code>DescriptionBean</code>.</p>
     *
     * @param namespace the namespace URI of the matching element, or an 
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just 
     *   the element name otherwise
     *
     * @exception IllegalStateException if the popped object is not
     *  of the correct type
     */
    public void end(String namespace, String name) throws Exception {

        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("[DescriptionTextRule]{" +
                                       digester.getMatch() +
                                       "} End (" + digester.peek() + ")");
        }

        // Pop the DOM object off the stack (works around a bug in
        // NodeCreateRule that won't be fixed until version 1.6 of
        // commons-digester is released)
        Node root = (Node) digester.pop();        

        // Serialize the child nodes into a StringBuffer
        DescriptionBean db = (DescriptionBean) digester.peek();
        StringBuffer sb = new StringBuffer();
        NodeList kids = root.getChildNodes();
        int n = kids.getLength();
        for (int i = 0; i < n; i++) {
            serialize(sb, kids.item(i));
        }

        // Use the StringBuffer to set the value of the description property
        db.setDescription(sb.toString());

    }


    /**
     * <p>No finish processing is required.</p>
     *
     */
    public void finish() throws Exception {
    }


    // ---------------------------------------------------------- Public Methods


    public String toString() {

        StringBuffer sb = new StringBuffer("DescriptionTextRule[]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Package Methods


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Append the serialized version of the specified node to the
     * string buffer being accumulated.</p>
     *
     * @param sb StringBuffer to which serialized text is appended
     * @param node Node to be serialized
     *
     * @exception Exception if any processing exception occurs
     */
    private void serialize(StringBuffer sb, Node node) throws Exception {

        // Processing depends on the node type
        switch (node.getNodeType()) {

        case Node.ELEMENT_NODE:

            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("  Processing element node '" +
                                           node.getNodeName() + "'");
            }

            // Open the element and echo the attributes
            sb.append("<");
            sb.append(node.getNodeName());
            NamedNodeMap attrs = node.getAttributes();
            int n = attrs.getLength();
            for (int i = 0; i < n; i++) {
                Node attr = attrs.item(i);
                sb.append(" ");
                sb.append(attr.getNodeName());
                sb.append("=\"");
                sb.append(attr.getNodeValue());
                sb.append("\"");
            }

            // Does this element have any children?
            NodeList kids = node.getChildNodes();
            int m = kids.getLength();
            if (m > 0) {

                // Yes -- serialize child elements and close parent element
                sb.append(">");
                for (int j = 0; j < m; j++) {
                    serialize(sb, kids.item(j));
                }
                sb.append("</");
                sb.append(node.getNodeName());
                sb.append(">");

            } else {

                // No -- shorthand close of the parent element
                sb.append(" />");

            }
            break;

        case Node.TEXT_NODE:

            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("  Processing text node '" +
                                           node.getNodeValue() + "'");
            }

            // Append the text to our accumulating buffer
            sb.append(node.getNodeValue());
            break;

        default:
            throw new IllegalArgumentException("Cannot process node '" +
                                               node.getNodeName() +
                                               "' of type '" +
                                               node.getNodeType());

        }

    }


}
