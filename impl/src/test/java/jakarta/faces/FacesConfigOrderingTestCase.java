/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces;

import static com.sun.faces.util.Util.createLocalDocumentBuilderFactory;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.config.manager.documents.DocumentOrderingWrapper;

import jakarta.faces.context.FacesContext;

public class FacesConfigOrderingTestCase {

	// ---------------------------------------------------- Overall Test Methods
	// Set up instance variables required by this test case.
	@BeforeEach
	public void setUp() throws Exception {
		Method method = FacesContext.class.getDeclaredMethod(
				"setCurrentInstance", FacesContext.class);
		method.setAccessible(true);
		method.invoke(null, new Object[] { null });

	}

	// ------------------------------------------------- Individual Test Methods
	/**
	 * <p>
	 * verify that the overrides specified in the faces-config.xml in the user's
	 * webapp take precedence.
	 * </p>
	 *
	 * @throws java.lang.Exception
	 */
    @Test
	public void testNoOrderingStartWithCab() throws Exception {
		DocumentInfo docC = createDocument("C", null, null);
		DocumentInfo doca = createDocument("a", null, null);
		DocumentInfo docb = createDocument(null, null, null);

		List<DocumentOrderingWrapper> documents = new ArrayList<DocumentOrderingWrapper>();
		// J-
		Collections.addAll(documents, new DocumentOrderingWrapper(docC),
				new DocumentOrderingWrapper(doca), new DocumentOrderingWrapper(
						docb));
		// J+

		DocumentOrderingWrapper[] wrappers = documents
				.toArray(new DocumentOrderingWrapper[documents.size()]);
		String[] originalOrder = extractNames(wrappers);
		DocumentOrderingWrapper.sort(wrappers);

		String[] orderedNames = extractNames(wrappers);

		// a solution:
		// ['C', 'a', '']
		List<String> original = Arrays.asList(originalOrder);
		List<String> actually = Arrays.asList(orderedNames);

		List<String> possibility1 = Arrays.asList("C", "a", "");

		boolean assertion = (actually.equals(possibility1));
		String message = "\n original: " + original + "\n expected: "
				+ possibility1 + "\n actually: " + actually + "\n";
		assertTrue(assertion, message);
		System.out.println("testNoOrderingStartWithCab: Passed" + message);
	}

    @Test
	public void testCafteraStartWithCab() throws Exception {
		List<String> docCAfterIds = new ArrayList<String>();
		Collections.addAll(docCAfterIds, "a");

		DocumentInfo docC = createDocument("C", null, docCAfterIds);
		DocumentInfo doca = createDocument("a", null, null);
		DocumentInfo docb = createDocument(null, null, null);

		List<DocumentOrderingWrapper> documents = new ArrayList<DocumentOrderingWrapper>();
		//J-
		Collections.addAll(documents,
			new DocumentOrderingWrapper(docC),
			new DocumentOrderingWrapper(doca),
			new DocumentOrderingWrapper(docb)
		);
		//J+

		DocumentOrderingWrapper[] wrappers = documents.toArray(new DocumentOrderingWrapper[documents.size()]);
		String[] originalOrder = extractNames(wrappers);
		DocumentOrderingWrapper.sort(wrappers);

		String[] orderedNames = extractNames(wrappers);

		// a solution:
		// ['a', '', 'C']
		List<String> original = Arrays.asList(originalOrder);
		List<String> actually = Arrays.asList(orderedNames);

		List<String> possibility1 = Arrays.asList("a", "", "C");

		boolean assertion = (actually.equals(possibility1));
		String message = "\n original: " + original + "\n expected: " + possibility1 +
			"\n actually: " + actually + "\n";
		assertTrue(assertion, message);
		System.out.println("testCafteraStartWithCab: Passed" + message);
	}

