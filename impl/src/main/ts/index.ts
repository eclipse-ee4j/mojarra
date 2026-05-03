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
