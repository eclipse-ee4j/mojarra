/**
 * Implementation of `faces.getProjectStage`, `faces.getViewState` and `faces.getClientWindow`
 * from the Jakarta Faces JavaScript API.
 * @see api/.../faces.d.ts namespace `faces`
 */

import type FacesSpec from "../../../../../faces/api/src/main/resources/META-INF/resources/jakarta.faces/faces";
import type { MojarraNamespace } from "../mojarra";

import { UDEF, EMPTY, SPACE, FORM, ALWAYS_EXECUTE_IDS, CLIENT_WINDOW_PARAM } from "./constants";
import { isNotNull } from "./lang";
import { getElemById, getFormInputElementByName, containsNamedChild } from "./dom";

/** `window.mojarra` as seen from the api module — a (possibly-partial) mojarra namespace
 *  plus the project-stage cache the api module reads/writes. `Partial<>` because
 *  `getProjectStage()` is allowed to be invoked before mojarra.ts has registered. */
interface MojarraGlobal extends Partial<MojarraNamespace> {
    projectStageCache?: FacesSpec.ProjectStage;
}

const getMojarra = (): MojarraGlobal | undefined => (window as unknown as { mojarra?: MojarraGlobal }).mojarra;
const setMojarra = (m: MojarraGlobal): void => { (window as unknown as { mojarra: MojarraGlobal }).mojarra = m; };

/** Valid {@link FacesSpec.ProjectStage} literal values, used to validate the URL-encoded stage param. */
const PROJECT_STAGES = ["Development", "UnitTest", "SystemTest", "Production"] as const;

// Compile-time bidirectional check: PROJECT_STAGES must equal FacesSpec.ProjectStage exactly.
// If either union drifts, the line below stops compiling.
type _ProjectStagesMatchSpec =
    [FacesSpec.ProjectStage] extends [typeof PROJECT_STAGES[number]]
        ? [typeof PROJECT_STAGES[number]] extends [FacesSpec.ProjectStage] ? true : never
        : never;
const _projectStagesMatchSpec: _ProjectStagesMatchSpec = true;
void _projectStagesMatchSpec;

const isProjectStage = (value: unknown): value is FacesSpec.ProjectStage =>
    typeof value === "string" && (PROJECT_STAGES as readonly string[]).includes(value);

/**
 * Return the value of `Application.getProjectStage()` for the currently running application instance.
 * Calling this method must not cause any network transaction to happen to the server.
 */
export const getProjectStage: typeof FacesSpec.getProjectStage = function getProjectStage(): FacesSpec.ProjectStage {
    const moj = getMojarra();
    if (moj && moj.projectStageCache !== undefined) {
        return moj.projectStageCache;
    }
    const _script = document.querySelector<HTMLScriptElement>("script[src*='jakarta.faces.resource/faces.js']");
    const scriptSrcSearchParam = _script ? new URLSearchParams(_script.src) : null;

    const urlStage = scriptSrcSearchParam?.get("stage");
    const stage: FacesSpec.ProjectStage = isProjectStage(urlStage) ? urlStage : "Production";

    const m: MojarraGlobal = moj ?? {};
    setMojarra(m);
    m.projectStageCache = stage;

    return m.projectStageCache;
};

/** Form control eligible for serialization. */
type SerializableControl = HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement | HTMLButtonElement | HTMLObjectElement;

