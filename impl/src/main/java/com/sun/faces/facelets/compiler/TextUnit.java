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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.sun.faces.facelets.el.ELText;

import jakarta.el.ELException;
import jakarta.faces.view.facelets.CompositeFaceletHandler;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
final class TextUnit extends CompilationUnit {

    private final StringBuffer buffer;

    private final StringBuffer textBuffer;

    private final List<Instruction> instructionBuffer;

    private final Stack<Tag> tags;

    private final List children;

    private boolean startTagOpen;

    private final String alias;

    private final String id;

    public TextUnit(String alias, String id) {
        this.alias = alias;
        this.id = id;
        buffer = new StringBuffer();
        textBuffer = new StringBuffer();
        instructionBuffer = new ArrayList<>();
        tags = new Stack<>();
        children = new ArrayList<>();
        startTagOpen = false;
    }

    @Override
    public FaceletHandler createFaceletHandler() {
        flushBufferToConfig(true);

        if (children.size() == 0) {
            return LEAF;
        }

        FaceletHandler[] h = new FaceletHandler[children.size()];
        Object obj;
        for (int i = 0; i < h.length; i++) {
            obj = children.get(i);
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
        flushTextBuffer(false);
        instructionBuffer.add(instruction);
    }

    private void flushTextBuffer(boolean child) {
        if (textBuffer.length() > 0) {
            String s = textBuffer.toString();

            if (child) {
                s = trimRight(s);
            }
            if (s.length() > 0) {
                ELText txt = ELText.parse(s, alias);
                if (txt != null) {
                    if (txt.isLiteral()) {
                        instructionBuffer.add(new LiteralTextInstruction(txt.toString()));
                    } else {
                        instructionBuffer.add(new TextInstruction(alias, txt));
                    }
                }
            }

        }
        textBuffer.setLength(0);
    }

    public void write(String text) {
        finishStartTag();
        textBuffer.append(text);
        buffer.append(text);
    }

    public void writeInstruction(String text) {
        finishStartTag();
        ELText el = ELText.parse(text);
        if (el.isLiteral()) {
            addInstruction(new LiteralXMLInstruction(text));
        } else {
            addInstruction(new XMLInstruction(el));
        }
        buffer.append(text);
    }

    public void writeComment(String text) {
        finishStartTag();

        ELText el = ELText.parse(text);
        if (el.isLiteral()) {
            addInstruction(new LiteralCommentInstruction(text));
        } else {
            addInstruction(new CommentInstruction(el));
        }

        buffer.append("<!--").append(text).append("-->");
    }

    public void startTag(Tag tag) {

        // finish any previously written tags
        finishStartTag();

        // push this tag onto the stack
        tags.push(tag);

        // write it out
        buffer.append('<');
        buffer.append(tag.getQName());

        addInstruction(new StartElementInstruction(tag.getQName()));

        TagAttribute[] attrs = tag.getAttributes().getAll();
        for (TagAttribute attr : attrs) {
            String qname = attr.getQName();
            String value = attr.getValue();
            buffer.append(' ').append(qname).append("=\"").append(value).append("\"");

            ELText txt = ELText.parse(value);
            if (txt != null) {
                if (txt.isLiteral()) {
                    addInstruction(new LiteralAttributeInstruction(qname, txt.toString()));
                } else {
                    addInstruction(new AttributeInstruction(alias, qname, txt));
                }
            }
        }

        // notify that we have an open tag
        startTagOpen = true;
    }

    private void finishStartTag() {
        if (tags.size() > 0 && startTagOpen) {
            buffer.append('>');
            startTagOpen = false;
        }
    }

    public void endTag() {
        Tag tag = (Tag) tags.pop();

        addInstruction(new EndElementInstruction(tag.getQName()));

        if (startTagOpen) {
            buffer.append("/>");
            startTagOpen = false;
        } else {
            buffer.append("</").append(tag.getQName()).append('>');
        }
    }

    @Override
    public void addChild(CompilationUnit unit) {
        // if we are adding some other kind of unit
        // then we need to capture our buffer into a UITextHandler
        finishStartTag();
        flushBufferToConfig(true);
        children.add(unit);
    }

    protected void flushBufferToConfig(boolean child) {

//        // NEW IMPLEMENTATION
//        if (true) {

        flushTextBuffer(child);

        int size = instructionBuffer.size();
        if (size > 0) {
            try {
                String s = buffer.toString();
                if (child) {
                    s = trimRight(s);
                }
                ELText txt = ELText.parse(s);
                if (txt != null) {
                    Instruction[] instructions = instructionBuffer.toArray(new Instruction[size]);
                    children.add(new UIInstructionHandler(alias, id, instructions, txt));
                    instructionBuffer.clear();
                }

            } catch (ELException e) {
                if (tags.size() > 0) {
                    throw new TagException((Tag) tags.peek(), e.getMessage());
                } else {
                    throw new ELException(alias + ": " + e.getMessage(), e.getCause());
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
        buffer.setLength(0);
    }

    public boolean isClosed() {
        return tags.empty();
    }

    private static String trimRight(String s) {
        int i = s.length() - 1;
        while (i >= 0) {
            if (Character.isWhitespace(s.charAt(i))) {
                i--;
            } else {
                return s.substring(0,i+1);
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "TextUnit[" + children.size() + "]";
    }


}
