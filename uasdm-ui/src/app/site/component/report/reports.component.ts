///
///
///

import { Component, OnInit } from '@angular/core';
import { ReportService } from '@site/service/report.service';
import { ProductService } from '@site/service/product.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { ProductModalComponent } from '../modal/product-modal.component';
import { GenericTableColumn, GenericTableConfig, TableEvent } from '@shared/model/generic-table';
import { environment } from 'src/environments/environment';
import { AuthService } from '@shared/service/auth.service';
import { UasdmHeaderComponent } from '@shared/component/header/header.component';
import { NgIf } from '@angular/common';
import { ProcessingReportComponent } from '../processing-report/processing-report.component';
import { GenericTableComponent } from '@shared/component/generic-table/generic-table.component';



@Component({
    standalone: true,
    selector: 'reports',
    templateUrl: './reports.component.html',
    styleUrls: ['./reports.css'],
    imports: [UasdmHeaderComponent, NgIf, ProcessingReportComponent, GenericTableComponent]
})
export class ReportsComponent implements OnInit {
    message: string = null;
    admin: boolean = false;

    config: GenericTableConfig;
    cols: GenericTableColumn[] = [
        {
            header: 'Collection', field: 'collectionName', baseUrl: 'site/viewer/entity', urlField: 'collection', type: 'URL', sortable: true, columnType: (row: Object) => {
                if (!row['exists']) {
                    return 'TEXT';
                }

                return 'URL';
            }
        },
        { header: 'Collection Owner', field: 'userName', type: 'TEXT', sortable: true },
        { header: 'Collection Date', field: 'collectionDate', type: 'DATE', sortable: true },
        { header: 'Mission', field: 'missionName', type: 'TEXT', sortable: true },
        { header: 'Project', field: 'projectName', type: 'TEXT', sortable: true },
        { header: 'Site', field: 'siteName', type: 'TEXT', sortable: true },
        { header: 'Latitude', field: 'siteLatDecimalDegree', type: 'NUMBER', sortable: false, filter: false },
        { header: 'Longitude', field: 'siteLongDecimalDegree', type: 'NUMBER', sortable: false, filter: false },
        { header: 'Bureau', field: 'bureauName', type: 'TEXT', sortable: true },
        { header: 'Platform', field: 'platformName', type: 'TEXT', sortable: true },
        { header: 'Sensor', field: 'sensorName', type: 'TEXT', sortable: true },
        { header: 'FAA Id Number', field: 'faaIdNumber', type: 'TEXT', sortable: true },
        { header: 'Serial Number', field: 'serialNumber', type: 'TEXT', sortable: true },
        { header: 'RAW Images Count', field: 'rawImagesCount', type: 'NUMBER', sortable: false },
        { header: 'EROS Metadata Complete', field: 'erosMetadataComplete', type: 'BOOLEAN', sortable: false },
        { header: 'Video', field: 'video', type: 'BOOLEAN', sortable: false },
        { header: 'Storage size', field: 'allStorageSize', type: 'NUMBER', sortable: true, filter: false },
        { header: 'Number of Downloads', field: 'downloadCounts', type: 'NUMBER', sortable: true, filter: false },
        { header: 'Number of Products', field: 'numberOfProducts', type: 'NUMBER', sortable: true, filter: false },
        { header: 'Date of Create', field: 'createDate', type: 'DATE', sortable: true, filter: false },
        { header: 'Date of Delete', field: 'deleteDate', type: 'DATE', sortable: true, filter: false }
    ];

    constructor(private service: ReportService, private pService: ProductService, private modalService: BsModalService, private authService: AuthService) {
    }

    ngOnInit(): void {
        this.config = {
            service: this.service,
            remove: false,
            view: false,
            create: false,
            label: 'row',
            sort: { field: 'collectionName', order: 1 },
        }
        this.admin = this.authService.isAdmin();
    }

    onExportCSV(): void {
        window.open(environment.apiUrl + '/api/collection-report/export-csv', '_blank');
    }

    onClick(event: TableEvent): void {
        if (event.type === 'custom') {
            if (event.col.field === 'product') {

                const oid = event.row['product'];

                if (oid != null && oid.length > 0) {
                    this.pService.getDetail(oid, 1, 20).then(detail => {
                        const bsModalRef = this.modalService.show(ProductModalComponent, {
                            animated: true,
                            backdrop: true,
                            ignoreBackdropClick: true,
                            'class': 'product-info-modal modal-xl'
                        });
                        bsModalRef.content.init(detail);
                    });
                }
            }

        }
    }
}
