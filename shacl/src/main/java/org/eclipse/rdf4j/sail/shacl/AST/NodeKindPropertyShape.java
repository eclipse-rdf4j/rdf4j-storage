/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.shacl.AST;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.shacl.ShaclSailConnection;
import org.eclipse.rdf4j.sail.shacl.SourceConstraintComponent;
import org.eclipse.rdf4j.sail.shacl.planNodes.EnrichWithShape;
import org.eclipse.rdf4j.sail.shacl.planNodes.NodeKindFilter;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNode;
import org.eclipse.rdf4j.sail.shacl.planNodes.PlanNodeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Håvard Ottestad
 */
public class NodeKindPropertyShape extends PathPropertyShape {

	private final NodeKind nodeKind;
	private static final Logger logger = LoggerFactory.getLogger(NodeKindPropertyShape.class);

	NodeKindPropertyShape(Resource id, SailRepositoryConnection connection, NodeShape nodeShape, boolean deactivated,
			Resource path,
			Resource nodeKind) {
		super(id, connection, nodeShape, deactivated, path);

		this.nodeKind = NodeKind.from(nodeKind);

	}

	public enum NodeKind {

		BlankNode(SHACL.BLANK_NODE),
		IRI(SHACL.IRI),
		Literal(SHACL.LITERAL),
		BlankNodeOrIRI(SHACL.BLANK_NODE_OR_IRI),
		BlankNodeOrLiteral(SHACL.BLANK_NODE_OR_LITERAL),
		IRIOrLiteral(SHACL.IRI_OR_LITERAL),;

		IRI iri;

		NodeKind(IRI iri) {
			this.iri = iri;
		}

		public static NodeKind from(Resource resource) {
			for (NodeKind value : NodeKind.values()) {
				if (value.iri.equals(resource)) {
					return value;
				}
			}

			throw new IllegalStateException("Unknown nodeKind: " + resource);
		}
	}

	@Override
	public PlanNode getPlan(ShaclSailConnection shaclSailConnection, NodeShape nodeShape, boolean printPlans,
			PlanNodeProvider overrideTargetNode) {
		if (deactivated) {
			return null;
		}

		PlanNode invalidValues = StandardisedPlanHelper.getGenericSingleObjectPlan(shaclSailConnection, nodeShape,
				(parent) -> new NodeKindFilter(parent, nodeKind), this, overrideTargetNode);

		if (printPlans) {
			String planAsGraphvizDot = getPlanAsGraphvizDot(invalidValues, shaclSailConnection);
			logger.info(planAsGraphvizDot);
		}

		return new EnrichWithShape(invalidValues, this);

	}

	@Override
	public SourceConstraintComponent getSourceConstraintComponent() {
		return SourceConstraintComponent.NodeKindConstraintComponent;
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
		NodeKindPropertyShape that = (NodeKindPropertyShape) o;
		return nodeKind == that.nodeKind;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), nodeKind);
	}

	@Override
	public String toString() {
		return "NodeKindPropertyShape{" +
				"nodeKind=" + nodeKind +
				", path=" + path +
				'}';
	}
}
