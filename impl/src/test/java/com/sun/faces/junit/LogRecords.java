/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.faces.junit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Captures the records which a logger publishes while running a piece of code.
 */
public final class LogRecords {

    private LogRecords() {
    }

    /**
     * Runs the given code and returns the records of at least the given level which the given logger published meanwhile.
     *
     * @param logger the logger to capture
     * @param level the minimum level of the records to return
     * @param runnable the code to run
     * @return the captured records, in the order they were published
     */
    public static List<LogRecord> capture(Logger logger, Level level, Runnable runnable) {
        List<LogRecord> records = new CopyOnWriteArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                if (logRecord.getLevel().intValue() >= level.intValue()) {
                    records.add(logRecord);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(handler);
        try {
            runnable.run();
        } finally {
            logger.removeHandler(handler);
        }
        return records;
    }

}
