///
///
///

import { PageResult } from '@shared/model/page';
import { Sensor } from './sensor';
import { Platform } from './platform';
import { UAV } from './uav'
import { LocalizedValue } from '@shared/model/organization';

export const projectTypes: string[] = [
	"Other",
	"Lands & Realty",
	"Facilities & Infrastructure",
	"Administrative Boundaries",
	"Recreation",
	"Transportation",
	"Weather & Climate",
	"Fire & Aviation",
	"Forest Management",
	"Ecosystems",
	"Vegetation",
	"Hydrography",
	"Imagery",
	"Terrain"
];

export class Condition {
	name: string;
	value: string;
	type: string;
}

export class AttributeType {
	name: string;
	label: string;
	type: string;
	required: boolean;
	immutable: boolean;
	readonly: boolean;
	condition: Condition;
	options?: { value: string, label: string }[];
}

export class SiteObjectsResultSet {
	count: number;
	pageNumber: number;
	pageSize: number;
	results: SiteEntity[];
	folder: string;
}

export class SiteEntity {
	id: string;
	name: string;
	folderName: string;
	type: string;
	component: string;
	key: string;
	metadataUploaded?: boolean;
	geometry?: any;
	numberOfChildren: number;
	lastModified?: string;
	isOwner?: boolean;
	ownerName?: string;
	ownerPhone?: string;
	ownerEmail?: string;
	privilegeType?: string;
	children?: SiteEntity[];
	active?: boolean;
	exclude?: boolean;
	sensor?: Sensor;
	platform?: Platform;
	uav?: UAV;
	pilotName?: string;
	dateTime?: string;
	collectionDate?: string;
	collectionEndDate?: string;
	hasAllZip?: boolean;
	shortName?: string;
	restricted?: boolean;
	sunsetDate?: string;
	contractingOffice?: string;
	vendor?: string;
	northBound?: number;
	southBound?: number;
	eastBound?: number;
	westBound?: number;
	exifIncluded?: boolean;
	acquisitionDateStart?: string;
	acquisitionDateEnd?: string;
	flyingHeight?: number;
	numberOfFlights?: number;
	percentEndLap?: number;
	percentSideLap?: number;
	areaCovered?: number;
	weatherConditions?: string;
	presignedThumbnailDownload?: string;
	isLidar?: boolean;

	// Document metadata fields
	description?: string;
	tool?: string;
	ptEpsg?: number;
	projectionName?: string;
	orthoCorrectionModel?: string;
	isPrivate?: boolean;

}

export class CollectionArtifact {
	report?: boolean;
	items?: SiteEntity[];
}

export class CollectionArtifacts {
	productName?: string;
	primary?: boolean;
	dem?: CollectionArtifact;
	ortho?: CollectionArtifact;
	ptcloud?: CollectionArtifact;
}

export enum ProcessConfigType {
	ODM = 'ODM', LIDAR = 'LIDAR'
}

export class ProcessConfig {
	type: ProcessConfigType;

	// ODM processing options
	productName?: string;
	processPtcloud?: boolean;
	processDem?: boolean;
	processOrtho?: boolean;
	includeGeoLocationFile?: boolean;
	includeGroundControlPointFile?: boolean;
	outFileNamePrefix?: string;
	resolution?: number;
	videoResolution?: number;
	matcherNeighbors?: number;
	minNumFeatures?: number;
	pcQuality?: string;
	featureQuality?: string;
	radiometricCalibration?: string;
	geoLocationFormat?: string;
	geoLocationFileName?: string;
	groundControlPointFileName?: string;

	// LIDAR processing options;
	generateCopc?: boolean;
	generateTreeCanopyCover?: boolean;
	generateGSM?: boolean;
	generateTreeStructure?: boolean;
	generateTerrainModel?: boolean;
	productNamePrefix?: string;
}

export class ODMRun {
	output: string;
	config: ProcessConfig;
	report: SiteEntity;
	runStart: string;
	runEnd: string;
}


export class CollectionHierarchy {
	site: string;
	project: string;
	mission: string;
	collection: string;
}

export class ImageHierarchy {
	site: string;
	project: string;
	image: string;
}

export class Selection {
	type: string;
	isNew: boolean;
	value?: string;
	collectionId?: string;
	productId?: string;
	label: string;
	uav?: string;
	sensor?: string;
	shortName?: string;
	restricted?: boolean;
	sunsetDate?: string;
	projectType?: string;
	contractingOffice?: string;
	vendor?: string;
	collectionDate?: string;
	collectionEndDate?: string;
	pointOfContact?: {
		name: string,
		email: string
	};
	northBound?: number;
	southBound?: number;
	eastBound?: number;
	westBound?: number;
	exifIncluded?: boolean;
	acquisitionDateStart?: string;
	acquisitionDateEnd?: string;
	flyingHeight?: number;
	numberOfFlights?: number;
	percentEndLap?: number;
	percentSideLap?: number;
	areaCovered?: number;
	weatherConditions?: string;
	artifacts?: any[];
	isPrivate?: boolean;
};

