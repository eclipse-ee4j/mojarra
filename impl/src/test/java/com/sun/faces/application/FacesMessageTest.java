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

package com.sun.faces.application;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import jakarta.faces.application.FacesMessage;

public class FacesMessageTest {

    // Case 0 (nothing)
    @Test
    public void testSerializeable() throws Exception {
        FacesMessage message = null;
        message = new FacesMessage();
        persistAndCheck(message);
    }

    // Case 1 (summary)
    @Test
    public void testSerializeableSummary() throws Exception {
        FacesMessage message = null;
        message = new FacesMessage("This is a bad error.");
        persistAndCheck(message);
    }

    // Case 2 (summary & detail)
    @Test
    public void testSerializeableSummaryDetail() throws Exception {
        FacesMessage message = null;
        message = new FacesMessage("This is a bad error.", "This is a really bad error.");
        persistAndCheck(message);
    }

    // Case 3 (severity, summary & detail)
    @Test
    public void testSerializeableSummaryDetailSeverity() throws Exception {
        FacesMessage message = null;
        message = new FacesMessage(FacesMessage.SEVERITY_FATAL, "This is a bad error.",
                "This is a really bad error.");
        persistAndCheck(message);
    }

    private void persistAndCheck(FacesMessage message) {
        FacesMessage message1 = null;
        String mSummary, mSummary1 = null;
        String mDetail, mDetail1 = null;
        String severity, severity1 = null;
        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bis = null;

        mSummary = message.getSummary();
        mDetail = message.getDetail();
        severity = message.getSeverity().toString();

        try {
            bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(message);
            oos.close();
            byte[] bytes = bos.toByteArray();
            InputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(in);
            message1 = (FacesMessage)ois.readObject();
            ois.close();
            mSummary1 = message1.getSummary();
            mDetail1 = message1.getDetail();
            severity1 = message1.getSeverity().toString();
            if (null != mSummary1) {
                assertTrue(mSummary1.equals(mSummary));
            } else {
                assertTrue(mSummary == null);
            }
            if (null != mDetail1) {
                assertTrue(mDetail1.equals(mDetail));
            } else {
                assertTrue(mDetail == null);
            }
            if (null != severity1) {
                assertTrue(severity1.equals(severity));
            } else {
                assertTrue(severity == null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }



}
