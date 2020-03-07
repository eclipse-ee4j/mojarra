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

package com.sun.faces.facelets.tag;

import java.io.Serializable;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public class IterationStatus implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final int index;
    private final boolean first;
    private final boolean last;
    private final Integer begin;
    private final Integer end;
    private final Integer step;
    private final boolean even;
    private final Object current;
    private final int iterationCount;

    // ------------------------------------------------------------ Constructors

    /**
     * Constructor used for ui:repeat.
     */
    public IterationStatus(boolean first, boolean last, int index, Integer begin, Integer end, Integer step) {
        this(first, last, index, begin, end, step, null, 0);
    }

    /**
     * Constructor used for c:forEach varStatus
     */
    public IterationStatus(boolean first, boolean last, int index, Integer begin, Integer end, Integer step, Object current, int iterationCount) {
        this.index = index;
        this.begin = begin;
        this.end = end;
        this.step = step;
        this.first = first;
        this.last = last;
        this.current = current;
        int iBegin = begin != null ? begin : 0;
        int iStep = step != null ? step : 1;
        even = (index - iBegin) / iStep % 2 == 0;
        this.iterationCount = iterationCount;
    }

    // ---------------------------------------------- Methods from LoopTagStatus

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public Integer getBegin() {
        return begin;
    }

    public Integer getEnd() {
        return end;
    }

    public int getIndex() {
        return index;
    }

    public Integer getStep() {
        return step;
    }

    public Object getCurrent() {
        return current;
    }

    public int getCount() {
        return iterationCount;
    }

    // ---------------------------------------------------------- Public Methods

    public boolean isEven() {
        return even;
    }

    public boolean isOdd() {
        return !even;
    }

    @Override
    public String toString() {
        return "IterationStatus{" + "index=" + index + ", first=" + first + ", last=" + last + ", begin=" + begin + ", end=" + end + ", step=" + step
                + ", even=" + even + ", current=" + current + ", iterationCount=" + iterationCount + '}';
    }
}
