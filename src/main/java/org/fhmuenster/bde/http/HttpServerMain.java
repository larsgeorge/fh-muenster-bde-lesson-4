package org.fhmuenster.bde.http;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import com.google.inject.servlet.GuiceFilter;

import org.fhmuenster.bde.config.PortalConfiguration;

public class HttpServerMain {

  protected final static Logger LOG = LoggerFactory.getLogger(HttpServerMain.class);

  private PortalConfiguration configuration;
  private static int webPort = 8080;

  private CommandLine parseArgs(String[] args) throws ParseException {
    // create options
    Options options = new Options();
    Option o = new Option("i", "input", true, "specify where to load the index from");
    o.setArgName("filename");
    o.setRequired(true);
    options.addOption(o);
    options.addOption("c", "conf", true, "specify a different configuration file");
    options.addOption("p", "port", true, "specify the port to listen to");
    //options.addOption("d", "debug", false, "switch on DEBUG log level");
    options.addOption("h", "help", false, "show this help");
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (Exception e) { /* caught below */ }
    // print help
    boolean error = args.length == 0 || cmd == null;
    if (error || cmd.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      if (error) System.err.println("ERROR: Missing parameters!");
      formatter.printHelp("HttpServerMain", options, true);
      System.exit(0);
    }
    return cmd;
  }

  private static File getScratchDir() throws IOException {
 		File tempDir = new File(System.getProperty("java.io.tmpdir"));
 		File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

 		if (!scratchDir.exists()) {
 			if (!scratchDir.mkdirs()) {
 				throw new IOException("Unable to create scratch directory: " + scratchDir);
 			}
 		}
 		return scratchDir;
 	}

  private static List<ContainerInitializer> jspInitializers() {
 		JettyJasperInitializer sci = new JettyJasperInitializer();
 		ContainerInitializer initializer = new ContainerInitializer(sci, null);
 		List<ContainerInitializer> initializers = new ArrayList<ContainerInitializer>();
 		initializers.add(initializer);
 		return initializers;
 	}

  private void startServer(CommandLine params) throws Exception {
    // add Guice filter for all dynamic REST handling
    PortalServletContextListener listener = new PortalServletContextListener();
    FilterHolder guiceFilter = new FilterHolder(listener.getInjector().
      getInstance(GuiceFilter.class));
    configuration = listener.getInjector().getInstance(PortalConfiguration.class);
    // load extra configuration
    if (params.hasOption("c")) {
      configuration.load(params.getOptionValue("c"));
    }
    if (params.hasOption("p")) {
      String val = params.getOptionValue("p");
      webPort = Integer.parseInt(val);
      configuration.set("web.port", val);
    }
    configuration.set("input", params.getOptionValue("i"));
    LOG.info("Set input to " + configuration.get("input"));
    // set up context for webapp
    WebAppContext context = new WebAppContext();
    context.setResourceBase("./src/main/webapp");
    context.setContextPath("/");

    context.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
    context.setAttribute("javax.servlet.context.tempdir", getScratchDir());
    context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
    context.addBean(new ServletContainerInitializersStarter(context), true);
    context.setClassLoader(new URLClassLoader(new URL[0], this.getClass().getClassLoader()));

    context.setParentLoaderPriority(true);
    context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
    context.setInitParameter("keepgenerated", "true");
    context.setWelcomeFiles(new String[] { "index.jsp", "index.html" });
    // more Guice related wiring for the context
    context.addFilter(guiceFilter, "/rest/*", EnumSet.allOf(DispatcherType.class));
    context.addEventListener(listener);
    // tie all together and start up engine
    Server server = new Server(configuration.getInt("web.port", webPort));
    server.setHandler(context);
    server.start();
  }

  public static void main(String[] args) throws Exception {
    HttpServerMain server = new HttpServerMain();
    CommandLine cli = server.parseArgs(args);
    server.startServer(cli);
  }
}
