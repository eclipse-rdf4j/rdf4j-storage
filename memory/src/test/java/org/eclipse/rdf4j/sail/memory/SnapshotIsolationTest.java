/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.memory;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class SnapshotIsolationTest {

	@Test
	public void snapshot() throws InterruptedException {
		MemoryStore repo = new MemoryStore();
		repo.init();

		multithreaded(IsolationLevels.SNAPSHOT, repo);
		repo.shutDown();


	}

	@Test
	public void serializable() throws InterruptedException {
		MemoryStore repo = new MemoryStore();
		repo.init();

		multithreaded(IsolationLevels.SERIALIZABLE, repo);
		repo.shutDown();


	}


	private void multithreaded(IsolationLevels isolationLevel, Sail repo)
		throws InterruptedException {

		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI iri = vf.createIRI("http://example.com/resouce1");


		try (SailConnection connctionAddRemoveEarly = repo.getConnection()) {
			try (SailConnection connectionAddRemoveLate = repo.getConnection()) {
				try (SailConnection connectionNoWritesAtAll = repo.getConnection()) {


					connctionAddRemoveEarly.begin(isolationLevel);
					connectionAddRemoveLate.begin(isolationLevel);
					connectionNoWritesAtAll.begin(isolationLevel);

					connctionAddRemoveEarly.addStatement(RDF.TYPE, RDF.TYPE, RDFS.RESOURCE);
					connctionAddRemoveEarly.removeStatements(RDF.TYPE, RDF.TYPE, RDFS.RESOURCE);


					Runnable runnable1 = () -> {

						try (SailConnection connection = repo.getConnection()) {
							connection.begin(isolationLevel);

							connection.addStatement(iri, RDF.TYPE, RDFS.RESOURCE);
							connection.addStatement(iri, RDFS.LABEL, vf.createLiteral("a"));
							connection.addStatement(iri, RDFS.LABEL, vf.createLiteral("b"));


							connection.commit();

						}

					};


					Thread thread1 = new Thread(runnable1);
					thread1.start();
					thread1.join();


					connectionAddRemoveLate.addStatement(RDFS.RESOURCE, RDF.TYPE, RDFS.RESOURCE);
					connectionAddRemoveLate.removeStatements(RDFS.RESOURCE, RDF.TYPE, RDFS.RESOURCE);


					assertEquals(0, getCount(connctionAddRemoveEarly));
					assertEquals(0, getCount(connectionAddRemoveLate));
					assertEquals(0, getCount(connectionNoWritesAtAll));

					connctionAddRemoveEarly.commit();
					connectionAddRemoveLate.commit();
					connectionNoWritesAtAll.commit();

				}
			}


		}

	}

	private long getCount(SailConnection connection1) {

		try (Stream<? extends Statement> stream = Iterations.stream(connection1.getStatements(null, null, null, false))) {
			long count = stream.peek(System.out::println).count();
			return count;
		}
	}


}
