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
