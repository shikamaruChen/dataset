package dataset;

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

	public String dir = "/home/yifan/dataset/ppi/";
	private int nedge;
	private String patFile;
	private String contFile;
	private String supFile;

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
			ppi.splitResult("res", 5);
			// ppi.removePro("subgraph");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
