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
 * Bundle entry for the Mojarra impl of faces.js.
 *
 * Each side-effect import contributes its members to the global window:
 * - `./faces`   — public Jakarta Faces JavaScript API (faces.ajax / push / util / ...).
 * - `./mojarra` — Mojarra-private companion namespace used by Mojarra renderers.
 *
 * Loading order matches the legacy single-file layout: `faces` is set up
 * first under the version-guard, then `mojarra` is bound unconditionally.
 */
import "./faces";
import "./mojarra";
