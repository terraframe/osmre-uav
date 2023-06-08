
# Release Notes


## [0.16.1](https://github.com/terraframe/osmre-uav/releases/tag/0.16.1) (2023-06-08)

### Features

 - **site-viewer** busy spinner on create collection modal  ([#223](https://github.com/terraframe/osmre-uav/issues/223)) ([7855b](https://github.com/terraframe/osmre-uav/commit/7855b1f6f05a929f87de28572b26d6fc3ccdf6e4))
 - **processing** ability to specify radiometric-calibration   ([fa734](https://github.com/terraframe/osmre-uav/commit/fa734f05831b2b5c0e2c4df6c403b0a4b2cc1ef9))

### Bug Fixes

   - **processing** prevent collection clobbering  ([#228](https://github.com/terraframe/osmre-uav/issues/228)) ([2fa5a](https://github.com/terraframe/osmre-uav/commit/2fa5a041f065678d9f0bab26fd0d17e076f81710))
   - **workflow-tasks** deleting workflow tasks  ([#229](https://github.com/terraframe/osmre-uav/issues/229)) ([ef951](https://github.com/terraframe/osmre-uav/commit/ef9512d37b169ac88e44abb0cf3f9e98aa17eb45))
   - **uav-table** filtering on multiple columns throws error   ([218ef](https://github.com/terraframe/osmre-uav/commit/218ef3eed7ec283a07cd5050ad7bf1a0bf90949f))
   - **processing** occasional unspecified error during store   ([f80f5](https://github.com/terraframe/osmre-uav/commit/f80f55763b73f4642ae8b16c713e18b4b78370a7))
   - **collection-modal** not displaying raw .tiff images   ([e2a0c](https://github.com/terraframe/osmre-uav/commit/e2a0c3915405ad28aebe8e0d01ddd33e362a89ca))



## [0.16.0](https://github.com/terraframe/osmre-uav/releases/tag/0.16.0) (2023-05-30)

### Features

 - ![BREAKING CHANGE](https://raw.githubusercontent.com/terraframe/geoprism-registry/master/src/build/changelog/breaking-change.png) Upgrade Orientdb to v3.2
 - ![BREAKING CHANGE](https://raw.githubusercontent.com/terraframe/geoprism-registry/master/src/build/changelog/breaking-change.png) Upgrade ODM to v3.1.4
 - **site-viewer** improve ortho preview  ([#222](https://github.com/terraframe/osmre-uav/issues/222)) ([4b369](https://github.com/terraframe/osmre-uav/commit/4b369efa389ca71df82a16c44db1ac6fd619fa55))
 - add support for feature-quality odm config option   ([3bd65](https://github.com/terraframe/osmre-uav/commit/3bd654f51bbf3433ba079ab68e0130e7ea5da4b6))
 - update collection metadata when calculating product bbox   ([de1df](https://github.com/terraframe/osmre-uav/commit/de1df2c534e767199bf498fbf8d3176d3efd239d))
 - ability to view ODM run information on collections   ([12281](https://github.com/terraframe/osmre-uav/commit/122818cd77b11407b22bc1d9be3db790b1752a97))
 - replace special characters in uploaded files with underscores   ([3cb1c](https://github.com/terraframe/osmre-uav/commit/3cb1cba310378bcfc36451027ccf66014fda9b0f))
 - ability to download individual product artifacts   ([718e2](https://github.com/terraframe/osmre-uav/commit/718e2257cf94a5b5a26ad8b082acac4af07a905a))
 - ability to include or exclude the geo location file for ODM processing  ([a48d4](https://github.com/terraframe/osmre-uav/commit/a48d41c76089b7b43f8835c72867d0d4299c2fdb))
 - add exponential retry backoff to ODM communication   ([fe623](https://github.com/terraframe/osmre-uav/commit/fe6230c80c5fe978f2ed0bc121ce1b4a2446c1b1))
 - support for boosting STAC results by the bounds of the view port   ([2519e](https://github.com/terraframe/osmre-uav/commit/2519e9f84f446913a40fd5696d0c9f8bf44c2cbe))

### Bug Fixes

   - **upload** upload chunk merging is a post-processing step  ([#220](https://github.com/terraframe/osmre-uav/issues/220)) ([9582d](https://github.com/terraframe/osmre-uav/commit/9582de9a6cdeb9f7cd2990d5c714c0ba8be32856))
   - StaleEntityException when running ortho re-run   ([3d2f6](https://github.com/terraframe/osmre-uav/commit/3d2f63897d8f1031d0c3cef38b572d8d2b3a6f85))
   - **login** sessions expiring could sometimes leave people unable to log in again  ([f8253](https://github.com/terraframe/osmre-uav/commit/f8253d14ef451417c7c5676a8ecee5d8c2518be5))
   - **upload** prevent resuming uploads that are already processing   ([0df58](https://github.com/terraframe/osmre-uav/commit/0df5854207718dd3282e065fba142b7053961d58))
   - **upload** multispectral extension validation too rigid   ([5d2dd](https://github.com/terraframe/osmre-uav/commit/5d2dd31fc00aa4cc170506894b24c9b49d7ebeb5))
   - **upload** clean up workflow tasks when canceling an upload   ([42197](https://github.com/terraframe/osmre-uav/commit/42197c95c84e02d4cc9c000010c7e2a09ef6f45c))
   - **session** errors when hitting login page if session had expired   ([5d26c](https://github.com/terraframe/osmre-uav/commit/5d26cad102ed7dd13cb35ebea2789ea51646d4f9))
   - **security** Various security enhancements
   - **metadata** Fix of user unable to update old format metadata   ([e617c](https://github.com/terraframe/osmre-uav/commit/e617c3caca1fc611843f8b5a1095bb9aa1a9272a))
   - Reprocess Imagery button to refresh correctly on tab change   ([8b559](https://github.com/terraframe/osmre-uav/commit/8b55949bbd109859057d79ea68f9c067e95c6ed7))
   - **keycloak** Upgrade keycloak dependency.   ([771ee](https://github.com/terraframe/osmre-uav/commit/771ee071728955679688c1a6f2c6ffb2d8e3f66d))



## [0.15.2](https://github.com/terraframe/osmre-uav/releases/tag/0.15.2) (2022-10-13)

### Features

 -  usfs and usda bureaus ([5ed8cab145213dd](https://github.com/terraframe/osmre-uav/commit/5ed8cab145213dd1193785134f5780dde6794629))
 -  support for boosting STAC results by the bounds of the view port ([2519e9f84f44691](https://github.com/terraframe/osmre-uav/commit/2519e9f84f446913a40fd5696d0c9f8bf44c2cbe))

### Bug Fixes

 -  fix of bad patch for incomplete STAC asset data ([8b95271f1ee831e](https://github.com/terraframe/osmre-uav/commit/8b95271f1ee831ede4ea4f5708d397d59edc329a))
 -  fixed issue with deletes not cleaning up the search index ([565e347e9c8a213](https://github.com/terraframe/osmre-uav/commit/565e347e9c8a213ec82e9c603c3a94e084e21d48))
 -  fixed issue with playing/downloading videos ([9c85ae3b374ee44](https://github.com/terraframe/osmre-uav/commit/9c85ae3b374ee443d743d8dd747970a72f23d896))
 -  product image previews not rendering ([71c169ae3f93685](https://github.com/terraframe/osmre-uav/commit/71c169ae3f93685b51ca5b24cf671cf635b14bd4))
 -  **security** Various security enhancements
 -  **metadata**  Fix of user unable to update old format metadata ([e617c3caca1fc61](https://github.com/terraframe/osmre-uav/commit/e617c3caca1fc611843f8b5a1095bb9aa1a9272a))
 -  **indexing**  Comment out unused dependency cxf-rt-rs-client. ([41ac2bbe90a733b](https://github.com/terraframe/osmre-uav/commit/41ac2bbe90a733bc9608215583825bcf26223e36))
 -  Reprocess Imagery button to refresh correctly on tab change ([8b55949bbd10985](https://github.com/terraframe/osmre-uav/commit/8b55949bbd109859057d79ea68f9c067e95c6ed7))
 -  **keycloak**  Upgrade keycloak dependency. ([771ee0717289556](https://github.com/terraframe/osmre-uav/commit/771ee071728955679688c1a6f2c6ffb2d8e3f66d))
 -  uasdm-server/pom.xml to reduce vulnerabilities ([27b71e5234e960c](https://github.com/terraframe/osmre-uav/commit/27b71e5234e960cc5330d4ac8a7c95deaccbef8e))

