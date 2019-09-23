import { Component, OnInit, OnDestroy, AfterViewInit, Input } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Map, LngLatBounds, NavigationControl, ImageSource, MapboxOptions } from 'mapbox-gl';
import * as MapboxDraw from '@mapbox/mapbox-gl-draw';
import * as StaticMode from '@mapbox/mapbox-gl-draw-static-mode';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';

import { SiteEntity } from '../../model/management';
import { MapService } from '../../service/map.service';

declare var acp: any;
declare var gpAppType: any;

@Component( {
    selector: 'map-attribute',
    templateUrl: './map-attribute.component.html',
    styles: [],
} )
export class MapAttributeComponent implements OnInit, AfterViewInit, OnDestroy {

    /* 
     * mapbox-gl map
     */
    map: Map;

    /* 
     * Draw control
     */
    draw: MapboxDraw;

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

    @Input() site: SiteEntity;

    coordinate: {
        longitude: number,
        latitude: number
    } = { longitude: null, latitude: null };

    constructor( private mapService: MapService ) { }

    ngOnInit(): void {
        this.refreshCoordinateFromMap();
    }

    ngAfterViewInit() {

        // setTimeout(() => {
        //     if ( this.tree ) {
        //         this.tree.treeModel.expandAll();
        //     }
        // }, 1000 );

        let config: MapboxOptions = {
            container: 'map-attribute-div',
            style: 'mapbox://styles/mapbox/outdoors-v11',
            zoom: 2,
            center: [-78.880453, 42.897852]
        };

        if ( this.site.geometry != null ) {
            config.zoom = 10;
            config.center = this.site.geometry.coordinates;
        }


        this.map = new Map( config );

        this.map.on( 'load', () => {
            this.initMap();
        } );
    }

    ngOnDestroy(): void {
        this.map.remove();
    }

    initMap(): void {

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

        // Add zoom and rotation controls to the map.
        this.map.addControl( new NavigationControl() );

        this.map.on( "draw.update", ( $event ) => { this.onDrawUpdate( $event ) } );
        this.map.on( "draw.create", ( $event ) => { this.onDrawCreate( $event ) } );
        this.map.on( "draw.modechange", ( $event ) => { this.onDrawUpdate( $event ) } );

        this.map.on( 'style.load', () => {
            this.addLayers();
            //            this.refresh( false );
        } );

        this.addLayers();

        //        this.refresh( true );
    }


    addLayers(): void {

        if ( this.site.geometry != null ) {
            let feature = {
                id: this.site.id,
                type: 'Feature',
                properties: {
                    oid: this.site.id,
                    name: this.site.name
                },
                geometry: this.site.geometry
            };

            this.draw.add( feature );
            this.draw.changeMode( 'simple_select', { featureIds: [feature.id] } );
        }
        else {
            this.draw.changeMode( 'draw_point', {} );
        }

        this.map.addSource( 'sites', {
            type: 'geojson',
            data: {
                "type": "FeatureCollection",
                "features": []
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
            feature.id = this.site.id;

            this.updateGeometry( feature )
        }
    }

    updateGeometry( feature: any ): void {
        this.site.geometry = feature.geometry;

        this.refreshCoordinateFromMap();
    }

    refreshCoordinateFromMap(): void {
        if ( this.site != null && this.site.geometry != null ) {
            this.coordinate.longitude = this.site.geometry.coordinates[0];
            this.coordinate.latitude = this.site.geometry.coordinates[1];
        }
    }

    refreshCoordinateFromInput(): void {
        if ( this.coordinate.longitude != null && this.coordinate.latitude != null ) {
            this.site.geometry.coordinates = [this.coordinate.longitude, this.coordinate.latitude];
            
            console.log(this.site);

            var ids = this.draw.set( {
                type: 'FeatureCollection',
                features: [{
                    id: this.site.id,
                    type: 'Feature',
                    properties: {
                        oid: this.site.id,
                        name: this.site.name
                    },
                    geometry: this.site.geometry
                }]
            } );
        }
    }

    //    zoomToFeature( node: TreeNode ): void {
    //        if ( node.data.geometry != null ) {
    //            this.map.flyTo( {
    //                center: node.data.geometry.coordinates
    //            } );
    //        }
    //    }

    handleStyle( layer: any ): void {

        this.baseLayers.forEach( baseLayer => {
            baseLayer.selected = false;
        } );

        layer.selected = true;

        this.map.setStyle( 'mapbox://styles/mapbox/' + layer.id );
    }
}
