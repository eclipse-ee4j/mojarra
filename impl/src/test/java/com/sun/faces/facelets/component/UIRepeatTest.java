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

package com.sun.faces.facelets.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.context.FacesContext;

public class UIRepeatTest {

	private FacesContext ctx;

	private FacesMessage.Severity maximumSeverity = FacesMessage.SEVERITY_WARN;

	private Method uiRepeatHasErrorMessages;

	@Test
	public void testHasErrorMessages() throws Exception {
		ctx = Mockito.mock(FacesContext.class);
		when(ctx.getMaximumSeverity()).thenAnswer(new Answer<Severity>() {
		    @Override
		    public Severity answer(InvocationOnMock invocation) throws Throwable {
                return maximumSeverity;
		    }
		});

		maximumSeverity = FacesMessage.SEVERITY_WARN;
		assertEquals(false, hasErrorMessages(ctx));
		maximumSeverity = FacesMessage.SEVERITY_INFO;
		assertEquals(false, hasErrorMessages(ctx));
		maximumSeverity = FacesMessage.SEVERITY_ERROR;
		assertEquals(true, hasErrorMessages(ctx));
		maximumSeverity = FacesMessage.SEVERITY_FATAL;
		assertEquals(true, hasErrorMessages(ctx));
	}

	private boolean hasErrorMessages(FacesContext context) throws Exception {
		if (uiRepeatHasErrorMessages == null) {
			Class<?> uiRepeatClass = Class.forName(UIRepeat.class.getName());
			uiRepeatHasErrorMessages = uiRepeatClass.getDeclaredMethod(
					"hasErrorMessages", new Class[] { FacesContext.class });
			uiRepeatHasErrorMessages.setAccessible(true);
		}
		return (Boolean)uiRepeatHasErrorMessages.invoke(new UIRepeat(),
				new Object[] { context });
	}
}
