import {store} from "./store";
import {Client} from "@stomp/stompjs";
import {messageCallbackType} from "@stomp/stompjs/esm6/types";
import {StompHeaders} from "@stomp/stompjs/esm6/stomp-headers";
import {StompSubscription} from "@stomp/stompjs/esm6/stomp-subscription";
import {selectToken} from "../features/auth/authSlice";

class WebsocketClient {
    _stompClient = undefined as Client | undefined;

    connect(): Promise<void> {
        return new Promise((resolve, reject) => {
            let isPromiseFinished = false;
            this._stompClient = new Client({
                brokerURL: "ws://localhost:8085/stomp",
                beforeConnect() {
                    this.connectHeaders = {
                        "access-token": selectToken(store.getState()) ?? ""
                    };
                },
                onConnect: (frame) => {
                    console.log("connected")
                    console.log(frame)
                    if (!isPromiseFinished) {
                        isPromiseFinished = true;
                        resolve();
                    }

                    for (let sub of Subscription._allSubscriptions) {
                        sub.resubscribe();
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
    }

    subscribe(destination: string, callback: messageCallbackType, headers?: StompHeaders) {
        if (this._stompClient) {
            destination = `/user/topic${destination}`;
            const sub = this._stompClient.subscribe(destination, callback, headers)
            return new Subscription(sub, this._stompClient, destination, callback, headers);
        } else {
            console.log("stomp client undefined");
            throw Error("stomp client undefined")
        }
    }
}

export class Subscription {
    static _allSubscriptions = [] as Subscription[];
    _subscription: StompSubscription;
    _stompClient: Client;
    _destination: string;
    _callback: messageCallbackType;
    _headers?: StompHeaders

    constructor(subscription: StompSubscription, stompClient: Client, destination: string, callback: messageCallbackType, headers?: StompHeaders) {
        Subscription._allSubscriptions.push(this);
        this._subscription = subscription;
        this._stompClient = stompClient;
        this._destination = destination;
        this._callback = callback;
        this._headers = headers;
    }

    unsubscribe(headers?: StompHeaders) {
        const index = Subscription._allSubscriptions.indexOf(this, 0)
        if (index > -1) {
            Subscription._allSubscriptions.splice(index, 1)
        }
        this._subscription.unsubscribe(headers)
    }

    resubscribe() {
        // TODO updates while not connected are lost
        this._subscription = this._stompClient.subscribe(this._destination, this._callback, this._headers)
    }
}

export const websocketClient = new WebsocketClient();
