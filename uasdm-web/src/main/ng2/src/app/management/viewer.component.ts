import { Component, OnInit, AfterViewInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TreeNode, TreeComponent, TREE_ACTIONS } from 'angular-tree-component';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';
import { saveAs as importedSaveAs } from "file-saver";
import { LngLat, Map } from 'mapbox-gl';
import * as MapboxDraw from 'mapbox-gl-draw';

import { ConfirmModalComponent } from './modals/confirm-modal.component';
import { ErrorModalComponent } from './modals/error-modal.component';
import { SiteEntity } from './management';
import { ManagementService } from './management.service';
import { MapService } from '../service/map.service';

@Component( {
    selector: 'viewer',
    templateUrl: './viewer.component.html',
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

    map: Map;

    draw: MapboxDraw;

    constructor( private service: ManagementService, private mapService: MapService, private modalService: BsModalService, private contextMenuService: ContextMenuService, private route: ActivatedRoute ) { }

    ngOnInit(): void {
        let id = this.route.snapshot.paramMap.get( 'id' );

        this.service.roots( id ).then( nodes => {
            this.nodes = nodes;

            if ( id != null ) {
                setTimeout(() => {
                    if ( this.tree ) {
                        let node = this.tree.treeModel.getNodeById( id );
                        node.setActiveAndVisible();
                        node.expand();
                    }
                }, 20 );
            }

        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    ngAfterViewInit() {
        this.map = new Map( {
            container: 'map',
            style: 'mapbox://styles/mapbox/light-v9',
            zoom: 5,
            center: [-78.880453, 42.897852]
        } );

        this.draw = new MapboxDraw( {
            displayControlsDefault: false,
            controls: {
                point: true
            }
        } );

        this.mapService.features().then( geojson => {


            this.map.addLayer( {
                "id": "points",
                "type": "circle",
                "source": geojson,
                "paint": {
                    "circle-radius": 20,
                    "circle-color": '#3bb2d0',
                    "circle-stroke-width": 2,
                    "circle-stroke-color": '#223b53'
                },
                "layout": {
                    "text-field": "{name}",
                    "text-font": ["Open Sans Semibold", "Arial Unicode MS Bold"],
                    "text-offset": [0, 0.6],
                    "text-anchor": "top"
                }
            } );

        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    isData( node: any ): boolean {

        if ( node.data.typeLabel === "Site" ) {
            return false;
        }
        else if ( node.data.typeLabel === "Project" ) {
            return false;
        }
        else if ( node.data.typeLabel === "Mission" ) {
            return false;
        }
        else if ( node.data.typeLabel === "Collection" ) {
            return false;
        }
        else if ( node.data.type === "folder" ) {
            return false;
        }
        else {
            return true;
        }
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
