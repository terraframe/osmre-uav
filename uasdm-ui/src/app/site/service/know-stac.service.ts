///
///
///

import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';


import { EventService } from '@shared/service/event.service';

import { environment } from 'src/environments/environment';
import { StacCollection, StacItem, StacProperty } from '@site/model/layer';
import { ConfigurationService } from '@core/service/configuration.service';
import { firstValueFrom } from 'rxjs';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { ErrorModalComponent } from '@shared/component';



@Injectable()
export class KnowStacService {

	constructor(private http: HttpBackendClient, private modalService: BsModalService, private config: ConfigurationService) { }

	search(bbox: number[]): Promise<StacCollection> {

		// const collection: StacCollection = {
		// 	"id": "eyJwcm9wZXJ0aWVzIjp7fSwiYmJveCI6bnVsbH0=",
		// 	"title": "Query result collection",
		// 	"description": null,
		// 	"extent": {
		// 		"spatial": {
		// 			"bbox": [
		// 				[
		// 					-116.6291740650998,
		// 					25.44061412041646,
		// 					-80.50456496756331,
		// 					43.840090901385466
		// 				],
		// 				[
		// 					-111.12465947157641,
		// 					39.32045349943389,
		// 					-111.12332641085925,
		// 					39.32117283487989
		// 				],
		// 				[
		// 					-116.62912544086244,
		// 					43.83765650228306,
		// 					-116.62513075929621,
		// 					43.840090642138826
		// 				],
		// 				[
		// 					-81.75436410089196,
		// 					41.30303726259979,
		// 					-81.7501841628447,
		// 					41.30546319102727
		// 				],
		// 				[
		// 					-111.12440902273818,
		// 					39.32065872624324,
		// 					-111.12342568328083,
		// 					39.32107759237906
		// 				],
		// 				[
		// 					-116.6291740650998,
		// 					43.83763770428788,
		// 					-116.62513199976934,
		// 					43.840090901385466
		// 				],
		// 				[
		// 					-105.41494926765631,
		// 					39.93750047332301,
		// 					-105.40748333758673,
		// 					39.941380651257816
		// 				],
		// 				[
		// 					-80.50636381110417,
		// 					25.44061412041646,
		// 					-80.50456496756331,
		// 					25.44274680830498
		// 				],
		// 				[
		// 					-80.50644858256679,
		// 					25.440972162460067,
		// 					-80.50496028663058,
		// 					25.442633887948773
		// 				]
		// 			]
		// 		},
		// 		"temporal": {
		// 			"interval": [
		// 				"2023-06-02T00:00:00.000+0000",
		// 				"2024-08-15T22:22:57.000+0000"
		// 			]
		// 		}
		// 	},
		// 	"links": [
		// 		{
		// 			"href": "/api/query/collection?criteria=eyJwcm9wZXJ0aWVzIjp7fSwiYmJveCI6bnVsbH0%3D",
		// 			"rel": "self",
		// 			"type": "application/json"
		// 		},
		// 		{
		// 			"href": "/api/item/get?id=10b13809-f31b-46ec-84ae-9af239ec28f5",
		// 			"rel": "item",
		// 			"type": "application/geo+json",
		// 			"title": "hij"
		// 		},
		// 		{
		// 			"href": "/api/item/get?id=bdfe18f9-04dd-4df5-ac4b-63237c5c3d10",
		// 			"rel": "item",
		// 			"type": "application/geo+json",
		// 			"title": "200 feet (A7R)"
		// 		},
		// 		{
		// 			"href": "/api/item/get?id=0077ebc5-2226-46b6-86ea-6b0edec56569",
		// 			"rel": "item",
		// 			"type": "application/geo+json",
		// 			"title": "Collection"
		// 		},
		// 		{
		// 			"href": "/api/item/get?id=d03fa932-c852-48ea-8523-739a204bd368",
		// 			"rel": "item",
		// 			"type": "application/geo+json",
		// 			"title": "Collection TWO"
		// 		},
		// 		{
		// 			"href": "/api/item/get?id=5f6c046b-54e2-4026-b4b7-d8a8d44d8c10",
		// 			"rel": "item",
		// 			"type": "application/geo+json",
		// 			"title": "A7R"
		// 		},
		// 		{
		// 			"href": "/api/item/get?id=7b63d5d3-c0c6-4359-944f-5e83792ae82f",
		// 			"rel": "item",
		// 			"type": "application/geo+json",
		// 			"title": "post_pileburn"
		// 		},
		// 		{
		// 			"href": "/api/item/get?id=162667b1-8496-4f4f-bccc-ef3491f262fb",
		// 			"rel": "item",
		// 			"type": "application/geo+json",
		// 			"title": "Anafi "
		// 		},
		// 		{
		// 			"href": "/api/item/get?id=0eafc08c-6860-4c36-adaa-6fcbf6694c02",
		// 			"rel": "item",
		// 			"type": "application/geo+json",
		// 			"title": "SiteScan Solo"
		// 		}
		// 	],
		// 	"stac_version": "1.0.0",
		// 	"stac_extensions": []
		// }

		// const promise = new Promise<StacCollection>(function (myResolve, myReject) {

		// 	myResolve(collection); // when successful
		// });

		// return promise;

		let params: HttpParams = new HttpParams();
		params = params.set('criteria', btoa(JSON.stringify({ bbox: bbox })));

		return firstValueFrom(this.http.get<StacCollection>(this.config.getKnowStacURL() + 'api/query/collection', { params: params }))
			.catch(e => {
				const bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
				bsModalRef.content.message = "There was a problem communicating with the KnowSTAC server. Please try your request again later"

				return e;
			});
	}

