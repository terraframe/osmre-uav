import { Component, OnInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';
import { Subject } from 'rxjs/Subject';

import { MetadataModalComponent } from './modals/metadata-modal.component';
import { ErrorModalComponent } from './modals/error-modal.component';
import { Message, Task } from './management';
import { ManagementService } from './management.service';
import { SearchService } from './search.service';

@Component( {
    selector: 'search',
    templateUrl: './search.component.html',
    styleUrls: []
} )
export class SearchComponent implements OnInit {

	results: Object;
	searchTerm$ = new Subject<string>();
	
    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;
	
	constructor(private searchService: SearchService, private modalService: BsModalService) {
	    this.searchService.search(this.searchTerm$)
	      .subscribe(results => {
	    	  
	    	let hierarchyStr = "";
	    	results.forEach( (result:any) => {
	    		
	    		result.hierarchy.forEach( (h:any) => {
	    			hierarchyStr += "/" + h.label;
	    		})
	    		
	    		result.hierarchyStr = hierarchyStr;
	    	})
	    	
	        this.results = results;
	      });
	}
	
	ngOnInit(): void {
		
	}

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }

    }
}
