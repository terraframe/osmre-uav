///
///
///

import { Injectable } from '@angular/core';

import { SiteEntity } from '../model/management';
import { Metadata } from '../model/metadata';



@Injectable({ providedIn: 'root' })
export class MetadataService {

    private cache: any = {};

    constructor() {
        // ( root, leaf, drillable, expandable, uploadable, processable )
        this.cache['Site'] = new Metadata('Site', true, false, true, false, false, false, 'Projects');
        this.cache['Project'] = new Metadata('Project', false, false, true, false, false, false, 'Missions');
        this.cache['Mission'] = new Metadata('Mission', false, false, false, true, false, false, 'Collections');
        this.cache['Collection'] = new Metadata('Collection', false, true, false, false, false, true, 'Images');
        this.cache['folder'] = new Metadata('Folder', false, true, false, false, true, false, '');

        // Metadata for specific folder types
        this.cache['raw'] = new Metadata('Raw', false, false, false, false, true, false, '');
        this.cache['accessible'] = new Metadata('Accessible', false, false, false, false, true, false, '');
    }

    getMetadata(entity: SiteEntity): Metadata {
        return this.cache[entity.type];
    }

    getTypeContainsFolders(entity: SiteEntity): boolean {
        return (entity.type === 'Collection');
    }

    isUploadable(type: string): boolean {
        if (this.cache[type] != null) {
            return this.cache[type].uploadable;
        }

        return false;
    }

    hasExtraField(type: string, fieldName: string): boolean {
        if (type === 'Collection') {
            return (fieldName === 'collectionDate'
                || fieldName === 'collectionEndDate'
                || fieldName === 'northBound'
                || fieldName === 'southBound'
                || fieldName === 'eastBound'
                || fieldName === 'westBound'
                || fieldName === 'exifIncluded'
                || fieldName === 'acquisitionDateStart'
                || fieldName === 'acquisitionDateEnd'
                || fieldName === 'flyingHeight'
                || fieldName === 'numberOfFlights'
                || fieldName === 'percentEndLap'
                || fieldName === 'percentSideLap'
                || fieldName === 'areaCovered'
                || fieldName === 'weatherConditions'
                || fieldName === 'isPrivate'
                || fieldName === 'hasPIIConcern'
                || fieldName === 'sensor'
                || fieldName === 'uav');
        }
        else if (type === 'Project') {
            return (fieldName === 'shortName' || fieldName === 'restricted' || fieldName === 'sunsetDate' || fieldName == 'projectType');
        }
        else if (type === 'Mission') {
            return (fieldName === 'contractingOffice' || fieldName === 'vendor');
        }

        return false;
    }

    isProcessable(type: string): boolean {
        if (this.cache[type] != null) {
            return this.cache[type].processable;
        }

        return false;
    }

    getHierarchy(): string[] {

        return ['Site', 'Project', 'Mission', 'Collection'];
    }
}