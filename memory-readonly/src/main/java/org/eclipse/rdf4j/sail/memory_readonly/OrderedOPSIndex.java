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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Håvard Mikkelsen Ottestad
 */
class OrderedOPSIndex {

	private static final ArrayIndexIterable.EmptyArrayIndexIterable EMPTY_ARRAY_INDEX_ITERABLE = new ArrayIndexIterable.EmptyArrayIndexIterable();
	Statement[] orderedArray;

	Map<OpsCompound, ArrayIndex> opsIndex;
	Map<OpCompound, ArrayIndex> opIndex;
	Map<OCompound, ArrayIndex> oIndex;

	static final ValueComparator valueComparator = new ValueComparator();


	OrderedOPSIndex(Set<Statement> statementSet) {
		this(statementSet
			.stream()
			.sorted(getStatementComparator())
			.toArray(Statement[]::new), true);
	}



	OrderedOPSIndex(Statement[] sortableStatements, boolean sorted) {

		opsIndex = new HashMap<>(0);
		opIndex = new HashMap<>(sortableStatements.length/5, 0.5f);
		oIndex = new HashMap<>(sortableStatements.length/5, 0.5f);


		if(!sorted){
			sortableStatements = Arrays
				.stream(sortableStatements)
				.sorted(getStatementComparator())
				.toArray(Statement[]::new);
		}

		orderedArray = sortableStatements;

		for (int i = 0; i < orderedArray.length; i++) {
			Statement statement = orderedArray[i];

			int index = i;

			OCompound oKey = new OCompound(statement.getObject());
			OpCompound opKey = new OpCompound(statement.getObject(), statement.getPredicate());
			OpsCompound opsKey = new OpsCompound(statement.getObject(), statement.getPredicate(), statement.getSubject());

			oIndex.compute(oKey, (key, value) -> {
				if (value == null) {
					return new ArrayIndex(index, index + 1);
				} else {
					value.stopExclusive = index + 1;
					return value;
				}
			});

			opIndex.compute(opKey, (key, value) -> {
				if (value == null) {
					return new ArrayIndex(index, index + 1);
				} else {
					value.stopExclusive = index + 1;
					return value;
				}
			});

//			opsIndex.compute(opsKey, (key, value) -> {
//				if (value == null) {
//					return new ArrayIndex(index, index + 1);
//				} else {
//					value.stopExclusive = index + 1;
//					return value;
//				}
//			});

		}

	}

	ArrayIndexIterable getStatements(Resource subject, IRI predicate, Value object, Resource... context) {
		if (object != null) {
			if (predicate != null) {
				if (subject != null) {
					ArrayIndex arrayIndex = opsIndex.get(new OpsCompound(object, predicate, subject));
					if (arrayIndex == null) {
						return EMPTY_ARRAY_INDEX_ITERABLE;
					}
					if ((context == null || context.length == 0)) {
						return new ArrayIndexIterable(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
							false);
					} else {
						return new ArrayIndexIterable(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
							true);
					}

				} else {
					ArrayIndex arrayIndex = opIndex.get(new OpCompound(object, predicate));
					if (arrayIndex == null) {
						return EMPTY_ARRAY_INDEX_ITERABLE;
					}

					if ((context == null || context.length == 0)) {
						return new ArrayIndexIterable(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
							false);
					} else {
						return new ArrayIndexIterable(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
							true);
					}
				}

			} else {
				ArrayIndex arrayIndex = oIndex.get(new OCompound(object));
				if (arrayIndex == null) {
					return EMPTY_ARRAY_INDEX_ITERABLE;
				}

				if (subject == null && (context == null || context.length == 0)) {
					return new ArrayIndexIterable(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
						false);
				} else {
					return new ArrayIndexIterable(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
						true);
				}
			}

		} else {
			return new ArrayIndexIterable(orderedArray, 0, orderedArray.length, true);
		}

	}

	private static Comparator<Statement> getStatementComparator() {
		return (a, b) -> {
			int compare = valueComparator.compare(a.getObject(), b.getObject());
			if (compare != 0) return compare;

			compare = valueComparator.compare(a.getPredicate(), b.getPredicate());
			if (compare != 0) return compare;


			compare = valueComparator.compare(a.getSubject(), b.getSubject());
			return compare;
		};
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
		return Objects.hash(object, predicate, subject);
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
		return Objects.hash(object, predicate);
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


