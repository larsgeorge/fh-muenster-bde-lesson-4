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

## Datei Formate

Nachdem die JAR Datei wie oben beschrieben erstellt worden ist, kann man das Datei Format Beispiel ausführen. Dazu einfach die JAR Datei __ohne__ Parameter ausführen:

    $ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar
    An example program must be given as the first argument.
    Valid program names are:
      fileformats: Create various file formats.
      searchserver: Start the search server.
      testmorphline: Run a morphline locally.
      tfidf: MapReduce program to compute TF-IDF of input text files.

Die ganzen Optionen des Beispiels sind wiederum durch Aufruf __mit__ Programmname, aber __ohne__ weitere Argumente auflistbar: 

    $ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar fileformats
    ERROR: Missing parameters!
    usage: FileFormats [-c <arg>] [-f <arg>] [-h] [-k <arg>] [-n <arg>] -o
	   <filename> [-p <arg>] [-t <arg>]
     -c,--compressioncodec <arg>   the compression codec to use: snappy,
				   bzip2, gzip
     -f,--format <arg>             the format to write in: avro, sequence,
				   parquet
     -h,--help                     show this help
     -k,--sizeofkey <arg>          size in bytes of the record key
     -n,--numberofrecords <arg>    the number of records to create
     -o,--outputfile <filename>    name of the file to write to
     -p,--sizeofpayload <arg>      size in bytes of the record payload (value)
     -t,--compressiontype <arg>    the compression type to use: none, record,
				   block

Hier ein Test mit einem SequenceFile einmal ohne und einmal mit Komprimierung (hier Snappy):

    $ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar fileformats -o /tmp/sequence.bin -f sequence
    Writing to file: /tmp/sequence.bin
    Creating random arrays...
    14/12/23 11:44:42 INFO zlib.ZlibFactory: Successfully loaded & initialized native-zlib library
    14/12/23 11:44:42 INFO compress.CodecPool: Got brand-new compressor [.deflate]
    Starting loop...
    ..........
    Loop complete, emitted record count: 10000
    Elapsed time: 1052 ms
    Create file size: 2492359 bytes
    Path: hdfs://quickstart.cloudera:8020/tmp/sequence.bin

    $ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar fileformats -o /tmp/sequence.bin -f sequence -t block -c snappy
    Writing to file: /tmp/sequence.bin
    Creating random arrays...
    14/12/23 11:45:07 INFO compress.CodecPool: Got brand-new compressor [.snappy]
    Starting loop...
    ..........
    Loop complete, emitted record count: 10000
    Elapsed time: 456 ms
    Create file size: 122046 bytes
    Path: hdfs://quickstart.cloudera:8020/tmp/sequence.bin

Dies zeigt, dass ein SequenceFile mit Block Komprimierung mit Snappy ungefähr 20 mal kleiner ist, als ohne jegliche Komprimierung. 

### Quellen

* [Blog Post 1](https://github.com/jshmain/cloudera-search/tree/master/email-search)
* [Blog Post 2](https://github.com/alo-alt/solr-demo)
* Kite SDK - [Morphlines](http://kitesdk.org/docs/current/kite-morphlines/index.html)

Viel Glück!

Lars George

