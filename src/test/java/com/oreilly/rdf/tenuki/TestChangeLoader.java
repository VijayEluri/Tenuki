/*   
 * Copyright 2009 O'Reilly Media, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.oreilly.rdf.tenuki;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.oreilly.rdf.tenuki.Changeset;
import com.oreilly.rdf.tenuki.ChangesetHandler;
import com.oreilly.rdf.tenuki.InputStreamChangeset;



public class TestChangeLoader {
	public static final String TESTING_RESOURCE_URI = "http://example.com/res#thing";
	public static final String BEFORE_TITLE = "Original Title";
	public static final String AFTER_TITLE = "New Title";

	private class MockChangeSet implements Changeset {


		@Override
		public Statement[] toAdd() {
			Model model = ModelFactory.createDefaultModel();
			Resource testingResource = model.createResource(TESTING_RESOURCE_URI);
			testingResource.addProperty(DC.title, AFTER_TITLE);
			return allProperties(testingResource);
		}

		@Override
		public Statement[] toRemove() {

			Model model = ModelFactory.createDefaultModel();
			Resource testingResource = model.createResource(TESTING_RESOURCE_URI);
			testingResource.addProperty(DC.title, BEFORE_TITLE);
			return allProperties(testingResource);
		}

		private Statement[] allProperties(Resource testingResource) {
			ArrayList<Statement> statements = new ArrayList<Statement>();
			StmtIterator iter = testingResource.listProperties();
			while (iter.hasNext()) {
				statements.add(iter.nextStatement());
			}
			return  statements.toArray(new Statement[1]);
		}

		@Override
		public Resource getSubjectOfChange() {
			return null;
		}

	}

	private Model model;
	private DBConnection conn;

	@Before
	public void setUp() throws Exception {
		String className = "org.hsqldb.jdbcDriver";       // path of driver class
		Class.forName (className);                        // Load the Driver
		String DB_URL =    "jdbc:hsqldb:mem:testing";   // URL of database 
		String DB_USER =   "sa";                          // database user id
		String DB_PASSWD = "";                            // database password
		String DB =        "HSQL";                        // database type
		// Create database connection
		conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
		ModelMaker maker = ModelFactory.createModelRDBMaker(conn) ;
		model = maker.createDefaultModel();
		Resource testingResource = model.createResource(TESTING_RESOURCE_URI);
		testingResource.addProperty(DC.title, BEFORE_TITLE);
		model.commit();

	}

	@After
	public void tearDown() throws Exception {
		this.model.close();
		this.conn.cleanDB();
	}

	@Test
	public void testApplyChangeset() {
		ChangesetHandler handler = new ChangesetHandler(model);
		Changeset changeset = new MockChangeSet();
		handler.applyChangeset(changeset);
		Resource testingResource = model.createResource(TESTING_RESOURCE_URI);
		Statement result = testingResource.getProperty(DC.title);
		Literal title = result.getLiteral();
		Assert.assertEquals(AFTER_TITLE, title.getLexicalForm());
	}
	
	@Test
	public void testApplyChangesetFile() throws Exception {
		ClassPathResource changesetResource = new ClassPathResource("changeset.xml");
		Changeset changeset = new InputStreamChangeset(changesetResource
				.getInputStream());
		
		ChangesetHandler handler = new ChangesetHandler(model);
		handler.applyChangeset(changeset);
		Resource testingResource = model.createResource(TESTING_RESOURCE_URI);
		Statement result = testingResource.getProperty(DC.title);
		Literal title = result.getLiteral();
		Assert.assertEquals(AFTER_TITLE, title.getLexicalForm());

	}
}
