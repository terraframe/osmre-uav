import { Component, OnInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { Router } from '@angular/router';
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

    constructor( private searchService: SearchService, private modalService: BsModalService, private router: Router ) {
        this.searchService.search( this.searchTerm$ )
            .subscribe( results => {
                this.results = results;
            } );
    }

    ngOnInit(): void {

    }

    handleClick( $event: any, result: any ): void {
        this.router.navigate( ['viewer', result.hierarchy[result.hierarchy.length - 1].id] );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }

    }
}
