package com.oreilly.rdf.changes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class InputStreamChangeset implements Changeset {

	private Model model;

	public InputStreamChangeset(InputStream inputStream) {
		model = ModelFactory.createDefaultModel();
		model.read(inputStream, "");
	}

	@Override
	public Statement[] toAdd() {
		Property addition = model.createProperty(CHANGESET_NS,"addition");
		return reifiedStatements(addition);
	}
	@Override
	public Statement[] toRemove() {
		Property removal = model.createProperty(CHANGESET_NS,"removal");
		return reifiedStatements(removal);
	}


	private Statement[] reifiedStatements(Property property) {
		ArrayList<Statement> statementsToAdd = new ArrayList<Statement>();
		ResIterator iter = model.listSubjectsWithProperty(property);
		while (iter.hasNext()) {
			Resource changeset = iter.nextResource();
			StmtIterator additionIter = changeset.listProperties(property);	
			while (additionIter.hasNext()) {
				Statement statement = (Statement) additionIter.next();
				Resource r = (Resource) statement.getObject();
				Resource subject = r.getProperty(RDF.subject).getResource();
				Resource predicate = r.getProperty(RDF.predicate).getResource();
				RDFNode object = r.getProperty(RDF.object).getObject();
				Statement newStatement = model.createStatement(subject,
						model.createProperty(predicate.getURI()),
						object);
				statementsToAdd.add(newStatement);
			}
		}
		return statementsToAdd.toArray(new Statement[statementsToAdd.size()]);
	}


}