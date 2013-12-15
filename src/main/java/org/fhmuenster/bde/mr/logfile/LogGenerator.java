package org.fhmuenster.bde.mr.logfile;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class LogGenerator {

  private String[] uri = new String[] { "index.html", "contacts.html", "logo.gif" };
  private Random rand = new Random();

  public String getLine(boolean addNewLine) {
    // 127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
    StringBuilder sb = new StringBuilder();
    sb.append("10.0.1." + (rand.nextInt(10) + 1)).append(" - - ");
    sb.append("[10/Oct/2000:13:55:36 -0700] \"GET /").append(uri[rand.nextInt(uri.length)])
      .append(" HTTP/1.0\" ");
    sb.append(200).append(" ").append(rand.nextInt(3000));
    if (addNewLine) {
      sb.append("\n");
    }
    return sb.toString();
  }

  private CommandLine parseCommandLine(String[] args) {
    Options options = new Options();

    OptionGroup formats = new OptionGroup();
    Option o = new Option("t", "text", false, "write file in textfile format");
    formats.addOption(o);
    o = new Option("s", "sequence", false, "write file in sequencefile format");
    formats.addOption(o);
    options.addOptionGroup(formats);

    o = new Option("w", "writer", true, "sequencefile compression type");
    o.setArgName("{none|record|block}");
    options.addOption(o);
    o = new Option("c", "codec", true, "compression codec to use");
    o.setArgName("{zlib|gzip|bzip2|lzo|lz4|snappy}");
    options.addOption(o);
    o = new Option("j", "java", false, "use java compression codecs (not the native ones)");
    options.addOption(o);

    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (Exception e) { /* caught below */ }
    if (args.length == 0 || cmd == null) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("LogGenerator ", options, true);
      System.exit(-1);
    }
    return cmd;
  }

  public void run(String outputPath, int count) throws Exception {
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);
    Path outPath = new Path(outputPath);
    FSDataOutputStream out = fs.create(outPath);
    for (int n = 0; n < count; n++) {
      out.write(getLine(true).getBytes("ASCII"));
    }
    out.flush();
    out.close();
  }

  public static void main(String[] args) {
    LogGenerator lg = new LogGenerator();
    try {
      lg.run(args[0], Integer.parseInt(args[1]));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
