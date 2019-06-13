package org.eclipse.rdf4j.sail.memory_readonly_bplus;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

public class PartialStatement implements Statement {

	private final Resource subject;
	private final IRI predicate;
	private final Value object;
	private final Resource context;

	public PartialStatement(Resource subject, IRI predicate, Value object, Resource context) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.context = context;
	}

	@Override
	public Resource getSubject() {
		return subject;
	}

	@Override
	public IRI getPredicate() {
		return predicate;
	}

	@Override
	public Value getObject() {
		return object;
	}

	@Override
	public Resource getContext() {
		return context;
	}
}
