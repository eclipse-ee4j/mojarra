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

package com.sun.faces.facelets.tag.faces.core;

import java.io.IOException;
import java.io.Serializable;

import com.sun.faces.application.ApplicationAssociate;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.PostRenderViewEvent;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

/**
 * This is the TagHandler for the f:event tag.
 */
public class EventHandler extends TagHandler {
    protected final TagAttribute type;
    protected final TagAttribute listener;

    public EventHandler(TagConfig config) {
        super(config);
        type = getRequiredAttribute("type");
        listener = getRequiredAttribute("listener");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (ComponentHandler.isNew(parent)) {
            Class<? extends SystemEvent> eventClass = getEventClass(ctx);
            UIViewRoot viewRoot = ctx.getFacesContext().getViewRoot();
            // ensure that f:event can be used anywhere on the page for preRenderView and postRenderView,
            // not just as a direct child of the viewRoot
            if (null != viewRoot && (PreRenderViewEvent.class == eventClass || PostRenderViewEvent.class == eventClass) && parent != viewRoot) {
                parent = viewRoot;
            }
            if (eventClass != null) {
                parent.subscribeToEvent(eventClass,
                        new DeclarativeSystemEventListener(listener.getMethodExpression(ctx, Object.class, new Class<?>[] { ComponentSystemEvent.class }),
                                listener.getMethodExpression(ctx, Object.class, new Class<?>[] {})));
            }
        }
    }

    protected Class<? extends SystemEvent> getEventClass(FaceletContext ctx) {
        String eventType = (String) type.getValueExpression(ctx, String.class).getValue(ctx);
        if (eventType == null) {
            throw new FacesException("Attribute 'type' can not be null");
        }

        return ApplicationAssociate.getInstance(ctx.getFacesContext().getExternalContext()).getNamedEventManager().getNamedEvent(eventType);
    }

}

class DeclarativeSystemEventListener implements ComponentSystemEventListener, Serializable {

    private static final long serialVersionUID = 8945415935164238908L;

    private MethodExpression oneArgListener;
    private MethodExpression noArgListener;

    // Necessary for state saving
    public DeclarativeSystemEventListener() {
    }

    public DeclarativeSystemEventListener(MethodExpression oneArg, MethodExpression noArg) {
        oneArgListener = oneArg;
        noArgListener = noArg;
    }

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        final ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        try {
            noArgListener.invoke(elContext, new Object[] {});
        } catch (MethodNotFoundException | IllegalArgumentException mnfe) {
            // Attempt to call public void method(ComponentSystemEvent event)
            oneArgListener.invoke(elContext, new Object[] { event });
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeclarativeSystemEventListener that = (DeclarativeSystemEventListener) o;

        if (noArgListener != null ? !noArgListener.equals(that.noArgListener) : that.noArgListener != null) {
            return false;
        }
        if (oneArgListener != null ? !oneArgListener.equals(that.oneArgListener) : that.oneArgListener != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = oneArgListener != null ? oneArgListener.hashCode() : 0;
        result = 31 * result + (noArgListener != null ? noArgListener.hashCode() : 0);
        return result;
    }
}
