package graph.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import math.Distribution;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import static yifan.utils.IOUtils.*;

public class UGraph {
	Map<Integer, Vertex> vertices;
	Map<String, Edge> edges;

	public UGraph() {
		vertices = Maps.newTreeMap();
		edges = Maps.newHashMap();
	}

	public UGraph subgraph(int nodes, int edges, double rate) {
		console(String.format(
				"sampling graph: %d nodes, %d edges, %.3f burn rate", nodes,
				edges, rate));
		UGraph graph = new UGraph();
		Map<Integer, Integer> idmap = Maps.newHashMap();
		while (graph.numVertices() < nodes || graph.numEdges() < edges)
			forestFire(graph, idmap, nodes, edges, rate);
		
		try {
			BufferedWriter writer = bufferWriter("idmap");
			for(int key:idmap.keySet()){
				writer.write(key+"\t"+idmap.get(key));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return graph;
	}

	public void forestFire(UGraph graph, Map<Integer, Integer> idmap,
			int nodes, int edges, double rate) {

		Distribution rand = new Distribution(System.currentTimeMillis());
		Queue<Integer> q = Queues.newArrayDeque();
		int n = numVertices();
		int start = rand.nextInt(n);
		int target;
		int s, t;
		int nid = graph.numVertices();
		q.offer(start);

		if (!idmap.containsKey(start)) {
			idmap.put(start, nid);
			Vertex vertex = getNode(start);
			graph.addNode(nid, vertex.label);
		}

		List<Integer> neighbors = Lists.newArrayList();
		while (!q.isEmpty()
				&& (graph.numVertices() < nodes || graph.numEdges() < edges)) {
			start = q.remove();
			s = idmap.get(start);
			Vertex vertex = getNode(start);
			// find unburned links
			neighbors.clear();
			for (int neighbor : vertex.neighbors) {
				if (idmap.containsKey(neighbor)) {
					t = idmap.get(neighbor);
					if (graph.isEdge(s, t))
						continue;
				}
				neighbors.add(neighbor);
			}

			// int burn = rand.getGeoDev(rate) - 1;
			// burn = Math.min(burn, neighbors.size());
			int burn = neighbors.size();
			Collections.shuffle(neighbors);
			for (int i = 0; i < burn; i++) {
				target = neighbors.get(i);
				vertex = getNode(target);
				if (!idmap.containsKey(target)) {
					if (idmap.size() >= nodes)
						continue;
					nid = graph.numVertices();
					idmap.put(target, nid);
					graph.addNode(nid, vertex.label);
					q.offer(target);
				}
				if (graph.numEdges() >= edges)
					continue;
				Edge edge = getEdge(start, target);
				s = idmap.get(start);
				t = idmap.get(target);
				graph.addEdge(s, t, edge.label, edge.pro);
			}
		}
	}

	public void read(String file) throws FileNotFoundException, IOException {
		console("reading graph from " + file);
		BufferedReader reader = bufferReader(file);
		String line;
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(" ");
			if (terms[0].equals("v")) {
				int id = Integer.parseInt(terms[1]);
				int label = Integer.parseInt(terms[2]);
				addNode(id, label);
			} else {
				int s = Integer.parseInt(terms[1]);
				int t = Integer.parseInt(terms[2]);
				int label = Integer.parseInt(terms[3]);
				double pro = Double.parseDouble(terms[4]);
				addEdge(s, t, label, pro);
			}
		}
		reader.close();
	}

	public void write(String file) throws IOException {
		console("writing graph to" + file);
		BufferedWriter writer = bufferWriter(file);
		String line;
		for (int id : vertices.keySet()) {
			int label = vertices.get(id).label;
			line = String.format("v %d %d", id, label);
			writer.write(line);
			writer.newLine();
		}
		for (String id : edges.keySet()) {
			Edge edge = edges.get(id);
			line = String.format("e %s %d %.3f", id, edge.label, edge.pro);
			writer.write(line);
			writer.newLine();
		}
		writer.close();
	}

	public int numVertices() {
		return vertices.size();
	}

	public int numEdges() {
		return edges.size();
	}

	public boolean addNode(int id, int label) {
		if (vertices.containsKey(id))
			return false;
		vertices.put(id, new Vertex(label));
		return true;
	}

	public boolean isEdge(int s, int t) {
		String sid;
		if (s < t)
			sid = String.format("%d %d", s, t);
		else
			sid = String.format("%d %d", t, s);
		return edges.containsKey(sid);
	}

	public Edge getEdge(int s, int t) {
		String sid;
		if (s < t)
			sid = String.format("%d %d", s, t);
		else
			sid = String.format("%d %d", t, s);
		return edges.get(sid);
	}

	public void addEdge(int s, int t, int label, double pro) {
		if (s == t)
			return;
		if (!vertices.containsKey(s) || !vertices.containsKey(t))
			return;
		vertices.get(s).addNeighbor(t);
		vertices.get(t).addNeighbor(s);

		String sid;
		if (s < t)
			sid = String.format("%d %d", s, t);
		else
			sid = String.format("%d %d", t, s);
		if (!edges.containsKey(sid))
			edges.put(sid, new Edge(label, pro));
	}

	public Vertex getNode(int id) {
		return vertices.get(id);
	}

	public void statistic() {
		int m = vertices.size();
		int n = edges.size();
		console("num vertices:" + m);
		console("num edges:" + n);
		console("density:" + n * 1.0 / m);
	}
}

class Vertex {
	int label;
	Set<Integer> neighbors;

	public void addNeighbor(int t) {
		neighbors.add(t);
	}

	public int numBeighbors() {
		return neighbors.size();
	}

	public List<Integer> getNeighbors() {
		return new ArrayList<Integer>(neighbors);
	}

	public Vertex(int label) {
		this.label = label;
		neighbors = Sets.newTreeSet();
	}
}

class Edge {
	int label;
	double pro;

	public Edge(int label, double pro) {
		this.label = label;
		this.pro = pro;
	}
}