import { Component, OnInit, AfterViewInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition,
} from '@angular/animations';
import { TreeNode, TreeComponent, TREE_ACTIONS } from 'angular-tree-component';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';
import { saveAs as importedSaveAs } from "file-saver";
import { Map, LngLatBounds, NavigationControl, ImageSource } from 'mapbox-gl';
import * as MapboxDraw from '@mapbox/mapbox-gl-draw';
import * as StaticMode from '@mapbox/mapbox-gl-draw-static-mode';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';

import { CreateModalComponent } from './modals/create-modal.component';
import { ImagePreviewModalComponent } from './modals/image-preview-modal.component';
import { EditModalComponent } from './modals/edit-modal.component';
import { ConfirmModalComponent } from './modals/confirm-modal.component';
import { ErrorModalComponent } from './modals/error-modal.component';
import { SiteEntity } from '../model/management';
import { ManagementService } from '../service/management.service';
import { MapService } from '../service/map.service';
import { AuthService } from '../service/auth.service';

declare var acp: any;

@Component( {
    selector: 'projects',
    templateUrl: './projects.component.html',
    styles: [],
    animations: [
        trigger('fadeIn', [
            transition(':enter', [
                style({ opacity: '0' }),
                animate('.25s ease-out', style({ opacity: '1' })),
            ]),
        ])
    ]
} )
export class ProjectsComponent implements OnInit, AfterViewInit {

    images: any[] = [];
    showImagePanel = false;
    // imageToShow: any;
    thumbnails: any = {};

