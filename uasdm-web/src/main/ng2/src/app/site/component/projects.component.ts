import { Component, OnInit, OnDestroy, AfterViewInit, Inject, ViewChild, TemplateRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import {
    trigger,
    state,
    style,
    animate,
    transition,
} from '@angular/animations';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';
import { saveAs as importedSaveAs } from "file-saver";
import { Map, LngLatBounds, NavigationControl, ImageSource } from 'mapbox-gl';
import * as StaticMode from '@mapbox/mapbox-gl-draw-static-mode';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';

import { BasicConfirmModalComponent } from '../../shared/component/modal/basic-confirm-modal.component';
import { AuthService } from '../../shared/service/auth.service';

import { SiteEntity } from '../model/management';

import { EntityModalComponent } from './modal/entity-modal.component';
import { UploadModalComponent } from './modal/upload-modal.component';
import { CollectionModalComponent } from './modal/collection-modal.component';

import { ManagementService } from '../service/management.service';
import { MapService } from '../service/map.service';
import { MetadataService } from '../service/metadata.service';

const mbxStyles = require( '@mapbox/mapbox-sdk/services/geocoding' );
const geocodingService = mbxStyles( { accessToken: "pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNTFnaTYyZ2RuNDlxcmNnejNtNjN6In0.-kmlS8Tgb2fNc1NPb5rJEQ" } );

declare var acp: any;
declare var gpAppType: any;

@Component( {
    selector: 'projects',
    templateUrl: './projects.component.html',
    styles: [],
    animations: [
        trigger( 'fadeIn', [
            transition( ':enter', [
                style( { opacity: '0' } ),
                animate( '.25s ease-out', style( { opacity: '1' } ) ),
            ] ),
        ] )
    ]
} )
export class ProjectsComponent implements OnInit, AfterViewInit, OnDestroy {

    // imageToShow: any;
    userName: string = "";

    /*
     * Template for the delete confirmation
     */
    @ViewChild( 'confirmTemplate' ) public confirmTemplate: TemplateRef<any>;

    //    /*
    //     * Template for tree node menu
    //     */
    //    @ViewChild( 'nodeMenu' ) public nodeMenuComponent: ContextMenuComponent;
    //
    //    /*
    //     * Template for folder node menu
    //     */
    //    @ViewChild( 'folderMenu' ) public folderMenuComponent: ContextMenuComponent;
    //
    //    /*
    //     * Template for site items
    //     */
    //    @ViewChild( 'siteMenu' ) public siteMenuComponent: ContextMenuComponent;
    //
    //    /*
    //     * Template for leaf menu
    //     */
    //    @ViewChild( 'leafMenu' ) public leafMenuComponent: ContextMenuComponent;
    //
    //    /*
    //     * Template for object items
    //     */
    //    @ViewChild( 'objectMenu' ) public objectMenuComponent: ContextMenuComponent;

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
    previous = [] as SiteEntity[];

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

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    constructor( private service: ManagementService, private authService: AuthService, private mapService: MapService,
        private modalService: BsModalService, private metadataService: MetadataService ) {

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
        this.service.roots( null ).then( nodes => {
            this.nodes = nodes;
        } );
    }

    ngOnDestroy(): void {
        this.map.remove();
    }

    ngAfterViewInit() {

        // setTimeout(() => {
        //     if ( this.tree ) {
        //         this.tree.treeModel.expandAll();
        //     }
        // }, 1000 );

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

        let searchLocations = ( searchTerm ) => {

            var matchingFeatures = [];
            // this.service.searchEntites( searchTerm ).then( results => {

            let results = [{
                "hierarchy": [],
                "id": "e458d9e8-91b4-4f2f-b768-dc8102ea2b70",
                "label": "test",
                "place_name": "🌲 test",
                "center": [-104.99404, 39.75621]
            }]

            for ( var i = 0; i < results.length; i++ ) {
                var feature = results[i];
                // handle queries with different capitalization than the source data by calling toLowerCase()
                if ( feature.label.toLowerCase().search( searchTerm.toLowerCase() ) !== -1 ) {
                    // add a tree emoji as a prefix for custom data results
                    // using carmen geojson format: https://github.com/mapbox/carmen/blob/master/carmen-geojson.md
                    feature['place_name'] = '🌲 ' + feature.label;
                    feature['center'] = [-104.99404, 39.75621];
                    feature['place_type'] = ['park'];
                    matchingFeatures.push( feature );
                }
            }

            return matchingFeatures;
            // });

        }


        // this.map.addControl(new MapboxGeocoder({
        //     accessToken: "pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNTFnaTYyZ2RuNDlxcmNnejNtNjN6In0.-kmlS8Tgb2fNc1NPb5rJEQ",
        //     mapboxgl: this.map,
        //     localGeocoder: searchLocations,
        //     localGeocoderOnly: true,
        //     zoom: 14,
        //     placeholder: "Search for a site or place...",
        // }));


        this.refresh( true );

        // Add zoom and rotation controls to the map.
        this.map.addControl( new NavigationControl() );

        this.map.on( 'mousemove', function( e ) {
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
        } );

        // MapboxGL doesn't have a good way to detect when moving off the map
        let sidebar = document.getElementById( "location-explorer-list" );
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

        let hierarchy = {};

        function getParent( item ) {
            hierarchy[item.data.type.toLowerCase()] = item.data;

            if ( item.parent && item.parent.data.type ) {
                return getParent( item.parent );
            }
        }

        getParent( item );

        this.bsModalRef = this.modalService.show( UploadModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );
        this.bsModalRef.content.setHierarchy = hierarchy;
        this.bsModalRef.content.clickedItem = item;

        let that = this;
        this.bsModalRef.content.onUploadComplete.subscribe( node => {
            // that.service.getItems( node.data.component, node.data.name )
            // .then(data => {
            //     // TODO: update tree node children
            // })
        } );

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
            this.bsModalRef.content.newInstance = true;
            this.bsModalRef.content.admin = this.admin;
            this.bsModalRef.content.entity = data.item;
            this.bsModalRef.content.attributes = data.attributes;

            if ( parent != null ) {
                this.bsModalRef.content.parentId = parent.id;
            }

            this.bsModalRef.content.onNodeChange.subscribe( entity => {

                if ( parent != null ) {

                }
                else {
                    if ( this.previous.length == 0 ) {
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

    handleEdit( node: SiteEntity ): void {
        this.service.edit( node.id ).then( data => {
            this.bsModalRef = this.modalService.show( EntityModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
                'class': 'edit-modal'
            } );
            this.bsModalRef.content.newInstance = false;
            this.bsModalRef.content.admin = this.admin;
            this.bsModalRef.content.entity = data.item;
            this.bsModalRef.content.attributes = data.attributes;
            this.bsModalRef.content.onNodeChange.subscribe( entity => {
                // Do something
                if ( entity.type === 'Site' ) {
                    this.refresh( false );
                }
            } );
        } );
    }

    handleDownloadAll( node: SiteEntity ): void {

        window.location.href = acp + '/project/download-all?id=' + node.component + "&key=" + node.name;

        //      this.service.downloadAll( data.id ).then( data => {
        //        
        //      } ).catch(( err: HttpErrorResponse ) => {
        //          this.error( err );
        //      } );
    }

    handleDelete( node: SiteEntity ): void {
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

            if ( node.type !== 'Site' ) {
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

    highlight( match: any, query: string[] | string ): string {
        console.log( match );

        return 'Test';
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

    handleMapImage( node: SiteEntity ): void {

        const imageKey = node.imageKey;

        if ( this.map.getLayer( imageKey ) != null ) {
            this.map.removeLayer( imageKey );
            this.map.removeSource( imageKey );

            var index = this.layers.indexOf( imageKey );
            if ( index !== -1 ) {
                this.layers.splice( index, 1 );
            }
        }
        else {
            this.addImageLayer( imageKey );

            this.layers.push( imageKey );
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

    select( node: SiteEntity ): void {
        const metadata = this.metadataService.getMetadata( node );

        if ( node.type === "folder" ) {
            //            this.service.getItems( node.component, node.name ).then( nodes => {
            //                this.nodes = nodes;
            //            } );
        }
        else if ( node.type === "object" ) {
            // Do nothing there are no children
            //                return this.service.getItems( node.data.id, node.data.name );
        }
        else if ( metadata.expandable ) {
            if ( node.children == null || node.children.length == 0 ) {
                this.service.getItems( node.id, null ).then( nodes => {
                    node.children = nodes;

                    this.expand( node );
                } );
            }
            else {
                this.expand( node );
            }
        }
        else {
            this.service.getItems( node.id, null ).then( nodes => {

                if ( metadata.leaf ) {
                    this.showCollectionModal( node, nodes );
                }
                else {
                    this.current = node;
                    this.previous.push( node );
                    this.setNodes( nodes );
                }
            } );
        }

    }

    back( node: SiteEntity ): void {

        if ( node != null ) {
            this.service.getItems( node.id, null ).then( nodes => {
                var indexOf = this.previous.findIndex( i => i.id === node.id );

                this.current = node;
                this.previous.splice( indexOf + 1 );
                this.setNodes( nodes );
            } );
        }
        else if ( this.previous.length > 0 ) {
            this.service.roots( null ).then( nodes => {
                this.current = null;
                this.previous = [];
                this.setNodes( nodes );
            } );
        }
    }

    expand( node: SiteEntity ) {
        const cMetadata = this.metadataService.getMetadata( this.current );

        if ( cMetadata.expandable ) {
            this.previous.splice( this.previous.length - 1, 1 );
        }

        node.active = true;
        this.current = node;

        this.previous.push( node );

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

    showCollectionModal( collection: SiteEntity, folders: SiteEntity[] ): void {
        this.bsModalRef = this.modalService.show( CollectionModalComponent, {
            animated: true,
            backdrop: false,
            ignoreBackdropClick: false,
            class: 'image-preview-modal'
        } );
        this.bsModalRef.content.init( collection, folders, this.previous );
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
