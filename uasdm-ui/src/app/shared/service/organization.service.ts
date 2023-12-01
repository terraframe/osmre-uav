///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
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
}
