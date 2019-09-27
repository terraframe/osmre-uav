import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

import { Sensor, WAVELENGTHS } from '../../model/sensor';
import { SensorService } from '../../service/sensor.service';


@Component( {
    selector: 'sensor',
    templateUrl: './sensor.component.html',
    styleUrls: []
} )
export class SensorComponent implements OnInit {
    sensor: Sensor;
    newInstance: boolean = false;

    message: string = null;

    wavelengths: string[] = WAVELENGTHS;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onSensorChange: Subject<Sensor>;

    constructor( private service: SensorService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onSensorChange = new Subject();
    }

    handleOnSubmit(): void {
        this.message = null;

        this.service.apply( this.sensor ).then( data => {
            this.onSensorChange.next( data );
            this.bsModalRef.hide();
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    handleOnCancel(): void {
        this.message = null;

        if ( this.newInstance ) {
            this.bsModalRef.hide();
        }
        else {
            this.service.unlock( this.sensor.oid ).then( data => {
                this.bsModalRef.hide();
            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );
        }
    }

    updateSelectedWaveLength( event ) {

        const indexOf = this.sensor.waveLength.indexOf( event.target.name )

        if ( event.target.checked ) {

            if ( indexOf < 0 ) {
                this.sensor.waveLength.push( event.target.name );

            }
        } else {
            if ( indexOf > -1 ) {
                this.sensor.waveLength.splice( indexOf, 1 );
            }
        }
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );

            console.log( this.message );
        }
    }

}
