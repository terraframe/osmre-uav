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
    sensors: {
        oid: string,
        name: string
    }[];
}
