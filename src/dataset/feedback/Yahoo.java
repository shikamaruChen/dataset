package dataset.feedback;

import static yifan.utils.IOUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dataset.helper.ItemSet;


public class Yahoo {

	private static final String dir = "E:/dataset/yahoo/";

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		process();
//		int fold = 5;
//		for (int i = 1; i <= fold; i++)
//			leave_one_out(i);
		//clean();
	}

	public static void leave_one_out(int i) throws FileNotFoundException,
			IOException {
		BufferedReader reader = bufferReader(dir + "res/rating");
		BufferedWriter train_writer = bufferWriter(dir + "res/train" + i);
		BufferedWriter test_writer = bufferWriter(dir + "res/test" + i);
		String line;
		for (int j = 0; j < 2; j++) {
			line = reader.readLine();
			train_writer.write(line);
			train_writer.newLine();
		}
		int old = 0, uid = 0;
		List<String> ms = Lists.newArrayList();
		Random rand = new Random(System.currentTimeMillis());
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split(" ");
			uid = Integer.parseInt(terms[0]);
			if (uid != old) {
				old = uid;
				if (!ms.isEmpty()) {
					int j = rand.nextInt(ms.size());
					String item = ms.remove(j);
					test_writer.write(String.valueOf(item));
					test_writer.newLine();
					for (String m : ms) {
						train_writer
								.write(String.format("%d %s 1", uid - 1, m));
						train_writer.newLine();
					}
					ms.clear();
				}
			}
			ms.add(terms[1]);
		}
		int j = rand.nextInt(ms.size());
		String item = ms.remove(j);
		test_writer.write(item);
		test_writer.newLine();
		for (String m : ms) {
			train_writer.write(String.format("%d %s 1", uid, m));
			train_writer.newLine();
		}
		reader.close();
		train_writer.close();
		test_writer.close();
	}

	public static void clean() throws FileNotFoundException, IOException{
		BufferedReader reader = bufferReader(dir + "raw/train");
		BufferedWriter writer = bufferWriter(dir+"raw/clean");
		String line;
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split("\t");
			if(!terms[1].equals("0")){
				writer.write(line);
				writer.newLine();
			}
		}
		reader.close();
		writer.close();
	}
	
	
	public static void process() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = bufferReader(dir + "raw/train");
		String line;
		Map<String, Integer> usermap = Maps.newHashMap();//name:String, ID:int
		Map<String, Integer> itemmap = Maps.newHashMap();
		int userid = 1;
		int itemid = 1;
		int nnz = 0;
		while ((line = reader.readLine()) != null) {
			// console(line);
			String[] terms = line.split("\t");
			if(terms[1].equals("0")) console(line);
			int rating = Integer.parseInt(terms[3]);
			if (rating == 5) {
				if (!usermap.containsKey(terms[0]))
					usermap.put(terms[0], userid++);
				if (!itemmap.containsKey(terms[1]))
					itemmap.put(terms[1], itemid++);
				nnz++;
			}
		}
		reader.close();
		reader = bufferReader(dir + "raw/train");
		BufferedWriter writer = bufferWriter(dir + "res/rating");
		writer.write("%%MatrixMarket matrix coordinate real general");
		writer.newLine();
		writer.write(String.format("%d %d %d", userid - 1, itemid - 1, nnz));
		writer.newLine();
		int old = 0;
		Set<Integer> items = Sets.newTreeSet();
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split("\t");
			int rating = Integer.parseInt(terms[3]);
			if (rating == 5) {
				userid = usermap.get(terms[0]);
				if (userid != old) {
					old = userid;
					for (Integer item : items) {
						writer.write(String.format("%d %d 1", userid - 1, item));
						writer.newLine();
					}
					items.clear();
				}
				itemid = itemmap.get(terms[1]);
				items.add(itemid);
			}
		}
		if (!items.isEmpty())
			for (Integer item : items) {
				writer.write(String.format("%d %d 1", userid, item));
				writer.newLine();
			}
		items.clear();
		reader.close();
		writer.close();

		Map<Integer, String> profilemap = Maps.newTreeMap();
		reader = bufferReader(dir + "raw/feature");
		while ((line = reader.readLine()) != null) {
			String[] terms = line.split("\t");
			if (itemmap.containsKey(terms[0])) {
				int id = itemmap.get(terms[0]);
				itemmap.remove(terms[0]);
				profilemap.put(id, terms[1] + ". " + terms[2]);
			}
		}
		//console(itemmap);
		reader.close();
		ItemSet itemset = new ItemSet();
		itemset.initial();
		for (String profile : profilemap.values())
			itemset.addProfile(profile);
		itemset.globalWeight();
		itemset.output_wordlist(dir + "res/corpus");
		itemset.output_local(dir + "res/feature");
	}

	public static void movie() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = bufferReader(dir + "raw/movie");
		String line;
		ItemSet itemset = new ItemSet();
		itemset.initial();
		while ((line = reader.readLine()) != null) {
			String[] params = line.split("\t");
			itemset.addProfile(params[1] + ". " + params[2]);
		}
		reader.close();
		itemset.globalWeight();
		itemset.output_wordlist(dir + "res/corpus");
		itemset.output_local(dir + "res/movie");
	}

}
