package com.oreilly.rdf.changes.restlet;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.Model;
import com.oreilly.rdf.changes.MultiModelChangesetHandler;

public class WholeModelRepresentation extends OutputRepresentation {
	private Log log = LogFactory.getLog(WholeModelRepresentation.class);
	private Model model;

	public WholeModelRepresentation(Model model) {
		super(MediaType.APPLICATION_RDF_XML);
		this.model = model;
	}

	@Override
	public void write(OutputStream output) throws IOException {
		try {
			model.write(output);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		} finally {
			try{
				model.close();
				ModelRDB model = (ModelRDB) this.model;
				model.getConnection().close();
				log.info("Closed database connection");
			} catch (ClassCastException ce) {
				throw new RuntimeException("OMG NO", ce);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}

		}

	}

}
