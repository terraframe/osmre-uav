export interface UploadProgress {
    bytesUploaded: number
    bytesTotal: number
    percentage: number
}

export interface UploadMetadata {
    componentId: string;
    uploadTarget: string;
    type: string;
    filename?: string;
    filetype?: string;

    // Metadata for standalone uploads
    productName?: string;
    processOrtho?: boolean;
    processPtcloud?: boolean;
    processDem?: boolean;
    orthoCorrectionModel?: string;
    description?: string;
    tool?: string;
    projectionName?: string;
    ptEpsg?: number;

}