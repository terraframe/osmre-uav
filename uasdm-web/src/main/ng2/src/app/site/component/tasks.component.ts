import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { MetadataModalComponent } from './modal/metadata-modal.component';
import { BasicConfirmModalComponent } from '../../shared/component/modal/basic-confirm-modal.component';
import { PageResult } from '../../shared/model/page';

import { Message, Task, TaskGroup } from '../model/management';
import { ManagementService } from '../service/management.service';

declare var acp: any;

@Component({
	selector: 'tasks',
	templateUrl: './tasks.component.html',
	styleUrls: ['./tasks.css']
})
export class TasksComponent implements OnInit {

	userName: string = "";
	totalTaskCount: number = 0;
	taskPolling: any;
	activeTab: string = "action-required";
	showSite: boolean = false;
	initData: any;
	showUploads: boolean = false;
	showProcess: boolean = false;
	showStore: boolean = false;

    /*
     * Reference to the modal current showing
     */
	bsModalRef: BsModalRef;

    /*
     * List of messages
     */
	messages: Message[];

    /*
     * List of tasks
     */
	tasks: PageResult<Task>;

	taskGroups: TaskGroup[] = [];

	constructor(private managementService: ManagementService, private modalService: BsModalService) {

		// this.taskPolling = interval(5000).pipe(
		// 	switchMap(() => http.get<any>(acp + '/project/tasks')))
		// 	.subscribe((data) => {
		// 		this.updateTaskData(data);
		// 	});
	}

	ngOnInit(): void {
		this.userName = this.managementService.getCurrentUser();
		this.managementService.tasks().then(data => {

			this.setTaskData(data);

		});
	}

	ngOnDestroy(): void {

		if (this.taskPolling) {
			this.taskPolling.unsubscribe();
		}
	}

	onTabClick(event: any, tab: string): void {
		this.activeTab = tab;

		if(!event.target.parentNode.classList.contains("active")){

			let lis = event.target.parentNode.parentNode.getElementsByTagName("li");
			for(let i = 0; i<lis.length; i++){
				let li = lis[i];

				li.classList.forEach(cls => {
					if(cls === 'active'){
						li.classList.remove('active');
					}
				})
			}

			event.target.parentNode.classList.add('active');
		}
	}

	setTaskData(data: {messages:Message[], tasks:PageResult<Task>}): void {
		this.messages = data.messages;

		for(let i=0; i<data.tasks.resultSet.length; i++){
			let task = data.tasks.resultSet[i];
			let collectPosition = this.taskGroups.findIndex( value => { return task.collectionLabel === value.label } ); 
            
			if(collectPosition > -1){

				if(task.type === 'gov.geoplatform.uasdm.bus.WorkflowTask'){

					let taskGroupTypeIndex = this.taskGroups[collectPosition].groups.findIndex( value => { return value.type === 'UPLOAD' } ); 

					if(taskGroupTypeIndex === -1){
						this.taskGroups[collectPosition].groups.push({ tasks:[task], status:task.status, type: 'UPLOAD' })
					}
					else{
						this.taskGroups[collectPosition].groups[taskGroupTypeIndex].tasks.push(task);

						if(task.status === 'Failed' || task.status === 'Pending'){
							this.taskGroups[collectPosition].groups[taskGroupTypeIndex].type = task.status
						}
					}

				}
				else if(task.type === 'gov.geoplatform.uasdm.odm.ODMProcessingTask'){

					let taskGroupTypeIndex = this.taskGroups[collectPosition].groups.findIndex( value => { return value.type === 'PROCESS' } ); 

					if(taskGroupTypeIndex === -1){
						this.taskGroups[collectPosition].groups.push({ tasks:[task], status:task.status, type: 'PROCESS' })
					}
					else{
						this.taskGroups[collectPosition].groups[taskGroupTypeIndex].tasks.push(task);

						if(task.status === 'Failed' || task.status === 'Pending'){
							this.taskGroups[collectPosition].groups[taskGroupTypeIndex].type = task.status
						}
					}

				}
				else if(task.type === 'gov.geoplatform.uasdm.odm.ODMUploadTask'){

					let taskGroupTypeIndex = this.taskGroups[collectPosition].groups.findIndex( value => { return value.type === 'STORE' } );

					if(taskGroupTypeIndex === -1){
						this.taskGroups[collectPosition].groups.push({ tasks:[task], status:task.status, type: 'STORE' })
					}
					else{
						this.taskGroups[collectPosition].groups[taskGroupTypeIndex].tasks.push(task);

						if(task.status === 'Failed' || task.status === 'Pending'){
							this.taskGroups[collectPosition].groups[taskGroupTypeIndex].type = task.status
						}
					}
				}


				if(task.status === 'Failed' || task.status === 'Pending'){
					this.taskGroups[collectPosition].status = task.status
				}
			}
			else{

				if(task.type === 'gov.geoplatform.uasdm.bus.WorkflowTask'){

					this.taskGroups.push({
						label: task.collectionLabel, 
						groups: [{ tasks:[task], status:task.status, type: 'UPLOAD' }],
						status:task.status 
					});
				}
				else if(task.type === 'gov.geoplatform.uasdm.odm.ODMProcessingTask'){

					this.taskGroups.push({
						label: task.collectionLabel, 
						groups: [{ tasks:[task], status:task.status, type: 'PROCESS' }],
						status:task.status 
					});
				}
				else if(task.type === 'gov.geoplatform.uasdm.odm.ODMUploadTask'){

					this.taskGroups.push({
						label: task.collectionLabel, 
						groups: [{ tasks:[task], status:task.status, type: 'STORE' }],
						status:task.status 
					});
				}

			}
		}
		
		console.log(this.taskGroups)

		// this.tasks = data.tasks.sort((a: any, b: any) =>
		// 	new Date(b.lastUpdatedDate).getTime() - new Date(a.lastUpdatedDate).getTime()
		// );
	}

