package dataset.feedback;

import static yifan.utils.IOUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.google.common.collect.Maps;

public class Paper {
	private String dir;
	private Map<Integer, Integer> items;
	private int m = 0;
	private int n = 0;

	public Paper(String dir) {
		this.dir = dir;
		items = Maps.newHashMap();
	}

	public void rating_init(String name) throws IOException {
		BufferedReader reader = bufferReader(dir + "/raw/" + name);
		String line;
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(" ");
			int user = Integer.parseInt(terms[0]);
			int item = Integer.parseInt(terms[1]);
			m = (m < user) ? user : m;
			if (!items.containsKey(item))
				items.put(item, ++n);
		}
		console(n);
		reader.close();
	}

	public void rating(String name) throws IOException {
		BufferedReader reader = bufferReader(dir + "/raw/" + name);
		BufferedWriter writer = bufferWriter(dir + "/" + name + ".txt");
		String line;
		int nnz = 0;
		while ((line = reader.readLine()) != null)
			nnz++;
		reader.close();

		reader = bufferReader(dir + "/raw/" + name);
		line = "%%MatrixMarket matrix coordinate real general";
		writer.write(line);
		writer.newLine();
		line = String.format("%d %d %d", m + 1, n, nnz);
		writer.write(line);
		writer.newLine();
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(" ");
			int user = Integer.parseInt(terms[0]) + 1;
			int item = Integer.parseInt(terms[1]);
			line = String.format("%d %d 1", user, items.get(item));
			writer.write(line);
			writer.newLine();
		}
		reader.close();
		writer.close();
	}

	public void similarity() throws FileNotFoundException, IOException {
		BufferedReader reader = bufferReader(dir + "/raw/cf_matrix");
		String line;
		int nnz = 0;
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(" ");
			int item1 = Integer.parseInt(terms[0]);
			int item2 = Integer.parseInt(terms[1]);
			if (items.containsKey(item1) && items.containsKey(item2))
				nnz++;
		}
		reader.close();

		reader = bufferReader(dir + "/raw/cf_matrix");
		BufferedWriter writer = bufferWriter(dir + "/similarity.txt");
		line = "%%MatrixMarket matrix coordinate real general";
		writer.write(line);
		writer.newLine();
		line = String.format("%d %d %d", n, n, nnz);
		writer.write(line);
		writer.newLine();
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(" ");
			int item1 = Integer.parseInt(terms[0]);
			int item2 = Integer.parseInt(terms[1]);
			float sim = Float.parseFloat(terms[2]);
			if (items.containsKey(item1) && items.containsKey(item2)) {
				line = String.format("%d %d %f", items.get(item1), items.get(item2), sim);
				writer.write(line);
				writer.newLine();
			}
		}
		reader.close();
		writer.close();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Paper paper = new Paper("/Users/chenyifan/jianguo/dataset/paper");
		try {
			paper.rating_init("browse");
			paper.rating_init("paper_shown");
			paper.rating_init("paper_clicked");
			paper.similarity();
			paper.rating("browse");
			paper.rating("paper_shown");
			paper.rating("paper_clicked");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
