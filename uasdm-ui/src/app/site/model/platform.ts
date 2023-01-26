export class Platform {
    oid: string;
    name: string;
    label: string;
    description?: string;
    dateCreated?: string;
    dateUpdate?: string;
    platformTypeOid?: string;
    platformType?: PlatformType;
    manufacturer: string;
    sensors: string[];
}

export class PlatformType {
    name: string;
    oid: string;
}