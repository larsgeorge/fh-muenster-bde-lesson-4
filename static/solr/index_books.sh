#!/bin/sh
# HOSTNAME = the hostname of your ZK as well as NN (assumes that's on one host)
# USER = the local as well as hdfs user, should be the same (for kerberos setups especially)
# DOCS_DIR = the path to your documents (ex: /home/foo/bar/docs)

HOSTNAME=localhost
USER=cloudera
DOCS_DIR=/home/$USER/fh-muenster-bde-lesson-4/src/main/resources/books
SOLR_CFG_DIR=bd4_u4_solr_config
PROJECT_NAME=bde_u4_load_solr
COLLECTION_NAME=bdeu4docs

export PROJECT_HOME=/home/$USER/$PROJECT_NAME

#export SOLR_HOME=/opt/cloudera/parcels/SOLR-0.9.3-1.cdh4.3.0.p0.366/lib/solr
export SOLR_HOME=/usr/lib/solr

# remove remnants of earlier attempts
rm -rf $PROJECT_HOME
mkdir $PROJECT_HOME

# create default config directory
solrctl --zk $HOSTNAME:2181/solr instancedir --generate $PROJECT_HOME/$SOLR_CFG_DIR
# copy schema and morphline and other files
cp bde_u4_solr_schema.xml $PROJECT_HOME/$SOLR_CFG_DIR/conf/schema.xml
cp loadBooksIntoSolr.conf $PROJECT_HOME/
cp -r $DOCS_DIR $PROJECT_HOME/
cp log4j.props.solr $PROJECT_HOME/log4j.properties
# remove old and create new collection
solrctl --zk $HOSTNAME:2181/solr instancedir --delete $COLLECTION_NAME
solrctl --zk $HOSTNAME:2181/solr instancedir --create $COLLECTION_NAME $PROJECT_HOME/$SOLR_CFG_DIR
solrctl --zk $HOSTNAME:2181/solr collection --delete PDFCollection
solrctl --zk $HOSTNAME:2181/solr collection --create PDFCollection -s 1
# prepare HDFS side
hadoop fs -rm -r /user/$USER/$PROJECT_NAME/outdir
hadoop fs -rm -r /user/$USER/$DOCS_DIR
hadoop fs -mkdir -p /user/$USER/$PROJECT_NAME/outdir
hadoop fs -put $PROJECT_HOME/$DOCS_DIR /user/$USER/
# run actual job
hadoop jar $SOLR_HOME/contrib/mr/search-mr-1.0.0-job.jar org.apache.solr.hadoop.MapReduceIndexerTool -D 'mapred.child.java.opts=-Xmx500m' --log4j $PROJECT_HOME/log4j.properties --morphline-file $PROJECT_HOME/loadBooksIntoSolr.conf --output-dir hdfs://$HOSTNAME:8020/user/$USER/$PROJECT_NAME/outdir --verbose --go-live --zk-host $HOSTNAME:2181/solr --collection $COLLECTION_NAME hdfs://$HOSTNAME:8020/user/$USER/$DOCS_DIR

echo "DONE."