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

package org.glassfish.mojarra.facelets.tag.faces;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.MethodNotFoundException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.MethodExpressionActionListener;

import org.glassfish.mojarra.util.FacesLogger;

/**
 * Invokes the <code>listener</code> method expression of <code>f:event</code> and <code>f:ajax</code>, which may point to a method taking the event or to a
 * method taking no arguments.
 * <p>
 * Each invocation tries the variant taking the event first, in the same order as {@link MethodExpressionActionListener#processAction(ActionEvent)}. When that
 * throws {@link MethodNotFoundException} or {@link IllegalArgumentException}, the variant taking no arguments is invoked.
 * <p>
 * In {@link ProjectStage#Development}, it checks once per listener, on the first invocation of the variant taking the event, whether the bean also has the
 * method taking no arguments, and if so it logs a warning, because that method is then never invoked. An expression which provides its own parameters, such as
 * <code>#{bean.listen()}</code>, is not checked, because both variants then invoke the same method.
 */
public final class ListenerMethodInvoker {

    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();
    private static final String AMBIGUOUS = "faces.facelets.listener.method.ambiguous";
    private static final Object[] NO_ARGS = {};
    private static final Set<String> CHECKED_LISTENERS = ConcurrentHashMap.newKeySet();

    private ListenerMethodInvoker() {
    }

    /**
     * Invokes the variant taking the event, and when that throws {@link MethodNotFoundException} or {@link IllegalArgumentException}, the variant taking no
     * arguments. Any other exception propagates.
     *
     * @param context the Faces context.
     * @param listener the listener as it appears in a warning, such as the location and the value of its tag attribute, or <code>null</code> to use the
     * expression string.
     * @param oneArgListener the variant taking the event.
     * @param noArgListener the variant taking no arguments.
     * @param event the event.
     */
    public static void invoke(FacesContext context, String listener, MethodExpression oneArgListener, MethodExpression noArgListener, Object event) {
        ELContext elContext = context.getELContext();

        try {
            oneArgListener.invoke(elContext, new Object[] { event });
        }
        catch (MethodNotFoundException | IllegalArgumentException e) {
            noArgListener.invoke(elContext, NO_ARGS);
            return;
        }

        if (context.isProjectStage(ProjectStage.Development) && !oneArgListener.isParametersProvided()) {
            String description = listener != null ? listener : oneArgListener.getExpressionString();

            if (CHECKED_LISTENERS.add(description)) {
                warnIfBothMethodsExist(elContext, description, oneArgListener, noArgListener);
            }
        }
    }

    private static void warnIfBothMethodsExist(ELContext context, String description, MethodExpression oneArgListener, MethodExpression noArgListener) {
        MethodInfo oneArgMethod;
        MethodInfo noArgMethod;

        try {
            oneArgMethod = oneArgListener.getMethodInfo(context);
            noArgMethod = noArgListener.getMethodInfo(context);
        }
        catch (RuntimeException e) {
            return;
        }

        if (hasParameterCount(oneArgMethod, 1) && hasParameterCount(noArgMethod, 0)) {
            LOGGER.log(Level.WARNING, AMBIGUOUS, new Object[] { description, noArgMethod.getName() });
        }
    }

    private static boolean hasParameterCount(MethodInfo method, int count) {
        Class<?>[] parameterTypes = method == null ? null : method.getParamTypes();
        return parameterTypes != null && parameterTypes.length == count;
    }

}
