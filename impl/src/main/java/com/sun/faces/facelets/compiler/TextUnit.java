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

package com.sun.faces.facelets.compiler;

import com.sun.faces.facelets.el.ELText;

import javax.el.ELException;
import javax.faces.view.facelets.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
final class TextUnit extends CompilationUnit {

    private final StringBuffer buffer;

    private final StringBuffer textBuffer;

    private final List instructionBuffer;

    private final Stack tags;

    private final List children;

    private boolean startTagOpen;

    private final String alias;

    private final String id;
    
    public TextUnit(String alias, String id) {
        this.alias = alias;
        this.id = id;
        this.buffer = new StringBuffer();
        this.textBuffer = new StringBuffer();
        this.instructionBuffer = new ArrayList();
        this.tags = new Stack();
        this.children = new ArrayList();
        this.startTagOpen = false;
    }

    @Override
    public FaceletHandler createFaceletHandler() {
        this.flushBufferToConfig(true);

        if (this.children.size() == 0) {
            return LEAF;
        }

        FaceletHandler[] h = new FaceletHandler[this.children.size()];
        Object obj;
        for (int i = 0; i < h.length; i++) {
            obj = this.children.get(i);
            if (obj instanceof FaceletHandler) {
                h[i] = (FaceletHandler) obj;
            } else {
                h[i] = ((CompilationUnit) obj).createFaceletHandler();
            }
        }
        if (h.length == 1) {
            return h[0];
        }
        return new CompositeFaceletHandler(h);
    }

    private void addInstruction(Instruction instruction) {
        this.flushTextBuffer(false);
        this.instructionBuffer.add(instruction);
    }

    private void flushTextBuffer(boolean child) {
        if (this.textBuffer.length() > 0) {
            String s = this.textBuffer.toString();

            if (child) {
                s = trimRight(s);
            }
            if (s.length() > 0) {
                ELText txt = ELText.parse(s, alias);
                if (txt != null) {
                    if (txt.isLiteral()) {
                        this.instructionBuffer.add(new LiteralTextInstruction(
                                txt.toString()));
                    } else {
                        this.instructionBuffer.add(new TextInstruction(
                                this.alias, txt));
                    }
                }
            }

        }
        this.textBuffer.setLength(0);
    }

    public void write(String text) {
        this.finishStartTag();
        this.textBuffer.append(text);
        this.buffer.append(text);
    }

    public void writeInstruction(String text) {
        this.finishStartTag();
        ELText el = ELText.parse(text);
        if (el.isLiteral()) {
            this.addInstruction(new LiteralXMLInstruction(text));
        } else {
            this.addInstruction(new XMLInstruction(el));
        }
        this.buffer.append(text);
    }

    public void writeComment(String text) {
        this.finishStartTag();

        ELText el = ELText.parse(text);
        if (el.isLiteral()) {
            this.addInstruction(new LiteralCommentInstruction(text));
        } else {
            this.addInstruction(new CommentInstruction(el));
        }

        this.buffer.append("<!--" + text + "-->");
    }

    public void startTag(Tag tag) {

        // finish any previously written tags
        this.finishStartTag();

        // push this tag onto the stack
        this.tags.push(tag);

        // write it out
        this.buffer.append('<');
        this.buffer.append(tag.getQName());

        this.addInstruction(new StartElementInstruction(tag.getQName()));

        TagAttribute[] attrs = tag.getAttributes().getAll();
        if (attrs.length > 0) {
            for (int i = 0; i < attrs.length; i++) {
                String qname = attrs[i].getQName();
                String value = attrs[i].getValue();
                this.buffer.append(' ').append(qname).append("=\"").append(
                        value).append("\"");

                ELText txt = ELText.parse(value);
                if (txt != null) {
                    if (txt.isLiteral()) {
                        this.addInstruction(new LiteralAttributeInstruction(
                                qname, txt.toString()));
                    } else {
                        this.addInstruction(new AttributeInstruction(
                                this.alias, qname, txt));
                    }
                }
            }
        }

        // notify that we have an open tag
        this.startTagOpen = true;
    }

    private void finishStartTag() {
        if (this.tags.size() > 0 && this.startTagOpen) {
            this.buffer.append(">");
            this.startTagOpen = false;
        }
    }

    public void endTag() {
        Tag tag = (Tag) this.tags.pop();

        this.addInstruction(new EndElementInstruction(tag.getQName()));

        if (this.startTagOpen) {
            this.buffer.append("/>");
            this.startTagOpen = false;
        } else {
            this.buffer.append("</").append(tag.getQName()).append('>');
        }
    }

    @Override
    public void addChild(CompilationUnit unit) {
        // if we are adding some other kind of unit
        // then we need to capture our buffer into a UITextHandler
        this.finishStartTag();
        this.flushBufferToConfig(true);
        this.children.add(unit);
    }

    protected void flushBufferToConfig(boolean child) {

//        // NEW IMPLEMENTATION
//        if (true) {

            this.flushTextBuffer(child);

            int size = this.instructionBuffer.size();
            if (size > 0) {
                try {
                    String s = this.buffer.toString();
                    if (child)
                        s = trimRight(s);
                    ELText txt = ELText.parse(s);
                    if (txt != null) {
                        Instruction[] instructions = (Instruction[]) this.instructionBuffer
                                .toArray(new Instruction[size]);
                        this.children.add(new UIInstructionHandler(this.alias,
                                                                   this.id,
                                                                   instructions,
                                                                   txt));
                        this.instructionBuffer.clear();
                    }

                } catch (ELException e) {
                    if (this.tags.size() > 0) {
                        throw new TagException((Tag) this.tags.peek(), e
                                .getMessage());
                    } else {
                        throw new ELException(this.alias + ": "
                                + e.getMessage(), e.getCause());
                    }
                }
            }

//            // KEEP THESE SEPARATE SO LOGIC DOESN'T GET FUBARED
//        } else if (this.buffer.length() > 0) {
//            String s = this.buffer.toString();
//            if (s.trim().length() > 0) {
//                if (child) {
//                    s = trimRight(s);
//                }
//                if (s.length() > 0) {
//                    try {
//                        ELText txt = ELText.parse(s);
//                        if (txt != null) {
//                            if (txt.isLiteral()) {
//                                this.children.add(new UILiteralTextHandler(txt
//                                        .toString()));
//                            } else {
//                                this.children.add(new UITextHandler(this.alias,
//                                        txt));
//                            }
//                        }
//                    } catch (ELException e) {
//                        if (this.tags.size() > 0) {
//                            throw new TagException((Tag) this.tags.peek(), e
//                                    .getMessage());
//                        } else {
//                            throw new ELException(this.alias + ": "
//                                    + e.getMessage(), e.getCause());
//                        }
//                    }
//                }
//            }
//        }

        // ALWAYS CLEAR FOR BOTH IMPL
        this.buffer.setLength(0);
    }

    public boolean isClosed() {
        return this.tags.empty();
    }

    private static String trimRight(String s) {
        int i = s.length() - 1;
        while (i >= 0) {
            if (Character.isWhitespace(s.charAt(i))) {
                i--;
            } else {
                return s;
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "TextUnit[" + this.children.size() + "]";
    }
}
