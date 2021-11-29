
export class Classification {
    oid: string;
    name: string;
    seq?: number;
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