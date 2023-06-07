///
export class LPGSync {
    oid?: string;
    url: string;
    remoteType: string;
    displayLabel: any;
    remoteEntry: string;
    forDate: string;
    remoteVersion: string;
    versionNumber: number;
}

export class LabeledPropertyGraphTypeVersion {

    oid?: string;
    displayLabel: string;
    entry: string;
    graphType: string;
    forDate: string;
    createDate: string;
    publishDate: string;

    locales?: string[];
    refreshProgress?: any;
    working: boolean;
    isMember?: boolean;
    versionNumber: number;
    collapsed?: boolean;
    period?: {
        type: string,
        value: any
    };
    label?: string;
}

export class LabeledPropertyGraphTypeEntry {

    displayLabel: string;
    oid: string;
    graphType: string;
    forDate: string;
    period?: {
        type: string,
        value: any
    };

    versions?: LabeledPropertyGraphTypeVersion[];
    showAll?: boolean;

}


export class LabeledPropertyGraphType {

    oid?: string;
    code: string;
    displayLabel: any;
    description: any;
    hierarchy: string;
    strategyType?: string;
    strategyConfiguration?: any;
    graphType: string;

    // Attributes for the subtypes
    validOn?: string;
    publishingStartDate?: string;
    frequency?: string;
    intervalJson?: { startDate: string, endDate: string, readonly?: string, oid?: string }[]

    entries?: LabeledPropertyGraphTypeEntry[];
}
