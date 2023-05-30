///
///
///

import { Component, Input, ChangeDetectionStrategy, SimpleChanges } from '@angular/core';

import { Step, StepConfig } from './modal-step-indicator';

import { 
    fadeInOnEnterAnimation,
    fadeOutOnLeaveAnimation
 } from 'angular-animations';

// HOW TO USE:
// Set the component in a template:
//
//   <modal-step-indicator [stepConfig]="stepConfig"></modal-step-indicator>
//
// Pass a stepConfig object to the compenent as an input param. Example object:
//
// this.modalStepConfig = {"steps": [
//      {"label":"Category", "active":true, "enabled":false},
//      {"label":"Final", "active":true, "enabled":true}
// ]};

@Component( { 
    selector: 'modal-step-indicator',
    templateUrl: './modal-step-indicator.component.html',
    styleUrls: ['./modal-step-indicator.css'],
        animations: [
        fadeInOnEnterAnimation(),
        fadeOutOnLeaveAnimation() 
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
} )
export class ModalStepIndicatorComponent {

    @Input() stepConfig: StepConfig

    constructor( ) { 

    }

    ngOnInit(): void {

    }

    ngOnChanges(changes: SimpleChanges) {

    }

    ngOnDestroy() {

    }

    setActiveStep(step: Step):void {

    }

    setStepConfig(config: StepConfig):void {
        this.stepConfig = config;
    }
}
