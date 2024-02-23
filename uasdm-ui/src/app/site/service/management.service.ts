///
///
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { LngLatBounds } from 'mapbox-gl';
import { Observable } from 'rxjs';

// import 'rxjs/add/operator/toPromise';
import { finalize, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import { AuthService } from '@shared/service/auth.service';
import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { SiteEntity, Message, Task, AttributeType, Condition, SiteObjectsResultSet, TaskGroup, Selection, CollectionArtifacts, ODMRun, ODMRunConfig } from '../model/management';
import { Sensor } from '../model/sensor';
import { Platform } from '../model/platform';
import { PageResult } from '@shared/model/page';
import { Criteria, StacItem } from '@site/model/layer';
import { environment } from 'src/environments/environment';
import { MetadataResponse } from '@site/model/uav';



@Injectable()
export class ManagementService {

	constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService, private authService: AuthService) { }

	getChildren(id: string): Promise<SiteEntity[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);


		return this.http
			.get<SiteEntity[]>(environment.apiUrl + '/project/get-children', { params: params })
			.toPromise()
	}

	getObjects(id: string, key: string, pageNumber: number, pageSize: number, presigned: boolean = false): Promise<SiteObjectsResultSet> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);

		if (key != null) {
			params = params.set('key', key);
		}

		if (pageNumber != null) {
			params = params.set('pageNumber', pageNumber.toString());
		}
		if (pageSize != null) {
			params = params.set('pageSize', pageSize.toString());
		}
		
		let method = presigned ? "objects-presigned" : "objects";

		return this.http
			.get<SiteObjectsResultSet>(environment.apiUrl + '/project/' + method, { params: params })
			.toPromise()
	}

	view(id: string): Promise<{ breadcrumbs: SiteEntity[], item: SiteEntity }> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);
		
		this.eventService.start();

		return this.http
			.get<{ breadcrumbs: SiteEntity[], item: SiteEntity }>(environment.apiUrl + '/project/view', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	getItems(id: string, key: string, conditions: { hierarchy: any, array: { field: string, value: any }[] }): Promise<SiteEntity[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);

		if (key != null) {
			params = params.set('key', key);
		}

		if (conditions != null) {
			params = params.set('conditions', JSON.stringify(conditions));
		}

		this.eventService.start();


		return this.http
			.get<SiteEntity[]>(environment.apiUrl + '/project/items', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	getArtifacts(id: string): Promise<CollectionArtifacts> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);

		return this.http
			.get<CollectionArtifacts>(environment.apiUrl + '/project/get-artifacts', { params: params })
			.toPromise()
	}

	removeArtifacts(id: string, folder: string): Promise<CollectionArtifacts> {
		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		const params = {
			id: id,
			folder: folder
		};

		this.eventService.start();

		return this.http
			.post<CollectionArtifacts>(environment.apiUrl + '/project/remove-artifacts', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	getDefaultODMRunConfig(collectionId: string): Promise<ODMRunConfig> {
		let params: HttpParams = new HttpParams();
		params = params.set('collectionId', collectionId);

		return this.http
			.get<ODMRunConfig>(environment.apiUrl + '/project/get-default-odm-run-config', { params: params })
			.toPromise()
	}

	getODMRunByArtifact(artifactId: string): Promise<ODMRun> {
		let params: HttpParams = new HttpParams();
		params = params.set('artifactId', artifactId);

		return this.http
			.get<ODMRun>(environment.apiUrl + '/product/get-odm-run', { params: params })
			.toPromise()
	}

	getODMRunByTask(taskId: string): Promise<ODMRun> {
		let params: HttpParams = new HttpParams();
		params = params.set('taskId', taskId);

		return this.http
			.get<ODMRun>(environment.apiUrl + '/project/get-odm-run-by-task', { params: params })
			.toPromise()
	}

	roots(id: string, conditions: { hierarchy: any, array: { field: string, value: any }[] }, sort?: string): Promise<SiteEntity[]> {
		let params: HttpParams = new HttpParams();

		if (id != null) {
			params = params.set('id', id);
		}

		if (conditions != null) {
			params = params.set('conditions', JSON.stringify(conditions));
		}

		if (sort != null) {
			params = params.set('sort', sort);
		}


		return this.http
			.get<SiteEntity[]>(environment.apiUrl + '/project/roots', { params: params })
			.toPromise()
	}

	edit(id: string): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<{ item: SiteEntity, attributes: AttributeType[] }>(environment.apiUrl + '/project/edit', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	setExclude(id: string, exclude: boolean): Promise<SiteEntity> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<SiteEntity>(environment.apiUrl + '/project/set-exclude', JSON.stringify({ id: id, exclude: exclude }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	runOrtho(id: string, processPtcloud: boolean, processDem: boolean, processOrtho: boolean, configuration: any): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		//   this.eventService.start();

		const params = {
			id: id,
			processPtcloud: processPtcloud,
			processDem: processDem,
			processOrtho: processOrtho,
			configuration: JSON.stringify(configuration)
		};

		return this.http
			.post<{ item: SiteEntity, attributes: AttributeType[] }>(environment.apiUrl + '/project/run-ortho', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				//				this.eventService.complete();
			}))
			.toPromise()
	}

	update(entity: SiteEntity): Promise<SiteEntity> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.noErrorHttpClient
			.post<SiteEntity>(environment.apiUrl + '/project/update', JSON.stringify({ entity: entity }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	newChild(parentId: string, type: string): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

		let url = '/project/new-default-child';

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		let params = {} as any;

		if (parentId != null) {
			params.parentId = parentId;
		}

		if (type) {
			params.type = type;

			url = '/project/new-child';
		}


		this.eventService.start();


		return this.http
			.post<{ item: SiteEntity, attributes: AttributeType[] }>(environment.apiUrl + url, JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}



	applyWithParent(entity: SiteEntity, parentId: string): Promise<SiteEntity> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});


		let params = { entity: entity } as any;

		if (parentId != null) {
			params.parentId = parentId;
		}


		this.eventService.start();

		return this.noErrorHttpClient
			.post<SiteEntity>(environment.apiUrl + '/project/apply-with-parent', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	getCurrentUser(): string {
		//        let userName: string = "admin";
		//
		//        if ( this.cookieService.check( "user" ) ) {
		//            let cookieData: string = this.cookieService.get( "user" )
		//            let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
		//            userName = cookieDataJSON.userName;
		//        }
		//        else {
		//            console.log( 'Check fails for the existence of the cookie' )
		//
		//            let cookieData: string = this.cookieService.get( "user" )
		//
		//            if ( cookieData != null ) {
		//                let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
		//                userName = cookieDataJSON.userName;
		//            }
		//            else {
		//                console.log( 'Unable to get cookie' );
		//            }
		//        }

		return this.authService.getUserName();
	}

	remove(id: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(environment.apiUrl + '/project/remove', JSON.stringify({ id: id }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	removeObject(componentId: string, key: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(environment.apiUrl + '/project/removeObject', JSON.stringify({ id: componentId, key: key }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	removeUploadTask(uploadId: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(environment.apiUrl + '/project/remove-upload-task', JSON.stringify({ uploadId: uploadId }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	removeTask(taskId: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(environment.apiUrl + '/project/remove-task', JSON.stringify({ taskId: taskId }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	upload(id: string, folder: string, file: File, fileName: string = null): Promise<Document> {

		this.eventService.start();

		const formData = new FormData()

		if (fileName == null) {
			formData.append('file', file);
		} else {
			formData.append('file', file, fileName);
		}
		formData.append('id', id);
		formData.append('folder', folder);

		return this.http.post<Document>(environment.apiUrl + '/project/upload', formData)
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	tasks(statuses: string[], pageSize: number, pageNumber: number, token: number): Promise<PageResult<TaskGroup>> {

		// status options: PROCESSING, COMPLETE, ERROR, QUEUED
		let params: HttpParams = new HttpParams();
		params = params.set('statuses', JSON.stringify(statuses));
		params = params.set('pageSize', pageSize.toString());
		params = params.set('pageNumber', pageNumber.toString());
		params = params.set('token', token.toString());
		
		this.eventService.start();

		return this.http
			.get<PageResult<TaskGroup>>(environment.apiUrl + '/project/tasks', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}


	getTasks(collectionId: string): Promise<Task[]> {

		// status options: PROCESSING, COMPLETE, ERROR, QUEUED
		let params: HttpParams = new HttpParams();
		params = params.set('collectionId', collectionId);

		return this.http
			.get<Task[]>(environment.apiUrl + '/project/collection-tasks', { params: params })
			.toPromise()
	}

	task(id: string): Promise<{ messages: Message[], task: Task }> {

		let params: HttpParams = new HttpParams();
		params = params.set('id', id);

		return this.http
			.get<{ messages: Message[], task: Task }>(environment.apiUrl + '/project/task', { params: params })
			.toPromise();
	}

	getUploadTask(uploadId: string): Promise<Task> {

		let params: HttpParams = new HttpParams();
		params = params.set('uploadId', uploadId);

		return this.http
			.get<Task>(environment.apiUrl + '/project/get-upload-task', { params: params })
			.toPromise();
	}



	getMessages(pageSize: number, pageNumber: number): Promise<PageResult<Message>> {

		let params: HttpParams = new HttpParams();
		params = params.set('pageSize', pageSize.toString());
		params = params.set('pageNumber', pageNumber.toString());

		return this.http.get<PageResult<Message>>(environment.apiUrl + '/project/get-messages', { params: params })
			.toPromise();
	}
	
	downloadPresigned(url: string, useSpinner: boolean): Observable<Blob> {

		let params: HttpParams = new HttpParams();

		if (useSpinner) {
			this.eventService.start();
		}

		return this.noErrorHttpClient.get<Blob>(url, { params: params, responseType: 'blob' as 'json' })
			.pipe(finalize(() => {
				if (useSpinner) {
					this.eventService.complete();
				}
			}))
	}

	download(id: string, key: string, useSpinner: boolean): Observable<Blob> {

		let params: HttpParams = new HttpParams();
		params = params.set('id', id);
		params = params.set('key', key);

		if (useSpinner) {
			this.eventService.start();
		}

		return this.noErrorHttpClient.get<Blob>(environment.apiUrl + '/project/download', { params: params, responseType: 'blob' as 'json' })
			.pipe(finalize(() => {
				if (useSpinner) {
					this.eventService.complete();
				}
			}))
	}

	downloadProductPreview(productId: string, useSpinner: boolean): Observable<Blob> {

		let params: HttpParams = new HttpParams();
		params = params.set('productId', productId);
		params = params.set('artifactName', "ortho");

		if (useSpinner) {
			this.eventService.start();
		}

		return this.noErrorHttpClient.get<Blob>(environment.apiUrl + '/project/downloadProductPreview', { params: params, responseType: 'blob' as 'json' })
			.pipe(finalize(() => {
				if (useSpinner) {
					this.eventService.complete();
				}
			}))
	}

	downloadFile(url: string, useSpinner: boolean): Promise<Blob> {

		let params: HttpParams = new HttpParams();
		params = params.set('url', url);

		if (useSpinner) {
			this.eventService.start();
		}

		return this.noErrorHttpClient.get<Blob>(environment.apiUrl + '/project/download-file', { params: params, responseType: 'blob' as 'json' })
			.pipe(finalize(() => {
				if (useSpinner) {
					this.eventService.complete();
				}
			})).toPromise();
	}


	downloadAll(id: string, key: string, useSpinner: boolean): Observable<Blob> {

		let params: HttpParams = new HttpParams();
		params = params.set('id', id);
		params = params.set('key', key);

		if (useSpinner) {
			this.eventService.start();
		}

		return this.noErrorHttpClient.get<Blob>(environment.apiUrl + '/project/download-all', { params: params, responseType: 'blob' as 'json' })
			.pipe(finalize(() => {
				if (useSpinner) {
					this.eventService.complete();
				}
			}))
	}

	search(terms: Observable<string>) {
		return terms
			.pipe(debounceTime(400))
			.pipe(distinctUntilChanged())
			.pipe(switchMap(term => this.searchEntries(term)));
	}

	searchEntries(term: string): Observable<string> {

		let params: HttpParams = new HttpParams();
		params = params.set('term', term);

		return this.http
			.get<string>(environment.apiUrl + '/project/search', { params: params })
	}

	searchEntites(term: string): Promise<any> {

		let params: HttpParams = new HttpParams();
		params = params.set('term', term);

		return this.http
			.get(environment.apiUrl + '/project/search', { params: params })
			.toPromise()
	}

	submitCollectionMetadata(collectionId: string, metaObj: Object): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.noErrorHttpClient
			.post<void>(environment.apiUrl + '/project/submit-metadata', JSON.stringify({ collectionId: collectionId, json: metaObj }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	applyMetadata(selection: Selection): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(environment.apiUrl + '/project/apply-metadata', JSON.stringify({ selection: selection }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	createCollection(selections: Selection[]): Promise<{ oid: string }> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<{ oid: string }>(environment.apiUrl + '/project/create-collection', JSON.stringify({ selections: selections }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}


	pushToEros(collectionId: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(environment.apiUrl + '/eros/push', JSON.stringify({ collectionId: collectionId }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	getMetadataOptions(id: string): Promise<MetadataResponse> {

		let params: HttpParams = new HttpParams();

		if (id != null) {
			params = params.set('id', id);
		}

		return this.noErrorHttpClient
			.get<MetadataResponse>(environment.apiUrl + '/project/metadata-options', { params: params })
			.toPromise()
	}

	getUAVMetadata(uavId: string, sensorId: string): Promise<{ uav: any, sensor: any }> {

		let params: HttpParams = new HttpParams();
		params = params.set('uavId', uavId);
		params = params.set('sensorId', sensorId);

		return this.noErrorHttpClient
			.get<{ uav: any, sensor: any }>(environment.apiUrl + '/project/uav-metadata', { params: params })
			.toPromise()
	}


	evaluate(condition: Condition, entity: SiteEntity): boolean {
		if (condition != null && condition.type === 'eq') {
			return (entity[condition.name] === condition.value);
		}
		else if (condition != null && condition.type === 'admin') {
			return this.authService.isAdmin();
		}

		return false;
	}

	bureaus(): Promise<{ value: string, label: string }[]> {
		let params: HttpParams = new HttpParams();

		return this.http
			.get<{ value: string, label: string }[]>(environment.apiUrl + '/uav/bureaus', { params: params })
			.toPromise()
	}

	getTotals(text: string, filters: any[]): Promise<any> {

		let params: HttpParams = new HttpParams();
		params = params.set('text', text);
		params = params.set('filters', JSON.stringify(filters));

		return this.http
			.get(environment.apiUrl + '/project/get-totals', { params: params })
			.toPromise()
	}

	getStacItems(criteria: Criteria, pageSize: number, pageNumber: number): Promise<PageResult<StacItem>> {

		let params: HttpParams = new HttpParams();
		params = params.set('criteria', JSON.stringify(criteria));
		params = params.set('pageSize', pageSize.toString());
		params = params.set('pageNumber', pageNumber.toString());

		return this.http
			.get<PageResult<StacItem>>(environment.apiUrl + '/project/get-stac-items', { params: params })
			.toPromise()
	}
}
