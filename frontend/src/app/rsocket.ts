import {
    RSocketClient,
    JsonSerializer,
    BufferEncoders,
    UTF8Encoder,
    IdentitySerializer,
    encodeCompositeMetadata,
    MESSAGE_RSOCKET_AUTHENTICATION,
    encodeBearerAuthMetadata,
    MESSAGE_RSOCKET_ROUTING,
    encodeRoute
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {
    ReactiveSocket, Payload, ISubscriber, ISubscription, ConnectionStatus
} from 'rsocket-types'
import {MESSAGE_RSOCKET_COMPOSITE_METADATA} from "rsocket-core";
import {store} from "./store";
import {selectToken} from "../features/auth/authSlice";
import {Flowable, Single} from "rsocket-flowable";


class ResubscribeOperator<T> implements ISubscriber<T>, ISubscription {
    source: Flowable<T>;
    actualSubscriber: ISubscriber<T>;

    done: boolean;
    once: boolean;

    subscription: ISubscription | null;

    requested: number;

    constructor(source: Flowable<T>, actualSubscriber: ISubscriber<T>) {
        this.source = source;
        this.actualSubscriber = actualSubscriber;
        this.done = false;
        this.once = false;
        this.subscription = null;
        this.requested = 0;
    }

    onSubscribe(subscription: ISubscription) {
        if (this.done) {
            subscription.cancel();
            return;
        }

        this.subscription = subscription;

        if (!this.once) {
            this.once = true;
            this.actualSubscriber.onSubscribe(this);
            return;
        }

        subscription.request(this.requested);
    }

    onComplete() {
        if (this.done) {
            return;
        }

        this.done = true;
        this.actualSubscriber.onComplete();
    }

    onError(error: Error) {
        if (this.done) {
            return;
        }

        this.subscription = null;
        setTimeout(() => this.source.subscribe(this));
    }

    onNext(value: T) {
        if (this.done) {
            return;
        }

        this.requested--;
        this.actualSubscriber.onNext(value);
    }

    cancel() {
        if (this.done) {
            return;
        }

        this.done = true;

        if (this.subscription) {
            this.subscription.cancel();
            this.subscription = null;
        }
    }

    request(n: number) {
        this.requested += n;
        if (this.subscription) {
            this.subscription.request(n);
        }
    }
}

class ReconnectableRSocket<D, M> implements ReactiveSocket<D, M> {

    socket: ReactiveSocket<D, M> | null = null;
    clientFactory: () => RSocketClient<D, M>;

    constructor(clientFactory: () => RSocketClient<D, M>) {
        this.clientFactory = clientFactory;
        this.connect();
    }

    connect(): Promise<void> {
        return new Promise((resolve, reject) => {
            this.clientFactory().connect().then(
                socket => {
                    resolve();
                    this.socket = socket;
                    socket.connectionStatus().subscribe(event => {
                        if (event.kind !== 'CONNECTED') {
                            this.socket = null;
                            this.connect();
                        }
                    });
                },
                error => {
                    reject(error);
                    this.connect();
                }
            );
        });
    }

    fireAndForget(payload: Payload<D, M>): void {
        if (!this.socket) {
            throw new Error('Not Connected yet. Retry later');
        }

        this.socket.fireAndForget(payload);
    }

    requestResponse(payload: Payload<D, M>): Single<Payload<D, M>> {
        if (!this.socket) {
            return Single.error(new Error('Not Connected yet. Retry later'));
        }

        return this.socket.requestResponse(payload);
    }

    requestStream(payload: Payload<D, M>): Flowable<Payload<D, M>> {
        if (!this.socket) {
            return Flowable.error(new Error('Not Connected yet. Retry later'));
        }

        return this.socket.requestStream(payload);
    }

    requestChannel(payloads: Flowable<Payload<D, M>>): Flowable<Payload<D, M>> {
        if (!this.socket) {
            return Flowable.error(new Error('Not Connected yet. Retry later'));
        }

        return this.socket.requestChannel(payloads);
    }

    metadataPush(payload: Payload<D, M>): Single<void> {
        if (!this.socket) {
            return Single.error(new Error('Not Connected yet. Retry later'));
        }

        return this.socket.metadataPush(payload);
    }

    close(): void {
        if (!this.socket) {
            throw new Error('Not Connected yet. Retry later');
        }

        this.socket.close();
    }

    connectionStatus(): Flowable<ConnectionStatus> {
        if (!this.socket) {
            throw new Error('Not Connected yet. Retry later');
        }

        return this.socket.connectionStatus();
    }

    availability(): number {
        if (!this.socket) {
            throw new Error('Not Connected yet. Retry later');
        }

        return this.socket.availability();
    }
}

const clientFactory = () => new RSocketClient({
    serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
    },
    setup: {
        keepAlive: 60 * 1000,
        lifetime: 180 * 1000,
        dataMimeType: "application/json",
        metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
        payload: {
            metadata: encodeCompositeMetadata([
                [
                    MESSAGE_RSOCKET_AUTHENTICATION,
                    encodeBearerAuthMetadata(selectToken(store.getState()) ?? ""),
                ],
            ])
        }
    },
    transport: new RSocketWebSocketClient(
        {
            url: 'ws://localhost:7000',
            wsCreator: (url: string) => new WebSocket(url),
        },
        {
            ...BufferEncoders,
            data: UTF8Encoder,
        }
    ),
})

class WebsocketClient {
    _rsocket: ReconnectableRSocket<any, any>

    constructor(rsocket: ReconnectableRSocket<any, any>) {
        this._rsocket = rsocket
    }

    connect(): Promise<void> {
        return this._rsocket.connect();
    }

    sendCommand(route: string, command: any): Promise<{ data: void } | { error: any }> {
        return new Promise((resolve, reject) => {
            this._rsocket.requestResponse({
                data: command,
                metadata: encodeCompositeMetadata([
                    [MESSAGE_RSOCKET_ROUTING, encodeRoute(route)]
                ])
            }).then(_ => resolve({data: undefined}), error => reject({error: error}));
        });
    }

    subscribeUpdates<T>(route: string, onUpdate: (update: T) => void): Subscription {
        let subscription: ISubscription | undefined;

        const request = new Flowable<Payload<T, any>>(subscriber => {
            this._rsocket.requestStream({
                metadata: encodeCompositeMetadata([
                    [MESSAGE_RSOCKET_ROUTING, encodeRoute(route)]
                ])
            }).subscribe(subscriber);
        });

        request
            .lift<Payload<T, any>>(subscriber => new ResubscribeOperator<Payload<T, any>>(request, subscriber))
            .subscribe({
                onNext(payload) {
                    const update = payload.data;
                    onUpdate(update!);
                },
                onSubscribe(sub) {
                    subscription = sub;
                    sub.request(2147483647)
                }
            });
        return {
            cancel() {
                subscription?.cancel();
            }
        }
    }
}

export interface Subscription {
    cancel: () => void;
}

export let rsocket = new WebsocketClient(new ReconnectableRSocket(clientFactory))
