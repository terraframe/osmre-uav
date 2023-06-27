import { HttpClient } from '@angular/common/http';
import { Component, Input } from '@angular/core';
import { EventService } from '@shared/service/event.service';
import { ManagementService } from '@site/service/management.service';
import { BsModalService } from 'ngx-bootstrap/modal';

@Component({
  selector: 'artifact-upload',
  templateUrl: './artifact-upload.component.html',
  styleUrls: ['./artifact-upload.component.css']
})
export class ArtifactUploadComponent {
  @Input() componentId: string;
  @Input() folder: string;
  
  // Optional. If specified, we will rename the uploaded file, which is useful if the uploaded file is intended to replace an existing file.
  @Input() replaceName: string = null;
  
  uploadStatus: string = null;

  constructor(private http: HttpClient, private eventService: EventService, private managementService: ManagementService, private modalService: BsModalService) {}

  onFileSelected(event) {
    const file:File = event.target.files[0];

    if (file) {
	  let upload = this.replaceName == null ? "upload" : "reupload";
	  
	  this.managementService.upload(this.componentId, this.folder, file, this.replaceName).then(() => {
        this.uploadStatus = "success";
      }).catch(er => {
		this.uploadStatus = "error";
	  }).finally(() => {
		event.target.value = null;
	  })
    }
  }
}
