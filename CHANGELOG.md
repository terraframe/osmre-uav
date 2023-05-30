
# Release Notes


## [0.16.0](https://github.com/terraframe/osmre-uav/releases/tag/0.16.0) (2023-05-30)

### Features

 - **site-viewer** improve ortho preview  ([#222](https://github.com/terraframe/osmre-uav/issues/222)) ([4b369](https://github.com/terraframe/osmre-uav/commit/4b369efa389ca71df82a16c44db1ac6fd619fa55))
 - add support for feature-quality odm config option   ([3bd65](https://github.com/terraframe/osmre-uav/commit/3bd654f51bbf3433ba079ab68e0130e7ea5da4b6))
 - update collection metadata when calculating product bbox   ([de1df](https://github.com/terraframe/osmre-uav/commit/de1df2c534e767199bf498fbf8d3176d3efd239d))
 - ability to view ODM run information on collections   ([12281](https://github.com/terraframe/osmre-uav/commit/122818cd77b11407b22bc1d9be3db790b1752a97))
 - replace special characters in uploaded files with underscores   ([3cb1c](https://github.com/terraframe/osmre-uav/commit/3cb1cba310378bcfc36451027ccf66014fda9b0f))
 - ability to download individual product artifacts   ([718e2](https://github.com/terraframe/osmre-uav/commit/718e2257cf94a5b5a26ad8b082acac4af07a905a))
 - ability to include or exclude the geo location file for ODM processing  ([a48d4](https://github.com/terraframe/osmre-uav/commit/a48d41c76089b7b43f8835c72867d0d4299c2fdb))
 - add exponential retry backoff to ODM communication   ([fe623](https://github.com/terraframe/osmre-uav/commit/fe6230c80c5fe978f2ed0bc121ce1b4a2446c1b1))
 - support for boosting STAC results by the bounds of the view port   ([2519e](https://github.com/terraframe/osmre-uav/commit/2519e9f84f446913a40fd5696d0c9f8bf44c2cbe))
 - **test** Unit test for tika parsing   ([ebfb1](https://github.com/terraframe/osmre-uav/commit/ebfb137cea647503c43a2213866d897490b7c4e5))
 - **test** Added document generation as part of standard test setup   ([4a5f9](https://github.com/terraframe/osmre-uav/commit/4a5f913614ab0bbc7c1694c297bfdcd8905fc0da))
 - **test** RemoteFileFacade unit tests   ([13911](https://github.com/terraframe/osmre-uav/commit/139118527074e228f9b0331feec198d1bb351e1e))
 - **test** SiteItem serialization tests   ([f56d0](https://github.com/terraframe/osmre-uav/commit/f56d0a62ce840d4b7a87c1c753f412efc1f84771))
 - **test** mock implementation for index services   ([f27d5](https://github.com/terraframe/osmre-uav/commit/f27d5706df67fbe06160494c43fdd5a42572ad9d))

### Bug Fixes

   - **upload** upload chunk merging is a post-processing step  ([#220](https://github.com/terraframe/osmre-uav/issues/220)) ([9582d](https://github.com/terraframe/osmre-uav/commit/9582de9a6cdeb9f7cd2990d5c714c0ba8be32856))
   - StaleEntityException when running ortho re-run   ([3d2f6](https://github.com/terraframe/osmre-uav/commit/3d2f63897d8f1031d0c3cef38b572d8d2b3a6f85))
   - **login** sessions expiring could sometimes leave people unable to log in again  ([f8253](https://github.com/terraframe/osmre-uav/commit/f8253d14ef451417c7c5676a8ecee5d8c2518be5))
   - **upload** prevent resuming uploads that are already processing   ([0df58](https://github.com/terraframe/osmre-uav/commit/0df5854207718dd3282e065fba142b7053961d58))
   - **upload** multispectral extension validation too rigid   ([5d2dd](https://github.com/terraframe/osmre-uav/commit/5d2dd31fc00aa4cc170506894b24c9b49d7ebeb5))
   - **upload** clean up workflow tasks when canceling an upload   ([42197](https://github.com/terraframe/osmre-uav/commit/42197c95c84e02d4cc9c000010c7e2a09ef6f45c))
   - **session** errors when hitting login page if session had expired   ([5d26c](https://github.com/terraframe/osmre-uav/commit/5d26cad102ed7dd13cb35ebea2789ea51646d4f9))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426- https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518  ([0b8e9](https://github.com/terraframe/osmre-uav/commit/0b8e95c53e3495278cf6a74969c384751f4c083e))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426  ([fcf89](https://github.com/terraframe/osmre-uav/commit/fcf89ffecde23797fe3f96d579e7b8d206d751f6))
   - uasdm-fargate-erossync/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424  ([c341d](https://github.com/terraframe/osmre-uav/commit/c341db828f1bed44b24f10c806ab0fd46e061275))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGSPRINGFRAMEWORK-5422217  ([8f2f6](https://github.com/terraframe/osmre-uav/commit/8f2f66747f916e33bbf4dd834d44c5e4f7f413bb))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426  ([36ff9](https://github.com/terraframe/osmre-uav/commit/36ff9a8d588fbbe6d567b572f4dc429d7481069c))
   - uasdm-fargate-erossync/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426  ([8633b](https://github.com/terraframe/osmre-uav/commit/8633bdf2671641bbb4b48a219a9a638349cb3bd0))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426- https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518  ([caa09](https://github.com/terraframe/osmre-uav/commit/caa09c74c7ce8d2cb3657bd04924277abdccc5f7))
   - fix for issue where the supported format of internal archive files was not being displayed when uploading to the raw directory  ([0be80](https://github.com/terraframe/osmre-uav/commit/0be80ac713b7087b6fecba70178af6d831c82c16))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGSPRINGFRAMEWORK-3369852  ([2d593](https://github.com/terraframe/osmre-uav/commit/2d59352245cee2c78a2faf33bdebf4405961a818))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-2841508  ([4cb24](https://github.com/terraframe/osmre-uav/commit/4cb2497a4463da94030f89e159ef85a8f50250aa))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-NETLINGALAZIP4J-3227608  ([bcf54](https://github.com/terraframe/osmre-uav/commit/bcf54a63366edd2d9ff8db940b96edd8b41d50c1))
   - product image previews not rendering   ([71c16](https://github.com/terraframe/osmre-uav/commit/71c169ae3f93685b51ca5b24cf671cf635b14bd4))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082234- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082235- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082236- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1584063- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1584064- https://snyk.io/vuln/SNYK-JAVA-IONETTY-2812456  ([5e942](https://github.com/terraframe/osmre-uav/commit/5e942c8205f40615f39e560584fffe6ac19b5b6b))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082234- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082235- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082236- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1584063- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1584064- https://snyk.io/vuln/SNYK-JAVA-IONETTY-2812456  ([2af82](https://github.com/terraframe/osmre-uav/commit/2af820ba53b9beb20a9b7bbfc8c52fbb695014e5))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082234- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082235- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082236- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1584063- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1584064- https://snyk.io/vuln/SNYK-JAVA-IONETTY-2812456  ([a0739](https://github.com/terraframe/osmre-uav/commit/a07392b26b339c2199d2093d36379eac3c2cd1ef))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082234- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082235- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1082236- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1584063- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1584064- https://snyk.io/vuln/SNYK-JAVA-IONETTY-2812456  ([a34b2](https://github.com/terraframe/osmre-uav/commit/a34b28b9cb3ecd4850e3537ec5f3d03b70b83c4a))
   - Updated commons-compress to prevent DOS attacks   ([29e61](https://github.com/terraframe/osmre-uav/commit/29e613643e4dc03d53f5f64f81d8b92f4f58e083))
   - **metadata** Fix of user unable to update old format metadata   ([e617c](https://github.com/terraframe/osmre-uav/commit/e617c3caca1fc611843f8b5a1095bb9aa1a9272a))
   - updated jackson versions for security   ([0de1b](https://github.com/terraframe/osmre-uav/commit/0de1ba7c8e983adb2ef5c57f7d38e1b1cf0ebc93))
   - **indexing** Comment out unused dependency cxf-rt-rs-client.   ([41ac2](https://github.com/terraframe/osmre-uav/commit/41ac2bbe90a733bc9608215583825bcf26223e36))
   - removed unused downstream zookeeper dependency for security   ([bdc3b](https://github.com/terraframe/osmre-uav/commit/bdc3bfe4f5a6d4f0ee37fd249a044ed886a355c9))
   - Reprocess Imagery button to refresh correctly on tab change   ([8b559](https://github.com/terraframe/osmre-uav/commit/8b55949bbd109859057d79ea68f9c067e95c6ed7))
   - **keycloak** Upgrade keycloak dependency.   ([771ee](https://github.com/terraframe/osmre-uav/commit/771ee071728955679688c1a6f2c6ffb2d8e3f66d))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMAMAZONAWS-2952700- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-2326698- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-2421244- https://snyk.io/vuln/SNYK-JAVA-IONETTY-2812456  ([27b71](https://github.com/terraframe/osmre-uav/commit/27b71e5234e960cc5330d4ac8a7c95deaccbef8e))



## [0.15.2](https://github.com/terraframe/osmre-uav/releases/tag/0.15.2) (2022-10-13)

### Features

 -  usfs and usda bureaus ([5ed8cab145213dd](https://github.com/terraframe/osmre-uav/commit/5ed8cab145213dd1193785134f5780dde6794629))
 -  support for boosting STAC results by the bounds of the view port ([2519e9f84f44691](https://github.com/terraframe/osmre-uav/commit/2519e9f84f446913a40fd5696d0c9f8bf44c2cbe))
 -  **test**  Unit test for tika parsing ([ebfb137cea64750](https://github.com/terraframe/osmre-uav/commit/ebfb137cea647503c43a2213866d897490b7c4e5))
 -  **test**  Added document generation as part of standard test setup ([4a5f913614ab0bb](https://github.com/terraframe/osmre-uav/commit/4a5f913614ab0bbc7c1694c297bfdcd8905fc0da))
 -  **test**  RemoteFileFacade unit tests ([139118527074e22](https://github.com/terraframe/osmre-uav/commit/139118527074e228f9b0331feec198d1bb351e1e))
 -  **test**  SiteItem serialization tests ([f56d0a62ce840d4](https://github.com/terraframe/osmre-uav/commit/f56d0a62ce840d4b7a87c1c753f412efc1f84771))
 -  **test**  mock implementation for index services ([f27d5706df67fbe](https://github.com/terraframe/osmre-uav/commit/f27d5706df67fbe06160494c43fdd5a42572ad9d))

### Bug Fixes

 -  fix of bad patch for incomplete STAC asset data ([8b95271f1ee831e](https://github.com/terraframe/osmre-uav/commit/8b95271f1ee831ede4ea4f5708d397d59edc329a))
 -  fixed issue with deletes not cleaning up the search index ([565e347e9c8a213](https://github.com/terraframe/osmre-uav/commit/565e347e9c8a213ec82e9c603c3a94e084e21d48))
 -  fixed issue with playing/downloading videos ([9c85ae3b374ee44](https://github.com/terraframe/osmre-uav/commit/9c85ae3b374ee443d743d8dd747970a72f23d896))
 -  product image previews not rendering ([71c169ae3f93685](https://github.com/terraframe/osmre-uav/commit/71c169ae3f93685b51ca5b24cf671cf635b14bd4))
 -  uasdm-server/pom.xml to reduce vulnerabilities ([5e942c8205f4061](https://github.com/terraframe/osmre-uav/commit/5e942c8205f40615f39e560584fffe6ac19b5b6b))
 -  uasdm-server/pom.xml to reduce vulnerabilities ([2af820ba53b9beb](https://github.com/terraframe/osmre-uav/commit/2af820ba53b9beb20a9b7bbfc8c52fbb695014e5))
 -  uasdm-server/pom.xml to reduce vulnerabilities ([a07392b26b339c2](https://github.com/terraframe/osmre-uav/commit/a07392b26b339c2199d2093d36379eac3c2cd1ef))
 -  uasdm-server/pom.xml to reduce vulnerabilities ([a34b28b9cb3ecd4](https://github.com/terraframe/osmre-uav/commit/a34b28b9cb3ecd4850e3537ec5f3d03b70b83c4a))
 -  Updated commons-compress to prevent DOS attacks ([29e613643e4dc03](https://github.com/terraframe/osmre-uav/commit/29e613643e4dc03d53f5f64f81d8b92f4f58e083))
 -  **metadata**  Fix of user unable to update old format metadata ([e617c3caca1fc61](https://github.com/terraframe/osmre-uav/commit/e617c3caca1fc611843f8b5a1095bb9aa1a9272a))
 -  updated jackson versions for security ([0de1ba7c8e983ad](https://github.com/terraframe/osmre-uav/commit/0de1ba7c8e983adb2ef5c57f7d38e1b1cf0ebc93))
 -  **indexing**  Comment out unused dependency cxf-rt-rs-client. ([41ac2bbe90a733b](https://github.com/terraframe/osmre-uav/commit/41ac2bbe90a733bc9608215583825bcf26223e36))
 -  removed unused downstream zookeeper dependency for security ([bdc3bfe4f5a6d4f](https://github.com/terraframe/osmre-uav/commit/bdc3bfe4f5a6d4f0ee37fd249a044ed886a355c9))
 -  Reprocess Imagery button to refresh correctly on tab change ([8b55949bbd10985](https://github.com/terraframe/osmre-uav/commit/8b55949bbd109859057d79ea68f9c067e95c6ed7))
 -  **keycloak**  Upgrade keycloak dependency. ([771ee0717289556](https://github.com/terraframe/osmre-uav/commit/771ee071728955679688c1a6f2c6ffb2d8e3f66d))
 -  uasdm-server/pom.xml to reduce vulnerabilities ([27b71e5234e960c](https://github.com/terraframe/osmre-uav/commit/27b71e5234e960cc5330d4ac8a7c95deaccbef8e))

