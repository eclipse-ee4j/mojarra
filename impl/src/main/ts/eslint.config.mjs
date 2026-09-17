/**
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
import stylistic from "@stylistic/eslint-plugin";
import { defineConfig } from "eslint/config";
import tseslint from "typescript-eslint";

export default defineConfig(
    {
        files: ["**/*.ts"],
        languageOptions: {
            parser: tseslint.parser,
        },
    },
    {
        files: ["**/*.ts"],
        plugins: {
            "@stylistic": stylistic,
        },
        rules: {
            "@stylistic/brace-style": ["error", "stroustrup"],
            "@stylistic/indent": ["error", 4],
            "@stylistic/semi": ["error", "always"],
            "@stylistic/quotes": ["error", "double", { avoidEscape: true }],
            "@stylistic/comma-spacing": ["error", { before: false, after: true }],
            "@stylistic/space-before-blocks": ["error", "always"],
            "@stylistic/space-before-function-paren": ["error", { anonymous: "never", named: "never", asyncArrow: "never", catch: "always" }],
            "@stylistic/keyword-spacing": ["error", { before: true, after: true }],
            "@stylistic/space-infix-ops": "error",
            "@stylistic/arrow-spacing": ["error", { before: true, after: true }],
            "@stylistic/max-len": ["error", { code: 160, ignoreComments: true }],
            "@stylistic/no-trailing-spaces": "error",
            "@stylistic/eol-last": ["error", "always"],
        },
    },
);
