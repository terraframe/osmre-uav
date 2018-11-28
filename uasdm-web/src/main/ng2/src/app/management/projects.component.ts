import { Component, OnInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { TreeNode, TreeComponent, TREE_ACTIONS } from 'angular-tree-component';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { CreateModalComponent } from './modals/create-modal.component';
import { EditModalComponent } from './modals/edit-modal.component';
import { ConfirmModalComponent } from './modals/confirm-modal.component';
import { ErrorModalComponent } from './modals/error-modal.component';
import { SiteEntity } from './management';
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
    nodes = [] as SiteEntity[];

    /* 
     * Options to configure the tree widget, including the functions for getting children and showing the context menu
     */
    options = {
        getChildren: ( node: TreeNode ) => {
            return this.service.getChildren( node.data.id );
        },
        actionMapping: {
            mouse: {
                contextMenu: ( tree: any, node: any, $event: any ) => {
                    this.handleOnMenu( node, $event );
                },
                click : TREE_ACTIONS.TOGGLE_EXPANDED
            }
        }
    };

    /*
     * Tree component
     */
    @ViewChild( TreeComponent )
    private tree: TreeComponent;

    /*
     * Template for the delete confirmation
     */
    @ViewChild( 'confirmTemplate' ) public confirmTemplate: TemplateRef<any>;

    /*
     * Template for tree node menu
     */
    @ViewChild( 'nodeMenu' ) public nodeMenuComponent: ContextMenuComponent;

    /*
     * Template for leaf menu
     */
    @ViewChild( 'leafMenu' ) public leafMenuComponent: ContextMenuComponent;

    /* 
     * Currently clicked on id for delete confirmation modal 
     */
    current: TreeNode;

    constructor( private service: ManagementService, private modalService: BsModalService, private contextMenuService: ContextMenuService ) { }

    ngOnInit(): void {
        this.service.roots().then( nodes => {
            this.nodes = nodes;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }
    

    handleOnMenu( node: any, $event: any ): void {
    	
    	if(node.data.typeLabel === "Site"){
    		node.data.childType = "Project"
    	}
    	else if(node.data.typeLabel === "Project"){
    		node.data.childType = "Mission"
    	}
    	else if(node.data.typeLabel === "Mission"){
    		node.data.childType = "Collection"
    	}
    	else if(node.data.typeLabel === "Collection"){
    		node.data.childType = null
    	}
    	
    	
        this.contextMenuService.show.next( {
            contextMenu: (node.data.childType !== null ? this.nodeMenuComponent : this.leafMenuComponent),
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
            this.bsModalRef.content.entity = data;
            this.bsModalRef.content.parentId = parent.data.id;

            ( <CreateModalComponent>this.bsModalRef.content ).onNodeChange.subscribe( entity => {
                const d = this.current.data;

                if ( d.children != null ) {
                    d.children.push( entity );
                }
                else {
                    d.children = [entity];
                    d.hasChildren = true;
                }

                this.tree.treeModel.update();
            } );
        } ).catch(( err: any ) => {
            this.error( err.json() );
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
            this.bsModalRef.content.entity = data;

            ( <EditModalComponent>this.bsModalRef.content ).onNodeChange.subscribe( entity => {
                // Do something
                this.current.data = entity;
            } );
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    handleDelete( node: TreeNode ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = 'Are you sure you want to delete [' + node.data.name + ']';
        this.bsModalRef.content.data = node;

        ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
            this.remove( data );
        } );
    }

    remove( node: TreeNode ): void {
        this.service.remove( node.data.id ).then( response => {
            const parent = node.parent;
            let children = parent.data.children;

            parent.data.children = children.filter(( n: any ) => n.id !== node.data.id );

            if ( parent.data.children.length === 0 ) {
                parent.data.hasChildren = false;
            }

            this.tree.treeModel.update();
        } ).catch(( err: any ) => {
            this.error( err.json() );
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
