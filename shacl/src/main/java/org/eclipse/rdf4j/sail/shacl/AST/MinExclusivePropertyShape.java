/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.shacl.AST;


import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.ShaclSailConnection;
import org.eclipse.rdf4j.sail.shacl.SourceConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.planNodes.BigDecimalComparatorFilter;
import org.eclipse.rdf4j.sail.shacl.planNodes.EnrichWithShape;
import org.eclipse.rdf4j.sail.shacl.planNodes.LoggingNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.MinExclusiveFilter;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.stream.Stream;

/**
 * @author Håvard Ottestad
 */
public class MinExclusivePropertyShape extends PathPropertyShape {

	private final BigDecimal minExclusive;
	private static final Logger logger = LoggerFactory.getLogger(MinExclusivePropertyShape.class);

	MinExclusivePropertyShape(Resource id, SailRepositoryConnection connection, NodeShape nodeShape) {
		super(id, connection, nodeShape);

		try (Stream<Statement> stream = Iterations.stream(connection.getStatements(id, SHACL.MIN_EXCLUSIVE, null, true))) {
			minExclusive = stream.map(Statement::getObject).map(v -> ((SimpleLiteral) v).decimalValue()).findAny().orElseThrow(() -> new RuntimeException("Expected to find sh:minExclusive on " + id));
		}

	}


	@Override
	public PlanNode getPlan(ShaclSailConnection shaclSailConnection, NodeShape nodeShape, boolean printPlans, boolean assumeBaseSailValid) {

		PlanNode invalidValues =  StandardisedPlanHelper.getGenericSingleObjectPlan(
			shaclSailConnection,
			nodeShape,
			(parent, trueNode, falseNode) -> new BigDecimalComparatorFilter(parent, trueNode, falseNode, value -> minExclusive.compareTo(value) < 0),
			this
		);

		if (printPlans) {
			String planAsGraphvizDot = getPlanAsGraphvizDot(invalidValues, shaclSailConnection);
			logger.info(planAsGraphvizDot);
		}

		return new EnrichWithShape(new LoggingNode(invalidValues), this);

	}

	@Override
	public boolean requiresEvaluation(Repository addedStatements, Repository removedStatements) {
		return true;
	}

	@Override
	public SourceConstraintComponent getSourceConstraintComponent() {
		return SourceConstraintComponent.MinExclusiveConstraintComponent;
	}
}