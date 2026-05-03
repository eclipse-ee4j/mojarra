/** Constants shared across the `faces` namespace modules. */

export const UDEF = "undefined";
export const EMPTY = "";
export const SPACE = " ";
export const FORM = "form";

// --- Hidden state field names ---------------------------------------------------
export const VIEW_STATE_PARAM = "jakarta.faces.ViewState";
export const CLIENT_WINDOW_PARAM = "jakarta.faces.ClientWindow";
export const ALWAYS_EXECUTE_IDS = [VIEW_STATE_PARAM, CLIENT_WINDOW_PARAM];
export const ENCODED_URL_PARAM = "jakarta.faces.encodedURL";

// --- Partial-ajax post-data parameter names -------------------------------------
export const SOURCE_PARAM = "jakarta.faces.source";
export const PARTIAL_AJAX_PARAM = "jakarta.faces.partial.ajax";
export const PARTIAL_EVENT_PARAM = "jakarta.faces.partial.event";
export const PARTIAL_EXECUTE_PARAM = "jakarta.faces.partial.execute";
export const PARTIAL_RENDER_PARAM = "jakarta.faces.partial.render";
export const PARTIAL_RESET_VALUES_PARAM = "jakarta.faces.partial.resetValues";
export const BEHAVIOR_EVENT_PARAM = "jakarta.faces.behavior.event";

/** Experimental: do partial submit during ajax request. */
export const PARTIAL_SUBMIT_ENABLED = true;
