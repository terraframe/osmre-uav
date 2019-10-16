import { Component, OnInit, OnDestroy, AfterViewInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { CollapseModule } from 'ngx-bootstrap/collapse';
import { TabsetComponent } from 'ngx-bootstrap';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Map, LngLatBounds, NavigationControl, ImageSource, MapboxEvent } from 'mapbox-gl';
import { Observable } from 'rxjs/Observable';

import { BasicConfirmModalComponent } from '../../shared/component/modal/basic-confirm-modal.component';
import { AuthService } from '../../shared/service/auth.service';

import { SiteEntity, Product } from '../model/management';

import { EntityModalComponent } from './modal/entity-modal.component';
import { UploadModalComponent } from './modal/upload-modal.component';
import { LeafModalComponent } from './modal/leaf-modal.component';

import { ManagementService } from '../service/management.service';
import { MapService } from '../service/map.service';
import { MetadataService } from '../service/metadata.service';

import { 
    fadeInOnEnterAnimation,
    fadeOutOnLeaveAnimation
 } from 'angular-animations';


declare var acp: any;
declare var gpAppType: any;

@Component( {
    selector: 'projects',
    templateUrl: './projects.component.html',
    styles: ['./projects.css'],
    animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation()
    ]
} )
export class ProjectsComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild('staticTabs') staticTabs: TabsetComponent;

    // imageToShow: any;
    userName: string = "";

    /*
     * Template for the delete confirmation
     */
    @ViewChild( 'confirmTemplate' ) public confirmTemplate: TemplateRef<any>;

    /* 
     * Datasource to get search responses
     */
    dataSource: Observable<any>;

    /* 
     * Model for text being searched
     */
    search: string = "";

    /* 
     * Root nodes of the tree
     */
    nodes = [] as SiteEntity[];

    /* 
     * Root nodes of the tree
     */
    supportingData = [] as SiteEntity[];

    /* 
     * Breadcrumb of previous sites clicked on
     */
    breadcrumbs = [] as SiteEntity[];

    /* 
     * Root nodes of the tree
     */
    current: SiteEntity;

    /* 
     * mapbox-gl map
     */
    map: Map;

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

    layers: any[] = [];

    baselayerIconHover = false;

    hoverFeatureId: string;

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    constructor( private service: ManagementService, private authService: AuthService, private mapService: MapService,
        private modalService: BsModalService, private metadataService: MetadataService, private collapseModule: CollapseModule ) {

        this.dataSource = Observable.create(( observer: any ) => {

            this.mapService.mbForwardGeocode( this.search ).then( response => {
                const match = response.features;

                this.service.searchEntites( this.search ).then( results => {

                    // Add Mapbox results to any local results
                    match.forEach( obj => {
                        let newObj = {
                            id: obj.id,
                            hierarchy: [],
                            label: obj.place_name,
                            center: obj.center,
                            source: "MAPBOX"
                        }

                        results.push( newObj );
                    } );

                    observer.next( results );
                } );
            } );
        } );
    }

    ngOnInit(): void {
        this.admin = this.authService.isAdmin();
        this.worker = this.authService.isWorker();
        this.userName = this.service.getCurrentUser();
    }

    ngOnDestroy(): void {
        this.map.remove();
    }

    ngAfterViewInit() {

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

        this.map.on( 'mousemove', e => {
            // e.point is the x, y coordinates of the mousemove event relative
            // to the top-left corner of the map.
            // e.lngLat is the longitude, latitude geographical position of the event
            let coord = e.lngLat.wrap();

            // EPSG:3857 = WGS 84 / Pseudo-Mercator
            // EPSG:4326 = WGS 84 
            // let coord4326 = window.proj4(window.proj4.defs('EPSG:3857'), window.proj4.defs('EPSG:4326'), [coord.lng, coord.lat]);
            // let text = "Long: " + coord4326[0] + " Lat: " + coord4326[1];

            let text = "Lat: " + coord.lat + " Long: " + coord.lng;
            let mousemovePanel = document.getElementById( "mousemove-panel" );
            mousemovePanel.textContent = text;


            let features = this.map.queryRenderedFeatures(e.point, { layers: ['points'] });
            
            if(features.length > 0){
                let focusFeatureId = features[0].properties.oid; // just the first
                this.map.setFilter('hover-points', [ 'all',
                    [ '==', 'oid', focusFeatureId ]
                ])

                this.highlightListItem(focusFeatureId)
            }
            else {
                this.map.setFilter('hover-points', [ 'all',
                    [ '==', 'oid', "NONE" ]
                ])

                this.clearHighlightListItem();
            }
 
        } );

        this.map.on( 'zoomend', ( e ) => {
            this.handleExtentChange( e );
        } );

        this.map.on( 'moveend', ( e ) => {
            this.handleExtentChange( e );
        } );

        // MapboxGL doesn't have a good way to detect when moving off the map
        let sidebar = document.getElementById( "navigator-left-sidebar" );
        sidebar.addEventListener( "mouseenter", function() {
            let mousemovePanel = document.getElementById( "mousemove-panel" );
            mousemovePanel.textContent = "";
        } );
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

        // Hover style
        this.map.addLayer( {
            "id": "hover-points",
            "type": "circle",
            "source": 'sites',
            "paint": {
                "circle-radius": 13,
                "circle-color": '#cf0000',
                "circle-stroke-width": 2,
                "circle-stroke-color": '#FFFFFF'
            },
            filter: [ 'all',
                [ '==', 'id', 'NONE' ] // start with a filter that doesn't select anything
            ]
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

        this.layers.forEach( imageKey => {
            this.addImageLayer( imageKey );
        } );
    }

    handleExtentChange( e: MapboxEvent<MouseEvent | TouchEvent | WheelEvent> ): void {
        if ( this.current == null ) {
            this.service.roots( null, this.map.getBounds() ).then( nodes => {
                this.nodes = nodes;
            } );
        }
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
        else if ( node.data.type === "Imagery" ) {
            return false;
        }
        else {
            return true;
        }
    }



    handleOnUpdateData(): void {
        //        this.tree.treeModel.expandAll();
    }

    //    handleOnMenu( node: any, $event: any ): void {
    //
    //        if ( node.data.type === "object" ) {
    //            this.contextMenuService.show.next( {
    //                contextMenu: this.objectMenuComponent,
    //                event: $event,
    //                item: node,
    //            } );
    //            $event.preventDefault();
    //            $event.stopPropagation();
    //        }
    //        else if ( node.data.type !== "folder" ) {
    //            if ( node.data.type === "Site" ) {
    //                node.data.childType = "Project"
    //            }
    //            else if ( node.data.type === "Project" ) {
    //                node.data.childType = "Mission"
    //            }
    //            else if ( node.data.type === "Mission" ) {
    //                node.data.childType = "Collection"
    //            }
    //            else if ( node.data.type === "Collection" ) {
    //                node.data.childType = null
    //            }
    //            else if ( node.data.type === "Imagery" ) {
    //                node.data.childType = null
    //            }
    //
    //            if ( node.data.type !== "Site" || this.admin ) {
    //                this.contextMenuService.show.next( {
    //                    contextMenu: this.nodeMenuComponent,
    //                    event: $event,
    //                    item: node,
    //                } );
    //                $event.preventDefault();
    //                $event.stopPropagation();
    //            }
    //
    //        }
    //        else {
    //            this.contextMenuService.show.next( {
    //                contextMenu: this.folderMenuComponent,
    //                event: $event,
    //                item: node
    //            } );
    //            $event.preventDefault();
    //            $event.stopPropagation();
    //        }
    //    }


    handleUploadFile( item: SiteEntity ): void {

        this.bsModalRef = this.modalService.show( UploadModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );
        this.bsModalRef.content.init( this.breadcrumbs );

        this.bsModalRef.content.onUploadComplete.subscribe( node => {
            this.service.getItems( this.current.id, null ).then( nodes => {
                this.setNodes( nodes );
            } );
        } );

        //        this.bsModalRef.content.onHierarchyChange.subscribe( () => {
        //            const metadata = this.metadataService.getMetadata( item );
        //
        //            if ( metadata.expandable ) {
        ////                if ( node.children == null || node.children.length == 0 ) {
        ////                    this.service.getItems( node.id, null ).then( nodes => {
        ////                        node.children = nodes;
        ////
        ////                        this.expand( node );
        ////                    } );
        ////                }
        ////                else {
        ////                    this.expand( node );
        ////                }
        //            }
        //            else {
        //                this.service.getItems( item.id, null ).then( nodes => {
        //                    this.setNodes( nodes );
        //                } );
        //            }
        //
        //        } );
    }


    handleCreate( parent: SiteEntity, type: string ): void {
        let parentId = parent != null ? parent.id : null;

        this.service.newChild( parentId, type ).then( data => {
            this.bsModalRef = this.modalService.show( EntityModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'upload-modal'
            } );
            this.bsModalRef.content.init( true, this.admin, data.item, data.attributes, this.map.getCenter(), this.map.getZoom() );


            if ( parent != null ) {
                this.bsModalRef.content.parentId = parent.id;
            }

            this.bsModalRef.content.onNodeChange.subscribe( entity => {

                if ( parent != null ) {

                }
                else {
                    if ( this.breadcrumbs.length == 0 ) {
                        this.nodes.push( entity );
                    }

                    this.refresh( false );
                }
            } );
        } );
    }

    zoomToFeature( node: SiteEntity ): void {
        if ( node.geometry != null ) {
            this.map.flyTo( {
                center: node.geometry.coordinates
            } );
        }
    }

    handleEdit( node: SiteEntity, event: any ): void {

        event.stopPropagation();

        this.service.edit( node.id ).then( data => {
            this.bsModalRef = this.modalService.show( EntityModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'edit-modal'
            } );
            this.bsModalRef.content.init( false, this.admin, data.item, data.attributes, this.map.getCenter(), this.map.getZoom() );
            
            this.bsModalRef.content.onNodeChange.subscribe( entity => {
                // Update the node
                entity.children = node.children;
                entity.active = node.active;

                this.refreshEntity( entity, this.nodes );
                this.refreshEntity( entity, this.breadcrumbs );

                this.nodes.forEach( node => {
                    this.refreshEntity( entity, node.children );
                } );

                if ( this.metadataService.getMetadata( entity ).root ) {
                    this.refresh( false );
                }
            } );
        } );
    }

    refreshEntity( node: SiteEntity, nodes: SiteEntity[] ): void {

        if ( nodes != null ) {
            let indexOf = nodes.findIndex( i => i.id === node.id );

            if ( indexOf !== -1 ) {
                nodes[indexOf] = node;
            }
        }
    }

    handleDownloadAll( node: SiteEntity ): void {

        window.location.href = acp + '/project/download-all?id=' + node.component + "&key=" + node.name;

        //      this.service.downloadAll( data.id ).then( data => {
        //        
        //      } ).catch(( err: HttpErrorResponse ) => {
        //          this.error( err );
        //      } );
    }

    handleDelete( node: SiteEntity, event: any ): void {

        event.stopPropagation();

        this.bsModalRef = this.modalService.show( BasicConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = 'Are you sure you want to delete [' + node.name + ']?';
        this.bsModalRef.content.data = node;
        this.bsModalRef.content.type = 'DANGER';
        this.bsModalRef.content.submitText = 'Delete';

        ( <BasicConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
            this.remove( data );
        } );
    }

    remove( node: SiteEntity ): void {
        this.service.remove( node.id ).then( response => {
            this.nodes = this.nodes.filter(( n: any ) => n.id !== node.id );

            this.nodes.forEach( n => {
                if ( n.children != null ) {
                    n.children = n.children.filter(( child: any ) => child.id !== node.id );

                    n.numberOfChildren = n.children.length;
                }
            } );

            if ( node.type === 'Site' ) {
                this.refresh( false );
            }
        } );
    }


    handleDownload( node: SiteEntity ): void {
        window.location.href = acp + '/project/download?id=' + node.component + "&key=" + node.key;

        //this.service.download( node.data.component, node.data.key, true ).subscribe( blob => {
        //    importedSaveAs( blob, node.data.name );
        //} );
    }

    handleImageDownload( image: any ): void {
        window.location.href = acp + '/project/download?id=' + image.component + "&key=" + image.key;

        //this.service.download( node.data.component, node.data.key, true ).subscribe( blob => {
        //    importedSaveAs( blob, node.data.name );
        //} );
    }

    handleStyle( layer: any ): void {

        this.baseLayers.forEach( baseLayer => {
            baseLayer.selected = false;
        } );

        layer.selected = true;

        this.map.setStyle( 'mapbox://styles/mapbox/' + layer.id );
    }

    highlightMapFeature(id: string): void {

        this.map.setFilter('hover-points', [ 'all',
            [ '==', 'oid', id ]
        ])
  
    }

    clearHighlightMapFeature(): void {

        this.map.setFilter('hover-points', [ 'all',
            [ '==', 'oid', "NONE"]
        ])
  
    }

    onListEntityHover(event: any, site: SiteEntity): void {
        this.highlightMapFeature(site.id);

    }

    onListEntityHoverOff(): void {
        this.clearHighlightMapFeature();

    }

    highlightListItem(id: string): void {
        this.nodes.forEach(node => {
            if(node.id === id){
                this.hoverFeatureId = id;
            }
        })
    }

    clearHighlightListItem(): void {
        if(this.hoverFeatureId){
            this.nodes.forEach(node => {
                if(node.id === this.hoverFeatureId){
                    this.hoverFeatureId = null;
                }
            })
        }
    }


    handleClick( $event: any ): void {
        let result = $event.item;

        if ( result.center ) {
            this.map.flyTo( {
                center: result.center,
                zoom: 9
            } )
        }
    }

    handleMapImage( product: Product ): void {

        const mapKey = product.mapKey;

        if ( mapKey != null ) {
            if ( this.map.getLayer( mapKey ) != null ) {
                this.map.removeLayer( mapKey );
                this.map.removeSource( mapKey );

                var index = this.layers.indexOf( mapKey );
                if ( index !== -1 ) {
                    this.layers.splice( index, 1 );
                }

                product.orthoMapped = false;
            }
            else {
                this.addImageLayer( mapKey );

                this.layers.push( mapKey );

                product.orthoMapped = true;
                
                if (product.boundingBox != null)
                {
                  let bbox = product.boundingBox;
                  
                  let bounds = new LngLatBounds( [bbox[0], bbox[2]], [bbox[1], bbox[3]] );

                  this.map.fitBounds( bounds, { padding: 50 } );
                }
            }
        }
    }

    addImageLayer( imageKey: string ) {
        const workspace = encodeURI( 'uasdm' );
        const layerName = encodeURI( workspace + ':' + imageKey );

        this.map.addLayer( {
            'id': imageKey,
            'type': 'raster',
            'source': {
                'type': 'raster',
                'tiles': [
                    '/geoserver/' + workspace + '/wms?layers=' + layerName + '&bbox={bbox-epsg-3857}&format=image/png&service=WMS&version=1.1.1&request=GetMap&srs=EPSG:3857&transparent=true&width=256&height=256'
                ],
                'tileSize': 256
            },
            'paint': {}
        }, "points" );
    }

    handleGoto(): void {

        //    -111.12439336274211
        //    39.32066259372583
        //    -111.12342302258116
        // 39.32107716199166

        var bounds = new LngLatBounds( [-111.12439336274211, 39.32066259372583, -111.12342302258116, 39.32107716199166] );

        this.map.fitBounds( bounds );
    }


    getMetadata( node: SiteEntity ): any {
        const metadata = this.metadataService.getMetadata( node );

        return metadata;
    }


    select( node: SiteEntity, parent: SiteEntity, event: any ): void {

        if ( event != null ) {
            event.stopPropagation();
        }

        const metadata = this.metadataService.getMetadata( node );

        if ( metadata.leaf ) {
            const breadcrumbs = [...this.breadcrumbs];

            if ( parent != null ) {
                breadcrumbs.push( parent );
            }

            if ( this.metadataService.getTypeContainsFolders( node ) ) {
                this.service.getItems( node.id, null ).then( nodes => {
                    this.showLeafModal( node, nodes, breadcrumbs );
                } );
            }
            else {
                this.showLeafModal( this.current, [node], breadcrumbs );
            }
        }
        else if ( node.type === "object" ) {
            // Do nothing there are no children
            //                return this.service.getItems( node.data.id, node.data.name );
        }
        // else if ( metadata.expandable ) {
        //     if ( node.children == null || node.children.length == 0 ) {
        //         this.service.getItems( node.id, null ).then( nodes => {
        //             node.children = nodes;

        //             this.expand( node );
        //         } );
        //     }
        //     else {
        //         this.expand( node );
        //     }
        // }
        else {
            this.service.getItems( node.id, null ).then( nodes => {
                this.current = node;

                if ( parent != null ) {
                    this.addBreadcrumb( parent );
                }

                this.addBreadcrumb( node );
                this.setNodes( nodes );
            } );
        }
    }

    addBreadcrumb( node: SiteEntity ): void {

        if ( this.breadcrumbs.length == 0 || this.breadcrumbs[this.breadcrumbs.length - 1].id !== node.id ) {
            this.breadcrumbs.push( node );
        }
    }

    handleExpand( node: SiteEntity, event: any ): void {

        if ( event != null ) {
            event.stopPropagation();
        }

        if ( node.children == null || node.children.length == 0 ) {
            this.service.getItems( node.id, null ).then( nodes => {
                node.children = nodes;

                this.expand( node );
            } );
        }
        else {
            // this.expand( node );
            node.children = [];
            node.active = false;
        }
    }

    handleGotoSite( product: Product ): void {
        const entity = product.entities[product.entities.length - 1];

        const breadcrumbs = product.entities;

        this.service.getItems( entity.id, null ).then( nodes => {
            this.showLeafModal( entity, nodes, breadcrumbs );
        } );
    }


    back( node: SiteEntity ): void {

        if ( node != null ) {
            this.service.getItems( node.id, null ).then( nodes => {
                var indexOf = this.breadcrumbs.findIndex( i => i.id === node.id );

                this.current = node;
                this.breadcrumbs.splice( indexOf + 1 );
                this.setNodes( nodes );
            } );
        }
        else if ( this.breadcrumbs.length > 0 ) {
            this.service.roots( null, this.map.getBounds() ).then( nodes => {
                this.current = null;
                this.breadcrumbs = [];
                this.setNodes( nodes );
                this.staticTabs.tabs[0].active = true;
            } );
        }
    }

    expand( node: SiteEntity ) {
        const cMetadata = this.metadataService.getMetadata( this.current );

        node.active = true;
        this.current = node;
    }

    setNodes( nodes: SiteEntity[] ): void {
        this.nodes = [];
        this.supportingData = [];

        nodes.forEach( node => {
            if ( node.type === 'folder' ) {
                this.supportingData.push( node );
            }
            else {
                this.nodes.push( node );
            }
        } )
    }

    showLeafModal( collection: SiteEntity, folders: SiteEntity[], breadcrumbs: SiteEntity[] ): void {
        this.bsModalRef = this.modalService.show( LeafModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: 'leaf-modal'
        } );
        this.bsModalRef.content.init( collection, folders, breadcrumbs );
    }



    //    /*
    //     *  Context menu visibility functions
    //     */
    //    public canEdit = ( item: any ): boolean => {
    //        if ( this.admin ) {
    //            return true;
    //        }
    //        else if ( this.worker ) {
    //            return ( item.data.type === "Mission" || item.data.type === "Collection" );
    //        }
    //
    //        return false;
    //    }
    //
    //    public canRunOrtho = ( item: any ): boolean => {
    //        if ( item.data == null || item.data.type !== "Collection" ) {
    //            return false;
    //        }
    //
    //        return true;
    //
    //        // TODO : If we don't have raw images uploaded then they can't run ortho
    //
    //        // TODO : Different roles?
    //        //      if ( this.admin ) {
    //        //        return true;
    //        //      }
    //        //
    //        //      return false;
    //    }
    //
    //    public canDelete = ( item: any ): boolean => {
    //        if ( this.admin ) {
    //            return true;
    //        }
    //
    //        return false;
    //    }
    //
    //    public canAddChild = ( item: any ): boolean => {
    //        if ( this.admin && item.data.type !== "Collection" && item.data.type !== "Imagery" ) {
    //            return true;
    //        }
    //        else if ( this.worker && ( item.data.type === "Project" || item.data.type === "Mission" ) ) {
    //            return true;
    //        }
    //
    //        return false;
    //    }
    //
    //    public canCreateImageDir( item: any ): boolean {
    //        if ( gpAppType && gpAppType.toLowerCase() === 'nps' && item.data.type === 'Project' ) {
    //            return true;
    //        }
    //    }
    //
    //    public canEditSite = ( item: any ): boolean => {
    //        return item.data.type === "Site" && this.canEdit( item );
    //    }
    //
    //    public hasMapImage = ( item: any ): boolean => {
    //        return ( item.data.imageKey != null );
    //    }
    //
    //    public isSite = ( item: any ): boolean => {
    //        return item.data.type === "Site";
    //    }
    //
    //    public isImageDir = ( item: any ): boolean => {
    //        return item.data.type === "Imagery";
    //    }
    //
    //    public isCollection = ( item: any ): boolean => {
    //        return item.data.type === "Collection";
    //    }
    //
    //    public canUpload = ( item: any ): boolean => {
    //        // Only allow direct uploads on Imagery child nodes
    //        if ( gpAppType && gpAppType.toLowerCase() === 'nps' && item.parent.data.type !== "Collection" ) {
    //            if ( item.data.name === "raw" ) {
    //                return true;
    //            }
    //            else if ( item.data.name === "georef" ) {
    //                return true;
    //            }
    //            else if ( item.data.name === "ortho" ) {
    //                return true;
    //            }
    //            // else if(item.data.type === "Collection"){
    //            //     return true;
    //            // }
    //            // else if(item.data.type === "Imagery"){
    //            //     return true;
    //            // }
    //        }
    //
    //        return false;
    //    }
}
