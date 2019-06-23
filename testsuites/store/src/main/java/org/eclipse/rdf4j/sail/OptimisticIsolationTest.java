/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryFactory;
import org.eclipse.rdf4j.sail.optimistic.DeadLockTest;
import org.eclipse.rdf4j.sail.optimistic.DeleteInsertTest;
import org.eclipse.rdf4j.sail.optimistic.LinearTest;
import org.eclipse.rdf4j.sail.optimistic.ModificationTest;
import org.eclipse.rdf4j.sail.optimistic.MonotonicTest;
import org.eclipse.rdf4j.sail.optimistic.RemoveIsolationTest;
import org.eclipse.rdf4j.sail.optimistic.SailIsolationLevelTest;
import org.eclipse.rdf4j.sail.optimistic.SerializableTest;
import org.eclipse.rdf4j.sail.optimistic.SnapshotTest;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author James Leigh
 */
@RunWith(Suite.class)
@SuiteClasses({ DeadLockTest.class, DeleteInsertTest.class, LinearTest.class, ModificationTest.class,
		RemoveIsolationTest.class, SailIsolationLevelTest.class, MonotonicTest.class, SnapshotTest.class,
		SerializableTest.class })
public abstract class OptimisticIsolationTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		System.setProperty("org.eclipse.rdf4j.repository.debug", "true");
	}

	private static RepositoryFactory factory;

	private static File dataDir;

	public static void setRepositoryFactory(RepositoryFactory factory) throws IOException {
		if (dataDir != null && dataDir.isDirectory()) {
			FileUtil.deleteDir(dataDir);
			dataDir = null;
		}
		OptimisticIsolationTest.factory = factory;
	}

	public static Repository getEmptyInitializedRepository(Class<?> caller) throws RDF4JException, IOException {
		if (dataDir != null && dataDir.isDirectory()) {
			FileUtil.deleteDir(dataDir);
			dataDir = null;
		}
		dataDir = FileUtil.createTempDir(caller.getSimpleName());
		Repository repository = factory.getRepository(factory.getConfig());
		repository.setDataDir(dataDir);
		repository.initialize();
		RepositoryConnection con = repository.getConnection();
		try {
			con.clear();
			con.clearNamespaces();
		} finally {
			con.close();
		}
		return repository;
	}
}
