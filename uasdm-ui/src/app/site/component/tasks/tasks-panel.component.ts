///
///
///

import { Component, OnInit, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';

import { Task, TaskGroup, TaskGroupType } from '@site/model/management';
import { ManagementService } from '@site/service/management.service';


@Component({
	selector: 'tasks-panel',
	templateUrl: './tasks-panel.component.html',
	styleUrls: ['./tasks-panel.css']
})
export class TasksPanelComponent implements OnInit {

	@Input() taskGroupType: TaskGroupType;
	@Input() groupTypeId: number;
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

	constructor(private managementService: ManagementService, private modalService: BsModalService) {

	}

	ngOnInit(): void {
	  if (this.taskGroupType != null && this.taskGroupType.tasks != null)
	  {
		  for (var i = 0; i < this.taskGroupType.tasks.length; ++i)
		  {
		    let task = this.taskGroupType.tasks[i];
		    
		    if (task.actions.length > 0)
		    {
		      task.showError = true;
		    }
		  }
	  }
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
		this.managementService.removeTask(task.oid)
			.then(() => {
				let pos = null;
				for (let i = 0; i < this.taskGroupType.tasks.length; i++) {
					let thisTask = this.taskGroupType.tasks[i];

					if (thisTask.oid === task.oid) {
						pos = i;
						break;
					}
				}

				if (pos !== null) {
					this.taskGroupType.tasks.splice(pos, 1);
				}
			});
	}
}
