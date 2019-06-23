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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Håvard Mikkelsen Ottestad
 */
class OPSIndex {

	private static final ListIterable.EmptyListIterable EMPTY_ARRAY_INDEX_ITERABLE = new ListIterable.EmptyListIterable();
	List<Statement> allStatements;

	Map<OpCompound, List<Statement>> opIndex;
	Map<OCompound, List<Statement>> oIndex;


	OPSIndex(List<Statement> statements) {

		opIndex = new HashMap<>(statements.size() / 5, 0.5f);
		oIndex = new HashMap<>(statements.size() / 5, 0.5f);


		allStatements = statements;

		for (Statement statement : statements) {

			OCompound oKey = new OCompound(statement.getObject());
			OpCompound opKey = new OpCompound(statement.getObject(), statement.getPredicate());

			oIndex.compute(oKey, (k, v) -> {
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

			opIndex.compute(opKey, (k, v) -> {
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
		if (object != null) {
			if (predicate != null) {
				if (subject != null) {
					throw new IllegalStateException();

				} else {
					List<Statement> statements = opIndex.get(new OpCompound(object, predicate));
					if (statements == null) {
						return EMPTY_ARRAY_INDEX_ITERABLE;
					}

					if ((context == null || context.length == 0)) {
						return new ListIterable(statements, false);
					} else {
						return new ListIterable(statements, true);
					}
				}

			} else {
				List<Statement> statements = oIndex.get(new OCompound(object));
				if (statements == null) {
					return EMPTY_ARRAY_INDEX_ITERABLE;
				}

				if (subject == null && (context == null || context.length == 0)) {
					return new ListIterable(statements, false);

				} else {
					return new ListIterable(statements, true);

				}
			}

		} else {
			return new ListIterable(allStatements, true);
		}

	}


}

class OpsCompound {
	private Value object;
	private IRI predicate;
	private Resource subject;

	public OpsCompound(Value object, IRI predicate, Resource subject) {
		this.object = object;
		this.predicate = predicate;
		this.subject = subject;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		OpsCompound that = (OpsCompound) o;
		return object.equals(that.object) &&
			predicate.equals(that.predicate) &&
			subject.equals(that.subject);
	}

	@Override
	public int hashCode() {
		return Objects.hash((object), (predicate), (subject));
	}

	@Override
	public String toString() {
		return "OpsCompound{" +
			"object=" + object +
			", predicate=" + predicate +
			", subject=" + subject +
			'}';
	}
}

class OpCompound {
	private Value object;
	private IRI predicate;

	public OpCompound(Value object, IRI predicate) {
		this.object = object;
		this.predicate = predicate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		OpCompound that = (OpCompound) o;
		return object.equals(that.object) &&
			predicate.equals(that.predicate);
	}

	@Override
	public int hashCode() {
		return Objects.hash((object), (predicate));
	}

	@Override
	public String toString() {
		return "OpCompound{" +
			"object=" + object +
			", predicate=" + predicate +
			'}';
	}
}

class OCompound {
	private Value object;

	public OCompound(Value object) {
		this.object = object;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		OCompound oCompound = (OCompound) o;
		return object.equals(oCompound.object);
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	@Override
	public String toString() {
		return "OCompound{" +
			"object=" + object +
			'}';
	}


}
