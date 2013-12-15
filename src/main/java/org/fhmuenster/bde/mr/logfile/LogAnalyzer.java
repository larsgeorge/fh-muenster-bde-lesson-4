package org.fhmuenster.bde.mr.logfile;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class LogAnalyzer extends MapReduceBase implements
		Mapper<LongWritable, Text, Text, IntWritable>,
		Reducer<Text, IntWritable, Text, IntWritable> {

	private Text turi = new Text();
	private IntWritable tsize = new IntWritable();

	public void map(LongWritable offset, Text line,
			OutputCollector<Text, IntWritable> collector, Reporter reporter)
			throws IOException {
		// 127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700]
		// "GET /apache_pb.gif HTTP/1.0" 200 2326
		String[] parts = line.toString().split(" ");
		String uri = parts[6];
		turi.set(uri);
		int size = Integer.parseInt(parts[9]);
		tsize.set(size);
		collector.collect(turi, tsize);
	}

	public void reduce(Text uri, Iterator<IntWritable> values,
			OutputCollector<Text, IntWritable> collector, Reporter reporter)
			throws IOException {
		int sum = 0;
		while (values.hasNext()) {
			sum += values.next().get();
		}
		collector.collect(uri, new IntWritable(sum));
	}

	public void run(String inputPath, String outputPath) throws Exception {
		JobConf conf = new JobConf(LogAnalyzer.class);
		conf.setJobName("log-analyzer");

		// the keys are words (strings)
		conf.setOutputKeyClass(Text.class);
		// the values are counts (ints)
		conf.setOutputValueClass(IntWritable.class);

		conf.setMapperClass(LogAnalyzer.class);
		conf.setReducerClass(LogAnalyzer.class);

		FileInputFormat.addInputPath(conf, new Path(inputPath));
		FileOutputFormat.setOutputPath(conf, new Path(outputPath));

		JobClient.runJob(conf);
	}

	public static void main(String[] args) {
		LogAnalyzer la = new LogAnalyzer();
		try {
			la.run(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
