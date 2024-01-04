///
///
///

import { LocalizedValue } from "@shared/model/organization";

export class UAV {
    oid: string;
    serialNumber: string;
    faaNumber: string;
    description?: string;
    organization?: { code: string, label: LocalizedValue };
    platform: string;
}

export class MetadataOptions {
    oid: string;
    organization?: { code: string, label: LocalizedValue };
    platform: string;
    platformType: string;
    serialNumber: string;
    faaNumber: string;
    sensors: {
        oid: string,
        name: string
    }[];
    pointOfContact: {
        name: string,
        email: string
    };
}

export class MetadataResponse {
    name: string;
    email: string;
    uav: any;
    sensor: any;
    northBound?: number;
    southBound?: number;
    eastBound?: number;
    westBound?: number;
    exifIncluded?: boolean;
    acquisitionDateStart?: string;
    acquisitionDateEnd?: string;
    flyingHeight?: number;
    numberOfFlights?: number;
    percentEndLap?: number;
    percentSideLap?: number;
    areaCovered?: number;
    weatherConditions?: string;
    artifacts?: any[];
}

