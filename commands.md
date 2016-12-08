## Kommandos
Die folgenden Kommandos werden in der Cloudera QuickStart VM in einem Terminal Fenster ausgeführt. Dazu muss in der Menüzeile am oberen Bildschirmrand auf der linken Seite auf das Terminal Icon geklickt werden. Danach folgendes machen:

Klonen des Source Repositories in einem Terminal, mit anschliessendem Verzeichniswechsel:
```
$ git clone https://github.com/larsgeorge/fh-muenster-bde-lesson-4

Initialized empty Git repository in /home/cloudera/fh-muenster-bde-lesson-4/.git/
remote: Counting objects: 257, done.
remote: Total 257 (delta 0), reused 0 (delta 0), pack-reused 257
Receiving objects: 100% (257/257), 7.13 MiB | 111 KiB/s, done.
Resolving deltas: 100% (52/52), done.

$ cd fh-muenster-bde-lesson-4/
```

Dann kann man Maven (muss installiert sein) benutzen, um das Projekt zu compilieren und zu verpacken:
```
$ mvn package

[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building fh-muenster-bde-lesson-4 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
Downloading: http://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/2.3.2/maven-compiler-plugin-2.3.2.pom
Downloaded: http://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/2.3.2/maven-compiler-plugin-2.3.2.pom (8 KB at 10.7 KB/sec)
...
```

**Note** - Fehler beim Erstellen

Es kann bei Maven unter Umständen vorkommen, dass der Build Prozess mit einer Fehlermeldung wie unten gezeigt abbricht. Das ist meistens ein temporärer Fehler, und kann mit einen weiteren Versuch erfolgreich fortgesetzt werden (manchmal auch mehrfach anzuwenden):

```
...
Downloaded: http://repo.maven.apache.org/maven2/org/mockito/mockito-all/1.9.5/mockito-all-1.9.5.pom (2 KB at 5.4 KB/sec)
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 6:08.988s
[INFO] Finished at: Thu Dec 17 02:23:07 PST 2015
[INFO] Final Memory: 13M/105M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal on project fh-muenster-bde-lesson-4: Could not resolve dependencies for project org.fhmuenster.bde:fh-muenster-bde-lesson-4:jar:1.0-SNAPSHOT: Failed to collect dependencies for [org.apache.hadoop:hadoop-common:jar:2.5.0-cdh5.2.0 (compile), org.apache.hadoop:hadoop-hdfs:jar:2.5.0-cdh5.2.0 (compile), org.apache.hadoop:hadoop-client:jar:2.5.0-cdh5.2.0 (compile), 
...
org.hamcrest:hamcrest-core:jar:1.3 (compile), org.mockito:mockito-all:jar:1.9.5 (compile), junit:junit:jar:4.11 (test)]: Failed to read artifact descriptor for com.cloudera.cdk:cdk-morphlines-all:pom:0.9.2: Could not find artifact com.cloudera.cdk:cdk-morphlines:pom:0.9.2 in jboss-public-group (https://repository.jboss.org/nexus/content/groups/public) -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/DependencyResolutionException

$ mvn package
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5:13.315s
[INFO] Finished at: Thu Dec 17 02:28:34 PST 2015
[INFO] Final Memory: 32M/173M
[INFO] ------------------------------------------------------------------------
```

Am Ende sollte `BUILD SUCCESS` stehen. Damit wurden zwei JAR Dateien im `target` Verzeichnis erstellt. 

Optional, für die Hortonworks VM, zuerst die lokalen JARs und andere Ressourcen in die VM kopieren und dann wechseln:

```
$ scp -r -P 2222 src/main/resources/books root@localhost:/root/
$ scp -P 2222 target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT*.jar root@localhost:/root/
$ ssh root@localhost -p 2222
```

Nur die zweite, mit dem `-bin` Postfix ist die richtige mit eingebautem Manifest:

```
$ yarn jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT.jar 
RunJar jarFile [mainClass] args...

$ yarn jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar 
An example program must be given as the first argument.
Valid program names are:
  fileformats: Create various file formats.
  searchserver: Start the search server.
  tfidf: MapReduce program to compute TF-IDF of input text files.
```

### TF-IDF Suche