	item(href: string): Promise<StacItem> {

		// href = "https://osmre-uas-dev-public.s3.amazonaws.com/-stac-/a5569fce-98ea-49d1-b5e7-707a245084c9.json";

		return firstValueFrom(this.http.get<StacItem>(href)).catch(e => {
			const bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
			bsModalRef.content.message = "There was a problem communicating with the KnowSTAC server. Please try your request again later"

			return e;
		});

	}

	properties(): Promise<StacProperty[]> {

		// const properties: StacProperty[] = [
		// 	{
		// 		"name": "datetime",
		// 		"label": "Date Time",
		// 		"type": "DATE",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "agency",
		// 		"label": "Agency",
		// 		"type": "ORGANIZATION",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "collection",
		// 		"label": "Collection",
		// 		"type": "STRING",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "description",
		// 		"label": "Description",
		// 		"type": "STRING",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "platform",
		// 		"label": "Platform",
		// 		"type": "STRING",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "project",
		// 		"label": "Project",
		// 		"type": "STRING",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "sensor",
		// 		"label": "Sensor",
		// 		"type": "STRING",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "site",
		// 		"label": "Site",
		// 		"type": "STRING",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "title",
		// 		"label": "Title",
		// 		"type": "STRING",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "faaNumber",
		// 		"label": "UAV FAA Number",
		// 		"type": "ENUMERATION",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "serialNumber",
		// 		"label": "UAV Serial Number",
		// 		"type": "ENUMERATION",
		// 		"location": null
		// 	},
		// 	{
		// 		"name": "operational",
		// 		"label": "USFS Operational",
		// 		"type": "LOCATION",
		// 		"location": {
		// 			"synchronizationId": "010c0c72-d2ec-458b-ac64-c43e79000554",
		// 			"forDate": 1704067200000,
		// 			"label": "USFS Operational"
		// 		}
		// 	}
		// ];

		// const promise = new Promise<StacProperty[]>(function (myResolve, myReject) {

		// 	myResolve(properties); // when successful
		// });

		// return promise;

		let params: HttpParams = new HttpParams();

		return firstValueFrom(this.http.get<StacProperty[]>(this.config.getKnowStacURL() + 'api/stac-property/get-all', { params: params })).catch(e => {
			const bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
			bsModalRef.content.message = "There was a problem communicating with the KnowSTAC server. Please try your request again later"

			return e;
		});
		;
	}


}
