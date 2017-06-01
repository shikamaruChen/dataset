package dataset;

import org.json.JSONObject;

import com.google.common.collect.Maps;

import dataset.helper.ItemSet;

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

	public String dir = "/Users/User/Desktop/yifan/dataset/beauty/";
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

	public void filter(int threshold) throws IOException {

		for (Iterator<Map.Entry<Integer, Set<Integer>>> it = transmap.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, Set<Integer>> entry = it.next();
			if (entry.getValue().size() < threshold)
				it.remove();
		}

		int nnz = 0;
		for (Integer user : transmap.keySet())
			nnz += transmap.get(user).size();

		BufferedWriter writer = bufferWriter(dir + "rating");
		writer.write("%%MatrixMarket matrix coordinate real general");
		writer.newLine();
		writer.write(String.format("%d %d %d", usermap.size(), itemmap.size(), nnz));
		writer.newLine();
		for (int user : transmap.keySet())
			for (int item : transmap.get(user)) {
				writer.write(String.format("%d %d 1", user, item));
				writer.newLine();
			}
		writer.close();
	}

	public void rating(String name) throws IOException {
		console("forming rating matrix");
		BufferedReader reader = bufferReader(dir + name);
		String line;
		int u = 0;
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
		}
		reader.close();
	}

	public static void main(String[] args) throws IOException {
		Amazon amazon = new Amazon();
		amazon.feature("output.strict");
		amazon.rating("ratings_Beauty.csv");
		amazon.filter(3);
	}
}
