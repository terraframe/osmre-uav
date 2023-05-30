///
///
///


export class Filter {
    id: string;
    label: string;
    field: string;
    value?: any;
    startDate?: string;
    endDate?: string;
};

export class Criteria {
    should?: Filter[];
    must?: Filter[];
};

export class StacAsset {
    href: string;
    title: string;
    type: string;
    roles?: string[];
}

export class StacItem {
    stac_version: string;
    stac_extensions: string[];
    type: string;
    id: string;
    bbox?: number[];
    geometry: any;
    properties: any;
    collection?: string;
    asset?: string;
    enabled?: boolean;
    links: {
        rel: string;
        href: string;
        type: string;
        title: string;
    }[];
    assets: {
        [key: string]: StacAsset
    }
}

export class StacLayer {
    id: string;
    startDate: string = "";

    endDate: string = "";

    /* 
     * Layer name
     */
    layerName: string = "";

    /*
     * Criteria
     */
    filters: Filter[] = [];
    items?: StacItem[] = [];
    active?: boolean;
}
