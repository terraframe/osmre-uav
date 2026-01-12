///
///
///

import { Component, Input, Output, EventEmitter, SimpleChanges, OnDestroy } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { ImagePreviewModalComponent } from '../modal/image-preview-modal.component';

import { CollectionRawSetView, Filter, RawSet, ProductCriteria, SELECTION_TYPE, ViewerSelection, ProductDocument } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';

import {
    fadeInOnEnterAnimation,
    fadeOutOnLeaveAnimation,
    bounceInOnEnterAnimation,
    bounceOutOnLeaveAnimation
} from 'angular-animations';
import { ConfigurationService } from '@core/service/configuration.service';
import EnvironmentUtil from '@core/utility/environment-util';
import { AuthService } from '@shared/service/auth.service';
import { LocalizedValue } from '@shared/model/organization';
import { RawSetService } from '@site/service/raw-set.service';
import { ModalTypes } from '@shared/model/modal';

@Component({
    standalone: false,
    selector: 'raw-set-panel',
    templateUrl: './raw-set-panel.component.html',
    styleUrl: './raw-set-panel.component.scss',
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation(),
        bounceInOnEnterAnimation(),
        bounceOutOnLeaveAnimation()
    ]
})
export class RawSetPanelComponent implements OnDestroy {

    @Input() selection: ViewerSelection;

    @Input() filter: Filter;

    @Input() organization?: { code: string, label: LocalizedValue };

    @Output() toggleMapSet = new EventEmitter<RawSet>();

    @Output() onViewsChange = new EventEmitter<CollectionRawSetView[]>();


    /* 
     * List of rawSets for the current node
     */
    @Input() views: CollectionRawSetView[] = [];

    thumbnails: any = {};

    loading: boolean = false;

    requestId: number = 0;

    context: string;
    isAdmin: boolean = false;


    constructor(private configuration: ConfigurationService,
        private pService: RawSetService,
        private mService: ManagementService,
        private modalService: BsModalService,
        private authService: AuthService) {

        this.context = EnvironmentUtil.getApiUrl();
        this.isAdmin = this.authService.isAdmin();
    }

    ngOnDestroy(): void {
        this.onViewsChange.emit([]);
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.selection != null) {
            this.refreshRawSets(changes.selection.currentValue);
        }
    }


    refresh(): void {
        this.refreshRawSets(this.selection);
    }

    refreshRawSets(selection: ViewerSelection): void {
        this.onViewsChange.emit([]);
        this.thumbnails = {};

        this.loading = true;

        const original = ++this.requestId;

        const criteria: ProductCriteria = {
            type: selection.type,
            sortField: "name",
            sortOrder: 'ASC'
        }

        if (criteria.type === SELECTION_TYPE.SITE) {
            criteria.id = selection.data.id;
        }

        let conditions = [];

        conditions.push({ field: 'organization', value: this.organization });
        conditions.push({ field: 'collectionDate', value: this.filter.collectionDate });
        conditions.push({ field: 'owner', value: this.filter.owner });
        conditions.push({ field: 'platform', value: this.filter.platform });
        conditions.push({ field: 'projectType', value: this.filter.projectType });
        conditions.push({ field: 'sensor', value: this.filter.sensor });
        conditions.push({ field: 'uav', value: this.filter.uav });
        conditions = conditions.filter(c => {
            if (c.field === 'organization') {
                return c.value != null && c.value.code.length > 0
            }

            return c.value != null && c.value.length > 0
        });

        if (criteria.type === SELECTION_TYPE.LOCATION) {
            criteria.uid = selection.data.properties.uid;
            criteria.hierarchy = selection.hierarchy;
            criteria.conditions = conditions;
        }

        this.pService.getRawSets(criteria).then(views => {
            if (original === this.requestId) {

                this.loading = false;

                views.forEach(view => {
                    // view.rawSet = view.sets[0];
                    // view.rawSetId = view.rawSet.id;

                    // this.getThumbnail(view.rawSet);
                });

                this.onViewsChange.emit(views);
            }
        });
    }

    setDocument(view: CollectionRawSetView, set: RawSet): void {

        set.document = set.documents.find(p => p.id === set.documentId);

        this.getThumbnail(view, set);
    }


    createImageFromBlob(image: Blob, document: ProductDocument) {
        let reader = new FileReader();
        reader.addEventListener("load", () => {
            // this.imageToShow = reader.result;
            this.thumbnails[document.id] = reader.result;
        }, false);

        if (image) {
            reader.readAsDataURL(image);
        }
    }

    getThumbnail(view: CollectionRawSetView, set: RawSet): void {

        // imageKey only exists if an image actually exists on s3
        if (set.document.key != null) {
            const component: string = view.componentId;
            const rootPath: string = set.document.key.substr(0, set.document.key.lastIndexOf("/"));
            const fileName: string = /[^/]*$/.exec(set.document.key)[0];
            const lastPeriod: number = fileName.lastIndexOf(".");
            const thumbKey: string = rootPath + "/thumbnails/" + fileName.substr(0, lastPeriod) + ".png";

            this.mService.download(component, thumbKey, false).subscribe(blob => {
                this.createImageFromBlob(blob, set.document);
            }, error => {
                console.log(error);

                this.thumbnails[set.document.id] = this.context + 'assets/thumbnail-default.png';

            });
        }
        else {
            this.thumbnails[set.document.id] = this.context + 'assets/thumbnail-default.png';
        }
    }

    getDefaultImgURL(event: any): void {
        event.target.src = this.context + 'assets/thumbnail-default.png';
    }

    handleMapIt(rawSet: RawSet): void {
        this.toggleMapSet.emit(rawSet);
    }


    handleDelete(view: CollectionRawSetView, set: RawSet, event: any): void {

        event.stopPropagation();

        const bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: 'modal-xl'
        });
        bsModalRef.content.message = 'Are you sure you want to delete [' + set.name + ']?';
        bsModalRef.content.data = set;
        bsModalRef.content.type = ModalTypes.danger;
        bsModalRef.content.submitText = 'Delete';

        bsModalRef.content.onConfirm.subscribe(data => {
            const rawSetId = set.id;

            this.pService.remove(rawSetId).then(response => {
                view.sets = view.sets.filter((n: RawSet) => n.id !== rawSetId);

                this.onViewsChange.emit(
                    this.views.filter((n: CollectionRawSetView) => n.componentId !== view.componentId)
                );
            });
        });
    }

    previewImage(document: ProductDocument): void {

        if (document.key != null) {

            // const component: string = rawSet.entities[rawSet.entities.length - 1].id;

            // const bsModalRef = this.modalService.show(ImagePreviewModalComponent, {
            //     animated: true,
            //     backdrop: true,
            //     ignoreBackdropClick: false,
            //     'class': 'image-preview-modal modal-xl'
            // });

            // bsModalRef.content.initRawSet(document.id, rawSet.rawSetName);
        }
    }


    handleTogglePublish(rawSet: RawSet): void {
        this.pService.togglePublish(rawSet.id).then(p => {
            const mapIt: boolean = rawSet.mapped;

            if (mapIt) {
                this.toggleMapSet.emit(rawSet);
            }

            rawSet.published = p.published;

            if (mapIt) {
                this.toggleMapSet.emit(rawSet);
            }
        });
    }

    handleToggleLock(rawSet: RawSet): void {
        this.pService.toggleLock(rawSet.id).then(() => {
            rawSet.locked = !rawSet.locked;
        });
    }

}
