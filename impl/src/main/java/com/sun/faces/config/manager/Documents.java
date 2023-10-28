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

import static com.sun.faces.RIConstants.DOCUMENT_NAMESPACE;
import static com.sun.faces.RIConstants.DOCUMENT_VERSION;
import static com.sun.faces.util.Util.createLocalDocumentBuilderFactory;
import static com.sun.faces.util.Util.isEmpty;
import static java.util.Arrays.asList;
import static java.util.logging.Level.INFO;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.sun.faces.config.ConfigurationException;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.config.manager.documents.DocumentOrderingWrapper;
import com.sun.faces.config.manager.tasks.FindConfigResourceURIsTask;
import com.sun.faces.config.manager.tasks.ParseConfigResourceToDOMTask;
import com.sun.faces.spi.ConfigurationResourceProvider;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.ApplicationConfigurationPopulator;
import jakarta.servlet.ServletContext;

public class Documents {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <p>
     * Obtains an array of <code>Document</code>s to be processed
     * </p>
     *
     * @param servletContext the <code>ServletContext</code> for the application to be processed
     * @param providers <code>List</code> of <code>ConfigurationResourceProvider</code> instances that provide the URL of
     * the documents to parse.
     * @param executor the <code>ExecutorService</code> used to dispatch parse request to
     * @param validating flag indicating whether or not the documents should be validated
     * @return an array of <code>DocumentInfo</code>s
     */
    public static DocumentInfo[] getXMLDocuments(ServletContext servletContext, List<ConfigurationResourceProvider> providers, ExecutorService executor,
            boolean validating) {

        // Query all configuration providers to give us a URL to the configuration they are providing

        List<FutureTask<Collection<URI>>> uriTasks = new ArrayList<>(providers.size());

        for (ConfigurationResourceProvider provider : providers) {
            FutureTask<Collection<URI>> uriTask = new FutureTask<>(new FindConfigResourceURIsTask(provider, servletContext));
            uriTasks.add(uriTask);

            if (executor != null) {
                executor.execute(uriTask);
            } else {
                uriTask.run();
            }
        }

        // Load and XML parse all documents to which the URLs that we collected above point to

        List<FutureTask<DocumentInfo>> docTasks = new ArrayList<>(providers.size() << 1);
        Set<URI> processedUris = new HashSet<>();

        for (FutureTask<Collection<URI>> uriTask : uriTasks) {
            try {
                for (URI uri : uriTask.get()) {
                    if (processedUris.add(uri)) {
                        FutureTask<DocumentInfo> docTask = new FutureTask<>(new ParseConfigResourceToDOMTask(servletContext, validating, uri));
                        docTasks.add(docTask);
                        
                        if (executor != null) {
                            executor.execute(docTask);
                        } else {
                            docTask.run();
                        }
                    }
                }
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }
        }

        // Collect the results of the documents we parsed above

        List<DocumentInfo> docs = new ArrayList<>(docTasks.size());
        for (FutureTask<DocumentInfo> docTask : docTasks) {
            try {
                docs.add(docTask.get());
            } catch (ExecutionException e) {
                throw new ConfigurationException(e);
            } catch (InterruptedException ignored) {
            }
        }

        return docs.toArray(new DocumentInfo[docs.size()]);
    }

    public static List<DocumentInfo> getProgrammaticDocuments(List<ApplicationConfigurationPopulator> configPopulators) throws ParserConfigurationException {

        List<DocumentInfo> programmaticDocuments = new ArrayList<>();

        DOMImplementation domImpl = createDOMImplementation();
        for (ApplicationConfigurationPopulator populator : configPopulators) {

            Document facesConfigDoc = createEmptyFacesConfigDocument(domImpl);

            try {
                populator.populateApplicationConfiguration(facesConfigDoc);

                programmaticDocuments.add(new DocumentInfo(facesConfigDoc, null));
            } catch (Throwable e) {
                if (LOGGER.isLoggable(INFO)) {
                    LOGGER.log(INFO, "{0} thrown when invoking {1}.populateApplicationConfigurationResources: {2}",
                            new String[] { e.getClass().getName(), populator.getClass().getName(), e.getMessage() });
                }
            }
        }

        return programmaticDocuments;
    }

