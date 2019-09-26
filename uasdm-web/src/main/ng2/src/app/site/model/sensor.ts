export class Sensor {
    newInstance: boolean;
    oid: string;
    name: string;
    displayLabel: string;
    sensorType: string;
    model: string;
    waveLength: string[];
}

export const WAVELENGTHS: string[] = ["Natural Color RGB", "Thermal", "Red Edge", "Near Infra Red", "LiDAR", "Other"];
