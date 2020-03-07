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

package jakarta.faces.view;

import java.io.Serializable;

/**
 * <p class="changed_added_2_0">
 * An object that represents the Location of a tag or attribute of a tag in a View Declaration Language file.
 * </p>
 *
 * @since 2.0
 *
 */
public class Location implements Serializable {

    private static final long serialVersionUID = -1962991571371912405L;

    private final String path;

    private final int line;

    private final int column;

    public Location(String path, int line, int column) {
        this.path = path;
        this.line = line;
        this.column = column;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the estimated character column.
     * </p>
     *
     * @return the column
     */
    public int getColumn() {
        return column;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the line number in the page for this location.
     * </p>
     *
     * @return the line number
     *
     */
    public int getLine() {
        return line;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the file path to the page represented by this location.
     * </p>
     *
     * @return the file path
     *
     */
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path + " @" + line + "," + column;
    }
}
