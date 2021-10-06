declare module 'can-ndjson-stream' {
    export default function ndjsonStream<T>(data: unknown): {
        getReader: () => {
            read: () => Promise<{
                done: boolean;
                value: T;
            }>;
        };
        cancel: () => void;
    }
}