Um den Suchserver für diese Übung mit Daten zu versehen, muss man noch einmal den TF-IDF Index berechnen lassen. Dazu werden die mitgelieferten Top 20 Bücher auf Gutenberg.org nach HDFS kopiert, und dann der Job angestossen. Dies sieht leicht anders je nach VM aus.

Für die Hortonworks VM:

```
$ hdfs dfs -mkdir /user/root
$ hdfs dfs -put books
```

Für die Cloudera VM:

```
$ hdfs dfs -put src/main/resources/books
```

Dann wir der Job gestartet (hier mit der Ausgabe auf der Hortonworks VM):

```
$ yarn jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar tfidf books tfidf1
Still running...
16/12/08 11:15:48 INFO impl.TimelineClientImpl: Timeline service address: http://sandbox.hortonworks.com:8188/ws/v1/timeline/
16/12/08 11:15:48 INFO client.RMProxy: Connecting to ResourceManager at sandbox.hortonworks.com/172.17.0.2:8050
16/12/08 11:15:49 INFO client.AHSProxy: Connecting to Application History server at sandbox.hortonworks.com/172.17.0.2:10200
16/12/08 11:15:50 INFO input.FileInputFormat: Total input paths to process : 20
16/12/08 11:15:50 INFO lzo.GPLNativeCodeLoader: Loaded native gpl library
16/12/08 11:15:50 INFO lzo.LzoCodec: Successfully loaded & initialized native-lzo library [hadoop-lzo rev 7a4b57bedce694048432dd5bf5b90a6c8ccdba80]
16/12/08 11:15:51 INFO mapreduce.JobSubmitter: number of splits:20
16/12/08 11:15:51 INFO mapreduce.JobSubmitter: Submitting tokens for job: job_1481193792470_0001
16/12/08 11:15:52 INFO impl.YarnClientImpl: Submitted application application_1481193792470_0001
16/12/08 11:15:52 INFO mapreduce.Job: The url to track the job: http://sandbox.hortonworks.com:8088/proxy/application_1481193792470_0001/
Still running...
Still running...
...
```

Dies braucht ein wenig Zeit (es sind drei MapReduce Jobs), aber am Ende sollte der Index in HDFS liegen. Diesen dann bitte in das lokale Dateisystem kopieren, und zuletzt den Server starten:

```
...
Still running...

$ hdfs dfs -ls tfidf1
Found 2 items
-rw-r--r--   1 cloudera cloudera          0 2015-12-17 02:33 tfidf1/_SUCCESS
-rw-r--r--   1 cloudera cloudera    8436658 2015-12-17 02:33 tfidf1/part-r-00000
$ hdfs dfs -get tfidf1/part-r-00000 index.dat
$ ll
total 8304
-rw-r--r-- 1 cloudera cloudera 8436658 Dec 17 02:34 index.dat
-rw-rw-r-- 1 cloudera cloudera   39159 Dec 17 02:16 pom.xml
-rw-rw-r-- 1 cloudera cloudera    5870 Dec 17 02:16 README.md
drwxrwxr-x 2 cloudera cloudera    4096 Dec 17 02:16 slides
drwxrwxr-x 3 cloudera cloudera    4096 Dec 17 02:16 src
drwxrwxr-x 4 cloudera cloudera    4096 Dec 17 02:16 static
drwxrwxr-x 9 cloudera cloudera    4096 Dec 17 02:28 target
$ less index.dat 
$ /bin/sh target/bin/run -i index.dat 
...
```

Sollte alles geklappt haben, steht der Server unter `http://localhost:8080` zur Verfügung.

