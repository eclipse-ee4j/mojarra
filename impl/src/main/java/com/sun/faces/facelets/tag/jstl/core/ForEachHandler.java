/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.tag.jstl.core;

import com.sun.faces.facelets.tag.TagHandlerImpl;

import com.sun.faces.facelets.tag.jsf.IterationIdManager;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;

import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Jacob Hookom
 * @author Andrew Robinson
 */
public final class ForEachHandler extends TagHandlerImpl {

    private static class ArrayIterator implements Iterator {

        protected final Object array;

        protected int i;

        protected final int len;

        public ArrayIterator(Object src) {
            this.i = 0;
            this.array = src;
            this.len = Array.getLength(src);
        }

        @Override
        public boolean hasNext() {
            return this.i < this.len;
        }

        @Override
        public Object next() {
            try {
                return Array.get(this.array, this.i++);
            } catch (ArrayIndexOutOfBoundsException ioob) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final TagAttribute begin;

    private final TagAttribute end;

    private final TagAttribute items;

    private final TagAttribute step;

    private final TagAttribute tranzient;

    private final TagAttribute var;

    private final TagAttribute varStatus;

    /**
     * @param config
     */
    public ForEachHandler(TagConfig config) {
        super(config);
        this.items = this.getAttribute("items");
        this.var = this.getAttribute("var");
        this.begin = this.getAttribute("begin");
        this.end = this.getAttribute("end");
        this.step = this.getAttribute("step");
        this.varStatus = this.getAttribute("varStatus");
        this.tranzient = this.getAttribute("transient");

        if (this.items == null && this.begin != null && this.end == null) {
            throw new TagAttributeException(this.tag, this.begin,
                    "If the 'items' attribute is not specified, but the 'begin' attribute is, then the 'end' attribute is required");
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        int s = this.getBegin(ctx);
        int e = this.getEnd(ctx);
        int m = this.getStep(ctx);
        Integer sO = this.begin != null ? s : null;
        Integer eO = this.end != null ? e : null;
        Integer mO = this.step != null ? m : null;

        boolean t = this.getTransient(ctx);
        Object src = null;
        ValueExpression srcVE = null;
        if (this.items != null) {
            srcVE = this.items.getValueExpression(ctx, Object.class);
            src = srcVE.getValue(ctx);
        } else {
            byte[] b = new byte[e + 1];
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) i;
            }
            src = b;
        }
        if (src != null) {
            Iterator itr = this.toIterator(src);
            if (itr != null) {
                int i = 0;

                // move to start
                while (i < s && itr.hasNext()) {
                    itr.next();
                    i++;
                }

                String v = this.getVarName(ctx);
                String vs = this.getVarStatusName(ctx);
                VariableMapper vars = ctx.getVariableMapper();
                ValueExpression ve = null;
                ValueExpression vO = this.capture(v, vars);
                ValueExpression vsO = this.capture(vs, vars);
                int mi = 0;
                Object value = null;
                int count = 0;

                IterationIdManager.startIteration(ctx);

                try {
                    boolean first = true;
                    while (i <= e && itr.hasNext()) {
                        count++;
                        value = itr.next();

                        // set the var
                        if (v != null) {
                            if (t || srcVE == null) {
                                ctx.setAttribute(v, value);
                            } else {
                                ve = this.getVarExpr(srcVE, src, value, i, s);
                                vars.setVariable(v, ve);
                            }
                        }

                        // set the varStatus
                        if (vs != null) {
                            JstlIterationStatus itrS = new JstlIterationStatus(first, !itr.hasNext(), i, sO, eO, mO, value, count);
                            if (t || srcVE == null) {
                                ctx.setAttribute(vs, itrS);
                            } else {
                                ve = new IterationStatusExpression(itrS);
                                vars.setVariable(vs, ve);
                            }
                        }

                        // execute body
                        this.nextHandler.apply(ctx, parent);

                        // increment steps
                        mi = 1;
                        while (mi < m && itr.hasNext()) {
                            itr.next();
                            mi++;
                            i++;
                        }
                        i++;

                        first = false;
                    }
                } finally {
                    if (v != null) {
                        vars.setVariable(v, vO);
                    }
                    if (vs != null) {
                        vars.setVariable(vs, vsO);
                    }
                    IterationIdManager.stopIteration(ctx);
                }
            }
        }
    }

    private ValueExpression capture(String name, VariableMapper vars) {
        if (name != null) {
            return vars.setVariable(name, null);
        }
        return null;
    }

    private int getBegin(FaceletContext ctx) {
        if (this.begin != null) {
            return this.begin.getInt(ctx);
        }
        return 0;
    }

    private int getEnd(FaceletContext ctx) {
        if (this.end != null) {
            return this.end.getInt(ctx);
        }
        return Integer.MAX_VALUE - 1; // hotspot bug in the JVM
    }

    private int getStep(FaceletContext ctx) {
        if (this.step != null) {
            return this.step.getInt(ctx);
        }
        return 1;
    }

    private boolean getTransient(FaceletContext ctx) {
        if (this.tranzient != null) {
            return this.tranzient.getBoolean(ctx);
        }
        return false;
    }

    private ValueExpression getVarExpr(ValueExpression ve, Object src, Object value, int i, int start) {
        if (src instanceof List || src.getClass().isArray()) {
            return new IndexedValueExpression(ve, i);
        } else if (src instanceof Map && value instanceof Map.Entry) {
            return new MappedValueExpression(ve, (Map.Entry) value);
        } else if (src instanceof Collection) {
            return new IteratedValueExpression(ve, start, i);
        }
        throw new IllegalStateException("Cannot create VE for: " + src);
    }

    private String getVarName(FaceletContext ctx) {
        if (this.var != null) {
            return this.var.getValue(ctx);
        }
        return null;
    }

    private String getVarStatusName(FaceletContext ctx) {
        if (this.varStatus != null) {
            return this.varStatus.getValue(ctx);
        }
        return null;
    }

    private Iterator toIterator(Object src) {
        if (src == null) {
            return null;
        } else if (src instanceof Collection) {
            return ((Collection) src).iterator();
        } else if (src instanceof Map) {
            return ((Map) src).entrySet().iterator();
        } else if (src.getClass().isArray()) {
            return new ArrayIterator(src);
        } else {
            throw new TagAttributeException(this.tag, this.items, "Must evaluate to a Collection, Map, Array, or null.");
        }
    }

}
