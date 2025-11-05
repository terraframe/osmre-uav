///
///
///

import { HttpClient } from '@angular/common/http';
import { Component, Input } from '@angular/core';
import { EventService } from '@shared/service/event.service';
import { ManagementService } from '@site/service/management.service';
import { BsModalService } from 'ngx-bootstrap/modal';

@Component({
  standalone: false,
  selector: 'artifact-upload',
  templateUrl: './artifact-upload.component.html',
  styleUrls: ['./artifact-upload.component.css']
})
export class ArtifactUploadComponent {
  @Input() componentId: string;
  @Input() productName: string;
  @Input() folder: string;
  
  // Optional. If specified, we will tell the user they are reuploading to a file that already exists.
  @Input() label: string = "Upload";
  
  // Optional. If specified, we will rename the uploaded file, which is useful if the uploaded file is intended to replace an existing file.
  @Input() replaceName: string = null;
  
  // Optional. If specified, we will direct the user to upload a file of one of the specified file types. Multiple types can be provided with a comma. Example: .xls,.xlsx
  @Input() accept: string = "";
  
  uploadStatus: string = null;

  constructor(private http: HttpClient, private eventService: EventService, private managementService: ManagementService, private modalService: BsModalService) {}

  onFileSelected(event) {
    const file:File = event.target.files[0];

    if (file) {
	  this.managementService.upload(this.componentId, this.productName, this.folder, file, this.replaceName).then(() => {
        this.uploadStatus = "success";
      }).catch(er => {
		this.uploadStatus = "error";
	  }).finally(() => {
		event.target.value = null;
	  })
    }
  }
}
