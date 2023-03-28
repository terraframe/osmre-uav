import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { LngLat } from 'maplibre-gl';

import { ErrorHandler } from '@shared/component';

import { SiteEntity, AttributeType } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';


@Component({
	selector: 'entity-modal',
	templateUrl: './entity-modal.component.html',
	styleUrls: []
})
export class EntityModalComponent implements OnInit {
    /*
     * parent id of the node being created
     */
	parentId: string;
	userName: string = "";

	entity: SiteEntity;

	attributes: AttributeType[];

	admin: boolean = false;

	newInstance: boolean = false;

	message: string = null;

	center: LngLat = null;
	zoom: number = null;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
	public onNodeChange: Subject<SiteEntity>;

	constructor(private service: ManagementService, public bsModalRef: BsModalRef) { }

	ngOnInit(): void {
		this.onNodeChange = new Subject();
	}

	init(newInstance: boolean, userName: string, admin: boolean, entity: SiteEntity, attributes: AttributeType[], center: LngLat, zoom: number) {
		this.newInstance = newInstance;
		this.userName = userName;
		this.admin = admin;
		this.entity = entity;
		this.attributes = attributes;
		this.center = center;
		this.zoom = zoom;
	}

	handleOnSubmit(): void {
		this.message = null;

		if (this.entity.type !== 'Site' || this.entity.geometry != null) {
			if (this.newInstance) {
				this.service.applyWithParent(this.entity, this.parentId).then(data => {
					this.onNodeChange.next(data);
					this.bsModalRef.hide();
				}).catch((err: HttpErrorResponse) => {
					this.error(err);
				});
			}
			else {
				this.service.update(this.entity).then(node => {
					this.onNodeChange.next(node);

					this.bsModalRef.hide();
				}).catch((err: HttpErrorResponse) => {
					console.log(err);

					this.error(err);
				});
			}
		}
		else {
			this.message = "Sites require a location";
		}
	}

	evaluate(attribute: AttributeType): boolean {

		if (this.newInstance && attribute.readonly) {
			return false;
		}
		else if (attribute.condition != null) {
			return this.service.evaluate(attribute.condition, this.entity);
		}

		return true;
	}

	error(err: HttpErrorResponse): void {
	  this.message = ErrorHandler.getMessageFromError(err);
	}

}
