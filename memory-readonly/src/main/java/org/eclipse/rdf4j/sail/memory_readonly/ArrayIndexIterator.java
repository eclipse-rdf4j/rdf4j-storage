/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.memory_readonly;

import org.eclipse.rdf4j.model.Statement;

import java.util.Iterator;

/**
 * @author Håvard Mikkelsen Ottestad
 */
public class ArrayIndexIterator implements Iterable<Statement> {

	private Statement[] array;

	private int startInclusive;
	private int stopExclusive;

	private final boolean needsFurtherFiltering;

	ArrayIndexIterator(Statement[] array, int startInclusive, int stopExclusive, boolean needsFurtherFiltering) {
		this.array = array;
		this.startInclusive = startInclusive;
		this.stopExclusive = stopExclusive;
		this.needsFurtherFiltering = needsFurtherFiltering;
	}

	public boolean isNeedsFurtherFiltering() {
		return needsFurtherFiltering;
	}

	@Override
	public Iterator<Statement> iterator() {
		return new Iterator<Statement>() {
			int current = startInclusive;

			@Override
			public boolean hasNext() {
				return current < stopExclusive;
			}

			@Override
			public Statement next() {
				Statement temp = array[current];
				current++;
				return temp;
			}
		};
	}
}
