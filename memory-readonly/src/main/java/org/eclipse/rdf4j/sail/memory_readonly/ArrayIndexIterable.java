/* @formatter:off */
/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.memory_readonly;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.util.iterators.EmptyIterator;

import java.util.Iterator;

/**
 * @author Håvard Mikkelsen Ottestad
 */
public class ArrayIndexIterable implements Iterable<Statement> {

	private SortableStatement[] array;

	private int startInclusive;
	private int stopExclusive;

	private final boolean needsFurtherFiltering;

	ArrayIndexIterable(SortableStatement[] array, int startInclusive, int stopExclusive, boolean needsFurtherFiltering) {
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
				Statement temp = array[current].getStatement();
				current++;
				return temp;
			}
		};
	}

	static class EmptyArrayIndexIterable extends ArrayIndexIterable{

		EmptyArrayIndexIterable(SortableStatement[] array, int startInclusive, int stopExclusive, boolean needsFurtherFiltering) {
			super(array, startInclusive, stopExclusive, needsFurtherFiltering);
		}

		EmptyArrayIndexIterable(){
			super(null, -1,-1, false);
		}

		@Override
		public Iterator<Statement> iterator() {
			return new EmptyIterator<>();
		}
	}

}


