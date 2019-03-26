/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.shacl;

import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class ModifyShapesPostInitTest {

	@Test
	public void testUpdatingShapes() throws IOException {
		ShaclSail shaclSail = new ShaclSail(new MemoryStore());

		SailRepository sailRepository = new SailRepository(shaclSail);
		sailRepository.init();

		try (SailRepositoryConnection connection = sailRepository.getConnection()) {

			connection.begin();

			StringReader shaclRules = new StringReader(String.join("\n", "",
					"@prefix ex: <http://example.com/ns#> .",
					"@prefix sh: <http://www.w3.org/ns/shacl#> .",
					"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
					"@prefix foaf: <http://xmlns.com/foaf/0.1/>.",

					"ex:PersonShape",
					"        a sh:NodeShape  ;",
					"        sh:targetClass ex:Person ;",
					"        sh:property [",
					"                sh:path ex:age ;",
					"                sh:datatype xsd:integer ;",
					"        ] ."));

			connection.add(shaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
			connection.commit();

			add(connection, "ex:pete a ex:Person .");

			StringReader extraShaclRules = new StringReader(String.join("\n", "",
					"@prefix ex: <http://example.com/ns#> .",
					"@prefix sh: <http://www.w3.org/ns/shacl#> .",
					"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
					"@prefix foaf: <http://xmlns.com/foaf/0.1/>.",

					"ex:PersonShape",
					"        a sh:NodeShape  ;",
					"        sh:targetClass ex:Person ;",
					"        sh:property [",
					"                sh:path ex:birthyear ;",
					"                sh:datatype xsd:integer ;",
					"        ] ."));

			connection.add(extraShaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);

		}
	}

	@Test(expected = ShaclSailValidationException.class)
	public void testUpdatingShapesViolation() throws Throwable {
		ShaclSail shaclSail = new ShaclSail(new MemoryStore());

		SailRepository sailRepository = new SailRepository(shaclSail);
		sailRepository.init();

		try (SailRepositoryConnection connection = sailRepository.getConnection()) {

			connection.begin();

			StringReader shaclRules = new StringReader(String.join("\n", "",
					"@prefix ex: <http://example.com/ns#> .",
					"@prefix sh: <http://www.w3.org/ns/shacl#> .",
					"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
					"@prefix foaf: <http://xmlns.com/foaf/0.1/>.",

					"ex:PersonShape",
					"        a sh:NodeShape  ;",
					"        sh:targetClass ex:Person ;",
					"        sh:property [",
					"                sh:path ex:age ;",
					"                sh:datatype xsd:integer ;",
					"        ] ."));

			connection.add(shaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
			connection.commit();

			add(connection, "ex:pete a ex:Person .");

			StringReader extraShaclRules = new StringReader(String.join("\n", "",
					"@prefix ex: <http://example.com/ns#> .",
					"@prefix sh: <http://www.w3.org/ns/shacl#> .",
					"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
					"@prefix foaf: <http://xmlns.com/foaf/0.1/>.",

					"ex:PersonShape",
					"        a sh:NodeShape  ;",
					"        sh:targetClass ex:Person ;",
					"        sh:property [",
					"                sh:path ex:age ;",
					"                sh:minCount 1 ;",
					"        ] ."));

			try {
				connection.add(extraShaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
			}catch (RepositoryException e){
				throw e.getCause();
			}
		}
	}

	@Test(expected = ShaclSailValidationException.class)
	public void testAddingShapesAfterData() throws Throwable {
		ShaclSail shaclSail = new ShaclSail(new MemoryStore());

		SailRepository sailRepository = new SailRepository(shaclSail);
		sailRepository.init();

		try (SailRepositoryConnection connection = sailRepository.getConnection()) {

			add(connection, "ex:pete a ex:Person .");

			connection.begin();

			StringReader shaclRules = new StringReader(String.join("\n", "",
					"@prefix ex: <http://example.com/ns#> .",
					"@prefix sh: <http://www.w3.org/ns/shacl#> .",
					"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
					"@prefix foaf: <http://xmlns.com/foaf/0.1/>.",

					"ex:PersonShape",
					"        a sh:NodeShape  ;",
					"        sh:targetClass ex:Person ;",
					"        sh:property [",
					"                sh:path ex:age ;",
					"                sh:minCount 1 ;",
					"        ] ."));

			connection.add(shaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);

			try {
				connection.commit();
			}catch (RepositoryException e){
				throw e.getCause();
			}

		}
	}

	private void add(SailRepositoryConnection connection, String data) throws IOException {
		data = String.join("\n", "",
				"@prefix ex: <http://example.com/ns#> .",
				"@prefix foaf: <http://xmlns.com/foaf/0.1/>.",
				"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
				data);

		connection.begin();

		StringReader stringReader = new StringReader(data);

		connection.add(stringReader, "", RDFFormat.TURTLE);
		connection.commit();
	}

}