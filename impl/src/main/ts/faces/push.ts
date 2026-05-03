/**
 * Implementation of the `faces.push` namespace from the Jakarta Faces JavaScript API.
 * @see api/.../faces.d.ts namespace `faces.push`
 */

import type { faces as FacesSpec } from "../../../../../faces/api/src/main/resources/META-INF/resources/jakarta.faces/faces";
import type { WindowAsDict } from "./dom";

type OnOpen = FacesSpec.push.OnOpenHandler;
type OnMessage = FacesSpec.push.OnMessageHandler;
type OnError = FacesSpec.push.OnErrorHandler;
type OnClose = FacesSpec.push.OnCloseHandler;
type Behaviors = Record<string, Array<() => void>>;

interface ReconnectingSocket {
    open(): void;
    close(): void;
}

export const push: typeof FacesSpec.push = (function (window: WindowAsDict) {

    const RECONNECT_INTERVAL = 500;
    const MAX_RECONNECT_ATTEMPTS = 25;
    const REASON_EXPIRED = "Expired";

    const sockets: { [clientId: string]: ReconnectingSocket } = {};

    /**
     * Reconnecting websocket. Reconnects on timeout with cumulative intervals of 500ms,
     * up to 25 attempts (~3 minutes). The `onclose` function is called with the error code
     * of the last attempt.
     */
    function ReconnectingWebsocket(
        this: ReconnectingSocket,
        url: string,
        channel: string,
        onopen: OnOpen,
        onmessage: OnMessage,
        onerror: OnError,
        onclose: OnClose,
        behaviors: Behaviors,
    ) {

        let socket: WebSocket | null = null;
        let reconnectAttempts: number | null = null;
        const self = this;

        self.open = function () {
            if (socket && socket.readyState === WebSocket.OPEN) {
                return;
            }

            socket = new WebSocket(url);

            socket.onopen = function (_event) {
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
                    || event.code === 1008
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

    /** If `fn` is not a function, look it up by name on `window`; otherwise return `fn`. NOOP fallback. */
    function resolveFunction<F extends Function>(fn: F | string | null): F {
        if (typeof fn === "function") {
            return fn;
        }
        const named = typeof fn === "string" ? window[fn] : undefined;
        return (typeof named === "function" ? named : function () { /* NOOP */ }) as F;
    }

    /**
     * Get socket associated with given client identifier.
     * @throws {Error} When client identifier is unknown.
     */
    function getSocket(clientId: string): ReconnectingSocket {
        const socket = sockets[clientId];
        if (socket) return socket;
        else throw new Error("Unknown clientId: " + clientId);
    }

    return {
        init(clientId, url, channel, onopen, onmessage, onerror, onclose, behaviors, autoconnect) {
            const resolvedOnclose = resolveFunction<OnClose>(onclose);

            if (!window.WebSocket) {
                resolvedOnclose(-1, clientId, undefined as unknown as CloseEvent);
                return;
            }

            if (!sockets[clientId]) {
                sockets[clientId] = new (ReconnectingWebsocket as unknown as new (
                    url: string, channel: string,
                    onopen: OnOpen, onmessage: OnMessage, onerror: OnError, onclose: OnClose,
                    behaviors: Behaviors,
                ) => ReconnectingSocket)(
                    url, channel,
                    resolveFunction<OnOpen>(onopen),
                    resolveFunction<OnMessage>(onmessage),
                    resolveFunction<OnError>(onerror),
                    resolvedOnclose,
                    behaviors as Behaviors,
                );
            }

            if (autoconnect) {
                this.open(clientId);
            }
        },
        open(clientId) {
            getSocket(clientId).open();
        },
        close(clientId) {
            getSocket(clientId).close();
        },
    };

})(window as unknown as WindowAsDict);
