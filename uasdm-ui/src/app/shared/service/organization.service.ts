///
///
///

import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";

import { finalize } from "rxjs/operators";
import { firstValueFrom } from "rxjs";

import { EventService } from '@shared/service/event.service';
import { Organization, OrganizationNode } from "@shared/model/organization";

import { environment } from 'src/environments/environment';
import { PageResult } from "@shared/model/page";

@Injectable()
export class OrganizationService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getOrganizations(): Promise<Organization[]> {
        this.eventService.start();

        return firstValueFrom(
            this.http.get<Organization[]>(environment.apiUrl + "/api/organization/get-all")
                .pipe(finalize(() => {
                    this.eventService.complete();
                }))
        );
    }

    get(code: string): Promise<Organization> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);

        return firstValueFrom(
            this.http.get<Organization>(environment.apiUrl + "/api/organization/get", { params: params })
        );
    }

    search(text: string): Promise<Organization[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("text", text);

        return firstValueFrom(
            this.http.get<Organization[]>(environment.apiUrl + "/api/organization/search", { params: params })
        );
    }

    getChildren(code: string, pageNumber: number, pageSize: number): Promise<PageResult<Organization>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("pageSize", pageSize.toString());

        if (code != null) {
            params = params.set("code", code);
        }

        return firstValueFrom(
            this.http.get<PageResult<Organization>>(environment.apiUrl + "/api/organization/get-children", { params: params })
        );
    }

    getAncestorTree(rootCode: string, code: string, pageSize: number): Promise<OrganizationNode> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("pageSize", pageSize.toString());

        if (rootCode != null) {
            params = params.set("rootCode", rootCode);
        }

        return firstValueFrom(
            this.http.get<OrganizationNode>(environment.apiUrl + "/api/organization/get-ancestor-tree", { params: params })
        );
    }

    page(pageNumber: number, pageSize: number): Promise<PageResult<Organization>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("pageSize", pageSize.toString());

        return firstValueFrom(
            this.http.get<PageResult<Organization>>(environment.apiUrl + "/api/organization/page", { params: params })
        );
    }
}
