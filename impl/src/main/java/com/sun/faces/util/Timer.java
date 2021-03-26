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

package com.sun.faces.util;

import static java.util.logging.Level.FINE;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a simple wrapper for timing method calls. The traditional method is to add two variables, start, and
 * stop, and display the difference of these values. Encapsulates the process.
 */
public class Timer {

    private static final Logger LOGGER = FacesLogger.TIMING.getLogger();

    private final Level logLevel;

    private long start;
    private long stop;

    // ------------------------------------------------------------ Constructors

    private Timer(final Level logLevel) {
        this.logLevel = logLevel;
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * @return a new <code>Timer</code> instance if the <code>TIMING</code> logging level is <code>FINE</code>, otherwise,
     * return null;
     */
    public static Timer getInstance() {
        return getInstance(FINE);
    }

    public static Timer getInstance(Level logLevel) {
        if (LOGGER.isLoggable(logLevel)) {
            return new Timer(logLevel);
        }

        return null;
    }

    /**
     * Start timing.
     */
    public void startTiming() {
        start = System.currentTimeMillis();
    }

    /**
     * Stop timing.
     */
    public void stopTiming() {
        stop = System.currentTimeMillis();
    }

    /**
     * Log the timing result.
     *
     * @param taskInfo task description
     */
    public void logResult(String taskInfo) {
        if (LOGGER.isLoggable(logLevel)) {
            LOGGER.log(logLevel, " [TIMING] - [" + getTimingResult() + "ms] : " + taskInfo);
        }
    }

    // --------------------------------------------------------- Private Methods

    /**
     * @return the time for this task
     */
    private long getTimingResult() {
        return stop - start;
    }
}
