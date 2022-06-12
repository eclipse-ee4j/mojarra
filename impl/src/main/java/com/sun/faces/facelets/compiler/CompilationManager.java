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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.facelets.tag.TagAttributesImpl;
import com.sun.faces.facelets.tag.TagLibrary;
import com.sun.faces.facelets.tag.composite.CompositeLibrary;
import com.sun.faces.facelets.tag.composite.ImplementationHandler;
import com.sun.faces.facelets.tag.composite.InterfaceHandler;
import com.sun.faces.facelets.tag.faces.core.CoreLibrary;
import com.sun.faces.facelets.tag.ui.ComponentRefHandler;
import com.sun.faces.facelets.tag.ui.CompositionHandler;
import com.sun.faces.facelets.tag.ui.UILibrary;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagDecorator;
import jakarta.faces.view.facelets.TagException;

/**
 * Compilation unit for managing the creation of a single FaceletHandler based on events from an XML parser.
 *
 * @see {@link com.sun.faces.facelets.compiler.Compiler}
 *
 * @author Jacob Hookom
 */
final class CompilationManager {

    private final static Logger log = FacesLogger.FACELETS_COMPILER.getLogger();

    private final Compiler compiler;

    private final TagLibrary tagLibrary;

    private final TagDecorator tagDecorator;

    private final NamespaceManager namespaceManager;

    private final Stack<CompilationUnit> units;

    private int tagId;

    private boolean finished;

    private final String alias;

    private CompilationMessageHolder messageHolder = null;

    private WebConfiguration config;

    public CompilationManager(String alias, Compiler compiler) {

        // this is our alias
        this.alias = alias;

        // grab compiler state
        this.compiler = compiler;
        tagDecorator = compiler.createTagDecorator();
        tagLibrary = compiler.createTagLibrary(getCompilationMessageHolder());

        // namespace management
        namespaceManager = new NamespaceManager();

        // tag uids
        tagId = 0;

        // for composition use
        finished = false;

        // our compilationunit stack
        units = new Stack<>();
        units.push(new CompilationUnit());

        config = WebConfiguration.getInstance();

    }

    private InterfaceUnit interfaceUnit;

    private InterfaceUnit getInterfaceUnit() {
        return interfaceUnit;
    }

    public CompilationMessageHolder getCompilationMessageHolder() {
        if (null == messageHolder) {
            messageHolder = new CompilationMessageHolderImpl();
        }
        return messageHolder;
    }

    public String getAlias() {
        return alias;
    }

    public WebConfiguration getWebConfiguration() {
        return config;
    }

    public void setCompilationMessageHolder(CompilationMessageHolder messageHolder) {
        this.messageHolder = messageHolder;
    }

    private void setInterfaceUnit(InterfaceUnit interfaceUnit) {
        this.interfaceUnit = interfaceUnit;
    }

    public void writeInstruction(String value) {
        if (finished) {
            return;
        }

        // don't carelessly add empty tags
        if (value.length() == 0) {
            return;
        }

        TextUnit unit;
        if (currentUnit() instanceof TextUnit) {
            unit = (TextUnit) currentUnit();
        } else {
            unit = new TextUnit(alias, nextTagId());
            startUnit(unit);
        }
        unit.writeInstruction(value);
    }

    public void writeText(String value) {

        if (finished) {
            return;
        }

        // don't carelessly add empty tags
        if (value.length() == 0) {
            return;
        }

        TextUnit unit;
        if (currentUnit() instanceof TextUnit) {
            unit = (TextUnit) currentUnit();
        } else {
            unit = new TextUnit(alias, nextTagId());
            startUnit(unit);
        }
        unit.write(value);
    }

    public void writeComment(String text) {
        if (compiler.isTrimmingComments()) {
            return;
        }

        if (finished) {
            return;
        }

        // don't carelessly add empty tags
        if (text.length() == 0) {
            return;
        }

        TextUnit unit;
        if (currentUnit() instanceof TextUnit) {
            unit = (TextUnit) currentUnit();
        } else {
            unit = new TextUnit(alias, nextTagId());
            startUnit(unit);
        }

        unit.writeComment(text);
    }

    public void writeWhitespace(String text) {
        if (!compiler.isTrimmingWhitespace()) {
            writeText(text);
        }
    }

    private String nextTagId() {
        return Integer.toHexString(Math.abs(alias.hashCode() ^ 13 * tagId++));
    }

    public void pushTag(Tag orig) {

        if (finished) {
            return;
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Tag Pushed: " + orig);
        }

        Tag t = tagDecorator.decorate(orig);
        String[] qname = determineQName(t);
        t = trimAttributes(t);

        if (isTrimmed(qname[0], qname[1])) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Composition Found, Popping Parent Tags");
            }

