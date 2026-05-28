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

package com.sun.faces.perf;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import jakarta.faces.context.PartialResponseWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.renderkit.html_basic.HtmlResponseWriter;

/**
 * Manual performance harness for the response-writer hot paths exercised during Render Response
 * and Ajax responses: per-character escaping in {@code writeText}/{@code writeAttribute}/
 * {@code writeURIAttribute}, element framing in {@code startElement}/{@code endElement}, and the
 * wrapping cost of {@link PartialResponseWriter} for Ajax updates.
 *
 * <p>Output goes through a {@link NullWriter} (discards bytes without buffering or allocation) so
 * each scenario measures pure encode/escape/dispatch cost without I/O or buffering noise. Each
 * scenario reports the median ns/op over {@value #RUNS} measurement runs of {@value #ITERATIONS}
 * iterations each (after {@value #WARMUP} warmup iterations).
 *
 * <p>Disabled by default. To run:
 * {@code mvn -pl impl test -Dtest=ResponseWriterPerfHarness -Dperf=true}.
 *
 * <p>Scenarios cover both common cases (plain ASCII -- no escape branches hit) and worst cases
 * (text containing every HTML-special character).
 */
@EnabledIfSystemProperty(named = "perf", matches = "true")
public class ResponseWriterPerfHarness extends JUnitFacesTestCaseBase {

    private static final int WARMUP = 100_000;
    private static final int ITERATIONS = 1_000_000;
    private static final int RUNS = 5;

    private static boolean headerPrinted = false;

    private HtmlResponseWriter htmlWriter;
    private NullWriter sink;

    private static final String SHORT_PLAIN_TEXT = "Submit";                       // 6 ASCII chars, button label
    private static final String SHORT_HTML_TEXT  = "a > b & c < d";                // 13 chars, hits <, >, & escapes
    private static final String LONG_PLAIN_TEXT  = "The quick brown fox jumps over the lazy dog 1234567890";   // >16 chars, fast path
    private static final String LONG_HTML_TEXT   = "<p class=\"x\">a > b & c < d & e</p> and \"quoted\" too"; // >16 chars, lots of escapes
    private static final String PLAIN_ID         = "form:input123";                // typical client id, no escapes
    private static final String ATTR_WITH_QUOTES = "background: url('x.png'); color: \"red\"";
    private static final String URI_VALUE        = "/app/path?foo=bar&baz=qux#section";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        sink = new NullWriter();
        htmlWriter = new HtmlResponseWriter(sink, "text/html", "UTF-8");
        if (!headerPrinted) {
            System.out.println();
            System.out.println("ResponseWriterPerfHarness (warmup=" + WARMUP + ", iterations=" + ITERATIONS + ", runs=" + RUNS + ")");
            System.out.println();
            System.out.printf("%-60s %12s%n", "Scenario", "ns/op");
            System.out.printf("%-60s %12s%n", "-".repeat(60), "-".repeat(12));
            headerPrinted = true;
        }
    }

    // -------- writeText scenarios -------------------------------------------

    @Test
    void writeText_shortPlainAscii() {
        measure("writeText -- short plain ASCII (no escapes)", () -> {
            try {
                htmlWriter.writeText(SHORT_PLAIN_TEXT, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void writeText_shortHtmlSpecial() {
        measure("writeText -- short with <, >, &", () -> {
            try {
                htmlWriter.writeText(SHORT_HTML_TEXT, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void writeText_longPlainAscii() {
        measure("writeText -- long plain ASCII", () -> {
            try {
                htmlWriter.writeText(LONG_PLAIN_TEXT, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void writeText_longHtmlSpecial() {
        measure("writeText -- long with mixed escapes", () -> {
            try {
                htmlWriter.writeText(LONG_HTML_TEXT, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // -------- writeAttribute scenarios --------------------------------------

    @Test
    void writeAttribute_plainId() {
        // Attribute writes require an open element; emit one and never close it.
        startElementOnce("input");
        measure("writeAttribute -- id with colons (no escapes)", () -> {
            try {
                htmlWriter.writeAttribute("id", PLAIN_ID, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void writeAttribute_quotesAndSpecials() {
        startElementOnce("div");
        measure("writeAttribute -- style with quotes (escapes)", () -> {
            try {
                htmlWriter.writeAttribute("style", ATTR_WITH_QUOTES, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void writeURIAttribute_typicalLink() {
        startElementOnce("a");
        measure("writeURIAttribute -- href with query and fragment", () -> {
            try {
                htmlWriter.writeURIAttribute("href", URI_VALUE, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // -------- start/end element scenarios -----------------------------------

    @Test
    void startEnd_emptyDiv() {
        measure("startElement + endElement (empty <div>)", () -> {
            try {
                htmlWriter.startElement("div", null);
                htmlWriter.endElement("div");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void fullElementWithAttributesAndText() {
        // Mimics a real component render: <input id="..." name="..." value="..." class="..."/>
        // plus a wrapping <div> with one attribute and text body.
        measure("simulated component render (div + input with attrs + text)", () -> {
            try {
                htmlWriter.startElement("div", null);
                htmlWriter.writeAttribute("class", "panel", null);
                htmlWriter.startElement("input", null);
                htmlWriter.writeAttribute("id", PLAIN_ID, null);
                htmlWriter.writeAttribute("name", PLAIN_ID, null);
                htmlWriter.writeAttribute("type", "text", null);
                htmlWriter.writeAttribute("value", SHORT_PLAIN_TEXT, null);
                htmlWriter.endElement("input");
                htmlWriter.writeText(SHORT_PLAIN_TEXT, null);
                htmlWriter.endElement("div");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // -------- PartialResponseWriter scenarios -------------------------------

    @Test
    void partialResponseWriter_updateCycle() {
        // startUpdate + writeText + endUpdate -- the typical Ajax response fragment.
        PartialResponseWriter partial = new PartialResponseWriter(htmlWriter);
        measure("PartialResponseWriter -- startUpdate + text + endUpdate", () -> {
            try {
                partial.startUpdate("form:fragment");
                partial.write(LONG_PLAIN_TEXT);
                partial.endUpdate();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // -------- Workload helpers ----------------------------------------------

    private void startElementOnce(String name) {
        try {
            htmlWriter.startElement(name, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void measure(String label, Runnable workload) {
        warmUp(workload);
        long median = medianRun(workload);
        System.out.printf("%-60s %12d%n", label, median);
    }

    private static void warmUp(Runnable r) {
        for (int i = 0; i < WARMUP; i++) {
            r.run();
        }
    }

    private static long medianRun(Runnable r) {
        long[] times = new long[RUNS];
        for (int run = 0; run < RUNS; run++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                r.run();
            }
            times[run] = (System.nanoTime() - t0) / ITERATIONS;
        }
        Arrays.sort(times);
        return times[RUNS / 2];
    }

    /** Discards all writes -- isolates encode/escape cost from I/O or buffering. */
    private static final class NullWriter extends Writer {
        @Override public void write(int c) { /* discard */ }
        @Override public void write(char[] cbuf, int off, int len) { /* discard */ }
        @Override public void write(String str) { /* discard */ }
        @Override public void write(String str, int off, int len) { /* discard */ }
        @Override public void flush() { /* no-op */ }
        @Override public void close() { /* no-op */ }
    }
}