/** Shared "successful control" form-value collector used by getViewState and getPartialViewState. */
const collectFormParams = (
    form: HTMLFormElement,
    addField: (name: string, value: string) => void,
): void => {
    for (const el of Array.from(form.elements) as SerializableControl[]) {
        if (el.name === EMPTY) {
            continue;
        }
        if ((el as { disabled?: boolean }).disabled) {
            continue;
        }
        switch (el.type) {
            case "submit":
            case "reset":
            case "image":
            case "file":
                break;
            case "select-one": {
                const select = el as HTMLSelectElement;
                if (select.selectedIndex >= 0) {
                    addField(select.name, select.options[select.selectedIndex].value);
                }
                break;
            }
            case "select-multiple": {
                const select = el as HTMLSelectElement;
                for (const option of Array.from(select.options)) {
                    if (option.selected) {
                        addField(select.name, option.value);
                    }
                }
                break;
            }
            case "checkbox":
            case "radio": {
                const input = el as HTMLInputElement;
                if (input.checked) {
                    addField(input.name, input.value || "on");
                }
                break;
            }
            default: {
                const nodeName = el.nodeName.toLowerCase();
                if (nodeName === "input" || nodeName === "select" ||
                    nodeName === "button" || nodeName === "object" ||
                    nodeName === "textarea") {
                    addField(el.name, (el as HTMLInputElement).value);
                }
                break;
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
export const getPartialViewState = function getPartialViewState(form: HTMLFormElement, execute: string | undefined): string {
    if (!form) throw new Error("getPartialViewState:  form must be set");

    const partialExecuteIds = execute ? execute.split(SPACE).concat(ALWAYS_EXECUTE_IDS) : undefined;
    const partialExecuteDomElements = (partialExecuteIds ?? [])
        .map(getElemById)
        .filter((elem): elem is Element => !!elem);

    const params = new URLSearchParams();

    if (partialExecuteIds && !partialExecuteIds.includes(form.id)) {
        params.append(form.id, form.id);
    }

    const addField = (name: string, value: string): void => {
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
export const getViewState: typeof FacesSpec.getViewState = function getViewState(form: HTMLFormElement): string {
    if (!form) throw new Error("faces.getViewState:  form must be set");

    const params = new URLSearchParams();
    const addField = (name: string, value: string): void => {
        params.append(name, value);
    };

    collectFormParams(form, addField);
    return params.toString();
};

/** Return the windowId of the window in which the argument form is rendered. */
export const getClientWindow: typeof FacesSpec.getClientWindow = function getClientWindow(node?: HTMLElement | string): string | null {

    const getWindowIdElement = (form: HTMLFormElement): HTMLInputElement | null => {
        const direct = getFormInputElementByName(form, CLIENT_WINDOW_PARAM) as HTMLInputElement | null;
        if (direct) return direct;
        const sep = (window as unknown as { faces: { separatorchar: string } }).faces.separatorchar;
        return form.querySelector<HTMLInputElement>("input[name$='" + sep + CLIENT_WINDOW_PARAM + "']");
    };

    const fetchWindowIdFromForms = (forms: ArrayLike<HTMLFormElement>): string | undefined => {
        const result_idx: { [windowId: string]: boolean } = {};
        let result: string | undefined;
        let foundCnt = 0;

        for (const form of Array.from(forms)) {
            const windowIdElement = getWindowIdElement(form);
            const windowId: string | undefined = windowIdElement ? windowIdElement.value : undefined;
            if (UDEF !== typeof windowId) {
                if (foundCnt > 0 && UDEF === typeof result_idx[windowId!]) {
                    throw new Error("Multiple different windowIds found in document");
                }
                result = windowId;
                result_idx[windowId!] = true;
                foundCnt++;
            }
        }

        return result;
    };

    const getChildForms = (currentElement: HTMLElement | null): ArrayLike<HTMLFormElement> => {
        if (!currentElement) return document.forms;
        if (!currentElement.tagName) return [];
        if (currentElement.tagName.toLowerCase() === FORM) return [currentElement as HTMLFormElement];
        return currentElement.querySelectorAll<HTMLFormElement>(FORM);
    };

    const fetchWindowIdFromURL = (): string | null => {
        return new URLSearchParams(location.search).get("windowId");
    };

    const finalNode: HTMLElement | null = (node && (typeof node === "string" || node instanceof String)) ?
        document.getElementById(node as string) : ((node as HTMLElement | undefined) ?? null);

    const forms = getChildForms(finalNode);
    const result = fetchWindowIdFromForms(forms);
    return (result != null) ? result : fetchWindowIdFromURL();
};
