# Imagery Data Manager (IDM)

The Imagery Data Manager (IDM) is an open-source, cloud-based application for storing, managing, processing and sharing drone imagery. It was built for the U.S. Department of the Interior and is available as an application on [GeoPlatform.gov](https://www.geoplatform.gov/apps). Organizations working with U.S. agencies can request access to IDM at no cost.

This repository was formerly called `osmre-uav`, and many modules still use the `uasdm` prefix.

## Why IDM

Without a shared place to manage it, drone imagery and its related data often end up on personal laptops or in isolated systems. The people who need that imagery can't find it, work gets duplicated, and routine processing is skipped for lack of time or resources.

IDM addresses this by giving teams one place to upload, process, organize and share their imagery. When teams are ready to share beyond IDM, they can publish products using open standards, so other organizations can use them in their own tools. Sensors, flights, raw and processed imagery, metadata and processing outputs are all connected and searchable. They are indexed by location and time, so teams can see how a site changes across flights.

## What you can do with IDM

- **Upload imagery from a web browser**, using drag and drop. Large uploads work reliably even with low bandwidth or intermittent connectivity.
- **Process drone imagery in the cloud** into orthomosaics, digital surface and terrain models, and point clouds, with radiometric calibration for multispectral imagery.
- **Create derived products from point clouds as well as imagery.** Lidar point clouds are processed with [SilviMetric](https://silvimetric.org/en/latest/) into raster products such as terrain models, surface models, canopy cover and tree structure.
- **Organize data by site**, following a hierarchy of sites, projects, missions and collections.
- **View results in 2D and 3D**, including orthomosaics on a map and point clouds in a 3D viewer.
- **Search and share** imagery and products across teams and organizations.
- **Publish derived products to [Know-STAC](https://knowstac.geoprism.net/)** ([source code](https://github.com/terraframe/know-stac)), which catalogs them using the [SpatioTemporal Asset Catalog (STAC)](https://stacspec.org/) specification and serves them in cloud-native formats. Because STAC and cloud-native formats are open, widely adopted standards, published products can be found and used with common tools, without needing access to IDM.

## How it works

IDM stores imagery in Amazon S3 and runs processing on AWS. Photogrammetry, including multispectral processing, is handled by [OpenDroneMap](https://www.opendronemap.org/) through NodeODM and ClusterODM. Lidar is processed with PDAL and SilviMetric. Outputs are stored in cloud-native formats, as Cloud-Optimized GeoTIFFs and COPC point clouds. They are served to the map through TiTiler and to the 3D viewer through Potree.

Each product is also described as a STAC Item, with its location, date and assets, and indexed in Elasticsearch. This index powers IDM's location and date search. It's also what allows published products to be shared through Know-STAC.

IDM stores its data as a graph. Sites, projects, missions and collections are linked in a hierarchy, and each collection is connected to its sensor, UAV, metadata, imagery and processed products. IDM can also synchronize a labeled property graph from GeoPrism, which places each site within a geographic hierarchy based on its location.

The main technologies are:

- **Frontend:** Angular, MapLibre GL, Potree, and Uppy with tus for resumable uploads
- **Backend:** Java 17 on the GeoPrism and Runway SDK frameworks
- **Data and search:** PostgreSQL with PostGIS, OrientDB and Elasticsearch
- **Map services:** TiTiler
- **Processing:** OpenDroneMap, NodeODM, ClusterODM, PDAL, SilviMetric and GDAL
- **Formats and catalogs:** STAC, Cloud-Optimized GeoTIFF and COPC
- **Authentication:** Keycloak (OAuth)
- **Cloud:** Amazon Web Services, including S3, Lambda and Fargate

## Repository structure

| Folder | Contents |
| --- | --- |
| `uasdm-ui` | Angular web application |
| `uasdm-web` | Web application packaging and configuration |
| `uasdm-server` | Java server: data model, business logic, processing workflows and integrations |
| `uasdm-client` | Generated client classes for the server data model |
| `uasdm-test` | Automated tests |
| `uasdm-odm` | Wrapper for TerraFrame's forks of [ODM](https://github.com/terraframe/ODM) and [NodeODM](https://github.com/terraframe/NodeODM) |
| `uasdm-clusterodm` | Build and deployment for [ClusterODM](https://github.com/terraframe/ClusterODM), which scales ODM processing |
| `uasdm-clusterlidar` | Scheduler and workers for lidar processing with PDAL and SilviMetric |
| `uasdm-micasense` | Legacy Docker wrapper for MicaSense multispectral processing, no longer used |
| `uasdm-lambda-thumbnail` | AWS Lambda functions that generate image thumbnails |
| `uasdm-lambda-titiler` | Deployment for TiTiler, which serves imagery tiles |
| `uasdm-fargate-erossync` | AWS Fargate task that copies data from S3 to an EROS FTP server |
| `envcfg` | Environment configuration, with an example properties file |
| `geoplatform` | GeoPlatform.gov integration schema |
| `src` | Build, Docker, Solr and development scripts |
| `launches` | Eclipse launch configurations |

## Related projects

- [Know-STAC](https://knowstac.geoprism.net/), a live STAC catalog for publishing and sharing IDM products, and its [source code](https://github.com/terraframe/know-stac)
- [SilviMetric](https://silvimetric.org/en/latest/), used to create derived products from lidar point clouds
- [STAC specification](https://stacspec.org/)

## Releases

See the [changelog](CHANGELOG.md) for release notes, and [Releases](https://github.com/terraframe/osmre-uav/releases) for tagged versions.

## Contributing

Bug reports and feature requests are welcome as [issues](https://github.com/terraframe/osmre-uav/issues).

## About

IDM is developed by [TerraFrame](https://terraframe.com) for the U.S. Department of the Interior.

## License

IDM is released under the [Apache License, Version 2.0](COPYING).
