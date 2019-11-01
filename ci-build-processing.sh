

if [ "$build_micasense" == "true" ]; then
	cd $WORKSPACE/uasdm/uasdm-micasense
	sudo ./init.sh
	sudo ./build.sh
	sudo ./test.sh
	sudo ./deploy.sh
fi

if [ "$build_odm" == "true" ]; then
	cd $WORKSPACE/uasdm/uasdm-odm
	sudo ./init.sh
	sudo ./build.sh
	sudo ./deploy.sh
fi
