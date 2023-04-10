import { PageResult } from '@shared/model/page';
import { Sensor } from './sensor';
import { Platform } from './platform';
import { UAV } from './uav'

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
	description?: string;
	tool?: string;
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
}

export class CollectionArtifact {
	report?: boolean;
	items?: [SiteEntity];
}

export class CollectionArtifacts {
	dem?: CollectionArtifact;
	ortho?: CollectionArtifact;
	ptcloud?: CollectionArtifact;
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
	value: string;
	label: string;
	uav?: string;
	sensor?: string;
	shortName?: string;
	restricted?: boolean;
	sunsetDate?: string;
	contractingOffice?: string;
	vendor?: string;
	collectionDate?: string;
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
};

export class UploadForm {
	create?: boolean;
	name?: string;
	outFileName?: string;
	uasComponentOid?: string;
	site?: string;
	project?: string;
	mission?: string;
	collection?: any;
	imagery?: any;
	uploadTarget?: string;
	selections?: string;
	processUpload?: boolean;
	processOrtho?: boolean;
	processDem?: boolean;
	processPtcloud?: boolean;
	tool?: string;
	description?: string;
	includeGeoLocationFile?: boolean;
	resolution?: number;
	videoResolution?: number;
	matcherNeighbors?: number;
	minNumFeatures?: number;
	pcQuality?: string;
}

export class Action {
	createDate: string;
	lastUpdateDate: string;
	type: string;
	description: string;
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
}

export class Product {
	id: string;
	name: string;
	entities: SiteEntity[];
	published: boolean;
	imageKey?: string;
	orthoKey?: string;
	demKey?: string;
	boundingBox?: number[];
	layers: MapLayer[];
	orthoMapped?: boolean;
	demMapped?: boolean;
	hasPointcloud?: boolean;
	hasAllZip?: boolean;
}

export class MapLayer {
	workspace?: string;
	classification: string;
	key: string;
	isMapped?: boolean;
	public: boolean;
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
	sensor: Sensor;
	platform: Platform;
	uav: UAV;
	page?: PageResult<ProductDocument>;
}
