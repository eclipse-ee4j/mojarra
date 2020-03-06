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

import com.sun.faces.RIConstants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.facelets.tag.TagAttributesImpl;
import com.sun.faces.facelets.tag.TagLibrary;
import com.sun.faces.facelets.tag.composite.CompositeLibrary;
import com.sun.faces.facelets.tag.composite.ImplementationHandler;
import com.sun.faces.facelets.tag.composite.InterfaceHandler;
import com.sun.faces.facelets.tag.ui.ComponentRefHandler;
import com.sun.faces.facelets.tag.ui.CompositionHandler;
import com.sun.faces.facelets.tag.ui.UILibrary;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.view.facelets.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compilation unit for managing the creation of a single FaceletHandler based
 * on events from an XML parser.
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
        this.tagDecorator = compiler.createTagDecorator();
        this.tagLibrary = compiler.createTagLibrary(this.getCompilationMessageHolder());

        // namespace management
        this.namespaceManager = new NamespaceManager();

        // tag uids
        this.tagId = 0;

        // for composition use
        this.finished = false;

        // our compilationunit stack
        this.units = new Stack<>();
        this.units.push(new CompilationUnit());

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
        if (this.finished) {
            return;
        }

        // don't carelessly add empty tags
        if (value.length() == 0) {
            return;
        }

        TextUnit unit;
        if (this.currentUnit() instanceof TextUnit) {
            unit = (TextUnit) this.currentUnit();
        } else {
            unit = new TextUnit(this.alias, this.nextTagId());
            this.startUnit(unit);
        }
        unit.writeInstruction(value);
    }

    public void writeText(String value) {

        if (this.finished) {
            return;
        }

        // don't carelessly add empty tags
        if (value.length() == 0) {
            return;
        }

        TextUnit unit;
        if (this.currentUnit() instanceof TextUnit) {
            unit = (TextUnit) this.currentUnit();
        } else {
            unit = new TextUnit(this.alias, this.nextTagId());
            this.startUnit(unit);
        }
        unit.write(value);
    }

    public void writeComment(String text) {
        if (this.compiler.isTrimmingComments())
            return; 

        if (this.finished) {
            return;
        }
          
        // don't carelessly add empty tags
        if (text.length() == 0) {
            return;
        }
          
        TextUnit unit;
        if (this.currentUnit() instanceof TextUnit) {
            unit = (TextUnit) this.currentUnit();
        } else {
            unit = new TextUnit(this.alias, this.nextTagId());
            this.startUnit(unit);
        }
          
        unit.writeComment(text);
    }

    public void writeWhitespace(String text) {
        if (!this.compiler.isTrimmingWhitespace()) {
            this.writeText(text);
        }
    }

    private String nextTagId() {
        return Integer.toHexString(Math.abs(this.alias.hashCode() ^ 13 * this.tagId++));
    }

    public void pushTag(Tag orig) {

        if (this.finished) {
            return;
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Tag Pushed: " + orig);
        }

        Tag t = this.tagDecorator.decorate(orig);
        String[] qname = this.determineQName(t);
        t = this.trimAttributes(t);

        boolean handled = false;

        if (isTrimmed(qname[0], qname[1])) {
        	if (log.isLoggable(Level.FINE)) {
        		log.fine("Composition Found, Popping Parent Tags");
        	}
           
            CompilationUnit viewRootUnit = getViewRootUnitFromStack(units);
            this.units.clear();
            NamespaceUnit nsUnit = this.namespaceManager
                    .toNamespaceUnit(this.tagLibrary);
            this.units.push(nsUnit);
            if (viewRootUnit != null) {
                viewRootUnit.removeChildren();
                this.currentUnit().addChild(viewRootUnit);
            }
            this.startUnit(new TrimmedTagUnit(this.tagLibrary, qname[0], qname[1], t, this
                    .nextTagId()));
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
            this.units.clear();
            NamespaceUnit nsUnit = this.namespaceManager
                    .toNamespaceUnit(this.tagLibrary);
            this.units.push(nsUnit);
            this.currentUnit().addChild(iface);
            this.startUnit(new ImplementationUnit(this.tagLibrary, qname[0], qname[1], t, this
                    .nextTagId()));
            if (log.isLoggable(Level.FINE)) {
            	log.fine("New Namespace and ImplementationUnit pushed");
            }
            
        } else if (isRemove(qname[0], qname[1])) {
            this.units.push(new RemoveUnit());
        } else if (this.tagLibrary.containsTagHandler(qname[0], qname[1])) {
            if (isInterface(qname[0], qname[1])) {
                InterfaceUnit iface = new InterfaceUnit(this.tagLibrary, qname[0], qname[1], t, this.nextTagId());
                setInterfaceUnit(iface);
                this.startUnit(iface);
            } else {
                this.startUnit(new TagUnit(this.tagLibrary, qname[0], qname[1], t, this.nextTagId()));
            }
        } else if (this.tagLibrary.containsNamespace(qname[0], t)) {
            throw new TagException(orig, "Tag Library supports namespace: "+qname[0]+", but no tag was defined for name: "+qname[1]);
        } else {
            TextUnit unit;
            if (this.currentUnit() instanceof TextUnit) {
                unit = (TextUnit) this.currentUnit();
            } else {
                unit = new TextUnit(this.alias, this.nextTagId());
                this.startUnit(unit);
            }
            unit.startTag(t);
        }
    }

    public void popTag() {

        if (this.finished) {
            return;
        }

        CompilationUnit unit = this.currentUnit();

        if (unit instanceof TextUnit) {
            TextUnit t = (TextUnit) unit;
            if (t.isClosed()) {
                this.finishUnit();
            } else {
                t.endTag();
                return;
            }
        }

        unit = this.currentUnit();
        if (unit instanceof TagUnit) {
            TagUnit t = (TagUnit) unit;
            if (t instanceof TrimmedTagUnit) {
                this.finished = true;
                return;
            }
        }

        this.finishUnit();
    }

    public void popNamespace(String ns) {
        this.namespaceManager.popNamespace(ns);
        if (this.currentUnit() instanceof NamespaceUnit) {
            this.finishUnit();
        }
    }


    public void pushNamespace(String prefix, String uri) {

        if (log.isLoggable(Level.FINE)) {
            log.fine("Namespace Pushed " + prefix + ": " + uri);
        }

        boolean alreadyPresent = this.namespaceManager.getNamespace(prefix) != null;
        this.namespaceManager.pushNamespace(prefix, uri);
        NamespaceUnit unit;
        if (this.currentUnit() instanceof NamespaceUnit && !alreadyPresent) {
            unit = (NamespaceUnit) this.currentUnit();
        } else {
            unit = new NamespaceUnit(this.tagLibrary);
            this.startUnit(unit);
        }
        unit.setNamespace(prefix, uri);
    }

    public FaceletHandler createFaceletHandler() {
        return ((CompilationUnit) this.units.get(0)).createFaceletHandler();
    }

    private CompilationUnit currentUnit() {
        if (!this.units.isEmpty()) {
            return (CompilationUnit) this.units.peek();
        }
        return null;
    }

    private void finishUnit() {
        CompilationUnit unit = this.units.pop();
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
            log.fine("Starting Unit: " + unit + " and adding it to parent: "
                    + this.currentUnit());
        }

        this.currentUnit().addChild(unit);
        this.units.push(unit);
        unit.startNotify(this);
    }

    private Tag trimAttributes(Tag tag) {
        Tag t = this.trimJSFCAttribute(tag);
        t = this.trimNSAttributes(t);
        return t;
    }

    protected static boolean isRemove(String ns, String name) {
        return (UILibrary.Namespace.equals(ns) || UILibrary.XMLNSNamespace.equals(ns))
                && "remove".equals(name);
    }

    // edburns: This is the magic line that tells the system to trim out the 
    // extra content above and below the tag.
    protected static boolean isTrimmed(String ns, String name) {
        boolean matchInUILibrary = (UILibrary.Namespace.equals(ns) || UILibrary.XMLNSNamespace.equals(ns)) && 
                (CompositionHandler.Name.equals(name) || 
                ComponentRefHandler.Name.equals(name));
        return matchInUILibrary;
    }

    protected static boolean isImplementation(String ns, String name) {
        boolean matchInCompositeLibrary = (CompositeLibrary.Namespace.equals(ns) || CompositeLibrary.XMLNSNamespace.equals(ns)) && 
                (ImplementationHandler.Name.equals(name));
        return matchInCompositeLibrary;
    }

    protected static boolean isInterface(String ns, String name) {
        boolean matchInCompositeLibrary = (CompositeLibrary.Namespace.equals(ns) || CompositeLibrary.XMLNSNamespace.equals(ns)) && 
                (InterfaceHandler.Name.equals(name));
        return matchInCompositeLibrary;
    }

    private String[] determineQName(Tag tag) {
        TagAttribute attr = tag.getAttributes().get("jsfc");
        if (attr != null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(attr + " JSF Facelet Compile Directive Found");
            }
            String value = attr.getValue();
            String namespace, localName;
            int c = value.indexOf(':');
            if (c == -1) {
                namespace = this.namespaceManager.getNamespace("");
                localName = value;
            } else {
                String prefix = value.substring(0, c);
                namespace = this.namespaceManager.getNamespace(prefix);
                if (namespace == null) {
                    throw new TagAttributeException(tag, attr,
                            "No Namespace matched for: " + prefix);
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
            if (attr[i].getQName().startsWith("xmlns")
                    && this.tagLibrary.containsNamespace(attr[i].getValue(), null)) {
                remove |= 1 << i;
                if (log.isLoggable(Level.FINE)) {
                    log.fine(attr[i] + " Namespace Bound to TagLibrary");
                }
            }
        }
        if (remove == 0) {
            return tag;
        } else {
            List attrList = new ArrayList(attr.length);
            int p = 0;
            for (int i = 0; i < attr.length; i++) {
                p = 1 << i;
                if ((p & remove) == p) {
                    continue;
                }
                attrList.add(attr[i]);
            }
            attr = (TagAttribute[]) attrList.toArray(new TagAttribute[attrList
                    .size()]);
            return new Tag(tag.getLocation(), tag.getNamespace(), tag
                    .getLocalName(), tag.getQName(), new TagAttributesImpl(attr));
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
        while(iterator.hasNext()) {
            CompilationUnit compilationUnit = iterator.next();
            if (compilationUnit instanceof TagUnit) {
                TagUnit tagUnit = (TagUnit) compilationUnit;
                String ns = tagUnit.getTag().getNamespace();
                if ((ns.equals(RIConstants.CORE_NAMESPACE) || ns.equals(RIConstants.CORE_NAMESPACE_NEW)) &&
                        tagUnit.getTag().getLocalName().equals("view")) {
                    result = tagUnit;
                    break;
                }
            }
        }
        return result;
    }
}
