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

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class TableBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private RowFactory rowFactory;

    private List<Row> readonlyRows;
    private List<Row> inputRows;
    private List<Row> heavyReadonlyRows;
    private List<Row> heavyInputRows;

    @PostConstruct
    public void init() {
        readonlyRows = rowFactory.generate(200);
        inputRows = rowFactory.generate(50);
        heavyReadonlyRows = rowFactory.generate(2000);
        heavyInputRows = rowFactory.generate(200);
    }

    public List<Row> getReadonlyRows() {
        return readonlyRows;
    }

    public List<Row> getInputRows() {
        return inputRows;
    }

    public List<Row> getHeavyReadonlyRows() {
        return heavyReadonlyRows;
    }

    public List<Row> getHeavyInputRows() {
        return heavyInputRows;
    }

    public String save() {
        return null;
    }
}
