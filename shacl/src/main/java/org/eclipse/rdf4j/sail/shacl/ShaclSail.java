/*******************************************************************************
 * Copyright (c) 2018 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sail.NotifyingSail;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.NotifyingSailWrapper;
import org.eclipse.rdf4j.sail.shacl.AST.NodeShape;

/**
 * @author Heshan Jayasinghe
 */
public class ShaclSail extends NotifyingSailWrapper {

	private List<NodeShape> nodeShapes;

	public static final IRI SHACL_GRAPH = SimpleValueFactory.getInstance().createIRI("shacl:graph");
	
	ShaclSailConfig config = new ShaclSailConfig();

	public ShaclSail(NotifyingSail baseSail) {
		super(baseSail);
	}

	@Override
	public NotifyingSailConnection getConnection()
		throws SailException
	{
		return new ShaclSailConnection(this, super.getConnection());
	}

	public void disableValidation() {
		config.validationEnabled = false;
	}

	public void enableValidation() {
		config.validationEnabled = true;
	}
	
	protected List<Shape> getShapes(ShaclSailConnection conn) {
		if (shapes == null) {
			shapes = Shape.Factory.getShapes(conn);
		}
		return shapes;
	}

	public boolean isDebugPrintPlans() {
		return debugPrintPlans;
	}

	public void setDebugPrintPlans(boolean debugPrintPlans) {
		this.debugPrintPlans = debugPrintPlans;
	}
}

class ShaclSailConfig {

	boolean validationEnabled = true;

}
