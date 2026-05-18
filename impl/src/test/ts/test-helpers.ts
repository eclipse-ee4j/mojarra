/**
 * Shared mock XMLHttpRequest and DOM helpers for faces.ajax tests.
 */

export interface MockXHRInstance {
    method: string | null;
    url: string | null;
    async: boolean;
    requestHeaders: Record<string, string>;
    /** Whatever was passed to send(): a URL-encoded string for normal POSTs, or a FormData
     *  for multipart submissions. Tests that read the body as a string should narrow first. */
    body: string | null;
    readyState: number;
    status: number;
    responseText: string;
    responseXML: Document | null;
    onreadystatechange: ((this: XMLHttpRequest, ev: Event) => void) | null;
    open(method: string, url: string, async?: boolean): void;
    setRequestHeader(name: string, value: string): void;
    send(body?: string | FormData | null): void;
    /** Test helper: simulate server response. */
    respond(status: number, responseText: string, responseXML?: string): void;
}

let xhrInstances: MockXHRInstance[];
let OriginalXHR: typeof XMLHttpRequest;

export function installMockXHR(): void {
    xhrInstances = [];
    OriginalXHR = window.XMLHttpRequest;

    (window as unknown as Record<string, unknown>).XMLHttpRequest = function MockXHR(this: MockXHRInstance) {
        this.method = null;
        this.url = null;
        this.async = true;
        this.requestHeaders = {};
        this.body = null;
        this.readyState = 0;
        this.status = 0;
        this.responseText = "";
        this.responseXML = null;
        this.onreadystatechange = null;

        this.open = function (method: string, url: string, async = true) {
            this.method = method;
            this.url = url;
            this.async = async;
            this.readyState = 1;
        };

        this.setRequestHeader = function (name: string, value: string) {
            this.requestHeaders[name] = value;
        };

        this.send = function (body?: string | FormData | null) {
            this.body = (body as string | null) ?? null;
        };

        this.respond = function (status: number, responseText: string, responseXML?: string) {
            this.status = status;
            this.responseText = responseText;
            if (responseXML) {
                this.responseXML = new DOMParser().parseFromString(responseXML, "application/xml");
            }
            this.readyState = 4;
            if (this.onreadystatechange) {
                this.onreadystatechange.call(this as unknown as XMLHttpRequest, new Event("readystatechange"));
            }
        };

        xhrInstances.push(this);
    } as unknown as typeof XMLHttpRequest;
}

export function drainXHRQueue(): void {
    // Complete any pending XHRs so the internal queue is drained for the next test.
    for (const xhr of xhrInstances) {
        if (xhr.readyState !== 4) {
            xhr.status = 200;
            xhr.responseText = "";
            xhr.responseXML = new DOMParser().parseFromString(
                '<?xml version="1.0" encoding="UTF-8"?><partial-response id="j_id1"><changes></changes></partial-response>',
                "application/xml"
            );
            xhr.readyState = 4;
            if (xhr.onreadystatechange) {
                xhr.onreadystatechange.call(xhr as unknown as XMLHttpRequest, new Event("readystatechange"));
            }
        }
    }
}

export function uninstallMockXHR(): void {
    drainXHRQueue();
    window.XMLHttpRequest = OriginalXHR;
}

export function lastXHR(): MockXHRInstance {
    return xhrInstances[xhrInstances.length - 1];
}

export function getXHRInstances(): MockXHRInstance[] {
    return xhrInstances;
}

export function createAjaxForm(formId = "testForm", buttonId = "testButton"): { form: HTMLFormElement; button: HTMLButtonElement } {
    const form = document.createElement("form");
    form.id = formId;
    form.method = "post";
    form.action = "/test/action";

    const viewState = Object.assign(document.createElement("input"), {
        type: "hidden",
        name: "jakarta.faces.ViewState",
        value: "testViewState123",
    });
    form.appendChild(viewState);

    const button = document.createElement("button");
    button.type = "button";
    button.id = buttonId;
    button.name = buttonId;
    form.appendChild(button);

    document.body.appendChild(form);
    return { form, button };
}
