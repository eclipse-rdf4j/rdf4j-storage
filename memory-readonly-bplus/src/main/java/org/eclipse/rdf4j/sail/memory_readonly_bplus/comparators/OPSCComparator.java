package org.eclipse.rdf4j.sail.memory_readonly_bplus.comparators;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;

import java.util.Comparator;

public class OPSCComparator implements Comparator<Statement> {

	final static ValueComparator vc = new ValueComparator();


	@Override
	public int compare(Statement o1, Statement o2) {

		int compare;

		if(o1.getObject() == null || o2.getObject() == null) return 0;
		compare = vc.compare(o1.getObject(), o2.getObject());
		if(compare != 0) return compare;


		if(o1.getPredicate() == null || o2.getPredicate() == null) return 0;
		compare = o1.getPredicate().toString().compareTo(o2.getPredicate().toString());
		if(compare != 0) return compare;


		if(o1.getSubject() == null || o2.getSubject() == null) return 0;
		compare = vc.compare(o1.getSubject(), o2.getSubject());
		if(compare != 0) return compare;


		if(o1.getContext() == null || o2.getContext() == null) return 0;
		compare = vc.compare(o1.getContext(), o2.getContext());
		return compare;

	}
}
