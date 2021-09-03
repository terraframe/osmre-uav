import { PageResult } from "@shared/model/page";

export interface GenericTableService {
    page(criteria: Object, baseUrl?: string): Promise<PageResult<Object>>;
}

export class GenericTableConfig {
    service: GenericTableService;
    remove: boolean;
    view: boolean;
    create: boolean;
    label: string;
}

export class TableEvent {
    type: string;
    row?: Object;
}