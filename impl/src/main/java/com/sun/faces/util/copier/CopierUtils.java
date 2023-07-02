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

package com.sun.faces.util.copier;

import static com.sun.faces.util.ReflectionUtils.instance;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.faces.context.FacesContext;

public class CopierUtils {

    private static final String ERROR_COPIER_NAME = "The copier name should be a Java valid simple/qualified name.";
    private static final String COPIER_PREFIX = "com.sun.faces.util.copier.";

    private final static Set<String> keywords = Set.of(
        "abstract", "continue", "for", "new", "switch", "assert", "default", "if", "package", "synchronized", "boolean", "do", "goto",
        "private", "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum",
        "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class",
        "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while",
        // literals
        "null", "true", "false"
    );

    public static Copier getCopier(FacesContext context, String copierType) {
        Copier copier = null;

        if (!isEmpty(copierType)) {

            // TODO: or should validate only against {"MultiStrategyCopier", "SerializationCopier",
            // "NewInstanceCopier", "CopyCtorCopier", "CloneCopier"} strings / enum

            if (isCopierTypeSimpleName(copierType)) {
                copierType = COPIER_PREFIX.concat(copierType);
            } else if (!isName(copierType)) {
                throw new IllegalArgumentException(ERROR_COPIER_NAME);
            }

            Object expressionResult = evaluateExpressionGet(context, copierType);

            if (expressionResult instanceof Copier) {
                copier = (Copier) expressionResult;
            } else if (expressionResult instanceof String) {
                copier = instance((String) expressionResult);
            }
        }

        if (copier == null) {
            copier = new MultiStrategyCopier();
        }

        return copier;
    }

    @SuppressWarnings("unchecked")
    private static <T> T evaluateExpressionGet(FacesContext context, String expression) {
        if (expression == null) {
            return null;
        }

        return (T) context.getApplication().evaluateExpressionGet(context, expression, Object.class);
    }

    private static boolean isCopierTypeSimpleName(String copierType) {
        return isIdentifier(copierType) && !isKeyword(copierType);
    }

    // maybe the following four methods should be moved in com.sun.faces.util
    private static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    private static boolean isName(CharSequence name) {
        String id = name.toString();

        for (String s : id.split("\\.", -1)) {
            if (!isIdentifier(s) || isKeyword(s)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isIdentifier(CharSequence name) {
        String id = name.toString();

        if (id.length() == 0) {
            return false;
        }
        int cp = id.codePointAt(0);
        if (!Character.isJavaIdentifierStart(cp)) {
            return false;
        }
        for (int i = Character.charCount(cp); i < id.length(); i += Character.charCount(cp)) {
            cp = id.codePointAt(i);
            if (!Character.isJavaIdentifierPart(cp)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isKeyword(CharSequence s) {
        String keywordOrLiteral = s.toString();
        return keywords.contains(keywordOrLiteral);
    }

}
