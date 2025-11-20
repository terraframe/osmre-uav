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
  { label: "Still Imagery (RGB)",            value: "STILL_IMAGERY_RGB" },
  { label: "Still Thermal Imagery (RGB)",    value: "STILL_THERMAL_RGB" },
  { label: "Still Radiometric Imagery",      value: "STILL_RADIOMETRIC" },
  { label: "Still Multrispectral Imagery",   value: "STILL_MULTISPECTRAL" },
  { label: "Video (RGB)",                    value: "VIDEO_RGB" },
  { label: "Video Thermal (RGB)",            value: "VIDEO_THERMAL_RGB" },
  { label: "Video Radiometric",              value: "VIDEO_RADIOMETRIC" },
  { label: "Video Multispectral",            value: "VIDEO_MULTISPECTRAL" },
  { label: "LIDAR",                          value: "LIDAR" }
] as const;
export type CollectionFormat = typeof COLLECTION_FORMATS[number]["value"];

