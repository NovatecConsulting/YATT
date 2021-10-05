import ndjsonStream from "can-ndjson-stream";

const baseUrl = 'http://localhost:8080/v2';

export async function subscriptionQuery<InitialResultType, UpdateResultType>(
    path: string,
    onFirstResult: (initial: InitialResultType) => void,
    onUpdate: (update: UpdateResultType) => void
) {
    const response = await fetch(`${baseUrl}/${path}`, {
        headers: new Headers({
            'Accept': 'application/x-ndjson',
        })
    });
    if (response.body) {
        const reader = ndjsonStream(response.body).getReader();
        const {value, done} = await reader.read();
        if (!done) {
            onFirstResult(value as InitialResultType);
            while (true) {
                const {value, done} = await reader.read();
                if (done) break;
                onUpdate(value as UpdateResultType);
            }
        }
    } else {
        throw 'no response body';
    }
}