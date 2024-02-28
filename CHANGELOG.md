
# Release Notes


## [1.0.4](https://github.com/terraframe/osmre-uav/releases/tag/1.0.4) (2024-02-28)

### Features

 - **collections** improve speed of opening collection modal and product viewer by at least 2x  ([fcd89](https://github.com/terraframe/osmre-uav/commit/fcd89aa40241c40524b3cee64d4880b805e1450e))




## [1.0.3](https://github.com/terraframe/osmre-uav/releases/tag/1.0.3) (2024-02-19)





## [1.0.2](https://github.com/terraframe/osmre-uav/releases/tag/1.0.2) (2024-02-15)


### Bug Fixes

   - **keycloak** fix 404 login issue on keycloak/loginRedirect  ([#315](https://github.com/terraframe/osmre-uav/issues/315)) ([621eb](https://github.com/terraframe/osmre-uav/commit/621eb8ef90ce48a5f831227f036f0c5b0affe8aa))
   - **processing** output file name prefix configuration option does nothing (refs #317) ([#317](https://github.com/terraframe/osmre-uav/issues/317)) ([947d3](https://github.com/terraframe/osmre-uav/commit/947d3e81a3951f92a5efc99a5a9aba7ab77319ee))
   - **keycloak** login events no longer being recorded in session log   ([a0900](https://github.com/terraframe/osmre-uav/commit/a09009727609cf2b512d23a6ee56807f268e4661))



## [1.0.1](https://github.com/terraframe/osmre-uav/releases/tag/1.0.1) (2024-02-08)

### Features

 - **report** system error report for collection processing   ([48844](https://github.com/terraframe/osmre-uav/commit/48844e379e042610c03e29df3a638f28ddde8efb))
 - **account** ability to import users into the system using a csv file   ([42bd1](https://github.com/terraframe/osmre-uav/commit/42bd1f553c126f51426fd54841008bb8b04d9557))

### Bug Fixes

   - **account** email is not case sensitive and a uniqueness constraint for users  ([14c16](https://github.com/terraframe/osmre-uav/commit/14c1655b8e3d0a8c6edc06a51bbbf645e3efd134))
   - **keycloak** allow admins to edit emails of existing users   ([7e827](https://github.com/terraframe/osmre-uav/commit/7e827f6a73b8e51a783eff929b084ff900310600))
   - **keycloak** ignore case when matching email   ([d0df9](https://github.com/terraframe/osmre-uav/commit/d0df97c5c545329b35fba5830501ffafac5a35d2))





## [0.20.1](https://github.com/terraframe/osmre-uav/releases/tag/0.20.1) (2024-01-22)





## [0.20.0](https://github.com/terraframe/osmre-uav/releases/tag/0.20.0) (2024-01-18)

### Features

 - **products** url link for public collections  ([#294](https://github.com/terraframe/osmre-uav/issues/294)) ([0955e](https://github.com/terraframe/osmre-uav/commit/0955edc1e0c7987cb2aae2f1f841820965be1079))
 - **tile-service** ability to hit public tile service without logging in  ([c5e4f](https://github.com/terraframe/osmre-uav/commit/c5e4fddbb9f093eeabbb811882682d6b85aa4f14))

### Bug Fixes

   - **site-viewer** elevation not rendering on map explorer  ([#286](https://github.com/terraframe/osmre-uav/issues/286)) ([23499](https://github.com/terraframe/osmre-uav/commit/234990045a4f42b173c07e9826381e7fca75bfb4))
   - **sensor** cant edit sensor metadata  ([#289](https://github.com/terraframe/osmre-uav/issues/289)) ([ed010](https://github.com/terraframe/osmre-uav/commit/ed010e9890273d42176c0ca343cd92ef2fc7e285))
   - **upload** manual upload instructions are wrong  ([#293](https://github.com/terraframe/osmre-uav/issues/293)) ([68ef9](https://github.com/terraframe/osmre-uav/commit/68ef9c56201a6e7991068ab0bf93940d623fb367))
   - **processing** null pointer exception thrown when uploading data   ([12ce4](https://github.com/terraframe/osmre-uav/commit/12ce481638fba056b40c5ba03cb6cce441ed2b6f))
   - remove action counter from hamburger   ([4b9a0](https://github.com/terraframe/osmre-uav/commit/4b9a0efbd82fbf2c145c193c917a71d23f28e566))



## [0.19.0](https://github.com/terraframe/osmre-uav/releases/tag/0.19.0) (2023-11-09)

### Features

 - **collection** ability to upload reports and product table styling (refs #216) ([#216](https://github.com/terraframe/osmre-uav/issues/216)) ([cab5f](https://github.com/terraframe/osmre-uav/commit/cab5f19a8ccc5d7a4aa95c804212f111f6bf9727))
 - **site-viewer** busy spinner on create collection modal  ([#223](https://github.com/terraframe/osmre-uav/issues/223)) ([7855b](https://github.com/terraframe/osmre-uav/commit/7855b1f6f05a929f87de28572b26d6fc3ccdf6e4))
 - **odm-config** geo location defaults when reprocessing  ([#232](https://github.com/terraframe/osmre-uav/issues/232)) ([4ba25](https://github.com/terraframe/osmre-uav/commit/4ba253b3b1aabf33ac7448da6eea6c5a9b04b7c8))
 - **workflow-tasks** Ability To View ODM Run Information For Failed Processing Runs (refs #233) ([#233](https://github.com/terraframe/osmre-uav/issues/233)) ([d3fcb](https://github.com/terraframe/osmre-uav/commit/d3fcbc9736f5f2b1cff1857c66840b17279473db))
 - **sensor** default geologger configuration by sensor  ([#241](https://github.com/terraframe/osmre-uav/issues/241)) ([b3425](https://github.com/terraframe/osmre-uav/commit/b3425e61cedcde01811cd9edc0a2e2af15665061))
 - **upload** better advanced settings styling  ([#243](https://github.com/terraframe/osmre-uav/issues/243)) ([45b92](https://github.com/terraframe/osmre-uav/commit/45b92cdf5a4109427ed1d5de8d0e9ec0d63d0722))
 - **collection** ability to reupload a geo location file  ([#245](https://github.com/terraframe/osmre-uav/issues/245)) ([6ae40](https://github.com/terraframe/osmre-uav/commit/6ae40687a02b318b506f735db80c690d0b044621))
 - **geolocation** show all geo location failures as a list of complete errors, not just a one by one basis (refs #246) ([#246](https://github.com/terraframe/osmre-uav/issues/246)) ([47ba0](https://github.com/terraframe/osmre-uav/commit/47ba02483e95d4ee153c76c44c674bfdb118c030))
 - **collection** increase speed for opening a collection  ([#247](https://github.com/terraframe/osmre-uav/issues/247)) ([bad3d](https://github.com/terraframe/osmre-uav/commit/bad3dc516b15d72b2b3901a9fc1f48b65b951313))
 - add a ProjectType field to project  ([#253](https://github.com/terraframe/osmre-uav/issues/253)) ([0be3e](https://github.com/terraframe/osmre-uav/commit/0be3eb99303ca67009ac4d9cf263b2f4c996536f))
 - **odm** Workswell Thermal EXIF Header  ([#267](https://github.com/terraframe/osmre-uav/issues/267)) ([1cc59](https://github.com/terraframe/osmre-uav/commit/1cc597ffe37f1917b5390ec316678d902bb6c85d))
 - **products** products tab needs to show file sizes  ([#278](https://github.com/terraframe/osmre-uav/issues/278)) ([d6a1b](https://github.com/terraframe/osmre-uav/commit/d6a1bfafc8e25601d3fcac6aa9912eea7483c055))
 - **processing** increase max runtime on prod to 7 days   ([fad7c](https://github.com/terraframe/osmre-uav/commit/fad7cb05f430b058695e8c9cd4ef991c4b9c484c))
 - **geologging** Added format validation for geo location files   ([ab1c5](https://github.com/terraframe/osmre-uav/commit/ab1c50b6121ab4777f70fcb1c5564b368faf466b))
 - **processing** ability to specify radiometric-calibration   ([fa734](https://github.com/terraframe/osmre-uav/commit/fa734f05831b2b5c0e2c4df6c403b0a4b2cc1ef9))

### Bug Fixes

   - **processing** prevent collection clobbering  ([#228](https://github.com/terraframe/osmre-uav/issues/228)) ([2fa5a](https://github.com/terraframe/osmre-uav/commit/2fa5a041f065678d9f0bab26fd0d17e076f81710))
   - **workflow-tasks** deleting workflow tasks  ([#229](https://github.com/terraframe/osmre-uav/issues/229)) ([ef951](https://github.com/terraframe/osmre-uav/commit/ef9512d37b169ac88e44abb0cf3f9e98aa17eb45))
   - **workflow-tasks** Workflow Task Timestamps Are In 12 Hour Clock And Missing AM/PM (refs #234) ([#234](https://github.com/terraframe/osmre-uav/issues/234)) ([161ba](https://github.com/terraframe/osmre-uav/commit/161bac9df73b7aa20244b63cc6372a5ebf8b70bd))
   - **collection** automatically check the "generate" boxes  ([#239](https://github.com/terraframe/osmre-uav/issues/239)) ([f3ca8](https://github.com/terraframe/osmre-uav/commit/f3ca8cec508ea0c0e7c1c02f11a3512de8fdb17d))
   - **upload** a few jobs are stalled in processing a manual ortho upload (refs #274) ([#274](https://github.com/terraframe/osmre-uav/issues/274)) ([b5e69](https://github.com/terraframe/osmre-uav/commit/b5e696e63820f94d26a3dac021fb8363819702ca))
   - users don't know the app is doing something  ([#275](https://github.com/terraframe/osmre-uav/issues/275)) ([c26b4](https://github.com/terraframe/osmre-uav/commit/c26b4f2f43cda31c871fb1b04a7d49e4c0f04d64))
   - **tasks** don't show processing in action required tab  ([#277](https://github.com/terraframe/osmre-uav/issues/277)) ([79608](https://github.com/terraframe/osmre-uav/commit/79608ab666a09f7926140afe9ba81e072cf62148))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-5771340  ([0d498](https://github.com/terraframe/osmre-uav/commit/0d498b583dab4e895591a9d5be522d094af3a230))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426- https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518  ([493ca](https://github.com/terraframe/osmre-uav/commit/493ca792bec6ba1a783cfaf7bd4bddca376042aa))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426- https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518  ([39c10](https://github.com/terraframe/osmre-uav/commit/39c1003f9300c5ef91900c5844e63a7bdab90f08))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGECLIPSEJETTY-5426161  ([31b7d](https://github.com/terraframe/osmre-uav/commit/31b7d39f2f1530c39a9f104e4de11cd159f277d5))
   - **tasks** inner directories are reported as info not warning   ([f37fc](https://github.com/terraframe/osmre-uav/commit/f37fced2efdbbc24639e7d0ffe0a629daec0cf07))
   - **processing** unspecified error generating bounding boxes   ([2c7e4](https://github.com/terraframe/osmre-uav/commit/2c7e42d0ebfc6cafd3883c8014fcd4b04d491ad2))
   - **uav-table** filtering on multiple columns throws error   ([218ef](https://github.com/terraframe/osmre-uav/commit/218ef3eed7ec283a07cd5050ad7bf1a0bf90949f))
   - **processing** occasional unspecified error during store   ([f80f5](https://github.com/terraframe/osmre-uav/commit/f80f55763b73f4642ae8b16c713e18b4b78370a7))
   - **collection-modal** not displaying raw .tiff images   ([e2a0c](https://github.com/terraframe/osmre-uav/commit/e2a0c3915405ad28aebe8e0d01ddd33e362a89ca))



## [0.18.1](https://github.com/terraframe/osmre-uav/releases/tag/0.18.1) (2023-09-12)

### Features

 - **collection** ability to upload reports and product table styling (refs #216) ([#216](https://github.com/terraframe/osmre-uav/issues/216)) ([cab5f](https://github.com/terraframe/osmre-uav/commit/cab5f19a8ccc5d7a4aa95c804212f111f6bf9727))
 - **site-viewer** busy spinner on create collection modal  ([#223](https://github.com/terraframe/osmre-uav/issues/223)) ([7855b](https://github.com/terraframe/osmre-uav/commit/7855b1f6f05a929f87de28572b26d6fc3ccdf6e4))
 - **odm-config** geo location defaults when reprocessing  ([#232](https://github.com/terraframe/osmre-uav/issues/232)) ([4ba25](https://github.com/terraframe/osmre-uav/commit/4ba253b3b1aabf33ac7448da6eea6c5a9b04b7c8))
 - **workflow-tasks** Ability To View ODM Run Information For Failed Processing Runs (refs #233) ([#233](https://github.com/terraframe/osmre-uav/issues/233)) ([d3fcb](https://github.com/terraframe/osmre-uav/commit/d3fcbc9736f5f2b1cff1857c66840b17279473db))
 - **sensor** default geologger configuration by sensor  ([#241](https://github.com/terraframe/osmre-uav/issues/241)) ([b3425](https://github.com/terraframe/osmre-uav/commit/b3425e61cedcde01811cd9edc0a2e2af15665061))
 - **upload** better advanced settings styling  ([#243](https://github.com/terraframe/osmre-uav/issues/243)) ([45b92](https://github.com/terraframe/osmre-uav/commit/45b92cdf5a4109427ed1d5de8d0e9ec0d63d0722))
 - **collection** ability to reupload a geo location file  ([#245](https://github.com/terraframe/osmre-uav/issues/245)) ([6ae40](https://github.com/terraframe/osmre-uav/commit/6ae40687a02b318b506f735db80c690d0b044621))
 - **geolocation** show all geo location failures as a list of complete errors, not just a one by one basis (refs #246) ([#246](https://github.com/terraframe/osmre-uav/issues/246)) ([47ba0](https://github.com/terraframe/osmre-uav/commit/47ba02483e95d4ee153c76c44c674bfdb118c030))
 - **collection** increase speed for opening a collection  ([#247](https://github.com/terraframe/osmre-uav/issues/247)) ([bad3d](https://github.com/terraframe/osmre-uav/commit/bad3dc516b15d72b2b3901a9fc1f48b65b951313))
 - add a ProjectType field to project  ([#253](https://github.com/terraframe/osmre-uav/issues/253)) ([0be3e](https://github.com/terraframe/osmre-uav/commit/0be3eb99303ca67009ac4d9cf263b2f4c996536f))
 - add bureau to account (profile) modal   ([58a6c](https://github.com/terraframe/osmre-uav/commit/58a6ce3c0a37a6d96b11e2fcc755ca36cce7862f))
 - **processing** increase max runtime on prod to 7 days   ([fad7c](https://github.com/terraframe/osmre-uav/commit/fad7cb05f430b058695e8c9cd4ef991c4b9c484c))
 - **geologging** Added format validation for geo location files   ([ab1c5](https://github.com/terraframe/osmre-uav/commit/ab1c50b6121ab4777f70fcb1c5564b368faf466b))
 - **processing** ability to specify radiometric-calibration   ([fa734](https://github.com/terraframe/osmre-uav/commit/fa734f05831b2b5c0e2c4df6c403b0a4b2cc1ef9))

### Bug Fixes

   - **processing** prevent collection clobbering  ([#228](https://github.com/terraframe/osmre-uav/issues/228)) ([2fa5a](https://github.com/terraframe/osmre-uav/commit/2fa5a041f065678d9f0bab26fd0d17e076f81710))
   - **workflow-tasks** deleting workflow tasks  ([#229](https://github.com/terraframe/osmre-uav/issues/229)) ([ef951](https://github.com/terraframe/osmre-uav/commit/ef9512d37b169ac88e44abb0cf3f9e98aa17eb45))
   - **workflow-tasks** Workflow Task Timestamps Are In 12 Hour Clock And Missing AM/PM (refs #234) ([#234](https://github.com/terraframe/osmre-uav/issues/234)) ([161ba](https://github.com/terraframe/osmre-uav/commit/161bac9df73b7aa20244b63cc6372a5ebf8b70bd))
   - **collection** automatically check the "generate" boxes  ([#239](https://github.com/terraframe/osmre-uav/issues/239)) ([f3ca8](https://github.com/terraframe/osmre-uav/commit/f3ca8cec508ea0c0e7c1c02f11a3512de8fdb17d))
   - odm output not present unless the all zip was generated   ([c5483](https://github.com/terraframe/osmre-uav/commit/c548319c55d0b70e76c22446713033c1189d78b5))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-5771340  ([0d498](https://github.com/terraframe/osmre-uav/commit/0d498b583dab4e895591a9d5be522d094af3a230))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426- https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518  ([493ca](https://github.com/terraframe/osmre-uav/commit/493ca792bec6ba1a783cfaf7bd4bddca376042aa))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426- https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518  ([39c10](https://github.com/terraframe/osmre-uav/commit/39c1003f9300c5ef91900c5844e63a7bdab90f08))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGECLIPSEJETTY-5426161  ([31b7d](https://github.com/terraframe/osmre-uav/commit/31b7d39f2f1530c39a9f104e4de11cd159f277d5))
   - **tasks** inner directories are reported as info not warning   ([f37fc](https://github.com/terraframe/osmre-uav/commit/f37fced2efdbbc24639e7d0ffe0a629daec0cf07))
   - **processing** unspecified error generating bounding boxes   ([2c7e4](https://github.com/terraframe/osmre-uav/commit/2c7e42d0ebfc6cafd3883c8014fcd4b04d491ad2))
   - **uav-table** filtering on multiple columns throws error   ([218ef](https://github.com/terraframe/osmre-uav/commit/218ef3eed7ec283a07cd5050ad7bf1a0bf90949f))
   - **processing** occasional unspecified error during store   ([f80f5](https://github.com/terraframe/osmre-uav/commit/f80f55763b73f4642ae8b16c713e18b4b78370a7))
   - **collection-modal** not displaying raw .tiff images   ([e2a0c](https://github.com/terraframe/osmre-uav/commit/e2a0c3915405ad28aebe8e0d01ddd33e362a89ca))



## [0.18.0](https://github.com/terraframe/osmre-uav/releases/tag/0.18.0) (2023-09-09)

### Features

 - **collection** ability to upload reports and product table styling (refs #216) ([#216](https://github.com/terraframe/osmre-uav/issues/216)) ([cab5f](https://github.com/terraframe/osmre-uav/commit/cab5f19a8ccc5d7a4aa95c804212f111f6bf9727))
 - **site-viewer** busy spinner on create collection modal  ([#223](https://github.com/terraframe/osmre-uav/issues/223)) ([7855b](https://github.com/terraframe/osmre-uav/commit/7855b1f6f05a929f87de28572b26d6fc3ccdf6e4))
 - **odm-config** geo location defaults when reprocessing  ([#232](https://github.com/terraframe/osmre-uav/issues/232)) ([4ba25](https://github.com/terraframe/osmre-uav/commit/4ba253b3b1aabf33ac7448da6eea6c5a9b04b7c8))
 - **workflow-tasks** Ability To View ODM Run Information For Failed Processing Runs (refs #233) ([#233](https://github.com/terraframe/osmre-uav/issues/233)) ([d3fcb](https://github.com/terraframe/osmre-uav/commit/d3fcbc9736f5f2b1cff1857c66840b17279473db))
 - **sensor** default geologger configuration by sensor  ([#241](https://github.com/terraframe/osmre-uav/issues/241)) ([b3425](https://github.com/terraframe/osmre-uav/commit/b3425e61cedcde01811cd9edc0a2e2af15665061))
 - **upload** better advanced settings styling  ([#243](https://github.com/terraframe/osmre-uav/issues/243)) ([45b92](https://github.com/terraframe/osmre-uav/commit/45b92cdf5a4109427ed1d5de8d0e9ec0d63d0722))
 - **collection** ability to reupload a geo location file  ([#245](https://github.com/terraframe/osmre-uav/issues/245)) ([6ae40](https://github.com/terraframe/osmre-uav/commit/6ae40687a02b318b506f735db80c690d0b044621))
 - **geolocation** show all geo location failures as a list of complete errors, not just a one by one basis (refs #246) ([#246](https://github.com/terraframe/osmre-uav/issues/246)) ([47ba0](https://github.com/terraframe/osmre-uav/commit/47ba02483e95d4ee153c76c44c674bfdb118c030))
 - **collection** increase speed for opening a collection  ([#247](https://github.com/terraframe/osmre-uav/issues/247)) ([bad3d](https://github.com/terraframe/osmre-uav/commit/bad3dc516b15d72b2b3901a9fc1f48b65b951313))
 - add a ProjectType field to project  ([#253](https://github.com/terraframe/osmre-uav/issues/253)) ([0be3e](https://github.com/terraframe/osmre-uav/commit/0be3eb99303ca67009ac4d9cf263b2f4c996536f))
 - **processing** increase max runtime on prod to 7 days   ([fad7c](https://github.com/terraframe/osmre-uav/commit/fad7cb05f430b058695e8c9cd4ef991c4b9c484c))
 - **geologging** Added format validation for geo location files   ([ab1c5](https://github.com/terraframe/osmre-uav/commit/ab1c50b6121ab4777f70fcb1c5564b368faf466b))
 - **processing** ability to specify radiometric-calibration   ([fa734](https://github.com/terraframe/osmre-uav/commit/fa734f05831b2b5c0e2c4df6c403b0a4b2cc1ef9))

### Bug Fixes

   - **processing** prevent collection clobbering  ([#228](https://github.com/terraframe/osmre-uav/issues/228)) ([2fa5a](https://github.com/terraframe/osmre-uav/commit/2fa5a041f065678d9f0bab26fd0d17e076f81710))
   - **workflow-tasks** deleting workflow tasks  ([#229](https://github.com/terraframe/osmre-uav/issues/229)) ([ef951](https://github.com/terraframe/osmre-uav/commit/ef9512d37b169ac88e44abb0cf3f9e98aa17eb45))
   - **workflow-tasks** Workflow Task Timestamps Are In 12 Hour Clock And Missing AM/PM (refs #234) ([#234](https://github.com/terraframe/osmre-uav/issues/234)) ([161ba](https://github.com/terraframe/osmre-uav/commit/161bac9df73b7aa20244b63cc6372a5ebf8b70bd))
   - **collection** automatically check the "generate" boxes  ([#239](https://github.com/terraframe/osmre-uav/issues/239)) ([f3ca8](https://github.com/terraframe/osmre-uav/commit/f3ca8cec508ea0c0e7c1c02f11a3512de8fdb17d))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-5771340  ([0d498](https://github.com/terraframe/osmre-uav/commit/0d498b583dab4e895591a9d5be522d094af3a230))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426- https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518  ([493ca](https://github.com/terraframe/osmre-uav/commit/493ca792bec6ba1a783cfaf7bd4bddca376042aa))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038424- https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-3038426- https://snyk.io/vuln/SNYK-JAVA-COMMONSCODEC-561518  ([39c10](https://github.com/terraframe/osmre-uav/commit/39c1003f9300c5ef91900c5844e63a7bdab90f08))
   - uasdm-server/pom.xml to reduce vulnerabilities The following vulnerabilities are fixed with an upgrade:- https://snyk.io/vuln/SNYK-JAVA-ORGECLIPSEJETTY-5426161  ([31b7d](https://github.com/terraframe/osmre-uav/commit/31b7d39f2f1530c39a9f104e4de11cd159f277d5))
   - **tasks** inner directories are reported as info not warning   ([f37fc](https://github.com/terraframe/osmre-uav/commit/f37fced2efdbbc24639e7d0ffe0a629daec0cf07))
   - **processing** unspecified error generating bounding boxes   ([2c7e4](https://github.com/terraframe/osmre-uav/commit/2c7e42d0ebfc6cafd3883c8014fcd4b04d491ad2))
   - **uav-table** filtering on multiple columns throws error   ([218ef](https://github.com/terraframe/osmre-uav/commit/218ef3eed7ec283a07cd5050ad7bf1a0bf90949f))
   - **processing** occasional unspecified error during store   ([f80f5](https://github.com/terraframe/osmre-uav/commit/f80f55763b73f4642ae8b16c713e18b4b78370a7))
   - **collection-modal** not displaying raw .tiff images   ([e2a0c](https://github.com/terraframe/osmre-uav/commit/e2a0c3915405ad28aebe8e0d01ddd33e362a89ca))



## [0.17.0](https://github.com/terraframe/osmre-uav/releases/tag/0.17.0) (2023-07-24)

### Features

 - **collection** ability to upload reports and product table styling (refs #216) ([#216](https://github.com/terraframe/osmre-uav/issues/216)) ([cab5f](https://github.com/terraframe/osmre-uav/commit/cab5f19a8ccc5d7a4aa95c804212f111f6bf9727))
 - **site-viewer** busy spinner on create collection modal  ([#223](https://github.com/terraframe/osmre-uav/issues/223)) ([7855b](https://github.com/terraframe/osmre-uav/commit/7855b1f6f05a929f87de28572b26d6fc3ccdf6e4))
 - **odm-config** geo location defaults when reprocessing  ([#232](https://github.com/terraframe/osmre-uav/issues/232)) ([4ba25](https://github.com/terraframe/osmre-uav/commit/4ba253b3b1aabf33ac7448da6eea6c5a9b04b7c8))
 - **workflow-tasks** Ability To View ODM Run Information For Failed Processing Runs (refs #233) ([#233](https://github.com/terraframe/osmre-uav/issues/233)) ([d3fcb](https://github.com/terraframe/osmre-uav/commit/d3fcbc9736f5f2b1cff1857c66840b17279473db))
 - **sensor** default geologger configuration by sensor  ([#241](https://github.com/terraframe/osmre-uav/issues/241)) ([b3425](https://github.com/terraframe/osmre-uav/commit/b3425e61cedcde01811cd9edc0a2e2af15665061))
 - **upload** better advanced settings styling  ([#243](https://github.com/terraframe/osmre-uav/issues/243)) ([45b92](https://github.com/terraframe/osmre-uav/commit/45b92cdf5a4109427ed1d5de8d0e9ec0d63d0722))
 - **collection** ability to reupload a geo location file  ([#245](https://github.com/terraframe/osmre-uav/issues/245)) ([6ae40](https://github.com/terraframe/osmre-uav/commit/6ae40687a02b318b506f735db80c690d0b044621))
 - **geolocation** show all geo location failures as a list of complete errors, not just a one by one basis (refs #246) ([#246](https://github.com/terraframe/osmre-uav/issues/246)) ([47ba0](https://github.com/terraframe/osmre-uav/commit/47ba02483e95d4ee153c76c44c674bfdb118c030))
 - **collection** increase speed for opening a collection  ([#247](https://github.com/terraframe/osmre-uav/issues/247)) ([bad3d](https://github.com/terraframe/osmre-uav/commit/bad3dc516b15d72b2b3901a9fc1f48b65b951313))
 - **processing** increase max runtime on prod to 7 days   ([fad7c](https://github.com/terraframe/osmre-uav/commit/fad7cb05f430b058695e8c9cd4ef991c4b9c484c))
 - **geologging** Added format validation for geo location files   ([ab1c5](https://github.com/terraframe/osmre-uav/commit/ab1c50b6121ab4777f70fcb1c5564b368faf466b))
 - **processing** ability to specify radiometric-calibration   ([fa734](https://github.com/terraframe/osmre-uav/commit/fa734f05831b2b5c0e2c4df6c403b0a4b2cc1ef9))

### Bug Fixes

   - **processing** prevent collection clobbering  ([#228](https://github.com/terraframe/osmre-uav/issues/228)) ([2fa5a](https://github.com/terraframe/osmre-uav/commit/2fa5a041f065678d9f0bab26fd0d17e076f81710))
   - **workflow-tasks** deleting workflow tasks  ([#229](https://github.com/terraframe/osmre-uav/issues/229)) ([ef951](https://github.com/terraframe/osmre-uav/commit/ef9512d37b169ac88e44abb0cf3f9e98aa17eb45))
   - **workflow-tasks** Workflow Task Timestamps Are In 12 Hour Clock And Missing AM/PM (refs #234) ([#234](https://github.com/terraframe/osmre-uav/issues/234)) ([161ba](https://github.com/terraframe/osmre-uav/commit/161bac9df73b7aa20244b63cc6372a5ebf8b70bd))
   - **collection** automatically check the "generate" boxes  ([#239](https://github.com/terraframe/osmre-uav/issues/239)) ([f3ca8](https://github.com/terraframe/osmre-uav/commit/f3ca8cec508ea0c0e7c1c02f11a3512de8fdb17d))
   - **tasks** inner directories are reported as info not warning   ([f37fc](https://github.com/terraframe/osmre-uav/commit/f37fced2efdbbc24639e7d0ffe0a629daec0cf07))
   - **processing** unspecified error generating bounding boxes   ([2c7e4](https://github.com/terraframe/osmre-uav/commit/2c7e42d0ebfc6cafd3883c8014fcd4b04d491ad2))
   - **uav-table** filtering on multiple columns throws error   ([218ef](https://github.com/terraframe/osmre-uav/commit/218ef3eed7ec283a07cd5050ad7bf1a0bf90949f))
   - **processing** occasional unspecified error during store   ([f80f5](https://github.com/terraframe/osmre-uav/commit/f80f55763b73f4642ae8b16c713e18b4b78370a7))
   - **collection-modal** not displaying raw .tiff images   ([e2a0c](https://github.com/terraframe/osmre-uav/commit/e2a0c3915405ad28aebe8e0d01ddd33e362a89ca))



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

