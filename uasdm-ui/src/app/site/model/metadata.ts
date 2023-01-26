export class Metadata {
    root: boolean;
    leaf: boolean;
    drillable: boolean;
    expandable: boolean;
    uploadable: boolean;
    processable: boolean;
    childLabel: string;

    constructor( root: boolean, leaf: boolean, drillable: boolean, expandable: boolean, uploadable: boolean, processable: boolean, childLabel: string ) {
        this.root = root;
        this.leaf = leaf;
        this.drillable = drillable;
        this.expandable = expandable;
        this.uploadable = uploadable;
        this.processable = processable;
        this.childLabel = childLabel;
    }
}