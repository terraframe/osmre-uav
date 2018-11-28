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

