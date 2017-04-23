package dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static yifan.utils.IOUtils.*;

public class CiteULike {

	private Map<Integer, Integer> items;

	private String dir = "/Users/User/Desktop/yifan/dataset/";
	private String content;

	public CiteULike() {
		items = Maps.newTreeMap();
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		CiteULike cul = new CiteULike();
		// cul.crawler("http://www.citeulike.org/user/irenas/article/42");
		// cul.rating();
		cul.crawler(110993,3330);
		//cul.rating();
		// rating();

	}

	private boolean crawler(String url) throws IOException {
		Document doc = Jsoup.connect(url).timeout(500000).ignoreHttpErrors(true).get();
		Element title = doc.getElementById("article_title");
		Element abs = doc.select("#abstract-body p").first();
		if (title == null || abs == null)
			return false;
		content = title.text() + abs.text();
		return true;
	}

	public void crawler(int start, int num) throws IOException {
		FileWriter writer;
		for (int i = 0; i < num; i++) {
			int n = i + start;
			String url = "http://www.citeulike.org/search/all?q=" + n;
			if (!crawler(url)) {
				console(n + ": empty");
			} else {
				console(n + ": hit");
				writer = new FileWriter(dir + "content", true);
				writer.write(String.format("%d\t%s\n", n, content));
				writer.close();
				// itemset.addProfile(content);
			}
		}
	}

	public void crawler(int max) throws IOException {
		ItemSet itemset = new ItemSet();
		itemset.initial();
		BufferedReader reader = bufferReader(dir + "current");
		String line;
		// List<Integer> list = Lists.newArrayList();
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split("\\|");
			// list.add(Integer.parseInt(terms[0]));
			items.put(Integer.parseInt(terms[0]), 0);
		}
		reader.close();
		FileWriter writer;
		Iterator<Integer> iter = items.keySet().iterator();
		while (iter.hasNext()) {
			if (itemset.size() == max) {
				iter.remove();
				continue;
			}
			int num = iter.next();
			String url = "http://www.citeulike.org/search/all?q=" + num;
			if (!crawler(url)) {
				console(num + ": empty");
				iter.remove();
			} else {
				console(num + ": hit");
				writer = new FileWriter(dir + "content", true);
				writer.write(String.format("%d\t%s\n", num, content));
				writer.close();
				// itemset.addProfile(content);
			}
		}
		itemset.output_local(dir + "feature");
		int i = 1;
		for (Integer key : items.keySet())
			items.put(key, i++);
	}

	public void rating() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = bufferReader(dir + "current");
		String line;
		Map<Integer, Set<Integer>> R = Maps.newTreeMap();
		Map<String, Integer> users = Maps.newHashMap();

		int user = 0;
		int nnz = 0;
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split("\\|");
			if (!users.containsKey(terms[1]))
				users.put(terms[1], ++user);
			int it = Integer.parseInt(terms[0]);
			if (!items.containsKey(it))
				continue;
			int u = users.get(terms[1]);
			int i = items.get(it);
			// console(String.format("u=%d,i=%d", user, item));
			if (!R.containsKey(u)) {
				Set<Integer> set = Sets.newTreeSet();
				R.put(u, set);
			}
			Set<Integer> set = R.get(u);
			if (!set.contains(i)) {
				nnz++;
				set.add(i);
			}
		}
		reader.close();

		BufferedWriter writer = bufferWriter(dir + "rating");
		writer.write("%%MatrixMarket matrix coordinate real general");
		writer.newLine();
		writer.write(String.format("%d %d %d", user, items.size(), nnz));
		writer.newLine();
		for (int u : R.keySet()) {
			Set<Integer> set = R.get(u);
			for (int i : set) {
				writer.write(String.format("%d %d 1", u, i));
				writer.newLine();
			}
		}
		writer.close();
	}

}
