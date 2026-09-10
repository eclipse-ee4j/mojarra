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

package org.glassfish.mojarra.facelets.el;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

import org.glassfish.mojarra.util.FacesLogger;

/**
 * The parameter namespace of a single tag file invocation.
 * <p>
 * A name the invocation received as an attribute resolves to the expression it was given. A name an enclosing invocation received, and this one did not,
 * resolves to nothing rather than to the enclosing value. Every other name resolves against the scope the invocation was made from, so a tag file still sees
 * the variables in scope where it is used.
 */
public final class UserTagParameterVariableMapper extends VariableMapper {

    private static final Logger LOGGER = FacesLogger.FACELETS_EL.getLogger();

    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private final VariableMapper target;

    private final Map<String, ValueExpression> parameters;

    private final String invocation;

    private UserTagParameterVariableMapper(VariableMapper target, Map<String, ValueExpression> parameters, String invocation) {
        this.target = target;
        this.parameters = parameters;
        this.invocation = invocation;
    }

    /**
     * Returns the variable mapper a tag file invocation resolves its names against: the given attributes, plus every parameter name already in scope bound to
     * nothing, over the scope the invocation was made from. The enclosing scope is returned unchanged when the invocation supplies no attributes and inherits
     * no parameter names, which is every tag file used outside another one.
     *
     * @param enclosing the variable mapper in effect where the tag file is invoked
     * @param attributes the attributes supplied at the invocation
     * @param invocation describes the invocation for the benefit of a diagnostic, or {@code null} to raise none
     * @return the variable mapper the tag file resolves its names against
     */
    public static VariableMapper forInvocation(VariableMapper enclosing, Map<String, ValueExpression> attributes, String invocation) {
        Set<String> inherited = parameterNamesInScope(enclosing);

        if (attributes.isEmpty() && inherited.isEmpty()) {
            return enclosing;
        }

        Map<String, ValueExpression> parameters = new HashMap<>(inherited.size() + attributes.size());

        for (String name : inherited) {
            parameters.put(name, null);
        }

        parameters.putAll(attributes);

        return new UserTagParameterVariableMapper(enclosing, parameters, invocation);
    }

    private static Set<String> parameterNamesInScope(VariableMapper mapper) {
        while (mapper instanceof VariableMapperWrapper) {
            mapper = ((VariableMapperWrapper) mapper).getTarget();
        }

        return mapper instanceof UserTagParameterVariableMapper ? ((UserTagParameterVariableMapper) mapper).parameters.keySet() : Set.of();
    }

    @Override
    public ValueExpression resolveVariable(String variable) {
        if (parameters.containsKey(variable)) {
            ValueExpression parameter = parameters.get(variable);

            if (parameter == null && invocation != null) {
                reportInherited(variable);
            }

            return parameter;
        }

        return target.resolveVariable(variable);
    }

    /**
     * Reports a name which an enclosing invocation supplied and this one did not, but only where the enclosing scope holds it, which is where a page written
     * against an implementation that let it through resolves it differently here. Reported once per invocation and name.
     */
    private void reportInherited(String variable) {
        if (target.resolveVariable(variable) != null && REPORTED.add(invocation + ' ' + variable)) {
            LOGGER.warning(
                () -> "The tag file behind " + invocation + " resolves #{" + variable
                    + "} to nothing: the name is an attribute of an enclosing tag file and is not inherited."
                    + " Supply it as an attribute of this tag where the tag file needs it."
            );
        }
    }

    /**
     * @throws UnsupportedOperationException always, because the parameters of a tag file invocation are fixed at the invocation, and whatever the tag file
     * itself sets belongs to the mapper wrapping this one
     */
    @Override
    public ValueExpression setVariable(String variable, ValueExpression expression) {
        throw new UnsupportedOperationException("The parameters of a tag file invocation cannot be changed");
    }

}
