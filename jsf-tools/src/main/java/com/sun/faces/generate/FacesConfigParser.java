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

package com.sun.faces.generate;

import static java.lang.System.out;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class FacesConfigParser {

    private final DocumentBuilder documentBuilder = createDocumentBuilder();
    
    private Consumer<Element> onStartElement = e -> out.println("START: " + e.getNodeName());
    private BiConsumer<Element, Text> onLeafElementWithText = (e,t) -> out.println("Value: " + t.getWholeText().trim());
    private Consumer<Element> onEndElement = e -> out.println("END: " + e.getNodeName());
    private boolean skipDocumentRoot = true;
    
    public void parseFromRelativePath(String rootPath) throws IOException {
        out.println("Scanning from " + rootPath + " = " + get(rootPath).toAbsolutePath().toRealPath() + "\n");
        
        parse(document(get(rootPath)).getDocumentElement());
    }
    
    public void parseFromClassPath(String resource) throws IOException {
        
        try {
            URL documentURL = 
                new URI(this.getClass()
                            .getClassLoader()
                            .getResource(resource)
                            .toExternalForm()
                            .replaceAll(" ", "%20"))
                .toURL();
        
            InputSource is = new InputSource(getInputStream(documentURL));
            is.setSystemId(documentURL.toExternalForm());
            
            parse(document(is).getDocumentElement());
            
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void parse(Element root) throws IOException {

        Deque<Element> elements = new ArrayDeque<>();
        
        if (skipDocumentRoot) {
            pushChildElements(elements, getChildElements(root.getChildNodes()));
        } else {
            elements.push(root);
        }
        
        Element currentElement = null;
        Element previousElement = null;
        
        while (!elements.isEmpty()) {
            
            currentElement = elements.peek();
            
            if (previousElement != null && (previousElement.getParentNode().isEqualNode(currentElement) || previousElement.isEqualNode(currentElement))) {
                elements.pop();
                onEndElement.accept(currentElement);
            } else {
                
                onStartElement.accept(currentElement);
                
                List<Element> childElements = getChildElements(currentElement.getChildNodes());
                
                if (childElements.isEmpty()) {
                    Element parentElement = currentElement; 
                    
                    getTextContent(parentElement.getChildNodes()).stream().findAny().ifPresent(
                        e -> onLeafElementWithText.accept(parentElement, e)
                    );
                } else {
                    pushChildElements(elements, childElements);
                }
            }
            
            previousElement = currentElement;
        }
    }
    
    public void setOnStartElement(Consumer<Element> onStartElement) {
        this.onStartElement = onStartElement;
    }

    public void setOnLeafElementWithText(BiConsumer<Element, Text> onLeafElementWithText) {
        this.onLeafElementWithText = onLeafElementWithText;
    }

    public void setOnEndElement(Consumer<Element> onEndElement) {
        this.onEndElement = onEndElement;
    }
    
    public boolean isSkipDocumentRoot() {
        return skipDocumentRoot;
    }

    public void setSkipDocumentRoot(boolean skipDocumentRoot) {
        this.skipDocumentRoot = skipDocumentRoot;
    }
    
    private void pushChildElements(Deque<Element> elements, List<Element> childElements) {
        for (Element child : reverse(childElements)) {
            elements.push(child);
        } 
    }

    private DocumentBuilder createDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private Document document(Path path) {
        try {
            return document(new InputSource(newInputStream(path)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Document document(InputSource inputSource) {
        try {
            Document document = documentBuilder.parse(inputSource);
            document.getDocumentElement().normalize();
            return document;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private List<Element> getChildElements(NodeList nodes) {
        
        List<Element> childElements = new ArrayList<>();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == ELEMENT_NODE) {
                
                if (node.getParentNode() != null && node.getParentNode().getNodeName() != null) {
                    
                    String nodeName = node.getNodeName();
                    String grandParentName = node.getParentNode().getParentNode() == null? null : node.getParentNode().getParentNode().getNodeName();
                    
                    switch (node.getParentNode().getNodeName()) {
                        case "component":
                            if (!"component-type".equals(nodeName) && !"component-class".equals(nodeName)) {
                                continue;
                            }
                            break;
                        case "converter":
                            if (!"converter-id".equals(nodeName) && !"converter-class".equals(nodeName) && !"converter-for-class".equals(nodeName) ) {
                                continue;
                            }
                            break;
                        case "validator":
                            if (!"validator-id".equals(nodeName) && !"validator-class".equals(nodeName)) {
                                continue;
                            }
                            break;
                        case "render-kit":
                            if (!"render-kit-id".equals(nodeName) && !"render-kit-class".equals(nodeName) && !"renderer".equals(nodeName) && !"client-behavior-renderer".equals(nodeName)) {
                                continue;
                            }
                            break;
                        case "renderer":
                            if ("render-kit".equals(grandParentName)) {
                                if (!"component-family".equals(nodeName) && !"renderer-type".equals(nodeName) && !"renderer-class".equals(nodeName)) {
                                    continue;
                                }
                            }
                            break;
                        case "client-behavior-renderer":
                            if ("render-kit".equals(grandParentName)) {
                                if (!"client-behavior-renderer-type".equals(nodeName) && !"client-behavior-renderer-class".equals(nodeName)) {
                                    continue;
                                }
                            }
                            break;
                    }
                }
                
                childElements.add((Element) node);
            }
        }
        
        return childElements;
    }
    
    private List<Text> getTextContent(NodeList nodes) {
        
        List<Text> childElements = new ArrayList<>();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == TEXT_NODE) {
                childElements.add((Text) node);
            }
        }
        
        return childElements;
    }
    
    private List<Element> reverse(List<Element> list) {
        Collections.reverse(list);
        return list;
    }
    
    private static InputStream getInputStream(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        
        return new BufferedInputStream(connection.getInputStream());
    }

}
