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

package jakarta.faces.component.behavior;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.ClientBehaviorRenderer;
import jakarta.faces.render.RenderKit;

/**
 * <p class="changed_added_2_0">
 * <strong>ClientBehaviorBase</strong> is a convenience base class that implements the default concrete behavior of all
 * methods defined by {@link ClientBehavior}.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * Subclasses should either override getRendererType() to identify the {@link ClientBehaviorRenderer} to delegate to, or
 * they should override <code>getScript()</code> to locally generate the desired Behavior script, and
 * <code>decode()</code>.
 * </p>
 *
 * </div>
 *
 * @since 2.0
 */
public class ClientBehaviorBase extends BehaviorBase implements ClientBehavior {

    private static final Logger logger = Logger.getLogger("jakarta.faces.component.behavior", "jakarta.faces.LogStrings");

    /**
     * <p class="changed_added_2_0">
     * Default implementation of of {@link ClientBehavior#getScript}. If a {@link ClientBehaviorRenderer} is available for
     * the specified behavior renderer type, this method delegates to the {@link ClientBehaviorRenderer#getScript} method.
     * Otherwise, this method returns null.
     * </p>
     *
     * @param behaviorContext the {@link ClientBehaviorContext}
     *
     * @return the script provided by the associated ClientBehaviorRenderer, or null if no ClientBehaviorRenderer is
     * available.
     *
     * @throws NullPointerException if <code>behaviorContext</code> is <code>null</code>
     *
     * @since 2.0
     */
    @Override
    public String getScript(ClientBehaviorContext behaviorContext) {

        if (null == behaviorContext) {
            throw new NullPointerException();
        }

        ClientBehaviorRenderer renderer = getRenderer(behaviorContext.getFacesContext());
        String script = null;
        if (null != renderer) {
            script = renderer.getScript(behaviorContext, this);
        }
        return script;
    }

    /**
     * <p class="changed_added_2_0">
     * Default implementation of of {@link ClientBehavior#decode}. If a {@link ClientBehaviorRenderer} is available for the
     * specified behavior renderer type, this method delegates to the ClientBehaviorRenderer's decode() method. Otherwise,
     * no decoding is performed.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @param component {@link UIComponent} the component associated with this {@link ClientBehavior}
     *
     * @throws NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>.
     *
     * @since 2.0
     */
    @Override
    public void decode(FacesContext context, UIComponent component) {

        if (null == context || null == component) {
            throw new NullPointerException();
        }

        ClientBehaviorRenderer renderer = getRenderer(context);
        if (null != renderer) {
            renderer.decode(context, component, this);
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Returns the renderer type of the {@link ClientBehaviorRenderer} to use for the behavior. The default implementation
     * returns null. Subclasses should either override this method to return a string that identifies the type of
     * {@link ClientBehaviorRenderer} to use, or should override {@link #getScript} and perform script rendering locally in
     * the {@link ClientBehavior} implementation.
     * </p>
     *
     * @return the default renderer type, which is null.
     *
     * @since 2.0
     */
    public String getRendererType() {
        return null;
    }

    /**
     * <p class="changed_added_2_0">
     * Default implementation of {@link ClientBehavior#getHints()}. By default, no hints are specified, and this method
     * returns an empty, umodifiable set.
     * </p>
     *
     * @return an empty, unmodifiable set of {@link ClientBehaviorHint}s.
     *
     * @since 2.0
     */
    @Override
    public Set<ClientBehaviorHint> getHints() {
        return Collections.emptySet();
    }

    /**
     * <p class="changed_added_2_0">
     * Convenience method to return the {@link ClientBehaviorRenderer} instance associated with this {@link ClientBehavior},
     * if any; otherwise, return <code>null</code>.
     * </p>
     *
     * @param context {@link FacesContext} for the request we are processing
     * @return {@link ClientBehaviorRenderer} instance from the current {@link RenderKit} or null.
     *
     * @throws NullPointerException if <code>context</code> is null.
     *
     * @since 2.0
     */
    protected ClientBehaviorRenderer getRenderer(FacesContext context) {
        if (null == context) {
            throw new NullPointerException();
        }
        ClientBehaviorRenderer renderer = null;
        String rendererType = getRendererType();
        if (null != rendererType) {
            RenderKit renderKit = context.getRenderKit();
            if (null != renderKit) {
                renderer = renderKit.getClientBehaviorRenderer(rendererType);
            }
            if (null == renderer) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Can't get  behavior renderer for type " + rendererType);
                }
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("No renderer-type for behavior " + this.getClass().getName());
            }
        }
        return renderer;
    }
}
