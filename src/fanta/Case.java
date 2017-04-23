package fanta;

import static yifan.utils.IOUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.standard.Sides;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import graph.data.UGraph;
import yifan.utils.sscanf.Sscanf;

public class Case {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		// run();
		// author();
		// int n = authors(103096, 112703);
		// console(n);
		differ();
		// clean();
	}

	private static void clean() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = bufferReader("D:/experiment/fanta/result/case/res_pro");
		BufferedWriter writer = bufferWriter("D:/experiment/fanta/result/case/res_clean");
		String line;
		int count = 0;
		while ((line = reader.readLine()) != null) {
			if (line.contains("t #"))
				writer.write("t # " + count++);
			else
				writer.write(line);
			writer.newLine();
		}
		reader.close();
		writer.close();
	}

	private static void differ() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = bufferReader("D:/experiment/fanta/result/case/res");
		String line;
		// for(int i=0;i<v.length;i++)v = new Integer(0);
		Set<Integer> set = Sets.newHashSet();
		int max = 0;
		while ((line = reader.readLine()) != null) {
			Object[] objs = Sscanf.scan(line, "%d(%d) - %d(%d) 0 #%d", 1, 1, 1, 1, 1);
			int v = (int) objs[3];
			if (max < v)
				max = v;
			set.add(v);
		}
		reader.close();
		boolean[] matched = new boolean[max + 1];
		for (int v : set)
			matched[v] = true;
		int notmatch = 0;
		for (int i = 0; i < max; i++)
			if (!matched[i]) {
				console(i);
				notmatch++;
			}
		console("not matched:" + notmatch);
	}

	private static int authors(int id1, int id2) throws IOException {
		// TODO Auto-generated method stub
		String dir = "D:/experiment/fanta/result/case study/";
		BufferedReader reader = bufferReader(dir + "authors");
		reader = bufferReader(dir + "author");
		String line;
		int ln = 0;
		String name1 = null, name2 = null;
		while ((line = reader.readLine()) != null) {
			if (ln == id1)
				name1 = line.split("\t")[0];
			if (ln == id2)
				name2 = line.split("\t")[0];
			ln++;
		}
		reader.close();
		if (name1 == null || name2 == null)
			return 0;
		reader = bufferReader(dir + "authors");
		while ((line = reader.readLine()) != null) {

			if (line.contains(name1) && line.contains(name2)) {
				int num = Integer.parseInt(line.split("\t")[2]);
				return num;
			}
		}
		reader.close();
		return 0;
	}

	private static void author() throws IOException {
		// TODO Auto-generated method stub
		String dir = "D:/experiment/fanta/result/case study/";
		BufferedReader reader = bufferReader(dir + "idmap");
		Map<Integer, Integer> ids = Maps.newHashMap();
		String line;
		String find = "811 181 166 175 181 955";
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split("\t");
			for (String s : find.split(" ")) {
				if (terms[1].equals(s)) {
					ids.put(Integer.parseInt(terms[0]), Integer.parseInt(s));
					// console(s+":"+terms[0]);
				}
			}
		}
		reader.close();
		reader = bufferReader(dir + "author");
		int ln = 0;
		while ((line = reader.readLine()) != null) {
			if (ids.containsKey(ln))
				console(String.format("%d\t%d\t%s", ids.get(ln), ln, line));
			ln++;
		}
	}

	private static void run() throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		UGraph graph = new UGraph();
		graph.read("/Users/User/Desktop/yifan/dataset/dblp/origin");
		// graph.read("graph");
		UGraph subgraph = graph.subgraph(1000, 5000, -1);
		subgraph.write("/Users/User/Desktop/yifan/dataset/dblp/sub");
	}

}
