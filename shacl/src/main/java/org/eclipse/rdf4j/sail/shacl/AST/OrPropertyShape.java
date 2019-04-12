/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.shacl.AST;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.shacl.ShaclSailConnection;
import org.eclipse.rdf4j.sail.shacl.SourceConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.planNodes.BufferedPlanNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.BufferedSplitter;
import org.eclipse.rdf4j.sail.shacl.planNodes.EnrichWithShape;
import org.eclipse.rdf4j.sail.shacl.planNodes.EqualsJoin;
import org.eclipse.rdf4j.sail.shacl.planNodes.InnerJoin;
import org.eclipse.rdf4j.sail.shacl.planNodes.IteratorData;
import org.eclipse.rdf4j.sail.shacl.planNodes.LoggingNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNodeProvider;
import org.eclipse.rdf4j.sail.shacl.planNodes.TrimTuple;
import org.eclipse.rdf4j.sail.shacl.planNodes.UnionNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.Unique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Håvard Ottestad
 */
public class OrPropertyShape extends PropertyShape {

	private final List<List<PropertyShape>> or;

	private static final Logger logger = LoggerFactory.getLogger(OrPropertyShape.class);

	OrPropertyShape(Resource id, SailRepositoryConnection connection, NodeShape nodeShape, boolean deactivated,
			Resource or) {
		super(id, nodeShape, deactivated);
		this.or = toList(connection, or).stream()
				.map(v -> PropertyShape.Factory.getPropertyShapesInner(connection, nodeShape, (Resource) v))
				.collect(Collectors.toList());

	}

	@Override
	public PlanNode getPlan(ShaclSailConnection shaclSailConnection, NodeShape nodeShape, boolean printPlans,
			PlanNodeProvider overrideTargetNode) {
		if (deactivated) {
			return null;
		}

		List<List<PlanNode>> initialPlanNodes = or.stream()
				.map(shapes -> shapes.stream()
						.map(shape -> shape.getPlan(shaclSailConnection, nodeShape, false, null))
						.filter(Objects::nonNull)
						.collect(Collectors.toList()))
				.filter(list -> !list.isEmpty())
				.collect(Collectors.toList());

		PlanNodeProvider targetNodesToValidate;
		if (overrideTargetNode == null) {
			targetNodesToValidate = new BufferedSplitter(unionAll(initialPlanNodes.stream()
					.flatMap(Collection::stream)
					.map(p -> new TrimTuple(p, 0, 1)) // we only want the targets
					.collect(Collectors.toList())));

		} else {
			if (shaclSailConnection.sail.isCacheSelectNodes()) {
				targetNodesToValidate = new BufferedSplitter(overrideTargetNode.getPlanNode());
			} else {
				targetNodesToValidate = overrideTargetNode;
			}
		}

		List<List<PlanNode>> plannodes = or
				.stream()
				.map(shapes -> shapes
						.stream()
						.map(shape -> {
							if (shaclSailConnection.stats.isBaseSailEmpty()) {
								return shape.getPlan(shaclSailConnection, nodeShape, false, null);
							}
							return shape.getPlan(shaclSailConnection, nodeShape, false,
									targetNodesToValidate);
						})
						.filter(Objects::nonNull)
						.collect(Collectors.toList()))
				.filter(list -> !list.isEmpty())
				.collect(Collectors.toList());

		List<IteratorData> iteratorDataTypes = plannodes.stream()
				.flatMap(shapes -> shapes.stream().map(PlanNode::getIteratorDataType))
				.distinct()
				.collect(Collectors.toList());

		if (iteratorDataTypes.size() > 1) {
			throw new UnsupportedOperationException(
					"No support for OR shape with mix between aggregate and raw triples");
		}

		IteratorData iteratorData = iteratorDataTypes.get(0);

		if (iteratorData == IteratorData.tripleBased) {

			List<Path> collect = getPaths().stream().distinct().collect(Collectors.toList());
			if (collect.size() > 1) {
				iteratorData = IteratorData.aggregated;
			}
			if (collect.stream().anyMatch(Objects::isNull)) {
				iteratorData = IteratorData.aggregated;
			}
		}

		PlanNode ret;

		if (iteratorData == IteratorData.tripleBased) {

			PlanNode equalsJoin = new LoggingNode(
					new EqualsJoin(unionAll(plannodes.get(0)), unionAll(plannodes.get(1)), true), "");

			for (int i = 2; i < plannodes.size(); i++) {
				equalsJoin = new LoggingNode(new EqualsJoin(equalsJoin, unionAll(plannodes.get(i)), true), "");
			}

			ret = new LoggingNode(equalsJoin, "");
		} else if (iteratorData == IteratorData.aggregated) {

			PlanNode innerJoin = new LoggingNode(new InnerJoin(unionAll(plannodes.get(0)), unionAll(plannodes.get(1)))
					.getJoined(BufferedPlanNode.class), "");

			for (int i = 2; i < plannodes.size(); i++) {
				innerJoin = new LoggingNode(
						new InnerJoin(innerJoin, unionAll(plannodes.get(i))).getJoined(BufferedPlanNode.class), "");
			}

			ret = new LoggingNode(innerJoin, "");
		} else {
			throw new IllegalStateException("Should not get here!");
		}

		if (printPlans) {
			String planAsGraphiz = getPlanAsGraphvizDot(ret, shaclSailConnection);
			logger.info(planAsGraphiz);
		}

		return new EnrichWithShape(ret, this);

	}

	@Override
	public List<Path> getPaths() {
		return or.stream().flatMap(a -> a.stream().flatMap(b -> b.getPaths().stream())).collect(Collectors.toList());
	}

	private PlanNode unionAll(List<PlanNode> planNodes) {
		return new Unique(new UnionNode(planNodes.toArray(new PlanNode[0])));
	}

	@Override
	public boolean requiresEvaluation(SailConnection addedStatements, SailConnection removedStatements) {
		if (deactivated) {
			return false;
		}

		return super.requiresEvaluation(addedStatements, removedStatements) || or.stream()
				.flatMap(Collection::stream)
				.map(p -> p.requiresEvaluation(addedStatements, removedStatements))
				.reduce((a, b) -> a || b)
				.orElse(false);
	}

	@Override
	public SourceConstraintComponent getSourceConstraintComponent() {
		return SourceConstraintComponent.OrConstraintComponent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		OrPropertyShape that = (OrPropertyShape) o;
		return or.equals(that.or);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), or);
	}

	@Override
	public String toString() {
		return "OrPropertyShape{" +
				"or=" + toString(or) +
				'}';
	}
}
