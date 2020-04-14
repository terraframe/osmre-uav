import { Component, Input, ViewChild, ElementRef } from '@angular/core';

@Component({
	selector: 'video-player',
	templateUrl: './video-player.component.html',
	styles: []
})
export class VideoPlayerComponent {

	@Input() src: string = null;

	videoPlayer: HTMLVideoElement;

	@ViewChild('videoPlayer')
	set mainVideoEl(el: ElementRef) {
		this.videoPlayer = el.nativeElement;
	}

	constructor() { }
}