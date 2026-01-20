///
///
///

import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { LazyLoadEvent, PrimeTemplate } from 'primeng/api';

import { PageResult } from '@shared/model/page';

import { Subject } from 'rxjs';
import { GenericTableColumn, GenericTableConfig, TableEvent } from '@shared/model/generic-table';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { NgFor, NgClass, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';
import { DropdownModule } from 'primeng/dropdown';
import { RouterLink } from '@angular/router';
@Component({
    standalone: true,
    selector: 'generic-table',
    templateUrl: './generic-table.component.html',
    styleUrls: ['./generic-table.css'],
    imports: [TableModule, PrimeTemplate, NgFor, NgClass, NgIf, NgSwitch, NgSwitchCase, DropdownModule, RouterLink, NgSwitchDefault]
})
export class GenericTableComponent implements OnInit, OnDestroy {
    page: PageResult<Object> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };

    @Input() cols: GenericTableColumn[] = [];

    @Input() baseUrl: string = null;

    @Input() config: GenericTableConfig;

    @Input() refresh: Subject<void>;

    @Output() click = new EventEmitter<TableEvent>();

    loading: boolean = true;

    booleanOptions: any = [];

    event: TableLazyLoadEvent = null;

    constructor() {
        this.booleanOptions = [{ label: '', value: null }, { value: true, label: 'True' }, { value: false, label: 'False' }];
    }

    ngOnInit(): void {

        if (this.refresh != null) {
            this.refresh.subscribe(() => {
                if (this.event != null) {
                    this.onPageChange(this.event);
                }
            });
        }
    }

    ngOnDestroy(): void {

        if (this.refresh != null) {
            this.refresh.unsubscribe(); 0
        }
    }

    onPageChange(event: TableLazyLoadEvent): void {
        this.loading = true;
        this.event = event;

        setTimeout(() => {
            this.config.service.page(event, this.baseUrl).then(page => {
                this.page = page;
            }).finally(() => {
                this.loading = false;
            });
        }, 1000);
    }

    onClick(type: string, row: Object, col: GenericTableColumn): void {
        this.click.emit({
            type: type,
            row: row,
            col: col
        });
    }

    getColumnType(row: Object, col: GenericTableColumn): string {
        if (col.columnType != null) {
            return col.columnType(row);
        }

        return col.type;
    }

    handleInput(dt: any, event: any, col: GenericTableColumn, operation: string): void {
        dt.filter(event.value, col.field, operation)
    }

}
