NODE_MODULES=../ng2/node_modules

npm run build
[ -e $NODE_MODULES/geoprism ] && rm -r $NODE_MODULES/geoprism
cp -r ./lib $NODE_MOUDLES/geoprism
