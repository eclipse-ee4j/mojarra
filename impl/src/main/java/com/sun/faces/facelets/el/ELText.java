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

package com.sun.faces.facelets.el;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.sun.faces.el.ELUtils;
import com.sun.faces.util.HtmlUtils;
import com.sun.faces.util.MessageUtils;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.view.Location;

/**
 * Handles parsing EL Strings in accordance with the EL-API Specification. The parser accepts either <code>${..}</code>
 * or <code>#{..}</code>.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public class ELText {

    private static final class LiteralValueExpression extends ValueExpression {

        private static final long serialVersionUID = 1L;

        private final String text;

        public LiteralValueExpression(String text) {
            this.text = text;
        }

        @Override
        public boolean isLiteralText() {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String getExpressionString() {
            return text;
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }

        @Override
        public void setValue(ELContext context, Object value) {
        }

        @Override
        public boolean isReadOnly(ELContext context) {
            return false;
        }

        @Override
        public <T> T getValue(ELContext context) {
            return null;
        }

        @Override
        public Class getType(ELContext context) {
            return null;
        }

        @Override
        public Class getExpectedType() {
            return null;
        }

    }

    private static final class ELTextComposite extends ELText {
        private final ELText[] txt;

        public ELTextComposite(ELText[] txt) {
            super(null);
            this.txt = txt;
        }

        @Override
        public void write(Writer out, ELContext ctx) throws ELException, IOException {
            for (int i = 0; i < txt.length; i++) {
                txt[i].write(out, ctx);
            }
        }

        @Override
        public void writeText(ResponseWriter out, ELContext ctx) throws ELException, IOException {
            for (int i = 0; i < txt.length; i++) {
                txt[i].writeText(out, ctx);
            }
        }

        @Override
        public String toString(ELContext ctx) {
            StringBuilder sb = new StringBuilder();
            for (ELText elText : txt) {
                sb.append(elText.toString(ctx));
            }
            return sb.toString();
        }

        /*
         * public String toString(ELContext ctx) { StringBuffer sb = new StringBuffer(); for (int i = 0; i < this.txt.length;
         * i++) { sb.append(this.txt[i].toString(ctx)); } return sb.toString(); }
         */

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (ELText elText : txt) {
                sb.append(elText.toString());
            }
            return sb.toString();
        }

        @Override
        public boolean isLiteral() {
            return false;
        }

        @Override
        public ELText apply(ExpressionFactory factory, ELContext ctx) {
            int len = txt.length;
            ELText[] nt = new ELText[len];
            for (int i = 0; i < len; i++) {
                nt[i] = txt[i].apply(factory, ctx);
            }
            return new ELTextComposite(nt);
        }
    }

    private static final class ELTextVariable extends ELText {
        private final ValueExpression ve;

        public ELTextVariable(ValueExpression ve) {
            super(ve.getExpressionString());
            this.ve = ve;
        }

        @Override
        public boolean isLiteral() {
            return false;
        }

        @Override
        public ELText apply(ExpressionFactory factory, ELContext ctx) {
            ELText result = null;
            if (ve instanceof ContextualCompositeValueExpression) {
                result = new ELTextVariable(ve);
            } else {
                result = new ELTextVariable(factory.createValueExpression(ctx, ve.getExpressionString(), String.class));
            }

            return result;
        }

        @Override
        public void write(Writer out, ELContext ctx) throws ELException, IOException {
            Object v = ve.getValue(ctx);
            if (v != null) {
                char[] buffer = new char[1028];
                HtmlUtils.writeTextForXML(out, v.toString(), buffer);
            }
        }

        @Override
        public String toString(ELContext ctx) throws ELException {
            Object v = ve.getValue(ctx);
            if (v != null) {
                return v.toString();
            }

            return null;
        }

        @Override
        public void writeText(ResponseWriter out, ELContext ctx) throws ELException, IOException {
            Object v = ve.getValue(ctx);
            if (v != null) {
                out.writeText(v.toString(), null);
            }
        }
    }

    protected final String literal;

    public ELText(String literal) {
        this.literal = literal;
    }

    /**
     * If it's literal text
     *
     * @return true if the String is literal (doesn't contain <code>#{..}</code> or <code>${..}</code>)
     */
    public boolean isLiteral() {
        return true;
    }

    /**
     * Return an instance of <code>this</code> that is applicable given the ELContext and ExpressionFactory state.
     *
     * @param factory the ExpressionFactory to use
     * @param ctx the ELContext to use
     * @return an ELText instance
     */
    public ELText apply(ExpressionFactory factory, ELContext ctx) {
        return this;
    }

    /**
     * Allow this instance to write to the passed Writer, given the ELContext state
     *
     * @param out Writer to write to
     * @param ctx current ELContext state
     * @throws ELException when an EL exception occurs
     * @throws IOException when an I/O exception occurs
     */
    public void write(Writer out, ELContext ctx) throws ELException, IOException {
        out.write(literal);
    }

    public void writeText(ResponseWriter out, ELContext ctx) throws ELException, IOException {
        out.writeText(literal, null);
    }

    /**
     * Evaluates the ELText to a String
     *
     * @param ctx current ELContext state
     * @throws ELException when an EL exception occurs
     * @return the evaluated String
     */
    public String toString(ELContext ctx) throws ELException {
        return literal;
    }

    @Override
    public String toString() {
        return literal;
    }

    /**
     * Parses the passed string to determine if it's literal or not
     *
     * @param in input String
     * @return true if the String is literal (doesn't contain <code>#{..}</code> or <code>${..}</code>)
     */
    public static boolean isLiteral(String in) {
        ELText txt = parse(in);
        return txt == null || txt.isLiteral();
    }

    /**
     * Factory method for creating an unvalidated ELText instance. NOTE: All expressions in the passed String are treated as
     * {@link com.sun.faces.facelets.el.ELText.LiteralValueExpression}, with one exception: composite component expressions.
     * These are treated as ContextualCompositeValueExpressions.
     *
     * @param in String to parse
     * @return ELText instance that knows if the String was literal or not
     * @throws ELException when an EL exception occurs
     */
    public static ELText parse(String in) throws ELException {
        return parse(null, null, in);
    }

    public static ELText parse(String in, String alias) throws ELException {
        return parse(null, null, in, alias);
    }

    public static ELText parse(ExpressionFactory fact, ELContext ctx, String in) throws ELException {
        return parse(null, null, in, null);
    }

    /**
     * Factory method for creating a validated ELText instance. When an Expression is hit, it will use the ExpressionFactory
     * to create a ValueExpression instance, resolving any functions at that time.
     * 
     * Variables and properties will not be evaluated.
     *
     * @param fact ExpressionFactory to use
     * @param ctx ELContext to validate against
     * @param in String to parse
     * @param alias the alias
     * @return ELText that can be re-applied later
     * @throws ELException when an EL exception occurs
     */
    public static ELText parse(ExpressionFactory fact, ELContext ctx, String in, String alias) throws ELException {
        char[] ca = in.toCharArray();
        int i = 0;
        char c = 0;
        int len = ca.length;
        int end = len - 1;
        boolean esc = false;
        int vlen = 0;

        StringBuilder buff = new StringBuilder(128);
        List<ELText> text = new ArrayList<>();
        ELText t = null;
        ValueExpression ve = null;

        while (i < len) {
            c = ca[i];
            if ('\\' == c) {
                esc = !esc;
                if (esc && i < end && (ca[i + 1] == '$' || ca[i + 1] == '#')) {
                    i++;
                    continue;
                }
            } else if (!esc && ('$' == c || '#' == c)) {
                if (i < end) {
                    if ('{' == ca[i + 1]) {
                        if (buff.length() > 0) {
                            text.add(new ELText(buff.toString()));
                            buff.setLength(0);
                        }
                        vlen = findVarLength(ca, i);
                        if (ctx != null && fact != null) {
                            ve = fact.createValueExpression(ctx, new String(ca, i, vlen), String.class);
                            t = new ELTextVariable(ve);
                        } else {
                            String expr = new String(ca, i, vlen);
                            if (null != alias && ELUtils.isCompositeComponentExpr(expr)) {
                                if (ELUtils.isCompositeComponentLookupWithArgs(expr)) {
                                    String message = MessageUtils.getExceptionMessageString(MessageUtils.ARGUMENTS_NOT_LEGAL_CC_ATTRS_EXPR);
                                    throw new ELException(message);
                                }
                                FacesContext context = FacesContext.getCurrentInstance();
                                ELContext elContext = context.getELContext();
                                ValueExpression delegate = context.getApplication().getExpressionFactory().createValueExpression(elContext, expr, Object.class);
                                Location location = new Location(alias, -1, -1);
                                ve = new ContextualCompositeValueExpression(location, delegate);

                            } else {
                                ve = new LiteralValueExpression(expr);
                            }
                            t = new ELTextVariable(ve);
                        }
                        text.add(t);
                        i += vlen;
                        continue;
                    }
                }
            }
            esc = false;
            buff.append(c);
            i++;
        }

        if (buff.length() > 0) {
            text.add(new ELText(buff.toString()));
            buff.setLength(0);
        }

        if (text.isEmpty()) {
            return new ELText("");
        } else if (text.size() == 1) {
            return text.get(0);
        } else {
            ELText[] ta = text.toArray(new ELText[text.size()]);
            return new ELTextComposite(ta);
        }
    }

    private static int findVarLength(char[] ca, int s) throws ELException {
        int i = s;
        int len = ca.length;
        char c = 0;
        int str = 0;
        int nested = 0;
        boolean insideString = false;
        while (i < len) {
            c = ca[i];
            if ('\\' == c && i < len - 1) {
                i++;
            } else if ('\'' == c || '"' == c) {
                if (str == c) {
                    insideString = false;
                    str = 0;
                } else {
                    insideString = true;
                    str = c;
                }
            } else if ('{' == c && !insideString) {
                nested++;
            } else if (str == 0 && '}' == c) {
                if (nested > 1) {
                    nested--;
                } else {
                    return i - s + 1;
                }
            } else if ('}' == c && !insideString) {
                nested--;
            }
            i++;
        }
        throw new ELException("EL Expression Unbalanced: ... " + new String(ca, s, i - s));
    }

}
