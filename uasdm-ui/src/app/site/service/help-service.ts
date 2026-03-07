import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { LocalizedValue } from "@shared/model/organization";
import { EventService } from "@shared/service/event.service";
import { Observable, finalize } from "rxjs";
import { environment } from "src/environments/environment";

export interface Organization {
  code: string;
  label: LocalizedValue;
}

export interface HelpPageContentResponse {
  markdown: string;
  organization: Organization;
}

@Injectable({ providedIn: "root" })
export class HelpService {
  constructor(
    private eventService: EventService,
    private http: HttpClient
  ) {}

  content(orgCode: string): Observable<HelpPageContentResponse> {
    let params = new HttpParams();
    
    if (orgCode != null)
      params = params.set("orgCode", orgCode);

    return this.http.get<HelpPageContentResponse>(
      `${environment.apiUrl}/api/help/content`,
      { params }
    );
  }

  edit(orgCode: string, content: string): Observable<void> {
    const headers = new HttpHeaders({
      "Content-Type": "application/json"
    });

    const body = {
      orgCode,
      content
    };

    this.eventService.start();

    return this.http
      .post<void>(`${environment.apiUrl}/api/help/edit`, body, { headers })
      .pipe(
        finalize(() => {
          this.eventService.complete();
        })
      );
  }
}