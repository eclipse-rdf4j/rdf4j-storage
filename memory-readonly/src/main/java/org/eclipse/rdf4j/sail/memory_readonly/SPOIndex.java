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
import java.util.Objects;

/**
 * @author Håvard Mikkelsen Ottestad
 */
class SPOIndex {

	private static final ListIterable EMPTY_ITERABLE = new ListIterable.EmptyListIterable();


	final Map<SpoCompound, List<Statement>> spoIndex;
	final Map<SpCompound, List<Statement>> spIndex;
	final Map<SCompound, List<Statement>> sIndex;

	final List<Statement> allStatements;

	private static final ValueComparator valueComparator = new ValueComparator();

	SPOIndex(List<Statement> statements) {

		allStatements = statements;

		spoIndex = new HashMap<>(statements.size() / 5, 0.5f);
		spIndex = new HashMap<>(statements.size() / 5, 0.5f);
		sIndex = new HashMap<>(statements.size() / 5, 0.5f);

		for (Statement statement : statements) {
			SCompound sKey = new SCompound(statement.getSubject());
			SpCompound spKey = new SpCompound(statement.getSubject(), statement.getPredicate());
			SpoCompound spoKey = new SpoCompound(statement.getSubject(), statement.getPredicate(), statement.getObject());

			sIndex.compute(sKey, (k, v) -> {
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

			spIndex.compute(spKey, (k, v) -> {
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

			spoIndex.compute(spoKey, (k, v) -> {
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

		if (subject != null) {
			if (predicate != null) {
				if (object != null) {
					List<Statement> arrayIndex = spoIndex.get(new SpoCompound(subject, predicate, object));
					if (arrayIndex == null) {
						return EMPTY_ITERABLE;
					}

					if ((context == null || context.length == 0)) {
						return new ListIterable(arrayIndex, false);
					} else {
						return new ListIterable(arrayIndex, true);

					}

				} else {
					List<Statement> arrayIndex = spIndex.get(new SpCompound(subject, predicate));
					if (arrayIndex == null) {
						return EMPTY_ITERABLE;
					}

					if ((context == null || context.length == 0)) {
						return new ListIterable(arrayIndex, false);
					} else {
						return new ListIterable(arrayIndex, true);

					}
				}

			} else {
				List<Statement> arrayIndex = sIndex.get(new SCompound(subject));
				if (arrayIndex == null) {
					return EMPTY_ITERABLE;
				}

				if ((context == null || context.length == 0)) {
					return new ListIterable(arrayIndex, false);
				} else {
					return new ListIterable(arrayIndex, true);

				}
			}

		} else {
			return new ListIterable(allStatements, true);
		}


	}


	public boolean isEmpty() {
		return sIndex.isEmpty();
	}
}

class SpoCompound {
	private Resource subject;
	private IRI predicate;
	private Value object;

	SpoCompound(Resource subject, IRI predicate, Value object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		SpoCompound that = (SpoCompound) o;
		return subject.equals(that.subject) &&
			predicate.equals(that.predicate) &&
			object.equals(that.object);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subject, predicate, object);
	}

	@Override
	public String toString() {
		return "SpoCompound{" +
			"subject=" + subject +
			", predicate=" + predicate +
			", object=" + object +
			'}';
	}
}

class SpCompound {
	private Resource subject;
	private IRI predicate;

	SpCompound(Resource subject, IRI predicate) {
		this.subject = subject;
		this.predicate = predicate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		SpCompound that = (SpCompound) o;
		return subject.equals(that.subject) &&
			predicate.equals(that.predicate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subject, predicate);
	}

	@Override
	public String toString() {
		return "SpCompound{" +
			"subject=" + subject +
			", predicate=" + predicate +
			'}';
	}
}

class SCompound {
	private Resource subject;

	SCompound(Resource subject) {
		this.subject = subject;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		SCompound sCompound = (SCompound) o;
		return subject.equals(sCompound.subject);
	}

	@Override
	public int hashCode() {
		return subject.hashCode();
	}

	@Override
	public String toString() {
		return "SCompound{" +
			"subject=" + subject +
			'}';
	}
}