    public static DocumentInfo[] mergeDocuments(DocumentInfo[] facesDocuments, List<DocumentInfo> programmaticDocuments) {

        if (programmaticDocuments.isEmpty()) {
            return facesDocuments;
        }

        if (isEmpty(facesDocuments)) {
            return programmaticDocuments.toArray(new DocumentInfo[0]);
        }

        List<DocumentInfo> mergedDocuments = new ArrayList<>(facesDocuments.length + programmaticDocuments.size());

        // The first programmaticDocuments element represents the faces implementation,
        // and should be the first one in the merged list
        mergedDocuments.add(programmaticDocuments.get(0));

        // Copy the existing facesDocuments next to the merged list
        mergedDocuments.addAll(asList(facesDocuments));

        // Copy the programmaticDocuments next, but skip the first one as we've already added that
        mergedDocuments.addAll(programmaticDocuments.subList(1, programmaticDocuments.size()));

        return mergedDocuments.toArray(new DocumentInfo[0]);
    }

    /**
     * <p>
     * Sort the <code>faces-config</code> documents found on the classpath and those specified by the
     * <code>jakarta.faces.CONFIG_FILES</code> context init parameter.
     * </p>
     *
     * @param facesDocuments an array of <em>all</em> <code>faces-config</code> documents
     * @param webInfFacesConfig FacesConfigInfo representing the WEB-INF/faces-config.xml for this app
     *
     * @return the sorted documents
     */
    public static DocumentInfo[] sortDocuments(DocumentInfo[] facesDocuments, FacesConfigInfo webInfFacesConfig) {

        int len = webInfFacesConfig.isWebInfFacesConfig() ? facesDocuments.length - 1 : facesDocuments.length;

        List<String> absoluteOrdering = webInfFacesConfig.getAbsoluteOrdering();

        if (len > 1) {
            List<DocumentOrderingWrapper> list = new ArrayList<>();
            for (int i = 1; i < len; i++) {
                list.add(new DocumentOrderingWrapper(facesDocuments[i]));
            }

            DocumentOrderingWrapper[] ordering = list.toArray(new DocumentOrderingWrapper[list.size()]);
            if (absoluteOrdering == null) {
                DocumentOrderingWrapper.sort(ordering);

                // Sorting complete, now update the appropriate locations within
                // the original array with the sorted documentation.
                for (int i = 1; i < len; i++) {
                    facesDocuments[i] = ordering[i - 1].getDocument();
                }

                return facesDocuments;
            } else {
                DocumentOrderingWrapper[] result = DocumentOrderingWrapper.sort(ordering, absoluteOrdering);
                DocumentInfo[] ret = new DocumentInfo[webInfFacesConfig.isWebInfFacesConfig() ? result.length + 2 : result.length + 1];

                for (int i = 1; i < len; i++) {
                    ret[i] = result[i - 1].getDocument();
                }

                // Add the impl specific config file
                ret[0] = facesDocuments[0];

                // Add the WEB-INF if necessary
                if (webInfFacesConfig.isWebInfFacesConfig()) {
                    ret[ret.length - 1] = facesDocuments[facesDocuments.length - 1];
                }
                return ret;
            }
        }

        return facesDocuments;
    }

    private static DOMImplementation createDOMImplementation() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = createLocalDocumentBuilderFactory();
        documentBuilderFactory.setNamespaceAware(true);

        return documentBuilderFactory.newDocumentBuilder().getDOMImplementation();
    }

    private static Document createEmptyFacesConfigDocument(DOMImplementation domImpl) {
        Document document = domImpl.createDocument(DOCUMENT_NAMESPACE, "faces-config", null);

        Attr versionAttribute = document.createAttribute("version");
        versionAttribute.setValue(DOCUMENT_VERSION);
        document.getDocumentElement().getAttributes().setNamedItem(versionAttribute);

        return document;
    }

}