export class UploadForm extends ProcessConfig {
	create?: boolean;
	name?: string;
	outFileNamePrefix?: string;
	uasComponentOid?: string;
	site?: string;
	project?: string;
	mission?: string;
	collection?: any;
	imagery?: any;
	selections?: string;

	// Processing configuration fields
	uploadTarget?: string;
	processUpload?: boolean;

	// Document metadata fields
	tool?: string;
	ptEpsg?: number;
	projectionName?: string;
	orthoCorrectionModel?: string;
	description?: string;
}

export class Action {
	createDate: string;
	lastUpdateDate: string;
	type: string;
	description: string;
}

export class Notification {
	text: string;
}


export class Task {
	oid: string;
	label: string;
	createDate: string;
	lastUpdateDate: string;
	status: string;
	message: string;
	actions: Action[];
	uploadId: string;
	odmOutput: string;
	odmRunId: string;
	collection: string;
	collectionLabel: string;
	type: string;
	uploadTarget?: string;
	visible?: boolean;
	showError?: boolean;
	ancestors?: string[];
	sensorName?: string;
	showODMOutput?: boolean;
}

export class TaskGroup {
	label: string;
	collectionId: string;
	productId?: string;
	productName?: string;
	visible?: boolean;
	loading?: boolean;
	groups: TaskGroupType[];
	status: string;
	lastUpdatedDate: string;
	ancestors?: string[];
}

export class TaskGroupType {
	tasks: Task[];
	status: string;
	type: string;
}

export class Message {
	oid: string;
	message: string;
	type: string;
	collectionId?: string;
	data: { [key: string]: any };
}

// export class Message {
// 	collectionId: string;
// 	collectionName: string;
// 	ancestors: string[];
// 	message: string;
// 	imageWidth: string;
// 	imageHeight: string;
// }

export class ProductDocument {
	id: string;
	name: string;
	key: string;
	presignedThumbnailDownload?: string;
}

export class ProductCriteria {
	type: SELECTION_TYPE;
	id?: string;
	hierarchy?: string;
	conditions?: any[];
	uid?: string;
	sortField: string;
	sortOrder: string;
}

export class Product {
	id: string;
	name: string;
	componentType: string;
	productName: string;
	entities: SiteEntity[];
	published: boolean;
	imageKey?: string;
	orthoKey?: string;
	demKey?: string;
	boundingBox?: number[];
	layers: MapAsset[];
	orthoMapped?: boolean;
	demMapped?: boolean;
	hasPointcloud?: boolean;
	hasAllZip?: boolean;
	publicStacUrl?: string;
	locked?: boolean
	primary?: boolean;
	removable?: boolean;
}

export class UserAccess {

	oid: string;
	component: string;
	type: string;
	name: string;
}



export class CollectionProductView {
	componentId: string;
	componentType: string;
	products: Product[];

	productId?: string;
	product?: Product;
}

export class MapAsset {
	classification: string;
	key: string;
	public: boolean;
	url: string;
}


export class MapLayer {
	classification: string;
	key: string;
	isMapped?: boolean;
	url: string;
}

//export class ProductDetail extends Product {
//	pilotName: string;
//	dateTime: string;
//	sensorName: string;
//    sensorId: string;
//    sensorType: string;
//    sensorModel: string;
//    sensorDescription: string;
//	page?: PageResult<ProductDocument>;
//}

export class ProductDetail extends Product {
	pilotName: string;
	dateTime: string;
	collectionDate: string;
	collectionEndDate?: string;
	sensor: Sensor;
	platform: Platform;
	uav: UAV;
	page?: PageResult<ProductDocument>;
}

export enum SELECTION_TYPE {
	SITE = 0,
	LOCATION = 1,
	ROOT = 2
}

export class ViewerSelection {
	type: SELECTION_TYPE;
	data: {
		folderName?: string;
		hasAllZip?: boolean;
		id?: string;
		isOwner?: boolean;
		metadataUploaded?: boolean;
		name?: string;
		numberOfChildren?: number;
		shortName?: string;
		type?: string;
		typeLabel?: string;
		children?: any[];
		component?: string;
		key?: string;
		properties?: any;
	};
	metadata: {
		childLabel?: string;
		drillable?: boolean;
		expandable?: boolean;
		label?: string;
		leaf?: boolean;
		processable?: boolean;
		root?: boolean;
		uploadable?: boolean;
		attributes?: any[];
	};
	hierarchy?: string;
}

export class Filter {

	collectionDate?: string;
	sensor?: string;
	uav?: string;
	platform?: string;
	owner?: string;
	projectType?: string;
	organization?: { code: string, label: LocalizedValue };
}
