=================================
FH Münster - Big Data Engineering
=================================

## Code für Übung Nummer 4

Das Repository enthält die MapReduce und Search Beispiele aus der letzten Vorlesung.

## Resourcen

Wie besprochen, hier einige Links mit Ideen zur Textbearbeitung:

* Flume basierend
   - [Blog Post 1](http://blog.cloudera.com/blog/2012/09/analyzing-twitter-data-with-hadoop/)
   - [Blog Post 2](http://blog.cloudera.com/blog/2012/10/analyzing-twitter-data-with-hadoop-part-2-gathering-data-with-flume/)
   - [Blog Post 3](http://blog.cloudera.com/blog/2012/11/analyzing-twitter-data-with-hadoop-part-3-querying-semi-structured-data-with-hive/)
   - [Blog Post 4](http://jameskinley.tumblr.com/post/57704266739/real-time-analytics-in-apache-flume-part-1)
* Data processing with Hive and MapReduce
   - [Data Wrangler](http://www.datawrangling.com/)
     + [Trending Topics](http://blog.cloudera.com/blog/2009/07/tracking-trends-with-hadoop-and-hive-on-ec2/)

## Building

### Maven

__Hinweis:__ Auf der Cloudera VM ist Maven bereits installiert und die folgenden Schritte sind nicht
notwending.

Die Schritte im Einzelnen, um Maven zum Laufen zu bekommen:

    $ wget http://mirror.derwebwolf.net/apache/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.tar.gz
    $ tar -zxvf apache-maven-3.1.1-bin.tar.gz
    $ apache-maven-3.1.1/bin/mvn

Am besten ist es das Verzeichnis, in dem Maven entpackt wurde, in der Shell Umgebung bekannt zu machen:

    $ EXPORT M2_HOME="<pfad>/apache-maven-3.1.1"

### Project Build

Wenn Maven installiert ist, kann das Projekt wie folgt übersetzt werden:

    $ git clone https://github.com/larsgeorge/fh-muenster-bde-lesson-4.git
    $ cd fh-muenster-bde-lesson-4
    $ $M2_HOME/bin/mvn package

Danach wie zuvor die JAR Datei aufrufen:

    $ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-mrjob.jar



## TF-IDF Übung

In dieser Übung ging es darum die Ausgabe des TF-IDF Codes aus der letzten Übung mit einem Lucene basierten Index zu vergleichen. Hier die einzelnen Schritte:

    $ # build project and run tf-idf code on supplied documents
    $ mvn package
    $ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar tfidf target/classes/books/ tf1

    $ # check that there is a result that looks coherent
    $ hadoop fs -text tf1/part-r-00000 | less
    aaron@pg4300.txt        [1/20 , 2/174698 , 0.00001489]
    aback@pg4300.txt        [3/20 , 1/174698 , 0.00000472]
    ...

    $ # run the Jetty server with the data
    $ /bin/sh target/bin/run -i tf1/part-r-00000

Danach steht die Jetty basierte Weboberfläche unter http://localhost:8080 zur Verfügung. Eine Suche sollte so aussehen:

![Suchergebnis](https://raw.github.com/larsgeorge/fh-muenster-bde-lesson-4/master/static/img/search1.png)

### Quellen

* [Blog Post 1](https://github.com/jshmain/cloudera-search/tree/master/email-search)
* [Blog Post 2](https://github.com/alo-alt/solr-demo)
* Kite SDK - [Morphlines](http://kitesdk.org/docs/current/kite-morphlines/index.html)

Viel Glück!

Lars George