///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { BsModalRef } from 'ngx-bootstrap/modal';
import { ErrorHandler } from '@shared/component';

import { TreeComponent, TreeNode } from '@circlon/angular-tree-component';
import { Organization, OrganizationNode } from '@shared/model/organization';
import { PageResult } from '@shared/model/page';
import { OrganizationService } from '@shared/service/organization.service';
import { Observer, Subject, Subscription } from 'rxjs';
import { ContextMenuComponent, ContextMenuService } from '@perfectmemory/ngx-contextmenu';

const PAGE_SIZE: number = 100;

enum TreeNodeType {
	// eslint-disable-next-line no-unused-vars
	OBJECT = 0, LINK = 1
}

class PaginatedTreeNode<T> {

	name: string;
	code: string;
	type: TreeNodeType
	object?: T;
	hasChildren: boolean;
	children?: PaginatedTreeNode<T>[];
	parent?: PaginatedTreeNode<T>;
	pageNumber?: number;
}

@Component({
	selector: 'organization-hierarchy-modal',
	templateUrl: './organization-hierarchy-modal.component.html',
	styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class OrganizationHierarchyModalComponent implements OnInit, OnDestroy {

	/*
	 * Organization Tree component
	 */
	nodes: PaginatedTreeNode<Organization>[] = [];

	disabled: boolean = true;

	rootCode: string = null;

	/*
	 * Organization Tree component
	 */
	@ViewChild(TreeComponent)
	private tree: TreeComponent;

	select: Subject<Organization> = new Subject<Organization>();

	/*
	 * Template for tree node menu
	 */
	@ViewChild("nodeMenu") public nodeMenuComponent: ContextMenuComponent<TreeNode>;

	options = {
		idField: "code",
		getChildren: (node: TreeNode) => {
			return this.getChildren(node);
		},
		actionMapping: {
			mouse: {
				click: (tree: TreeComponent, node: TreeNode, $event: any) => {
					this.treeNodeOnClick(node, $event);
				},
				contextMenu: (tree: any, node: TreeNode, $event: any) => {
					this.handleOnMenu(node, $event);
				}
			}
		},
		allowDrag: false,
		allowDrop: false,
		animateExpand: true,
		scrollOnActivate: true,
		animateSpeed: 2,
		animateAcceleration: 1.01
	}

	message: string = null;

	constructor(
		private service: OrganizationService,
		private contextMenuService: ContextMenuService<TreeNode>,
		public bsModalRef: BsModalRef
	) { }


	ngOnInit(): void {
	}

	ngOnDestroy(): void {
		this.select.unsubscribe();
	}


	init(disabled: boolean, value: { code: string }, observer: Partial<Observer<Organization>>): Subscription {
		this.disabled = disabled;

		if (value != null) {
			this.service.getAncestorTree(this.rootCode, value.code, PAGE_SIZE).then(ancestor => {
				const node = this.build(null, ancestor);

				this.nodes = [node];

				window.setTimeout(() => {
					const node: TreeNode = this.tree.treeModel.getNodeById(value.code);

					if (node != null) {
						node.setActiveAndVisible();
					}
				}, 100);

				this.getChildren(null).then(nodes => {
					this.nodes = this.nodes.concat(nodes.filter(n => n.code !== node.code));
				});
			});
		}
		else if (this.rootCode != null) {
			this.service.get(this.rootCode).then(organization => {
				this.nodes = [{
					code: organization.code,
					name: organization.label.localizedValue,
					type: TreeNodeType.OBJECT,
					object: organization,
					hasChildren: true
				}];
			});
		} else {
			this.getChildren(null).then(nodes => {
				this.nodes = nodes;
			});
		}

		return this.select.subscribe(observer);
	}


	getChildren(treeNode: TreeNode): Promise<PaginatedTreeNode<Organization>[]> {
		const node: PaginatedTreeNode<Organization> = treeNode != null ? treeNode.data : null;

		const code = node != null ? node.object.code : null;

		return this.service.getChildren(code, 1, PAGE_SIZE).then(page => {
			const nodes = this.createNodes(node, page);

			if (node != null) {
				if (node.children == null) {
					node.children = [];
				}

				node.children.concat(nodes);
			}

			return nodes;
		}).catch(ex => {
			return [];
		});
	}

	build(parent: PaginatedTreeNode<Organization>, cNode: OrganizationNode): PaginatedTreeNode<Organization> {
		const node: PaginatedTreeNode<Organization> = {
			code: cNode.object.code,
			name: cNode.object.label.localizedValue,
			type: TreeNodeType.OBJECT,
			object: cNode.object,
			hasChildren: true
		};

		if (cNode.children != null) {
			const nodes: PaginatedTreeNode<Organization>[] = cNode.children.resultSet.map(child => this.build(parent, child));

			const page = cNode.children;

			// Add page node if needed
			if (page.count > page.pageNumber * page.pageSize) {
				nodes.push({
					code: "...",
					name: "...",
					type: TreeNodeType.LINK,
					hasChildren: false,
					pageNumber: page.pageNumber + 1,
					parent: parent
				} as PaginatedTreeNode<Organization>);
			}

			node.children = nodes;
		}

		return node;
	}

	createNodes(parent: PaginatedTreeNode<Organization>, page: PageResult<Organization>): PaginatedTreeNode<Organization>[] {
		const nodes = page.resultSet.map(child => {
			return {
				code: child.code,
				name: child.label.localizedValue,
				object: child,
				hasChildren: true,
				type: TreeNodeType.OBJECT,
			} as PaginatedTreeNode<Organization>;
		});

		// Add page node if needed
		if (page.count > page.pageNumber * page.pageSize) {
			nodes.push({
				code: "...",
				name: "...",
				type: TreeNodeType.LINK,
				hasChildren: false,
				pageNumber: page.pageNumber + 1,
				parent: parent
			} as PaginatedTreeNode<Organization>);
		}

		return nodes;
	}

	treeNodeOnClick(treeNode: TreeNode, $event: any): void {
		const node: PaginatedTreeNode<Organization> = treeNode != null ? treeNode.data : null;

		if (node != null && node.type === TreeNodeType.LINK) {
			if (treeNode.parent != null) {
				const parentNode: PaginatedTreeNode<Organization> = treeNode.parent.data;
				const code = parentNode.object.code;
				const pageNumber = node.pageNumber;

				this.service.getChildren(code, pageNumber, PAGE_SIZE).then(page => {
					const nodes = this.createNodes(parentNode, page);

					parentNode.children = parentNode.children.filter(node => node.code !== "...");
					parentNode.children = parentNode.children.concat(nodes);

					this.tree.treeModel.update();
				}).catch(ex => {
				});
			}
		} else {
			if (treeNode.isExpanded) {
				treeNode.collapse();
			} else {
				treeNode.expand();
			}

			treeNode.setActiveAndVisible();
		}
	}

	handleOnMenu(node: TreeNode, $event: any): void {
		if (!this.disabled) {
			this.contextMenuService.show(this.nodeMenuComponent, {
				value: node,
				x: $event.x,
				y: $event.y
			});
			$event.preventDefault();
			$event.stopPropagation();
		}
	}


	onSelect(treeNode: TreeNode): void {
		console.log(treeNode);

		const node: PaginatedTreeNode<Organization> = treeNode != null ? treeNode.data : null;

		if (node.type === TreeNodeType.OBJECT) {
			this.select.next(node.object);

			this.bsModalRef.hide();
		}
	}


	onClose(): void {
		this.bsModalRef.hide();
	}

	public error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}