package org.fhmuenster.bde.fileformats;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;

/**
 * This program allows to create various Hadoop file formats.
 * 
 * @author larsgeorge
 */
public class FileFormats {

  private static final int DEFAULT_RECORD_COUNT = 10000;
  private static final int DEFAULT_KEY_SIZE = 20;
  private static final int DEFAULT_PAYLOAD_SIZE = 200;
  
  private static final String AVRO_SCHEMA = "{ " +
    "\"type\": \"record\", " +
    "\"name\": \"KeyValuePair\", " +
    "\"doc\": \"KeyValue Pair.\", " +
    "\"fields\": [ " +
    "  { \"name\": \"key\", \"type\": \"bytes\" }, " +
    "  { \"name\": \"payload\", \"type\": \"bytes\" }" +
    "] }";

  private Configuration configuration;
  private FileSystem hdfs;
  private Path path;
  private FileFormat fileFormat = FileFormat.TEXT;
  private CompressionCodec compressionCodec = CompressionCodec.NONE;
  private CompressionType compressionType = CompressionType.NONE;
  private int recordCount = DEFAULT_RECORD_COUNT;
  private int keySize = DEFAULT_KEY_SIZE;
  private int payloadSize = DEFAULT_PAYLOAD_SIZE;
  private Random random = new Random();
  
  enum FileFormat { TEXT, AVRO, SEQUENCE, PARQUET }
  enum CompressionType { NONE, RECORD, BLOCK }

  /**
   * Enum that encapsulates the details for compression codecs.
   */
  enum CompressionCodec {
    NONE(null),
    DEFLATE("org.apache.hadoop.io.compress.DefaultCodec"),
    GZIP("org.apache.hadoop.io.compress.GzipCodec"),
    BZIP2("org.apache.hadoop.io.compress.BZip2Codec"),
    LZO("com.hadoop.compression.lzo.LzopCodec"),
    LZ4("org.apache.hadoop.io.compress.Lz4Codec"),
    SNAPPY("org.apache.hadoop.io.compress.SnappyCodec");

    private String classname;

    CompressionCodec(String classname) {
      this.classname = classname;      
    }
    
    
    public String getClassname() {
      return classname;
    }

    public org.apache.hadoop.io.compress.CompressionCodec getCodec() 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {
      Class<?> clazz = Class.forName(classname);
      return (org.apache.hadoop.io.compress.CompressionCodec) clazz.newInstance();
    }
  }

  /**
   * Factory class to create matching writer implementation.
   */
  public class WriterFactory {
    public Writer getWriter(Path path, FileFormat format, CompressionCodec codec, 
      CompressionType ctype) throws IOException {
      switch (format) {
      case AVRO:
        return new AvroWriter(path, codec, ctype);
      case SEQUENCE:
        return new SequenceWriter(path, codec, ctype);
      case PARQUET:
        return new ParquetWriter(path, codec, ctype);
      default: // same as TEXT
        return new TextWriter(path, codec);
      }
    }
  }

  /**
   * Helper to implement writers for various output types.
   */
  public abstract class Writer {
    private Path path;
    private CompressionCodec compressionCodec = CompressionCodec.NONE;
    private CompressionType compressionType = CompressionType.NONE;

    public Writer() {
    }

    public Writer(Path path) {
      this.path = path;
    }

    public Writer(Path path, CompressionCodec compressionCodec) {
      this.path = path;
      this.compressionCodec = compressionCodec;
    }

    public Writer(Path path, CompressionCodec compressionCodec, CompressionType compressionType) {
      this.path = path;
      this.compressionCodec = compressionCodec;
      this.compressionType = compressionType;
    }

    public abstract void write(byte[] key, byte[] payload) throws IOException;    
    public abstract void close() throws IOException;
  }


  /**
   * Writer for specific output format.
   */
  public class AvroWriter extends Writer {
    private Schema schema;
    private FSDataOutputStream out;
    private DatumWriter<GenericRecord> writer;
    private Encoder encoder;
    private ByteBuffer keyData;
    private ByteBuffer payloadData;
    
