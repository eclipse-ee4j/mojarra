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
 * A Facelet version of the Jakarta Server Pages <code>ConverterTag</code>. All the attributes specified
 * in the documentation for the converter tags are valid attributes.
 * </p>
 *
 * @since 2.0
 *
 */
public interface ConverterConfig extends TagConfig {

    /**
     * <p class="changed_added_2_0">
     * Return the converter id to be used in instantiating this converter
     * </p>
     *
     * @return the converter id to be used in instantiating this converter
     */
    String getConverterId();

}
