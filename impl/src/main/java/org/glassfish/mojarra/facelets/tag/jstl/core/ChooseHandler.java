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

package org.glassfish.mojarra.facelets.tag.jstl.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

import org.glassfish.mojarra.facelets.tag.TagHandlerImpl;

/**
 * @author Jacob Hookom
 */
public final class ChooseHandler extends TagHandlerImpl {

    /**
     * The decision saved for a build that took no when branch at all.
     */
    private static final int OTHERWISE = -1;

    private final ChooseOtherwiseHandler otherwise;
    private final ChooseWhenHandler[] when;

    /**
     * Whether one build of a view can take another branch here than another build, which a choose whose tests are all
     * literal cannot: it takes the same branch every time and there is nothing to replay.
     */
    private final boolean dynamic;

    public ChooseHandler(TagConfig config) {
        super(config);

        List<ChooseWhenHandler> whenList = new ArrayList<>();
        Iterator<ChooseWhenHandler> itr = this.findNextByType(ChooseWhenHandler.class);
        while (itr.hasNext()) {
            whenList.add(itr.next());
        }
        if (whenList.isEmpty()) {
            throw new TagException(tag, "Choose Tag must have one or more When Tags");
        }
        when = whenList.toArray(new ChooseWhenHandler[whenList.size()]);

        Iterator<ChooseOtherwiseHandler> itr2 = this.findNextByType(ChooseOtherwiseHandler.class);
        if (itr2.hasNext()) {
            otherwise = itr2.next();
        } else {
            otherwise = null;
        }

        boolean dynamic = false;
        for (ChooseWhenHandler branch : when) {
            dynamic |= branch.isDynamicTest();
        }
        this.dynamic = dynamic;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        String key = dynamic ? buildTimeDecisionKey(ctx) : null;
        Integer rendered = replayBuildTimeDecision(ctx, key, Integer.class);

        for (int i = 0; i < when.length; i++) {
            ValueExpression testExpression = when[i].getTestExpression(ctx);
            boolean b = rendered != null ? rendered == i : Boolean.TRUE.equals(testExpression.getValue(ctx));
            recordBuildTimeDecision(ctx, testExpression, b);
            if (b) {
                saveBuildTimeDecision(ctx, key, i);
                when[i].apply(ctx, parent);
                return;
            }
        }

        saveBuildTimeDecision(ctx, key, OTHERWISE);

        if (otherwise != null) {
            otherwise.apply(ctx, parent);
        }
    }

}
