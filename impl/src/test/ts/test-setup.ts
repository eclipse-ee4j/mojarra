/**
 * Shared test configuration and helpers for faces.js tests.
 */

import fs from "fs";
import path from "path";

declare global {
    var faces: Record<string, unknown>;
    var mojarra: Record<string, unknown>;
}

export const FACES_JS_UNCOMPRESSED = path.resolve(__dirname, "../../../target/classes/META-INF/resources/jakarta.faces/faces-uncompressed.js");
export const FACES_JS = path.resolve(__dirname, "../../../target/classes/META-INF/resources/jakarta.faces/faces.js");

/**
 * Parse the @version JSDoc tag from faces-uncompressed.js and derive expected specversion and implversion.
 * E.g. @version 4.0.15 -> specversion 40000, implversion 15.
 */
export function parseFacesJsVersion(): { specversion: number; implversion: number } {
    const source = fs.readFileSync(FACES_JS_UNCOMPRESSED, "utf-8");
    const match = source.match(/@version\s+(\d+)\.(\d+)\.(\d+)/);
    if (!match) {
        throw new Error("Could not parse @version tag from faces-uncompressed.js");
    }
    const [, major, minor, patch] = match;
    return {
        specversion: parseInt(major) * 10000 + parseInt(minor) * 100,
        implversion: parseInt(patch),
    };
}

/**
 * Load the compressed faces.js into jsdom, replacing EL expressions with test values.
 * Call this in beforeAll().
 */
export function loadFacesJs(): void {
    loadFacesJsFile(FACES_JS);
}

/**
 * Load the uncompressed faces.js into jsdom, replacing EL expressions with test values.
 * This is the variant served in Development stage.
 * Call this in beforeAll().
 */
export function loadFacesJsUncompressed(): void {
    loadFacesJsFile(FACES_JS_UNCOMPRESSED);
}

function loadFacesJsFile(file: string): void {
    const source = fs.readFileSync(file, "utf-8");
    const { specversion, implversion } = parseFacesJsVersion();
    const evaluated = source
        .replace("#{facesContext.namingContainerSeparatorChar}", ":")
        .replace("#{facesContext.externalContext.requestContextPath}", "/test")
        .replace(/#\{applicationScope\["com\.sun\.faces\.mojarraVersion"\]\.specversion\}/g, String(specversion))
        .replace(/#\{applicationScope\["com\.sun\.faces\.mojarraVersion"\]\.implversion\}/g, String(implversion));

    const script = document.createElement("script");
    script.textContent = evaluated;
    document.head.appendChild(script);
}

/**
 * Names of all own properties currently present on `window`.
 * Capture this at module level, before faces.js is loaded, to get a clean baseline.
 */
export function globalNames(): Set<string> {
    return new Set(Object.getOwnPropertyNames(window));
}

/**
 * Names added to `window` since the given baseline, sorted.
 */
export function globalNamesAddedSince(baseline: Set<string>): string[] {
    return Object.getOwnPropertyNames(window).filter(name => !baseline.has(name)).sort();
}
