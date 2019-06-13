/* @formatter:off */
/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.memory_readonly_bplus;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategyFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.UnknownSailTransactionStateException;
import org.eclipse.rdf4j.sail.base.SailSourceConnection;
import org.eclipse.rdf4j.sail.base.SailStore;

/**
 * @author Håvard Mikkelsen Ottestad
 */
public class MemoryStoreReadOnlyBplusConnection extends SailSourceConnection {
	MemoryStoreReadonlyBplus sail;

	protected MemoryStoreReadOnlyBplusConnection(MemoryStoreReadonlyBplus sail, SailStore store, FederatedServiceResolver resolver) {
		super(sail, store, resolver);
		this.sail = sail;
	}

	protected MemoryStoreReadOnlyBplusConnection(MemoryStoreReadonlyBplus sail, SailStore store,
												 EvaluationStrategyFactory evalStratFactory) {
		super(sail, store, evalStratFactory);
		this.sail = sail;
	}

	@Override
	protected void addStatementInternal(Resource subj, IRI pred, Value obj, Resource... contexts) throws SailException {
	}

	@Override
	protected void removeStatementsInternal(Resource subj, IRI pred, Value obj, Resource... contexts)
		throws SailException {
	}

	public MemoryStoreReadonlyBplus getSail() {
		return sail;
	}


	@Override
	public boolean hasStatement(Resource subj, IRI pred, Value obj, boolean includeInferred, Resource... contexts) throws SailException {
		try (CloseableIteration<? extends Statement, SailException> stIter = getStatements(subj, pred, obj, includeInferred, contexts)) {
			return stIter.hasNext();
		}
	}


	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, IRI pred,
																				 Value obj, boolean includeInferred, Resource... contexts) throws SailException {
		return getStatementsInternal(subj, pred, obj, includeInferred, contexts);

	}

	@Override
	public boolean isActive() throws UnknownSailTransactionStateException {
		return true;
	}

	@Override
	protected IsolationLevel getTransactionIsolation() {
		return IsolationLevels.NONE;
	}


}
