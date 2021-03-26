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

package jakarta.faces.render;

import java.io.IOException;

import jakarta.faces.FacesWrapper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;

/**
 * <p class="changed_added_2_2">
 * <span class="changed_modified_2_3">Provides</span> a simple implementation of {@link Renderer} that can be subclassed
 * by developers wishing to provide specialized behavior to an existing {@link Renderer} instance. The default
 * implementation of all methods is to call through to the wrapped {@link Renderer} instance.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.2
 */

public abstract class RendererWrapper extends Renderer implements FacesWrapper<Renderer> {

    private Renderer wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public RendererWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this renderer has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public RendererWrapper(Renderer wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Renderer getWrapped() {
        return wrapped;
    }

    @Override
    public String convertClientId(FacesContext context, String clientId) {
        return getWrapped().convertClientId(context, clientId);
    }

    @Override
    public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
        return getWrapped().getConvertedValue(context, component, submittedValue);
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        getWrapped().decode(context, component);
    }

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        getWrapped().encodeBegin(context, component);
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        getWrapped().encodeChildren(context, component);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        getWrapped().encodeEnd(context, component);
    }

    @Override
    public boolean getRendersChildren() {
        return getWrapped().getRendersChildren();
    }

}