            CompilationUnit viewRootUnit = getViewRootUnitFromStack(units);
            units.clear();
            NamespaceUnit nsUnit = namespaceManager.toNamespaceUnit(tagLibrary);
            units.push(nsUnit);
            if (viewRootUnit != null) {
                viewRootUnit.removeChildren();
                currentUnit().addChild(viewRootUnit);
            }
            startUnit(new TrimmedTagUnit(tagLibrary, qname[0], qname[1], t, nextTagId()));
            if (log.isLoggable(Level.FINE)) {
                log.fine("New Namespace and [Trimmed] TagUnit pushed");
            }
        } else if (isImplementation(qname[0], qname[1])) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Composite Component Implementation Found, Popping Parent Tags");
            }

            // save aside the InterfaceUnit
            InterfaceUnit iface = getInterfaceUnit();
            if (null == iface) {
                throw new TagException(orig, "Unable to find interface for implementation.");
            }

            // Cleare the parent tags
            units.clear();
            NamespaceUnit nsUnit = namespaceManager.toNamespaceUnit(tagLibrary);
            units.push(nsUnit);
            currentUnit().addChild(iface);
            startUnit(new ImplementationUnit(tagLibrary, qname[0], qname[1], t, nextTagId()));
            if (log.isLoggable(Level.FINE)) {
                log.fine("New Namespace and ImplementationUnit pushed");
            }

        } else if (isRemove(qname[0], qname[1])) {
            units.push(new RemoveUnit());
        } else if (tagLibrary.containsTagHandler(qname[0], qname[1])) {
            if (isInterface(qname[0], qname[1])) {
                InterfaceUnit iface = new InterfaceUnit(tagLibrary, qname[0], qname[1], t, nextTagId());
                setInterfaceUnit(iface);
                startUnit(iface);
            } else {
                startUnit(new TagUnit(tagLibrary, qname[0], qname[1], t, nextTagId()));
            }
        } else if (tagLibrary.containsNamespace(qname[0], t)) {
            throw new TagException(orig, "Tag Library supports namespace: " + qname[0] + ", but no tag was defined for name: " + qname[1]);
        } else {
            TextUnit unit;
            if (currentUnit() instanceof TextUnit) {
                unit = (TextUnit) currentUnit();
            } else {
                unit = new TextUnit(alias, nextTagId());
                startUnit(unit);
            }
            unit.startTag(t);
        }
    }

    public void popTag() {

        if (finished) {
            return;
        }

        CompilationUnit unit = currentUnit();

        if (unit instanceof TextUnit) {
            TextUnit t = (TextUnit) unit;
            if (t.isClosed()) {
                finishUnit();
            } else {
                t.endTag();
                if (t.isClosed()) {
                    this.finishUnit();
                }
                return;
            }
            unit = this.currentUnit();
        }

        if (unit instanceof TagUnit) {
            TagUnit t = (TagUnit) unit;
            if (t instanceof TrimmedTagUnit) {
                finished = true;
                return;
            }
        }

        finishUnit();
    }

    public void popNamespace(String ns) {
        namespaceManager.popNamespace(ns);
        if (currentUnit() instanceof NamespaceUnit) {
            finishUnit();
        }
    }

    public void pushNamespace(String prefix, String uri) {

        if (log.isLoggable(Level.FINE)) {
            log.fine("Namespace Pushed " + prefix + ": " + uri);
        }

        boolean alreadyPresent = namespaceManager.getNamespace(prefix) != null;
        namespaceManager.pushNamespace(prefix, uri);
        NamespaceUnit unit;
        if (currentUnit() instanceof NamespaceUnit && !alreadyPresent) {
            unit = (NamespaceUnit) currentUnit();
        } else {
            unit = new NamespaceUnit(tagLibrary);
            startUnit(unit);
        }
        unit.setNamespace(prefix, uri);
    }

    public FaceletHandler createFaceletHandler() {
        return units.get(0).createFaceletHandler();
    }

    private CompilationUnit currentUnit() {
        if (!units.isEmpty()) {
            return units.peek();
        }
        return null;
    }

    private void finishUnit() {
        CompilationUnit unit = units.pop();
        unit.finishNotify(this);

        if (log.isLoggable(Level.FINE)) {
            log.fine("Finished Unit: " + unit);
        }
    }

