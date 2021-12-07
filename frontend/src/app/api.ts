import {store} from "./store";
import {Client} from "@stomp/stompjs";
import {messageCallbackType} from "@stomp/stompjs/esm6/types";
import {StompHeaders} from "@stomp/stompjs/esm6/stomp-headers";

export const websocketClient = {
    _stompClient: undefined as Client | undefined,
    connect(): Promise<void> {
        return new Promise((resolve, reject) => {
            let isPromiseFinished = false;
            this._stompClient = new Client({
                brokerURL: "ws://localhost:8085/stomp",
                connectHeaders: {
                    "access-token": store.getState().auth.token!!
                },
                onConnect: (frame) => {
                    console.log("connected")
                    console.log(frame)
                    if (!isPromiseFinished) {
                        isPromiseFinished = true;
                        resolve();
                    }
                },
                onDisconnect: (frame) => {
                    console.log("disconnected")
                    console.log(frame)
                },
                onWebSocketClose: (event) => {
                    console.log("websocket closed")
                    console.log(event)
                    if (!isPromiseFinished) {
                        isPromiseFinished = true;
                        reject();
                    }
                },
                onWebSocketError: (event) => {
                    console.log("websocket error")
                    console.log(event)
                    if (!isPromiseFinished) {
                        isPromiseFinished = true;
                        reject();
                    }
                }
            });
            this._stompClient.activate();
        });
    },
    subscribe(destination: string, callback: messageCallbackType, headers?: StompHeaders) {
        if (this._stompClient) {
            return this._stompClient.subscribe(`/user/topic${destination}`, callback, headers)
        } else {
            console.log("stomp client undefined");
            throw Error("stomp client undefined")
        }
    }
}