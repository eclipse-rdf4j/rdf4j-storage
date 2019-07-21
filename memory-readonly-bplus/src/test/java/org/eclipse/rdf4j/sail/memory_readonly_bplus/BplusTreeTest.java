package org.eclipse.rdf4j.sail.memory_readonly_bplus;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.comparators.SPOCComparator;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class BplusTreeTest {

	@Test
	public void initialTest() throws IOException {

		BplusTree<Statement> statementBplusTree = loadSPOC();

	}

	@Test
	public void getFirstElement() throws IOException {

		BplusTree<Statement> statementBplusTree = loadSPOC();

		SimpleValueFactory vf = SimpleValueFactory.getInstance();

		String NS = "http://example.com/";

		Statement subB_predB_objA = vf.createStatement(vf.createIRI(NS, "subB"), vf.createIRI(NS, "predB"),
				vf.createIRI(NS, "objA"));

		BplusTree<Statement>.DataNode firstNode = statementBplusTree.getFirstNode(subB_predB_objA);

		System.out.println();

	}

	@Test
	public void getFirstElementPartialIndex() throws IOException {

		BplusTree<Statement> statementBplusTree = loadSPOC();

		SimpleValueFactory vf = SimpleValueFactory.getInstance();

		String NS = "http://example.com/";

		Statement subB_predB = new PartialStatement(vf.createIRI(NS, "subB"), vf.createIRI(NS, "predB"), null, null);

		BplusTree<Statement>.DataNode firstNode = statementBplusTree.getFirstNode(subB_predB);

		System.out.println();

	}

	@Test
	public void getLastElementPartialIndex() throws IOException {

		BplusTree<Statement> statementBplusTree = loadSPOC();

		SimpleValueFactory vf = SimpleValueFactory.getInstance();

		String NS = "http://example.com/";

		Statement subB_predB = new PartialStatement(vf.createIRI(NS, "subB"), vf.createIRI(NS, "predB"), null, null);

		BplusTree<Statement>.DataNode last = statementBplusTree.getLastNode(subB_predB);

		System.out.println();

	}

	@Test
	public void getLastElementPartialIndex2() throws IOException {

		BplusTree<Statement> statementBplusTree = loadSPOC();

		SimpleValueFactory vf = SimpleValueFactory.getInstance();

		String NS = "http://example.com/";

		Statement subB_predB = new PartialStatement(vf.createIRI(NS, "sub3B"), vf.createIRI(NS, "pre3dA"), null, null);

		BplusTree<Statement>.DataNode lastNode = statementBplusTree.getLastNode(subB_predB);

		System.out.println();

	}

	private BplusTree<Statement> loadSPOC() throws IOException {
		Model parse = Rio.parse(BplusTreeTest.class.getClassLoader().getResourceAsStream("test.ttl"), "",
				RDFFormat.TURTLE);

		ArrayList<Statement> statements = new ArrayList<>(parse);

		ValueComparator valueComparator = new ValueComparator();

		statements.sort(new SPOCComparator());

		return new BplusTree<>(statements, new SPOCComparator());
	}

}