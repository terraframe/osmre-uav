====

====

How to use these animations:

1. Add and export an animation to one of the files in this directory.
2. Import the animation to a component like this. 

import { slideInOut } from '../../shared/animation/animation';  <--- HERE

@Component( {
    selector: 'example',
    templateUrl: './example.component.html',
    styles: [],
    animations: [ slideInOut ]  <--- HERE
} )
export class ProjectsComponent implements OnInit, AfterViewInit, OnDestroy {
  ... 
}


3. add the animation to the template like this. 

<div [@slideInOut] ></div>