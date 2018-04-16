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

/**
 * <p class="changed_added_2_0">An interface that allows other code 
 * to identify FaceletHandlers that may provide text (String) content.</p>
 *
 * @since 2.0
 */
public interface TextHandler {

    /**
     * <p class="changed_added_2_0">Returns the literal String value of the 
     * contained text.</p>
     * 
     * @return the literal String value of the contained text
     */
    public String getText();
    
    /**
     * <p class="changed_added_2_0">Returns the resolved literal String value 
     * of the contained text after evaluating EL.</p>
     *
     * @param ctx the <code>FaceletContext</code> for this view execution
     * 
     * @return the resolved literal String value of the contained text after evaluating EL
     */
    public String getText(FaceletContext ctx);
}
