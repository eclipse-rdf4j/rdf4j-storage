
/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.memory_readonly.MemoryStoreReadonly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Håvard Mikkelsen Ottestad
 */
public class Temp {

	public static void main(String[] args) throws IOException, InterruptedException {
		SimpleValueFactory vf = SimpleValueFactory.getInstance();
		ArrayList<Statement> statements1 = new ArrayList<>(
				Arrays.asList(vf.createStatement(RDFS.RESOURCE, RDF.TYPE, RDFS.RESOURCE)));
		SailRepository sail = new SailRepository(new MemoryStoreReadonly(statements1, new ArrayList<>()));
		sail.initialize();

		try (SailRepositoryConnection connection = sail.getConnection()) {
			try (RepositoryResult<Statement> statements = connection.getStatements(null, null, null)) {
				while (statements.hasNext()) {
					System.out.println(statements.next());
				}
			}

			System.out.println(connection.hasStatement(RDFS.RESOURCE, RDF.TYPE, RDFS.RESOURCE, false));
			System.out.println(connection.hasStatement(null, RDF.TYPE, RDFS.RESOURCE, false));
			System.out.println(connection.hasStatement(null, RDF.TYPE, null, false));
			System.out.println(connection.hasStatement(null, null, null, false));
			System.out.println(connection.hasStatement(null, null, RDFS.RESOURCE, false));
			System.out.println(connection.hasStatement(RDFS.RESOURCE, RDF.TYPE, null, false));
			System.out.println(connection.hasStatement(RDFS.RESOURCE, null, null, false));

		}

	}

}