    /* 
     * Options to configure the tree widget, including the functions for getting children and showing the context menu
     */
    options = {
        getChildren: ( node: TreeNode ) => {
            if ( node.data.type === "folder" ) {
                return this.service.getItems( node.data.component, node.data.name )
                // return []; // preventing the 'Loading...' message
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
                click:  ( tree: any, node: any, $event: any ) => {

                    if (node.data.type === "folder") {

                        // clear any existing images
                        this.images = [];

                        node.toggleExpanded();
                        this.showImagePanel = false;

                        if (!node.isCollapsed) {

                            // open the panel immediatly
                            this.showImagePanel = true;

                            this.service.getItems(node.data.component, node.data.name)
                                .then(items => {
                                    //this.images = [items[0]]; // not yet handling different types of files

                                    // this.images = items;

                                    for (let i = 0; i < items.length; ++i) {
                                        let item = items[i];

                                        if(item.name.toLowerCase().indexOf(".png") !== -1 || item.name.toLowerCase().indexOf(".jpg") !== -1 || 
                                            item.name.toLowerCase().indexOf(".jpeg") !== -1 || item.name.toLowerCase().indexOf(".tif") !== -1 ||
                                            item.name.toLowerCase().indexOf(".tiff") !== -1) {
                                            
                                            this.images.push(item);
                                        }
                                    }

                                    this.images.forEach(image => {
                                        this.getThumbnail(image);
                                    })

                                }).catch((err: any) => {
                                    this.error(err.json());
                                });
                        }
                    }
                    else if (node.data.type === "object") {
                        // clicked on raw file. do nothing.
                    }
                    else {
                        node.toggleExpanded();

                        this.images = [];

                        this.showImagePanel = false;
                    }

                }
            }
        },
        animateExpand: true,
        animateSpeed: 5000,
        animateAcceleration: 1,
        allowDrag: false,
        allowDrop: false,
        scrollContainer: document.getElementById('hierarchy-tree-container')
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
     * Template for folder node menu
     */
    @ViewChild( 'folderMenu' ) public folderMenuComponent: ContextMenuComponent;

    /*
     * Template for site items
     */
    @ViewChild( 'siteMenu' ) public siteMenuComponent: ContextMenuComponent;

    /*
     * Template for leaf menu
     */
    @ViewChild( 'leafMenu' ) public leafMenuComponent: ContextMenuComponent;

    /*
     * Template for worker only options
     */
    @ViewChild( 'workerNodeMenu' ) public workerNodeMenu: ContextMenuComponent;

    /*
     * Template for object items
     */
    @ViewChild( 'objectMenu' ) public objectMenuComponent: ContextMenuComponent;

    /*
     * Template for image items
     */
    @ViewChild( 'imageMenu' ) public imageMenuComponent: ContextMenuComponent;

    /* 
     * Datasource to get search responses
     */
    dataSource: Observable<any>;

    /* 
     * Model for text being searched
     */
    search: string;

    /* 
     * Root nodes of the tree
     */
    nodes = [] as SiteEntity[];

    /* 
     * Currently clicked on id
     */
    current: TreeNode;

    /* 
     * mapbox-gl map
     */
    map: Map;

    /* 
     * Draw control
     */
    draw: MapboxDraw;

    /* 
     * Flag denoting if the user is an admin
     */
    admin: boolean = false;

    /* 
     * Flag denoting if the user is a worker
     */
    worker: boolean = false;

    /* 
     * Flag denoting the draw control is active
     */
    active: boolean = false;

    /* 
     * List of base layers
     */
    baseLayers: any[] = [{
        label: 'Outdoors',
        id: 'outdoors-v11',
        selected: true
    }, {
        label: 'Satellite',
        id: 'satellite-v9'
    }, {
        label: 'Streets',
        id: 'streets-v11'
    }];

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    constructor( private service: ManagementService, private authService: AuthService, private mapService: MapService, private modalService: BsModalService, private contextMenuService: ContextMenuService ) {
        this.dataSource = Observable.create(( observer: any ) => {
            this.service.searchEntites( this.search ).then( results => {
                observer.next( results );
            } );
        } );
    }

    ngOnInit(): void {
        this.admin = this.authService.isAdmin();
        this.worker = this.authService.isWorker();

        this.service.roots( null ).then( nodes => {
            this.nodes = nodes;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    ngAfterViewInit() {

        setTimeout(() => {
            if ( this.tree ) {
                this.tree.treeModel.expandAll();
            }
        }, 1000 );

        this.map = new Map( {
            container: 'map',
            style: 'mapbox://styles/mapbox/outdoors-v11',
            zoom: 2,
            center: [-78.880453, 42.897852]
        } );

        this.map.on( 'load', () => {
            this.initMap();
        } );

    }

    initMap(): void {

        this.map.on( 'style.load', () => {
            this.addLayers();
            this.refresh( false );
        } );

        this.addLayers();

        this.refresh( true );

        // Add zoom and rotation controls to the map.
        this.map.addControl( new NavigationControl() );

        if ( this.admin ) {
            let modes = MapboxDraw.modes;
            modes.static = StaticMode;

            this.draw = new MapboxDraw( {
                modes: modes,
                displayControlsDefault: false,
                controls: {
                    static: true
                }
            } );

            this.map.addControl( this.draw );

            this.map.on( "draw.update", ( $event ) => { this.onDrawUpdate( $event ) } );
            this.map.on( "draw.create", ( $event ) => { this.onDrawCreate( $event ) } );
            this.map.on( "draw.modechange", ( $event ) => { this.onDrawUpdate( $event ) } );
        }
    }

    addLayers(): void {

        this.map.addSource( 'sites', {
            type: 'geojson',
            data: {
                "type": "FeatureCollection",
                "features": []
            }
        } );

        // Point layer
        this.map.addLayer( {
            "id": "points",
            "type": "circle",
            "source": 'sites',
            "paint": {
                "circle-radius": 10,
                "circle-color": '#800000',
                "circle-stroke-width": 2,
                "circle-stroke-color": '#FFFFFF'
            }
        } );

        // Label layer
        this.map.addLayer( {
            "id": "points-label",
            "source": 'sites',
            "type": "symbol",
            "paint": {
                "text-color": "black",
                "text-halo-color": "#fff",
                "text-halo-width": 2
            },
            "layout": {
                "text-field": "{name}",
                "text-font": ["Open Sans Semibold", "Arial Unicode MS Bold"],
                "text-offset": [0, 0.6],
                "text-anchor": "top",
                "text-size": 12,
            }
        } );
    }

    refresh( zoom: boolean ): void {
        this.mapService.features().then( data => {
            ( <any>this.map.getSource( 'sites' ) ).setData( data.features );

            if ( zoom ) {
                let bounds = new LngLatBounds( [data.bbox[0], data.bbox[1]], [data.bbox[2], data.bbox[3]] );

                this.map.fitBounds( bounds, { padding: 50 } );
            }
        } );
    }

    onDrawUpdate( event: any ): void {
        if ( event.action === 'move' && event.features != null && event.features.length > 0 ) {
            this.updateGeometry( event.features[0] )
        }
    }

    onDrawCreate( event: any ): void {
        if ( event.features != null && event.features.length > 0 ) {

            let feature = event.features[0];
            feature.id = this.current.data.id;

            this.updateGeometry( feature )
        }
    }

    updateGeometry( feature: any ): void {
        let index = this.nodes.findIndex( node => {
            return node.id === feature.id;
        } );

        if ( index !== -1 ) {
            let entity = { ...this.nodes[index] };
            entity.geometry = feature.geometry;

            this.service.update( entity ).then( node => {
                this.nodes[index] = node;
                this.current.data = node;

                this.refresh( false );
            } ).catch(( err: any ) => {
                this.error( err.json() );
            } );
        }

        this.draw.deleteAll();

        // Most be after the draw has been added to trigger a repaint of the map
        this.map.setFilter( "points" );
        this.map.setFilter( "points-label" );
        this.active = false;
    }

    cancelDraw(): void {
        this.draw.deleteAll();
        this.draw.changeMode( 'static' );

        // Most be after the draw has been added to trigger a repaint of the map
        this.map.setFilter( "points" );
        this.map.setFilter( "points-label" );
        this.active = false;
    }


    isData( node: any ): boolean {

        if ( node.data.type === "Site" ) {
            return false;
        }
        else if ( node.data.type === "Project" ) {
            return false;
        }
        else if ( node.data.type === "Mission" ) {
            return false;
        }
        else if ( node.data.type === "Collection" ) {
            return false;
        }
        else {
            return true;
        }
    }


    handleOnUpdateData(): void {
        //        this.tree.treeModel.expandAll();
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
        else if ( node.data.type !== "folder" ) {
            if ( node.data.type === "Site" ) {
                node.data.childType = "Project"
            }
            else if ( node.data.type === "Project" ) {
                node.data.childType = "Mission"
            }
            else if ( node.data.type === "Mission" ) {
                node.data.childType = "Collection"
            }
            else if ( node.data.type === "Collection" ) {
                node.data.childType = null
            }

            if ( node.data.type !== "Site" || this.admin ) {
                this.contextMenuService.show.next( {
                    contextMenu: this.nodeMenuComponent,
                    event: $event,
                    item: node,
                } );
                $event.preventDefault();
                $event.stopPropagation();
            }

        }
        else
        {
          this.contextMenuService.show.next( {
            contextMenu: this.folderMenuComponent,
            event: $event,
            item: node
          } );
          $event.preventDefault();
          $event.stopPropagation();
        }
    }

    handleCreate( parent: TreeNode ): void {
        this.current = parent;

        let parentId = parent != null ? parent.data.id : null;

        this.service.newChild( parentId ).then( data => {
            this.bsModalRef = this.modalService.show( CreateModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'upload-modal'
            } );
            this.bsModalRef.content.entity = data.item;
            this.bsModalRef.content.attributes = data.attributes;

            if ( parent != null ) {
                this.bsModalRef.content.parentId = parent.data.id;
            }

            this.bsModalRef.content.onNodeChange.subscribe( entity => {

                if ( this.current != null ) {
                    let d = this.current.data;

                    if ( d.children != null ) {
                        d.children.push( entity );
                    }
                    else {
                        d.children = [entity];
                        d.hasChildren = true;
                    }

                    if ( this.tree ) {
                        this.tree.treeModel.update();
                    }
                }
                else {
                    this.nodes.push( entity );

                    if ( this.tree ) {
                        this.tree.treeModel.update();
                    }

                    this.refresh( false );
                }
            } );
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    handleEditGeom( node: TreeNode ): void {
        this.current = node;

        if ( this.current.data.geometry != null ) {
            let feature = {
                id: node.data.id,
                type: 'Feature',
                properties: {
                    oid: node.data.id,
                    name: node.data.name
                },
                geometry: node.data.geometry
            };

            this.draw.add( feature );
            this.draw.changeMode( 'simple_select', { featureIds: [feature.id] } );
        }
        else {
            this.draw.changeMode( 'draw_point', {} );
        }

        this.active = true;

        // Most be after the draw has been added to trigger a repaint of the map
        this.map.setFilter( "points", ["==", "id", ""] );
        this.map.setFilter( "points-label", ["==", "id", ""] );
    }

    zoomToFeature( node: TreeNode ): void {
        if ( node.data.geometry != null ) {
            this.map.flyTo( {
                center: node.data.geometry.coordinates
            } );
        }
    }



    handleEdit( node: TreeNode ): void {
        this.current = node;

        let data = node.data;

        this.service.edit( data.id ).then( data => {
            this.bsModalRef = this.modalService.show( EditModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'upload-modal'
            } );
            this.bsModalRef.content.entity = data.item;
            this.bsModalRef.content.attributes = data.attributes;

            ( <EditModalComponent>this.bsModalRef.content ).onNodeChange.subscribe( entity => {
                // Do something
                this.current.data = entity;
            } );
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }
    
    handleRunOrtho( node: TreeNode ): void {
      this.current = node;

      let data = node.data;

      this.service.runOrtho( data.id ).then( data => {
        alert("Your ortho task is running."); // TODO : Handle this better
      } ).catch(( err: any ) => {
          this.error( err.json() );
      } );
    }
    
    handleDownloadAll( node: TreeNode ): void {
      this.current = node;

      let data = node.data;

      window.location.href = acp + '/project/download-all?id=' + node.data.component +"&key=" + node.data.name;
      
//      this.service.downloadAll( data.id ).then( data => {
//        
//      } ).catch(( err: any ) => {
//          this.error( err.json() );
//      } );
    }
    
    handleDelete( node: TreeNode ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = 'Are you sure you want to delete [' + node.data.name + ']?';
        this.bsModalRef.content.data = node;
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = 'Delete';

        ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
            this.remove( data );
        } );
    }

    remove( node: TreeNode ): void {
      console.log("Remove on, ", node);
      console.log("id is", node.data.id);
    
      if ( node.data.type === "object" )
      {
        this.service.removeObject( node.data.component, node.data.key ).then( response => {
            let parent = node.parent;
            let children = parent.data.children;

            console.log("children = ", parent.data.children);
            parent.data.children = children.filter(( n: any ) => n.id !== node.data.id );

            if ( parent.data.children.length === 0 ) {
                parent.data.hasChildren = false;
            }

            this.tree.treeModel.update();
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
      }
      else
      {
        this.service.remove( node.data.id ).then( response => {
            if ( node.data.type !== 'Site' ) {
                let parent = node.parent;
                let children = parent.data.children;

                parent.data.children = children.filter(( n: any ) => n.id !== node.data.id );

                if ( parent.data.children.length === 0 ) {
                    parent.data.hasChildren = false;
                }
            }
            else {
                this.nodes = this.nodes.filter(( n: any ) => n.id !== node.data.id );

                this.refresh( false );
            }

            this.tree.treeModel.update();
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
      }
    }


    handleDownload( node: TreeNode ): void {
        window.location.href = acp + '/project/download?id=' + node.data.component +"&key=" + node.data.key;

        //this.service.download( node.data.component, node.data.key, true ).subscribe( blob => {
        //    importedSaveAs( blob, node.data.name );
        //} );
    }

    handleImageDownload( image: any ): void {
        window.location.href = acp + '/project/download?id=' + image.component +"&key=" + image.key;

        //this.service.download( node.data.component, node.data.key, true ).subscribe( blob => {
        //    importedSaveAs( blob, node.data.name );
        //} );
    }


    createImageFromBlob(image: Blob, imageData: any) {
        let reader = new FileReader();
        reader.addEventListener("load", () => {
            // this.imageToShow = reader.result;
            this.thumbnails[imageData.key] = reader.result;
        }, false);

        if (image) {
            reader.readAsDataURL(image);
        }
    }

    getThumbnail(image: any): void {

        let rootPath: string = image.key.substr(0, image.key.lastIndexOf("/"));
        let fileName: string = /[^/]*$/.exec(image.key)[0];
        let thumbKey: string = rootPath + "/thumbnails/" + fileName;

        this.service.download(image.component, thumbKey, false).subscribe(blob => {
            this.createImageFromBlob(blob, image);
        }, error => {
            console.log(error);
        });
    }



    handleStyle( layer: any ): void {

        this.baseLayers.forEach( baseLayer => {
            baseLayer.selected = false;
        } );

        layer.selected = true;

        this.map.setStyle( 'mapbox://styles/mapbox/' + layer.id );
    }

    highlight( match: any, query: string[] | string ): string {
        console.log( match );

        return 'Test';
    }

    handleClick( $event: any ): void {
        let result = $event.item;
        let id = result.hierarchy[result.hierarchy.length - 1].id;

        if ( id != null ) {
            let node = this.tree.treeModel.getNodeById( id );

            if ( node != null ) {
                node.setActiveAndVisible();
                node.expand();
            }
            else {
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
        }
    }

    previewImage(event:any, image:any): void {
        this.bsModalRef = this.modalService.show( ImagePreviewModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'image-preview-modal'
            } );
            this.bsModalRef.content.image = image;
            this.bsModalRef.content.src = event.target.src;
    }

    getDefaultImgURL(event: any): void {
        event.target.src = acp + "/net/geoprism/images/thumbnail-default.png";
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }

    /*
     *  Context menu visibility functions
     */
    public canEdit = ( item: any ): boolean => {
        if ( this.admin ) {
            return true;
        }
        else if ( this.worker ) {
            return ( item.data.type === "Mission" || item.data.type === "Collection" );
        }

        return false;
    }
    
    public canRunOrtho = ( item: any ): boolean => {
      if (item.data.type !== "Collection")
      {
        return false;
      }
      
      return true;
      
      // TODO : If we don't have raw images uploaded then they can't run ortho
      
      // TODO : Different roles?
//      if ( this.admin ) {
//        return true;
//      }
//
//      return false;
  }

    public canDelete = ( item: any ): boolean => {
        if ( this.admin ) {
            return true;
        }

        return false;
    }

    public canAddChild = ( item: any ): boolean => {
        if ( this.admin && item.data.type !== "Collection" ) {
            return true;
        }
        else if ( this.worker && ( item.data.type === "Project" || item.data.type === "Mission" ) ) {
            return true;
        }

        return false;
    }

    public canEditSite = ( item: any ): boolean => {
        return item.data.type === "Site" && this.canEdit( item );
    }

    public isSite = ( item: any ): boolean => {
        return item.data.type === "Site";
    }
}
