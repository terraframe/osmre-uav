export class Metadata {
    drillable: boolean;
    expandable: boolean;
    leaf: boolean;
    uploadable: boolean;
    processable: boolean;

    constructor( drillable: boolean, expandable: boolean, leaf: boolean, uploadable: boolean, processable: boolean ) {
        this.drillable = drillable;
        this.expandable = expandable;
        this.leaf = leaf;
        this.uploadable = uploadable;
        this.processable = processable;
    }
}