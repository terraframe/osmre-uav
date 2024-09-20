///
///
///

import { BBox } from "@turf/turf";
import { MapAsset, Product } from "./management";


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

export class StacProperty {
    name: string;
    label: string;
    type: string;
    location: any;
}

export class StacAsset {
    href: string;
    title: string;
    type: string;
    roles?: string[];
}

export class StacLink {
    rel: string;
    href: string;
    type: string;
    title?: string;
    item?: StacItem;
    open?: boolean;
    bbox?: number[];
    id?: string
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
    links: StacLink[];
    assets: {
        [key: string]: StacAsset
    }
    thumbnail?: string;
}

export class StacCollection {
    stac_version: string;
    stac_extensions: string[];
    id: string;

    title?: string;
    description?: string;
    extent: {
        spatial?: {
            bbox?: number[][];
        }
        temporal?: {
            interval?: string[];
        }
    }
    links: StacLink[];
}


export enum ToggleableLayerType {
    STAC = 0, PRODUCT = 1, KNOWSTAC = 2
}

export class ToggleableLayer {
    id: string;
    type: ToggleableLayerType;
    layerName: string = "";
    active?: boolean;
    item?: any
    asset?: MapAsset;
}

export class StacLayer {
    startDate: string = "";

    endDate: string = "";

    /*
     * Criteria
     */
    filters: Filter[] = [];
    items?: StacItem[] = [];
}
