///
///
///

export class Sensor {
    seq?: string;
    oid: string;
    name: string;
    description?: string;
    model?: string;
    dateCreated?: string;
    dateUpdate?: string;
    sensorTypeOid: string;
    sensorType: SensorType;
    hasGeologger: boolean;
    wavelengths: string[];
    pixelSizeWidth: number;
    pixelSizeHeight: number;
    sensorWidth: number;
    sensorHeight: number;
    focalLength: number;
    platforms: {
        oid: string;
        name: string;
    }[];

}

export class SensorType {
    name: string;
    isMultispectral: boolean;
    oid: string;
}

export const WAVELENGTHS: string[] = ["Natural Color RGB", "Thermal", "Red Edge", "Near Infra Red", "LiDAR", "Other"];
