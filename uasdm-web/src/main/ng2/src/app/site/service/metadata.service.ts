import { Injectable } from '@angular/core';

import { SiteEntity } from '../model/management';
import { Metadata } from '../model/metadata';

declare var acp: any;

@Injectable()
export class MetadataService {

    private cache: any = {};

    constructor() {
        this.cache['Site'] = new Metadata( true, false, false, false );
        this.cache['Project'] = new Metadata( false, true, false, false );
        this.cache['Mission'] = new Metadata( true, false, false, false );
        this.cache['Collection'] = new Metadata( false, false, true, false );
        this.cache['folder'] = new Metadata( false, false, true, true );
        this.cache['raw'] = new Metadata( false, false, false, true );
        this.cache['ptcloud'] = new Metadata( false, false, false, false );
        this.cache['dem'] = new Metadata( false, false, false, false );
        this.cache['ortho'] = new Metadata( false, false, false, false );
    }

    getMetadata( entity: SiteEntity ): Metadata {

        return this.cache[entity.type];
    }

}