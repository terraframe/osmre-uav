
set -e

sudo rpm -i https://packagecloud.io/github/git-lfs/packages/el/6/git-lfs-2.9.0-1.el6.x86_64.rpm/download

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
