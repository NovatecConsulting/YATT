declare module 'can-ndjson-stream' {
    export type CancelCallback = (reason?: string) => Promise<string | undefined>
    export type  Result<T> = {
        done: boolean;
        value: T | undefined;
    };

    export default function ndjsonStream<T>(data: ReadableStream<Uint8Array>): {
        getReader: () => {
            read: () => Promise<Result<T>>;
            releaseLock: () => void;
            cancel: CancelCallback;
        };
        cancel: CancelCallback;
    }
}