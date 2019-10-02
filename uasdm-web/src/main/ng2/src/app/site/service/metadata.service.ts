import { Injectable } from '@angular/core';

import { SiteEntity } from '../model/management';
import { Metadata } from '../model/metadata';

declare var acp: any;

@Injectable()
export class MetadataService {

    private cache: any = {};

    constructor() {
        // ( root, leaf, drillable, expandable, uploadable, processable )
        this.cache['Site'] = new Metadata( true, false, true, false, false, false );
        this.cache['Project'] = new Metadata( false, false, true, false, false, false );
        this.cache['Mission'] = new Metadata( false, false, false, true, false, false );
        this.cache['Collection'] = new Metadata( false, true, false, false, false, true );
        this.cache['folder'] = new Metadata( false, true, false, false, true, false );

        // Metadata for specific folder types
        this.cache['raw'] = new Metadata( false, false, false, false, true, false );
        this.cache['accessible'] = new Metadata( false, false, false, false, true, false );
    }

    getMetadata( entity: SiteEntity ): Metadata {
        return this.cache[entity.type];
    }

    getTypeContainsFolders( entity: SiteEntity ): boolean {
        return ( entity.type === 'Collection' );
    }

    isUploadable( type: string ): boolean {
        if ( this.cache[type] != null ) {
            return this.cache[type].uploadable;
        }

        return false;
    }

    isProcessable( type: string ): boolean {
        if ( this.cache[type] != null ) {
            return this.cache[type].processable;
        }

        return false;
    }

    getUploadTarget( type: string ): string {
        if ( type === 'Imagery' ) {
            return 'imagery';
        }

        return 'raw';
    }

    getHierarchy(): string[] {

        return ['Site', 'Project', 'Mission', 'Collection'];
    }
}