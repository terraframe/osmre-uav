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
    highResolution: boolean;
    wavelengths: string[];
    pixelSizeWidth: number;
    pixelSizeHeight: number;
    sensorWidth: number;
    sensorHeight: number;
    focalLength: number;
    collectionFormats: CollectionFormat[] = [];
    platforms: {
        oid: string;
        name: string;
    }[];

}

export class SensorType {
    name: string;
    oid: string;
}

export const WAVELENGTHS: string[] = ["Natural Color RGB", "Thermal", "Red Edge", "Near Infra Red", "LiDAR", "Other"];

export interface CollectionFormatMetadata { label: string, value: string }
export const COLLECTION_FORMATS: CollectionFormatMetadata[] = [
    { label: "Still Imagery", value: "STILL_IMAGERY_RGB" },
    { label: "Still thermal imagery (non-radiometric)", value: "STILL_THERMAL_RGB" },
    { label: "Still thermal imagery (radiometric)", value: "STILL_RADIOMETRIC" },
    { label: "Multispectral Still Imagery (non-radiometric)", value: "STILL_MULTISPECTRAL" },
    { label: "Video", value: "VIDEO_RGB" },
    { label: "Thermal video (non-radiometric)", value: "VIDEO_THERMAL_RGB" },
    { label: "Thermal video (radiometric)", value: "VIDEO_RADIOMETRIC" },
    { label: "Multispectral Video", value: "VIDEO_MULTISPECTRAL" },
    { label: "LIDAR", value: "LIDAR" }
] as const;
export type CollectionFormat = typeof COLLECTION_FORMATS[number]["value"];