![Ergebnis einer Suche](https://raw.githubusercontent.com/larsgeorge/fh-muenster-bde-lesson-4/master/static/img/search1.png)

### Hive
Hive wird über die Kommandozeile gestartet. Dann kann man mit der DDL und DML Tabellen anlegen und anschauen:


```
$ hive

Logging initialized using configuration in file:/etc/hive/conf.dist/hive-log4j.properties
WARNING: Hive CLI is deprecated and migration to Beeline is recommended.
hive> show databases;
OK
default
Time taken: 0.445 seconds, Fetched: 1 row(s)
hive> show tables;
OK
hbase_table_1
pokes
Time taken: 0.205 seconds, Fetched: 2 row(s)
hive> create table testtable (key int, msg string) row format delimited fields terminated by ',';
OK
Time taken: 0.459 seconds
hive> select * from testtable;
OK
Time taken: 0.736 seconds
hive> exit;
```

Hinweis: Wir haben die Tabelle vorsorglich (siehe `echo` Befehl unten) so angelegt, das der Spaltentrenner ein Komma ist (Vorage ist ein binäres Steuerzeichen).

Tabellen in Hive sind per Vorgabe Dateien in HDFS, unter dem Verzeichnis `/user/hive/warehouse/<tablename>`. Schaut man sich dieses Verzeichnis an, ist es erst einmal leer. Wir legen dann dort eine Datei (der Name ist egal, auch viele Dateien angelegt werden), und speichern darin drei Zeilen mit Schlüssel und Werten:

```
$ hdfs dfs -ls /user/hive/warehouse
Found 1 items
drwxrwxrwx   - cloudera hive          0 2015-12-17 04:18 /user/hive/warehouse/testtable
$ hdfs dfs -ls /user/hive/warehouse/testtable

$ echo -e "1,foo\n2,bar\n3,hello" | hdfs dfs -put - /user/hive/warehouse/testtable/data.txt
$ hdfs dfs -ls /user/hive/warehouse/testtable
Found 1 items
-rw-r--r--   1 cloudera hive         20 2015-12-17 04:23 /user/hive/warehouse/testtable/data.txt
$ hdfs dfs -cat /user/hive/warehouse/testtable/data.txt
1,foo
2,bar
3,hello
```

Jetzt wieder Hive starten, und schauen, ob die Werte angezeigt werden. Das geht auch mit `WHERE` Klauseln:

```
$ hive
hive> select * from testtable;
OK
1	foo
2	bar
3	hello
Time taken: 1.461 seconds, Fetched: 3 row(s)
hive> select * from testtable where key in (1,2);
OK
1	foo
2	bar
Time taken: 0.196 seconds, Fetched: 2 row(s)
```

Zu beachten ist, das für diese einfachen Kommandos *kein* MapReduce Job gestartet wurde, sondern diese direkt in der Shell ausgeführt werden.

Jetzt legen wir eine neue Tabelle mit `SequenceFile` als Dateiformat an. Dann kopieren wir die Daten von der ersten in die zweite Tabelle, was einem umschreiben gleichkommt.

```
hive> create table testtable2 (key int, msg string) stored as sequencefile;
OK
Time taken: 0.249 seconds
hive> insert into testtable2 select * from testtable;
Query ID = cloudera_20151217042828_9cd1839f-1b55-43f9-aebf-44f4e4baddfd
Total jobs = 3
Launching Job 1 out of 3
Number of reduce tasks is set to 0 since there's no reduce operator
Starting Job = job_1448350936237_0008, Tracking URL = http://quickstart.cloudera:8088/proxy/application_1448350936237_0008/
...
OK
Time taken: 22.71 seconds
hive> select * from testtable2;
OK
1	foo
2	bar
3	hello
Time taken: 0.093 seconds, Fetched: 3 row(s)
hive> exit;
```

Im `testtable2` Verzeichnis liegen die Daten als `SequenceFile`s vor. Man koennte diese mit `-cat` ausgeben, was aber binäre Werte mitausgibt. Man sieht aber gut, das diese Dateien ein Signatur haben (`SEQ` als Magic Bytes), und auch den Schlüssel und Werte Typ (als `Writables`) speichern. Besser ist es die Datei mit der `-text` Option auszugeben, denn diese kennt `SequenceFiles` und druckt die Wertepaare zeilenweise über deren `toString()` methode aus:

```
$ hdfs dfs -ls /user/hive/warehouse/testtable2Found 1 items
-rwxrwxrwx   1 cloudera hive        143 2015-12-17 04:28 /user/hive/warehouse/testtable2/000000_0
[cloudera@quickstart fh-muenster-bde-lesson-4]$ hdfs dfs -cat /user/hive/warehouse/testtable2/000000_0
SEQ"org.apache.hadoop.io.BytesWritablesorg.apache.hadoop.io.Text"F�	�|}_|� ��au
1foo
2ar
3hello

$ hdfs dfs -text /user/hive/warehouse/testtable2/000000_0
	1foo
	2ar
	3hello
```

### Dateiformate
Die JAR Datei für die Übung kommt auch mit einer Implementierung für verschiedene Dateiformate. Diese erlaubt es, Testdaten in verschiedener Größe, Format und Komprimierung zu erzeugen:

```
$ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar fileformats -c snappy -f sequence -k 30 -n 100000 -p 1024 -t block -o snapseq30100k1kblk.bin
Writing to file: snapseq30100k1kblk.bin
Creating random arrays...
15/12/17 04:47:49 INFO compress.CodecPool: Got brand-new compressor [.snappy]
Starting loop...
....................................................................................................
Loop complete, emitted record count: 100000
Elapsed time: 396 ms
Create file size: 8265750 bytes
Path: hdfs://quickstart.cloudera:8020/user/cloudera/snapseq30100k1kblk.bin

$ hdfs dfs -ls -h 
Found 6 items
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:32 1-word-freq
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:33 2-word-counts
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:30 books
drwxr-xr-x   - cloudera cloudera          0 2015-11-23 14:10 performance_evaluation
-rw-r--r--   1 cloudera cloudera      7.9 M 2015-12-17 04:47 snapseq30100k1kblk.bin
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:33 tfidf1

$ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar fileformats -c snappy -f sequence -k 30 -n 100000 -p 1024 -t record -o snapseq30100k1krec.bin
Writing to file: snapseq30100k1krec.bin
Creating random arrays...
15/12/17 04:48:50 INFO compress.CodecPool: Got brand-new compressor [.snappy]
Starting loop...
....................................................................................................
Loop complete, emitted record count: 100000
Elapsed time: 1225 ms
Create file size: 109300118 bytes
Path: hdfs://quickstart.cloudera:8020/user/cloudera/snapseq30100k1krec.bin

$ hdfs dfs -ls -h 
Found 7 items
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:32 1-word-freq
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:33 2-word-counts
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:30 books
drwxr-xr-x   - cloudera cloudera          0 2015-11-23 14:10 performance_evaluation
-rw-r--r--   1 cloudera cloudera      7.9 M 2015-12-17 04:47 snapseq30100k1kblk.bin
-rw-r--r--   1 cloudera cloudera    104.2 M 2015-12-17 04:48 snapseq30100k1krec.bin
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:33 tfidf1

$ hadoop jar target/fh-muenster-bde-lesson-4-1.0-SNAPSHOT-bin.jar fileformats -c bzip2 -f sequence -k 30 -n 100000 -p 1024 -t block -o bzip2seq30100k1kblk.bin
Writing to file: bzip2seq30100k1kblk.bin
Creating random arrays...
15/12/17 04:49:51 INFO bzip2.Bzip2Factory: Successfully loaded & initialized native-bzip2 library system-native
15/12/17 04:49:51 INFO compress.CodecPool: Got brand-new compressor [.bz2]
Starting loop...
....................................................................................................
Loop complete, emitted record count: 100000
Elapsed time: 133532 ms
Create file size: 640408 bytes
Path: hdfs://quickstart.cloudera:8020/user/cloudera/bzip2seq30100k1kblk.bin

$ hdfs dfs -ls -h 
Found 8 items
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:32 1-word-freq
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:33 2-word-counts
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:30 books
-rw-r--r--   1 cloudera cloudera    625.4 K 2015-12-17 04:52 bzip2seq30100k1kblk.bin
drwxr-xr-x   - cloudera cloudera          0 2015-11-23 14:10 performance_evaluation
-rw-r--r--   1 cloudera cloudera      7.9 M 2015-12-17 04:47 snapseq30100k1kblk.bin
-rw-r--r--   1 cloudera cloudera    104.2 M 2015-12-17 04:48 snapseq30100k1krec.bin
drwxr-xr-x   - cloudera cloudera          0 2015-12-17 02:33 tfidf1
```

Beachten Sie die verbrauchte Zeit und die erzeugte Dateigröße. Man sieht das `bzip2` sehr lange braucht, aber auch mit Abstand die kleinste Datei erzeugt. `Snappy` ist sehr schnell, aber nicht so effektiv. Auch sieht man wie `RECORD` Komprimierung, selbst bei den gewählten 1K Daten pro Wertepaar, insgesamt viel schlechter als `BLOCK` Komprimierung abschneidet.

