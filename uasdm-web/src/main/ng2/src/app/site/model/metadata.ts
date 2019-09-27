export class Metadata {
    drillable: boolean;
    expandable: boolean;
    leaf: boolean;
    upload: boolean;

    constructor( drillable: boolean, expandable: boolean, leaf: boolean, upload: boolean ) {
        this.drillable = drillable;
        this.expandable = expandable;
        this.leaf = leaf;
        this.upload = upload;
    }
}