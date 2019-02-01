:
: ----------------------------------
:  CONFIGURE  
: ----------------------------------
:
# Configure the build based on Jenkins parameters
sed -i -e 's/ec2-52-33-51-128.us-west-2.compute.amazonaws.com/ip-172-31-22-81.us-west-2.compute.internal/g' geoprism-platform/ansible/inventory/uasdm/$environment.ini
sed -i -e "s/clean_db=true/clean_db=$clean_db/g" geoprism-platform/ansible/inventory/uasdm/$environment.ini
sed -i -e "s/clean_db=false/clean_db=$clean_db/g" geoprism-platform/ansible/inventory/uasdm/$environment.ini
sed -i -e "s/clean_solr=true/clean_solr=$clean_solr/g" geoprism-platform/ansible/inventory/uasdm/$environment.ini
sed -i -e "s/clean_solr=false/clean_solr=$clean_solr/g" geoprism-platform/ansible/inventory/uasdm/$environment.ini

#sudo docker run --name postgres -e POSTGRES_PASSWORD=postgres -d -p 5432:5432 mdillon/postgis:9.5

source /home/ec2-user/ansible/hacking/env-setup

export M2_HOME=/usr/local/apache-maven
export M2=$M2_HOME/bin 
export PATH=$M2:$PATH
export ANSIBLE_HOST_KEY_CHECKING=false


if [ "$build_artifact" == "true" ]; then
:
: ----------------------------------
:  BUILD
: ----------------------------------
:
## Build angular source ##
cd $WORKSPACE/geoprism/geoprism-web/src/main/ng2
npm install
npm install typings
typings install lodash
npm run build
cd $WORKSPACE/uasdm/uasdm-web/src/main/ng2
npm install
npm install typings
typings install lodash
npm run build

:
: ----------------------------------
:  TEST  
: ----------------------------------
:
if [ "$run_tests" == "true" ]; then
cd $WORKSPACE/uasdm
#mvn clean install -B -P patch -Dgeoprism.basedir=$WORKSPACE/geoprism -Droot.clean=true -Ddatabase.port=5432
#cd uasdm-test && mvn test -Dgeoprism.basedir=$WORKSPACE/geoprism -Ddatabase.port=5432
fi


:
: ----------------------------------
: DEPLOY ARTIFACT
: ----------------------------------
:
cd $WORKSPACE/uasdm
mvn clean deploy -B -P package-deployable -Dgeoprism.basedir=$WORKSPACE/geoprism
fi


:
: ----------------------------------
:  DEPLOY
: ----------------------------------
:

cd $WORKSPACE/geoprism-platform/ansible

[ -e ./roles ] && unlink ./roles
ln -s $WORKSPACE/geoprism-cloud/ansible/roles ./roles
[ -e ./uasdm.yml ] && unlink ./uasdm.yml
ln -s $WORKSPACE/geoprism-cloud/ansible/uasdm.yml ./uasdm.yml

sudo chmod 400 ../permissions/uasdm/rich.rowlands.id_rsa
sudo chmod 400 ../permissions/RichardsMacbook.pem
pip install boto

ansible-playbook -v -i ./inventory/uasdm/$environment.ini ./uasdm.yml