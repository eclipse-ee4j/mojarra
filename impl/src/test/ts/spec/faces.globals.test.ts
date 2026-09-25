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
 * Tests that the minified faces.js keeps its internals private.
 *
 * Every helper and constant of faces.ts is module scoped, and the bundle must keep it that
 * way: a build which hoists them to the global scope publishes them on `window`, where a
 * webapp global of the same name silently wins.
 */

import { loadFacesJs, globalNames, globalNamesAddedSince } from "../test-setup";

const baseline = globalNames();

beforeAll(() => loadFacesJs());

describe("faces.js global scope", () => {

    test("publishes only faces and mojarra on window", () => {
        expect(globalNamesAddedSince(baseline)).toEqual(["faces", "mojarra"]);
    });

    test("a webapp global named after an internal constant does not alter faces.getViewState", () => {
        const getViewState = faces.getViewState as (form: HTMLFormElement) => string;
        const form = document.createElement("form");
        form.innerHTML = "<input type='text' name='tracking' value='abc'><input type='text' name='q' value='xyz'>";
        document.body.appendChild(form);

        try {
            const expected = "tracking=abc&q=xyz";
            expect(getViewState(form)).toBe(expected);

            (window as unknown as Record<string, unknown>).EMPTY = "tracking";
            expect(getViewState(form)).toBe(expected);
        }
        finally {
            delete (window as unknown as Record<string, unknown>).EMPTY;
            form.remove();
        }
    });
});
