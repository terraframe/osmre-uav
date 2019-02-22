export class SiteEntity {
    id: string;
    name: string;
    hasChildren: boolean;
}

export class UploadForm {
    site: string;
    project: string;
    mission: string;
    collection: string;
    create: boolean;
    name: string;
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
    missionId: string;
    message: string;
}
