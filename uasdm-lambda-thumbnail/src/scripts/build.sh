# CLEAN
rm -r $THUMBNAIL_WORKSPACE/target
mkdir $THUMBNAIL_WORKSPACE/target

# COMPILE
cp $THUMBNAIL_WORKSPACE/src/index.js $THUMBNAIL_WORKSPACE/target/index.js

cd $THUMBNAIL_WORKSPACE/target
npm install async gm

# PACKAGE
cd $THUMBNAIL_WORKSPACE/target
zip -r uasdm-lambda-thumbnail.zip .


cd $THUMBNAIL_WORKSPACE
ls target
