package org.eclipse.rdf4j.sail.lucene;

import org.eclipse.rdf4j.query.algebra.BindingSetAssignment;
import org.eclipse.rdf4j.query.algebra.EmptySet;
import org.eclipse.rdf4j.query.algebra.QueryModelNode;

public abstract class AbstractSearchQueryEvaluator implements SearchQueryEvaluator {

	@Override
	public void replaceQueryPatternsWithResults(final BindingSetAssignment bsa) {
		final QueryModelNode placeholder = removeQueryPatterns();
		if (bsa != null && bsa.getBindingSets() != null && bsa.getBindingSets().iterator().hasNext()) {
			placeholder.replaceWith(bsa);
		} else {
			placeholder.replaceWith(new EmptySet());
		}
	}

}
