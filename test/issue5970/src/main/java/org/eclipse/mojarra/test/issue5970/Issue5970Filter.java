/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.issue5970;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

/**
 * What the state management strategy reports about the view rebuilt for a postback goes to the log and nowhere else,
 * which leaves it invisible to a test. This collects the reports of the request being served into the
 * {@value #REPORTS} request attribute, from where a view renders them for the response to be asserted against.
 */
@WebFilter("/*")
public class Issue5970Filter implements Filter {

    /**
     * The request attribute holding the reports logged while the request is served.
     */
    public static final String REPORTS = "issue5970Reports";

    private static final String LOGGER_NAME = "jakarta.enterprise.resource.webcontainer.faces.application.view";

    private static final ThreadLocal<List<String>> CURRENT = new ThreadLocal<>();

    /**
     * Held onto for as long as the handler is installed on it, since a logger nothing references is collectable
     * along with the handlers it was given.
     */
    private Logger logger;

    private Handler collector;

    @Override
    public void init(FilterConfig config) {
        collector = new ReportCollector();
        logger = Logger.getLogger(LOGGER_NAME);
        logger.addHandler(collector);
    }

    @Override
    public void destroy() {
        logger.removeHandler(collector);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        List<String> reports = new ArrayList<>();
        request.setAttribute(REPORTS, reports);
        CURRENT.set(reports);

        try {
            chain.doFilter(request, response);
        } finally {
            CURRENT.remove();
        }
    }

    private static class ReportCollector extends Handler {

        private final SimpleFormatter formatter = new SimpleFormatter();

        @Override
        public void publish(LogRecord record) {
            List<String> reports = CURRENT.get();

            if (reports != null && record.getLevel().intValue() >= Level.WARNING.intValue()) {
                reports.add(formatter.formatMessage(record));
            }
        }

        @Override
        public void flush() {
            // Nothing to flush, the reports are collected in memory.
        }

        @Override
        public void close() {
            // Nothing to close, the reports are collected in memory.
        }
    }
}
