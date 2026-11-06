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
package org.eclipse.mojarra.test.restorebuildtimedecisionsafteredit;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Edits a page of the deployed web application, as a developer does while the page is open in the browser: it swaps
 * the two blocks between the three block markers of the facelet given by the <code>page</code> parameter, or with the
 * <code>restore</code> parameter it puts back what the facelet held before its first swap. It responds with
 * {@value #EDITED} once the web application serves the edited facelet. The webapp sets the facelet refresh period to
 * 0, so every request compiles the facelet again.
 */
@WebServlet("/swapblocks")
public class SwapBlocksServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static final String EDITED = "edited";
    private static final Set<String> PAGES = Set.of("swappedconditional.xhtml", "swappediteration.xhtml", "swappedinclude.xhtml",
            "WEB-INF/includes/before.xhtml", "WEB-INF/includes/edited.xhtml");
    private static final String BLOCK_MARKER = "<!-- block -->";
    private static final int BLOCK_PARTS = 4;
    private static final long SERVED_TIMEOUT_MILLIS = 15_000;
    private static final long SERVED_POLL_MILLIS = 100;
    private static final Map<String, byte[]> ORIGINALS = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String page = request.getParameter("page");

        if (!PAGES.contains(page)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String realPath = getServletContext().getRealPath("/" + page);

        if (realPath == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The web application is not deployed as a directory");
            return;
        }

        Path path = Paths.get(realPath);
        byte[] current = Files.readAllBytes(path);
        byte[] edited;

        if (request.getParameter("restore") != null) {
            edited = ORIGINALS.getOrDefault(page, current);
        }
        else {
            String[] parts = new String(current, UTF_8).split(BLOCK_MARKER, -1);

            if (parts.length != BLOCK_PARTS) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Expected " + (BLOCK_PARTS - 1) + " block markers in " + page);
                return;
            }

            ORIGINALS.putIfAbsent(page, current);
            edited = (parts[0] + BLOCK_MARKER + parts[2] + BLOCK_MARKER + parts[1] + BLOCK_MARKER + parts[3]).getBytes(UTF_8);
        }

        Files.write(path, edited);

        if (!awaitServed(page, edited)) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The web application does not serve the edited " + page);
            return;
        }

        response.getWriter().write(EDITED);
    }

    /**
     * Waits until the web application serves the given content for the given page, since a container may serve a
     * resource from a cache which it refreshes only after a while.
     */
    private boolean awaitServed(String page, byte[] content) throws IOException {
        long deadline = System.currentTimeMillis() + SERVED_TIMEOUT_MILLIS;

        while (!Arrays.equals(served(page), content)) {
            if (System.currentTimeMillis() > deadline) {
                return false;
            }

            try {
                Thread.sleep(SERVED_POLL_MILLIS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return true;
    }

    private byte[] served(String page) throws IOException {
        try (InputStream input = getServletContext().getResourceAsStream("/" + page)) {
            return input == null ? null : input.readAllBytes();
        }
    }
}
