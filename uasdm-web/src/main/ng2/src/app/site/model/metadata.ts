export class Metadata {
    root: boolean;
    leaf: boolean;
    drillable: boolean;
    expandable: boolean;
    uploadable: boolean;
    processable: boolean;

    constructor( root: boolean, leaf: boolean, drillable: boolean, expandable: boolean, uploadable: boolean, processable: boolean ) {
        this.root = root;
        this.leaf = leaf;
        this.drillable = drillable;
        this.expandable = expandable;
        this.uploadable = uploadable;
        this.processable = processable;
    }
}