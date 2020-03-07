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
 * Passed to the constructor of {@link TagHandler} subclass, it defines the document definition of the handler we are
 * instantiating.
 * </p>
 *
 * @since 2.0
 */
public interface TagConfig {

    /**
     * <p class="changed_added_2_0">
     * Return the <code>Tag</code> representing this handler.
     * </p>
     *
     * @return the {@code Tag} instance
     */
    Tag getTag();

    /**
     * <p class="changed_added_2_0">
     * The next {@link FaceletHandler} (child or children) to be applied. This must never be <code>null</code>.
     * </p>
     *
     * @return the next {@code FaceletHandler} to be applied
     */
    FaceletHandler getNextHandler();

    /**
     * <p class="changed_added_2_0">
     * A document-unique id, follows the convention "_tagId##"
     * </p>
     *
     * @return the id of this tag, unique within the current document.
     */
    String getTagId();
}
