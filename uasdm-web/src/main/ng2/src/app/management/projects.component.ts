import { Component, OnInit, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { DOCUMENT } from "@angular/platform-browser";

@Component({
  selector: 'projects',
  templateUrl: './projects.component.html',
  styleUrls: []
})
export class ProjectsComponent implements OnInit {
  
  constructor(private router: Router) {    
  }

  ngOnInit(): void {
  }  
}
