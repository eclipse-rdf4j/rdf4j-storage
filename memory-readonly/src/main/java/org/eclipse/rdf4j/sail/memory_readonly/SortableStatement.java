package org.eclipse.rdf4j.sail.memory_readonly;

import org.eclipse.rdf4j.model.Statement;

public class SortableStatement implements Comparable<SortableStatement> {

	private Statement statement;
	private String key;

	public SortableStatement(Statement statement, String key) {
		this.statement = statement;
		this.key = key;
	}

	public Statement getStatement() {
		return statement;
	}

	@Override
	public int compareTo(SortableStatement o) {
		return key.compareTo(o.key);
	}
}
