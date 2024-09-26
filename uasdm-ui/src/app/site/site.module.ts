///
///
///

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
//import { TreeModule } from 'angular-tree-component';
//import { ContextMenuModule } from 'ngx-contextmenu';
import { NgxFileDropModule } from 'ngx-file-drop';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { AccordionModule } from 'ngx-bootstrap/accordion';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { AlertModule } from 'ngx-bootstrap/alert';
import { NgxPaginationModule } from 'ngx-pagination';
import { CollapseModule } from 'ngx-bootstrap/collapse';
import { PopoverModule } from 'ngx-bootstrap/popover';

import { MapAttributeComponent } from './component/map-attribute/map-attribute.component';
import { ProductPanelComponent } from './component/product-panel/product-panel.component';
import { EntityModalComponent } from './component/modal/entity-modal.component';
import { MetadataModalComponent } from './component/modal/metadata-modal.component';
import { ImagePreviewModalComponent } from './component/modal/image-preview-modal.component';
import { UploadModalComponent } from './component/modal/upload-modal.component';
import { ODMRunModalComponent } from './component/modal/odmrun-modal.component';
import { CollectionModalComponent } from './component/modal/collection-modal.component';
import { AccessibleSupportModalComponent } from './component/modal/accessible-support-modal.component';
import { ProductModalComponent } from './component/modal/product-modal.component';
import { ProjectsComponent } from './component/projects.component';
import { UploadComponent } from './component/upload.component';
import { TasksComponent } from './component/tasks.component';
import { TasksPanelComponent } from './component/tasks/tasks-panel.component';
import { SensorsComponent } from './component/sensor/sensors.component';
import { SensorComponent } from './component/sensor/sensor.component';
import { PlatformsComponent } from './component/platform/platforms.component';
import { PlatformComponent } from './component/platform/platform.component';
import { ClassificationsComponent } from './component/classification/classifications.component';
import { ClassificationComponent } from './component/classification/classification.component';
import { EquipmentComponent } from './component/equipment/equipment.component';
import { ProcessingReportComponent } from './component/processing-report/processing-report.component';

import { ForbiddenNameDirective } from './directive/forbidden-name.directive';
import { OnlyNumber } from './directive/number-only.directive';

import { ProductService } from './service/product.service';
import { ManagementService } from './service/management.service';
import { MapService } from './service/map.service';
import { CanDeactivateGuardService } from './service/can.deactivate.guard.service';
import { SensorService } from './service/sensor.service';
import { PlatformService } from './service/platform.service';
import { MetadataService } from './service/metadata.service';
import { ClassificationService } from './service/classification.service';

import { SiteRoutingModule } from './site-routing.module';
import { SharedModule } from '../shared/shared.module';
import { UAVsComponent } from './component/uav/uavs.component';
import { UAVsPageComponent } from './component/uav/uavs-page.component';
import { UAVComponent } from './component/uav/uav.component';
import { UAVService } from './service/uav.service';
import { MetadataPageComponent } from './component/metadata-page/metadata-page.component';
import { ReportsComponent } from './component/report/reports.component';
import { ReportService } from './service/report.service';
import { CreateCollectionModalComponent } from './component/modal/create-collection-modal.component';
import { CreateStandaloneProductModalComponent } from './component/modal/create-standalone-product-group-modal.component';
import { ArtifactPageComponent } from './component/modal/artifact-page.component';
import { LayerPanelComponent } from './component/layer-panel/layer-panel.component';
import { ArtifactUploadComponent } from './component/artifact-upload/artifact-upload.component';
import { FilterModalComponent } from './component/modal/filter-modal.component';
import { CreateProductGroupModalComponent } from './component/modal/create-product-group-modal.component';
import { ShareProductModalComponent } from './component/modal/share-product-modal.component';
import { UserAccessService } from './service/user-access.service';
import { UserAccessModalComponent } from './component/modal/user-access-modal.component';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RunProcessModalComponent } from './component/modal/run-process-modal.component';

@NgModule({
	imports: [
		CommonModule,
		RouterModule,
		FormsModule,
		NgxPaginationModule,
		//        TreeModule,
		//        ContextMenuModule,
		//        ModalModule.forRoot(),
		AlertModule,
		BsDropdownModule,
		TypeaheadModule,
		AccordionModule,
		TabsModule.forRoot(),
		CollapseModule.forRoot(),
		SharedModule,
		SiteRoutingModule,
		PopoverModule.forRoot(),
		NgxFileDropModule
	],
	declarations: [
		MapAttributeComponent,
		ProductPanelComponent,
		EntityModalComponent,
		MetadataModalComponent,
		ImagePreviewModalComponent,
		CreateCollectionModalComponent,
		CreateStandaloneProductModalComponent,
		UploadModalComponent,
		ODMRunModalComponent,
		ArtifactPageComponent,
		RunProcessModalComponent,
		CollectionModalComponent,
		AccessibleSupportModalComponent,
		ProductModalComponent,
		FilterModalComponent,
		ProjectsComponent,
		UploadComponent,
		ProcessingReportComponent,
		TasksComponent,
		ForbiddenNameDirective,
		OnlyNumber,
		SensorComponent,
		SensorsComponent,
		PlatformComponent,
		PlatformsComponent,
		TasksPanelComponent,
		ClassificationsComponent,
		ClassificationComponent,
		UAVsComponent,
        UAVsPageComponent,
		UAVComponent,
		MetadataPageComponent,
		ReportsComponent,
        EquipmentComponent,
		LayerPanelComponent,
		ArtifactUploadComponent,
		CreateProductGroupModalComponent,
		ShareProductModalComponent,
		UserAccessModalComponent
	],
	providers: [
		CanDeactivateGuardService,
		ManagementService,
		ProductService,
		MapService,
		SensorService,
		PlatformService,
		MetadataService,
		ClassificationService,
		UAVService,
		ReportService,
		UserAccessService
	],
	entryComponents: [
		FilterModalComponent,
		RunProcessModalComponent,
		CreateCollectionModalComponent,
		CreateStandaloneProductModalComponent,
		UploadModalComponent,
		ODMRunModalComponent,
		EntityModalComponent,
		ImagePreviewModalComponent,
		MetadataModalComponent,
		CollectionModalComponent,
		ProductModalComponent,
		CreateProductGroupModalComponent,
		ShareProductModalComponent,
		UserAccessModalComponent
	]
})
export class SiteModule { }
