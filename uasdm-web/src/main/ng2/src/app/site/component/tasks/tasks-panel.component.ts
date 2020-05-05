import { Component, OnInit, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '../../../shared/component/modal/basic-confirm-modal.component';
import { PageResult } from '../../../shared/model/page';

import { HttpClient } from '@angular/common/http';
import { interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { Message, Task, TaskGroup, TaskGroupType } from '../../model/management';
import { ManagementService } from '../../service/management.service';
import { doesNotMatch } from 'assert';

declare var acp: any;

@Component({
	selector: 'tasks-panel',
	templateUrl: './tasks-panel.component.html',
	styleUrls: ['./tasks-panel.css']
})
export class TasksPanelComponent implements OnInit {

	@Input() taskGroupType: TaskGroupType;
	@Input() groupTypeId: string;
	@Input() taskCategory: string;
	showUploads: boolean = false;
	showProcess: boolean = false;
	showStore: boolean = false;
	showODMOutput: boolean = false;
	showError: boolean = false;

    /*
     * Reference to the modal current showing
     */
	bsModalRef: BsModalRef;


    /*
     * List of tasks
     */
	taskGroups: TaskGroup[] = [];

	constructor(http: HttpClient, private managementService: ManagementService, private modalService: BsModalService) {

	}

	ngOnInit(): void {
	}

	ngOnDestroy(): void {
	}


	removeTask(task: Task): void {

		this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = 'Are you sure you want to delete [' + task.label + '?';
		this.bsModalRef.content.data = task;
		this.bsModalRef.content.type = 'DANGER';
		this.bsModalRef.content.submitText = 'Delete';

		(<BasicConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(task => {
			this.deleteTask(task);
		});

	}

	deleteTask(task: Task) {
		this.managementService.removeTask(task.uploadId)
			.then(() => {
				let pos = null;
				for (let i = 0; i < this.taskGroupType.tasks.length; i++) {
					let thisTask = this.taskGroupType.tasks[i];

					if (thisTask.uploadId === task.uploadId) {
						pos = i;
						break;
					}
				}

				if (pos !== null) {
					this.taskGroupType.tasks.splice(pos, 1);
				}

				this.getMissingMetadata();
			});
	}

	getMissingMetadata(): void {

		this.managementService.getMissingMetadata()
			.then(messages => {
				// this.messages = messages;
				// TODO ^^
			});
	}

}