//    private CompilationUnit searchUnits(Class type) {
//        CompilationUnit unit = null;
//        int i = this.units.size();
//        while (unit == null && --i >= 0) {
//            if (type.isAssignableFrom(this.units.get(i).getClass())) {
//                unit = (CompilationUnit) this.units.get(i);
//            }
//        }
//        return unit;
//    }

    private void startUnit(CompilationUnit unit) {

        if (log.isLoggable(Level.FINE)) {
            log.fine("Starting Unit: " + unit + " and adding it to parent: " + currentUnit());
        }

        currentUnit().addChild(unit);
        units.push(unit);
        unit.startNotify(this);
    }

    private Tag trimAttributes(Tag tag) {
        Tag t = trimJSFCAttribute(tag);
        t = trimNSAttributes(t);
        return t;
    }

    protected static boolean isRemove(String ns, String name) {
        return UILibrary.NAMESPACES.contains(ns) && "remove".equals(name);
    }

    // edburns: This is the magic line that tells the system to trim out the
    // extra content above and below the tag.
    protected static boolean isTrimmed(String ns, String name) {
        boolean matchInUILibrary = UILibrary.NAMESPACES.contains(ns)
                && (CompositionHandler.Name.equals(name) || ComponentRefHandler.Name.equals(name));
        return matchInUILibrary;
    }

    protected static boolean isImplementation(String ns, String name) {
        boolean matchInCompositeLibrary = CompositeLibrary.NAMESPACES.contains(ns)
                && ImplementationHandler.Name.equals(name);
        return matchInCompositeLibrary;
    }

    protected static boolean isInterface(String ns, String name) {
        boolean matchInCompositeLibrary = CompositeLibrary.NAMESPACES.contains(ns)
                && InterfaceHandler.Name.equals(name);
        return matchInCompositeLibrary;
    }

    private String[] determineQName(Tag tag) {
        TagAttribute attr = tag.getAttributes().get("jsfc");
        if (attr != null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(attr + " Facelet Compile Directive Found");
            }
            String value = attr.getValue();
            String namespace, localName;
            int c = value.indexOf(':');
            if (c == -1) {
                namespace = namespaceManager.getNamespace("");
                localName = value;
            } else {
                String prefix = value.substring(0, c);
                namespace = namespaceManager.getNamespace(prefix);
                if (namespace == null) {
                    throw new TagAttributeException(tag, attr, "No Namespace matched for: " + prefix);
                }
                localName = value.substring(c + 1);
            }
            return new String[] { namespace, localName };
        } else {
            return new String[] { tag.getNamespace(), tag.getLocalName() };
        }
    }

    private Tag trimJSFCAttribute(Tag tag) {
        TagAttribute attr = tag.getAttributes().get("jsfc");
        if (attr != null) {
            TagAttribute[] oa = tag.getAttributes().getAll();
            TagAttribute[] na = new TagAttribute[oa.length - 1];
            int p = 0;
            for (int i = 0; i < oa.length; i++) {
                if (!"jsfc".equals(oa[i].getLocalName())) {
                    na[p++] = oa[i];
                }
            }
            return new Tag(tag, new TagAttributesImpl(na));
        }
        return tag;
    }

    private Tag trimNSAttributes(Tag tag) {
        TagAttribute[] attr = tag.getAttributes().getAll();
        int remove = 0;
        for (int i = 0; i < attr.length; i++) {
            if (attr[i].getQName().startsWith("xmlns") && tagLibrary.containsNamespace(attr[i].getValue(), null)) {
                remove |= 1 << i;
                if (log.isLoggable(Level.FINE)) {
                    log.fine(attr[i] + " Namespace Bound to TagLibrary");
                }
            }
        }
        if (remove == 0) {
            return tag;
        } else {
            List<TagAttribute> attrList = new ArrayList<>(attr.length);
            int p = 0;
            for (int i = 0; i < attr.length; i++) {
                p = 1 << i;
                if ((p & remove) == p) {
                    continue;
                }
                attrList.add(attr[i]);
            }
            attr = attrList.toArray(new TagAttribute[attrList.size()]);
            return new Tag(tag.getLocation(), tag.getNamespace(), tag.getLocalName(), tag.getQName(), new TagAttributesImpl(attr));
        }
    }

    /**
     *
     * @param units the compilation units.
     * @return Get the view
     */
    private CompilationUnit getViewRootUnitFromStack(Stack<CompilationUnit> units) {
        CompilationUnit result = null;
        Iterator<CompilationUnit> iterator = units.iterator();
        while (iterator.hasNext()) {
            CompilationUnit compilationUnit = iterator.next();
            if (compilationUnit instanceof TagUnit) {
                TagUnit tagUnit = (TagUnit) compilationUnit;
                String ns = tagUnit.getTag().getNamespace();
                if (CoreLibrary.NAMESPACES.contains(ns) && tagUnit.getTag().getLocalName().equals("view")) {
                    result = tagUnit;
                    break;
                }
            }
        }
        return result;
    }
}
