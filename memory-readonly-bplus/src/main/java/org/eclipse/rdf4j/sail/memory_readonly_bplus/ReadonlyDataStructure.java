/* @formatter:off */
/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.memory_readonly_bplus;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.indexes.OPSIndex;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.indexes.PSOIndex;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.indexes.SPOIndex;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Håvard Mikkelsen Ottestad
 */
public class ReadonlyDataStructure extends DataStructureInterface {

	private final ValueFactory vf = SimpleValueFactory.getInstance();

	private final static ListIterable.EmptyListIterable emptyListIterable = new ListIterable.EmptyListIterable();

	private SPOIndex SPOIndex;
	private PSOIndex PSOIndex;
	private OPSIndex OPSIndex;


	private Map<Value, Integer> valueMap = new HashMap<>();

	private ValueComparator valueComparator = new ValueComparator();


	ReadonlyDataStructure(List<Statement> statements) {

		int i = 0;


//		for (Statement s : statements) {
//
//
//			if(!valueMap.containsKey(s.getSubject())){
//				valueMap.put(s.getSubject(), i++);
//			}
//
//			if(!valueMap.containsKey(s.getPredicate())){
//				valueMap.put(s.getPredicate(), i++);
//			}
//
//			if(!valueMap.containsKey(s.getObject())){
//				valueMap.put(s.getObject(), i++);
//			}
//		}
//
//		List<Statement> collect = statements.stream()
//			.map(statement -> {
//				Resource subject = statement.getSubject();
//				IRI predicate = statement.getPredicate();
//				Value object = statement.getObject();
//
//				subject = (Resource) valueMap.get(subject);
//				predicate = (IRI) valueMap.get(predicate);
//				object = valueMap.get(object);
//
//				return vf.createStatement(subject, predicate, object, statement.getContext());
//
//			})
//			.collect(Collectors.toList());
//

		if (statements.isEmpty()) {
			return;
		}

		Stream
			.of(
				(Runnable) () -> SPOIndex = new SPOIndex(statements),
				(Runnable) () -> PSOIndex = new PSOIndex(statements),
				(Runnable) () -> OPSIndex = new OPSIndex(statements)
			)
			.parallel()
			.forEach(Runnable::run);

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
		ListIterable iterable;

		if (SPOIndex.isEmpty()) {
			return new CloseableIterationOverIterator(emptyListIterable.iterator());
		}

		if (subject == null && object != null && predicate != null) {
			iterable = OPSIndex.getStatements(subject, predicate, object, context);
		} else if (subject == null && predicate == null && object != null) {
			iterable = OPSIndex.getStatements(subject, predicate, object, context);
		} else if (subject == null && predicate != null) {
			iterable = PSOIndex.getStatements(subject, predicate, object, context);
		} else {
			iterable = SPOIndex.getStatements(subject, predicate, object, context);
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

	private final Iterator<Statement> iterator;

	CloseableIterationOverIterator(Iterator<Statement> iterator) {
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
