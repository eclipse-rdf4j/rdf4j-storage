/*******************************************************************************
 * Copyright (c) 2016 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.AST;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.shacl.plan.Select;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.ShaclSailConnection;

import java.util.stream.Stream;

/**
 * @author Heshan Jayasinghe
 */
public class TargetClass extends Shape implements PlanGenerator {

	Resource targetClass;

	public TargetClass(Resource id, SailRepositoryConnection connection) {
		super(id, connection);

		try (Stream<Statement> stream = Iterations.stream(connection.getStatements(id, SHACL.TARGET_CLASS, null))) {
			targetClass = stream.map(Statement::getObject).map(v -> (Resource) v).findAny().get();
		}

	}

	@Override
	public Select getPlan(ShaclSailConnection shaclSailConnection, Shape shape) {
		Select select = new Select(shaclSailConnection, null, RDF.TYPE, targetClass);
		return select;
	}
}