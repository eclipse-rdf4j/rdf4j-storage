/*******************************************************************************
 * Copyright (c) 2019 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.sail.shacl.benchmark;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.memory_readonly.MemoryStoreReadonly;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.MemoryStoreReadonlyBplus;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.comparators.OPSCComparator;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.comparators.PSOCComparator;
import org.eclipse.rdf4j.sail.memory_readonly_bplus.comparators.SPOCComparator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Håvard Ottestad
 */
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@BenchmarkMode({ Mode.AverageTime })
@Fork(value = 1, jvmArgs = { "-Xms8G", "-Xmx8G" })
//@Fork(value = 1, jvmArgs = {"-Xms8G", "-Xmx8G", "-XX:+UnlockCommercialFeatures", "-XX:StartFlightRecording=delay=15s,duration=120s,filename=recording.jfr,settings=profile", "-XX:FlightRecorderOptions=samplethreads=true,stackdepth=1024", "-XX:+UnlockDiagnosticVMOptions", "-XX:+DebugNonSafepoints"})
@Measurement(iterations = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DataLoadingBenchmark {

	List<Statement> statements;
	List<String> statementsString;

	{
		try {

			Model parse = Rio.parse(
					DataLoadingBenchmark.class.getClassLoader().getResourceAsStream("complexBenchmark/generated.ttl"),
					"", RDFFormat.TURTLE);

			SimpleValueFactory vf = SimpleValueFactory.getInstance();

			statements = parse.stream()
					.map(s -> vf.createStatement(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext()))
					.collect(Collectors.toList());

			Collections.shuffle(statements);

			statementsString = statements.stream().map(s -> {
				StringBuilder stringBuilder = new StringBuilder();
				return stringBuilder.append(s.getSubject().toString())
						.append("::")
						.append(s.getPredicate().toString())
						.append("::")
						.append(s.getObject().toString())
						.toString();

			}).collect(Collectors.toList());

		} catch (IOException e) {

		}

	}

	@Benchmark
	public void bplus() {

		new MemoryStoreReadonlyBplus(statements);

	}

	@Benchmark
	public void hash() {

		new MemoryStoreReadonly(statements);

	}

	@Benchmark
	public void oldMem() {

		MemoryStore memoryStore = new MemoryStore();
		memoryStore.init();

		try (NotifyingSailConnection connection = memoryStore.getConnection()) {
			connection.begin(IsolationLevels.NONE);
			statements.forEach(
					s -> connection.addStatement(s.getSubject(), s.getPredicate(), s.getObject(), s.getContext()));
			connection.commit();
		}

	}

	@Benchmark
	public void sortSPOC() {

		ArrayList<Statement> statements = new ArrayList<>(this.statements);
		statements.sort(new SPOCComparator());

	}

	@Benchmark
	public void stringSort() {

		List<String> collect = statements.stream().map(s -> {
			StringBuilder stringBuilder = new StringBuilder();
			return stringBuilder.append(s.getSubject().toString())
					.append("::")
					.append(s.getPredicate().toString())
					.append("::")
					.append(s.getObject().toString())
					.toString();

		}).sorted().collect(Collectors.toList());

	}

	@Benchmark
	public void stringSort2() {

		ArrayList<String> strings = new ArrayList<>(statementsString);
		strings.sort(String::compareTo);

	}

	@Benchmark
	public void stringOPSCSort() {

		List<String> collect = statements.stream().map(s -> {
			StringBuilder stringBuilder = new StringBuilder();
			return stringBuilder.append(s.getObject().toString())
					.append("::")
					.append(s.getPredicate().toString())
					.append("::")
					.append(s.getSubject().toString())
					.toString();

		}).sorted().collect(Collectors.toList());

	}

	@Benchmark
	public void sortOPSC() {

		ArrayList<Statement> statements = new ArrayList<>(this.statements);
		statements.sort(new OPSCComparator());

	}

	@Benchmark
	public void sortPSOC() {

		ArrayList<Statement> statements = new ArrayList<>(this.statements);
		statements.sort(new PSOCComparator());

	}

}
