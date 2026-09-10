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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

import org.junit.jupiter.api.Test;

class UserTagParameterVariableMapperTest {

    private final ValueExpression ambient = mock(ValueExpression.class);
    private final ValueExpression outerAttribute = mock(ValueExpression.class);
    private final ValueExpression innerAttribute = mock(ValueExpression.class);

    private final VariableMapper page = new MapVariableMapper(singletonMap("ambient", ambient));

    @Test
    void anAttributeOfTheInvocationResolvesToTheExpressionItWasGiven() {
        VariableMapper mapper = UserTagParameterVariableMapper.forInvocation(page, singletonMap("attribute", innerAttribute), null);

        assertSame(innerAttribute, mapper.resolveVariable("attribute"));
    }

    @Test
    void aNameInScopeAtTheInvocationResolvesThroughIt() {
        VariableMapper mapper = UserTagParameterVariableMapper.forInvocation(page, singletonMap("attribute", innerAttribute), null);

        assertSame(ambient, mapper.resolveVariable("ambient"));
    }

    @Test
    void anAttributeOfAnEnclosingInvocationResolvesToNothing() {
        VariableMapper outer = UserTagParameterVariableMapper.forInvocation(page, singletonMap("attribute", outerAttribute), null);
        VariableMapper inner = UserTagParameterVariableMapper.forInvocation(new VariableMapperWrapper(outer), emptyMap(), null);

        assertNull(inner.resolveVariable("attribute"));
        assertSame(ambient, inner.resolveVariable("ambient"));
    }

    @Test
    void theEnclosingScopeIsReturnedUnchangedWhenNoParameterIsInPlay() {
        assertSame(page, UserTagParameterVariableMapper.forInvocation(page, emptyMap(), null));
    }

    @Test
    void theParametersOfAnInvocationCannotBeChanged() {
        VariableMapper mapper = UserTagParameterVariableMapper.forInvocation(page, singletonMap("attribute", innerAttribute), null);

        assertThrows(UnsupportedOperationException.class, () -> mapper.setVariable("attribute", ambient));
    }

    private static class MapVariableMapper extends VariableMapper {

        private final Map<String, ValueExpression> variables;

        MapVariableMapper(Map<String, ValueExpression> variables) {
            this.variables = new HashMap<>(variables);
        }

        @Override
        public ValueExpression resolveVariable(String variable) {
            return variables.get(variable);
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression expression) {
            return variables.put(variable, expression);
        }

    }

}