    @Test
	public void testAafterD_BafterCbeforeOthers_CafterDbeforeB_startWithABCD() throws Exception {
		List<String> docAAfterIds = new ArrayList<String>();
		Collections.addAll(docAAfterIds, "D");

		// C should before B, hence B needs to be after C
		List<String> docBAfterIds = new ArrayList<String>();
		Collections.addAll(docBAfterIds, "C");

		List<String> docBBeforeIds = new ArrayList<String>();
		Collections.addAll(docBBeforeIds, "@others");

		List<String> docCAfterIds = new ArrayList<String>();
		Collections.addAll(docCAfterIds, "D");

		List<String> docCBeforeIds = new ArrayList<String>();
		Collections.addAll(docCBeforeIds, "B");

		DocumentInfo docA = createDocument("A", null, docAAfterIds);
		DocumentInfo docB = createDocument("B", docBBeforeIds, docBAfterIds);
		DocumentInfo docC = createDocument("C", docCBeforeIds, docCAfterIds);
		DocumentInfo docD = createDocument("D", null, null);

		List<DocumentOrderingWrapper> documents = new ArrayList<DocumentOrderingWrapper>();
		//J-
		Collections.addAll(documents,
			new DocumentOrderingWrapper(docA),
			new DocumentOrderingWrapper(docB),
			new DocumentOrderingWrapper(docC),
			new DocumentOrderingWrapper(docD)
		);
		//J+

		DocumentOrderingWrapper[] wrappers = documents.toArray(new DocumentOrderingWrapper[documents.size()]);
		String[] originalOrder = extractNames(wrappers);
		DocumentOrderingWrapper.sort(wrappers);

		String[] orderedNames = extractNames(wrappers);

		// a solution:
		// ['D', 'C', 'B', 'A']
		List<String> original = Arrays.asList(originalOrder);
		List<String> actually = Arrays.asList(orderedNames);

		List<String> possibility1 = Arrays.asList("D", "C", "B", "A");

		boolean assertion = (actually.equals(possibility1)
				);
		String message = "\n original: " + original + "\n expected: " + possibility1 +
			"\n actually: " + actually + "\n";
		assertTrue(assertion, message);
		System.out.println("testAafterD_BafterCbeforeOthers_CafterDbeforeB_startWithABCD: Passed" + message);
	}

    @Test
	public void testAafterD_BafterCbeforeOthers_CafterDbeforeB_startWithADBC() throws Exception {

		List<String> docAAfterIds = new ArrayList<String>();
		Collections.addAll(docAAfterIds, "D");

		// C should before B, hence B needs to be after C
		List<String> docBAfterIds = new ArrayList<String>();
		Collections.addAll(docBAfterIds, "C");

		List<String> docBBeforeIds = new ArrayList<String>();
		Collections.addAll(docBBeforeIds, "@others");

		List<String> docCAfterIds = new ArrayList<String>();
		Collections.addAll(docCAfterIds, "D");

		List<String> docCBeforeIds = new ArrayList<String>();
		Collections.addAll(docCBeforeIds, "B");

		DocumentInfo docA = createDocument("A", null, docAAfterIds);
		DocumentInfo docB = createDocument("B", docBBeforeIds, docBAfterIds);
		DocumentInfo docC = createDocument("C", docCBeforeIds, docCAfterIds);
		DocumentInfo docD = createDocument("D", null, null);

		List<DocumentOrderingWrapper> documents = new ArrayList<DocumentOrderingWrapper>();
		//J-
		Collections.addAll(documents,
			new DocumentOrderingWrapper(docA),
			new DocumentOrderingWrapper(docD),
			new DocumentOrderingWrapper(docB),
			new DocumentOrderingWrapper(docC)
		);
		//J+

		DocumentOrderingWrapper[] wrappers = documents.toArray(new DocumentOrderingWrapper[documents.size()]);
		String[] originalOrder = extractNames(wrappers);
		DocumentOrderingWrapper.sort(wrappers);

		String[] orderedNames = extractNames(wrappers);

		// a solution:
		// ['D', 'C', 'B', 'A']
		List<String> original = Arrays.asList(originalOrder);
		List<String> actually = Arrays.asList(orderedNames);

		List<String> possibility1 = Arrays.asList("D", "C", "B", "A");

		boolean assertion = (actually.equals(possibility1)
				);
		String message = "\n original: " + original + "\n expected: " + possibility1 +
			"\n actually: " + actually + "\n";
		assertTrue(assertion, message);
		System.out.println("testAafterD_BafterCbeforeOthers_CafterDbeforeB_startWithADBC: Passed" + message);

	}

