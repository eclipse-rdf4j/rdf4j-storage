/* @formatter:off */
/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.memory_readonly;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategyFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.StrictEvaluationStrategyFactory;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractNotifyingSail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Håvard Mikkelsen Ottestad
 */
public class MemoryStoreReadonly extends AbstractNotifyingSail implements FederatedServiceResolverClient {

	private MemorySailStoreReadonly sailStore;

	public MemoryStoreReadonly(Set<Statement> statements, Set<Statement> statementsInferred) {
		sailStore = new MemorySailStoreReadonly(statements, statementsInferred);

	}

	public MemoryStoreReadonly(Set<Statement> statements) {
		sailStore = new MemorySailStoreReadonly(statements);

	}


	@Override
	public List<IsolationLevel> getSupportedIsolationLevels() {
		return Arrays.asList(IsolationLevels.NONE, IsolationLevels.READ_COMMITTED);
	}

	@Override
	public IsolationLevel getDefaultIsolationLevel() {
		return IsolationLevels.NONE;
	}

	@Override
	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {

	}

	@Override
	protected void shutDownInternal() throws SailException {
		sailStore = null;
	}

	@Override
	protected NotifyingSailConnection getConnectionInternal() throws SailException {
		return new MemoryStoreReadOnlyConnection(this, sailStore, getEvaluationStrategyFactory());
	}

	@Override
	public boolean isWritable() throws SailException {
		return false;
	}

	@Override
	public ValueFactory getValueFactory() {
		return SimpleValueFactory.getInstance();
	}

	private EvaluationStrategyFactory evalStratFactory;

	public synchronized EvaluationStrategyFactory getEvaluationStrategyFactory() {
		if (evalStratFactory == null) {
			evalStratFactory = new StrictEvaluationStrategyFactory(getFederatedServiceResolver());
		}
		evalStratFactory.setQuerySolutionCacheThreshold(0);
		return evalStratFactory;
	}

	/**
	 * independent life cycle
	 */
	private FederatedServiceResolver serviceResolver;

	/**
	 * dependent life cycle
	 */
	private FederatedServiceResolverImpl dependentServiceResolver;

	public synchronized FederatedServiceResolver getFederatedServiceResolver() {
		if (serviceResolver == null) {
			if (dependentServiceResolver == null) {
				dependentServiceResolver = new FederatedServiceResolverImpl();
			}
			setFederatedServiceResolver(dependentServiceResolver);
		}
		return serviceResolver;
	}
}
