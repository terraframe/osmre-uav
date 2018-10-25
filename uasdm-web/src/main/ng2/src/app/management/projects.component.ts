import { Component, OnInit, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { DOCUMENT } from "@angular/platform-browser";

@Component( {
    selector: 'projects',
    templateUrl: './projects.component.html',
    styleUrls: []
} )
export class ProjectsComponent implements OnInit {
    nodes = [
        {
            id: 1,
            name: 'root1',
            children: [
                { id: 2, name: 'child1' },
                { id: 3, name: 'child2' }
            ]
        },
        {
            id: 4,
            name: 'root2',
            children: [
                { id: 5, name: 'child2.1' },
                {
                    id: 6,
                    name: 'child2.2',
                    children: [
                        { id: 7, name: 'subsub' }
                    ]
                }
            ]
        }
    ];

    options = {};
    constructor( private router: Router ) {
    }

    ngOnInit(): void {
    }
}
