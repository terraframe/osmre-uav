
export class Filter {
    id: string;
    label: string;
    field: string;
    value?: string;
    startDate?: string;
    endDate?: string;
};

export class StacLayer {
    id: string;
    startDate: string = "";

    endDate: string = "";

    /* 
     * Layer name
     */
    layerName: string = "";

    /*
     * Criteria
     */
    filters: Filter[] = [];
}