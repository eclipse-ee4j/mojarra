/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.config;

import static jakarta.faces.application.ProjectStage.PROJECT_STAGE_JNDI_NAME;
import static jakarta.faces.application.ProjectStage.PROJECT_STAGE_PARAM_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import jakarta.faces.application.ProjectStage;

import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * The project stage may be configured through a JNDI environment entry, which every Jakarta EE server supports and which takes precedence over the context
 * parameter. Nothing else can reach that path, since a mock ServletContext has no JNDI.
 */
class JndiProjectStageTest {

    private static final String FACTORY_PROPERTY = "java.naming.factory.initial";

    private static String stageInJndi;
    private String originalFactory;

    @BeforeEach
    void installNamingFactory() {
        originalFactory = System.getProperty(FACTORY_PROPERTY);
        System.setProperty(FACTORY_PROPERTY, StageContextFactory.class.getName());
    }

    @AfterEach
    void restoreNamingFactory() {
        stageInJndi = null;

        if (originalFactory == null) {
            System.clearProperty(FACTORY_PROPERTY);
        }
        else {
            System.setProperty(FACTORY_PROPERTY, originalFactory);
        }
    }

    /**
     * Reading the entry is what decides the stage, and the stage is what decides at which level the entries themselves are reported, so the two cannot simply
     * be done in one pass.
     */
    @Test
    void theEnvironmentEntryDecidesTheStage() {
        stageInJndi = ProjectStage.Development.name();

        assertEquals(ProjectStage.Development, WebConfiguration.getInstance(new MockServletContext()).getProjectStage());
    }

    @Test
    void theEnvironmentEntryWinsOverTheContextParameter() {
        stageInJndi = ProjectStage.Development.name();

        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(PROJECT_STAGE_PARAM_NAME, ProjectStage.Production.name());

        assertEquals(ProjectStage.Development, WebConfiguration.getInstance(servletContext).getProjectStage());
    }

    @Test
    void theContextParameterAppliesWhenThereIsNoEntry() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(PROJECT_STAGE_PARAM_NAME, ProjectStage.SystemTest.name());

        assertEquals(ProjectStage.SystemTest, WebConfiguration.getInstance(servletContext).getProjectStage());
    }

    /**
     * Serves only the project stage entry, and reports every other name as absent the way a real server does.
     */
    public static final class StageContextFactory implements InitialContextFactory {

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            Context context = Mockito.mock(Context.class);

            Mockito.when(context.lookup(Mockito.anyString())).thenAnswer(invocation -> {
                if (PROJECT_STAGE_JNDI_NAME.equals(invocation.getArgument(0)) && stageInJndi != null) {
                    return stageInJndi;
                }

                throw new NamingException("not bound");
            });

            Mockito.when(context.lookup(Mockito.any(Name.class))).thenThrow(new NamingException("not bound"));

            return context;
        }

    }

}
