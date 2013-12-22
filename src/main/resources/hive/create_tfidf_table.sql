CREATE TABLE tfidf (
  word STRING,
  bookname STRING,
  wordInDocs STRING,
  totalDocs STRING,
  wordInDoc STRING,
  wordsInDoc STRING,
  tfidf STRING)
ROW FORMAT SERDE 'org.apache.hadoop.hive.contrib.serde2.RegexSerDe'
WITH SERDEPROPERTIES (
  "input.regex" = "(\w+)@(\w+)\.\w+\s+\[\d+/\d+ , \d+/\d+ , (\S+)\]",
  "output.format.string" = "%1$s %2$s %3$s %4$s %5$s %6$s %7$s"
)
LOCATION '/user/cloudera/tf1';