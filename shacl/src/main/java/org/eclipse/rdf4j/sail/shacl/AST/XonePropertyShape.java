/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.shacl.AST;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.shacl.ConnectionsGroup;
import org.eclipse.rdf4j.sail.shacl.SourceConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.planNodes.BufferedPlanNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.BufferedSplitter;
import org.eclipse.rdf4j.sail.shacl.planNodes.EnrichWithShape;
import org.eclipse.rdf4j.sail.shacl.planNodes.ExactCountFilter;
import org.eclipse.rdf4j.sail.shacl.planNodes.GroupByCount;
import org.eclipse.rdf4j.sail.shacl.planNodes.LeftOuterJoin;
import org.eclipse.rdf4j.sail.shacl.planNodes.MaxCountFilter;
import org.eclipse.rdf4j.sail.shacl.planNodes.MinCountFilter;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNodeProvider;
import org.eclipse.rdf4j.sail.shacl.planNodes.TrimTuple;
import org.eclipse.rdf4j.sail.shacl.planNodes.UnBufferedPlanNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.UnionNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.Unique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.Buffer;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Håvard Ottestad
 */
public class XonePropertyShape extends PathPropertyShape {

	private final List<List<PathPropertyShape>> xone;

	private static final Logger logger = LoggerFactory.getLogger(XonePropertyShape.class);

	XonePropertyShape(Resource id, SailRepositoryConnection connection, NodeShape nodeShape, boolean deactivated,
					  PathPropertyShape parent, Resource path, Resource or) {
		super(id, connection, nodeShape, deactivated, parent, path);
		this.xone = toList(connection, or).stream()
			.map(v -> Factory.getPropertyShapesInner(connection, nodeShape, (Resource) v, this))
			.collect(Collectors.toList());

	}

	XonePropertyShape(Resource id, SailRepositoryConnection connection, NodeShape nodeShape, boolean deactivated,
					  PathPropertyShape parent, Resource path,
					  List<List<PathPropertyShape>> xone) {
		super(id, connection, nodeShape, deactivated, parent, path);
		this.xone = xone;

	}

	public XonePropertyShape(Resource id, NodeShape nodeShape, boolean deactivated, PathPropertyShape parent, Path path,
							 List<List<PathPropertyShape>> xone) {
		super(id, nodeShape, deactivated, parent, path);
		this.xone = xone;
	}

