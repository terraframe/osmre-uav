import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { LngLatBounds } from 'mapbox-gl';
import { Observable } from 'rxjs';

// import 'rxjs/add/operator/toPromise';
import { finalize, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import { AuthService } from '@shared/service/auth.service';
import { EventService } from '@shared/service/event.service';
import { HttpBackendClient } from '@shared/service/http-backend-client.service';

import { SiteEntity, Message, Task, AttributeType, Condition, SiteObjectsResultSet, TaskGroup, Selection, CollectionArtifacts } from '../model/management';
import { Sensor } from '../model/sensor';
import { Platform } from '../model/platform';
import { PageResult } from '@shared/model/page';

declare var acp: any;

@Injectable()
export class ManagementService {

	constructor(private http: HttpClient, private noErrorHttpClient: HttpBackendClient, private eventService: EventService, private authService: AuthService) { }

	getChildren(id: string): Promise<SiteEntity[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);


		return this.http
			.get<SiteEntity[]>(acp + '/project/get-children', { params: params })
			.toPromise()
	}

	getObjects(id: string, key: string, pageNumber: number, pageSize: number): Promise<SiteObjectsResultSet> {
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

		return this.http
			.get<SiteObjectsResultSet>(acp + '/project/objects', { params: params })
			.toPromise()
	}

	view(id: string): Promise<{ breadcrumbs: SiteEntity[], item: SiteEntity }> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);

		return this.http
			.get<{ breadcrumbs: SiteEntity[], item: SiteEntity }>(acp + '/project/view', { params: params })
			.toPromise()
	}

	getItems(id: string, key: string): Promise<SiteEntity[]> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);

		if (key != null) {
			params = params.set('key', key);
		}

		return this.http
			.get<SiteEntity[]>(acp + '/project/items', { params: params })
			.toPromise()
	}

	getArtifacts(id: string): Promise<CollectionArtifacts> {
		let params: HttpParams = new HttpParams();
		params = params.set('id', id);

		return this.http
			.get<CollectionArtifacts>(acp + '/project/get-artifacts', { params: params })
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
			.post<CollectionArtifacts>(acp + '/project/remove-artifacts', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}



	roots(id: string, bounds: LngLatBounds): Promise<SiteEntity[]> {
		let params: HttpParams = new HttpParams();

		if (id != null) {
			params = params.set('id', id);
		}

		if (bounds != null) {
			params = params.set('bounds', JSON.stringify(bounds));
		}

		return this.http
			.get<SiteEntity[]>(acp + '/project/roots', { params: params })
			.toPromise()
	}

	edit(id: string): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<{ item: SiteEntity, attributes: AttributeType[] }>(acp + '/project/edit', JSON.stringify({ id: id }), { headers: headers })
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
			.post<SiteEntity>(acp + '/project/set-exclude', JSON.stringify({ id: id, exclude: exclude }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	runOrtho(id: string, processPtcloud: boolean, processDem: boolean, processOrtho: boolean): Promise<{ item: SiteEntity, attributes: AttributeType[] }> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		//   this.eventService.start();

		const params = {
			id: id,
			processPtcloud: processPtcloud,
			processDem: processDem,
			processOrtho: processOrtho
		};

		return this.http
			.post<{ item: SiteEntity, attributes: AttributeType[] }>(acp + '/project/run-ortho', JSON.stringify(params), { headers: headers })
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
			.post<SiteEntity>(acp + '/project/update', JSON.stringify({ entity: entity }), { headers: headers })
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
			.post<{ item: SiteEntity, attributes: AttributeType[] }>(acp + url, JSON.stringify(params), { headers: headers })
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
			.post<SiteEntity>(acp + '/project/apply-with-parent', JSON.stringify(params), { headers: headers })
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
			.post<void>(acp + '/project/remove', JSON.stringify({ id: id }), { headers: headers })
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
			.post<void>(acp + '/project/removeObject', JSON.stringify({ id: componentId, key: key }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	removeTask(uploadId: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<void>(acp + '/project/remove-task', JSON.stringify({ uploadId: uploadId }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	upload(id: string, folder: string, file: File): Promise<Document> {

		this.eventService.start();

		const formData = new FormData()
		formData.append('file', file);
		formData.append('id', id);
		formData.append('folder', folder);

		return this.http.post<Document>(acp + '/project/upload', formData)
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

		return this.http
			.get<PageResult<TaskGroup>>(acp + '/project/tasks', { params: params })
			.toPromise()
	}


	getTasks(collectionId: string): Promise<Task[]> {

		// status options: PROCESSING, COMPLETE, ERROR, QUEUED
		let params: HttpParams = new HttpParams();
		params = params.set('collectionId', collectionId);

		return this.http
			.get<Task[]>(acp + '/project/collection-tasks', { params: params })
			.toPromise()
	}

	task(id: string): Promise<{ messages: Message[], task: Task }> {

		let params: HttpParams = new HttpParams();
		params = params.set('id', id);

		return this.http
			.get<{ messages: Message[], task: Task }>(acp + '/project/task', { params: params })
			.toPromise();
	}

	getUploadTask(uploadId: string): Promise<Task> {

		let params: HttpParams = new HttpParams();
		params = params.set('uploadId', uploadId);

		return this.http
			.get<Task>(acp + '/project/get-upload-task', { params: params })
			.toPromise();
	}



	getMessages(pageSize: number, pageNumber: number): Promise<PageResult<Message>> {

		let params: HttpParams = new HttpParams();
		params = params.set('pageSize', pageSize.toString());
		params = params.set('pageNumber', pageNumber.toString());

		return this.http.get<PageResult<Message>>(acp + '/project/get-messages', { params: params })
			.toPromise();
	}

	download(id: string, key: string, useSpinner: boolean): Observable<Blob> {

		let params: HttpParams = new HttpParams();
		params = params.set('id', id);
		params = params.set('key', key);

		if (useSpinner) {
			this.eventService.start();
		}

		return this.noErrorHttpClient.get<Blob>(acp + '/project/download', { params: params, responseType: 'blob' as 'json' })
			.pipe(finalize(() => {
				if (useSpinner) {
					this.eventService.complete();
				}
			}))
	}

	downloadAll(id: string, key: string, useSpinner: boolean): Observable<Blob> {

		let params: HttpParams = new HttpParams();
		params = params.set('id', id);
		params = params.set('key', key);

		if (useSpinner) {
			this.eventService.start();
		}

		return this.noErrorHttpClient.get<Blob>(acp + '/project/download-all', { params: params, responseType: 'blob' as 'json' })
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
			.get<string>(acp + '/project/search', { params: params })
	}

	searchEntites(term: string): Promise<any> {

		let params: HttpParams = new HttpParams();
		params = params.set('term', term);

		return this.http
			.get(acp + '/project/search', { params: params })
			.toPromise()
	}

	submitCollectionMetadata(collectionId: string, metaObj: Object): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.noErrorHttpClient
			.post<void>(acp + '/project/submit-metadata', JSON.stringify({ collectionId: collectionId, json: metaObj }), { headers: headers })
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
			.post<void>(acp + '/project/apply-metadata', JSON.stringify({ selection: selection }), { headers: headers })
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
			.post<{ oid: string }>(acp + '/project/create-collection', JSON.stringify({ selections: selections }), { headers: headers })
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
			.post<void>(acp + '/eros/push', JSON.stringify({ collectionId: collectionId }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise()
	}

	getMetadataOptions(id: string): Promise<{ name: string, email: string, uav: any, sensor: any }> {

		let params: HttpParams = new HttpParams();

		if (id != null) {
			params = params.set('id', id);
		}

		return this.noErrorHttpClient
			.get<{ name: string, email: string, uav: any, sensor: any }>(acp + '/project/metadata-options', { params: params })
			.toPromise()
	}

	getUAVMetadata(uavId: string, sensorId: string): Promise<{ uav: any, sensor: any }> {

		let params: HttpParams = new HttpParams();
		params = params.set('uavId', uavId);
		params = params.set('sensorId', sensorId);

		return this.noErrorHttpClient
			.get<{ uav: any, sensor: any }>(acp + '/project/uav-metadata', { params: params })
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
}
