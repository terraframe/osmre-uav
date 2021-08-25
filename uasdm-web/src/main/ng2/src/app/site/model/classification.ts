
export class Classification {
    oid: string;
    code: string;
    label: string;
    seq: number;
}

export class ClassificationComponentMetadata {
    title: string;
    label: string;
    baseUrl: string;
    columns?: {
        name: string,
        label: string,
        type: string
    }[]
}