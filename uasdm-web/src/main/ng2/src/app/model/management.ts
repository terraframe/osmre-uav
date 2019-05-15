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
}

export class SiteEntity {
    id: string;
    name: string;
    folderName: string;
    type: string;
    geometry: any;
    hasChildren: boolean;
    ownerName: string;
    ownerPhone: string;
    ownerEmail: string;
    privilegeType: string;
}

export class UploadForm {
    site: string;
    project: string;
    mission: string;
    collection: string;
    create: boolean;
    name: string;
    outFileName: string;
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
    lastUpdatedDate: string;
    status: string;
    message: string;
    actions: Action[];
    uploadId: string;
}

export class Message {
    collectionId: string;
    message: string;
    imageWidth: string;
    imageHeight: string;
}
