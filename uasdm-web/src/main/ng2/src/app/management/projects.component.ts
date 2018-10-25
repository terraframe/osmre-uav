import { Component, OnInit, Inject } from '@angular/core';
import { TreeNode } from 'angular-tree-component';

import { ManagementService } from './management.service';

@Component( {
    selector: 'projects',
    templateUrl: './projects.component.html',
    styleUrls: []
} )
export class ProjectsComponent implements OnInit {
    nodes = [] as TreeNode[];

    options = {
        getChildren: ( node: TreeNode ) => {
          return this.service.getChildren(node);
        }
    };

    constructor(private service:ManagementService) { }

    ngOnInit(): void {
        this.service.roots().then(nodes => {
            this.nodes = nodes;
        });
    }
}
