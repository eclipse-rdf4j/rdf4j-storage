/* @formatter:off */
/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.memory_readonly;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Håvard Mikkelsen Ottestad
 */
class PSOIndex {

	private static final ListIterable EMPTY_ARRAY_INDEX_ITERABLE = new ListIterable.EmptyListIterable();
	private final List<Statement> allStatements;

	private final Map<PCompound, List<Statement>> pIndex;

	private static final ValueComparator valueComparator = new ValueComparator();


	PSOIndex(List<Statement> statements) {


		pIndex = new HashMap<>(statements.size() / 5, 0.5f);


		allStatements = statements;

		for (Statement statement : statements) {
			PCompound pKey = new PCompound(statement.getPredicate());

			pIndex.compute(pKey, (k, v) -> {
				List<Statement> list = v;
				if (list == null) {
					list = Collections.singletonList(statement);
				} else {
					if (!(list instanceof ArrayList)) {
						list = new ArrayList<>(list);
					}
					list.add(statement);

				}

				return list;

			});


		}
	}

	ListIterable getStatements(Resource subject, IRI predicate, Value object, Resource... context) {
		if (predicate != null) {
			List<Statement> statements = pIndex.get(new PCompound(predicate));

			if (statements == null) {
				return EMPTY_ARRAY_INDEX_ITERABLE;
			}

			if (subject != null || object != null || context.length > 0) {
				return new ListIterable(statements, true);
			}


			return new ListIterable(statements, false);


		} else {
			return new ListIterable(allStatements, true);


		}

	}

}

class PCompound {
	private IRI predicate;

	PCompound(IRI predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		PCompound pCompound = (PCompound) o;
		return predicate.equals(pCompound.predicate);
	}

	@Override
	public int hashCode() {
		return predicate.hashCode();
	}

	@Override
	public String toString() {
		return "PCompound{" +
			"predicate=" + predicate +
			'}';
	}
}
