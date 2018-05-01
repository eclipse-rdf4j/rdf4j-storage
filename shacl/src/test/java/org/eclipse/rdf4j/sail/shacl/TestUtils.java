/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Håvard Ottestad
 */
public class TestUtils {

	public static SailRepository getShaclRepository(String shapesFile) {
		SailRepository shaclRepository = new SailRepository(new ShaclSail(new MemoryStore()));
		shaclRepository.initialize();
		try (RepositoryConnection connection = shaclRepository.getConnection()) {
			connection.add(TestUtils.class.getClassLoader().getResourceAsStream(shapesFile), "", RDFFormat.TURTLE,
					ShaclSail.SHACL_GRAPH);
		}
		catch (IOException | NullPointerException e) {
			System.out.println("Error reading: " + shapesFile);
			throw new RuntimeException(e);
		}
		return shaclRepository;
	}

	static class Ex {

		public final static String ns = "http://example.com/ns#";

		public final static IRI Person = createIri("Person");

		public final static IRI ssn = createIri("ssn");

		public final static IRI name = createIri("name");

		public static IRI createIri(String name) {
			return SimpleValueFactory.getInstance().createIRI(ns + name);
		}

		public static IRI createIri() {
			return SimpleValueFactory.getInstance().createIRI(ns + UUID.randomUUID().toString());
		}
	}
}
