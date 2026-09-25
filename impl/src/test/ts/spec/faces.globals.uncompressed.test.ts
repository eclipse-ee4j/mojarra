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

/**
 * Tests that the uncompressed faces.js keeps its internals private.
 *
 * This is the variant served in Development stage, so its global surface must match the
 * minified one served in every other stage. A difference means faces.js behaves one way
 * while developing and another way in production.
 *
 * This is a separate test file because each variant needs its own jsdom environment.
 */

import { loadFacesJsUncompressed, globalNames, globalNamesAddedSince } from "../test-setup";

const baseline = globalNames();

beforeAll(() => loadFacesJsUncompressed());

describe("faces-uncompressed.js global scope", () => {

    test("publishes only faces and mojarra on window", () => {
        expect(globalNamesAddedSince(baseline)).toEqual(["faces", "mojarra"]);
    });
});
