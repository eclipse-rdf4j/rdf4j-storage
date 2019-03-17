/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.memory_readonly;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.base.SailSource;
import org.eclipse.rdf4j.sail.base.SailStore;

import java.util.HashSet;

/**
 * @author Håvard Mikkelsen Ottestad
 */
public class MemorySailStoreReadonly implements SailStore {

	private MemNamespaceStore mns = new MemNamespaceStore();

	private MemorySailSourceReadonly sailSource;
	private MemorySailSourceReadonly sailSourceInferred;

	MemorySailStoreReadonly(HashSet<Statement> statements, HashSet<Statement> inferredStatements) {
		sailSource = new MemorySailSourceReadonly(new ReadonlyDataStructure(statements), mns);
		sailSourceInferred = new MemorySailSourceReadonly(new ReadonlyDataStructure(inferredStatements), mns);
	}

	@Override
	public void close() throws SailException {

	}

	@Override
	public ValueFactory getValueFactory() {
		return SimpleValueFactory.getInstance();
	}

	@Override
	public EvaluationStatistics getEvaluationStatistics() {
		return new EvaluationStatistics() {
		};
	}

	@Override
	public SailSource getExplicitSailSource() {
		return sailSource;
	}

	@Override
	public SailSource getInferredSailSource() {
		return sailSourceInferred;
	}
}
