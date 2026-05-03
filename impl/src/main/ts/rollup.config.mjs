import typescript from "@rollup/plugin-typescript";

const BANNER = `/*!
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 * Copyright 2004-2008 Emmanouil Batsis, mailto: mbatsis at users full stop sourceforge full stop net
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
 *
 * @project Faces JavaScript Library
 * @version 5.0.0
 * @description This is the standard implementation of the Faces JavaScript Library.
 */`;

export default {
    input: "index.ts",
    output: {
        file: "../../../target/classes/META-INF/resources/jakarta.faces/faces-uncompressed.js",
        format: "iife",
        banner: BANNER,
        strict: false,
        generatedCode: { constBindings: true },
    },
    plugins: [
        typescript({
            tsconfig: "./tsconfig.json",
            noEmitOnError: true,
            compilerOptions: { noEmit: false },
        }),
    ],
};
