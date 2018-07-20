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

package com.sun.faces.facelets.tag.jsf;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.component.ActionSource;
import javax.faces.component.ActionSource2;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRule;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.MetadataTarget;
import javax.faces.view.facelets.TagAttribute;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
final class ActionSourceRule extends MetaRule {

    public final static Class[] ACTION_SIG = new Class[0];

    public final static Class[] ACTION_LISTENER_SIG = new Class[] { ActionEvent.class };
    public final static Class[] ACTION_LISTENER_ZEROARG_SIG = new Class[] { };


    final static class ActionMapper2 extends Metadata {

        private final TagAttribute attr;

        public ActionMapper2(TagAttribute attr) {
            this.attr = attr;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((ActionSource2) instance).setActionExpression(this.attr
                    .getMethodExpression(ctx, Object.class,
                            ActionSourceRule.ACTION_SIG));
        }

    }

    final static class ActionListenerMapper2 extends Metadata {

        private final TagAttribute attr;

        public ActionListenerMapper2(TagAttribute attr) {
            this.attr = attr;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {

            ExpressionFactory expressionFactory = ctx.getExpressionFactory();

            MethodExpression methodExpressionOneArg = attr.getMethodExpression(
                ctx, null, ActionSourceRule.ACTION_LISTENER_SIG);

            MethodExpression methodExpressionZeroArg =
                    expressionFactory.createMethodExpression(
                        ctx, methodExpressionOneArg.getExpressionString(),
                        Void.class, ActionSourceRule.ACTION_LISTENER_ZEROARG_SIG);

            ((ActionSource2) instance)
                    .addActionListener(new MethodExpressionActionListener(
                            methodExpressionOneArg, methodExpressionZeroArg));

        }

    }

    public final static ActionSourceRule Instance = new ActionSourceRule();

    public ActionSourceRule() {
        super();
    }

    @Override
    public Metadata applyRule(String name, TagAttribute attribute,
            MetadataTarget meta) {
        if (meta.isTargetInstanceOf(ActionSource.class)) {

            if ("action".equals(name)) {
                return new ActionMapper2(attribute);
            }

            if ("actionListener".equals(name)) {
                return new ActionListenerMapper2(attribute);
            }
        }

        return null;
    }
}
