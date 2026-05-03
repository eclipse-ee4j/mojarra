/**
 * Mojarra-private companion to the public `faces` namespace.
 *
 * Used by Mojarra's own renderers (command link/button, ajax behaviour,
 * command script) to invoke faces.ajax.request() and to manage hidden
 * form parameters. Not part of the Jakarta Faces spec.
 */

/** Hidden input elements added by {@link apf}, tracked on the form for later removal. */
type FormWithAddedParams = HTMLFormElement & { adp?: HTMLInputElement[] };

export interface MojarraNamespace {
    /** Cached return value of faces.getProjectStage(). */
    projectStageCache?: string;

    /** Delete the hidden parameters previously added by {@link apf}. */
    dpf(f: FormWithAddedParams): void;

    /** Add hidden parameters to the form, tracked on `f.adp` for later removal. */
    apf(f: FormWithAddedParams, pvp: Record<string, string>): void;

    /** Submit the form (command link/button) with optional target, adding then removing hidden params. */
    cljs(f: HTMLFormElement, pvp: Record<string, string>, t?: string): void;

    /** Invoke `f` with `t` as `this` and `e` as the first argument. */
    facescbk<T, E, R>(f: (this: T, e: E) => R, t: T, e: E): R;

    /** Trigger a faces.ajax.request() call from an AjaxBehaviorRenderer-rendered script. */
    ab(s: Element | string, e?: Event, n?: string, ex?: string, re?: string, op?: Record<string, unknown>): void;

    /** Register a window-load callback (used by command script with autorun=true). */
    l(callback: () => void): void;

    /** Add an event listener to the element with the given id. */
    ael(id: string, ev: string, fn: EventListenerOrEventListenerObject): void;
}

declare global {
    interface Window {
        mojarra: MojarraNamespace;
    }
}

const mojarra: MojarraNamespace = window.mojarra ?? ({} as MojarraNamespace);
window.mojarra = mojarra;

mojarra.dpf = function dpf(f) {
    const adp = f.adp;
    if (adp !== null) {
        for (const param of adp!) {
            param.remove();
        }
    }
};

mojarra.apf = function apf(f, pvp) {
    const adp: HTMLInputElement[] = [];
    f.adp = adp;
    let i = 0;
    for (const k of Object.keys(pvp)) {
        const p = document.createElement("input");
        p.type = "hidden";
        p.name = k;
        p.value = pvp[k];
        f.appendChild(p);
        adp[i++] = p;
    }
};

mojarra.cljs = function cljs(f, pvp, t) {
    mojarra.apf(f, pvp);
    const ft = f.target;
    if (t) {
        f.target = t;
    }

    const input = document.createElement("input");
    input.type = "submit";
    f.appendChild(input);
    input.click();
    input.remove();

    f.target = ft;
    mojarra.dpf(f);
};

mojarra.facescbk = function facescbk(f, t, e) {
    return f.call(t, e);
};

mojarra.ab = function ab(s, e, n, ex, re, op) {
    if (!op) op = {};
    if (n)   op["jakarta.faces.behavior.event"] = n;
    if (ex)  op["execute"] = ex;
    if (re)  op["render"] = re;
    window.faces.ajax.request(s, e, op);
};

mojarra.l = function l(callback) {
    if (document.readyState === "complete") {
        setTimeout(callback);
    } else {
        window.addEventListener("load", callback);
    }
};

mojarra.ael = function ael(id, ev, fn) {
    document.getElementById(id)!.addEventListener(ev, fn);
};
