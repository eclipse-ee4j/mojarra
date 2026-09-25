/**
 * Tests that the minified faces.js keeps its internals private.
 *
 * Everything in faces.js lives in one top-level block, so every helper and constant in it
 * is block scoped. Minification must preserve that scope: a build which turns them into
 * `var` publishes them on `window`, where a webapp global of the same name silently wins.
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
