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

package com.sun.faces.facelets.tag.faces;

import java.util.List;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.annotation.FacesComponentUsage;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

public class FacesComponentTagLibrary extends LazyTagLibrary {

    private ApplicationAssociate appAss;

    public FacesComponentTagLibrary(String ns) {
        super(ns);
        if (null == ns) {
            throw new NullPointerException();
        }
        appAss = ApplicationAssociate.getCurrentInstance();
    }

    @Override
    public boolean containsTagHandler(String ns, String localName) {
        Util.notNull("namespace", ns);
        Util.notNull("tagName", localName);

        if (!ns.equals(getNamespace())) {
            return false;
        }

        // Check the cache maintained by our superclass...
        boolean containsTagHandler = super.containsTagHandler(ns, localName);
        if (!containsTagHandler) {
            FacesComponentUsage matchingFacesComponentUsage = findFacesComponentUsageForLocalName(ns, localName);
            containsTagHandler = null != matchingFacesComponentUsage;

        }
        return containsTagHandler;
    }

    private FacesComponentUsage findFacesComponentUsageForLocalName(String ns, String localName) {
        FacesComponentUsage result = null;

        Util.notNull("namespace", ns);
        Util.notNull("tagName", localName);

        if (!ns.equals(getNamespace())) {
            return result;
        }
        List<FacesComponentUsage> componentsForNamespace = appAss.getComponentsForNamespace(ns);
        String tagName;
        for (FacesComponentUsage cur : componentsForNamespace) {
            FacesComponent curFacesComponent = cur.getAnnotation();
            tagName = curFacesComponent.tagName();
            // if the current entry has an explicitly declared tagName...
            if (null != tagName && 0 < tagName.length()) {
                // compare it to the argument tagName
                if (localName.equals(tagName)) {
                    result = cur;
                    break;
                }
            } else if (null != tagName) {
                tagName = cur.getTarget().getSimpleName();
                tagName = tagName.substring(0, 1).toLowerCase() + tagName.substring(1);
                if (localName.equals(tagName)) {
                    result = cur;
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public TagHandler createTagHandler(String ns, String localName, TagConfig tag) throws FacesException {
        assert containsTagHandler(ns, localName);
        TagHandler result = super.createTagHandler(ns, localName, tag);
        if (null == result) {
            FacesComponentUsage facesComponentUsage = findFacesComponentUsageForLocalName(ns, localName);
            String componentType = facesComponentUsage.getAnnotation().value();

            if (null == componentType || 0 == componentType.length()) {
                componentType = facesComponentUsage.getTarget().getSimpleName();
                componentType = Character.toLowerCase(componentType.charAt(0)) + componentType.substring(1);
            }

            UIComponent throwAwayComponent = FacesContext.getCurrentInstance().getApplication().createComponent(componentType);
            String rendererType = throwAwayComponent.getRendererType();
            super.addComponent(localName, componentType, rendererType);
            result = super.createTagHandler(ns, localName, tag);
        }
        return result;
    }

    @Override
    public boolean tagLibraryForNSExists(String ns) {
        boolean result = false;
        List<FacesComponentUsage> componentsForNamespace = appAss.getComponentsForNamespace(ns);

        result = !componentsForNamespace.isEmpty();

        return result;
    }

}
