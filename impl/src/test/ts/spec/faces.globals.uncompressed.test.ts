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
