/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.memory_readonly;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sail.SailException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Håvard Mikkelsen Ottestad
 */
public class ReadonlyDataStructure extends DataStructureInterface {

	private Set<Statement> statementSet;

	private OrderedSPOIndex orderedSPOIndex;
	private OrderedPSOIndex orderedPSOIndex;

	ReadonlyDataStructure(Set<Statement> statements) {
		statementSet = statements;
		orderedSPOIndex = new OrderedSPOIndex(statements);
		orderedPSOIndex = new OrderedPSOIndex(statements);
	}

	@Override
	public void addStatement(Statement statement) {
		throw new IllegalStateException();
	}

	@Override
	public void removeStatement(Statement statement) {
		throw new IllegalStateException();
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(Resource subject, IRI predicate,
																				Value object, Resource... context) {
		ArrayIndexIterable iterable;

		if (subject == null && predicate != null) {
			iterable = orderedPSOIndex.getStatements(subject, predicate, object, context);

		} else {
			iterable = orderedSPOIndex.getStatements(subject, predicate, object, context);
		}

		CloseableIteration<Statement, SailException> iterator = new CloseableIterationOverIterator(iterable.iterator());

		if (iterable.isNeedsFurtherFiltering()) {
			iterator = new ComparingIterator(iterator, subject, predicate, object, context);
		}

		return iterator;

	}

	@Override
	public void flush() {
		// no-op
	}

}

class CloseableIterationOverIterator implements CloseableIteration<Statement, SailException> {

	Iterator<Statement> iterator;

	public CloseableIterationOverIterator(Iterator<Statement> iterator) {
		this.iterator = iterator;
	}

	@Override
	public void close() throws SailException {
		// no-op
	}

	@Override
	public boolean hasNext() throws SailException {
		return iterator.hasNext();
	}

	@Override
	public Statement next() throws SailException {
		return iterator.next();
	}

	@Override
	public void remove() throws SailException {
		throw new IllegalStateException();
	}
}


