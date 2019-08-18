/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.planNodes;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;

/**
 *
 * @author Håvard Mikkelsen Ottestad
 */
public class DatatypeFilter extends FilterPlanNode {

	private final Resource datatype;

	public DatatypeFilter(PlanNode parent, Resource datatype) {
		super(parent);
		this.datatype = datatype;
	}

	@Override
	boolean checkTuple(Tuple t) {
		boolean result = false;
		if (t.line.get(1) instanceof Literal) {
			Literal literal = (Literal) t.line.get(1);
			result = literal.getDatatype() == datatype || literal.getDatatype().equals(datatype);
		}

		return result;
	}

	@Override
	public String toString() {
		return "DatatypeFilter{" + "datatype=" + Formatter.prefix(datatype) + '}';
	}
}
