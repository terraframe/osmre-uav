FROM amazoncorretto:11-alpine

ADD target/lib /erossync/lib
ADD target/classes /erossync/classes

ENTRYPOINT ["java","-cp","/erossync/lib/*:/erossync/classes","gov.osmre.uasdm.erossync.App"]
