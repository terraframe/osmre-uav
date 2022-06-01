import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { TabsetComponent } from 'ngx-bootstrap';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Map, LngLatBounds, NavigationControl, MapboxEvent, AttributionControl } from 'mapbox-gl';

import { Observable, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from "rxjs/operators";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { BasicConfirmModalComponent } from '@shared/component/modal/basic-confirm-modal.component';
import { NotificationModalComponent } from '@shared/component/modal/notification-modal.component';
import { AuthService } from '@shared/service/auth.service';

import { SiteEntity, Product, Task, GeoserverLayer } from '../model/management';

import { EntityModalComponent } from './modal/entity-modal.component';
import { CollectionModalComponent } from './modal/collection-modal.component';
import { AccessibleSupportModalComponent } from './modal/accessible-support-modal.component';

import { ManagementService } from '../service/management.service';
import { MapService } from '../service/map.service';
import { MetadataService } from '../service/metadata.service';
import { CookieService } from 'ngx-cookie-service';

import {
  fadeInOnEnterAnimation,
  fadeOutOnLeaveAnimation
} from 'angular-animations';
import { ActivatedRoute } from '@angular/router';
import { CreateCollectionModalComponent } from './modal/create-collection-modal.component';
import { UIOptions } from 'fine-uploader';
import { FineUploaderBasic } from 'fine-uploader/lib/core';
import { UploadModalComponent } from './modal/upload-modal.component';


declare var acp: any;

@Component({
  selector: 'projects',
  templateUrl: './projects.component.html',
  styles: ['./projects.css'],
  animations: [
    fadeInOnEnterAnimation(),
    fadeOutOnLeaveAnimation()
  ]
})
export class ProjectsComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('staticTabs') staticTabs: TabsetComponent;

  // imageToShow: any;
  userName: string = "";

  /*
   * Template for the delete confirmation
   */
  @ViewChild('confirmTemplate') public confirmTemplate: TemplateRef<any>;

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

  allPointsBounds;

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

  loadingSites: boolean = true;

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

  layers: GeoserverLayer[] = [];

  baselayerIconHover = false;

  hoverFeatureId: string;

  /* 
     * debounced subject for map extent change events
     */
  subject: Subject<MapboxEvent<MouseEvent | TouchEvent | WheelEvent>>;

  /*
   * Reference to the modal current showing
  */
  private bsModalRef: BsModalRef;

  notifier: WebSocketSubject<any>;

  tasks: Task[] = [];

  existingTask: {
    task: Task,
    filename: string
  } = null;

  constructor(private service: ManagementService, private authService: AuthService, private mapService: MapService,
    private modalService: BsModalService, private metadataService: MetadataService, private route: ActivatedRoute,
    private cookieService: CookieService) {

    this.subject = new Subject();
    this.subject.pipe(debounceTime(300), distinctUntilChanged()).subscribe(event => this.handleExtentChange(event));

    this.dataSource = new Observable((observer: any) => {

      this.mapService.mbForwardGeocode(this.search).then(response => {
        const match = response.features;

        this.service.searchEntites(this.search).then(results => {

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
    });
  }

  ngOnInit(): void {
    this.admin = this.authService.isAdmin();
    this.worker = this.authService.isWorker();
    this.userName = this.service.getCurrentUser();


    let baseUrl = "wss://" + window.location.hostname + (window.location.port ? ':' + window.location.port : '') + acp;

    this.notifier = webSocket(baseUrl + '/websocket/notify');
    this.notifier.subscribe(message => {
      console.log(message);
      if (message.type === 'UPLOAD_JOB_CHANGE') {
        this.tasks.push(message.content);
      }
    });

    const oid = this.route.snapshot.params['oid'];
    const action = this.route.snapshot.params['action'];

    if (oid != null && action != null && action === 'collection') {
      this.handleViewSite(oid);
    }

    let uiOptions: UIOptions = {
      debug: false,
      autoUpload: false,
      multiple: false,
      request: {
        endpoint: acp + "/file/upload",
        forceMultipart: true
      },
      resume: {
        enabled: true,
        recordsExpireIn: 1
      },
      chunking: {
        enabled: true
      },
      retry: {
        enableAuto: true
      },
      text: {
        defaultResponseError: "Upload failed"
      },
      failedUploadTextDisplay: {
        mode: 'none'
      },
      validation: {
        allowedExtensions: ['zip', 'tar.gz']
      },
      showMessage: function (message: string) {
        // 
      },
      callbacks: {
        onUpload: function (id: any, name: any): void {
        },
        onProgress: function (id: any, name: any, uploadedBytes: any, totalBytes: any): void {
        },
        onUploadChunk: function (id: any, name: any, chunkData: any): void {
        },
        onUploadChunkSuccess: function (id: any, chunkData: any, responseJSON: any, xhr: any): void {
        },
        onComplete: function (id: any, name: any, responseJSON: any, xhrOrXdr: any): void {
        },
        onCancel: function (id: number, name: string) {
        },
        onError: function (id: number, errorReason: string, xhrOrXdr: string) {
        }

      }
    };

    const uploader = new FineUploaderBasic(uiOptions);

    let resumables = uploader.getResumableFilesData() as any[];
    if (resumables.length > 0) {
      const resumable = resumables[0];

      this.service.getUploadTask(resumable.uuid).then(task => {
        this.existingTask = {
          task: task,
          filename: resumable.name
        };

        console.log(this.existingTask);
      })
    }
  }


  ngOnDestroy(): void {
    this.map.remove();

    this.notifier.complete();
  }

  ngAfterViewInit() {

    this.map = new Map({
      container: 'map',
      style: 'mapbox://styles/mapbox/outdoors-v11',
      zoom: 2,
      attributionControl: false,
      center: [-78.880453, 42.897852]
    });

    this.map.on('load', () => {
      this.initMap();
    });

  }

  initMap(): void {

    this.map.on('style.load', () => {
      this.addLayers();
      this.refreshMapPoints(false);
    });

    this.addLayers();


    this.refreshMapPoints(true);

    // Add zoom and rotation controls to the map.
    this.map.addControl(new NavigationControl());
    this.map.addControl(new AttributionControl({ compact: true }), 'bottom-left');

    this.map.on('mousemove', e => {
      // e.point is the x, y coordinates of the mousemove event relative
      // to the top-left corner of the map.
      // e.lngLat is the longitude, latitude geographical position of the event
      let coord = e.lngLat.wrap();

      // EPSG:3857 = WGS 84 / Pseudo-Mercator
      // EPSG:4326 = WGS 84 
      // let coord4326 = window.proj4(window.proj4.defs('EPSG:3857'), window.proj4.defs('EPSG:4326'), [coord.lng, coord.lat]);
      // let text = "Long: " + coord4326[0] + " Lat: " + coord4326[1];

      let text = "Lat: " + coord.lat + " Long: " + coord.lng;
      let mousemovePanel = document.getElementById("mousemove-panel");
      mousemovePanel.textContent = text;


      let features = this.map.queryRenderedFeatures(e.point, { layers: ['points'] });

      if (this.current == null) {
        if (features.length > 0) {
          let focusFeatureId = features[0].properties.oid; // just the first
          this.map.setFilter('hover-points', ['all',
            ['==', 'oid', focusFeatureId]
          ])

          this.highlightListItem(focusFeatureId)
        }
        else {
          this.map.setFilter('hover-points', ['all',
            ['==', 'oid', "NONE"]
          ])

          this.clearHighlightListItem();
        }
      }
    });

    this.map.on('zoomend', (e) => {
      this.subject.next(e);
    });

    this.map.on('moveend', (e) => {
      this.subject.next(e);
    });

    // Sit selection from map
    this.map.on('dblclick', (e) => {
      let features = this.map.queryRenderedFeatures(e.point, { layers: ['points'] });

      if (features.length > 0) {
        let focusFeatureId = features[0].properties.oid; // just the first

        this.handleViewSite(focusFeatureId);
      }
    });

    // MapboxGL doesn't have a good way to detect when moving off the map
    let sidebar = document.getElementById("navigator-left-sidebar");
    sidebar.addEventListener("mouseenter", function () {
      let mousemovePanel = document.getElementById("mousemove-panel");
      mousemovePanel.textContent = "";
    });


    // Show disclaimer
    if (!Boolean(this.cookieService.get("acceptedDisclaimer"))) {

      this.bsModalRef = this.modalService.show(NotificationModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
      });
      this.bsModalRef.content.messageTitle = "Disclaimer";
      this.bsModalRef.content.message = (window as any).uasAppDisclaimer;
      this.bsModalRef.content.submitText = 'I Accept';

      (<NotificationModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
        this.cookieService.set('acceptedDisclaimer', "true");
      });
    }

  }

  addLayers(): void {

    this.map.addSource('sites', {
      type: 'geojson',
      data: {
        "type": "FeatureCollection",
        "features": []
      }
    });


    // Point layer
    this.map.addLayer({
      "id": "points",
      "type": "circle",
      "source": 'sites',
      "paint": {
        "circle-radius": 10,
        "circle-color": '#800000',
        "circle-stroke-width": 2,
        "circle-stroke-color": '#FFFFFF'
      }
    });

    // Hover style
    this.map.addLayer({
      "id": "hover-points",
      "type": "circle",
      "source": 'sites',
      "paint": {
        "circle-radius": 13,
        "circle-color": '#cf0000',
        "circle-stroke-width": 2,
        "circle-stroke-color": '#FFFFFF'
      },
      filter: ['all',
        ['==', 'id', 'NONE'] // start with a filter that doesn't select anything
      ]
    });


    // Label layer
    this.map.addLayer({
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
    });

    this.layers.forEach(layer => {
      if (layer.isMapped) {
        this.addImageLayer(layer);
      }
    });
  }

  handleExtentChange(e: MapboxEvent<MouseEvent | TouchEvent | WheelEvent>): void {
    if (this.current == null) {
      const bounds = this.map.getBounds();

      // Sometimes bounds aren't valid for 4326, so validate it before sending to server
      if (this.isValidBounds(bounds)) {
        this.loadingSites = true;
        this.service.roots(null, bounds).then(nodes => {
          this.nodes = nodes;
          this.loadingSites = false;
        });
      }
      else {
        // console.log("Invalid bounds", bounds);
      }
    }
  }

  isValidBounds(bounds: LngLatBounds): boolean {

    const ne = bounds.getNorthEast();
    const sw = bounds.getSouthWest();

    if (Math.abs(ne.lng) > 180 || Math.abs(sw.lng) > 180) {
      return false;
    }

    if (Math.abs(ne.lat) > 90 || Math.abs(sw.lat) > 90) {
      return false;
    }

    return true;
  }

  /**
   * Goes to the server and fetches all points for all sites. Returns GeoJSON which is then used to refresh the map.
   */
  refreshMapPoints(zoom: boolean): void {
    this.mapService.features().then(data => {
      (<any>this.map.getSource('sites')).setData(data.features);

      if (zoom) {
        this.allPointsBounds = new LngLatBounds([data.bbox[0], data.bbox[1]], [data.bbox[2], data.bbox[3]]);

        this.map.fitBounds(this.allPointsBounds, { padding: 50 });
      }
    });
  }

  isData(node: any): boolean {

    if (node.data.type === "Site") {
      return false;
    }
    else if (node.data.type === "Project") {
      return false;
    }
    else if (node.data.type === "Mission") {
      return false;
    }
    else if (node.data.type === "Collection") {
      return false;
    }
    else if (node.data.type === "Imagery") {
      return false;
    }
    else {
      return true;
    }
  }

  handleOnUpdateData(): void {
    //        this.tree.treeModel.expandAll();
  }

  handleCloseToast(idx: number): void {
    this.tasks.splice(idx, 1);
  }

  handleCreateCollection(): void {

    this.bsModalRef = this.modalService.show(CreateCollectionModalComponent, {
      animated: true,
      backdrop: true,
      ignoreBackdropClick: true,
      'class': 'upload-modal'
    });
    this.bsModalRef.content.init(this.breadcrumbs);

    this.bsModalRef.content.onCreateComplete.subscribe(oid => {

      this.handleViewSite(oid);
    });
  }


  handleCreate(parent: SiteEntity, type: string): void {
    let parentId = parent != null ? parent.id : null;

    this.service.newChild(parentId, type).then(data => {
      this.bsModalRef = this.modalService.show(EntityModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        'class': 'upload-modal'
      });
      this.bsModalRef.content.init(true, this.userName, this.admin, data.item, data.attributes, this.map.getCenter(), this.map.getZoom());


      if (parent != null) {
        this.bsModalRef.content.parentId = parent.id;
      }

      this.bsModalRef.content.onNodeChange.subscribe(entity => {

        if (parent != null) {

        }
        else {
          if (this.breadcrumbs.length == 0) {
            this.nodes.push(entity);
          }

          this.refreshMapPoints(false);
        }
      });
    });
  }

  zoomToFeature(node: SiteEntity): void {
    if (node.geometry != null) {
      this.map.flyTo({
        center: node.geometry.coordinates
      });
    }
  }

  handleExistingTask(): void {

    this.service.view(this.existingTask.task.collection).then(resp => {
      const modal = this.modalService.show(UploadModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        'class': 'upload-modal'
      });
      modal.content.init(resp.item, this.existingTask.task.uploadTarget);
      modal.content.onUploadCancel.subscribe(() => {
        this.existingTask = null;
      });
      modal.content.onUploadComplete.subscribe(() => {
        this.existingTask = null;
      });

    });
  }

  handleEdit(node: SiteEntity, event: any): void {

    event.stopPropagation();

    this.service.edit(node.id).then(data => {
      this.bsModalRef = this.modalService.show(EntityModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        'class': 'edit-modal'
      });
      this.bsModalRef.content.init(false, this.userName, this.admin, data.item, data.attributes, this.map.getCenter(), this.map.getZoom());

      this.bsModalRef.content.onNodeChange.subscribe(entity => {
        // Update the node
        entity.children = node.children;
        entity.active = node.active;

        this.refreshEntity(entity, this.nodes);
        this.refreshEntity(entity, this.breadcrumbs);

        this.nodes.forEach(node => {
          this.refreshEntity(entity, node.children);
        });

        if (this.metadataService.getMetadata(entity).root) {
          this.refreshMapPoints(false);
        }
      });
    });
  }

  refreshEntity(node: SiteEntity, nodes: SiteEntity[]): void {

    if (nodes != null) {
      let indexOf = nodes.findIndex(i => i.id === node.id);

      if (indexOf !== -1) {
        nodes[indexOf] = node;
      }
    }
  }

  handleDownloadAll(node: SiteEntity): void {

    window.location.href = acp + '/project/download-all?id=' + node.component + "&key=" + node.name;

    //      this.service.downloadAll( data.id ).then( data => {
    //        
    //      } ).catch(( err: HttpErrorResponse ) => {
    //          this.error( err );
    //      } );
  }

  handleDelete(node: SiteEntity, event: any): void {

    event.stopPropagation();

    let sText = '<b>IMPORTANT:</b> [' + node.name + '] will be deleted along with all underlying data including all files in Collections and Accessible Support';

    if (node.type === 'Collection') {
      sText = '<b>IMPORTANT:</b> [' + node.name + '] will be deleted along with all underlying data including all files.';
    }

    sText += ' This can <b>NOT</b> be undone';

    this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
      animated: true,
      backdrop: true,
      ignoreBackdropClick: true,
    });
    this.bsModalRef.content.message = 'Are you sure you want to delete [' + node.name + ']?';
    this.bsModalRef.content.subText = sText;
    this.bsModalRef.content.data = node;
    this.bsModalRef.content.type = 'DANGER';
    this.bsModalRef.content.submitText = 'Delete';

    (<BasicConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
      this.remove(data);
    });
  }

  remove(node: SiteEntity): void {
    this.service.remove(node.id).then(() => {
      this.nodes = this.nodes.filter((n: any) => n.id !== node.id);

      this.nodes.forEach(n => {
        if (n.children != null) {
          n.children = n.children.filter((child: any) => child.id !== node.id);

          n.numberOfChildren = n.children.length;
        }
      });

      if (node.type === 'Site') {
        this.refreshMapPoints(false);
      }
    });
  }


  handleDownload(node: SiteEntity): void {
    window.location.href = acp + '/project/download?id=' + node.component + "&key=" + node.key;

    //this.service.download( node.data.component, node.data.key, true ).subscribe( blob => {
    //    importedSaveAs( blob, node.data.name );
    //} );
  }

  handleImageDownload(image: any): void {
    window.location.href = acp + '/project/download?id=' + image.component + "&key=" + image.key;

    //this.service.download( node.data.component, node.data.key, true ).subscribe( blob => {
    //    importedSaveAs( blob, node.data.name );
    //} );
  }

  handleStyle(layer: any): void {

    this.baseLayers.forEach(baseLayer => {
      baseLayer.selected = false;
    });

    layer.selected = true;

    this.map.setStyle('mapbox://styles/mapbox/' + layer.id);
  }

  highlightMapFeature(id: string): void {

    this.map.setFilter('hover-points', ['all',
      ['==', 'oid', id]
    ])

  }

  clearHighlightMapFeature(): void {

    this.map.setFilter('hover-points', ['all',
      ['==', 'oid', "NONE"]
    ])

  }

  onListEntityHover(event: any, site: SiteEntity): void {
    if (this.current == null) {
      this.highlightMapFeature(site.id);
    }
  }

  onListEntityHoverOff(): void {
    this.clearHighlightMapFeature();
  }

  highlightListItem(id: string): void {
    this.nodes.forEach(node => {
      if (node.id === id) {
        this.hoverFeatureId = id;
      }
    })
  }

  clearHighlightListItem(): void {
    if (this.hoverFeatureId) {
      this.nodes.forEach(node => {
        if (node.id === this.hoverFeatureId) {
          this.hoverFeatureId = null;
        }
      })
    }
  }


  handleClick($event: any): void {
    let result = $event.item;

    if (result.center) {
      this.map.flyTo({
        center: result.center,
        zoom: 18
      })
    }
    else {
      const index = result.hierarchy.length - 1;

      const selected = result.hierarchy[index];

      this.handleViewSite(selected.id);
    }
  }

  handleViewSite(id: string): void {
    this.service.view(id).then(response => {
      const node = response.item;
      const breadcrumbs = response.breadcrumbs;

      if (this.getMetadata(node).leaf) {
        this.breadcrumbs = breadcrumbs;
        this.current = breadcrumbs[breadcrumbs.length - 1];
        this.nodes = this.current.children;

        this.select(node, null, null);
      }
      else {
        const parent = breadcrumbs.length > 0 ? breadcrumbs[breadcrumbs.length - 1] : null;
        this.breadcrumbs = breadcrumbs;

        this.select(node, parent, null);
      }
    });

  }

  handleMapOrtho(product: Product): void {

    const layer = this.getLayerByClassification("ORTHO", product);

    if (layer != null && layer.key != null) {
      if (this.map.getLayer(layer.key) != null) {
        this.map.removeLayer(layer.key);
        this.map.removeSource(layer.key);

        layer.isMapped = false;
        product.orthoMapped = false;
      }
      else {
        this.addImageLayer(layer);

        layer.isMapped = true;
        product.orthoMapped = true;

        if (product.boundingBox != null) {
          let bbox = product.boundingBox;

          let bounds = new LngLatBounds([bbox[0], bbox[2]], [bbox[1], bbox[3]]);

          this.map.fitBounds(bounds, { padding: 50 });
        }
      }
    }
  }

  getLayerByClassification(classification: string, product: Product): GeoserverLayer {
    let len = product.layers.length;

    for (let i = 0; i < len; ++i) {
      let layer: GeoserverLayer = product.layers[i];

      if (layer.classification === classification) {
        return layer;
      }
    }

    return null;
  }

  handleMapDem(product: Product): void {

    const layer = this.getLayerByClassification("DEM_DSM", product);

    if (layer != null && layer.key != null) {
      if (this.map.getLayer(layer.key) != null) {
        this.map.removeLayer(layer.key);
        this.map.removeSource(layer.key);

        layer.isMapped = false;
        product.demMapped = false;
      }
      else {
        this.addImageLayer(layer);

        layer.isMapped = true;
        product.demMapped = true;

        if (product.boundingBox != null) {
          let bbox = product.boundingBox;

          let bounds = new LngLatBounds([bbox[0], bbox[2]], [bbox[1], bbox[3]]);

          this.map.fitBounds(bounds, { padding: 50 });
        }
      }
    }
  }

  addImageLayer(layer: GeoserverLayer) {
    const workspace = encodeURI(layer.workspace);
    const layerName = encodeURI(layer.workspace + ':' + layer.key);

    this.map.addLayer({
      'id': layer.key,
      'type': 'raster',
      'source': {
        'type': 'raster',
        'tiles': [
          '/geoserver/' + workspace + '/wms?layers=' + layerName + '&bbox={bbox-epsg-3857}&format=image/png&service=WMS&version=1.1.1&request=GetMap&srs=EPSG:3857&transparent=true&width=256&height=256'
        ],
        'tileSize': 256
      },
      'paint': {}
    }, "points");
  }

  handleGoto(): void {

    //    -111.12439336274211
    //    39.32066259372583
    //    -111.12342302258116
    // 39.32107716199166

    var bounds = new LngLatBounds([-111.12439336274211, 39.32066259372583, -111.12342302258116, 39.32107716199166]);

    this.map.fitBounds(bounds);
  }


  getMetadata(node: SiteEntity): any {
    const metadata = this.metadataService.getMetadata(node);

    return metadata;
  }


  select(node: SiteEntity, parent: SiteEntity, event: any): void {

    if (event != null) {
      event.stopPropagation();
    }

    if (node != null && node.geometry != null && node.geometry.type === "Point") {
      //this.map.fitBounds(this.allPointsBounds, { padding: 50 });

      this.map.easeTo({
        center: node.geometry.coordinates,
        zoom: 8
      });
    }

    const metadata = this.metadataService.getMetadata(node);

    if (metadata.leaf) {
      const breadcrumbs = [...this.breadcrumbs];

      if (parent != null) {
        breadcrumbs.push(parent);
      }

      if (this.metadataService.getTypeContainsFolders(node)) {
        this.service.getItems(node.id, null).then(nodes => {
          this.showLeafModal(node, nodes, breadcrumbs);
        });
      }
      else {
        this.showLeafModal(this.current, [node], breadcrumbs);
      }
    }
    else if (node.type === "object") {
      // Do nothing there are no children
      //                return this.service.getItems( node.data.id, node.data.name );
    }
    else {
      this.service.getItems(node.id, null).then(nodes => {
        this.current = node;

        if (parent != null) {
          this.addBreadcrumb(parent);
        }

        this.addBreadcrumb(node);
        this.setNodes(nodes);
      });
    }
  }

  addBreadcrumb(node: SiteEntity): void {

    if (this.breadcrumbs.length == 0 || this.breadcrumbs[this.breadcrumbs.length - 1].id !== node.id) {
      this.breadcrumbs.push(node);
    }
  }

  handleExpand(node: SiteEntity, event: any): void {

    if (event != null) {
      event.stopPropagation();
    }

    if (node.children == null || node.children.length == 0) {
      this.service.getItems(node.id, null).then(nodes => {
        node.children = nodes;

        this.expand(node);
      });
    }
    else {
      // this.expand( node );
      node.children = [];
      node.active = false;
    }
  }

  handleGotoSite(product: Product): void {
    const entity = product.entities[product.entities.length - 1];

    const breadcrumbs = product.entities;

    this.service.getItems(entity.id, null).then(nodes => {
      this.showLeafModal(entity, nodes, breadcrumbs);
    });
  }


  back(node: SiteEntity): void {

    if (node != null) {
      if (node.geometry != null && node.geometry.type === "Point") {
        //this.map.fitBounds(this.allPointsBounds, { padding: 50 });

        this.map.easeTo({
          center: node.geometry.coordinates,
          zoom: 8
        });
      }

      this.service.getItems(node.id, null).then(nodes => {
        var indexOf = this.breadcrumbs.findIndex(i => i.id === node.id);

        this.current = node;
        this.breadcrumbs.splice(indexOf + 1);
        this.setNodes(nodes);
      });
    }
    else if (this.breadcrumbs.length > 0) {
      this.loadingSites = true;
      this.service.roots(null, null).then(nodes => {
        this.loadingSites = false;
        this.breadcrumbs = [];
        this.setNodes(nodes);
        this.staticTabs.tabs[0].active = true;

        this.map.fitBounds(this.allPointsBounds, { padding: 50 });

        // This hack exists because the handleExtentChange method gets called immediately after we do fitBounds
        // and it gets called with some really closely zoomed-in bbox which dumps our nodes we just fetched...
        let that = this;
        window.setTimeout(function () {
          that.current = null;
        }, 500);
      });
    }
  }

  expand(node: SiteEntity) {
    node.active = true;
    this.current = node;
  }

  setNodes(nodes: SiteEntity[]): void {
    this.nodes = [];
    this.supportingData = [];

    nodes.forEach(node => {
      if (node.type === 'folder') {
        this.supportingData.push(node);
      }
      else {
        this.nodes.push(node);
      }
    })
  }

  showLeafModal(collection: SiteEntity, folders: SiteEntity[], breadcrumbs: SiteEntity[]): void {

    if (collection.type === 'Mission') {
      this.bsModalRef = this.modalService.show(AccessibleSupportModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        class: 'leaf-modal modal-lg'
      });
      this.bsModalRef.content.init(collection, folders, breadcrumbs);
    }
    else {
      this.bsModalRef = this.modalService.show(CollectionModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        class: 'leaf-modal modal-lg'
      });
      this.bsModalRef.content.init(collection, folders, breadcrumbs);
    }
  }
}
