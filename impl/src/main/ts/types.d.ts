///
/// Copyright (c) 2026 Contributors to the Eclipse Foundation.
///
/// This program and the accompanying materials are made available under the
/// terms of the Eclipse Public License v. 2.0, which is available at
/// http://www.eclipse.org/legal/epl-2.0.
///
/// This Source Code may also be made available under the following Secondary
/// Licenses when the conditions for such availability set forth in the
/// Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
/// version 2 with the GNU Classpath Exception, which is available at
/// https://www.gnu.org/software/classpath/license.html.
///
/// SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
///

/*
 * Internal type declarations for the Mojarra impl of faces.js.
 * Not shipped — only consumed by tsc at compile time.
 *
 * The legacy monolithic faces.ts is still under `@ts-nocheck`; declaring
 * `faces` as `any` here keeps cross-module references (e.g.
 * `window.faces` from mojarra.ts) ergonomic during the transition.
 *
 * Per-module strict typing against api/.../faces.d.ts happens as each
 * module is extracted.
 */

declare global {
    interface Window {
        faces: any;
    }

    var faces: any;
}

export {};
