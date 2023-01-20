import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { MetadataModalComponent } from './modal/metadata-modal.component';
import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { CollectionModalComponent } from './modal/collection-modal.component';
import { PageResult } from '@shared/model/page';

import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { Message, Task, TaskGroup } from '../model/management';
import { ManagementService } from '../service/management.service';
import EnvironmentUtil from '@core/utility/environment-util';



@Component({
  selector: 'tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.css']
})
export class TasksComponent implements OnInit {

  userName: string = "";
  totalTaskCount: number = 0;
  activeTab: string = "all";
  showSite: boolean = false;
  initData: any;
  showUploads: boolean = false;
  showProcess: boolean = false;
  showStore: boolean = false;
  tasks: any;
  taskPage: PageResult<TaskGroup> = { count: 0, pageSize: 10, pageNumber: 1, resultSet: [] };
  errorStatuses = ["Failed", "Processing"];
  completeStatuses = ["Complete"];
  visible: {};

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

  notifier: WebSocketSubject<{ type: string, content: any }>;

  constructor(private managementService: ManagementService, private modalService: BsModalService) { }

  ngOnInit(): void {
    this.userName = this.managementService.getCurrentUser();
    this.managementService.tasks([], this.taskPage.pageSize, this.taskPage.pageNumber, this.token).then(data => {
      this.setTaskData(data);
    });

    this.getMessages();

    let baseUrl = "wss://" + window.location.hostname + (window.location.port ? ':' + window.location.port : '') + EnvironmentUtil.getApiUrl();

    this.notifier = webSocket(baseUrl + '/websocket-notifier/notify');
    this.notifier.subscribe(message => {
      if (message.type === 'JOB_CHANGE') {
        this.managementService.tasks(this.statuses, this.taskPage.pageSize, this.taskPage.pageNumber, this.token).then(data => {
          if (data['token'] === this.token) {
            this.updateTaskData(data);
          }
        });
        this.getMessages();
      }
    });

  }

  ngOnDestroy(): void {

    this.notifier.complete();
  }

  onPageChange(pageNumber: number): void {
    this.token++;

    this.managementService.tasks(this.statuses, this.taskPage.pageSize, pageNumber, this.token).then(tasks => {

      this.setTaskData(tasks);
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

      this.setTaskData(tasks);
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

  setTaskData(tasks: PageResult<TaskGroup>): void {
    this.taskPage = tasks;
    this.visible = {};

    this.taskPage.resultSet.forEach(task => {
      this.visible[task.collectionId] = false;
    });
  }

  setGroupTasks(collection: TaskGroup, tasks: Task[]): void {
    collection.groups = [];
    collection.groups.push({ tasks: [], status: null, type: 'UPLOAD' });
    collection.groups.push({ tasks: [], status: null, type: 'PROCESS' });
    collection.groups.push({ tasks: [], status: null, type: 'STORE' });


    for (let i = 0; i < tasks.length; i++) {
      let task = tasks[i];


      if (task.type === 'gov.geoplatform.uasdm.bus.WorkflowTask') {

        let taskGroupTypeIndex = collection.groups.findIndex(value => { return value.type === 'UPLOAD' });

        if (taskGroupTypeIndex === -1) {
          collection.groups.push({ tasks: [task], status: task.status, type: 'UPLOAD' })
        }
        else {
          collection.groups[taskGroupTypeIndex].tasks.push(task);
        }
      }
      else if (task.type === 'gov.geoplatform.uasdm.odm.ODMProcessingTask' || task.type === 'gov.geoplatform.uasdm.bus.OrthoProcessingTask') {

        let taskGroupTypeIndex = collection.groups.findIndex(value => { return value.type === 'PROCESS' });

        if (taskGroupTypeIndex === -1) {
          collection.groups.push({ tasks: [task], status: task.status, type: 'PROCESS' })
        }
        else {
          collection.groups[taskGroupTypeIndex].tasks.push(task);
        }
      }
      else if (task.type === 'gov.geoplatform.uasdm.odm.ODMUploadTask') {

        let taskGroupTypeIndex = collection.groups.findIndex(value => { return value.type === 'STORE' });

        if (taskGroupTypeIndex === -1) {
          collection.groups.push({ tasks: [task], status: task.status, type: 'STORE' })
        }
        else {
          collection.groups[taskGroupTypeIndex].tasks.push(task);
        }
      }
    }

    this.setTaskGroupStatuses(collection);
  }


  setTaskGroupStatuses(collection: TaskGroup): void {

    let latestDate: Date = null;

    collection.groups.forEach(group => {

      let isLatestTask: boolean = false;

      if (group.tasks.length > 0) {
        const sortedTasks = group.tasks.sort((a: any, b: any) =>
          new Date(b.lastUpdateDate).getTime() - new Date(a.lastUpdateDate).getTime()
        );

        group.status = sortedTasks[0].status;

        const firstDate: Date = new Date(sortedTasks[0].lastUpdateDate);

        isLatestTask = (latestDate == null || latestDate.getTime() - firstDate.getTime() <= 0);

        if (isLatestTask) {
          latestDate = firstDate;
        }

        if (group.status === 'Complete' && sortedTasks[0].actions.length > 0) {
          group.status = 'Warning';
        }
      }
    });
  }


  updateTaskData(page: PageResult<TaskGroup>): void {
    this.taskPage = page;

    this.taskPage.resultSet.forEach(task => {
      if (this.visible[task.collectionId]) {
        this.managementService.getTasks(task.collectionId).then(tasks => {
          this.setGroupTasks(task, tasks);
        });
      }
    });
  }

  setVisibility(taskGroup: TaskGroup): void {
    if (!this.visible[taskGroup.collectionId]) {
      this.visible[taskGroup.collectionId] = true;

      if (taskGroup.groups == null && !taskGroup.loading) {
        taskGroup.loading = true;
        this.managementService.getTasks(taskGroup.collectionId).then(tasks => {
          this.setGroupTasks(taskGroup, tasks);

          taskGroup.loading = false;
        });
      }
    }
    else {
      this.visible[taskGroup.collectionId] = false;
    }
  }


  handleMessage(message: Message): void {

    if (message.type === 'MissingMetadataMessage') {
      this.bsModalRef = this.modalService.show(MetadataModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        'class': 'upload-modal'
      });
      this.bsModalRef.content.init(message.data.collectionId);

      this.bsModalRef.content.onMetadataChange.subscribe(() => {
        this.getMessages();
      });
    }

  }

  handleGoto(collectionId: string): void {
    // let breadcrumbs = []

    this.managementService.view(collectionId).then(response => {
      const entity = response.item;
      const breadcrumbs = response.breadcrumbs;

      this.managementService.getItems(collectionId, null).then(nodes => {
        this.bsModalRef = this.modalService.show(CollectionModalComponent, {
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

        this.getMessages();

        this.totalTaskCount = this.tasks.count;

      });
  }

  getMessages(): void {
    this.onMessagePageChange(this.messages.pageNumber);
  }

  onMessagePageChange(pageNumber: number): void {
    this.managementService.getMessages(this.messages.pageSize, pageNumber).then(messages => {
      this.messages = messages;
    });
  }
}
