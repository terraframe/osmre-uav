///
///
///

import { Component, OnInit, OnDestroy, AfterViewInit, Input } from '@angular/core';
import * as MapboxDraw from '@mapbox/mapbox-gl-draw';
import * as StaticMode from '@mapbox/mapbox-gl-draw-static-mode';
import { Observable } from 'rxjs';


import { SiteEntity } from '@site/model/management';
import { MapService } from '@site/service/map.service';
import { ConfigurationService } from '@core/service/configuration.service';
import { LngLat, Map, NavigationControl } from 'maplibre-gl';
import { isMapboxURL, transformMapboxUrl } from 'maplibregl-mapbox-request-transformer';


@Component({
    selector: 'map-attribute',
    templateUrl: './map-attribute.component.html',
    styleUrls: [],
})
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

    @Input() center: LngLat = new LngLat(-78.880453, 42.897852);
    @Input() zoom: number = 2;

    coordinate: {
        longitude: number,
        latitude: number
    } = { longitude: null, latitude: null };

    /* 
     * Datasource to get search responses
     */
    dataSource: Observable<any>;

    /* 
     * Model for text being searched
     */
    search: string = "";

    constructor(
        private configuration: ConfigurationService,
        private mapService: MapService
    ) {
        this.dataSource = Observable.create((observer: any) => {

            this.mapService.mbForwardGeocode(this.search).then(response => {
                const match = response.features;
                let results = [];

                // Add Mapbox results to any local results
                match.forEach(obj => {
                    let newObj = {
                        id: obj.id,
                        hierarchy: [],
                        label: obj.place_name,
                        center: obj.center,
                        source: "MAPBOX"
                    }

                    results.push(newObj);
                });

                observer.next(results);
            });
        });
    }

    ngOnInit(): void {
        this.refreshCoordinateFromMap();
    }

    ngAfterViewInit() {

        // setTimeout(() => {
        //     if ( this.tree ) {
        //         this.tree.treeModel.expandAll();
        //     }
        // }, 1000 );

        let config = {
            container: 'map-attribute-div',
            style: 'mapbox://styles/mapbox/outdoors-v11',
            zoom: this.zoom,
            center: this.center,
            transformRequest: (url: string, resourceType: string) => {
                if (isMapboxURL(url)) {
                    return transformMapboxUrl(url, resourceType, this.configuration.getMapboxKey())
                }

                return { url }
            }
        };

        if (this.site.geometry != null) {
            //                    config.zoom = 10;
            config.center = this.site.geometry.coordinates;
        }


        this.map = new Map(config);

        this.map.on('load', () => {
            this.initMap();
        });
    }

    ngOnDestroy(): void {
        this.map.remove();
    }

    initMap(): void {

        let modes = MapboxDraw.modes;
        modes.static = StaticMode;

        this.draw = new MapboxDraw({
            modes: modes,
            displayControlsDefault: false,
            controls: {
                static: true
            }
        });

        this.map.addControl(this.draw);

        // Add zoom and rotation controls to the map.
        this.map.addControl(new NavigationControl({ visualizePitch: true }));

        this.map.on("draw.update", ($event) => { this.onDrawUpdate($event) });
        this.map.on("draw.create", ($event) => { this.onDrawCreate($event) });
        this.map.on("draw.modechange", ($event) => { this.onDrawUpdate($event) });

        this.map.on('style.load', () => {
            this.addLayers();
            //            this.refresh( false );
        });

        this.addLayers();
    }


    addLayers(): void {

        if (this.site.geometry != null) {
            let feature = {
                id: this.site.id,
                type: 'Feature',
                properties: {
                    oid: this.site.id,
                    name: this.site.name
                },
                geometry: this.site.geometry
            };

            this.draw.add(feature);
            this.draw.changeMode('simple_select', { featureIds: [feature.id] });
        }
        else {
            this.draw.changeMode('draw_point', {});
        }

        this.map.addSource('sites', {
            type: 'geojson',
            data: {
                "type": "FeatureCollection",
                "features": []
            }
        });
    }

    onDrawUpdate(event: any): void {
        if (event.action === 'move' && event.features != null && event.features.length > 0) {
            this.updateGeometry(event.features[0])
        }
    }

    onDrawCreate(event: any): void {
        if (event.features != null && event.features.length > 0) {

            let feature = event.features[0];
            feature.id = this.site.id;

            this.updateGeometry(feature)
        }
    }

    updateGeometry(feature: any): void {
        this.site.geometry = feature.geometry;

        this.refreshCoordinateFromMap();
    }

    refreshCoordinateFromMap(): void {
        if (this.site != null && this.site.geometry != null) {
            this.coordinate.longitude = this.site.geometry.coordinates[0];
            this.coordinate.latitude = this.site.geometry.coordinates[1];
        }
    }

    refreshCoordinateFromInput(): void {
        if (this.coordinate.longitude != null && this.coordinate.latitude != null) {
            if (this.site.geometry == null) {
                this.site.geometry = { type: 'Point' };
            }

            this.site.geometry.coordinates = [this.coordinate.longitude, this.coordinate.latitude];

            var ids = this.draw.set({
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
            });

            this.draw.changeMode('simple_select', { featureIds: ids });
        }
    }

    //    zoomToFeature( node: TreeNode ): void {
    //        if ( node.data.geometry != null ) {
    //            this.map.flyTo( {
    //                center: node.data.geometry.coordinates
    //            } );
    //        }
    //    }

    handleStyle(layer: any): void {

        this.baseLayers.forEach(baseLayer => {
            baseLayer.selected = false;
        });

        layer.selected = true;

        this.map.setStyle('mapbox://styles/mapbox/' + layer.id + "?access_token=" + this.configuration.getMapboxKey());
    }

    handleClick($event: any): void {
        let result = $event.item;

        if (result.center) {
            this.map.flyTo({
                center: result.center,
                zoom: 9
            })
        }
    }

}
