///
///
///

export class UAV {
    oid: string;
    serialNumber: string;
    faaNumber: string;
    description?: string;
    bureau: string;
    platform: string;
}

export class MetadataOptions {
    oid: string;
    bureau: string;
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
	focalLength?: number;
	flyingHeight?: number;
	numberOfFlights?: number;
	percentEndLap?: number;
	percentSideLap?: number;
	areaCovered?: number;
	weatherConditions?: string;
}

