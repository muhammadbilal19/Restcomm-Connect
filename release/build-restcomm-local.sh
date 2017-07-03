#!/bin/bash
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
export ANT_HOME=/opt/ant/apache-ant-1.8.3
export WORKSPACE=/tmp/workspace
mkdir $WORKSPACE
cp -ar ../* $WORKSPACE

CURRENT_BRANCH=`git rev-parse --abbrev-ref HEAD`
export MAJOR_VERSION_NUMBER=8.0.0
export RESTCOMM_BRANCH=$CURRENT_BRANCH
export RUN_TESTSUITE=false
export BUILD_NUMBER=$RESTCOMM_BRANCH-local

rm Mobicents-Restcomm*.zip
rm dependencies -rf

echo "MAJOR VERSION NUMBER: $MAJOR_VERSION_NUMBER"
echo "RESTCOMM BRANCH: $RESTCOMM_BRANCH"
echo "RUN TESTSUITE: $RUN_TESTSUITE"

export DEPENDENCIES_HOME=$WORKSPACE/dependencies
mkdir $DEPENDENCIES_HOME
export RESTCOMM_HOME=$WORKSPACE/Restcomm-Connect
mkdir $RESTCOMM_HOME/restcomm
export RELEASE=$RESTCOMM_HOME/release
cd $RESTCOMM_HOME/restcomm
# git checkout -b restcomm-release-$MAJOR_VERSION_NUMBER.$BUILD_NUMBER
# git rev-parse HEAD > git-info-restcomm.txt
echo $MAJOR_VERSION_NUMBER.$BUILD_NUMBER >> mss-version.txt

mvn versions:set -DnewVersion=$MAJOR_VERSION_NUMBER.$BUILD_NUMBER -P docs
git commit -a -m "New release candidate $MAJOR_VERSION_NUMBER.$BUILD_NUMBER"

cd $RELEASE
FILE=$RESTCOMM_HOME/restcomm/configuration/mss-sip-stack.properties
sed -e "s|MAJOR_VERSION_NUMBER.BUILD_NUMBER|$MAJOR_VERSION_NUMBER.$BUILD_NUMBER|g" $FILE > $FILE.bak
mv $FILE.bak $FILE
ant release -f $RESTCOMM_HOME/release/build.xml -Drestcomm.release.version=$MAJOR_VERSION_NUMBER.$BUILD_NUMBER -Drestcomm.branch.name=restcomm-release-$MAJOR_VERSION_NUMBER.$BUILD_NUMBER -Dcheckout.restcomm.dir=$RESTCOMM_HOME -Dworkspace.restcomm.dir=$RESTCOMM_HOME/restcomm -Dcheckout.dir=$DEPENDENCIES_HOME
mv $RELEASE/Restcomm-*.zip $WORKSPACE

cd $RESTCOMM_HOME/restcomm
#commenting the deploy command as it eats up storage on artifactory
#mvn deploy -Dmaven.test.skip=true

if [ "$RUN_TESTSUITE" = "true" ]
then
mvn -fn test -Dmaven.test.failure.ignore=true
else
echo "Will not run test suite because variable is $RUN_TESTSUITE"
fi

echo "$MAJOR_VERSION_NUMBER.$BUILD_NUMBER" > $WORKSPACE/restcomm-version.txt
ls -la $WORKSPACE/*.zip
md5sum $WORKSPACE/*.zip
sha1sum $WORKSPACE/*.zip

# git tag $MAJOR_VERSION_NUMBER.$BUILD_NUMBER
# git push origin $MAJOR_VERSION_NUMBER.$BUILD_NUMBER
