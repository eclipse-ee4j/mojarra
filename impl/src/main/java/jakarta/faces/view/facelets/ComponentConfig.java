/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2005-2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jakarta.faces.view.facelets;

/**
 * <p class="changed_added_2_0">
 * Passed to the constructor of {@link ComponentHandler}. Represents a component-type/renderer-type pair.
 * </p>
 *
 * @since 2.0
 *
 */
public interface ComponentConfig extends TagConfig {
    /**
     * <p class="changed_added_2_0">
     * ComponentType to pass to the <code>Application</code>. Cannot be <code>null</code>.
     *
     * @return the component type to pass to the {@code Application}.
     *
     * @since 2.0
     */
    String getComponentType();

    /**
     * <p class="changed_added_2_0">
     * RendererType to set on created <code>UIComponent</code> instances.
     *
     * @return the renderer type to pass to the {@code Application}.
     *
     * @since 2.0
     */
    String getRendererType();
}
