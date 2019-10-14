import {
    trigger,
    state,
    style,
    animate,
    transition,
    group, 
    query, 
    stagger,
    keyframes
} from '@angular/animations';



export const slideInOut = trigger('slideInOut', [
	transition(':enter', [
		style({ transform: 'translateX(-100%)', opacity: '1' }),
		animate(250)
	]),
	transition(':leave', [
		group([
			animate('250ms ease', style({
				transform: 'translateX(100%)', opacity: '0'
			}))
		])
	])
]);



