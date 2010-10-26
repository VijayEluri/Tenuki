package com.oreilly.rdf.tenuki;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.hp.hpl.jena.sdb.StoreDesc;
import com.oreilly.rdf.tenuki.jaxrs.TenukiApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Tenuki {

	/**
	 * @param args
	 * @throws Exception 
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		CommandLineParser parser = new PosixParser();

		Options options = new Options();
		options.addOption("h", "help", false, "show this message");
		options
				.addOption(OptionBuilder.withLongOpt("port").withDescription(
						"use PORT for server").hasArg().withArgName("PORT")
						.create("p"));
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("tenuki-server", options);
				return;
			}

			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName("org.postgresql.Driver");
			dataSource.setUrl("jdbc:postgresql:sdb");
			dataSource.setUsername("sdb");
			dataSource.setPassword("sdb");

			StoreDesc storeDesc = new StoreDesc("layout2/index", "postgresql");

			Integer port = Integer
					.parseInt(line.getOptionValue("port", "7070"));
			
			ServletContainer jaxrsContainer = new ServletContainer(new TenukiApplication());

			Server server = new Server(port);

			WebAppContext webAppContext = new WebAppContext();
			webAppContext
					.setConfigurationClasses(new String[] { 
							"org.eclipse.jetty.plus.webapp.EnvConfiguration",
							"org.eclipse.jetty.plus.webapp.PlusConfiguration",
							"org.eclipse.jetty.annotations.AnnotationConfiguration"
							});
			
			webAppContext.setContextPath("/");
			webAppContext.addServlet(new ServletHolder(jaxrsContainer), "/*");
									
			new Resource("jdbc/sdbDataSource", dataSource);
			new Resource("jdbc/sdbStoreDesc", storeDesc);
						
			server.setHandler(webAppContext);
			server.start();
			server.join();

		} catch (ParseException e) {
			System.out.println("Unexpected exception:" + e.getMessage());
		}

	}
}