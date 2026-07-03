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
package org.eclipse.mojarra.test.perf.beans;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RowFactory {

    public List<Row> generate(int count) {
        List<Row> rows = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rows.add(new Row(
                    i,
                    "Item " + i,
                    // Non-ASCII + HTML metacharacters exercise the slow char-by-char escaping path in
                    // HtmlUtils.writeText; description is render-only (never a posted input) so it round-trips safely.
                    "Ünïcode ‹" + i + "› — <b>café</b> & \"quoted\" 日本語 filler",
                    i * 3 + 1,
                    new BigDecimal("9.99").add(BigDecimal.valueOf(i)),
                    LocalDate.of(2026, 1, 1).plusDays(i % 365),
                    i % 2 == 0));
        }
        return rows;
    }
}
