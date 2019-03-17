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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Håvard Mikkelsen Ottestad
 */
class OrderedPSOIndex {

	Statement[] orderedArray;

	Map<PsoCompound, ArrayIndex> psoIndex = new HashMap<>();
	Map<PsCompound, ArrayIndex> psIndex = new HashMap<>();
	Map<PCompound, ArrayIndex> pIndex = new HashMap<>();

	OrderedPSOIndex(Set<Statement> statementSet) {

		orderedArray = statementSet.toArray(new Statement[0]);
		Arrays.sort(orderedArray, (o1, o2) -> {

			String o1String = "<" + o1.getPredicate().toString() + "><" + o1.getSubject().toString() + "><"
				+ o1.getObject().toString() + ">";
			String o2String = "<" + o2.getPredicate().toString() + "><" + o2.getSubject().toString() + "><"
				+ o2.getObject().toString() + ">";

			return o1String.compareTo(o2String);
		});

		for (int i = 0; i < orderedArray.length; i++) {
			Statement statement = orderedArray[i];

			int index = i;

			PCompound sKey = new PCompound(statement.getPredicate());
			PsCompound spKey = new PsCompound(statement.getPredicate(), statement.getSubject());
			PsoCompound spoKey = new PsoCompound(statement.getPredicate(), statement.getSubject(), statement.getObject());

			pIndex.compute(sKey, (key, value) -> {
				if (value == null) {
					return new ArrayIndex(index, index + 1);
				} else {
					value.stopExclusive = index + 1;
					return value;
				}
			});

			psIndex.compute(spKey, (key, value) -> {
				if (value == null) {
					return new ArrayIndex(index, index + 1);
				} else {
					value.stopExclusive = index + 1;
					return value;
				}
			});

			psoIndex.compute(spoKey, (key, value) -> {
				if (value == null) {
					return new ArrayIndex(index, index + 1);
				} else {
					value.stopExclusive = index + 1;
					return value;
				}
			});

		}

	}

	ArrayIndexIterator getStatements(Resource subject, IRI predicate, Value object, Resource... context) {
		if (predicate != null) {
			if (subject != null) {
				if (object != null) {
					ArrayIndex arrayIndex = psoIndex.get(new PsoCompound(predicate, subject, object));
					if (context == null) {
						return new ArrayIndexIterator(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
							false);
					} else {
						return new ArrayIndexIterator(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
							true);
					}

				} else {
					ArrayIndex arrayIndex = psIndex.get(new PsCompound(predicate, subject));
					if (context == null) {
						return new ArrayIndexIterator(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
							false);
					} else {
						return new ArrayIndexIterator(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
							true);
					}
				}

			} else {
				ArrayIndex arrayIndex = pIndex.get(new PCompound(predicate));
				if (object == null && context == null) {
					return new ArrayIndexIterator(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
						false);
				} else {
					return new ArrayIndexIterator(orderedArray, arrayIndex.startInclusive, arrayIndex.stopExclusive,
						true);
				}
			}

		} else {
			return new ArrayIndexIterator(orderedArray, 0, orderedArray.length, true);
		}

	}
}

class PsoCompound {
	private IRI predicate;
	private Resource subject;
	private Value object;

	public PsoCompound(IRI predicate, Resource subject, Value object) {
		this.predicate = predicate;
		this.subject = subject;
		this.object = object;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PsoCompound that = (PsoCompound) o;
		return predicate.equals(that.predicate) &&
			subject.equals(that.subject) &&
			object.equals(that.object);
	}

	@Override
	public int hashCode() {
		return Objects.hash(predicate, subject, object);
	}

	@Override
	public String toString() {
		return "PsoCompound{" +
			"predicate=" + predicate +
			", subject=" + subject +
			", object=" + object +
			'}';
	}
}

class PsCompound {
	private IRI predicate;
	private Resource subject;

	public PsCompound(IRI predicate, Resource subject) {
		this.predicate = predicate;
		this.subject = subject;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PsCompound that = (PsCompound) o;
		return predicate.equals(that.predicate) &&
			subject.equals(that.subject);
	}

	@Override
	public int hashCode() {
		return Objects.hash(predicate, subject);
	}

	@Override
	public String toString() {
		return "PsCompound{" +
			"predicate=" + predicate +
			", subject=" + subject +
			'}';
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
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PCompound pCompound = (PCompound) o;
		return predicate.equals(pCompound.predicate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(predicate);
	}

	@Override
	public String toString() {
		return "PCompound{" +
			"predicate=" + predicate +
			'}';
	}
}


