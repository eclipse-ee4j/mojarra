/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.view.facelets;

import java.net.URL;

/**
 * <p class="changed_added_2_0"><span class="changed_deleted_2_2">Deprecated as 
 * of JSF 2.2.  The same functionality is more correctly provided by
 * {@link javax.faces.application.ResourceHandler}.</span> Provide
 * a hook to decorate or override
 * the way that Facelets loads template files.  A default implementation
 * must be provided that satisfies the requirements for loading
 * templates as in Pre-JSF 2.0 Facelets.</p>

 * <div class="changed_added_2_0">

 * <p>If a <code>&lt;context-param&gt;</code> with the param name equal
 * to the value of {@link #FACELETS_RESOURCE_RESOLVER_PARAM_NAME}
 * exists, the runtime must interpret its value as a fully qualified
 * classname of a java class that extends <code>ResourceResolver</code>
 * and has a zero argument public constructor or a one argument public
 * constructor where the type of the argument is
 * <code>ResourceResolver</code>. If this param is set and its value
 * does not conform to those requirements, the runtime must log a
 * message and continue. If it does conform to these requirements and
 * has a one-argument constructor, the default
 * <code>ResourceResolver</code> must be passed to the constructor. If
 * it has a zero argument constructor it is invoked directly. In either
 * case, the new <code>ResourceResolver</code> replaces the old
 * one. </p>

 * </div>

 */

@Deprecated
public abstract class ResourceResolver {

    public static final String FACELETS_RESOURCE_RESOLVER_PARAM_NAME = 
        "javax.faces.FACELETS_RESOURCE_RESOLVER";

    /**
     * <p class="changed_added_2_0">Returns the <code>URL</code> of a
     * Facelet template file. Called by the Facelets Runtime to load a
     * template file referred to in a Facelets page.</p>
     *
     * @param path the internal path to the template resource.
     * @return the resolved URL.
     */
    abstract public URL resolveUrl(String path);
}