	updateTaskData(data: any): void {
		this.messages = data.messages;

		this.totalTaskCount = data.tasks.count;

		// Update existing tasks
		for (let i = 0; i < data.tasks.length; i++) {
			let newTask = data.tasks[i];

			for (let i2 = 0; i2 < this.tasks.resultSet.length; i2++) {
				let existingTask = this.tasks[i2];
				if (existingTask.oid === newTask.oid) {
					if (existingTask.label !== newTask.label) {
						existingTask.label = newTask.label;
					}
					if (existingTask.lastUpdateDate !== newTask.lastUpdateDate) {
						existingTask.lastUpdateDate = newTask.lastUpdateDate;
					}
					if (existingTask.lastUpdatedDate !== newTask.lastUpdatedDate) {
						existingTask.lastUpdatedDate = newTask.lastUpdatedDate;
					}
					if (existingTask.message !== newTask.message) {
						existingTask.message = newTask.message;
					}
					if (existingTask.status !== newTask.status) {
						existingTask.status = newTask.status;
					}
					if (existingTask.odmOutput !== newTask.odmOutput) {
						existingTask.odmOutput = newTask.odmOutput;
					}
				}
			}
		}

		// Add new tasks
		let newTasks = data.tasks.filter((o: Task) => !this.tasks.resultSet.find(o2 => o.oid === o2.oid));
		if (newTasks && newTasks.length > 0) {
			newTasks.forEach((tsk: Task) => {
				this.tasks.resultSet.unshift(tsk);
			})
		}
	}


	handleMessage(message: Message): void {
		this.bsModalRef = this.modalService.show(MetadataModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'upload-modal'
		});
		this.bsModalRef.content.init(message.collectionId);

		this.bsModalRef.content.onMetadataChange.subscribe((collectionId: string) => {

			let index = -1;
			for (let i = 0; i < this.messages.length; i++) {
				let msg = this.messages[i];
				if (msg.collectionId === collectionId) {
					index = i;
				}
			}

			if (index >= 0) {
				this.messages.splice(index, 1);
			}

		});

	}

	handleGoto(collectionId: string): void {
		// const entity = this.product.entities[this.product.entities.length - 1];
		// const breadcrumbs = this.product.entities;

		this.managementService.getItems("71ac9976-1a3b-4707-800d-4769520005bf", null).then(nodes => {

			let entity = {"numberOfChildren":82,"ownerName":"admin","ownerPhone":"","name":"Copper collect","description":"","typeLabel":"Collection","id":"71ac9976-1a3b-4707-800d-4769520005bf","folderName":"b71a7425b2754c5e9127a773971c53d3","type":"Collection","privilegeType":"AGENCY","ownerEmail":"admin@noreply.com"}
			let breadcrumbs = [{"numberOfChildren":1,"name":"Copper","otherBureauTxt":"","description":"","typeLabel":"Site","geometry":{"coordinates":[-107.3016,43.0521],"type":"Point"},"id":"b220b1c9-aee0-41cb-b99c-875d830005bc","folderName":"cbbdc906312645ecb269d6d5f17db924","bureau":"adb54227-b659-4243-81b6-3aaa8b0005ba","type":"Site","geoPoint":"POINT (-107.3016159523629 43.05214347865024)"},{"numberOfChildren":1,"name":"Copper proj","description":"","typeLabel":"Project","id":"4aeb9cfb-d9c5-404f-98d0-1fae550005bd","folderName":"92493681cf144704973ba0e2936df9bf","type":"Project"},{"numberOfChildren":2,"name":"Copper Miss","description":"","typeLabel":"Mission","id":"031508cd-d442-4ab6-bcf2-c365a90005be","folderName":"8930cf209283412bb42cd76e472c034d","type":"Mission"},{"numberOfChildren":82,"ownerName":"admin","ownerPhone":"","name":"Copper collect","description":"","typeLabel":"Collection","id":"71ac9976-1a3b-4707-800d-4769520005bf","folderName":"b71a7425b2754c5e9127a773971c53d3","type":"Collection","privilegeType":"AGENCY","ownerEmail":"admin@noreply.com"}]
			
			this.initData = { "entity": entity, "folders": nodes, "previous": breadcrumbs }

			this.showSite = true;
		});

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
				for (let i = 0; i < this.tasks.resultSet.length; i++) {
					let thisTask = this.tasks[i];

					if (thisTask.uploadId === task.uploadId) {
						pos = i;
						break;
					}
				}

				if (pos !== null) {
					this.tasks.resultSet.splice(pos, 1);
				}

				this.getMissingMetadata();

				this.totalTaskCount = this.tasks.count;

			});
	}

	getMissingMetadata(): void {

		this.managementService.getMissingMetadata()
			.then(messages => {
				this.messages = messages;
			});
	}
}
