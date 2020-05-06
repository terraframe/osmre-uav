import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { MetadataModalComponent } from './modal/metadata-modal.component';
import { BasicConfirmModalComponent } from '../../shared/component/modal/basic-confirm-modal.component';
import { LeafModalComponent } from './modal/leaf-modal.component';
import { PageResult } from '../../shared/model/page';

import { HttpClient } from '@angular/common/http';
import { interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { Message, Task, TaskGroup } from '../model/management';
import { ManagementService } from '../service/management.service';
import { Page } from 'ngx-pagination/dist/pagination-controls.directive';

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
	activeTab: string = "all";
	showSite: boolean = false;
	initData: any;
	showUploads: boolean = false;
	showProcess: boolean = false;
	showStore: boolean = false;
	tasks:any;
	taskPage: PageResult<Task> = {count:0, pageSize: 20, pageNumber: 1, resultSet: []};
	errorStatuses = ["Error", "Failed", "Queued", "Processing"];
	completeStatuses = ["Complete"];

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
	// tasks: PageResult<Task>;

	collectionGroups: TaskGroup[] = [];

	constructor(http: HttpClient, private managementService: ManagementService, private modalService: BsModalService) {

		// this.taskPolling = interval(5000).pipe(
		// 	switchMap(() => http.get<any>(acp + '/project/tasks')))
		// 	.subscribe((data) => {
		// 		this.updateTaskData(data);
		// 	});
	}

	ngOnInit(): void {
		this.userName = this.managementService.getCurrentUser();
		this.managementService.tasks([], this.taskPage.pageSize, this.taskPage.pageNumber).then(data => {
			this.setTaskData(data);
		});
	}

	ngOnDestroy(): void {

		if (this.taskPolling) {
			this.taskPolling.unsubscribe();
		}
	}

	updatePage(data: any): void{
		this.taskPage.pageNumber = data.pageNumber;
		this.taskPage.pageSize = data.pageSize;
		this.taskPage.count = data.count;
		this.taskPage.resultSet = data
	}

	onPageChange(pageNumber: number, statuses): void{
		this.managementService.tasks(statuses, this.taskPage.pageSize, pageNumber).then(data => {

			this.updatePage(data.tasks);
			
			this.setTaskData(data);
		});
	}

	onTabClick(event: any, tab: string): void {
		this.activeTab = tab;
		this.taskPage = {count:0, pageSize: 20, pageNumber: 1, resultSet: []};

		if(tab === "success"){

			this.managementService.tasks(this.completeStatuses, this.taskPage.pageSize, this.taskPage.pageNumber).then(data => {

				this.updatePage(data.tasks);
				
				this.setTaskData(data);
			});
		}
		else if(tab === "action-required") {
			this.managementService.tasks(this.errorStatuses, this.taskPage.pageSize, this.taskPage.pageNumber).then(data => {

				this.updatePage(data.tasks);

				this.setTaskData(data);
			});
		}
		else if(tab === "all") {
			this.managementService.tasks([], this.taskPage.pageSize, this.taskPage.pageNumber).then(data => {

				this.updatePage(data.tasks);

				this.setTaskData(data);
			});
		}

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

		this.updatePage(data.tasks);

		this.collectionGroups = [];

		for(let i=0; i<data.tasks.resultSet.length; i++){
			let task = data.tasks.resultSet[i];
			let collectPosition = this.collectionGroups.findIndex( value => { return task.collectionLabel === value.label } ); 
            
			if(collectPosition > -1){

				if(task.type === 'gov.geoplatform.uasdm.bus.WorkflowTask'){

					let taskGroupTypeIndex = this.collectionGroups[collectPosition].groups.findIndex( value => { return value.type === 'UPLOAD' } ); 

					if(taskGroupTypeIndex === -1){
						this.collectionGroups[collectPosition].groups.push({ tasks:[task], status:task.status, type: 'UPLOAD' })
					}
					else{
						this.collectionGroups[collectPosition].groups[taskGroupTypeIndex].tasks.push(task);
					}
				}
				else if(task.type === 'gov.geoplatform.uasdm.odm.ODMProcessingTask'){

					let taskGroupTypeIndex = this.collectionGroups[collectPosition].groups.findIndex( value => { return value.type === 'PROCESS' } ); 

					if(taskGroupTypeIndex === -1){
						this.collectionGroups[collectPosition].groups.push({ tasks:[task], status:task.status, type: 'PROCESS' })
					}
					else{
						this.collectionGroups[collectPosition].groups[taskGroupTypeIndex].tasks.push(task);
					}
				}
				else if(task.type === 'gov.geoplatform.uasdm.odm.ODMUploadTask'){

					let taskGroupTypeIndex = this.collectionGroups[collectPosition].groups.findIndex( value => { return value.type === 'STORE' } );

					if(taskGroupTypeIndex === -1){
						this.collectionGroups[collectPosition].groups.push({ tasks:[task], status:task.status, type: 'STORE' })
					}
					else{
						this.collectionGroups[collectPosition].groups[taskGroupTypeIndex].tasks.push(task);
					}
				}
			}
			else{

				if(task.type === 'gov.geoplatform.uasdm.bus.WorkflowTask'){

					this.collectionGroups.push({
						label: task.collectionLabel, 
						collectionId: task.collection,
						groups: [{ tasks:[task], status:task.status, type: 'UPLOAD' }],
						status:task.status,
						lastUpdatedDate: task.lastUpdatedDate
					});
				}
				else if(task.type === 'gov.geoplatform.uasdm.odm.ODMProcessingTask'){

					this.collectionGroups.push({
						label: task.collectionLabel, 
						collectionId: task.collection,
						groups: [{ tasks:[task], status:task.status, type: 'PROCESS' }],
						status:task.status,
						lastUpdatedDate: task.lastUpdatedDate
					});
				}
				else if(task.type === 'gov.geoplatform.uasdm.odm.ODMUploadTask'){

					this.collectionGroups.push({
						label: task.collectionLabel, 
						collectionId: task.collection,
						groups: [{ tasks:[task], status:task.status, type: 'STORE' }],
						status:task.status,
						lastUpdatedDate: task.lastUpdatedDate
					});
				}

			}
		}
		
		this.collectionGroups = this.collectionGroups.sort((a: any, b: any) =>
			new Date(b.lastUpdatedDate).getTime() - new Date(a.lastUpdatedDate).getTime()
		);

		this.setTaskGroupStatuses();
	}


	setTaskGroupStatuses(): void {

		this.collectionGroups.forEach(collectionGroup => {
			
			let isError: boolean = false;
			let isWorking: boolean = false;
			let isComplete: boolean = false;

			collectionGroup.groups.forEach(group => {

				if (group.tasks.length > 0) {
					let sortedTasks = group.tasks.sort((a: any, b: any) =>
						new Date(b.lastUpdatedDate).getTime() - new Date(a.lastUpdatedDate).getTime()
					);

					group.status = sortedTasks[group.tasks.length - 1].status;
				}

				if (group.status === "Error" || group.status === "Failed") {
					isError = true;
				}
				else if (group.status === "Queued" || group.status === "Processing") {
					isWorking = true;
				}
				else if (group.status === "Complete") {
					isComplete = true;
				}
			});

			if(isWorking){
				collectionGroup.status = "Processing";
			}
			else if(isError){
				collectionGroup.status = "Failed";
			}
			else{
				collectionGroup.status = "Complete";
			}

		})
	}


	updateTaskData(data: {messages:Message[], tasks:PageResult<Task>}): void {
		this.messages = data.messages;
		let noMatch = [];

		this.totalTaskCount = data.tasks.count;

		// Update existing tasks
		data.tasks.resultSet.forEach(newTask => {

			this.collectionGroups.forEach(existingTaskGrp => {
				existingTaskGrp.groups.forEach(existingGroup => {

					let matchFound: boolean = false;
					existingGroup.tasks.forEach(existingTask => {
						if (existingTask.oid === newTask.oid) {
							
							matchFound = true;

							// Update props
							if (existingTask.label !== newTask.label) {
								existingTask.label = newTask.label;
							}
							if (existingTask.lastUpdateDate !== newTask.lastUpdateDate) {
								existingTask.lastUpdateDate = newTask.lastUpdateDate;
							}
							if (existingTask.lastUpdatedDate !== newTask.lastUpdatedDate) {
								existingTask.lastUpdatedDate = newTask.lastUpdatedDate;
								matchFound = true;
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
					})

					if(!matchFound){
						noMatch.push(newTask);
					}
				});
			})
		})

		// Add new tasks
		// let newTasks = data.tasks.filter((o: Task) => !this.collectionGroups.resultSet.find(o2 => o.oid === o2.oid));
		if(noMatch && noMatch.length > 0) {
			this.setTaskData({messages : data.messages, tasks: {resultSet: noMatch, count:data.tasks.count, pageNumber:this.taskPage.pageNumber, pageSize:this.taskPage.pageSize}})
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
		// let breadcrumbs = []

		this.managementService.view(collectionId).then(response => {
			const entity = response.item;
			const breadcrumbs = response.breadcrumbs;

			this.managementService.getItems(collectionId, null).then(nodes => {
				this.bsModalRef = this.modalService.show(LeafModalComponent, {
					animated: true,
					backdrop: true,
					ignoreBackdropClick: true,
					class: 'leaf-modal'
				});
				this.bsModalRef.content.init(entity, nodes, breadcrumbs);
			})
		})
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
