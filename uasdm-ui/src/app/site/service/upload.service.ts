import { Injectable } from '@angular/core'
import { UploadProgress } from '@site/model/upload'
import { Upload, UploadOptions } from 'tus-js-client'
import { WebStorageUrlStorage } from 'tus-js-client/lib/browser/urlStorage.js'



@Injectable({
    providedIn: 'root',
})
export class UploadService {
    private upload: Upload | null = null

    private storage = new WebStorageUrlStorage();


    findAllUploads(): Promise<any[]> {
        return this.storage.findAllUploads();
    }

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
            onShouldRetry: function (err, retryAttempt, options) {
                const status = err.originalResponse ? err.originalResponse.getStatus() : 0;
                if (status === 403) {
                    return false;
                }
                return retryAttempt < 1000;
            },
            onSuccess: () => {
                onSuccess()
            },
            storeFingerprintForResuming: true,
            removeFingerprintOnSuccess: true,
        }

        this.upload = new Upload(file, options)
        this.upload.findPreviousUploads().then((previousUploads) => {

            // Found previous uploads so we select the first one.
            if (previousUploads.length) {
                console.log('Found previous upload', previousUploads);

                this.upload.resumeFromPreviousUpload(previousUploads[0])
            }

            // Start the upload
            this.upload.start()
        })
    }

    abortUpload(): void {
        if (this.upload) {
            this.upload.abort()
        }
    }
}
