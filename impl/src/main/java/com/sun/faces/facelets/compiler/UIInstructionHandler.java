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

package com.sun.faces.facelets.compiler;

import java.io.IOException;
import java.io.Writer;

import com.sun.faces.facelets.el.ELText;
import com.sun.faces.facelets.impl.IdMapper;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.facelets.util.FastWriter;

import jakarta.el.ELException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UniqueIdVendor;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;

/**
 * @author Adam Winer
 * @version $Id$
 */
final class UIInstructionHandler extends AbstractUIHandler {

    private final String alias;

    private final String id;

    private final ELText txt;

    private final Instruction[] instructions;

    private final int length;

    private final boolean literal;

    public UIInstructionHandler(String alias, String id, Instruction[] instructions, ELText txt) {
        this.alias = alias;
        this.id = id;
        this.instructions = instructions;
        this.txt = txt;
        length = txt.toString().length();

        boolean literal = true;
        int size = instructions.length;

        for (int i = 0; i < size; i++) {
            Instruction ins = this.instructions[i];
            if (!ins.isLiteral()) {
                literal = false;
                break;
            }
        }

        this.literal = literal;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (parent != null) {
            // our id
            String id = ctx.generateUniqueId(this.id);
            FacesContext context = ctx.getFacesContext();

            // grab our component
            UIComponent c = ComponentSupport.findUIInstructionChildByTagId(context, parent, id);
            boolean componentFound = false;
            boolean suppressEvents = false;
            if (c != null) {
                componentFound = true;
                suppressEvents = ComponentSupport.suppressViewModificationEvents(ctx.getFacesContext());
                // mark all children for cleaning
                ComponentSupport.markForDeletion(c);
            } else {
                Instruction[] applied;
                if (literal) {
                    applied = instructions;
                } else {
                    int size = instructions.length;
                    applied = new Instruction[size];
                    // Create a new list with all of the necessary applied
                    // instructions
                    Instruction ins;
                    for (int i = 0; i < size; i++) {
                        ins = instructions[i];
                        applied[i] = ins.apply(ctx.getExpressionFactory(), ctx);
                    }
                }

                c = new UIInstructions(txt, applied);
                // mark it owned by a facelet instance
                String uid;
                IdMapper mapper = IdMapper.getMapper(ctx.getFacesContext());
                String mid = mapper != null ? mapper.getAliasedId(id) : id;
                UIComponent ancestorNamingContainer = parent.getNamingContainer();
                if (null != ancestorNamingContainer && ancestorNamingContainer instanceof UniqueIdVendor) {
                    uid = ((UniqueIdVendor) ancestorNamingContainer).createUniqueId(ctx.getFacesContext(), mid);
                } else {
                    uid = ComponentSupport.getViewRoot(ctx, parent).createUniqueId(ctx.getFacesContext(), mid);
                }

                c.setId(uid);
                c.getAttributes().put(ComponentSupport.MARK_CREATED, id);
            }
            // finish cleaning up orphaned children
            if (componentFound) {
                ComponentSupport.finalizeForDeletion(c);
                if (suppressEvents) {
                    context.setProcessingEvents(false);
                }
                parent.getChildren().remove(c);
                if (suppressEvents) {
                    context.setProcessingEvents(true);
                }
            }

            // add the component
            if (componentFound && suppressEvents) {
                context.setProcessingEvents(false);
            }
            addComponent(ctx, parent, c);
            if (componentFound && suppressEvents) {
                context.setProcessingEvents(true);
            }
        }
    }

    @Override
    public String toString() {
        return txt.toString();
    }

    @Override
    public String getText() {
        return txt.toString();
    }

    @Override
    public String getText(FaceletContext ctx) {
        Writer writer = new FastWriter(length);
        try {
            txt.apply(ctx.getExpressionFactory(), ctx).write(writer, ctx);
        } catch (IOException e) {
            throw new ELException(alias + ": " + e.getMessage(), e.getCause());
        }
        return writer.toString();
    }

}
