ISPN_VERSION="10.1.7.Final";

wget -nc https://downloads.jboss.org/infinispan/${ISPN_VERSION}/infinispan-server-${ISPN_VERSION}.zip

unzip -o infinispan-server-${ISPN_VERSION}.zip -d server1
unzip -o infinispan-server-${ISPN_VERSION}.zip -d server2

SERVER1_HOME=server1/infinispan-server-${ISPN_VERSION}
SERVER2_HOME=server2/infinispan-server-${ISPN_VERSION}

mvn clean test -Dorg.infinispan.test.server.home=${SERVER1_HOME},${SERVER2_HOME}