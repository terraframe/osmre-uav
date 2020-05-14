import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { MetadataModalComponent } from './modal/metadata-modal.component';
import { BasicConfirmModalComponent } from '../../shared/component/modal/basic-confirm-modal.component';
import { LeafModalComponent } from './modal/leaf-modal.component';
import { PageResult } from '../../shared/model/page';

import { interval, from } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { Message, Task, TaskGroup } from '../model/management';
import { ManagementService } from '../service/management.service';

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
    tasks: any;
    taskPage: PageResult<Task> = { count: 0, pageSize: 10, pageNumber: 1, resultSet: [] };
    errorStatuses = ["Error", "Failed", "Queued", "Processing"];
    completeStatuses = ["Complete"];

    statuses = [];

    /*
     * Token used to determine if a change has occured in the page before loading the polling values
     */
    token: number = 0;

    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;

    /*
     * List of messages
     */
    messages: PageResult<Message> = { count: 0, pageSize: 5, pageNumber: 1, resultSet: [] };

    /*
     * List of tasks
     */
    // tasks: PageResult<Task>;

    collectionGroups: TaskGroup[] = [];

    constructor(private managementService: ManagementService, private modalService: BsModalService) {

        this.taskPolling = interval(5000).pipe(
            switchMap(() => from(this.managementService.tasks(this.statuses, this.taskPage.pageSize, this.taskPage.pageNumber, this.token))))
            .subscribe((data) => {
                if (data['token'] === this.token) {
                    this.updateTaskData(data);
                }
            });

    }

    ngOnInit(): void {
        this.userName = this.managementService.getCurrentUser();
        this.managementService.tasks([], this.taskPage.pageSize, this.taskPage.pageNumber, this.token).then(data => {
            this.setTaskData(data, false);
        });

        this.getMissingMetadata();
    }

    ngOnDestroy(): void {

        if (this.taskPolling) {
            this.taskPolling.unsubscribe();
        }
    }

    updatePage(data: PageResult<Task>): void {
        this.taskPage.pageNumber = data.pageNumber;
        this.taskPage.pageSize = data.pageSize;
        this.taskPage.count = data.count;
        this.taskPage.resultSet = data.resultSet;
    }

    onPageChange(pageNumber: number): void {
        this.token++;

        this.managementService.tasks(this.statuses, this.taskPage.pageSize, pageNumber, this.token).then(tasks => {

            this.updatePage(tasks);

            this.setTaskData(tasks, false);
        });
    }

    onTabClick(event: any, tab: string): void {
        this.activeTab = tab;
        this.taskPage = { count: 0, pageSize: 20, pageNumber: 1, resultSet: [] };
        this.token++;

        if (tab === "success") {
            this.statuses = this.completeStatuses;
        }
        else if (tab === "action-required") {
            this.statuses = this.errorStatuses;
        }
        else if (tab === "all") {
            this.statuses = [];
        }

        this.managementService.tasks(this.statuses, this.taskPage.pageSize, this.taskPage.pageNumber, this.token).then(tasks => {

            this.updatePage(tasks);

            this.setTaskData(tasks, false);
        });


        if (!event.target.parentNode.classList.contains("active")) {

            let lis = event.target.parentNode.parentNode.getElementsByTagName("li");
            for (let i = 0; i < lis.length; i++) {
                let li = lis[i];

                li.classList.forEach(cls => {
                    if (cls === 'active') {
                        li.classList.remove('active');
                    }
                })
            }

            event.target.parentNode.classList.add('active');
        }
    }

    setTaskData(tasks: PageResult<Task>, addOnly: boolean): void {

        if (!addOnly) {
            this.updatePage(tasks);

            this.collectionGroups = [];
        }

        for (let i = 0; i < tasks.resultSet.length; i++) {
            let task = tasks.resultSet[i];
            let collectPosition = this.collectionGroups.findIndex(value => { return task.collection === value.collectionId });

            if (collectPosition > -1) {

                if (task.type === 'gov.geoplatform.uasdm.bus.WorkflowTask') {

                    let taskGroupTypeIndex = this.collectionGroups[collectPosition].groups.findIndex(value => { return value.type === 'UPLOAD' });

                    if (taskGroupTypeIndex === -1) {
                        this.collectionGroups[collectPosition].groups.push({ tasks: [task], status: task.status, type: 'UPLOAD' })
                    }
                    else {
                        this.collectionGroups[collectPosition].groups[taskGroupTypeIndex].tasks.push(task);
                    }
                }
                else if (task.type === 'gov.geoplatform.uasdm.odm.ODMProcessingTask') {

                    let taskGroupTypeIndex = this.collectionGroups[collectPosition].groups.findIndex(value => { return value.type === 'PROCESS' });

                    if (taskGroupTypeIndex === -1) {
                        this.collectionGroups[collectPosition].groups.push({ tasks: [task], status: task.status, type: 'PROCESS' })
                    }
                    else {
                        this.collectionGroups[collectPosition].groups[taskGroupTypeIndex].tasks.push(task);
                    }
                }
                else if (task.type === 'gov.geoplatform.uasdm.odm.ODMUploadTask') {

                    let taskGroupTypeIndex = this.collectionGroups[collectPosition].groups.findIndex(value => { return value.type === 'STORE' });

                    if (taskGroupTypeIndex === -1) {
                        this.collectionGroups[collectPosition].groups.push({ tasks: [task], status: task.status, type: 'STORE' })
                    }
                    else {
                        this.collectionGroups[collectPosition].groups[taskGroupTypeIndex].tasks.push(task);
                    }
                }
            }
            else {

                if (task.type === 'gov.geoplatform.uasdm.bus.WorkflowTask') {

                    this.collectionGroups.push({
                        label: task.collectionLabel,
                        collectionId: task.collection,
                        groups: [{ tasks: [task], status: task.status, type: 'UPLOAD' }],
                        status: task.status,
                        lastUpdatedDate: task.lastUpdatedDate
                    });
                }
                else if (task.type === 'gov.geoplatform.uasdm.odm.ODMProcessingTask') {

                    this.collectionGroups.push({
                        label: task.collectionLabel,
                        collectionId: task.collection,
                        groups: [{ tasks: [task], status: task.status, type: 'PROCESS' }],
                        status: task.status,
                        lastUpdatedDate: task.lastUpdatedDate
                    });
                }
                else if (task.type === 'gov.geoplatform.uasdm.odm.ODMUploadTask') {

                    this.collectionGroups.push({
                        label: task.collectionLabel,
                        collectionId: task.collection,
                        groups: [{ tasks: [task], status: task.status, type: 'STORE' }],
                        status: task.status,
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

            collectionGroup.groups.forEach(group => {

                if (group.tasks.length > 0) {
                    let sortedTasks = group.tasks.sort((a: any, b: any) =>
                        new Date(b.lastUpdatedDate).getTime() - new Date(a.lastUpdatedDate).getTime()
                    );

                    group.status = sortedTasks[0].status;
                }

                if (group.status === "Error" || group.status === "Failed") {
                    isError = true;
                }
                else if (group.status === "Queued" || group.status === "Processing" || group.status === "Running" || group.status === "Pending") {
                    isWorking = true;
                }
            });

            if (isWorking) {
                collectionGroup.status = "Processing";
            }
            else if (isError) {
                collectionGroup.status = "Failed";
            }
            else {
                collectionGroup.status = "Complete";
            }

        })
    }


    updateTaskData(tasks: PageResult<Task>): void {
        let noMatch = [];

        this.totalTaskCount = tasks.count;

        // Update existing tasks
        tasks.resultSet.forEach(newTask => {

            let matchFound: boolean = false;

            this.collectionGroups.forEach(existingTaskGrp => {
                existingTaskGrp.groups.forEach(existingGroup => {

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
                            
                            existingTask.actions = newTask.actions;
                        }
                    })
                });
            });

            if (!matchFound) {
                noMatch.push(newTask);
            }
        })

        // Add new tasks
        // let newTasks = data.tasks.filter((o: Task) => !this.collectionGroups.resultSet.find(o2 => o.oid === o2.oid));
        if (noMatch && noMatch.length > 0) {
            this.setTaskData({ resultSet: noMatch, count: tasks.count, pageNumber: this.taskPage.pageNumber, pageSize: this.taskPage.pageSize }, true);
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
            for (let i = 0; i < this.messages.resultSet.length; i++) {
                let msg = this.messages.resultSet[i];
                if (msg.collectionId === collectionId) {
                    index = i;
                }
            }

            if (index >= 0) {
                this.messages.resultSet.splice(index, 1);
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
        this.onMessagePageChange(this.messages.pageNumber);
    }

    onMessagePageChange(pageNumber: number): void {
        this.managementService.getMissingMetadata(this.messages.pageSize, pageNumber).then(messages => {
            this.messages = messages;
        });
    }
}
