package org.eclipse.rdf4j.sail.memory_readonly_bplus.indexes;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.BplusTree;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.ListIterable;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.comparators.SPOCComparator;

import java.util.ArrayList;
import java.util.List;

public class SPOIndex {

	BplusTree<Statement> index;

	public SPOIndex(List<Statement> collect) {

		collect = new ArrayList<>(collect);

		collect.sort(new SPOCComparator());

		index = new BplusTree<>(collect, new SPOCComparator());

	}

	public boolean isEmpty() {
		return false;
	}

	public ListIterable getStatements(Resource subject, IRI predicate, Value object, Resource[] context) {
		return null;
	}
}
