import { PageResult } from '@shared/model/page';

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
	metadataUploaded: boolean;
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
	platform?: string;
	sensor?: string;
};


export class UploadForm {
	create: boolean;
	name: string;
	outFileName: string;
	uasComponentOid: string;
	site: string;
	project: string;
	mission: string;
	collection: any;
	imagery: any;
	uploadTarget: string;
	selections: string;
	processUpload: boolean;
}

export class Action {
	createDate: string;
	lastUpdatedDate: string;
	type: string;
	description: string;
}

export class Task {
	oid: string;
	label: string;
	createDate: string;
	lastUpdateDate: string;
	lastUpdatedDate: string;
	status: string;
	message: string;
	actions: Action[];
	uploadId: string;
	odmOutput: string;
	collection: string;
	collectionLabel: string;
	type: string;
	visible?: boolean;
	showError?: boolean;
	ancestors?: string[];
  sensorName?: string;
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
	collectionId: string;
	collectionName: string;
	ancestors: string[];
	message: string;
	imageWidth: string;
	imageHeight: string;
}

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
	boundingBox?: number[];
	layers: GeoserverLayer[];
	orthoMapped?: boolean;
	demMapped?: boolean;
	hasPointcloud?: boolean;
}

export class GeoserverLayer {
  workspace: string;
  classification: string;
  key: string;
  isMapped?: boolean;
}

export class ProductDetail extends Product {
	pilotName: string;
	dateTime: string;
	sensor: string;
	page?: PageResult<ProductDocument>;
}
