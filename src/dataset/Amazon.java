package dataset;

import org.json.JSONObject;

import com.google.common.collect.Maps;

import dataset.helper.ItemSet;
import structure.matrix.SparseMatrix;

import static yifan.utils.IOUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class Amazon {

	public String dir = "";
	private Map<String, Integer> usermap;
	private Map<String, Integer> itemmap;
	private Map<Integer, Set<Integer>> transmap;

	public Amazon() {
		usermap = Maps.newHashMap();
		itemmap = Maps.newHashMap();
		transmap = Maps.newTreeMap();
	}

	public Amazon(String dir) {
		this.dir = dir;
		usermap = Maps.newHashMap();
		itemmap = Maps.newHashMap();
		transmap = Maps.newTreeMap();
	}

	public void feature(String name) throws IOException {
		console("extracting features ...");
		int i = 0;
		BufferedReader reader = bufferReader(dir + name);
		String line = reader.readLine();
		String review;
		ItemSet itemset = new ItemSet();
		itemset.initial();
		JSONObject json = new JSONObject(line);
		String itemId = json.getString("asin");
		itemmap.put(itemId, ++i);
		review = json.getString("reviewText");
		while ((line = reader.readLine()) != null) {
			json = new JSONObject(line);
			itemId = json.getString("asin");
			if (!itemmap.containsKey(itemId)) {
				itemmap.put(itemId, ++i);
				itemset.addProfile(review);
				review = "";
			}
			review += json.getString("reviewText");
		}
		reader.close();
		itemset.addProfile(review);
		itemset.globalWeight();
		itemset.output_local(dir + "feature");
	}

	public void rating(String name) throws IOException {
		console("forming rating matrix");
		BufferedReader reader = bufferReader(dir + name);
		String line;
		int u = 0;
		int nnz = 0;
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(",");
			if (!itemmap.containsKey(terms[1]))
				continue;
			int item = itemmap.get(terms[1]);
			if (!usermap.containsKey(terms[0]))
				usermap.put(terms[0], ++u);
			int user = usermap.get(terms[0]);
			if (!transmap.containsKey(user))
				transmap.put(user, new TreeSet<Integer>());
			transmap.get(user).add(item);
			nnz++;
		}
		reader.close();
		BufferedWriter writer = bufferWriter(dir + "origin");
		writer.write("%%MatrixMarket matrix coordinate real general");
		writer.newLine();
		writer.write(String.format("%d %d %d", u, itemmap.size(), nnz));
		writer.newLine();
		for (int user : transmap.keySet())
			for (int item : transmap.get(user)) {
				writer.write(String.format("%d %d 1", user, item));
				writer.newLine();
			}
		writer.close();
	}

	/**
	 * get a subset of original dataset
	 * 
	 * @param user
	 *            the least number of trans for user to be filtered
	 * @param item
	 *            the least number of trans for item to be filtered
	 * @throws Exception
	 */
	public void filter(int user, int item, int side) throws Exception {
		SparseMatrix rating = SparseMatrix.readMatrix(dir + "origin");
		SparseMatrix feature = SparseMatrix.readMatrix(dir + "feature");
		int[] rows = rating.selectRow(user);
		SparseMatrix sub = rating.selectRow(rows);
		int[] cols = sub.selectColumn(item);
		SparseMatrix ssub = sub.selectColumn(cols);
		ssub.writeMatrix(dir + "rating");
		sub = feature.selectRow(cols);
		cols = sub.selectColumn(side);
		ssub = sub.selectColumn(cols);
		ssub.writeMatrix(dir + "subfeature");
	}

	public static void main(String[] args) throws IOException {
		Amazon amazon = new Amazon("/home/yifan/dataset/beauty/origin/");
		try {
			amazon.filter(8, 5, 30);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
