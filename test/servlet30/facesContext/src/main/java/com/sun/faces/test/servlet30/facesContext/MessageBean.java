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

package com.sun.faces.test.servlet30.facesContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import static org.junit.Assert.*;

/**
 * The managed bean for the message tests.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
@ManagedBean(name = "messageBean")
@RequestScoped
public class MessageBean implements Serializable {

    public String getMessageResult1() {
        FacesContext context = FacesContext.getCurrentInstance();
        assertNotNull(context);

        try {
            context.addMessage(null, null);
            fail();
        } catch (NullPointerException exception) {
        }

        try {
            context.addMessage(null, null);
            fail();
        } catch (NullPointerException exception) {
        }
        return "PASSED";
    }
    
    public String getMessageResult2() {
        FacesContext context = FacesContext.getCurrentInstance();
        assertTrue(context != null);

        FacesMessage msg1 = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                             "summary1", "detail1");
        context.addMessage(null, msg1);

        FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_FATAL,
                                             "summary2", "detail2");
        context.addMessage(null, msg2);

        UICommand command = new UICommand();
        FacesMessage msg3 = new FacesMessage(FacesMessage.SEVERITY_FATAL,
                                             "summary3", "detail3");
        context.addMessage(command.getClientId(context), msg3);

        FacesMessage msg4 = new FacesMessage(FacesMessage.SEVERITY_WARN,
                                             "summary4", "detail4");
        context.addMessage(command.getClientId(context), msg4);

        assertTrue(context.getMaximumSeverity() == FacesMessage.SEVERITY_FATAL);

        List controlList = new ArrayList();
        controlList.add(msg1);
        controlList.add(msg2);
        controlList.add(msg3);
        controlList.add(msg4);
        Iterator it = context.getMessages();
        for (int i = 0, size = controlList.size(); i < size; i++) {
            assertTrue(controlList.get(i).equals(it.next()));
        }

        controlList.clear();
        controlList.add(msg3);
        controlList.add(msg4);
        it = context.getMessages(command.getClientId(context));
        for (int i = 0, size = controlList.size(); i < size; i++) {
            assertTrue(controlList.get(i).equals(it.next()));
        }

        controlList.clear();
        controlList.add(msg1);
        controlList.add(msg2);
        it = context.getMessages(null);
        for (int i = 0, size = controlList.size(); i < size; i++) {
            assertTrue(controlList.get(i).equals(it.next()));
        }
        return "PASSED";
    }

    public String getMessageResult3() {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage msg1 = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "");
        FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "");
        FacesMessage msg3 = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "");
        FacesMessage msg4 = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "");

        context.addMessage(null, msg1);
        context.addMessage("id1", msg2);
        context.addMessage("id2", msg3);
        context.addMessage("id2", msg4);

        Class unmodifiableType = Collections.unmodifiableList(Collections.emptyList()).getClass();

        List list = context.getMessageList(null);
        assertTrue(list.size() == 1);
        assertTrue(unmodifiableType.isInstance(list));
        assertTrue(msg1.equals(list.get(0)));

        list = context.getMessageList("id1");
        assertTrue(list.size() == 1);
        assertTrue(unmodifiableType.isInstance(list));
        assertTrue(msg2.equals(list.get(0)));

        list = context.getMessageList("id2");
        assertTrue(list.size() == 2);
        assertTrue(unmodifiableType.isInstance(list));
        assertTrue(msg3.equals(list.get(0)));
        assertTrue(msg4.equals(list.get(1)));

        list = context.getMessageList();
        assertTrue(list.size() == 4);
        assertTrue(unmodifiableType.isInstance(list));
        assertTrue(list.contains(msg1));
        assertTrue(list.contains(msg2));
        assertTrue(list.contains(msg3));
        assertTrue(list.contains(msg4));
        return "PASSED";
    }

    public String getMessageResult4() {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage msg1 = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "");
        FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "");

        context.addMessage(null, msg2);
        context.addMessage(null, msg1);

        assertTrue(FacesMessage.SEVERITY_WARN.equals(context.getMaximumSeverity()));
        return "PASSED";
    }

    public String getMessageResult5() {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage msg1 = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "");
        FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "");
        FacesMessage msg3 = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "");

        context.addMessage(null, msg2);
        context.addMessage(null, msg1);
        context.addMessage(null, msg3);

        assertTrue(FacesMessage.SEVERITY_ERROR.equals(context.getMaximumSeverity()));
        return "PASSED";
    }

    public String getMessageResult6() {
        FacesContext context = FacesContext.getCurrentInstance();
        Iterator<FacesMessage> messages = context.getMessages();
        assertTrue(!messages.hasNext());

        FacesMessage msg1 = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "");
        FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "");
        FacesMessage msg3 = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "");
        FacesMessage msg4 = new FacesMessage(FacesMessage.SEVERITY_FATAL, "", "");
        context.addMessage(null, msg2);
        context.addMessage(null, msg1);
        context.addMessage(null, msg3);

        messages = context.getMessages();
        assertTrue(messages.hasNext());
        while (messages.hasNext()) {
            messages.next();
            messages.remove();
        }
        assertTrue(context.getMaximumSeverity() == null);

        context.addMessage("id1", msg1);
        context.addMessage("id3", msg1);
        context.addMessage("id3", msg3);
        context.addMessage("id3", msg1);
        context.addMessage(null, msg4);
        assertTrue(context.getMaximumSeverity() == FacesMessage.SEVERITY_FATAL);

        for (Iterator<FacesMessage> i = context.getMessages(); i.hasNext(); ) {
            FacesMessage m = i.next();
            if (m.getSeverity() == FacesMessage.SEVERITY_FATAL) {
                i.remove();
            }
        }
        assertTrue(context.getMaximumSeverity() == FacesMessage.SEVERITY_ERROR);

        for (Iterator<FacesMessage> i = context.getMessages(); i.hasNext(); ) {
            FacesMessage m = i.next();
            if (m.getSeverity() == FacesMessage.SEVERITY_ERROR) {
                i.remove();
            }
        }
        assertTrue(context.getMaximumSeverity() == FacesMessage.SEVERITY_INFO);

        for (Iterator<FacesMessage> i = context.getMessages(); i.hasNext(); ) {
            FacesMessage m = i.next();
            if (m.getSeverity() == FacesMessage.SEVERITY_INFO) {
                i.remove();
            }
        }
        assertTrue(context.getMaximumSeverity() == null);
        return "PASSED";
    }

    public String getMessageResult7() {
        FacesContext context = FacesContext.getCurrentInstance();
        Iterator<FacesMessage> messages = context.getMessages();
        assertTrue(!messages.hasNext());

        FacesMessage msg1 = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "");
        FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "");
        FacesMessage msg3 = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "");
        FacesMessage msg4 = new FacesMessage(FacesMessage.SEVERITY_FATAL, "", "");
        context.addMessage("id1", msg1);
        context.addMessage("id3", msg2);
        context.addMessage("id3", msg3);
        context.addMessage("id3", msg4);
        context.addMessage("id2", msg1);

        for (Iterator<String> i = context.getClientIdsWithMessages();
              i.hasNext();) {
            String id = i.next();
            if ("id3".equals(id)) {
                i.remove();
            }
        }

        assertTrue(!context.getMessages("id3").hasNext());
        assertTrue(context.getMaximumSeverity() == FacesMessage.SEVERITY_INFO);

        for (Iterator<String> i = context.getClientIdsWithMessages();
              i.hasNext();) {
            i.next();
            i.remove();
        }

        assertTrue(context.getMaximumSeverity() == null);
        return "PASSED";
    }

    public String getMessageResult8() {
        FacesContext context = FacesContext.getCurrentInstance();

        // we use a custom iterator for iterating over all messages.
        // ensure the proper exceptions are thrown by next() and remove()
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "", ""));

        // next should throw NoSuchElementException after the second call to next()
        Iterator i = context.getMessages();
        i.next();
        try {
            i.next();
            assertTrue(false);
        } catch (NoSuchElementException nsee) { }

        // remove should throw an IllegalStateException if called without having
        // called next()
        i = context.getMessages();
        try {
            i.remove();
            assertTrue(false);
        } catch (IllegalStateException ise) { }               

        return "PASSED";
    }
}
