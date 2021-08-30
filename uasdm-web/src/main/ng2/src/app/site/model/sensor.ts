export class Sensor {
    seq?: string;
    oid: string;
    name: string;
    description?: string;
    model?: string;
    dateCreated?: string;
    dateUpdate?: string;
    sensorType: string;
    wavelengths: string[];
    pixelSizeWidth: number;
    pixelSizeHeight: number;
    sensorWidth: number;
    sensorHeight: number;
    platforms: {
        oid: string;
        name: string;
    }[];

}