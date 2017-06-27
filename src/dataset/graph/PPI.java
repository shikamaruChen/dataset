package dataset.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

import yifan.utils.FileIO;

import static yifan.utils.IOUtils.*;

public class PPI {

	private String dir = "/Users/User/Desktop/yifan/dataset/ppi/";
	private int nedge;
	private String patFile;
	private String contFile;
	private String supFile;
	private Map<String, Integer> cogMap;
	private Map<String, Integer> funcMap;
	private Map<String, Integer> idMap;

	public PPI() {
		cogMap = Maps.newHashMap();
		funcMap = Maps.newHashMap();
		idMap = Maps.newHashMap();
	}

	private void readFunc(String file) throws IOException {
		console("read COG vs function");
		BufferedReader reader = bufferReader(file);
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split("\t");
			int func = terms[1].charAt(0) - 'A';
			funcMap.put(terms[0], func);
		}
		reader.close();
	}

	private void readCog(String file) throws IOException {
		console("read all proteins vs function");
		BufferedReader reader = bufferReader(file);
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split("\t");
			if (terms[3].contains("COG") && funcMap.containsKey(terms[3])) {
				int label = funcMap.get(terms[3]);
				cogMap.put(terms[0], label);
			}
		}
		reader.close();
	}

	private void readWriteName(String rFile, BufferedWriter writer) throws IOException {
		console("read proteins and write the nodes");
		BufferedReader reader = bufferReader(rFile);
		String line = reader.readLine();
		int id = 0;
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(" ");
			if (cogMap.containsKey(terms[0])) {
				int label = cogMap.get(terms[0]);
				idMap.put(terms[0], id);
				writer.write(String.format("v %d %d", id++, label));
				writer.newLine();
			}
		}
		reader.close();
	}

	private void readWriteEdge(String rFile, BufferedWriter writer) throws IOException {
		console("read proteins and write the edges");
		BufferedReader reader = bufferReader(rFile);
		String line = reader.readLine();
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(" ");
			if (idMap.containsKey(terms[0]) && idMap.containsKey(terms[1])) {
				double p = Integer.parseInt(terms[2]) * 1.0 / 1000;
				int id1 = idMap.get(terms[0]);
				int id2 = idMap.get(terms[1]);
				writer.write(String.format("e %d %d 1 %f", id1, id2, p));
				writer.newLine();
			}
		}
		reader.close();
	}

	public void dataset(String proteinFile, String dataFile) throws IOException {
		String funcFile = "cognames2003-2014.tab.txt";
		String cogFile = "COG.mappings.v9.1.txt";
		readFunc(dir + funcFile);
		readCog(dir + cogFile);
		BufferedWriter writer = bufferWriter(dir+dataFile);
		writer.write("t # 1");
		writer.newLine();
		readWriteName(dir + proteinFile, writer);
		readWriteEdge(dir + proteinFile, writer);
		writer.close();
	}

	public void splitResult(String rfile, int n) throws Exception {
		File rdir = new File(dir + "predict");
		if (rdir.isDirectory())
			if (rdir.exists()) {
				File[] files = rdir.listFiles();
				for (File file : files)
					file.delete();
				rdir.delete();
			}
		rdir.mkdir();
		supFile = rdir.getPath() + "/support";
		contFile = rdir.getPath() + "/contain";
		patFile = rdir.getPath() + "/gdb";

		BufferedReader reader = bufferReader(dir + rfile);
		BufferedWriter writer = bufferWriter(contFile);
		String line;
		String graph = "";
		String title = "";
		double lower = 0, upper = 0;
		int id1 = 0;
		int id2 = 0;
		nedge = 0;
		Map<Double, Integer> support = Maps.newTreeMap(new Comparator<Double>() {
			@Override
			public int compare(Double key1, Double key2) {
				// TODO Auto-generated method stub
				if (key1 > key2)
					return -1;
				else
					return 1;
			}
		});
		Map<Integer, Set<Integer>> node = Maps.newHashMap();
		while ((line = reader.readLine()) != null) {
			if (line.contains("t")) {
				if (nedge == n - 1) {
					title = "t # " + id1;
					FileIO.writeString(patFile + "_cand", title + "\n" + graph, true);
					for (Entry<Integer, Set<Integer>> entry : node.entrySet())
						for (int pos : entry.getValue()) {
							writer.write(String.format("%d %d %d", id1, entry.getKey(), pos));
							writer.newLine();
						}
					id1++;
				}
				if (nedge == n) {
					title = "t # " + id2;
					FileIO.writeString(patFile, title + "\n" + graph, true);
					support.put((lower + upper) / 2, id2++);
				}
				graph = "";
				node.clear();
				nedge = 0;
			} else if (line.contains("v ")) {
				graph += line;
				graph += "\n";
			} else if (line.contains("e ")) {
				graph += line;
				graph += "\n";
				nedge++;
			} else if (line.contains("lower"))
				lower = Double.parseDouble(line.split("=")[1]);
			else if (line.contains("upper"))
				upper = Double.parseDouble(line.split("=")[1]);
			else {
				String[] terms = line.split(" ");
				for (int i = 0; i < terms.length; i++) {
					int v = Integer.parseInt(terms[i]);
					if (!node.containsKey(v))
						node.put(v, new HashSet<Integer>());
					Set<Integer> set = node.get(v);
					set.add(i);
				}
				// FileIO.writeString(contFile, line.split("=")[1], true);
				// FileIO.writeString(patFile, graph);
			}
		}
		reader.close();
		if (nedge == n - 1) {
			title = "t # " + id1;
			FileIO.writeString(patFile + "_cand", title + "\n" + graph, true);
			for (Entry<Integer, Set<Integer>> entry : node.entrySet())
				for (int pos : entry.getValue()) {
					writer.write(String.format("%d %d %d", id1, entry.getKey(), pos));
					writer.newLine();
				}
			id1++;
		}
		if (nedge == n) {
			title = "t # " + id2;
			FileIO.writeString(patFile, title + "\n" + graph, true);
			support.put((lower + upper) / 2, id2++);
		}
		writer.close();
		writer = bufferWriter(supFile);
		for (Entry<Double, Integer> entry : support.entrySet()) {
			writer.write(String.format("%f %d", entry.getKey(), entry.getValue()));
			writer.newLine();
		}
		writer.close();
	}

	public void removePro(String file) throws IOException {
		BufferedReader reader = bufferReader(dir + file);
		BufferedWriter writer = bufferWriter(dir + file + "0");
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("e")) {
				String[] terms = line.split(" ");
				writer.write(String.format("e %s %s %s", terms[1], terms[2], terms[3]));
				writer.newLine();
			} else {
				writer.write(line);
				writer.newLine();
			}
		}
		reader.close();
		writer.close();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PPI ppi = new PPI();
		try {
			ppi.dataset("4952.protein.links.v9.1.txt", "ppi_4952");
			// ppi.splitResult("res", 5);
			// ppi.removePro("subgraph");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
