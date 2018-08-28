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

package com.sun.faces.test.servlet30.systest;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SelectManyCollectionITCase extends HtmlUnitFacesITCase {

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public SelectManyCollectionITCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return new TestSuite(SelectManyCollectionITCase.class);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------------ Test Methods

    public void testSelectManyCollections() throws Exception {

        HtmlPage page = getPage("/faces/standard/selectmany05.xhtml");
        String[] selectIds = { "array", // 0
                "list", // 1
                "set", // 2
                "sortedset", // 3
                "collection", // 4
                "ilist", // 5
                "ilist2", // 6
                "iset", // 7
                "isortedset", // 8
                "icollection", // 9
                "hintString", "hintClass", "object", "intList1", "integerList1", "escape01", "escape02", "emptyItems", };
        int[] totalNumberOfSelections = { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 1, };
        String[][] initialSelectionLabelFragments = { new String[0], // 0
                new String[0], // 1
                new String[0], // 2
                new String[0], // 3
                new String[0], // 4
                new String[] { "Bilbo", "Pippin", "Merry" }, // 5
                new String[] { "Bilbo", "Pippin", "Merry" }, // 6
                new String[] { "Frodo" }, // 7
                new String[] { "Pippin", "Frodo" }, // 8
                new String[] { "Bilbo", "Merry" }, // 9
                new String[0], new String[0], new String[0], new String[0], new String[0],
                new String[] { "Bilbo - &lt;Ring Finder&gt;", "Merry - &lt;Trouble Maker&gt;" },
                // Unescaped label fragments. Fragments since difficult to get raw representation
                // using HtmlUnit
                // Can't check for <trouble maker since it's duplicate
                new String[] { "<ring finder", "Merry" }, new String[0], };

        String[][] postBackSelections = { new String[] { "Bilbo" }, new String[] { "Bilbo" }, new String[] { "Bilbo" },
                new String[] { "Bilbo" }, new String[] { "Bilbo" }, new String[] { "Bilbo" }, new String[] { "Bilbo" },
                new String[] { "Bilbo" }, new String[] { "Bilbo" }, new String[] { "Bilbo" }, new String[] { "Bilbo" },
                new String[] { "Bilbo" }, new String[] { "Bilbo" }, new String[] { "2" }, new String[] { "3" },
                new String[] { "Bilbo - &lt;Ring Finder&gt;" }, new String[] {}, new String[] {}, };

        // =====================================================================
        // Validate initial page state
        //
        List<HtmlSelect> selects = new ArrayList<HtmlSelect>(18);
        getAllElementsOfGivenClass(page, selects, HtmlSelect.class);
        assertTrue(selects.size() == 18);
        for (int i = 0; i < selectIds.length; i++) {
            String id = selectIds[i];
            // System.out.println("Validating HtmlSelect with ID: " + id);
            String[] initialSelection = initialSelectionLabelFragments[i];
            String[] newSelection = postBackSelections[i];
            HtmlSelect select = getHtmlSelectForId(selects, id);
            assertNotNull(select);

            validateState(select, totalNumberOfSelections[i], initialSelection);
            updateSelections(select, totalNumberOfSelections[i], newSelection);
        }

        HtmlInput input = getInputContainingGivenId(page, "command");
        page = (HtmlPage) input.click();

        // ensure no messages were queued by the post-back
        assertTrue(!page.asText().contains("Error"));

        selects.clear();
        getAllElementsOfGivenClass(page, selects, HtmlSelect.class);
        assertTrue(selects.size() == 18);
        for (int i = 0; i < selectIds.length; i++) {
            String id = selectIds[i];
            if ("escape02".equals(id)) {
                continue;
            }
            String[] newSelection = postBackSelections[i];
            HtmlSelect select = getHtmlSelectForId(selects, id);
            assertNotNull(select);
            validateState(select, totalNumberOfSelections[i], newSelection);
        }

    }

    // --------------------------------------------------------- Private Methods

    private void updateSelections(HtmlSelect select, int totalNumberOfOptions, String[] selectedOptions) {

        assertNotNull(select);
        List<HtmlOption> options = select.getOptions();
        assertTrue(options.size() == totalNumberOfOptions);
        for (String s : selectedOptions) {
            for (HtmlOption option : options) {
                option.setSelected(s.equals(option.asText()));
            }
        }
    }

    private void validateState(HtmlSelect select, int totalNumberOfOptions, String[] labelFragmentToLookFor) {

        assertNotNull(select);
        List<HtmlOption> options = select.getOptions();

        assertTrue(options.size() == totalNumberOfOptions);
        if (labelFragmentToLookFor == null || labelFragmentToLookFor.length == 0) {
            for (HtmlOption option : options) {
                // System.out.println(option.asText());
                assertTrue(!option.isSelected());
            }
        } else {
            for (String labelFragment : labelFragmentToLookFor) {

                // System.out.println("*** text to look for:" + text);

                for (HtmlOption option : options) {
//                    System.out.println("*** option as text:" + option.asText());
//                    System.out.println("*** option as xml:" + option.asXml());
//
//                    System.out.println("*** label attribute:" + option.getLabelAttribute());

                    if (option.asXml().contains(labelFragment)) {
                        assertTrue(option.isSelected());
                    }

                }
            }
        }

    }

    private HtmlSelect getHtmlSelectForId(List<HtmlSelect> selects, String id) {

        for (HtmlSelect select : selects) {
            if (select.getId().contains(id)) {
                return select;
            }
        }

        return null;
    }

}