    @Test
	public void testAafterD_BafterCbeforeOthers_CafterDbeforeB_shuffle() throws Exception {

		List<String> docAAfterIds = new ArrayList<String>();
		Collections.addAll(docAAfterIds, "D");

		// C should before B, hence B needs to be after C
		List<String> docBAfterIds = new ArrayList<String>();
		Collections.addAll(docBAfterIds, "C");

		List<String> docBBeforeIds = new ArrayList<String>();
		Collections.addAll(docBBeforeIds, "@others");

		List<String> docCAfterIds = new ArrayList<String>();
		Collections.addAll(docCAfterIds, "D");

		List<String> docCBeforeIds = new ArrayList<String>();
		Collections.addAll(docCBeforeIds, "B");

		DocumentInfo docA = createDocument("A", null, docAAfterIds);
		DocumentInfo docB = createDocument("B", docBBeforeIds, docBAfterIds);
		DocumentInfo docC = createDocument("C", docCBeforeIds, docCAfterIds);
		DocumentInfo docD = createDocument("D", null, null);

		List<DocumentOrderingWrapper> documents = new ArrayList<DocumentOrderingWrapper>();
		//J-
		Collections.addAll(documents,
			new DocumentOrderingWrapper(docA),
			new DocumentOrderingWrapper(docB),
			new DocumentOrderingWrapper(docC),
			new DocumentOrderingWrapper(docD)
		);
		//J+

		int number = 100;
		for (int i = 0; i < number; i++) {

			Collections.shuffle(documents);

			DocumentOrderingWrapper[] wrappers = documents.toArray(new DocumentOrderingWrapper[documents.size()]);
			String[] originalOrder = extractNames(wrappers);
			DocumentOrderingWrapper.sort(wrappers);

			String[] orderedNames = extractNames(wrappers);

			// some solutions:
			// [D, C, B, A]
			// [D, A, C, B]
			List<String> original = Arrays.asList(originalOrder);
			List<String> actually = Arrays.asList(orderedNames);

			List<String> possibility1 = Arrays.asList("D", "C", "B", "A");
			List<String> possibility2 = Arrays.asList("D", "A", "C", "B");

			boolean assertion = (actually.equals(possibility1)
					|| actually.equals(possibility2)
				);
			String message = "\n original: " + original +
				"\n expected: " + possibility1 +
				"\n       or: " + possibility2 +
				"\n actually: " + actually + "\n";
			assertTrue(assertion, message);

		}

		System.out.println("testAafterD_BafterCbeforeOthers_CafterDbeforeB_shuffle: " + number + " shuffles passed.");

	}

	private DocumentInfo createDocument(String documentId,
			List<String> beforeIds, List<String> afterIds) throws Exception {

		String ns = "http://java.sun.com/xml/ns/javaee";
		Document document = newDocument();
		Element root = document.createElementNS(ns, "faces-config");
		if (documentId != null) {
			Element nameElement = document.createElementNS(ns, "name");
			nameElement.setTextContent(documentId);
			root.appendChild(nameElement);
		}
		document.appendChild(root);
		boolean hasBefore = (beforeIds != null && !beforeIds.isEmpty());
		boolean hasAfter = (afterIds != null && !afterIds.isEmpty());
		boolean createOrdering = (hasBefore || hasAfter);
		if (createOrdering) {
			Element ordering = document.createElementNS(ns, "ordering");
			root.appendChild(ordering);
			if (hasBefore) {
				populateIds("before", beforeIds, ns, document, ordering);
			}
			if (hasAfter) {
				populateIds("after", afterIds, ns, document, ordering);
			}
		}

		return new DocumentInfo(document, null);

	}

	public static String[] extractNames(DocumentOrderingWrapper[] documents) {
		String[] extractedNames = new String[documents.length];
		int i = 0;

		for (DocumentOrderingWrapper w : documents) {
			extractedNames[i] = w.getDocumentId();
			i++;
		}

		return extractedNames;
	}

	private void populateIds(String elementName, List<String> ids, String ns,
			Document document, Element ordering) {

		Element element = document.createElementNS(ns, elementName);
		ordering.appendChild(element);
		for (String id : ids) {
			Element append;
			if ("@others".equals(id)) {
				append = document.createElementNS(ns, "others");
			} else {
				append = document.createElementNS(ns, "name");
				append.setTextContent(id);
			}
			element.appendChild(append);
		}

	}

	private Document newDocument() throws ParserConfigurationException {

		DocumentBuilderFactory factory = createLocalDocumentBuilderFactory();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().newDocument();

	}
}
