import { Injectable } from '@angular/core'
import { UploadProgress } from '@site/model/upload'
import { Upload, UploadOptions } from 'tus-js-client'


@Injectable({
    providedIn: 'root',
})
export class UploadService {
    private upload: Upload | null = null

    startUpload(
        file: File,
        endpoint: string,
        entityId: string,
        uploadTarget: string,
        onProgress: (progress: UploadProgress) => void,
        onSuccess: () => void,
        onError: (error: Error) => void,
    ): void {
        const options: UploadOptions = {
            endpoint,
            retryDelays: [0, 1000, 3000, 5000],
            chunkSize: 5 * 1024 * 1024, // 5MB chunks for optimal performance
            metadata: {
                filename: file.name,
                filetype: file.type,
                entityId: entityId,
                uploadTarget: uploadTarget
            },
            onError: (error) => {
                if (error.name === 'AbortError') {
                    onError(new Error('Upload was aborted'))
                } else if (error.name === 'NetworkError') {
                    onError(new Error('Network connection lost - upload will resume automatically'))
                } else {
                    onError(error)
                }
            },
            onProgress: (bytesUploaded: number, bytesTotal: number) => {
                onProgress({
                    bytesUploaded,
                    bytesTotal,
                    percentage: (bytesUploaded / bytesTotal) * 100,
                })
            },
            onSuccess: () => {
                onSuccess()
            },
        }

        this.upload = new Upload(file, options)
        this.upload.start()
    }

    abortUpload(): void {
        if (this.upload) {
            this.upload.abort()
        }
    }
}
