/**
 * Implementation of the `faces.push` namespace from the Jakarta Faces JavaScript API.
 * @see api/.../faces.d.ts namespace `faces.push`
 */

import type { faces as FacesSpec } from "../../../../../faces/api/src/main/resources/META-INF/resources/jakarta.faces/faces";

type WindowAsDict = Window & { [key: string]: any };

export const push: typeof FacesSpec.push = (function (window: WindowAsDict) {

    const RECONNECT_INTERVAL = 500;
    const MAX_RECONNECT_ATTEMPTS = 25;
    const REASON_EXPIRED = "Expired";
    const REASON_UNKNOWN_CHANNEL = "Unknown channel";

    const sockets: { [clientId: string]: any } = {};
    const self: any = {};

    /**
     * Reconnecting websocket. Reconnects on timeout with cumulative intervals of 500ms,
     * up to 25 attempts (~3 minutes). The `onclose` function is called with the error code
     * of the last attempt.
     */
    function ReconnectingWebsocket(this: any, url: string, channel: string,
        onopen: Function, onmessage: Function, onerror: Function, onclose: Function,
        behaviors: { [message: string]: Function[] }) {

        let socket: WebSocket | null;
        let reconnectAttempts: number | null;
        const self = this;

        self.open = function () {
            if (socket && socket.readyState === 1) {
                return;
            }

            socket = new WebSocket(url);

            socket.onopen = function (event) {
                if (reconnectAttempts == null) {
                    onopen(channel);
                }

                reconnectAttempts = 0;
            };

            socket.onmessage = function (event) {
                const message = JSON.parse(event.data).data;
                onmessage(message, channel, event);
                const functions = behaviors[message];

                if (functions && functions.length) {
                    for (let i = 0; i < functions.length; i++) {
                        functions[i]();
                    }
                }
            };

            socket.onclose = function (event) {
                if (!socket
                    || (event.code === 1000 && event.reason === REASON_EXPIRED)
                    || (event.code === 1008 || event.reason === REASON_UNKNOWN_CHANNEL) // Older IE versions incorrectly return 1005 instead of 1008, hence the fallback check on the message.
                    || (reconnectAttempts == null)
                    || (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS)) {
                    onclose(event.code, channel, event);
                }
                else {
                    onerror(event.code, channel, event);
                    setTimeout(self.open, RECONNECT_INTERVAL * reconnectAttempts++);
                }
            };
        };

        self.close = function () {
            if (socket) {
                const s = socket;
                socket = null;
                reconnectAttempts = null;
                s.close();
            }
        };

    }

    self.init = function (clientId: string, url: string, channel: string,
        onopen: Function, onmessage: Function, onerror: Function, onclose: Function,
        behaviors: any, autoconnect: boolean) {
        onclose = resolveFunction(onclose);

        if (!window.WebSocket) {
            onclose(-1, clientId);
            return;
        }

        if (!sockets[clientId]) {
            sockets[clientId] = new (ReconnectingWebsocket as any)(url, channel,
                resolveFunction(onopen), resolveFunction(onmessage), resolveFunction(onerror),
                onclose, behaviors);
        }

        if (autoconnect) {
            self.open(clientId);
        }
    };

    self.open = function (clientId: string) {
        getSocket(clientId).open();
    };

    self.close = function (clientId: string) {
        getSocket(clientId).close();
    };

    /**
     * If given function is actually not a function, then try to interpret it as name of a global function.
     * If it still doesn't resolve to anything, then return a NOOP function.
     */
    function resolveFunction(fn: any) {
        return (typeof fn !== "function") && (fn = window[fn] || function () { }), fn;
    }

    /**
     * Get socket associated with given client identifier.
     * @throws {Error} When client identifier is unknown.
     */
    function getSocket(clientId: string) {
        const socket = sockets[clientId];
        if (socket) return socket;
        else throw new Error("Unknown clientId: " + clientId);
    }

    return self;

})(window as WindowAsDict);
