import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { LazyLoadEvent } from 'primeng/api';

import { PageResult } from '@shared/model/page';

import { GenericTableConfig, TableEvent } from '@site/model/generic-table';
import { Subject } from 'rxjs';
@Component({
    selector: 'generic-table',
    templateUrl: './generic-table.component.html',
    styles: ['./generic-table.css']
})
export class GenericTableComponent implements OnInit, OnDestroy {
    page: PageResult<Object> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };

    @Input() cols: any = [];

    @Input() baseUrl: string = null;

    @Input() config: GenericTableConfig;

    @Input() refresh: Subject<void>;

    @Output() click = new EventEmitter<TableEvent>();

    loading: boolean = true;

    booleanOptions: any = [];

    event: LazyLoadEvent = null;

    constructor() {
        this.booleanOptions = [{ label: '', value: null }, { value: true, label: 'True' }, { value: false, label: 'False' }];
    }

    ngOnInit(): void {
        // this.onPageChange(1);
        this.refresh.subscribe(() => {
            if (this.event != null) {
                this.onPageChange(this.event);
            }
        });
    }

    ngOnDestroy(): void {
        this.refresh.unsubscribe(); 0
    }

    onPageChange(event: LazyLoadEvent): void {
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

    onClick(type: string, row: Object): void {
        this.click.emit({
            type:type,
            row:row
        });
    }
}
