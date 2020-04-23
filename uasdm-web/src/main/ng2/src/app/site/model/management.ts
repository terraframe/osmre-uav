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

export class Page<T> {
	count: number;
	pageNumber: number;
	pageSize: number;
	results: T[];
}

export class SiteObjectsResultSet extends Page<SiteEntity> {
	folder: string;
}

export class SiteEntity {
	id: string;
	name: string;
	folderName: string;
	type: string;
	geometry: any;
	numberOfChildren: number;
	ownerName: string;
	ownerPhone: string;
	ownerEmail: string;
	privilegeType: string;
	component: string;
	key: string;
	imageKey: string;
	children: SiteEntity[];
	active: boolean;
	excludeFromProcess: boolean;
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
	visible?: boolean;
}

export class Message {
	collectionId: string;
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
	imageKey: string;
	mapKey: string;
	boundingBox: number[];
	orthoMapped?: boolean;
}

export class ProductDetail extends Product {
	pilotName: string;
	dateTime: string;
	sensor: string;
	page?: Page<ProductDocument>;
}
