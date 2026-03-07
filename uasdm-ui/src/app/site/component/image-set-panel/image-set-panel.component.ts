///
///
///

import { Component, Input, SimpleChanges, OnDestroy, inject } from '@angular/core';
import { Location, NgClass, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, UrlTree } from '@angular/router';
import { Clipboard } from '@angular/cdk/clipboard';
import { bounceInOnEnterAnimation, bounceOutOnLeaveAnimation, fadeInOnEnterAnimation, fadeOutOnLeaveAnimation } from 'angular-animations';
import { BsModalService } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { ImagePreviewModalComponent } from '../modal/image-preview-modal.component';

import { Filter, ImageSet, ProductCriteria, SELECTION_TYPE, ViewerSelection, ProductDocument } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';

import EnvironmentUtil from '@core/utility/environment-util';
import { AuthService } from '@shared/service/auth.service';
import { LocalizedValue } from '@shared/model/organization';
import { ImageSetService } from '@site/service/image-set.service';
import { ModalTypes } from '@shared/model/modal';
import { SafeHtmlPipe } from '@shared/pipe/safe-html.pipe';
import { Store } from '@ngrx/store';
import { getImageSets, MapActions } from 'src/app/state/map.state';
import { Observable } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
    standalone: true,
    selector: 'image-set-panel',
    templateUrl: './image-set-panel.component.html',
    styleUrl: './image-set-panel.component.scss',
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation(),
        bounceInOnEnterAnimation(),
        bounceOutOnLeaveAnimation()
    ],
    imports: [FormsModule, NgFor, NgIf, NgClass, SafeHtmlPipe]
})
export class ImageSetPanelComponent implements OnDestroy {
    private store = inject(Store);

    @Input() selection: ViewerSelection;

    @Input() filter: Filter;

    @Input() organization?: { code: string, label: LocalizedValue };

    sets$: Observable<ImageSet[]> = this.store.select(getImageSets);


    /* 
     * List of imageSets for the current node
     */
    sets: ImageSet[] = [];

    thumbnails: any = {};

    loading: boolean = false;

    requestId: number = 0;

    context: string;
    isAdmin: boolean = false;


    constructor(
        private pService: ImageSetService,
        private mService: ManagementService,
        private modalService: BsModalService,
        private authService: AuthService,
        private location: Location,
        private clipboard: Clipboard,
        private router: Router
    ) {

        this.context = EnvironmentUtil.getApiUrl();
        this.isAdmin = this.authService.isAdmin();

        this.sets$.pipe(takeUntilDestroyed()).subscribe((sets) => this.sets = sets);
    }

    ngOnDestroy(): void {
        this.store.dispatch(MapActions.setImageSets({ sets: [] }));
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.selection != null) {
            this.refreshImageSets(changes.selection.currentValue);
        }
    }


    refresh(): void {
        this.refreshImageSets(this.selection);
    }

    refreshImageSets(selection: ViewerSelection): void {
        this.store.dispatch(MapActions.setImageSets({ sets: [] }));

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

        this.pService.getImageSets(criteria).then(views => {
            if (original === this.requestId) {

                const sets = views.flatMap(view => view.sets)

                sets.forEach(set => {
                    set.mapped = false;
                    if (set.documents.length > 0) {
                        set.document = set.documents[0];
                        set.documentId = set.document.id;

                        this.getThumbnail(set);
                    }
                })

                this.loading = false;

                this.store.dispatch(MapActions.setImageSets({ sets }));
            }
        });
    }

    setDocument(set: ImageSet): void {

        set.document = set.documents.find(p => p.id === set.documentId);

        this.getThumbnail(set);
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

    getThumbnail(set: ImageSet): void {

        // imageKey only exists if an image actually exists on s3
        if (set.document != null) {
            if (set.document.key != null) {
                const component: string = set.components[set.components.length - 1].id;
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
    }

    getDefaultImgURL(event: any): void {
        event.target.src = this.context + 'assets/thumbnail-default.png';
    }

    handleMapIt(set: ImageSet): void {

        this.location.replaceState('/site/viewer/set/' + set.id);


        this.store.dispatch(MapActions.toggleSet({ set }));
    }


    handleDelete(set: ImageSet, event: any): void {

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
            const imageSetId = set.id;

            this.pService.remove(imageSetId).then(() => {

                this.store.dispatch(MapActions.removeImageSet({ id: set.id }));
            });
        });
    }

    previewImage(imageSet: ImageSet): void {

        if (imageSet.document.key != null) {

            const component: string = imageSet.components[imageSet.components.length - 1].id;

            const bsModalRef = this.modalService.show(ImagePreviewModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: false,
                'class': 'image-preview-modal modal-xl'
            });

            bsModalRef.content.initRaw(component, imageSet.document.key);
        }
    }


    handleToggleLock(imageSet: ImageSet): void {
        this.pService.toggleLock(imageSet.id).then(() => {
            imageSet.locked = !imageSet.locked;
        });
    }

    handleClipboard(imageSet: ImageSet) {
        const urlTree: UrlTree = this.router.createUrlTree(
            ['/site/viewer', 'set', imageSet.id]
        );

        // Convert UrlTree to a string
        const generatedUrl = this.router.serializeUrl(urlTree);

        this.clipboard.copy(window.location.origin + "/#" + generatedUrl);
    }
}
