package org.eclipse.rdf4j.sail.memory_readonly;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class OrderedSPOIndexTest {


	private static final SimpleValueFactory vf = SimpleValueFactory.getInstance();
	private static final IRI subA = vf.createIRI("http://example.com/", "subA");
	private static final IRI predA = vf.createIRI("http://example.com/", "predA");
	private static final IRI objA = vf.createIRI("http://example.com/", "objA");
	private static final IRI objB = vf.createIRI("http://example.com/", "objB");
	private static final IRI objC = vf.createIRI("http://example.com/", "objC");
	private static final IRI predB = vf.createIRI("http://example.com/", "predB");
	private static final IRI subB = vf.createIRI("http://example.com/", "subB");


	@Test
	public void order() {
		HashSet<Statement> statements = load("test.ttl", RDFFormat.TURTLE);

		OrderedSPOIndex orderedSPOIndex = new OrderedSPOIndex(statements);

		SailRepository sailRepository = getSailRepository(statements);

		List<String> sortedBySparql;

		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			try (Stream<BindingSet> stream = Iterations.stream(connection.prepareTupleQuery("select distinct ?a where {?a ?b ?c} order by ?a").evaluate())) {
				sortedBySparql = stream.map(bindings -> bindings.getValue("a").stringValue()).collect(Collectors.toList());
			}
		}

		List<String> sortedByOrderedIndex = Arrays
			.stream(orderedSPOIndex.orderedArray)
			.map(SortableStatement::getStatement)
			.map(Statement::getSubject)
			.map(Value::stringValue)
			.distinct()
			.collect(Collectors.toList());

		assertEquals(sortedBySparql, sortedByOrderedIndex);

	}

	private SailRepository getSailRepository(HashSet<Statement> statements) {
		SailRepository sailRepository = new SailRepository(new MemoryStore());
		sailRepository.init();

		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			connection.begin();
			connection.add(statements);
			connection.commit();
		}
		return sailRepository;
	}


	@Test
	public void sIndex() {
		HashSet<Statement> statements = load("test.ttl", RDFFormat.TURTLE);

		OrderedSPOIndex orderedSPOIndex = new OrderedSPOIndex(statements);

		ArrayIndex arrayIndex = orderedSPOIndex.sIndex.get(new SCompound(subA));

		assertEquals(subA, orderedSPOIndex.orderedArray[arrayIndex.startInclusive].getStatement().getSubject());
		assertNotEquals(subA, orderedSPOIndex.orderedArray[arrayIndex.stopExclusive].getStatement().getSubject());

	}


	@Test
	public void spIndex() {
		HashSet<Statement> statements = load("test.ttl", RDFFormat.TURTLE);

		OrderedSPOIndex orderedSPOIndex = new OrderedSPOIndex(statements);

		ArrayIndex arrayIndex = orderedSPOIndex.spIndex.get(new SpCompound(subA, predB));

		assertEquals(subA, orderedSPOIndex.orderedArray[arrayIndex.startInclusive].getStatement().getSubject());
		assertEquals(predB, orderedSPOIndex.orderedArray[arrayIndex.startInclusive].getStatement().getPredicate());
		assertNotEquals(predB, orderedSPOIndex.orderedArray[arrayIndex.stopExclusive].getStatement().getPredicate());

	}

	@Test
	public void spoIndex() {
		HashSet<Statement> statements = load("test.ttl", RDFFormat.TURTLE);

		OrderedSPOIndex orderedSPOIndex = new OrderedSPOIndex(statements);

		ArrayIndex arrayIndex = orderedSPOIndex.spoIndex.get(new SpoCompound(subA, predB, objB));

		assertEquals(subA, orderedSPOIndex.orderedArray[arrayIndex.startInclusive].getStatement().getSubject());
		assertEquals(predB, orderedSPOIndex.orderedArray[arrayIndex.startInclusive].getStatement().getPredicate());
		assertEquals(objB, orderedSPOIndex.orderedArray[arrayIndex.startInclusive].getStatement().getObject());
		assertNotEquals(objB, orderedSPOIndex.orderedArray[arrayIndex.stopExclusive].getStatement().getObject());

	}


	@Test
	public void orderSpIndex() {
		HashSet<Statement> statements = load("test.ttl", RDFFormat.TURTLE);

		OrderedSPOIndex orderedSPOIndex = new OrderedSPOIndex(statements);


		List<String> sortedByOrderedIndex = StreamSupport.stream(orderedSPOIndex.getStatements(subA, predB, null).spliterator(), false)
			.map(s -> s.getSubject().stringValue() + " " + s.getPredicate().stringValue() + " " + s.getObject().stringValue())
			.collect(Collectors.toList());

		SailRepository sailRepository = getSailRepository(statements);

		List<String> sortedBySparql;

		try (SailRepositoryConnection connection = sailRepository.getConnection()) {
			String query = "select * where {\nbind(<" + subA + "> as ?a)\n  bind(<" + predB + "> as ?b)\n ?a ?b ?c.} order by ?a ?b ?c";
			try (Stream<BindingSet> stream = Iterations.stream(connection.prepareTupleQuery(query).evaluate())) {
				sortedBySparql = stream
					.map(bindings -> bindings.getValue("a").stringValue() + " " + bindings.getValue("b").stringValue() + " " + bindings.getValue("c").stringValue())
					.collect(Collectors.toList());
			}
		}


		assertEquals(sortedBySparql, sortedByOrderedIndex);

	}

	private HashSet<Statement> load(String name, RDFFormat turtle) {

		Model model;
		try {
			model = Rio.parse(OrderedSPOIndexTest.class.getClassLoader().getResourceAsStream(name), "", turtle);
		} catch (IOException e) {
			throw new IllegalStateException("File not found: " + name);
		}
		return new HashSet<>(model);
	}

}
