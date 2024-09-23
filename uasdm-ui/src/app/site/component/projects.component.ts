///
///
///

import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, TemplateRef, Inject } from "@angular/core";
import { BsModalService } from "ngx-bootstrap/modal";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Map, LngLatBounds, NavigationControl, MapboxEvent, AttributionControl, GeoJSONSource } from "mapbox-gl";
import { v4 as uuid } from "uuid";

import { Observable, Subject } from "rxjs";
import { debounceTime, distinctUntilChanged } from "rxjs/operators";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { BasicConfirmModalComponent } from "@shared/component/modal/basic-confirm-modal.component";
import { NotificationModalComponent } from "@shared/component/modal/notification-modal.component";
import { AuthService } from "@shared/service/auth.service";

import { SiteEntity, Product, Task, MapLayer, ViewerSelection, SELECTION_TYPE, Filter, CollectionProductView } from "../model/management";

import { EntityModalComponent } from "./modal/entity-modal.component";
import { CollectionModalComponent } from "./modal/collection-modal.component";
import { AccessibleSupportModalComponent } from "./modal/accessible-support-modal.component";

import { ManagementService } from "../service/management.service";
import { MapService } from "../service/map.service";
import { MetadataService } from "../service/metadata.service";
import { CookieService } from "ngx-cookie-service";

import {
  fadeInOnEnterAnimation,
  fadeOutOnLeaveAnimation
} from "angular-animations";
import { ActivatedRoute } from "@angular/router";
import { CreateCollectionModalComponent } from "./modal/create-collection-modal.component";
import { CreateStandaloneProductModalComponent } from "./modal/create-standalone-product-group-modal.component";
import { UIOptions } from "fine-uploader";
import { FineUploaderBasic } from "fine-uploader/lib/core";
import { UploadModalComponent } from "./modal/upload-modal.component";
import { LayerColor, StacCollection, StacItem, StacLink, StacProperty, ToggleableLayer, ToggleableLayerType } from "@site/model/layer";

import { BBox, bbox, bboxPolygon, centroid, envelope, featureCollection } from "@turf/turf";
import EnvironmentUtil from "@core/utility/environment-util";
import { environment } from "src/environments/environment";
import { ConfigurationService } from "@core/service/configuration.service";
import { WebSockets } from "@core/utility/web-sockets";
import { LPGSync } from "@shared/model/lpg";
import { LPGSyncService } from "@shared/service/lpg-sync.service";
import { FilterModalComponent } from "./modal/filter-modal.component";
import { LocalizedValue } from "@shared/model/organization";
import { ProductService } from "@site/service/product.service";
import { ProductModalComponent } from "./modal/product-modal.component";
import { KnowStacModalComponent } from "./know-stac-modal/know-stac-modal.component";
import { KnowStacService } from "@site/service/know-stac.service";


const enum PANEL_TYPE {
  SITE = 0,
  STAC = 1,
  LPG = 2,
  KNOWSTAC = 3,
  IMAGES = 4
}

@Component({
  selector: "projects",
  templateUrl: "./projects.component.html",
  styleUrls: ["./projects.css"],
  animations: [
    fadeInOnEnterAnimation(),
    fadeOutOnLeaveAnimation()
  ]
})
export class ProjectsComponent implements OnInit, AfterViewInit, OnDestroy {

  // imageToShow: any;
  userName: string = "";

  /*
   * Template for the delete confirmation
   */
  @ViewChild("confirmTemplate") public confirmTemplate: TemplateRef<any>;

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
  breadcrumbs: ViewerSelection[] = [];

