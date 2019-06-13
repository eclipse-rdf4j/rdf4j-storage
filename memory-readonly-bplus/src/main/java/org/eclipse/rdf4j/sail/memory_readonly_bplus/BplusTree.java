package org.eclipse.rdf4j.sail.memory_readonly_bplus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class BplusTree<E> {

	private static final int NODE_SIZE = 4;

	private final IndexNode root;

	Comparator<E> comparator;

	public BplusTree(Collection<E> statements, Comparator<E> comparator) {
		this.comparator = comparator;

		List<DataNode> dataNodes = buildDataNodes(statements);

		List<IndexNode> linked = index(dataNodes);

		while (linked.size() > 1) {
			linked = index(linked);
		}

		root = linked.get(0);
	}

	private List<IndexNode> index(Object o) {

		ArrayList<IndexNode> newIndex = new ArrayList<>();

		if (o instanceof List) {
			List list = (List) o;
			if (DataNode.class.isInstance(list.get(0))) {
				fillDataNodes(newIndex, (ArrayList<DataNode>) list);

			} else if (IndexNode.class.isInstance(list.get(0))) {
				fillIndexNodes(newIndex, (ArrayList<IndexNode>) list);

			}


		} else {
			throw new IllegalStateException();
		}


		return newIndex;
	}

	private void fillIndexNodes(ArrayList<IndexNode> newIndex, ArrayList<IndexNode> list) {
		List<IndexNode> indexNodes = (ArrayList<IndexNode>) list;

		int counter = 0;
		IndexNode indexNode = null;
		for (IndexNode indexNodeTemp : indexNodes) {
			if (counter >= NODE_SIZE) {
				counter = 0;
			}
			if (counter == 0) {
				indexNode = new IndexNode<IndexNode>();
				newIndex.add(indexNode);
			}

			indexNode.setNode(counter, indexNodeTemp, indexNodeTemp.getIndex(0), false);
			indexNode.size++;

			counter++;

		}
	}

	private void fillDataNodes(ArrayList<IndexNode> newIndex, ArrayList<DataNode> list) {
		List<DataNode> datanodes = (ArrayList<DataNode>) list;

		int counter = 0;
		IndexNode indexNode = null;
		for (DataNode dataNode : datanodes) {
			if (counter >= NODE_SIZE) {
				counter = 0;
			}
			if (counter == 0) {
				indexNode = new IndexNode<DataNode>();
				newIndex.add(indexNode);
			}

			indexNode.setNode(counter, dataNode, dataNode.getNode(0), true);
			indexNode.size++;

			counter++;

		}
	}

	private List<DataNode> buildDataNodes(Collection<E> statements) {
		int numberOfDataNodes = (statements.size() / NODE_SIZE) + 1;

		List<DataNode> dataNodes = new ArrayList<>(numberOfDataNodes);

		int counter = 0;
		DataNode dataNode = null;
		for (E statement : statements) {
			if (counter >= NODE_SIZE) {
				counter = 0;
			}
			if (counter == 0) {
				dataNode = new DataNode();
				dataNodes.add(dataNode);
			}

			dataNode.setNode(counter, statement);
			dataNode.size++;

			counter++;

		}


		// link
		link(dataNodes);

		return dataNodes;

	}

	private void link(List<DataNode> dataNodes) {
		for (int i = 0; i < dataNodes.size() - 1; i++) {
			dataNodes.get(i).next = dataNodes.get(i + 1);
		}
	}

	public DataNode getFirstNode(E find) {

		IndexNode node = root;

		while (node != null && node.hasDatanode == false) {

			node = (IndexNode) node.getNode(find, comparator);

		}


		if (node == null) {
			return null;
		}

		return (DataNode) node.getNode(find, comparator);

	}

	public DataNode getLastNode(E find) {
		IndexNode node = root;

		while (node != null && node.hasDatanode == false) {

			node = (IndexNode) node.getLastNode(find, comparator);

		}


		if (node == null) {
			return null;
		}

		return (DataNode) node.getLastNode(find, comparator);
	}

	class DataNode {
		final Object[] datanodes = new Object[NODE_SIZE];

		DataNode next;

		int size = 0;

		E getNode(int i) {
			return (E) datanodes[i];
		}

		void setNode(int i, E e) {
			datanodes[i] = e;
		}
	}

	class IndexNode<F> {

		// usually RDF4J Statement
		final Object[] index = new Object[NODE_SIZE];
		// either IndexNode or DataNode
		final Object[] nodes = new Object[NODE_SIZE];

		boolean hasDatanode;

		public int size;

		public void setNode(int i, F dataNode, E node, boolean hasDatanode) {
			index[i] = node;
			nodes[i] = dataNode;
			this.hasDatanode = hasDatanode;
		}

		public E getIndex(int i) {
			return (E) index[i];
		}

		public F getNode(int i) {
			return (F) nodes[i];
		}

		public Object getNode(E find, Comparator<E> comparator) {

			for (int i = 0; i < size; i++) {
				int compare = comparator.compare((E) index[i], find);

				if (compare >= 0) {
					if (i > 0) {
						return nodes[i - 1];
					} else {
						return null;
					}
				}

			}
			return nodes[size - 1];
		}

		public Object getLastNode(E find, Comparator<E> comparator) {
			for (int i = 0; i < size; i++) {
				int compare = comparator.compare((E) index[i], find);

				if (compare > 0) {
					if (i > 0) {
						return nodes[i - 1];
					} else {
						return null;
					}
				}

			}
			return nodes[size - 1];
		}
	}

}
