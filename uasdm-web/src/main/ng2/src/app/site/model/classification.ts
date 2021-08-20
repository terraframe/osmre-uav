import { PageResult } from "@shared/model/page";

export class Classification {
    oid: string;
    code: string;
    label: string;
    seq: number;
}

export interface ClassificationService {
    page(p: number): Promise<PageResult<Classification>>;

    edit(oid: string): Promise<Classification>;

    newInstance(): Promise<Classification>;

    remove(oid: string): Promise<void>;

    apply(sensor: Classification): Promise<Classification>;
}

export class ComponentMetadata {
    title: string;
    label: string;
    service: ClassificationService;
}