  /* 
   * Root nodes of the tree
   */
  current: ViewerSelection;

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
    label: "Outdoors",
    id: "outdoors-v11",
    selected: true
  }, {
    label: "Satellite",
    id: "satellite-v9"
  }, {
    label: "Streets",
    id: "streets-v11"
  }, {
    label: "OSM",
    id: "osm"
  }];

  layers: MapLayer[] = [];

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

  /* 
   * Filter by bureau
   */
  filter: Filter = {};
  hasFilter: boolean = false;
  // bureaus: { value: string, label: string }[] = [];
  bounds: LngLatBounds = null;
  sort: string = "name";

  mapLayers: ToggleableLayer[] = [];

  mapLayer: ToggleableLayer = null;

  organization?: { code: string, label: LocalizedValue };

  /*
  * Fields for hierarchy layers
  */
  syncs: LPGSync[] = [];

  // oid of currently selected synchronization profile
  hierarchy: {
    oid: string,
    visible: boolean,
    label: string,
    version: string
  } = {
      oid: null,
      visible: true,
      label: "",
      version: null
    };

  // map of the child type metadata
  childMap: { [key: string]: any[] } = {};

  // list of geojson (not including the geometries) objects of the children locations
  children: any[] = [];

  // Cache of all of the selected metadata types. Used to prevent needing multiple trips to the server
  metadataCache: { [key: string]: any } = {};

  // List of direct types of the selected location
  types: Set<any> = new Set();

  // Type of panel being displayed
  panelType: PANEL_TYPE = PANEL_TYPE.SITE;

  // Content of the location panel
  content: string = "";

  tilesLoaded: boolean = false;

  activeTab: string = "";

  collection: StacCollection = null;

  views: CollectionProductView[] = [];

  properties: StacProperty[] = null;

  constructor(
    private configuration: ConfigurationService,
    private service: ManagementService,
    private authService: AuthService,
    private pService: ProductService,
    private stacService: KnowStacService,
    private mapService: MapService,
    private modalService: BsModalService,
    private metadataService: MetadataService,
    private route: ActivatedRoute,
    private cookieService: CookieService,
    private syncService: LPGSyncService
  ) {

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
    this.organization = this.authService.getOrganization();

    this.notifier = webSocket(WebSockets.buildBaseUrl() + "/websocket-notifier/notify");

    this.notifier.subscribe(message => {
      if (message.type === "UPLOAD_JOB_CHANGE") {
        this.tasks.push(message.content);
      }
    });

    const oid = this.route.snapshot.params["oid"];
    const action = this.route.snapshot.params["action"];

    if (oid != null && action != null && action === "collection") {
      this.handleViewSite(oid);
    }

    let uiOptions: UIOptions = {
      debug: false,
      autoUpload: false,
      multiple: false,
      request: {
        endpoint: EnvironmentUtil.getApiUrl() + "/file/upload",
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
        mode: "none"
      },
      validation: {
        allowedExtensions: ["zip", "tar.gz"]
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
      })
    }



  }


  ngOnDestroy(): void {

    if (this.subject != null) {
      this.subject.unsubscribe();
    }

    this.map.remove();

    this.notifier.complete();
  }

  ngAfterViewInit() {

    this.map = new Map({
      container: "map",
      style: "mapbox://styles/mapbox/outdoors-v11",
      zoom: 2,
      attributionControl: false,
      center: [-78.880453, 42.897852]
    });

    this.map.on("load", () => {
      this.initMap();

      this.map.on('data', () => {
        this.tilesLoaded = this.map.areTilesLoaded();
      });

      if (this.organization != null) {
        this.onOrganizationChange();
      }
    });
  }

  initMap(): void {

    this.map.on("style.load", () => {
      this.addLayers();
      this.refreshMapPoints(false);
    });

    this.addLayers();


    this.refreshMapPoints(true);

    // Add zoom and rotation controls to the map.
    this.map.addControl(new NavigationControl(), "bottom-right");
    this.map.addControl(new AttributionControl({ compact: true }), "bottom-left");

    this.map.on("mousemove", e => {
      // e.point is the x, y coordinates of the mousemove event relative
      // to the top-left corner of the map.
      // e.lngLat is the longitude, latitude geographical position of the event
      let coord = e.lngLat.wrap();

      // EPSG:3857 = WGS 84 / Pseudo-Mercator
      // EPSG:4326 = WGS 84 
      // let coord4326 = window.proj4(window.proj4.defs("EPSG:3857"), window.proj4.defs("EPSG:4326"), [coord.lng, coord.lat]);
      // let text = "Long: " + coord4326[0] + " Lat: " + coord4326[1];

      let text = "Lat: " + coord.lat + " Long: " + coord.lng;
      let mousemovePanel = document.getElementById("mousemove-panel");
      mousemovePanel.textContent = text;


      let features = this.map.queryRenderedFeatures(e.point, { layers: ["points"] });

      if (this.current == null || this.current.type === SELECTION_TYPE.LOCATION) {
        if (features.length > 0) {
          let focusFeatureId = features[0].properties.oid; // just the first
          this.map.setFilter("hover-points", ["all",
            ["==", "oid", focusFeatureId]
          ])

          this.highlightListItem(focusFeatureId)
        }
        else {
          this.map.setFilter("hover-points", ["all",
            ["==", "oid", "NONE"]
          ])

          this.clearHighlightListItem();
        }
      }
    });

    this.map.on("zoomend", (e) => {
      this.subject.next(e);
    });

    this.map.on("moveend", (e) => {
      this.subject.next(e);
    });

    this.map.on("click", (e) => {
      // Site selection from map
      let features = this.map.queryRenderedFeatures(e.point, { layers: ["points"] });

      if (features.length > 0) {
        let focusFeatureId = features[0].properties.oid; // just the first

        const index = this.nodes.findIndex(n => n.id === focusFeatureId);

        if (index !== -1) {
          this.select(this.nodes[index], null, null);
        }
        else {
          this.handleViewSite(focusFeatureId);
        }
      }

      // Items
      features = this.map.queryRenderedFeatures(e.point, { layers: ["items"] });

      if (features.length > 0) {
        const id = features[0].properties.id;
        const type = features[0].properties.type;

        if (type === ToggleableLayerType.PRODUCT) {
          this.handleGetProductInfo(id);
        }
        else if (type === ToggleableLayerType.KNOWSTAC && this.collection != null) {
          const link = this.collection.links.find(l => l.id === id);

          if (link != null) {
            this.handleGetKnowStacInfo(link);
          }
        }
      }
    });



    // MapboxGL doesn"t have a good way to detect when moving off the map
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
      this.bsModalRef.content.message = this.configuration.getAppDisclaimer();
      this.bsModalRef.content.submitText = "I Accept";

      (<NotificationModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
        this.cookieService.set("acceptedDisclaimer", "true");
      });
    }

  }

  addLayers(): void {

    // STAC collection source and layer
    this.map.addSource('collection', {
      'type': 'geojson',
      'data': {
        type: "FeatureCollection",
        features: []
      }
    });

    this.map.addLayer({
      'id': 'collection',
      'type': 'fill',
      'source': 'collection',
      'paint': {
        "fill-color": "grey",
        "fill-opacity": 0.3,
        "fill-outline-color": "black"
      }
    });

    // Add the site layers
    this.map.addSource("items", {
      type: "geojson",
      data: {
        "type": "FeatureCollection",
        "features": []
      }
    });

    // Item result layer
    this.map.addLayer({
      "id": "items",
      "type": "circle",
      "source": "items",
      "paint": {
        "circle-radius": 5,
        "circle-color": [
          "case",
          ['==', ['get', "type"], ToggleableLayerType.KNOWSTAC], LayerColor.KNOWSTAC,
          ['==', ['get', "type"], ToggleableLayerType.PRODUCT], LayerColor.PRODUCT,
          LayerColor.STAC
        ]
      }
    });

    // Item result label layer
    this.map.addLayer({
      "id": "items-label",
      "source": "items",
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

    // Add the site layers
    this.map.addSource("sites", {
      type: "geojson",
      data: {
        "type": "FeatureCollection",
        "features": []
      }
    });

    // Point layer
    this.map.addLayer({
      "id": "points",
      "type": "circle",
      "source": "sites",
      "paint": {
        "circle-radius": 10,
        "circle-color": LayerColor.SITE,
        "circle-stroke-width": 2,
        "circle-stroke-color": "#FFFFFF"
      }
    });

    // Hover style
    this.map.addLayer({
      "id": "hover-points",
      "type": "circle",
      "source": "sites",
      "paint": {
        "circle-radius": 13,
        "circle-color": "#cf0000",
        "circle-stroke-width": 2,
        "circle-stroke-color": "#FFFFFF"
      },
      filter: ["all",
        ["==", "id", "NONE"] // start with a filter that doesn"t select anything
      ]
    });


    // Label layer
    this.map.addLayer({
      "id": "points-label",
      "source": "sites",
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
    const bounds = this.map.getBounds();

    if (this.isValidBounds(bounds)) {
      this.bounds = bounds;
    }

    if (this.current == null || this.current.type === SELECTION_TYPE.LOCATION) {
      this.refreshSites();
    }
  }

  refreshSites(): Promise<void> {

    const conditions = this.getConditions();

    this.refreshMapPoints(false);

    this.loadingSites = true;

    return this.service.roots(null, conditions, this.sort).then(nodes => {
      this.setNodes(nodes);
    }).finally(() => {
      this.loadingSites = false;
    })
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

    const conditions = this.getConditions();

    this.mapService.features(conditions).then(data => {
      (<any>this.map.getSource("sites")).setData(data.features);

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
      "class": "upload-modal"
    });
    this.bsModalRef.content.init(this.breadcrumbs.filter(b => b.type === SELECTION_TYPE.SITE).map(b => b.data as SiteEntity));

    this.bsModalRef.content.onCreateComplete.subscribe(oid => {

      this.handleViewSite(oid);
    });
  }

  handleUploadProducts(): void {

    this.bsModalRef = this.modalService.show(CreateStandaloneProductModalComponent, {
      animated: true,
      backdrop: true,
      ignoreBackdropClick: true,
      "class": "upload-modal"
    });
    this.bsModalRef.content.init(this.breadcrumbs.filter(b => b.type === SELECTION_TYPE.SITE).map(b => b.data as SiteEntity));

    this.bsModalRef.content.onCreateComplete.subscribe(oid => {

      this.handleViewStandaloneProduct(oid);
    });
  }


  handleCreate(parent: SiteEntity, type: string): void {
    let parentId = parent != null ? parent.id : null;

    this.service.newChild(parentId, type).then(data => {
      this.bsModalRef = this.modalService.show(EntityModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        "class": "upload-modal"
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
        "class": "upload-modal"
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
        "class": "edit-modal"
      });
      this.bsModalRef.content.init(false, this.userName, this.admin, data.item, data.attributes, this.map.getCenter(), this.map.getZoom());

      this.bsModalRef.content.onNodeChange.subscribe(entity => {
        // Update the node
        entity.children = node.children;
        entity.active = node.active;

        this.refreshEntity(entity, this.nodes);
        this.refreshEntity(entity, this.breadcrumbs.filter(b => b.type === SELECTION_TYPE.SITE).map(b => b.data as SiteEntity));

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

    window.location.href = environment.apiUrl + "/project/download-all?id=" + node.component + "&key=" + node.name;

    //      this.service.downloadAll( data.id ).then( data => {
    //        
    //      } ).catch(( err: HttpErrorResponse ) => {
    //          this.error( err );
    //      } );
  }

  handleDelete(node: SiteEntity, event: any): void {

    event.stopPropagation();

    let sText = "<b>IMPORTANT:</b> [" + node.name + "] will be deleted along with all underlying data including all files in Collections and Accessible Support";

    if (node.type === "Collection") {
      sText = "<b>IMPORTANT:</b> [" + node.name + "] will be deleted along with all underlying data including all files.";
    }

    sText += " This can <b>NOT</b> be undone";

    this.bsModalRef = this.modalService.show(BasicConfirmModalComponent, {
      animated: true,
      backdrop: true,
      ignoreBackdropClick: true,
    });
    this.bsModalRef.content.message = "Are you sure you want to delete [" + node.name + "]?";
    this.bsModalRef.content.subText = sText;
    this.bsModalRef.content.data = node;
    this.bsModalRef.content.type = "DANGER";
    this.bsModalRef.content.submitText = "Delete";

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

      if (node.type === "Site") {
        this.refreshMapPoints(false);
      }
    });
  }


  handleDownload(node: SiteEntity): void {
    window.location.href = environment.apiUrl + "/project/download?id=" + node.component + "&key=" + node.key;

    //this.service.download( node.data.component, node.data.key, true ).subscribe( blob => {
    //    importedSaveAs( blob, node.data.name );
    //} );
  }

  handleImageDownload(image: any): void {
    window.location.href = environment.apiUrl + "/project/download?id=" + image.component + "&key=" + image.key;

    //this.service.download( node.data.component, node.data.key, true ).subscribe( blob => {
    //    importedSaveAs( blob, node.data.name );
    //} );
  }

  handleStyle(layer: any): void {

    this.baseLayers.forEach(baseLayer => {
      baseLayer.selected = false;
    });

    layer.selected = true;

    if (layer.id === 'osm') {
      this.map.setStyle({
        version: 8,
        name: layer.id,
        metadata: {
          "mapbox:autocomposite": true
        },
        sources: {
          osm: {
            type: "raster",
            tiles: [
              "https://osm.gs.mil/tiles/default/{z}/{x}/{y}.png"
            ],
            tileSize: 256
          }
        },
        sprite: "https://demotiles.maplibre.org/styles/osm-bright-gl-style/sprite",
        glyphs: "mapbox://fonts/mapbox/{fontstack}/{range}.pbf",
        layers: [
          {
            id: layer.id,
            type: "raster",
            source: "osm"
          }
        ]
      });
    }
    else {
      this.map.setStyle("mapbox://styles/mapbox/" + layer.id);
    }
  }

  highlightMapFeature(id: string): void {

    this.map.setFilter("hover-points", ["all",
      ["==", "oid", id]
    ])

  }

  clearHighlightMapFeature(): void {

    this.map.setFilter("hover-points", ["all",
      ["==", "oid", "NONE"]
    ])

  }

  onListEntityHover(event: any, site: SiteEntity): void {
    if (this.current == null || this.current.type === SELECTION_TYPE.LOCATION) {
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

    if (result.type === 'SITE') {

      this.hierarchy.oid = null;
      this.onHierarchyChange();

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
    else if (result.type === 'LOCATION') {
      this.handleViewLocation(result.synchronizationId, result.oid);
    }
  }

  handleViewStandaloneProduct(id: string): void {
    this.pService.getDetail(id, 1, 20).then(detail => {
      this.bsModalRef = this.modalService.show(ProductModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        'class': 'product-info-modal'
      });
      this.bsModalRef.content.init(detail);
    });
  }

  handleViewSite(id: string): void {
    this.service.view(id).then(response => {
      const node = response.item;
      const breadcrumbs = response.breadcrumbs;

      if (this.getMetadata(node).leaf) {
        this.breadcrumbs = breadcrumbs.map(b => {
          return {
            type: SELECTION_TYPE.SITE,
            data: b,
            metadata: this.getMetadata(b)
          };
        });

        const site = breadcrumbs[breadcrumbs.length - 1];

        this.current = {
          type: SELECTION_TYPE.SITE,
          data: site,
          metadata: this.getMetadata(site)
        };

        this.nodes = this.current.data.children;

        this.select(node, null, null);
      }
      else {
        const parent = breadcrumbs.length > 0 ? breadcrumbs[breadcrumbs.length - 1] : null;

        this.breadcrumbs = breadcrumbs.map(b => {
          return {
            type: SELECTION_TYPE.SITE,
            data: b,
            metadata: this.getMetadata(b)
          }
        });

        this.select(node, parent, null);
      }
    });

  }


  addImageLayer(layer: MapLayer) {

    let url = layer.url;

    if (EnvironmentUtil.getApiUrl() !== "" && EnvironmentUtil.getApiUrl() != null) {
      url = EnvironmentUtil.getApiUrl() + "/" + url;
    }
    if (!url.startsWith("/")) {
      url = "/" + url;
    }

    this.map.addLayer({
      'id': layer.key,
      'type': 'raster',
      'source': {
        'type': 'raster',
        'url': url
      },
      'paint': {}
    }, "points");

    // Add the layer to the list of layers to be recreated on style change
    this.layers.push(layer);
  }

  removeImageLayer(id: string) {
    this.map.removeLayer(id);
    this.map.removeSource(id);

    this.layers = this.layers.filter(l => l.key !== id);
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
        breadcrumbs.push({
          type: SELECTION_TYPE.SITE,
          data: parent,
          metadata: this.getMetadata(parent)
        });
      }

      if (this.metadataService.getTypeContainsFolders(node)) {
        this.service.getItems(node.id, null, this.getConditions()).then(nodes => {
          this.showLeafModal(node, nodes, breadcrumbs.filter(b => b.type === SELECTION_TYPE.SITE).map(b => b.data as SiteEntity));
        });
      }
      else {
        this.showLeafModal(this.current.data as SiteEntity, [node], breadcrumbs.filter(b => b.type === SELECTION_TYPE.SITE).map(b => b.data as SiteEntity));
      }
    }
    else if (node.type === "object") {
      // Do nothing there are no children
      //                return this.service.getItems( node.data.id, node.data.name );
    }
    else {
      this.service.getItems(node.id, null, this.getConditions()).then(nodes => {
        this.current = {
          type: SELECTION_TYPE.SITE,
          data: node,
          metadata: this.getMetadata(node)
        };

        if (parent != null) {
          this.addBreadcrumb(parent);
        }

        this.addBreadcrumb(node);
        this.setNodes(nodes);

        console.log(this.current);
      });
    }
  }

  addBreadcrumb(node: SiteEntity): void {

    if (this.breadcrumbs.length == 0 || this.breadcrumbs[this.breadcrumbs.length - 1].data.id !== node.id) {

      this.breadcrumbs.push({
        type: SELECTION_TYPE.SITE,
        data: node,
        metadata: this.getMetadata(node)
      });
    }
  }

  handleExpand(node: SiteEntity, event: any): void {

    if (event != null) {
      event.stopPropagation();
    }

    if (node.children == null || node.children.length == 0) {
      this.service.getItems(node.id, null, this.getConditions()).then(nodes => {
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

    this.service.getItems(entity.id, null, this.getConditions()).then(nodes => {
      this.showLeafModal(entity, nodes, breadcrumbs);
    });
  }


  back(breadcrumb: ViewerSelection): void {

    if (breadcrumb == null) {
      if (this.breadcrumbs.length > 0) {

        if (this.hierarchy.oid != null && this.hierarchy.oid.length > 0) {
          this.breadcrumbs = [];
          this.current = null;

          this.onHierarchyChange();
        }
        else {
          this.refreshSites().then(() => {
            this.breadcrumbs = [];
            this.current = null;

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
    }
    else if (breadcrumb.type === SELECTION_TYPE.SITE) {
      const node: SiteEntity = breadcrumb.data as SiteEntity;

      if (node.geometry != null && node.geometry.type === "Point") {
        //this.map.fitBounds(this.allPointsBounds, { padding: 50 });

        this.map.easeTo({
          center: node.geometry.coordinates,
          zoom: 8
        });
      }

      this.service.getItems(node.id, null, this.getConditions()).then(nodes => {
        var indexOf = this.breadcrumbs.findIndex(i => i.type === SELECTION_TYPE.SITE && i.data.id === node.id);

        this.current = breadcrumb;
        this.breadcrumbs.splice(indexOf + 1);
        this.setNodes(nodes);
      });
    }
    else if (breadcrumb.type === SELECTION_TYPE.LOCATION) {
      const row: any = breadcrumb.data;

      var indexOf = this.breadcrumbs.findIndex(i => i.type === SELECTION_TYPE.LOCATION && i.data.properties.uid === row.properties.uid);

      this.breadcrumbs.splice(indexOf);

      this.clearHierarchyLayers();

      this.current = null;
      this.children = [];

      this.handleHierarchyClick(row);
    }
  }

  expand(node: SiteEntity) {
    node.active = true;

    this.current = {
      type: SELECTION_TYPE.SITE,
      data: node,
      metadata: this.getMetadata(node)
    };
  }

  setNodes(nodes: SiteEntity[]): void {
    this.nodes = [];
    this.supportingData = [];

    nodes.forEach(node => {
      if (node.type === "folder") {
        this.supportingData.push(node);
      }
      else {
        this.nodes.push(node);
      }
    })
  }

  showLeafModal(collection: SiteEntity, folders: SiteEntity[], breadcrumbs: SiteEntity[]): void {

    if (collection.type === "Mission") {
      this.bsModalRef = this.modalService.show(AccessibleSupportModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        class: "leaf-modal modal-lg"
      });
      this.bsModalRef.content.init(collection, folders, breadcrumbs);
    }
    else {
      this.bsModalRef = this.modalService.show(CollectionModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        class: "leaf-modal modal-lg"
      });
      this.bsModalRef.content.init(collection, folders, breadcrumbs);
    }
  }


  onContentChange(type: any): void {

    try {

      if (this.map.getLayer(type.code + "-polygon") != null) {
        this.map.removeLayer(type.code + "-polygon");
        this.map.removeLayer(type.code + "-label");

        this.map.removeSource(type.code + "-source");
      }
    }
    catch (e) {
      // Ignore errors
    }

    if (this.content !== type.code) {
      this.content = type.code;
      this.children = this.childMap[this.content];

      // Add a new vector source and layer
      const config = {
        oid: this.hierarchy.oid,
        typeCode: type.code
      };

      this.createHierarchyLayer(type, "#00ffff", 0.5, "parent");
    }
    else {
      this.content = "";
      this.children = [];
    }

  }

  createHierarchyLayer(type: any, color: string, opacity: number, attribute: string): void {

    let protocol = window.location.protocol;
    let host = window.location.host;

    // Add a new vector source and layer
    const config = {
      oid: this.hierarchy.version,
      typeCode: type.code
    };

    this.map.addSource(type.code + "-source", {
      type: "vector",
      tiles: [
        protocol + "//" + host + EnvironmentUtil.getApiUrl() + "/api/labeled-property-graph-synchronization/tile?x={x}&y={y}&z={z}&config=" + encodeURIComponent(JSON.stringify(config))
      ],
      promoteId: "uuid"
    });

    // Add the hierarchy polygon layer
    this.map.addLayer({
      "id": type.code + "-polygon",
      "source": type.code + "-source",
      "type": "fill",
      "paint": {
        "fill-color": color,
        "fill-opacity": opacity,
        "fill-outline-color": "black"
      },
      "source-layer": "context",
      "filter": [
        "all",
        [
          "==",
          this.current.data.properties.uuid,
          ["get", attribute]
        ]
      ],
    }, 'collection');

    // Add the hierarchy label layer
    this.map.addLayer({
      "id": type.code + "-label",
      "source": type.code + "-source",
      "type": "symbol",
      "paint": {
        "text-color": "black",
        "text-halo-color": "#fff",
        "text-halo-width": 2
      },
      "layout": {
        "text-field": ["get", "label"],
        "text-font": ["Open Sans Semibold", "Arial Unicode MS Bold"],
        "text-offset": [0, 0.6],
        "text-anchor": "top",
        "text-size": 12,
      },
      "source-layer": "context",
      "filter": [
        "all",
        [
          "==",
          this.current.data.properties.uuid,
          ["get", attribute]
        ]
      ],
    }, 'collection');

  }

  onOrganizationChange(): void {

    // Reset the hierarchy
    this.hierarchy = {
      oid: null,
      visible: true,
      label: '',
      version: null
    }

    this.syncs = [];

    // Get the list of synchronizations
    if (this.organization != null && this.organization.code.length > 0) {
      this.syncService.getForOrganization(this.organization.code).then(syncs => {
        this.syncs = syncs;
      })
    }

    // Refresh the sites with the filtered organization
    this.onHierarchyChange();
    this.refreshSites();
  }

  clearHierarchyLayers(): void {

    const removeType = (type) => {

      try {
        this.map.removeLayer(type.code + "-polygon");
        this.map.removeLayer(type.code + "-label");

        this.map.removeSource(type.code + "-source");
      }
      catch (e) {
        // Ignore errors
      }
    }

    if (this.current != null && this.current.metadata != null) {
      removeType(this.current.metadata);
    }

    this.types.forEach(type => {
      removeType(type);
    });
  }

  onHierarchyChange(): void {
    this.hierarchy.version = null;

    if (this.hierarchy.oid != null && this.hierarchy.oid.length > 0) {
      const sync = this.syncs.find(sync => sync.oid === this.hierarchy.oid);

      this.hierarchy.version = sync.version;

      // Remove existing hierarchy layers
      this.clearHierarchyLayers();

      this.syncService.roots(this.hierarchy.oid).then(resp => {
        this.hierarchy.label = this.syncs.filter(s => s.oid === this.hierarchy.oid).map(s => s.displayLabel.localizedValue).reduce((a, b) => a);

        this.current = {
          type: SELECTION_TYPE.ROOT,
          data: { properties: { uuid: resp.parent } },
          metadata: null,
          hierarchy: this.hierarchy.oid
        };

        // Pre populate the cache
        this.metadataCache = {};
        resp.metadata.forEach(metadata => {

          metadata.attributes = metadata.attributes.filter(attribute => {
            return (attribute.code == 'code'
              || (!attribute.isDefault
                && attribute.code != 'uuid'));
          });

          this.metadataCache[metadata.code] = metadata;
        });

        this.breadcrumbs = [];

        this.processLocations(resp.roots);

        // // Clear the parent layer
        // (this.map.getSource("parent") as any).setData({
        //   "type": "FeatureCollection",
        //   "features": []
        // });

      });
    } else {
      this.clearHierarchyLayers();

      this.content = "";
      this.current = null;

      this.childMap = {};
      this.children = [];
      this.breadcrumbs = [];


      // // Clear the parent layer
      // (this.map.getSource("parent") as any).setData({
      //   "type": "FeatureCollection",
      //   "features": []
      // });

      // (this.map.getSource("hierarchy") as any).setData({
      //   "type": "FeatureCollection",
      //   "features": []
      // });
    }
  }

  handleViewLocation(synchronizationId: string, oid: string): void {
    this.syncService.getObject(synchronizationId, oid).then(result => {
      this.hierarchy.oid = synchronizationId;

      this.breadcrumbs = result.parents.map(p => {
        return {
          type: SELECTION_TYPE.LOCATION,
          data: p
        }
      });

      this.handleHierarchyClick(result.object);
    });

  }

  handleHierarchyClick(row: any): void {
    if (this.hierarchy.oid != null && this.hierarchy.oid.length > 0) {
      this.clearHierarchyLayers();
      this.children = [];

      const includeMetadata = (this.metadataCache[row.properties.type] == null);

      this.syncService.select(this.hierarchy.oid, row.properties.type, row.properties.uid, includeMetadata).then(result => {

        this.hierarchy.version = result.version;

        if (result.metadata != null) {

          result.metadata.forEach(metadata => {

            metadata.attributes = metadata.attributes.filter(attribute => {
              return (attribute.code == 'code'
                || (!attribute.isDefault
                  && attribute.code != 'uuid'));
            });

            this.metadataCache[metadata.code] = metadata;
          })
        }

        this.current = {
          type: SELECTION_TYPE.LOCATION,
          data: row,
          metadata: this.metadataCache[row.properties.type],
          hierarchy: this.hierarchy.oid
        };

        this.breadcrumbs.push(this.current);


        // Add the parent layer
        const type = this.current.metadata;

        if (type != null) {
          this.createHierarchyLayer(type, "grey", 0.75, "uid");
        }

        this.processLocations(result.children);

        // Zoom to the layer
        if (result.envelope != null) {
          const env = envelope(result.envelope);
          const bounds = bbox(env) as [number, number, number, number];

          this.map.fitBounds(bounds, {
            padding: 20
          });
        }
      });
    }
  }

  processLocations(children: any[]): void {
    // Reset the cached child results
    this.childMap = {};
    this.types = new Set();
    this.children = [];

    let i = 0;

    children.forEach(c => {
      const type = c.properties.type;

      this.types.add(this.metadataCache[type]);

      if (this.childMap[type] == null) {
        this.childMap[type] = [];
      }

      this.childMap[type].push(c);
    });

    if (this.types.size > 0) {
      const type = this.types.values().next().value;

      this.onContentChange(type);
    }
  }


  handleVisibilityChange(): void {
    this.hierarchy.visible = !this.hierarchy.visible;

    if (!this.hierarchy.visible) {
      this.map.setLayoutProperty("parent-polygon", 'visibility', 'none');
      this.map.setLayoutProperty("parent-label", 'visibility', 'none');
      this.map.setLayoutProperty("hierarchy-polygon", 'visibility', 'none');
      this.map.setLayoutProperty("hierarchy-label", 'visibility', 'none');
    } else {
      this.map.setLayoutProperty("parent-polygon", 'visibility', 'visible');
      this.map.setLayoutProperty("parent-label", 'visibility', 'visible');
      this.map.setLayoutProperty("hierarchy-polygon", 'visibility', 'visible');
      this.map.setLayoutProperty("hierarchy-label", 'visibility', 'visible');
    }
  }


  // Filter functionality

  onFilterOpen(): void {


    this.bsModalRef = this.modalService.show(FilterModalComponent, {
      animated: true,
      backdrop: true,
      ignoreBackdropClick: true,
    });
    this.bsModalRef.content.init(this.filter);

    (<FilterModalComponent>this.bsModalRef.content).onFilterChange.subscribe(filter => {
      this.filter = filter;

      const conditions = this.getConditions();

      this.hasFilter = conditions.array.map(condition => condition.field !== 'bounds' && condition.field !== 'organization').reduce((a, b) => a || b);

      if (this.current == null || this.current.type === SELECTION_TYPE.LOCATION) {
        this.refreshSites();
      }
      else {
        // Refresh the current items
        this.service.getItems(this.current.data.id, null, this.getConditions()).then(nodes => {
          this.setNodes(nodes);
        });
      }
    });
  }

  getConditions(): { hierarchy: any, array: any[] } {
    const conditions = {
      hierarchy: null,
      array: []
    };

    conditions.array.push({ field: 'organization', value: this.organization });
    conditions.array.push({ field: 'collectionDate', value: this.filter.collectionDate });
    conditions.array.push({ field: 'owner', value: this.filter.owner });
    conditions.array.push({ field: 'platform', value: this.filter.platform });
    conditions.array.push({ field: 'projectType', value: this.filter.projectType });
    conditions.array.push({ field: 'sensor', value: this.filter.sensor });
    conditions.array.push({ field: 'uav', value: this.filter.uav });

    conditions.array = conditions.array.filter(c => {
      if (c.field === 'organization') {
        return c.value != null && c.value.code.length > 0
      }

      return c.value != null && c.value.length > 0
    });

    if (this.current != null && this.current.type === SELECTION_TYPE.LOCATION) {
      conditions.hierarchy = {
        oid: this.hierarchy.oid,
        uid: this.current.data.properties.uid
      }
    }

    if (this.bounds != null) {
      conditions.array.push({
        field: "bounds",
        value: this.bounds
      });
    }


    return conditions;
  }

  buildItemsLayer() {
    // Update the items layer with new data
    const features = [];

    if (this.collection != null) {

      this.collection.links.filter(link => link.rel === 'item').forEach(link => {

        features.push(centroid(bboxPolygon(<any>link.bbox), {
          name: link.title,
          type: ToggleableLayerType.KNOWSTAC,
          id: link.id
        }));
      })
    }

    if (this.views != null) {
      this.views.forEach(view => {

        view.products.filter(p => p.boundingBox != null).forEach(product => {
          let bbox = product.boundingBox;

          features.push(centroid(bboxPolygon([bbox[0], bbox[2], bbox[1], bbox[3]]), {
            name: product.name + " - " + product.productName,
            type: ToggleableLayerType.PRODUCT,
            id: product.id
          }));
        });
      });
    }

    (<any>this.map.getSource("items")).setData(featureCollection(features));
  }

  handleCollectionChange(collection: StacCollection): void {

    if (collection != null && collection.extent.spatial != null && collection.extent.spatial.bbox.length > 0) {

      // Setup the bbox information
      for (let i = 1; i < collection.links.length; i++) {
        collection.links[i].bbox = collection.extent.spatial.bbox[i];
        collection.links[i].id = uuid();
      }

      // Setup the map layer
      const collectionBbox: any = collection.extent.spatial.bbox[0];

      // Update the collection layer
      (<any>this.map.getSource("collection")).setData(featureCollection([bboxPolygon(collectionBbox)]));

      this.map.fitBounds(collectionBbox);
    }
    else {
      (<any>this.map.getSource("collection")).setData(featureCollection([]));
    }

    this.collection = collection;

    this.buildItemsLayer();
  }

  handleProductsChange(views: CollectionProductView[]): void {

    this.views = views;

    this.buildItemsLayer();
  }

  handleMapDem(product: Product): void {
    product.demMapped = !product.demMapped;

    this.handleProductAsset(product, 'DEM_DSM', product.demMapped);
  }

  handleMapOrtho(product: Product): void {
    product.orthoMapped = !product.orthoMapped;

    this.handleProductAsset(product, 'ORTHO', product.orthoMapped);
  }

  handleProductAsset(product: Product, classification: string, mapIt: boolean): void {
    const id = product.id + '-' + classification;

    if (mapIt) {

      // Create the map layer from the StacItem
      const index = this.mapLayers.findIndex(l => l.id === id);

      // The layer may already exist
      if (index === -1) {
        const layer: ToggleableLayer = {
          id: id,
          type: ToggleableLayerType.PRODUCT,
          layerName: product.name,
          active: true,
          item: product,
          asset: product.layers.find(l => l.classification === classification)
        };

        this.mapLayers.push(layer);

        this.showToggleableLayer(layer);
      }

      if (product.boundingBox != null) {
        let bbox = product.boundingBox;

        let bounds = new LngLatBounds([bbox[0], bbox[2]], [bbox[1], bbox[3]]);

        this.map.fitBounds(bounds, { padding: 50 });
      }
    }
    else {
      const index = this.mapLayers.findIndex(l => l.id === id);

      if (index !== -1) {
        this.handleRemoveToggleableLayer(this.mapLayers[index])
      }
    }
  }


  handleToggleMapItem(item: StacItem): void {
    if (item.enabled) {

      // Create the map layer from the StacItem
      const layer: ToggleableLayer = {
        id: item.id,
        type: ToggleableLayerType.KNOWSTAC,
        layerName: item.properties.title,
        active: true,
        item: item
      };

      this.mapLayers.push(layer);

      this.showToggleableLayer(layer);
    }
    else {
      const index = this.mapLayers.findIndex(l => l.id === item.id);

      if (index !== -1) {
        this.handleRemoveToggleableLayer(this.mapLayers[index])
      }
    }
  }

  handleViewExtent(link: StacLink): void {
    if (link.bbox != null) {
      this.map.fitBounds(<any>link.bbox);
    }
  }

  handleGotoExtent(layer: ToggleableLayer): void {
    if (layer.type === ToggleableLayerType.KNOWSTAC) {
      this.map.fitBounds(<any>layer.item.bbox);
    }
    else if (layer.type === ToggleableLayerType.PRODUCT) {
      const product: Product = layer.item;

      if (product.boundingBox != null) {
        let bbox = product.boundingBox;

        let bounds = new LngLatBounds([bbox[0], bbox[2]], [bbox[1], bbox[3]]);

        this.map.fitBounds(bounds, { padding: 50 });
      }

    }
  }

  handleLayerVisibilityChange(layer: ToggleableLayer): void {
    if (layer.active) {
      this.showToggleableLayer(layer);
    }
    else {
      this.hideToggleableLayer(layer);
    }
  }

  handleRemoveToggleableLayer(layer: ToggleableLayer): void {

    if (layer.active) {
      this.hideToggleableLayer(layer);
    }

    this.mapLayers = this.mapLayers.filter(f => f.id !== layer.id);
  }

  handleToggleToggleableLayer(layer: ToggleableLayer): void {
    layer.active = !layer.active;

    if (layer.active) {
      this.showToggleableLayer(layer);
    }
    else {
      this.hideToggleableLayer(layer);
    }
  }

  showToggleableLayer(layer: ToggleableLayer): void {

    if (layer.type === ToggleableLayerType.STAC) {

      layer.item.items.forEach(item => {

        const index = item.links.findIndex(link => link.rel === 'self');
        const link = item.links[index];

        let url = "/stac/tilejson.json";
        url += "?url=" + encodeURIComponent(link.href);
        url += "&assets=" + encodeURIComponent(item.asset);

        this.addImageLayer({
          classification: "ORTHO",
          key: layer.id + '-' + item.id + '-' + item.asset,
          isMapped: true,
          url: url
        });
      });
    }
    else if (layer.type === ToggleableLayerType.KNOWSTAC) {
      const item: StacItem = layer.item;

      // Add the layer to the map
      const index = item.links.findIndex(link => link.rel === 'self');
      const link = item.links[index];

      let url = "/stac/tilejson.json";
      url += "?url=" + encodeURIComponent(link.href);
      url += "&assets=" + encodeURIComponent(item.asset);

      this.addImageLayer({
        classification: "ORTHO",
        key: item.id + '-' + item.asset,
        isMapped: true,
        url: url
      });
    }
    else if (layer.type === ToggleableLayerType.PRODUCT) {

      this.addImageLayer({
        classification: layer.asset.classification,
        key: layer.id,
        isMapped: true,
        url: layer.asset.url
      });
    }
  }

  hideToggleableLayer(layer: ToggleableLayer): void {
    if (layer.type === ToggleableLayerType.STAC) {
      layer.item.items.forEach(item => {
        this.removeImageLayer(layer.id + '-' + item.id + '-' + item.asset);
      });
    }
    else if (layer.type === ToggleableLayerType.KNOWSTAC) {
      const item: StacItem = layer.item;

      this.removeImageLayer(item.id + '-' + item.asset);
    }
    else if (layer.type === ToggleableLayerType.PRODUCT) {
      this.removeImageLayer(layer.id);
    }
  }

  /*
   * Original STAC panel management
   */
  handleStacEdit(layer?: ToggleableLayer): void {

    this.panelType = PANEL_TYPE.STAC;

    if (layer != null) {
      this.mapLayer = JSON.parse(JSON.stringify(layer));
    }
    else {
      this.mapLayer = null;
    }
  }

  handleStacConfirm(layer: ToggleableLayer): void {
    const index = this.mapLayers.findIndex(l => l.id === layer.id);

    if (index !== -1) {
      // Remove existing image layers
      if (this.mapLayers[index].active) {
        this.hideToggleableLayer(this.mapLayers[index]);
      }

      this.mapLayers[index] = layer;
    }
    else {
      this.mapLayers.push(layer);
    }

    if (layer.active) {

      this.handleStacZoom(layer);

      this.showToggleableLayer(layer);
    }

    this.panelType = PANEL_TYPE.SITE;
    this.mapLayer = null;
  }

  handleStacZoom(layer: ToggleableLayer): void {

    if (layer.type === ToggleableLayerType.STAC) {
      const polygons = layer.item.items.map(item => bboxPolygon(item.bbox as [number, number, number, number]));

      // Determine the bounding box of the layer
      const features = featureCollection(polygons);
      const env = envelope(features);
      const bounds = bbox(env) as [number, number, number, number];

      this.map.fitBounds(bounds);
    }
  }

  handleStacCancel(): void {
    this.panelType = PANEL_TYPE.SITE;
    this.mapLayer = null;
  }

  handleGetProductInfo(productId: string): void {
    this.pService.getDetail(productId, 1, 20).then(detail => {
      const bsModalRef = this.modalService.show(ProductModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        'class': 'product-info-modal'
      });
      bsModalRef.content.init(detail);
    });
  }

  handleGetKnowStacInfo(link: StacLink): void {

    ((link.item != null) ? new Promise<StacItem>(function (myResolve, myReject) {
      myResolve(link.item);
    }) : this.stacService.item(link.href)).then(item => {
      link.item = item;

      const bsModalRef = this.modalService.show(KnowStacModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
        'class': 'product-info-modal'
      });
      bsModalRef.content.init(item, this.properties);
    });


  }

}
