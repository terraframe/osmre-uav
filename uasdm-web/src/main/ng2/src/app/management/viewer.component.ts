import { Component, OnInit, AfterViewInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TreeNode, TreeComponent, TREE_ACTIONS } from 'angular-tree-component';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';
import { saveAs as importedSaveAs } from "file-saver";

import { ConfirmModalComponent } from './modals/confirm-modal.component';
import { ErrorModalComponent } from './modals/error-modal.component';
import { SiteEntity } from './management';
import { ManagementService } from './management.service';

@Component( {
    selector: 'viewer',
    templateUrl: './projects.component.html',
    styleUrls: []
} )
export class ViewerComponent implements OnInit, AfterViewInit {

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    /* 
     * Root nodes of the tree
     */
    nodes = [] as SiteEntity[];

    /* 
     * Options to configure the tree widget, including the functions for getting children and showing the context menu
     */
    options = {
        getChildren: ( node: TreeNode ) => {
            if ( node.data.type === "folder" ) {
                return this.service.getItems( node.data.component, node.data.name );
            }
            else if ( node.data.type === "object" ) {
                // Do nothing there are no children
                //                return this.service.getItems( node.data.id, node.data.name );
            }
            else {
                return this.service.getItems( node.data.id, null );
            }
        },
        actionMapping: {
            mouse: {
                contextMenu: ( tree: any, node: any, $event: any ) => {
                    this.handleOnMenu( node, $event );
                },
                click: TREE_ACTIONS.TOGGLE_EXPANDED
            }
        },
        allowDrag: false,
        allowDrop: false
    };

    /*
     * Tree component
     */
    @ViewChild( TreeComponent )
    private tree: TreeComponent;

    /*
     * Template for leaf menu
     */
    @ViewChild( 'objectMenu' ) public objectMenuComponent: ContextMenuComponent;

    /* 
     * Currently clicked on id for delete confirmation modal 
     */
    current: TreeNode;

    constructor( private service: ManagementService, private modalService: BsModalService, private contextMenuService: ContextMenuService, private route: ActivatedRoute ) { }

    ngOnInit(): void {
        let id = this.route.snapshot.paramMap.get( 'id' );

        this.service.roots( id ).then( nodes => {
            this.nodes = nodes;

            if ( id != null ) {
                setTimeout(() => {
                    if ( this.tree ) {
                        let node = this.tree.treeModel.getNodeById(id);
                        node.setActiveAndVisible();
                        node.expand();
                    }
                }, 1000 );
            }

        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    ngAfterViewInit() {

        //        setTimeout(() => {
        //            if ( this.tree ) {
        //                this.tree.treeModel.expandAll();
        //            }
        //        }, 1000 )
    }


    handleOnMenu( node: any, $event: any ): void {

        if ( node.data.type === "object" ) {
            this.contextMenuService.show.next( {
                contextMenu: this.objectMenuComponent,
                event: $event,
                item: node,
            } );
            $event.preventDefault();
            $event.stopPropagation();
        }
    }


    handleDownload( node: TreeNode ): void {
        this.service.download( node.data.component, node.data.key ).subscribe( blob => {
            importedSaveAs( blob, node.data.name );
        } );
    }


    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }

    }
}
