=================================
FH Münster - Big Data Engineering
=================================

## Code für Übung Nummer 4

Das Repository enthält die MapReduce und Search Beispiele aus der letzten Vorlesung. Die commands.md Datei hat alle Schritte für diese Übung.

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

__Hinweise:__ 
- Auf der Cloudera VM ist Maven bereits installiert und die folgenden Schritte sind nicht
notwending.
- Am besten immer die aktuellste Version von Maven benutzen.

Die Schritte im Einzelnen, um Maven zum Laufen zu bekommen:

```
$ wget http://mirror.derwebwolf.net/apache/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.tar.gz
$ tar -zxvf apache-maven-3.1.1-bin.tar.gz
$ apache-maven-3.1.1/bin/mvn
```

Am besten ist es das Verzeichnis, in dem Maven entpackt wurde, in der Shell Umgebung bekannt zu machen:

```
$ export M2_HOME="<pfad>/apache-maven-3.1.1"
$ export PATH=$PATH:$M2_HOME/bin/
```

### Project Build

Wenn Maven installiert ist, kann das Projekt wie folgt übersetzt werden:

```
$ git clone https://github.com/larsgeorge/fh-muenster-bde-lesson-4.git
$ cd fh-muenster-bde-lesson-4
$ mvn package
```

Danach wie zuvor die JAR Datei aufrufen:

```
$ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar
```

### Quellen

* [Blog Post 1](https://github.com/jshmain/cloudera-search/tree/master/email-search)
* [Blog Post 2](https://github.com/alo-alt/solr-demo)
* Kite SDK - [Morphlines](http://kitesdk.org/docs/current/kite-morphlines/index.html)

Viel Glück!

Lars George

