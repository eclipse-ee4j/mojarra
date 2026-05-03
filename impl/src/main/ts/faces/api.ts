/**
 * Implementation of `faces.getProjectStage`, `faces.getViewState` and `faces.getClientWindow`
 * from the Jakarta Faces JavaScript API.
 * @see api/.../faces.d.ts namespace `faces`
 */

import type { faces as FacesSpec } from "../../../../../faces/api/src/main/resources/META-INF/resources/jakarta.faces/faces";

import { UDEF, EMPTY, SPACE, FORM, ALWAYS_EXECUTE_IDS, CLIENT_WINDOW_PARAM } from "./constants";
import { isNotNull } from "./lang";
import { getElemById, getFormInputElementByName, containsNamedChild } from "./dom";

/**
 * Return the value of `Application.getProjectStage()` for the currently running application instance.
 * Calling this method must not cause any network transaction to happen to the server.
 */
export const getProjectStage: typeof FacesSpec.getProjectStage = function getProjectStage(): string {
    const moj = (window as any).mojarra;
    if (typeof moj !== "undefined" && typeof moj.projectStageCache !== "undefined") {
        return moj.projectStageCache;
    }
    const _script = document.querySelector<HTMLScriptElement>("script[src*='jakarta.faces.resource/faces.js']");
    const scriptSrcSearchParam = isNotNull(_script) ? new URLSearchParams(_script!.src) : null;

    const stage = (isNotNull(scriptSrcSearchParam) && scriptSrcSearchParam!.get("stage") === "Development") ? "Development" : "Production";

    const m = (window as any).mojarra ?? ({} as any);
    (window as any).mojarra = m;
    m.projectStageCache = stage;

    return m.projectStageCache;
};

/** Shared "successful control" form-value collector used by getViewState and getPartialViewState. */
const collectFormParams = (
    form: HTMLFormElement,
    addField: (name: string, value: string) => void,
): void => {
    const els = form.elements;
    for (const el of Array.from(els) as any[]) {
        if (el.name === EMPTY) {
            continue;
        }
        if (!el.disabled) {
            switch (el.type) {
                case "submit":
                case "reset":
                case "image":
                case "file":
                    break;
                case "select-one":
                    if (el.selectedIndex >= 0) {
                        addField(el.name, el.options[el.selectedIndex].value);
                    }
                    break;
                case "select-multiple":
                    for (const option of el.options) {
                        if (option.selected) {
                            addField(el.name, option.value);
                        }
                    }
                    break;
                case "checkbox":
                case "radio":
                    if (el.checked) {
                        addField(el.name, el.value || "on");
                    }
                    break;
                default: {
                    const nodeName = el.nodeName.toLowerCase();
                    if (nodeName === "input" || nodeName === "select" ||
                        nodeName === "button" || nodeName === "object" ||
                        nodeName === "textarea") {
                        addField(el.name, el.value);
                    }
                    break;
                }
            }
        }
    }
};

/**
 * Collect and encode state for only those input controls within the specified form
 * that belong to the execute component set (partial submit). ViewState and ClientWindow
 * parameters are always included.
 *
 * Internal helper used by faces.ajax.request — not part of the public spec API.
 */
export const getPartialViewState = function (form: HTMLFormElement, execute: string | undefined): string {
    if (!form) throw new Error("getPartialViewState:  form must be set");

    const partialExecuteIds = execute ? execute.split(SPACE).concat(ALWAYS_EXECUTE_IDS) : undefined;
    const partialExecuteDomElements = (partialExecuteIds ?? []).map(getElemById).filter((elem): elem is Element => !!elem);

    const params = new URLSearchParams();

    if (partialExecuteIds && !partialExecuteIds.includes(form.id)) {
        params.append(form.id, form.id);
    }

    const addField = function (name: string, value: string): void {
        const add = !partialExecuteIds || partialExecuteIds.includes(name) || containsNamedChild(partialExecuteDomElements, name);
        if (add) params.append(name, value);
    };

    collectFormParams(form, addField);
    return params.toString();
};

/**
 * Collect and encode state for input controls associated with the specified `form` element.
 * This will include all input controls of type `hidden`.
 */
export const getViewState: typeof FacesSpec.getViewState = function getViewState(form: Element): string {
    if (!form) throw new Error("faces.getViewState:  form must be set");

    const params = new URLSearchParams();
    const addField = (name: string, value: string): void => {
        params.append(name, value);
    };

    collectFormParams(form as HTMLFormElement, addField);
    return params.toString();
};

/** Return the windowId of the window in which the argument form is rendered. */
export const getClientWindow: typeof FacesSpec.getClientWindow = function getClientWindow(node?: Element | string): string | null {

    const getWindowIdElement = function (form: HTMLFormElement): Element | null {
        return getFormInputElementByName(form, CLIENT_WINDOW_PARAM)
            || form.querySelector("input[name$='" + (window as any).faces.separatorchar + CLIENT_WINDOW_PARAM + "']");
    };

    const fetchWindowIdFromForms = function (forms: HTMLFormElement[] | HTMLCollectionOf<HTMLFormElement> | NodeListOf<Element>): string | undefined {
        const result_idx: { [windowId: string]: boolean } = {};
        let result: string | undefined;
        let foundCnt = 0;

        for (const form of Array.from(forms) as HTMLFormElement[]) {
            const windowIdElement = getWindowIdElement(form) as HTMLInputElement | null;
            const windowId = windowIdElement && windowIdElement.value;
            if (UDEF !== typeof windowId) {
                if (foundCnt > 0 && UDEF === typeof result_idx[windowId as string]) throw new Error("Multiple different windowIds found in document");
                result = windowId as string;
                result_idx[windowId as string] = true;
                foundCnt++;
            }
        }

        return result;
    };

    const getChildForms = function getChildForms(currentElement: Element | null): HTMLCollectionOf<HTMLFormElement> | NodeListOf<Element> | Element[] {
        if (!currentElement) return document.forms;
        if (!currentElement.tagName) return [];
        if (currentElement.tagName.toLowerCase() === FORM) return [currentElement];
        return currentElement.querySelectorAll(FORM);
    };

    const fetchWindowIdFromURL = function fetchWindowIdFromURL(): string | null {
        return new URLSearchParams(location.search).get("windowId");
    };

    const finalNode = (node && (typeof node === "string" || node instanceof String)) ?
        document.getElementById(node as string) : ((node as Element) || null);

    const forms = getChildForms(finalNode);
    const result = fetchWindowIdFromForms(forms as any);
    return (null != result) ? result : fetchWindowIdFromURL();
};