    public AvroWriter(Path path, CompressionCodec compressionCodec, 
      CompressionType compressionType) throws IOException {
      super(path, compressionCodec, compressionType);
      Schema.Parser parser = new Schema.Parser();
      schema = parser.parse(AVRO_SCHEMA);
      out = hdfs.create(path);
      writer = new GenericDatumWriter<GenericRecord>(schema);
      encoder = EncoderFactory.get().binaryEncoder(out, null);
    }
    
    @Override
    public void write(byte[] key, byte[] payload) throws IOException {
      GenericRecord record = new GenericData.Record(schema);
      keyData = ByteBuffer.wrap(key);
      payloadData = ByteBuffer.wrap(payload);
      record.put("key", keyData);
      record.put("payload", payloadData);
      writer.write(record, encoder);
    }

    @Override
    public void close() throws IOException {
      encoder.flush();
      out.close();
    }
  }

  /**
   * Writer for specific output format.
   */
  public class SequenceWriter extends Writer {
    private SequenceFile.Writer out;
    private BytesWritable keyData = new BytesWritable();
    private BytesWritable payloadData = new BytesWritable();
    
    public SequenceWriter(Path path, CompressionCodec compressionCodec, 
      CompressionType compressionType) throws IOException {
      super(path, compressionCodec, compressionType);
      SequenceFile.Writer.Option[] opts = { SequenceFile.Writer.file(path), 
        SequenceFile.Writer.keyClass(BytesWritable.class), 
        SequenceFile.Writer.valueClass(BytesWritable.class) };
      if (compressionType != CompressionType.NONE) 
        try { 
          opts = org.apache.hadoop.util.Options.prependOptions(opts, 
            SequenceFile.Writer.compression(
              SequenceFile.CompressionType.valueOf(compressionType.toString()), 
              compressionCodec.getCodec()));
        } catch (Exception e) {
          throw new IOException("Compression failed to load!", e);
        }
      out = SequenceFile.createWriter(configuration, opts);
    }

    @Override
    public void write(byte[] key, byte[] payload) throws IOException {
      keyData.set(key, 0, key.length);
      payloadData.set(payload, 0, payload.length);
      out.append(keyData, payloadData);
    }

    @Override
    public void close() throws IOException {
      out.close();      
    }
  }

  /**
   * Writer for specific output format.
   */
  public class ParquetWriter extends Writer {
    public ParquetWriter(Path path, CompressionCodec compressionCodec, 
      CompressionType compressionType) throws IOException {
      super(path, compressionCodec, compressionType);
    }

    @Override
    public void write(byte[] key, byte[] payload) {

    }

    @Override
    public void close() throws IOException {
      
    }
  }

  /**
   * Writer for specific output format.
   */
  public class TextWriter extends Writer {
    private FSDataOutputStream out;
    
    public TextWriter(Path path, CompressionCodec compressionCodec) throws IOException {
      super(path, compressionCodec);
      out = hdfs.create(path);
    }

    @Override
    public void write(byte[] key, byte[] payload) throws IOException {
      out.write(key);
      out.write('\t');
      out.write(payload);
    }

    @Override
    public void close() throws IOException {
      out.flush();
      out.close();
    }
  }

  /**
   * Constructor for main class. 
   * @throws IOException When there is an issue with HDFS.
   */
  public FileFormats() throws IOException {
    configuration = new Configuration();
    hdfs = FileSystem.get(configuration);
  }
  
