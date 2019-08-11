/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.planNodes;

import org.eclipse.rdf4j.model.Literal;

/**
 * @author Håvard Ottestad
 */
public class ExactCountFilter extends FilterPlanNode {

	private final long exactCount;
	private final int index;

	public ExactCountFilter(PlanNode parent, long exactCount) {
		this(parent, exactCount, 1);
	}

	public ExactCountFilter(PlanNode parent, long exactCount, int index) {
		super(parent);
		this.exactCount = exactCount;
		this.index = index;
	}

	@Override
	boolean checkTuple(Tuple t) {
		Literal literal = (Literal) t.line.get(index);
		return literal.longValue() == exactCount;
	}

	@Override
	public String toString() {
		return "ExactCountFilter{" + "exactCount=" + exactCount + '}';
	}
}
