import { Component, OnInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { TreeNode, TreeComponent } from 'angular-tree-component';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService } from 'ngx-contextmenu';

import { CreateModalComponent } from './modals/create-modal.component';
import { EditModalComponent } from './modals/edit-modal.component';
import { ManagementService } from './management.service';

@Component( {
    selector: 'projects',
    templateUrl: './projects.component.html',
    styleUrls: []
} )
export class ProjectsComponent implements OnInit {

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    /* 
     * Root nodes of the tree
     */
    nodes = [] as TreeNode[];

    /* 
     * Options to configure the tree widget, including the functions for getting children and showing the context menu
     */
    options = {
        getChildren: ( node: TreeNode ) => {
            return this.service.getChildren( node );
        },
        actionMapping: {
            mouse: {
                contextMenu: ( tree: any, node: any, $event: any ) => {
                    this.handleOnMenu( node, $event );
                }
            }
        }
    };

    /*
     * Tree component
     */
    @ViewChild( TreeComponent )
    private tree: TreeComponent;

    /*
     * Template for the delete confrimation
     */
    @ViewChild( 'confirmTemplate' ) public confirmTemplate: TemplateRef<any>;

    /* 
     * Currently clicked on id for delete confirmation modal 
     */
    current: TreeNode;

    constructor( private service: ManagementService, private modalService: BsModalService, private contextMenuService: ContextMenuService ) { }

    ngOnInit(): void {
        this.service.roots().then( nodes => {
            this.nodes = nodes;
        } );
    }

    handleOnMenu( node: any, $event: any ): void {
        this.contextMenuService.show.next( {
            event: $event,
            item: node,
        } );
        $event.preventDefault();
        $event.stopPropagation();
    }

    handleCreate( parent: TreeNode ): void {
        this.current = parent;

        this.service.newChild( parent.data.id ).then( data => {
            this.bsModalRef = this.modalService.show( CreateModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'upload-modal'
            } );
            this.bsModalRef.content.node = data;
            this.bsModalRef.content.parentId = parent.data.id;

            ( <CreateModalComponent>this.bsModalRef.content ).onNodeChange.subscribe( rData => {
                const d = this.current.data;

                if ( d.children != null ) {
                    d.children.push( rData );
                }
                else {
                    d.children = [rData];
                    d.hasChildren = true;
                }

                this.tree.treeModel.update();
            } );
        } );
    }

    handleEdit( node: TreeNode ): void {
        this.current = node;

        const data = node.data;

        this.service.edit( data.id ).then( data => {
            this.bsModalRef = this.modalService.show( EditModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'upload-modal'
            } );
            this.bsModalRef.content.node = data;

            ( <EditModalComponent>this.bsModalRef.content ).onNodeChange.subscribe( rData => {
                // Do something
                this.current.data = data;
            } );
        } );
    }

    handleDelete( node: TreeNode ): void {
        this.current = node;

        this.bsModalRef = this.modalService.show( this.confirmTemplate, { class: 'modal-sm' } );
    }

    confirm(): void {
        this.bsModalRef.hide();

        this.service.remove( this.current.data.id ).then( response => {
            const parent = this.current.parent;
            let children = parent.data.children;

            parent.data.children = children.filter(( node: any ) => node.id !== this.current.data.id );

            this.tree.treeModel.update();
        } );
    }
}