  /**
   * Parses the command line arguments.
   * 
   * @param args The command line arguments to parse.
   * @return The parsed arguments.
   * @throws ParseException When there is an issue parsing the arguments.
   */
  private CommandLine parseArgs(String[] args) throws ParseException {
    // create options
    Options options = new Options();
    Option o = new Option("o", "outputfile", true, "name of the file to write to");
    o.setArgName("filename");
    o.setRequired(true);
    options.addOption(o);
    options.addOption("n", "numberofrecords", true, "the number of records to create");
    options.addOption("k", "sizeofkey", true, "size in bytes of the record key");
    options.addOption("p", "sizeofpayload", true, "size in bytes of the record payload (value)");
    options.addOption("f", "format", true, "the format to write in: avro, sequence, parquet");
    options.addOption("c", "compressioncodec", true,
      "the compression codec to use: snappy, bzip2, gzip");
    options.addOption("t", "compressiontype", true,
      "the compression type to use: none, record, block");
    // options.addOption("d", "debug", false, "switch on DEBUG log level");
    options.addOption("h", "help", false, "show this help");
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (Exception e) { /* caught below */
    }
    // print help
    boolean error = args.length == 0 || cmd == null;
    if (error || cmd.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      if (error)
        System.err.println("ERROR: Missing parameters!");
      formatter.printHelp("FileFormats", options, true);
      System.exit(0);
    }
    determineConfiguration(cmd);
    return cmd;
  }

  /**
   * Uses the parses parameters to determine the configuration for this invocation.
   * 
   * @param params The command line parameters.
   */
  private void determineConfiguration(CommandLine params) {
    // determine what should be done
    if (params.hasOption("f"))
      fileFormat = FileFormat.valueOf(params.getOptionValue("f").toUpperCase());
    if (params.hasOption("n"))
      recordCount = Integer.parseInt(params.getOptionValue("n"));    
    if (params.hasOption("k"))
      keySize = Integer.parseInt(params.getOptionValue("k"));
    if (params.hasOption("p"))
      payloadSize = Integer.parseInt(params.getOptionValue("p"));
    if (params.hasOption("n"))
      recordCount = Integer.parseInt(params.getOptionValue("n"));
    if (params.hasOption("t"))
      compressionType = CompressionType.valueOf(params.getOptionValue("t").toUpperCase());
    if (params.hasOption("c"))
      compressionCodec = CompressionCodec.valueOf(params.getOptionValue("c").toUpperCase());
  }

  /**
   * Creates an array of the given length with random bytes.
   * 
   * @param size The array length.
   * @return The new array.
   */
  private byte[] createByteArray(int size) {
    byte[] res = new byte[size];
    random.nextBytes(res); 
    return res;
  }

  /**
   * Creates a file with the given properties.
   * 
   * @param params The command line flags to use.
   * @throws IOException When something with the I/O fails.
   */
  private void createFile(CommandLine params) throws IOException {
    // create path and key + payload arrays
    Path path = new Path(params.getOptionValue("o"));
    System.out.println("Writing to file: " + path.toUri());
    System.out.println("Creating random arrays...");
    byte[] key = createByteArray(keySize);
    byte[] payload = createByteArray(payloadSize);
    // create proper writer instance
    Writer w = new WriterFactory().getWriter(path, fileFormat, compressionCodec, compressionType);
    // do the loop to create the entries
    System.out.println("Starting loop...");
    long time = System.currentTimeMillis();
    for (int n = 0; n < recordCount; n++) {
      w.write(key, payload);
      if (n % 1000 == 0) System.out.print(".");
    }
    System.out.println("\nLoop complete, emitted record count: " + recordCount);
    // close writer, this is implementation specific
    w.close();
    // print statistics
    System.out.println("Elapsed time: " + (System.currentTimeMillis() - time) + " ms");
    ContentSummary cs = hdfs.getContentSummary(path);
    System.out.println("Create file size: " + cs.getLength() + " bytes");
    System.out.println("Path: " + hdfs.makeQualified(path));
  }

  /**
   * Main function, entry point into application. Parses command line parameters and starts
   * processing.
   * 
   * @param args The command line arguments.
   * @throws Exception When something goes awry.
   */
  public static void main(String[] args) throws Exception {
    FileFormats fileFormats = new FileFormats();
    CommandLine cli = fileFormats.parseArgs(args);
    fileFormats.createFile(cli);
  }
}