	@Override
	public PlanNode getPlan(ConnectionsGroup connectionsGroup, boolean printPlans,
							PlanNodeProvider overrideTargetNode, boolean negateThisPlan, boolean negateSubPlans) {

		if (deactivated) {
			return null;
		}

		if (this.getPath() != null) {

			List<PlanNode> collect = xone.stream()
				.map(l -> l.stream().map(p -> p.getPlan(connectionsGroup, false, overrideTargetNode, negateThisPlan, negateSubPlans)).collect(Collectors.toList()))
				.map(XonePropertyShape::unionAll)
				.collect(Collectors.toList());

			List<PlanNode> collect2 = xone.stream()
				.map(l -> l.stream().map(p -> p.getPlan(connectionsGroup, false, overrideTargetNode, true, negateSubPlans)).collect(Collectors.toList()))
				.map(XonePropertyShape::unionAll)
				.collect(Collectors.toList());

			PlanNode planNode = unionAll(collect);
			PlanNode planNode2 = unionAll(collect2);

			Unique uniqueTargets = new Unique(new UnionNode(new TrimTuple(planNode, 0, 1), new TrimTuple(planNode2, 0, 1)));

			BufferedSplitter bufferedSplitter = new BufferedSplitter(uniqueTargets);

			List<PlanNode> collect1 = xone.stream()
				.map(l -> l.stream().map(p -> p.getPlan(connectionsGroup, false, () -> bufferedSplitter.getPlanNode(), true, negateSubPlans)).collect(Collectors.toList()))
				.map(XonePropertyShape::unionAll)
				.map(Unique::new)
				.collect(Collectors.toList());

			PlanNode planNode1 = unionAll(collect1);

			PlanNode groupByCount = new GroupByCount(planNode1, 2);

			PlanNode falseNode = new ExactCountFilter(groupByCount, 1, 2).getFalseNode(UnBufferedPlanNode.class);

			PlanNode trimTuple = new TrimTuple(falseNode, 0, 2);

			return new EnrichWithShape(trimTuple, this);



		} else {


			List<PlanNode> collect = xone.stream()
				.map(l -> l.stream().map(p -> p.getPlan(connectionsGroup, false, overrideTargetNode, negateThisPlan, negateSubPlans)).collect(Collectors.toList()))
				.map(XonePropertyShape::unionAll)
				.collect(Collectors.toList());

			List<PlanNode> collect2 = xone.stream()
				.map(l -> l.stream().map(p -> p.getPlan(connectionsGroup, false, overrideTargetNode, true, negateSubPlans)).collect(Collectors.toList()))
				.map(XonePropertyShape::unionAll)
				.collect(Collectors.toList());

			PlanNode planNode = unionAll(collect);
			PlanNode planNode2 = unionAll(collect2);

			Unique uniqueTargets = new Unique(new UnionNode(new TrimTuple(planNode, 0, 1), new TrimTuple(planNode2, 0, 1)));

			BufferedSplitter bufferedSplitter = new BufferedSplitter(uniqueTargets);

			List<PlanNode> collect1 = xone.stream()
				.map(l -> l.stream().map(p -> p.getPlan(connectionsGroup, false, () -> bufferedSplitter.getPlanNode(), true, negateSubPlans)).collect(Collectors.toList()))
				.map(XonePropertyShape::unionAll)
				.map(Unique::new)
				.collect(Collectors.toList());

			PlanNode planNode1 = unionAll(collect1);

			PlanNode planNode3 = bufferedSplitter.getPlanNode();
			LeftOuterJoin leftOuterJoin = new LeftOuterJoin(planNode3, planNode1);

			PlanNode groupByCount = new GroupByCount(leftOuterJoin);


			PlanNode falseNode = new ExactCountFilter(groupByCount, 1).getFalseNode(UnBufferedPlanNode.class);


			return new EnrichWithShape(falseNode, this);


		}

	}


	public boolean childrenHasOwnPath() {
		return xone.stream().flatMap(a -> a.stream().map(PathPropertyShape::hasOwnPath)).anyMatch(a -> a);
	}

	private static PlanNode unionAll(List<PlanNode> planNodes) {
		return new UnionNode(planNodes.toArray(new PlanNode[0]));
	}

	@Override
	public boolean requiresEvaluation(SailConnection addedStatements, SailConnection removedStatements) {
		if (deactivated) {
			return false;
		}

//		return super.requiresEvaluation(addedStatements, removedStatements) || xone.stream()
//				.flatMap(Collection::stream)
//				.map(p -> p.requiresEvaluation(addedStatements, removedStatements))
//				.reduce((a, b) -> a || b)
//				.orElse(false);

		return true;

	}

	@Override
	public SourceConstraintComponent getSourceConstraintComponent() {
		return SourceConstraintComponent.XoneConstraintComponent;
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
		XonePropertyShape that = (XonePropertyShape) o;
		return xone.equals(that.xone);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), xone);
	}

	@Override
	public String toString() {
		return "XonePropertyShape{" +
			"xone=" + toString(xone) +
			'}';
	}

	@Override
	public PlanNode getAllTargetsPlan(ConnectionsGroup connectionsGroup, boolean negated) {

		Optional<PlanNode> reduce = xone.stream()
			.flatMap(Collection::stream)
			.map(a -> a.getAllTargetsPlan(connectionsGroup, negated))
			.reduce((a, b) -> new UnionNode(a, b));

		return new Unique(reduce.get());

	}
}
