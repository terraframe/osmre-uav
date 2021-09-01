import { Component, OnInit } from '@angular/core';
import { LazyLoadEvent } from 'primeng/api';

import { PageResult } from '@shared/model/page';

import { Report } from '@site/model/report';
import { ReportService } from '@site/service/report.service';
@Component({
    selector: 'reports',
    templateUrl: './reports.component.html',
    styles: ['./reports.css']
})
export class ReportsComponent implements OnInit {
    page: PageResult<Report> = {
        resultSet: [],
        count: 0,
        pageNumber: 1,
        pageSize: 10
    };
    message: string = null;

    cols: any = [
        { header: 'Collection', field: 'collectionName', type: 'TEXT', sortable: true },
        { header: 'Collection Owner', field: 'userName', type: 'TEXT', sortable: true },
        { header: 'Mission', field: 'missionName', type: 'TEXT', sortable: true },
        { header: 'Project', field: 'projectName', type: 'TEXT', sortable: true },
        { header: 'Site', field: 'siteName', type: 'TEXT', sortable: true },
        { header: 'Latitude', field: 'siteLatDecimalDegree', type: 'NUMBER', sortable: false },
        { header: 'Longitude', field: 'siteLongDecimalDegree', type: 'NUMBER', sortable: false },
        { header: 'Bureau', field: 'bureauName', type: 'TEXT', sortable: true },
        { header: 'Platform', field: 'platformName', type: 'TEXT', sortable: true },
        { header: 'Sensor', field: 'sensorName', type: 'TEXT', sortable: true },
        { header: 'FAA Id Number', field: 'faaIdNumber', type: 'TEXT', sortable: true },
        { header: 'Serial Number', field: 'serialNumber', type: 'TEXT', sortable: true },
        { header: 'ODM Processing', field: 'odmProcessing', type: 'BOOLEAN', sortable: false },
        { header: 'RAW Images Count', field: 'rawImagesCount', type: 'NUMBER', sortable: false },
        { header: 'EROS Metadata Complete', field: 'erosMetadataComplete', type: 'BOOLEAN', sortable: false },
        { header: 'Video', field: 'video', type: 'BOOLEAN', sortable: false },
        { header: 'Orthomosaic', field: 'orthomosaic', type: 'BOOLEAN', sortable: false },
        { header: 'Point Cloud', field: 'pointCloud', type: 'BOOLEAN', sortable: false },
        { header: 'Hillshade', field: 'hillshade', type: 'BOOLEAN', sortable: false },
        { header: 'Products Shared', field: 'productsShared', type: 'BOOLEAN', sortable: false },
    ];

    loading: boolean = true;

    rows: Report[];

    constructor(private service: ReportService) { }

    ngOnInit(): void {
        // this.onPageChange(1);
    }

    onPageChange(event: LazyLoadEvent): void {
        this.loading = true;

        console.log(event);

        setTimeout(() => {
            this.service.page(event).then(page => {
                this.page = page;
            }).finally(() => {
                this.loading = false;
            });
        }, 1000);
    }
}
